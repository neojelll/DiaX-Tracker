import { describe, expect, it } from 'vitest'
import {
  activeGoalGrams,
  carbGoalProgress,
  carbGoalReached,
  carbGoalStepGrams,
  MAX_CARB_GOAL_GRAMS,
  stepCarbGoal,
} from '@/diary/carbGoal'
import type { CarbDisplay } from '@/diary/carbs'

const xe12: CarbDisplay = { unit: 'XE', gramsPerXe: 12 }
const grams: CarbDisplay = { unit: 'GRAMS', gramsPerXe: 10 }

describe('carb goal', () => {
  it('is in effect only when switched on and above zero', () => {
    expect(activeGoalGrams({ enabled: true, grams: 150 })).toBe(150)
    expect(activeGoalGrams({ enabled: false, grams: 150 })).toBeNull()
    expect(activeGoalGrams({ enabled: true, grams: null })).toBeNull()
    expect(activeGoalGrams({ enabled: true, grams: 0 })).toBeNull()
  })

  it('progress runs from empty to full and stays full past the goal', () => {
    expect(carbGoalProgress(0, 150)).toBe(0)
    expect(carbGoalProgress(75, 150)).toBeCloseTo(0.5)
    expect(carbGoalProgress(150, 150)).toBe(1)
    expect(carbGoalProgress(200, 150)).toBe(1)
  })

  it('is reached at it and past it', () => {
    expect(carbGoalReached(149, 150)).toBe(false)
    expect(carbGoalReached(150, 150)).toBe(true)
    expect(carbGoalReached(180, 150)).toBe(true)
  })

  it('a step is half an XE in XE and 5 g in grams', () => {
    expect(carbGoalStepGrams(xe12)).toBe(6)
    expect(carbGoalStepGrams({ unit: 'XE', gramsPerXe: 10 })).toBe(5)
    expect(carbGoalStepGrams(grams)).toBe(5)
  })

  it('stepping starts from zero when unset and stays within bounds', () => {
    expect(stepCarbGoal(null, 1, xe12)).toBe(6)
    expect(stepCarbGoal(150, 1, xe12)).toBe(156)
    expect(stepCarbGoal(3, -1, xe12)).toBe(0)
    expect(stepCarbGoal(998, 1, grams)).toBe(MAX_CARB_GOAL_GRAMS)
  })
})
