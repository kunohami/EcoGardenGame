package com.rafarg.ecogardengame.auth

import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * --- ANDROID AUTHENTICATION REPOSITORY ---
 * Implements Google Sign-In and Firebase Authentication for Android.
 */
class AndroidAuthRepository(private val activity: ComponentActivity) : AuthRepository {

    private val auth = Firebase.auth
    
    /**
     * --- REACTIVE STATE ---
     * Firebase's authStateChanged can sometimes be slow to emit after a credential login.
     * We use a MutableStateFlow to manually trigger a UI update the moment login succeeds.
     */
    private val manualUserUpdate = MutableStateFlow<UserProfile?>(null)

    /**
     * Combines the official Firebase auth stream with our manual trigger.
     * This ensures the UI is always up-to-date.
     */
    override val currentUser: Flow<UserProfile?> = combine(
        auth.authStateChanged.map { user ->
            user?.let {
                UserProfile(
                    id = it.uid,
                    name = it.displayName,
                    email = it.email,
                    photoUrl = it.photoURL
                )
            }
        },
        manualUserUpdate
    ) { firebaseUser, manualUser ->
        // Priority: if we just logged in manually, use that info until Firebase catches up.
        manualUser ?: firebaseUser
    }

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("209061921114-a1qm2prhcjapljuh1d9sbh2b57elpcio.apps.googleusercontent.com")
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    val signInIntent get() = googleSignInClient.signInIntent

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        // Handled via activity result in MainActivity
        return Result.failure(Exception("Use platform intent flow"))
    }

    /**
     * Processes the result from the Google Sign-In Activity.
     * Links the Google Account with Firebase Auth.
     */
    suspend fun handleSignInResult(account: GoogleSignInAccount): Result<UserProfile> {
        return try {
            val idToken = account.idToken ?: throw Exception("No ID Token found")
            val credential = GoogleAuthProvider.credential(idToken, null)
            
            // Perform the Firebase login
            val authResult = auth.signInWithCredential(credential)
            val user = authResult.user ?: throw Exception("Firebase user is null")
            
            val profile = UserProfile(
                id = user.uid,
                name = user.displayName,
                email = user.email,
                photoUrl = user.photoURL
            )
            

            // Manually emit the new profile to force the UI to react instantly.
            manualUserUpdate.value = profile
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears both Firebase and Google sessions.
     */
    override suspend fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().await()
        // Reset manual flow
        manualUserUpdate.value = null
    }
}
