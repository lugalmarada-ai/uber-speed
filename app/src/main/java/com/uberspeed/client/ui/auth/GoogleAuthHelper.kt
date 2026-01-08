package com.uberspeed.client.ui.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleAuthHelper(private val activity: Activity) {

    companion object {
        private const val TAG = "GoogleAuthHelper"
        // Replace with your Web Client ID from Google Cloud Console
        private const val WEB_CLIENT_ID = "520874678088-1ids5lti5cm8kij8fj581pce00d897hm.apps.googleusercontent.com"
    }

    private var googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onError: (String) -> Unit) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                Log.d(TAG, "Google Sign-In success: ${account.email}")
                onSuccess(account)
            } else {
                onError("No se pudo obtener la cuenta de Google")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed: ${e.statusCode}", e)
            val errorMessage = when (e.statusCode) {
                12501 -> "Inicio de sesión cancelado"
                12502 -> "Inicio de sesión en progreso"
                10 -> "Error de configuración (verifica el Client ID)"
                else -> "Error de Google Sign-In: ${e.statusCode}"
            }
            onError(errorMessage)
        }
    }

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener(activity) {
            Log.d(TAG, "Google Sign-Out completed")
            onComplete()
        }
    }

    fun revokeAccess(onComplete: () -> Unit) {
        googleSignInClient.revokeAccess().addOnCompleteListener(activity) {
            Log.d(TAG, "Google access revoked")
            onComplete()
        }
    }

    fun getCurrentUser(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity)
    }

    fun isSignedIn(): Boolean {
        return getCurrentUser() != null
    }
}
