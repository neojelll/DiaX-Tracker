import { describe, expect, it } from 'vitest'
import { afterMealPeak, afterMealPoints, buildAfterMeal, feedRecords } from '@/diary/afterMeal'
import { plusMinutes } from '@/diary/time'
import { newEntry, type SavedEntry } from '@/diary/types'

const DAY = '2026-09-25T12:00:00'
const minutes = (m: number) => plusMinutes(DAY, m)
let nextId = 1

const meal = (at: string, label: string | null = 'Обед'): SavedEntry => ({
  ...newEntry(at, { mealLabel: label, carbsGrams: label == null ? 30 : null }),
  id: nextId++,
})
const check = (
  of: SavedEntry,
  hour: number,
  sugar: number,
  at = plusMinutes(of.createdAt, hour * 60),
): SavedEntry => ({
  ...newEntry(at, { bloodSugar: sugar, sugarSource: 'SENSOR', sourceEntryId: of.id, sourceHour: hour }),
  id: nextId++,
})
const manualReading = (at: string, sugar: number): SavedEntry => ({
  ...newEntry(at, { bloodSugar: sugar, sugarSource: 'MANUAL' }),
  id: nextId++,
})
const oldCheck = (at: string, sugar: number): SavedEntry => ({
  ...newEntry(at, { bloodSugar: sugar, sugarSource: 'SENSOR' }),
  id: nextId++,
})
const line = (entries: SavedEntry[], of: SavedEntry, now: string) =>
  buildAfterMeal(entries, now).byMeal.get(of.id!)?.values

describe('buildAfterMeal', () => {
  it('no line within the first hour', () => {
    const lunch = meal(DAY)
    expect(buildAfterMeal([lunch], minutes(59)).byMeal.size).toBe(0)
  })

  it('the line appears at one hour and fills up as the checks arrive', () => {
    const lunch = meal(DAY)
    const first = check(lunch, 1, 8.4)
    const atOneHour = buildAfterMeal([lunch, first], minutes(60))
    expect(atOneHour.byMeal.get(lunch.id)!.values).toEqual([8.4, null, null, null])
    expect([...atOneHour.hidden]).toEqual([first.id])
    const all = [lunch, first, check(lunch, 2, 7.9), check(lunch, 3, 7.1), check(lunch, 4, 6.6)]
    expect(line(all, lunch, minutes(250))).toEqual([8.4, 7.9, 7.1, 6.6])
  })

  it('a line with no checks yet still appears after an hour', () => {
    const lunch = meal(DAY)
    expect(line([lunch], lunch, minutes(75))).toEqual([null, null, null, null])
  })

  it('two overlapping meals each keep their own four checks', () => {
    const lunch = meal(DAY, 'Обед')
    const dinner = meal(minutes(90), 'Ужин')
    const entries = [
      lunch,
      dinner,
      ...[1, 2, 3, 4].map((h) => check(lunch, h, 6 + h)),
      ...[1, 2, 3, 4].map((h) => check(dinner, h, 9 + h)),
    ]
    const index = buildAfterMeal(entries, minutes(600))
    expect(index.byMeal.get(lunch.id)!.values).toEqual([7, 8, 9, 10])
    expect(index.byMeal.get(dinner.id)!.values).toEqual([10, 11, 12, 13])
    expect(index.hidden.size).toBe(8)
  })

  it('a check belongs to its meal even when stamped at an odd time', () => {
    const lunch = meal(DAY)
    expect(line([lunch, check(lunch, 2, 7.5, minutes(140))], lunch, minutes(300))![1]).toBe(7.5)
  })

  it('manual readings are not part of the line and stay in the feed', () => {
    const lunch = meal(DAY)
    const manual = manualReading(minutes(60), 8)
    const index = buildAfterMeal([lunch, manual], minutes(120))
    expect(index.byMeal.get(lunch.id)!.values).toEqual([null, null, null, null])
    expect(index.hidden.has(manual.id)).toBe(false)
  })

  it('a check whose meal is gone stays an ordinary row', () => {
    const orphan = check(meal(DAY), 1, 8)
    const index = buildAfterMeal([orphan], minutes(600))
    expect(index.byMeal.size).toBe(0)
    expect(index.hidden.has(orphan.id)).toBe(false)
  })

  it('an entry that is no longer a meal has no line', () => {
    const notMeal = { ...meal(DAY), mealLabel: null, carbsGrams: null }
    expect(buildAfterMeal([notMeal], minutes(300)).byMeal.has(notMeal.id)).toBe(false)
  })

  it('old checks without a link are placed by time and leave the feed', () => {
    const lunch = meal(DAY)
    const olds = [
      [60, 8.4],
      [121, 7.9],
      [180, 7.1],
      [240, 6.6],
    ].map(([m, s]) => oldCheck(minutes(m), s))
    const index = buildAfterMeal([lunch, ...olds], minutes(300))
    expect(index.byMeal.get(lunch.id)!.values).toEqual([8.4, 7.9, 7.1, 6.6])
    expect(index.hidden).toEqual(new Set(olds.map((o) => o.id)))
  })

  it('an old check goes to the last meal before it', () => {
    const lunch = meal(DAY, 'Обед')
    const dinner = meal(minutes(150), 'Ужин')
    const index = buildAfterMeal([lunch, dinner, oldCheck(minutes(210), 8)], minutes(600))
    expect(index.byMeal.get(dinner.id)!.values[0]).toBe(8)
    expect(index.byMeal.get(lunch.id)!.values[3]).toBeNull()
  })

  it('a linked check wins its hour over an old one', () => {
    const lunch = meal(DAY)
    expect(line([lunch, check(lunch, 1, 8), oldCheck(minutes(62), 9.9)], lunch, minutes(120))![0]).toBe(8)
  })

  it('an old reading with a note or a manual source is not taken for a check', () => {
    const lunch = meal(DAY)
    const withNote = { ...oldCheck(minutes(60), 8), notes: 'после прогулки' }
    const manual = { ...oldCheck(minutes(120), 7), sugarSource: 'MANUAL' as const }
    expect(buildAfterMeal([lunch, withNote, manual], minutes(300)).hidden.size).toBe(0)
  })
})

describe('feedRecords', () => {
  it('leaves out the checks shown inside a line, and nothing else', () => {
    const lunch = meal(DAY)
    const checks = [1, 2, 3, 4].map((h) => check(lunch, h, 7 + h))
    const reading = manualReading(minutes(150), 6.2)
    const orphanCheck = check(meal(minutes(-300)), 1, 9)
    expect(feedRecords([orphanCheck, lunch, ...checks, reading], minutes(300))).toEqual([
      orphanCheck,
      lunch,
      reading,
    ])
  })
})

describe('afterMealPoints', () => {
  const mealAt = '2026-10-21T14:10:00'

  it('starts at the sugar logged with the meal', () => {
    const points = afterMealPoints(5.9, { values: [8.2, 9.4, 7.6, 6.3] }, mealAt, plusMinutes(mealAt, 300))
    expect(points.map((p) => p.hour)).toEqual([0, 1, 2, 3, 4])
    expect(points.map((p) => p.value)).toEqual([5.9, 8.2, 9.4, 7.6, 6.3])
    expect(afterMealPeak(points)).toBe(2)
  })

  it('a check still ahead shows when it is due', () => {
    const points = afterMealPoints(5.9, { values: [8.2, 9.4, null, null] }, mealAt, plusMinutes(mealAt, 150))
    expect(points[3].dueAt).toBe('17:10')
    expect(points[4].dueAt).toBe('18:10')
    expect(points[1].dueAt).toBeNull()
  })

  it('a check whose time passed without a reading shows nothing', () => {
    const points = afterMealPoints(5.9, { values: [8.2, null, 7.6, 6.3] }, mealAt, plusMinutes(mealAt, 300))
    expect(points[2]).toEqual({ hour: 2, value: null, dueAt: null })
  })

  it('the starting point is never the peak', () => {
    expect(
      afterMealPeak(
        afterMealPoints(12, { values: [8.2, 7.4, null, null] }, mealAt, plusMinutes(mealAt, 150)),
      ),
    ).toBe(1)
    expect(
      afterMealPeak(
        afterMealPoints(6, { values: [null, null, null, null] }, mealAt, plusMinutes(mealAt, 70)),
      ),
    ).toBeNull()
  })
})
