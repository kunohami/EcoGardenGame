package com.rafarg.ecogardengame.auth

import kotlinx.coroutines.flow.Flow

/**
 * --- DATA MODEL: USER PROFILE ---
 * A simple data class that represents the authenticated user's information.
 * Data classes in Kotlin are designed to hold data and automatically provide
 * useful methods like 'equals()', 'hashCode()', and 'toString()'.
 */
data class UserProfile(
    val id: String, // Unique identifier (usually from Firebase or Google)
    val name: String?, // Display name, nullable because it might not be set
    val email: String?, // User's email address
    val photoUrl: String?, // URL to the user's profile picture
)

/**
 * --- INTERFACE ARCHITECTURE (Abstraction) ---
 * AuthRepository defines the "What" but not the "How".
 * This is a core principle of clean architecture: the UI and Business Logic
 * shouldn't care if we use Google, Facebook, or a custom login system.
 *
 * Implementation of this interface will be platform-specific (Android/iOS)
 * or service-specific (Firebase).
 */
interface AuthRepository {
    /**
     * --- REACTIVE STREAMS (Flow) ---
     * A 'Flow' is like a pipe that emits values over time.
     * Here, it emits the current UserProfile whenever the login state changes.
     * The UI can "collect" this flow to show or hide the login screen automatically.
     */
    val currentUser: Flow<UserProfile?>

    /**
     * Executes the Google Sign-In process.
     * Returns a 'Result' which is a standard Kotlin way to handle success or failure
     * without using try-catch blocks everywhere in the UI.
     */
    suspend fun signInWithGoogle(): Result<UserProfile>

    /**
     * Signs the user out and clears any active authentication session.
     */
    suspend fun signOut()
}
