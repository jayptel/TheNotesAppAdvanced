package com.rhythm.thenotesapp.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.Status
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.SheetsScopes

class GoogleSignInHelper(private val context: Context) {
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS), Scope("https://www.googleapis.com/auth/drive.file"))
            //.requestIdToken("Client id webapplication")
            .requestIdToken("Client id webapplication") // this code get into google console  where you use create
            // - webapplication  and android for OAth 2.0 but use here app webapplication clicent id
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        Log.d("GoogleSignInHelper", "Creating sign-in intent")
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("GoogleSignInHelper", "Handling sign-in result")
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GoogleSignInHelper", "Sign-in successful: ${account?.email}")
            if (account != null) {
                Log.d("GoogleSignInHelper", "Account details: displayName=${account.displayName}, email=${account.email}, idToken=${account.idToken}")
                onSuccess(account)
            } else {
                Log.e("GoogleSignInHelper", "Account is null")
                onFailure(ApiException(Status(Activity.RESULT_CANCELED)))
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignInHelper", "Sign-in failed: ${e.statusCode}", e)
            onFailure(e)
        }
    }


    fun getCredential(account: GoogleSignInAccount): GoogleAccountCredential {
        Log.d("GoogleSignInHelper", "Getting credential for account: ${account.email}")
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(SheetsScopes.SPREADSHEETS, "https://www.googleapis.com/auth/drive.file")
        ).setSelectedAccountName(account.email)
    }
}
