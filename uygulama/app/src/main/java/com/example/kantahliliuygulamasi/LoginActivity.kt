package com.example.kantahliliuygulamasi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kantahliliuygulamasi.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private val PREFS_NAME = "prefs"
    private val KEY_REMEMBER_ME = "rememberMe"
    private val KEY_SAVED_EMAIL = "savedEmail"
    private val KEY_SAVED_PASSWORD = "savedPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        applyAnimations()
        loadRememberMe()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("LoginActivity", "Kullanıcı zaten giriş yapmış: ${currentUser.email}")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Log.d("LoginActivity", "Giriş yapılmamış")
            binding.layoutButtonLogout.isEnabled = false
        }
    }

    private fun applyAnimations() {
        val slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left)
        binding.tvAppName.startAnimation(slideInLeft)

        val slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right)
        binding.loginCardLayout.startAnimation(slideInRight)

        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        binding.layoutButtonLogin.startAnimation(scaleUp)

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.layoutButtonRegister.startAnimation(fadeIn)
    }

    private fun setupClickListeners() {
        binding.layoutButtonLogin.setOnClickListener {
            val email = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.editTextUsername.error = "E-posta boş olamaz"
                binding.editTextUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editTextPassword.error = "Şifre boş olamaz"
                binding.editTextPassword.requestFocus()
                return@setOnClickListener
            }

            signIn(email, password)
        }

        binding.layoutButtonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.textForgotPassword.setOnClickListener {
            val email = binding.editTextUsername.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Lütfen şifrenizi sıfırlamak için e-posta adresinizi girin",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            sendPasswordResetEmail(email)
        }

        binding.layoutButtonLogout.setOnClickListener {
            auth.signOut()
            clearSavedCredentials()
            binding.layoutButtonLogout.isEnabled = false
            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
        }

        binding.checkBoxRememberMe.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                clearSavedCredentials()
                Log.d("LoginActivity", "RememberMe unchecked, credentials cleared")
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveRememberMe(email, password, binding.checkBoxRememberMe.isChecked)
                    Toast.makeText(this, "Giriş başarılı! Yönlendiriliyor...", Toast.LENGTH_SHORT).show()

                    // CountDownTimer başlatılıyor (3 saniye)
                    object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val secondsLeft = millisUntilFinished / 1000
                            Toast.makeText(
                                this@LoginActivity,
                                "Anasayfaya yönlendiriliyor... $secondsLeft",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onFinish() {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }.start()
                } else {
                    Toast.makeText(
                        this,
                        "Giriş başarısız: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Şifre sıfırlama e-postası gönderildi",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Şifre sıfırlama başarısız: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveRememberMe(email: String, password: String, rememberMe: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            if (rememberMe) {
                putString(KEY_SAVED_EMAIL, email)
                putString(KEY_SAVED_PASSWORD, password)
                Log.d("LoginActivity", "Credentials saved: $email / $password")
            } else {
                remove(KEY_SAVED_EMAIL)
                remove(KEY_SAVED_PASSWORD)
                Log.d("LoginActivity", "Credentials removed")
            }
            apply()
        }
    }

    private fun loadRememberMe() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        Log.d("LoginActivity", "RememberMe loaded: $rememberMe")
        binding.checkBoxRememberMe.isChecked = rememberMe
        if (rememberMe) {
            val savedEmail = prefs.getString(KEY_SAVED_EMAIL, "")
            val savedPassword = prefs.getString(KEY_SAVED_PASSWORD, "")
            Log.d("LoginActivity", "Saved email: $savedEmail, password: $savedPassword")
            binding.editTextUsername.setText(savedEmail)
            binding.editTextPassword.setText(savedPassword)
        }
    }

    private fun clearSavedCredentials() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            remove(KEY_REMEMBER_ME)
            remove(KEY_SAVED_EMAIL)
            remove(KEY_SAVED_PASSWORD)
            apply()
        }
        binding.checkBoxRememberMe.isChecked = false
        binding.editTextUsername.text.clear()
        binding.editTextPassword.text.clear()
    }
}
