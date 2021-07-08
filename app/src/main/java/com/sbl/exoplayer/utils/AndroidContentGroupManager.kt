package com.sbl.exoplayer.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout

/**
 * sunbolin 2021/7/8
 */
object AndroidContentGroupManager {

    fun addLandScapeVideoView(activity: Activity, view: View?) {
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val contentGroup = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        contentGroup.addView(view, params)
    }

    fun removeLandScapeVideoView(activity: Activity, view: View?) {
        val contentGroup = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        contentGroup.removeView(view)
    }
}