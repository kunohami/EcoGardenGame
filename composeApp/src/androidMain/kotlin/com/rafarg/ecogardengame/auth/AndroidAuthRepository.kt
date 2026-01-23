package com.rafarg.ecogardengame.auth

import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AndroidAuthRepository(private val activity: ComponentActivity) : AuthRepository {

    private val auth = Firebase.auth

    override val currentUser: Flow<UserProfile?> = auth.authStateChanged.map { user ->
        user?.let {
            UserProfile(
                id = it.uid,
                name = it.displayName,
                email = it.email,
                photoUrl = it.photoURL
            )
        }
    }

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("209061921114-a1qm2prhcjapljuh1d9sbh2b57elpcio.apps.googleusercontent.com")
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    val signInIntent get() = googleSignInClient.signInIntent

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        // This is handled via handleSignInResult after the activity result
        return Result.failure(Exception("Use platform intent flow"))
    }

    suspend fun handleSignInResult(account: GoogleSignInAccount): Result<UserProfile> {
        return try {
            val idToken = account.idToken ?: throw Exception("No ID Token found")
            // GitLive Firebase Auth GoogleAuthProvider expects (idToken, accessToken)
            val credential = GoogleAuthProvider.credential(idToken, null)
            val authResult = auth.signInWithCredential(credential)
            val user = authResult.user ?: throw Exception("Firebase user is null")
            
            Result.success(UserProfile(
                id = user.uid,
                name = user.displayName,
                email = user.email,
                photoUrl = user.photoURL
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().await()
    }
}
