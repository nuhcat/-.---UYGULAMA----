package com.example.kantahliliuygulamasi

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class TestDetayActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tumDegerler = listOf(
        "WBC", "Nötrofil", "Lenfosit", "Monosit", "Eozinofil", "Bazofil",
        "RBC", "HGB", "HCT", "MCV", "MCH", "MCHC",
        "RDW", "MPV", "PLT", "PCT", "PDW"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ana ScrollView (dikey kaydırma)
        val verticalScrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // İç ScrollView (yatay kaydırma)
        val horizontalScrollView = HorizontalScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Tablo yapısı
        val tableLayout = TableLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Başlık satırı
        val headerRow = TableRow(this)
        val tarihHeader = TextView(this).apply {
            text = "Tarih"
            setPadding(20, 20, 20, 20)
            textSize = 16f
            setTextColor(Color.BLACK)  // Siyah renk eklendi
        }
        headerRow.addView(tarihHeader)

        for (deger in tumDegerler) {
            val textView = TextView(this).apply {
                text = deger
                setPadding(20, 20, 20, 20)
                textSize = 16f
                setTextColor(Color.BLACK)  // Siyah renk eklendi
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Firebase'den veri çek
        val userId = auth.currentUser?.uid
        val secilenTarih = intent.getStringExtra("tarih")

        if (userId != null && secilenTarih != null) {
            firestore.collection("users")
                .document(userId)
                .collection("tests")
                .whereEqualTo("tarih", secilenTarih)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        val jsonString = doc.getString("degerler")
                        val tarih = doc.getString("tarih") ?: ""

                        val dataRow = TableRow(this)

                        val tarihCell = TextView(this).apply {
                            text = tarih
                            setPadding(20, 20, 20, 20)
                            textSize = 14f
                            setTextColor(Color.BLACK)  // Siyah renk eklendi
                        }
                        dataRow.addView(tarihCell)

                        val jsonObj = JSONObject(jsonString ?: "{}")

                        for (deger in tumDegerler) {
                            val textView = TextView(this).apply {
                                val value = jsonObj.optDouble(deger, Double.NaN)
                                text = if (value.isNaN()) "-" else value.toString()
                                setPadding(20, 20, 20, 20)
                                textSize = 14f
                                setTextColor(Color.BLACK)  // Siyah renk eklendi
                            }
                            dataRow.addView(textView)
                        }

                        tableLayout.addView(dataRow)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Veriler alınırken hata oluştu", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Kullanıcı veya tarih bilgisi eksik", Toast.LENGTH_SHORT).show()
        }

        horizontalScrollView.addView(tableLayout)
        verticalScrollView.addView(horizontalScrollView)
        setContentView(verticalScrollView)
    }
}
