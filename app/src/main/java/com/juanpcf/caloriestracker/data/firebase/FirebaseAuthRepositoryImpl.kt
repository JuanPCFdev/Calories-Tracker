package com.juanpcf.caloriestracker.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.juanpcf.caloriestracker.domain.model.UserProfile
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreUserRepository: FirestoreUserRepository
) : AuthRepository {

    override val currentUser: UserProfile?
        get() = auth.currentUser?.toProfile()

    override val authState: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toProfile()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> =
        runCatching {
            val profile = auth.signInWithEmailAndPassword(email, password).await().user!!.toProfile()
            runCatching { firestoreUserRepository.writeUserProfile(profile) }
            profile
        }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val profile = auth.signInWithCredential(credential).await().user!!.toProfile()
            runCatching { firestoreUserRepository.writeUserProfile(profile) }
            profile
        }

    override suspend fun registerWithEmail(email: String, password: String): Result<UserProfile> =
        runCatching {
            val profile = auth.createUserWithEmailAndPassword(email, password).await().user!!.toProfile()
            runCatching { firestoreUserRepository.writeUserProfile(profile) }
            profile
        }

    override suspend fun signOut() = auth.signOut()

    private fun com.google.firebase.auth.FirebaseUser.toProfile() = UserProfile(
        uid = uid,
        email = email ?: "",
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        createdAt = Instant.now()
    )
}
