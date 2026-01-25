package com.rafarg.ecogardengame.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import platform.UIKit.UIViewController
// Nota: Estas clases vendrán del Pod de GoogleSignIn cuando se instale en un Mac
// import cocoapods.GoogleSignIn.* 

class IosAuthRepository(private val rootViewController: UIViewController) : AuthRepository {

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

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        // En iOS, esto normalmente requiere llamar a GIDSignIn.sharedInstance.signIn
        // Como no podemos compilar las interops de C sin Mac, dejamos el placeholder
        // funcional para cuando se abra en un entorno Apple.
        return Result.failure(Exception("iOS Google Sign-In requires Mac compilation context"))
    }

    override suspend fun signOut() {
        auth.signOut()
        // GIDSignIn.sharedInstance.signOut()
    }
}
