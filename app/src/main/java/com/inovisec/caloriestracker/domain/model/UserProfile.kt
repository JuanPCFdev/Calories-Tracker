package com.inovisec.caloriestracker.domain.model

import java.time.Instant

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val createdAt: Instant
)