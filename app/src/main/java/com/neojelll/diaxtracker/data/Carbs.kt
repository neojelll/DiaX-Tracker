package com.neojelll.diaxtracker.data

import kotlin.math.roundToInt

/*
 * Carbohydrates are stored in grams - the physical quantity. Bread units (XE) are only a way of
 * showing and typing them; every screen converts through here, none does its own arithmetic.
 */

/** Grams in one XE. The diary's XE were entered at 10 g, which is also what migration 11 -> 12 used. */
const val DEFAULT_GRAMS_PER_XE = 10f

fun gramsToXe(grams: Float, gramsPerXe: Float = DEFAULT_GRAMS_PER_XE): Float = grams / gramsPerXe

fun xeToGrams(xe: Float, gramsPerXe: Float = DEFAULT_GRAMS_PER_XE): Float = xe * gramsPerXe

/**
 * XE to one decimal, without a trailing ".0": "3", "2.5", "2.1". A dot, not the locale's comma:
 * the same text goes back into the input fields, which parse dots.
 */
fun formatXe(grams: Float, gramsPerXe: Float = DEFAULT_GRAMS_PER_XE): String {
    val tenths = (gramsToXe(grams, gramsPerXe) * 10).roundToInt()
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
}

/** Typed XE ("2.5" or "2,5") to grams; null if it isn't a number. */
fun parseXeToGrams(text: String, gramsPerXe: Float = DEFAULT_GRAMS_PER_XE): Float? =
    text.replace(',', '.').toFloatOrNull()?.let { xeToGrams(it, gramsPerXe) }
