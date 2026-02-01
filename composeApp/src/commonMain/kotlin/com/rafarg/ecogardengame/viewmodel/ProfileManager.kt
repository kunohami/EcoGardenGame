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

@OptIn(ExperimentalTime::class)
class ProfileManager(
    private val scope: CoroutineScope,
    private val authRepository: AuthRepository?
) {
    var currentUser by mutableStateOf<UserProfile?>(null)
        private set

    var username by mutableStateOf("Farmer")
    var profileImageId by mutableStateOf("tomato")
    var lastProfileUpdateTime by mutableStateOf(0L)

    var searchResults = mutableStateListOf<PublicProfile>()
        private set
    var isSearching by mutableStateOf(false)
        private set

    init {
        observeAuth()
    }

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

    fun updateUsername(newName: String) {
        username = newName
    }

    fun updateProfileImage(imageId: String) {
        profileImageId = imageId
    }

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

    fun searchPlayers(query: String) {
        val cleanedQuery = query.trim()
        if (cleanedQuery.isBlank()) return
        
        isSearching = true
        searchResults.clear()
        
        scope.launch {
            try {
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
