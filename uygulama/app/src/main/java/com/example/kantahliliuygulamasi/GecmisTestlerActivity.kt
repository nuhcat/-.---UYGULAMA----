package com.example.kantahliliuygulamasi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GecmisTestlerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestTarihAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val testTarihListesi = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gecmis_testler) // XML dosyası gerekiyor

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TestTarihAdapter(testTarihListesi) { secilenTarih ->
            val intent = Intent(this, TestDetayActivity::class.java)
            intent.putExtra("tarih", secilenTarih)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("tests")
                .get()
                .addOnSuccessListener { documents ->
                    testTarihListesi.clear()
                    for (doc in documents) {
                        val tarih = doc.getString("tarih")
                        if (tarih != null) {
                            testTarihListesi.add(tarih)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Testler alınamadı", Toast.LENGTH_SHORT).show()
                }
        }
    }

    class TestTarihAdapter(
        private val tarihler: List<String>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<TestTarihAdapter.TarihViewHolder>() {

        inner class TarihViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tarihText: TextView = itemView.findViewById(R.id.textTarih)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarihViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_test_tarih, parent, false)
            return TarihViewHolder(view)
        }

        override fun onBindViewHolder(holder: TarihViewHolder, position: Int) {
            val tarih = tarihler[position]
            holder.tarihText.text = tarih
            holder.itemView.setOnClickListener {
                onItemClick(tarih)
            }
        }

        override fun getItemCount(): Int = tarihler.size
    }
}
