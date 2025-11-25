package com.moonx.app.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import com.moonx.app.R
import com.moonx.app.auth.GoogleAuth
import com.moonx.app.downloader.DownloadQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Elements
        val cardHeader = findViewById<CardView>(R.id.cardHeader)
        val cardLogin = findViewById<CardView>(R.id.cardLogin)
        val cardDownload = findViewById<CardView>(R.id.cardDownload)
        val cardStatus = findViewById<CardView>(R.id.cardStatus)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnQueue = findViewById<Button>(R.id.btnAddQueue)
        val etVersionCode = findViewById<TextInputEditText>(R.id.etVersionCode)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        
        // Animate cards on startup
        animateCardEntrance(cardHeader, 0)
        animateCardEntrance(cardLogin, 100)
        animateCardEntrance(cardDownload, 200)
        animateCardEntrance(cardStatus, 300)
        
        // Set initial status
        tvStatus.text = getString(R.string.status_ready)
        
        // Login button with animation
        btnLogin.setOnClickListener {
            animateButtonClick(it)
            tvStatus.text = getString(R.string.status_getting_token)
            
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val token = GoogleAuth.getTokenFromFirstAccount(this@MainActivity)
                    
                    if (token != null) {
                        withContext(Dispatchers.Main) {
                            tvStatus.text = getString(R.string.status_logged_in, token.take(20))
                            Toast.makeText(this@MainActivity, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show()
                            animateSuccess(cardLogin)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            tvStatus.text = getString(R.string.status_login_failed)
                            Toast.makeText(this@MainActivity, getString(R.string.toast_no_google_account), Toast.LENGTH_SHORT).show()
                            animateError(cardLogin)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvStatus.text = getString(R.string.status_error, e.message ?: "Unknown")
                        Log.e(TAG, getString(R.string.log_login_error), e)
                        animateError(cardLogin)
                    }
                }
            }
        }

        // Add to download queue with animation
        btnQueue.setOnClickListener {
            animateButtonClick(it)
            val vc = etVersionCode.text.toString().trim()

            if (vc.isNotEmpty()) {
                DownloadQueue.enqueue(
                    this,
                    "com.mojang.minecraftpe",  // Minecraft package
                    vc,                        // Version code
                    "arm64-v8a",               // Architecture
                    "release"                  // Channel
                )

                tvStatus.text = getString(R.string.status_queued, vc)
                Log.d(TAG, getString(R.string.log_queued_version, vc))
                animateSuccess(cardDownload)
                Toast.makeText(this, getString(R.string.toast_added_to_queue), Toast.LENGTH_SHORT).show()
            } else {
                tvStatus.text = getString(R.string.status_version_empty)
                animateError(cardDownload)
                Toast.makeText(this, getString(R.string.toast_enter_version_code), Toast.LENGTH_SHORT).show()
            }
        }

    }
    
    // Animasi entrance untuk cards
    private fun animateCardEntrance(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = 50f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(delay)
            .setInterpolator(OvershootInterpolator())
            .start()
    }
    
    // Animasi button click
    private fun animateButtonClick(view: View) {
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 0.9f)
        val scaleDown2 = ObjectAnimator.ofFloat(view, "scaleY", 0.9f)
        scaleDown.duration = 100
        scaleDown2.duration = 100
        scaleDown.start()
        scaleDown2.start()
        
        view.postDelayed({
            val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f)
            val scaleUp2 = ObjectAnimator.ofFloat(view, "scaleY", 1f)
            scaleUp.duration = 100
            scaleUp2.duration = 100
            scaleUp.start()
            scaleUp2.start()
        }, 100)
    }
    
    // Animasi success (pulse green)
    private fun animateSuccess(view: View) {
        val originalElevation = (view as? CardView)?.cardElevation ?: 0f
        val animator = ObjectAnimator.ofFloat(view, "cardElevation", originalElevation, originalElevation + 8f, originalElevation)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    // Animasi error (shake)
    private fun animateError(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 500
        animator.start()
    }
}
