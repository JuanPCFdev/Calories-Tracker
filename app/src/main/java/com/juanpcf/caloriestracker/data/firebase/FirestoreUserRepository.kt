package com.juanpcf.caloriestracker.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.juanpcf.caloriestracker.domain.model.UserGoals
import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import com.juanpcf.caloriestracker.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(userId: String) =
        firestore.collection("users").document(userId)

    suspend fun writeUserGoals(goals: UserGoals) {
        val data = mapOf(
            "dailyCalories" to goals.dailyCalories,
            "dailyProtein" to goals.dailyProtein,
            "dailyCarbs" to goals.dailyCarbs,
            "dailyFat" to goals.dailyFat,
            "dailySugar" to goals.dailySugar
        )
        userDoc(goals.userId).collection("goals").document("current").set(data).await()
    }

    suspend fun writeUserPhysicalProfile(profile: UserPhysicalProfile) {
        val data = mapOf(
            "heightCm" to profile.heightCm,
            "weightKg" to profile.weightKg,
            "birthDate" to profile.birthDate.toEpochDay(),
            "gender" to profile.gender.name,
            "activityLevel" to profile.activityLevel.name
        )
        userDoc(profile.userId).collection("physicalProfile").document("current").set(data).await()
    }

    suspend fun writeUserProfile(profile: UserProfile) {
        val data = mapOf(
            "email" to profile.email,
            "displayName" to profile.displayName,
            "photoUrl" to profile.photoUrl,
            "createdAt" to profile.createdAt.toEpochMilli()
        )
        userDoc(profile.uid).set(data).await()
    }
}
