package com.rafarg.ecogardengame.auth

import kotlinx.coroutines.flow.Flow

data class UserProfile(
    val id: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?
)

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun signOut()
}
