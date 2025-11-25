package com.moonx.app.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.moonx.app.R

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION = 2500L // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        // Animate logo card
        val cardLogo = findViewById<CardView>(R.id.cardLogo)
        animateLogoEntrance(cardLogo)

        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            // Add fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DURATION)
    }

    private fun animateLogoEntrance(view: View) {
        // Start from small and invisible
        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 0f

        // Animate scale and alpha
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Add rotation animation
        val rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        rotateAnimator.duration = 1000
        rotateAnimator.startDelay = 200
        rotateAnimator.start()
    }
}
