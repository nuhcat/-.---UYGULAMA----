package com.example.kantahliliuygulamasi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kantahliliuygulamasi.databinding.ActivitySonucBinding

class SonucActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySonucBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<String>()

    private var sonuc: String? = null
    private var oneriler: ArrayList<String>? = null
    private var genelOzet: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySonucBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // API'den gelen verileri al
        sonuc = intent.getStringExtra("sonuc") ?: "Sonuç alınamadı"
        oneriler = intent.getStringArrayListExtra("oneriler") ?: arrayListOf()
        genelOzet = intent.getStringExtra("genelOzet") ?: ""

        // RecyclerView ayarları
        chatAdapter = ChatAdapter(messageList)
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerChat.adapter = chatAdapter

        // Buton görünürlükleri
        binding.btnOzetle.visibility = View.GONE

        // API'den gelen sonucu yazdır
        chatAdapter.addMessageLineByLineAnimated(sonuc ?: "Sonuç bulunamadı") {
            scrollToBottom()
        }

        // Yeni Test Yükle
        binding.btnYeniTest.setOnClickListener {
            startActivity(Intent(this, OcrActivity::class.java))
        }

        // Öneri Sun
        binding.btnOneriSun.setOnClickListener {
            if (!oneriler.isNullOrEmpty()) {
                val onerilerText = "✅ Öneriler:\n" + oneriler!!.joinToString("\n") { "- $it" }
                chatAdapter.addMessageLineByLineAnimated(onerilerText) {
                    scrollToBottom()
                }
                binding.btnOneriSun.visibility = View.GONE
                binding.btnOzetle.visibility = View.VISIBLE
            }
        }

        // Özetle
        binding.btnOzetle.setOnClickListener {
            if (!genelOzet.isNullOrBlank()) {
                val ozetMesaj = "📌 Genel Özet:\n$genelOzet"
                chatAdapter.addMessageLineByLineAnimated(ozetMesaj) {
                    scrollToBottom()
                }
                binding.btnOzetle.visibility = View.GONE
                binding.btnOneriSun.visibility = View.GONE
            }
        }

        // Geçmiş Testlerim
        binding.btnGecmis.setOnClickListener {
            startActivity(Intent(this, GecmisTestlerActivity::class.java))
        }
    }

    private fun scrollToBottom() {
        binding.recyclerChat.post {
            binding.recyclerChat.scrollToPosition(messageList.size - 1)
        }
    }
}
