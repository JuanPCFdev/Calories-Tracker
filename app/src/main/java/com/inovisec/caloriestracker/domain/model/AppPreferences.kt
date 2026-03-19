package com.inovisec.caloriestracker.domain.model

data class AppPreferences(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.EN
)