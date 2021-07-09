package com.sbl.exoplayer.library

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.sbl.exoplayer.library.control.ExoPlayerControlLayout
import com.sbl.exoplayer.library.control.ExoPlayerControlListener
import kotlin.math.max
import kotlin.math.min

/**
 * Created by sunbolin on 16/4/18.
 */
class ExoPlayerLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context!!, attrs, defStyleAttr
), OnTouchListener, View.OnClickListener, ExoPlayerControlListener {

    companion object {
        private const val TAG = "ExoPlayerLayout"
    }

    private var playerView: PlayerView
    private var controlLayout: ExoPlayerControlLayout
    private var playerLoading: ProgressBar
    private var playerError: ImageView

    private var player: SimpleExoPlayer? = null

    private var activity: Activity? = null
    private var titleName: String? = null
    private var streamUrl: String? = null
    private var startWindow: Int = 0
    private var startPosition: Long = 0

    private var isPause = false


    init {
        LayoutInflater.from(context).inflate(R.layout.exo_player_layout, this)
        playerView = findViewById(R.id.player_view)
        playerLoading = findViewById(R.id.player_loading)
        playerError = findViewById(R.id.player_error)
        controlLayout = findViewById(R.id.control_layout)

        playerError.setOnClickListener(this)
        controlLayout.setupListener(this)
        setOnTouchListener(this)
    }


    private fun initializePlayer(shouldAutoPlay: Boolean) {
        if (streamUrl == null || streamUrl!!.isEmpty()) {
            return
        }
        if (player == null) {
            player = SimpleExoPlayer.Builder(context).build()
            player!!.addListener(PlayerEventListener())
            playerView.player = player
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


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> try {
                controlLayout.onClickPlayerView()
            } catch (var2: NullPointerException) {
                var2.printStackTrace()
            }
        }
        return false
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


    fun setResizeMode(resizeMode: Int) {
        playerView.resizeMode = resizeMode
    }


    fun play(isAutoPlay: Boolean) {
        if (streamUrl == null || streamUrl!!.isEmpty()) {
            return
        }
        initializePlayer(isAutoPlay)
    }


    fun onResume() {
        if (isPause) {
            initializePlayer(false)
        }
        isPause = false
    }


    fun onPause() {
        release()
        isPause = true
    }


    fun onDestroy() {
        releasePlayer()
    }
}