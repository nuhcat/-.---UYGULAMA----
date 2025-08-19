package com.example.kantahliliuygulamasi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kantahliliuygulamasi.databinding.ActivityYeniBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class YeniActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYeniBinding

    private val testNames = listOf("WBC", "RBC", "Lenfosit", "Hemoglobin", "MCH", "Platelet", "Hematokrit", "RDW")
    private var currentIndex = 0
    private val testValues = mutableMapOf<String, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYeniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateUI()

        binding.buttonNext.setOnClickListener {
            saveCurrentValue(binding.editTextValue.text.toString())
            if (currentIndex < testNames.size - 1) {
                currentIndex++
                updateUI()
            } else {
                Toast.makeText(this, "Son değere ulaşıldı", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonBack.setOnClickListener {
            saveCurrentValue(binding.editTextValue.text.toString())
            if (currentIndex > 0) {
                currentIndex--
                updateUI()
            } else {
                Toast.makeText(this, "İlk değere geldiniz", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonGonder.setOnClickListener {
            saveCurrentValue(binding.editTextValue.text.toString())
            val jsonObject = JSONObject(testValues as Map<*, *>)
            sendDataToApi(jsonObject)
        }
    }

    private fun updateUI() {
        val currentTest = testNames[currentIndex]
        binding.textViewTitle.text = currentTest
        binding.editTextValue.setText(testValues[currentTest]?.toString() ?: "")
    }

    private fun saveCurrentValue(value: String) {
        val key = testNames[currentIndex]
        val number = value.toDoubleOrNull()
        if (number != null) {
            testValues[key] = number
        }
    }

    private fun sendDataToApi(jsonData: JSONObject) {
        val JSONMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = RequestBody.create(JSONMediaType, jsonData.toString())

        val request = Request.Builder()
            .url("https://senin-api-url.com/analyze") // <<<<< BURAYI DEĞİŞTİR
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@YeniActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("API", "İstek başarısız: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread {
                    Toast.makeText(this@YeniActivity, "API Yanıtı: $body", Toast.LENGTH_LONG).show()
                }
                Log.d("API", "Yanıt: $body")
            }
        })
    }
}
