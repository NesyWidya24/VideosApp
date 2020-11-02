package com.nessy.videoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_videos.*

class VideosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        title = "Videos"

        addVideoFab.setOnClickListener {
            startActivity(Intent(this, AddVideoActivity::class.java))
        }
    }
}