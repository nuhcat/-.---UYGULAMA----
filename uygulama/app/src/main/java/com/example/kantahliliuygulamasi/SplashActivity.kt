package com.example.kantahliliuygulamasi

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase başlatılıyor
        FirebaseApp.initializeApp(this)

        // Splash ekranı layout gösteriliyor
        setContentView(R.layout.activity_splash)

        // 2 saniye bekleyip isLoggedIn kontrolü yapılıyor
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            // Debug amaçlı Toast
            Toast.makeText(
                this,
                "isLoggedIn = $isLoggedIn → ${if (isLoggedIn) "MainActivity" else "LoginActivity"}",
                Toast.LENGTH_SHORT
            ).show()

            // Giriş durumuna göre yönlendirme
            if (isLoggedIn) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
