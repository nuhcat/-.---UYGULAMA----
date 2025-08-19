package com.example.kantahliliuygulamasi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kantahliliuygulamasi.api.AnalyzeRequest
import com.example.kantahliliuygulamasi.api.AnalyzeResponse
import com.example.kantahliliuygulamasi.api.RetrofitClient
import com.example.kantahliliuygulamasi.databinding.ActivityOcrBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class OcrActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOcrBinding
    private val degerListesi = listOf(
        "WBC", "Nötrofil", "Lenfosit", "Monosit", "Eozinofil", "Bazofil",
        "RBC", "HGB", "HCT", "MCV", "MCH", "MCHC",
        "RDW", "MPV", "PLT", "PCT", "PDW"
    )
    private val girilenDegerler = mutableMapOf<String, String>()
    private var mevcutIndex = 0
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var progressJob: Job? = null
  

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.GONE  // Başlangıçta gizli

        guncelleGorunum()

        binding.buttonBack.setOnClickListener {
            kaydetGecerliDeger()
            if (mevcutIndex > 0) {
                mevcutIndex--
                guncelleGorunum()
            } else {
                Toast.makeText(this, "İlk değerdesiniz", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonNext.setOnClickListener {
            kaydetGecerliDeger()
            if (mevcutIndex < degerListesi.size - 1) {
                mevcutIndex++
                guncelleGorunum()
            } else {
                Toast.makeText(this, "Son değerdesiniz", Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutOcrButton.setOnClickListener {
            kaydetGecerliDeger()

            if (girilenDegerler.size < degerListesi.size || girilenDegerler.values.any { it.isBlank() }) {
                Toast.makeText(this, "Lütfen tüm değerleri girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.layoutOcrButton.isEnabled = false
            binding.progressBar.progress = 0
            binding.progressBar.visibility = View.VISIBLE

            progressJob = lifecycleScope.launch {
                animateProgressBar()
            }

            val jsonMap = mutableMapOf<String, Double>()
            for ((key, value) in girilenDegerler) {
                val doubleValue = value.replace(',', '.').toDoubleOrNull() ?: 0.0
                jsonMap[key] = doubleValue
            }

            val request = AnalyzeRequest(jsonMap)

            lifecycleScope.launch {
                try {
                    val response: AnalyzeResponse = RetrofitClient.apiService.analyzeResults(request)
                    val analiz = response.analiz
                    val genelOzet = response.genel_özet ?: "Genel özet bulunamadı."

                    val tumSonuclar = StringBuilder()
                    val tumOneriler = ArrayList<String>()

                    analiz?.forEach { (testAdi, sonuc) ->
                        val durum = sonuc.durum ?: "Durum bilinmiyor"
                        tumSonuclar.append("$testAdi: $durum\n")
                        sonuc.öneri?.forEach { oneri ->
                            tumOneriler.add("$testAdi: $oneri")
                        }
                    }

                    val currentUser = auth.currentUser
                    val userId = currentUser?.uid
                    if (userId != null) {
                        val tarih = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                        val kayitMap = hashMapOf(
                            "timestamp" to Timestamp.now(),
                            "tarih" to tarih,
                            "degerler" to JSONObject(jsonMap as Map<*, *>).toString(),
                            "analizSonuc" to tumSonuclar.toString(),
                            "genelOzet" to genelOzet,
                            "oneriler" to tumOneriler
                        )
                        firestore.collection("users").document(userId)
                            .collection("tests")
                            .add(kayitMap)
                    }

                    val intent = Intent(this@OcrActivity, SonucActivity::class.java)
                    intent.putExtra("sonuc", tumSonuclar.toString())
                    intent.putStringArrayListExtra("oneriler", tumOneriler)
                    intent.putExtra("genelOzet", genelOzet)
                    intent.putExtra("jsonString", JSONObject(jsonMap as Map<*, *>).toString())
                    startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(this@OcrActivity, "Sunucu veya bağlantı hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    progressJob?.cancel()
                    binding.progressBar.progress = progressMax
                    binding.progressBar.visibility = View.GONE
                    binding.layoutOcrButton.isEnabled = true
                }
            }
        }
    }

    private val progressDurationMillis = 5000L  // 5 saniye
    private val progressMax = 100               // Progress bar maksimum değeri

    private suspend fun animateProgressBar() {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + progressDurationMillis
        while (true) {
            val now = System.currentTimeMillis()
            if (now >= endTime) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.progress = progressMax
                }
                break
            }
            val fraction = (now - startTime).toFloat() / progressDurationMillis
            val progress = (fraction * progressMax).toInt()
            withContext(Dispatchers.Main) {
                binding.progressBar.progress = progress
            }
            delay(16) // 60 FPS'lik akıcı animasyon
        }
    }


    private fun guncelleGorunum() {
        val mevcutDeger = degerListesi[mevcutIndex]
        binding.textViewDeger.text = mevcutDeger
        binding.editTextValue.setText(girilenDegerler[mevcutDeger] ?: "")
    }

    private fun kaydetGecerliDeger() {
        val mevcutDeger = degerListesi[mevcutIndex]
        val girilen = binding.editTextValue.text.toString().trim()
        if (girilen.isNotEmpty()) {
            girilenDegerler[mevcutDeger] = girilen
        }
    }
}
