package com.example.navi_constants

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MusicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        val song : MediaPlayer = MediaPlayer.create(this@MusicActivity, R.raw.test_song)
        song.start()
    }
}