package com.example.kantahliliuygulamasi

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class OrnekActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ornek)

        val videoView = findViewById<VideoView>(R.id.videoView)

        // Videonun URI'sini al (raw klasöründen)
        val videoUri = Uri.parse("android.resource://${packageName}/raw/kadiii")

        videoView.setVideoURI(videoUri)

        // Oynatma kontrolleri ekle (duraklat, devam ettir vs)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.start()
    }
}
