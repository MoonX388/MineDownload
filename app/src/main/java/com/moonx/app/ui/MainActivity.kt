package com.moonx.app.ui
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.moonx.app.auth.GoogleAuth
import com.moonx.app.downloader.DownloadQueue


class MainActivity : AppCompatActivity() {
    companion object { private const val REQ_SIGN = 1001 }
    private val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


// Build Google sign in client (replace with your web client id)
        val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
        val client = GoogleAuth.buildClient(this, webClientId)


        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnQueue = findViewById<Button>(R.id.btnAddQueue)
        val etVersionCode = findViewById<EditText>(R.id.etVersionCode)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)


        btnLogin.setOnClickListener {
            startActivityForResult(client.signInIntent, REQ_SIGN)
        }


        btnQueue.setOnClickListener {
            val vc = etVersionCode.text.toString().trim()
            if (vc.isNotEmpty()) {
// For simplicity we'll queue a single item: packageName + versionCode + arch + channel
                DownloadQueue.enqueue(this, "com.mojang.minecraftpe", vc, "arm64-v8a", "release")
                tvStatus.text = "Queued: $vc"
            }
        }


// Show existing signed-in account if present
        GoogleSignIn.getLastSignedInAccount(this)?.let { acc ->
            Log.d(TAG, "Already signed in: ${acc.email}")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SIGN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.result
                val idToken = account.idToken
                if (idToken != null) {
                    GoogleAuth.saveIdToken(this, idToken)
                    Log.d("MainActivity", "Saved id token for later use")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Sign-in failed: ${e.message}")
            }
        }
    }
}