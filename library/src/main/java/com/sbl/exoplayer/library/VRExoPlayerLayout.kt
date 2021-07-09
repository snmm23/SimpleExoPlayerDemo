package com.sbl.exoplayer.library

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.asha.vrlib.MD360Director
import com.asha.vrlib.MD360DirectorFactory
import com.asha.vrlib.MDVRLibrary
import com.asha.vrlib.model.BarrelDistortionConfig
import com.asha.vrlib.model.MDPinchConfig
import com.google.android.exoplayer2.*
import com.sbl.exoplayer.library.control.ExoPlayerControlLayout
import com.sbl.exoplayer.library.control.ExoPlayerControlListener
import com.sbl.exoplayer.library.factory.CustomProjectionFactory
import kotlin.math.max
import kotlin.math.min


/**
 * sunbolin 2021/7/9
 */
class VRExoPlayerLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context!!, attrs, defStyleAttr
), View.OnClickListener, ExoPlayerControlListener {

    companion object {
        private const val TAG = "VRExoPlayerLayout"
    }

    private var playerView: GLSurfaceView
    private var controlLayout: ExoPlayerControlLayout
    private var playerLoading: ProgressBar
    private var playerError: ImageView

    private var player: SimpleExoPlayer? = null
    private var mVRLibrary: MDVRLibrary? = null

    private var activity: Activity? = null
    private var titleName: String? = null
    private var streamUrl: String? = null
    private var startWindow: Int = 0
    private var startPosition: Long = 0

    private var isPause = false


    init {
        LayoutInflater.from(context).inflate(R.layout.vr_exo_player_layout, this)
        playerView = findViewById(R.id.player_view)
        playerLoading = findViewById(R.id.player_loading)
        playerError = findViewById(R.id.player_error)
        controlLayout = findViewById(R.id.control_layout)

        playerError.setOnClickListener(this)
        controlLayout.canFullscreenAbility(false)
        controlLayout.setupListener(this)

        mVRLibrary = createVRLibrary()

//        if (is180) {
            mVRLibrary!!.switchDisplayMode(context, MDVRLibrary.DISPLAY_MODE_GLASS)
            mVRLibrary!!.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL)
//        } else {
//            mVRLibrary!!.switchDisplayMode(context, MDVRLibrary.DISPLAY_MODE_NORMAL)
//            mVRLibrary!!.switchProjectionMode(context, MDVRLibrary.PROJECTION_MODE_SPHERE)
//        }
        mVRLibrary!!.switchInteractiveMode(context, MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH)
    }


    private fun createVRLibrary(): MDVRLibrary {
        return MDVRLibrary.with(context)
            .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
            .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
            .asVideo { surface -> activity?.runOnUiThread { player!!.setVideoSurface(surface) } }
            .ifNotSupport { mode ->
                val tip =
                    if (mode === MDVRLibrary.INTERACTIVE_MODE_MOTION) "onNotSupport:MOTION" else "onNotSupport:$mode"
                Toast.makeText(context, tip, Toast.LENGTH_SHORT).show()
            }
            .listenGesture {
                controlLayout.onClickPlayerView()
            }
            .pinchConfig(MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
            .pinchEnabled(true)
            .directorFactory(object : MD360DirectorFactory() {
                override fun createDirector(index: Int): MD360Director? {
                    return MD360Director.builder().setPitch(90f).build()
                }
            })
            .projectionFactory(CustomProjectionFactory())
            .barrelDistortionConfig(
                BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f)
            )
            .build(playerView)
    }


    private fun initializePlayer(shouldAutoPlay: Boolean) {
        if (streamUrl == null || streamUrl!!.isEmpty()) {
            return
        }
        if (player == null) {
            player = SimpleExoPlayer.Builder(context).build()
            player!!.addListener(PlayerEventListener())
        }
        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(startWindow, startPosition)
        }
        player!!.playWhenReady = shouldAutoPlay
        val mediaItem = MediaItem.fromUri(
            streamUrl!!
        )
        player!!.setMediaItem(mediaItem, !haveStartPosition)
        player!!.prepare()
    }


    override fun onClick(v: View) {
        when (v) {
            playerError -> {
                playerLoading.visibility = VISIBLE
                playerError.visibility = GONE
                initializePlayer(false)
            }
        }
    }


    override fun start() {
        player?.playWhenReady = true
    }


    override fun pause() {
        player?.playWhenReady = false
    }


    override fun restart() {
        player?.apply {
            playWhenReady = true
            seekTo(0)
        }
    }


    override fun getDuration(): Long {
        return if (player != null) if (player!!.duration == -1L) 0 else player!!.duration else 0
    }


    override fun getCurrentPosition(): Long {
        return if (player != null) if (player!!.duration == -1L) 0 else player!!.currentPosition else 0
    }


    override fun seekTo(var1: Long) {
        if (player != null) player!!.seekTo(
            if (player!!.duration == -1L) 0 else min(
                max(
                    0,
                    var1
                ), getDuration()
            )
        )
    }


    override fun isPlaying(): Boolean {
        return player != null && player!!.playWhenReady
    }


    override fun getBufferPercentage(): Int {
        return if (player != null) player!!.bufferedPercentage else 0
    }


    override fun doHorizontalScreen() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }


    override fun doVerticalScreen() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    override fun doBack() {
        activity!!.finish()
    }


    private fun release() {
        if (player != null) {
            updateStartPosition()
            player!!.release()
            player = null
        }
    }


    private fun releasePlayer() {
        playerLoading.visibility = GONE
        playerError.visibility = GONE
        controlLayout.releaseControl()
        streamUrl = null
        activity = null
        clearStartPosition()
    }


    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlaybackStateChanged(state: Int) {
            Log.e(TAG, "playbackState = $state")
            controlLayout.onPlayerStateBack(state)

            when (state) {
                Player.STATE_BUFFERING -> {
                    playerLoading.visibility = VISIBLE
                    playerError.visibility = GONE
                }

                Player.STATE_READY -> {
                    mVRLibrary?.notifyPlayerChanged()

                    playerLoading.visibility = GONE
                    playerError.visibility = GONE
                }

                Player.STATE_ENDED -> {
                    playerLoading.visibility = GONE
                    playerError.visibility = GONE
                }
            }
        }


        override fun onPlayerError(e: ExoPlaybackException) {
            release()
            playerLoading.visibility = GONE
            playerError.visibility = VISIBLE
            controlLayout.onPlayerError()
        }
    }


    private fun updateStartPosition() {
        if (player != null) {
            startWindow = player!!.currentWindowIndex
            startPosition = max(0, player!!.contentPosition)
        }
    }


    private fun clearStartPosition() {
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }


    fun setLaunchDate(activity: Activity?, titleName: String?, streamUrl: String?) {
        this.activity = activity
        this.titleName = titleName
        this.streamUrl = streamUrl
        controlLayout.setTitleName(titleName)
    }


    fun play(isAutoPlay: Boolean) {
        if (streamUrl == null || streamUrl!!.isEmpty()) {
            return
        }
        initializePlayer(isAutoPlay)
    }


    fun onResume() {
        mVRLibrary?.onResume(context)
        if (isPause) {
            initializePlayer(false)
        }
        isPause = false
    }


    fun onPause() {
        mVRLibrary?.onPause(context)
        release()
        isPause = true
    }


    fun onDestroy() {
        mVRLibrary?.onDestroy()
        releasePlayer()
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mVRLibrary?.onOrientationChanged(context)
    }
}