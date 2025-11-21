package com.moonx.app.auth


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


object GoogleAuth {
    private const val TAG = "GoogleAuth"
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_ID_TOKEN = "id_token"


    private var client: GoogleSignInClient? = null


    fun buildClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
// request minimal scope; PLAY internal scopes are not requested here
            .requestScopes(Scope("https://www.googleapis.com/auth/androidpublisher"))
            .requestIdToken(webClientId)
            .build()
        client = GoogleSignIn.getClient(context, gso)
        return client!!
    }


    fun saveIdToken(context: Context, idToken: String) {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.edit().putString(KEY_ID_TOKEN, idToken).apply()
        Log.d(TAG, "ID Token saved in EncryptedSharedPreferences")
    }


    fun getIdToken(context: Context): String? {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return prefs.getString(KEY_ID_TOKEN, null)
    }
}