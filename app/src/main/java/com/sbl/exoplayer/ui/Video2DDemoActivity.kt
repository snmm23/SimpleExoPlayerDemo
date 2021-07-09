package com.sbl.exoplayer.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.sbl.exoplayer.R
import com.sbl.exoplayer.library.ExoPlayerLayout
import com.sbl.exoplayer.utils.AndroidContentGroupManager
import com.sbl.exoplayer.utils.ScreenSwitchHelper

/**
 * sunbolin 2021/7/8
 */
class Video2DDemoActivity : AppCompatActivity() {

    private lateinit var exoPlayerGroup: FrameLayout
    private lateinit var exoPlayerLayout: ExoPlayerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_2d_demo)
        exoPlayerGroup = findViewById(R.id.exoPlayerGroup)
        exoPlayerLayout = ExoPlayerLayout(this)
        exoPlayerLayout.setLaunchDate(
            this,
            "2D Video",
            "https://vfx.mtime.cn/Video/2019/01/15/mp4/190115161611510728_480.mp4"
        )
        exoPlayerLayout.play(false)
        onConfigurationChanged(resources.configuration)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ScreenSwitchHelper.exitFullScreen(this)
            AndroidContentGroupManager.removeLandScapeVideoView(this, exoPlayerLayout)
            exoPlayerGroup.addView(
                exoPlayerLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            ScreenSwitchHelper.enterFullScreen(this)
            exoPlayerGroup.removeView(exoPlayerLayout)
            AndroidContentGroupManager.addLandScapeVideoView(this, exoPlayerLayout)
        }
    }


    override fun onPause() {
        super.onPause()
        exoPlayerLayout.onPause()
    }


    override fun onResume() {
        super.onResume()
        exoPlayerLayout.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
        exoPlayerLayout.onDestroy()
    }
}