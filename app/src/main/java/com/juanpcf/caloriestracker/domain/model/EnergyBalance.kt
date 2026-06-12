package com.juanpcf.caloriestracker.domain.model

/**
 * Balance energético del día: lo consumido (del diario) contra lo quemado (TDEE estimado o medido).
 * [burnedIsEstimated] distingue el TDEE por fórmula (Fase 3) del dato real de un wearable (Fase 4).
 */
data class EnergyBalance(
    val consumed: Double,
    val burned: Double,
    val burnedIsEstimated: Boolean = true
) {
    /** Neto = consumido − quemado. Positivo = superávit; negativo = déficit. */
    val net: Double get() = consumed - burned
}
