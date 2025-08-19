package com.example.kantahliliuygulamasi

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kantahliliuygulamasi.databinding.ItemChatMessageBinding

class ChatAdapter(private val messages: MutableList<String>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: String) {
            // Satır için harf harf animasyon yok, çünkü mesajlar satır satır ekleniyor,
            // harf harf animasyon Satır Satır animasyon fonksiyonunda yapılacak.
            binding.textMessage.text = message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Satır satır harf harf animasyonlu mesaj ekleme fonksiyonu.
     * @param fullMessage -> Çok satırlı mesaj
     * @param delayPerChar -> Her harf için gecikme (ms)
     * @param onComplete -> Animasyon tamamlanınca çağrılır.
     */
    fun addMessageLineByLineAnimated(
        fullMessage: String,
        delayPerChar: Long = 30L,
        onComplete: (() -> Unit)? = null
    ) {
        val lines = fullMessage.split("\n")
        val handler = Handler(Looper.getMainLooper())

        fun animateLine(index: Int) {
            if (index >= lines.size) {
                onComplete?.invoke()
                return
            }

            val line = lines[index]
            // Yeni satırı boş olarak ekle
            messages.add("")
            val position = messages.size - 1
            notifyItemInserted(position)

            var charIndex = 0
            val runnable = object : Runnable {
                override fun run() {
                    if (charIndex <= line.length) {
                        messages[position] = line.substring(0, charIndex)
                        notifyItemChanged(position)
                        charIndex++
                        handler.postDelayed(this, delayPerChar)
                    } else {
                        // Bu satır bitti, bir sonraki satıra geç
                        animateLine(index + 1)
                    }
                }
            }
            handler.post(runnable)
        }

        animateLine(0)
    }
}
