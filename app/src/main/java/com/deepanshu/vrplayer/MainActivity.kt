package com.deepanshu.vrplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnPlayVideo).setOnClickListener {
            val videoUri = findViewById<EditText>(R.id.etVideoUri).text.toString().trim()
            if (videoUri.isNotEmpty()) {
                if (validateUri(Uri.parse(videoUri))) {
                    BaseVideoPlayerActivity.startVideo(this@MainActivity, Uri.parse(videoUri))
                } else Toast.makeText(
                    this, "This video format is not supported", Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(this, "Add a video URI/URL", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateUri(uri: Uri): Boolean {
        return uri.path!!.contains(".mp4") || uri.path!!.contains(".m3u8")
    }
}