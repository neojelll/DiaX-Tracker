import { xeToGrams, type CarbDisplay } from '@/diary/carbs'

/**
 * A personal daily carbohydrate goal: something to reach, not a limit - going past it is fine.
 * [grams] is null until the person types a number; there is no default on purpose, since any
 * preset value would read as a recommendation.
 */
export interface CarbGoal {
  enabled: boolean
  grams: number | null
}

export const DEFAULT_CARB_GOAL: CarbGoal = { enabled: false, grams: null }

/** Upper bound for a typed goal - guards against a stray extra digit, not an editorial limit. */
export const MAX_CARB_GOAL_GRAMS = 1000

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))

/** The goal in effect: switched on and set to more than zero; null otherwise. */
export function activeGoalGrams(goal: CarbGoal): number | null {
  return goal.enabled && goal.grams != null && goal.grams > 0 ? goal.grams : null
}

/** How far [eatenGrams] is toward [goalGrams], 0..1; past the goal stays full. */
export function carbGoalProgress(eatenGrams: number, goalGrams: number): number {
  return clamp(eatenGrams / goalGrams, 0, 1)
}

export function carbGoalReached(eatenGrams: number, goalGrams: number): boolean {
  return eatenGrams >= goalGrams
}

/** One −/+ step in grams: half an XE when carbs are shown in XE, 5 g when in grams. */
export function carbGoalStepGrams(display: CarbDisplay): number {
  return display.unit === 'XE' ? xeToGrams(0.5, display.gramsPerXe) : 5
}

/** [grams] moved by [steps] −/+ steps, kept within 0..MAX; from "not set" it starts at 0. */
export function stepCarbGoal(grams: number | null, steps: number, display: CarbDisplay): number {
  return clamp((grams ?? 0) + steps * carbGoalStepGrams(display), 0, MAX_CARB_GOAL_GRAMS)
}
