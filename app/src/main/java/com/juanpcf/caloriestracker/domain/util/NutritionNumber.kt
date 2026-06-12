package com.juanpcf.caloriestracker.domain.util

import java.util.Locale

/**
 * Parsing y formateo de números nutricionales. Centralizado en domain (sin dependencias de Android)
 * para no duplicarlo en ViewModels. Locale-aware: acepta coma decimal en la entrada y formatea con
 * punto (Locale.US) en la salida.
 */

/** Parsea un decimal aceptando coma como separador. Para campos controlados (ej. porciones). */
fun String.toDecimalOrNull(): Double? =
    trim().replace(",", ".").toDoubleOrNull()

/**
 * Parsea un valor nutricional tipeado libremente: acepta coma decimal y descarta cualquier otro
 * carácter no numérico (unidades, espacios, etc.). Para campos de texto libre (calorías, macros).
 */
fun String.toNutrientOrNull(): Double? =
    trim().replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull()

/** Formatea un nutriente: entero sin decimales; si no, a 1 decimal con punto. */
fun formatNutrient(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.US, "%.1f", value)

/** Formatea cantidad de porciones: entero sin decimales; si no, su representación natural. */
fun formatServings(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else value.toString()
