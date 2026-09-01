package com.neojelll.diaxtracker.sensor

private const val MG_DL_PER_MMOL_L = 18.0182

fun mgdlToMmol(mgdl: Double): Float = (mgdl / MG_DL_PER_MMOL_L).toFloat()
