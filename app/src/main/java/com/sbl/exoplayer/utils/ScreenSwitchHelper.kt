package com.sbl.exoplayer.utils

import android.app.Activity
import android.content.Context
import android.view.*

/**
 * sunbolin 2021/7/8
 */
object ScreenSwitchHelper {

    fun enterFullScreen(activity: Activity?) {
        if (activity != null) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            val decorView = activity.window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun exitFullScreen(activity: Activity?) {
        if (activity != null) {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            val flag = View.SYSTEM_UI_FLAG_VISIBLE
            if (checkDeviceHasNavigationBar(activity)) {
                activity.window.decorView.systemUiVisibility = flag
            }
        }
    }

    fun checkDeviceHasNavigationBar(context: Context?): Boolean {
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        return !hasMenuKey && !hasBackKey
    }
}