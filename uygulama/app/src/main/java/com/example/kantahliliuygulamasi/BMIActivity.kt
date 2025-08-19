package com.example.kantahliliuygulamasi

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BMIActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi)

        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnCalculate = findViewById<LinearLayout>(R.id.btnCalculateBMI)
        val tvScore = findViewById<TextView>(R.id.tvBMIScore)
        val tvDescription = findViewById<TextView>(R.id.tvBMIDescription)
        val resultContainer = findViewById<LinearLayout>(R.id.bmiResultContainer)

        btnCalculate.setOnClickListener {
            // ✅ Titreşim başlat
            vibrate()

            val weightText = etWeight.text.toString()
            val heightText = etHeight.text.toString()

            if (weightText.isEmpty() || heightText.isEmpty()) {
                Toast.makeText(this, "Lütfen kilo ve boy bilgilerini girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toFloatOrNull()
            val height = heightText.toFloatOrNull()

            if (weight == null || height == null || height == 0f) {
                Toast.makeText(this, "Geçerli değerler girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightInMeters = height / 100f
            val bmi = weight / (heightInMeters * heightInMeters)

            val category = when {
                bmi < 18.5 -> "Zayıfsınız"
                bmi < 25 -> "Normalsiniz"
                bmi < 30 -> "Kilolusunuz"
                else -> "Obezsiniz"
            }

            val bmiFormatted = String.format("%.1f", bmi)

            resultContainer.visibility = View.VISIBLE
            tvScore.text = bmiFormatted
            tvDescription.text = category
        }
    }

    // ✅ Titreşim fonksiyonu
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    }
}
