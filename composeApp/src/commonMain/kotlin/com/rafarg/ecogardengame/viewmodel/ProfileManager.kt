package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rafarg.ecogardengame.auth.AuthRepository
import com.rafarg.ecogardengame.auth.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Manages the user's profile, including authentication state, username, 
 * profile image, and public profile synchronization with Firebase.
 */
@OptIn(ExperimentalTime::class)
class ProfileManager(
    private val scope: CoroutineScope,
    private val authRepository: AuthRepository?
) {
    /** The currently authenticated user, or null if not signed in. */
    var currentUser by mutableStateOf<UserProfile?>(null)
        private set

    /** The user's display name, defaults to "Farmer". */
    var username by mutableStateOf("Farmer")
    
    /** The ID of the selected profile image/avatar. */
    var profileImageId by mutableStateOf("tomato")
    
    /** Timestamp of the last time the public profile was updated to enforce cooldown. */
    var lastProfileUpdateTime by mutableStateOf(0L)

    /** List of profiles found during a player search. */
    var searchResults = mutableStateListOf<PublicProfile>()
        private set
    
    /** Indicates if a search operation is currently in progress. */
    var isSearching by mutableStateOf(false)
        private set

    init {
        observeAuth()
    }

    /**
     * Observes changes in the authentication state from the AuthRepository.
     */
    private fun observeAuth() {
        scope.launch {
            authRepository?.currentUser?.collect { user ->
                currentUser = user
                if (user != null && username == "Farmer") {
                    username = user.name ?: "Farmer"
                }
            }
        }
    }

    /** Updates the local username. */
    fun updateUsername(newName: String) {
        username = newName
    }

    /** Updates the local profile image ID. */
    fun updateProfileImage(imageId: String) {
        profileImageId = imageId
    }

    /**
     * Synchronizes the user's local profile data with the Firebase Firestore public database.
     * Enforces a 60-second cooldown between updates.
     * 
     * @param unlockedAchievements List of achievement IDs to share publicly.
     * @param onSuccess Callback invoked when the update is successful.
     * @param onError Callback invoked with an error message (cooldown remaining or generic error).
     */
    fun updatePublicProfile(
        unlockedAchievements: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser ?: return
        val now = Clock.System.now().toEpochMilliseconds()
        
        if (now - lastProfileUpdateTime < 60000L) {
            onError(((60000L - (now - lastProfileUpdateTime)) / 1000).toInt().toString())
            return
        }

        scope.launch {
            try {
                Firebase.firestore.collection("users").document(user.id).set(
                    mapOf(
                        "username" to username,
                        "profileImageId" to profileImageId,
                        "achievements" to unlockedAchievements
                    ),
                    merge = true
                )
                lastProfileUpdateTime = now
                onSuccess()
            } catch (e: Exception) {
                onError("error")
            }
        }
    }

    /**
     * Searches for other players by username in the Firestore database.
     * @param query The search term.
     */
    fun searchPlayers(query: String) {
        val cleanedQuery = query.trim()
        if (cleanedQuery.isBlank()) return
        
        isSearching = true
        searchResults.clear()
        
        scope.launch {
            try {
                // Search both exact casing and lowercase for better results
                val searchTerms = listOf(cleanedQuery, cleanedQuery.lowercase()).distinct()
                searchTerms.forEach { term ->
                    val result = Firebase.firestore.collection("users")
                        .where { "username" greaterThanOrEqualTo term }
                        .where { "username" lessThanOrEqualTo term + "\uf8ff" }
                        .get()
                    
                    result.documents.forEach { doc ->
                        if (searchResults.none { it.id == doc.id }) {
                            val uname = doc.get<String?>("username") ?: "Unknown"
                            val pId = doc.get<String?>("profileImageId") ?: "tomato"
                            val achs = try { doc.get<List<String>?>("achievements") ?: emptyList() } catch (e: Exception) { emptyList() }
                            
                            searchResults.add(PublicProfile(doc.id, uname, pId, achs))
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail search
            } finally {
                isSearching = false
            }
        }
    }
}
