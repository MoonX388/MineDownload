package com.moonx.app.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    /**
     * Get Google accounts from device without needing Web Client ID
     */
    fun getDeviceGoogleAccounts(context: Context): Array<Account> {
        val accountManager = AccountManager.get(context)
        return accountManager.getAccountsByType("com.google")
    }

    /**
     * Get auth token from device Google account
     * This runs on background thread (IO)
     * Scope: Using basic email scope that doesn't require API Console registration
     */
    suspend fun getTokenFromDeviceAccount(context: Context, account: Account, scope: String = "oauth2:email"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val token = GoogleAuthUtil.getToken(context, account, scope)
                Log.d(TAG, "Got token from device account: ${account.name}")
                // Save to encrypted prefs
                saveIdToken(context, token)
                token
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get token from device account: ${e.message}")
                null
            }
        }
    }

    /**
     * Get token from first available Google account on device
     */
    suspend fun getTokenFromFirstAccount(context: Context): String? {
        val accounts = getDeviceGoogleAccounts(context)
        if (accounts.isEmpty()) {
            Log.e(TAG, "No Google accounts found on device")
            return null
        }
        Log.d(TAG, "Found ${accounts.size} Google account(s), using: ${accounts[0].name}")
        return getTokenFromDeviceAccount(context, accounts[0])
    }
}