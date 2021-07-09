package com.sbl.exoplayer.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.sbl.exoplayer.R

/**
 * sunbolin 2021/7/9
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn2D = findViewById<Button>(R.id.button_2d)
        val btnVR = findViewById<Button>(R.id.button_vr)

        btn2D.setOnClickListener {
            startActivity(Intent(this, Video2DDemoActivity::class.java))
        }

        btnVR.setOnClickListener {
            startActivity(Intent(this, VideoVRDemoActivity::class.java))
        }
    }
}