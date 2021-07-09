package com.sbl.exoplayer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sbl.exoplayer.R
import com.sbl.exoplayer.library.VRExoPlayerLayout
import com.sbl.exoplayer.utils.ScreenSwitchHelper

/**
 * sunbolin 2021/7/9
 */
class VideoVRDemoActivity : AppCompatActivity() {

    private lateinit var vrPlayerLayout: VRExoPlayerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenSwitchHelper.enterFullScreen(this)
        setContentView(R.layout.activity_video_vr_demo)

        vrPlayerLayout = findViewById(R.id.playerLayout)
        vrPlayerLayout.setLaunchDate(
            this,
            "VR Video",
            "https://vfx.mtime.cn/Video/2019/01/15/mp4/190115161611510728_480.mp4"
        )
        vrPlayerLayout.play(false)
    }


    override fun onPause() {
        super.onPause()
        vrPlayerLayout.onPause()
    }


    override fun onResume() {
        super.onResume()
        vrPlayerLayout.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
        vrPlayerLayout.onDestroy()
    }
}