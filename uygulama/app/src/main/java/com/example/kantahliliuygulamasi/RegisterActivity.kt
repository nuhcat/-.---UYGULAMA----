package com.example.kantahliliuygulamasi

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val registerButton = findViewById<LinearLayout>(R.id.layoutButtonRegisterSecond)
        val exampleRegisterText = findViewById<TextView>(R.id.tvForgotPassword) // id aynı kalıyor ama anlam değişiyor
        val animPen = findViewById<LottieAnimationView>(R.id.animPen)

        // 🔽 Kayıt butonuna tıklanınca Firebase Authentication ile kullanıcı kaydı yapılır
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Kayıt başarısız: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        // 🔽 "Örnek Kayıt Olma" yazısına tıklanınca OrnekActivity'e geç
        exampleRegisterText.text = "Örnek Kayıt Olma"
        exampleRegisterText.setOnClickListener {
            val intent = Intent(this, OrnekActivity::class.java)
            startActivity(intent)
        }
    }
}
