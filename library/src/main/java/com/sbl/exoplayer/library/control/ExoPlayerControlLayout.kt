package com.sbl.exoplayer.library.control

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.android.exoplayer2.Player
import com.sbl.exoplayer.library.R
import java.lang.ref.WeakReference
import java.util.*


/**
 * Created by sunbolin on 16/4/18.
 */
class ExoPlayerControlLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context!!, attrs, defStyleAttr
), View.OnClickListener, OnSeekBarChangeListener {

    companion object {
        private const val TAG = "ExoPlayerControlLayout"
        private const val hideTime = 5000
    }

    private var exoPlayerControlListener: ExoPlayerControlListener? = null

    private var back: ImageView
    private var title: TextView
    private var controlBottomLayout: LinearLayout
    private var seekBar: SeekBar
    private var totalTime: TextView
    private var currentTime: TextView
    private var pause: ImageView
    private var fullscreen: ImageView

    private var mHandler: Handler
    private var mFormatBuilder: StringBuilder
    private var mFormatter: Formatter

    private var isDragSeekBar = false
    private var isFinish = false
    private var isError = false


    private var viewAnimation: AlphaAnimation? = null
    private var isAnimationRun: Boolean = false
    private var isHide: Boolean = false


    init {
        LayoutInflater.from(context).inflate(R.layout.exo_player_control_layout, this)
        back = findViewById(R.id.back)
        title = findViewById(R.id.title)
        controlBottomLayout = findViewById(R.id.control_bottom_layout)
        fullscreen = findViewById(R.id.fullscreen)
        pause = findViewById(R.id.pause)
        seekBar = findViewById(R.id.seek_bar)
        totalTime = findViewById(R.id.total_time)
        currentTime = findViewById(R.id.current_time)

        back.setOnClickListener(this)
        fullscreen.setOnClickListener(this)
        pause.setOnClickListener(this)
        seekBar.setOnSeekBarChangeListener(this)
        seekBar.max = 1000
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        mHandler = MessageHandler(this)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateConfigurationViews()
    }


    private fun updateConfigurationViews() {
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            fullscreen.setImageResource(R.drawable.ic_round_fullscreen_24)
        } else {
            fullscreen.setImageResource(R.drawable.ic_round_fullscreen_exit_24)
        }
    }


    private fun stringForTime(timeMs: Long): String {
        val totalSeconds = timeMs.toInt() / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) mFormatter.format(
            "%02d:%02d:%02d",
            *arrayOf<Any>(hours, minutes, seconds)
        ).toString() else mFormatter.format("%02d:%02d", *arrayOf<Any>(minutes, seconds))
            .toString()
    }


    private fun setProgress(): Long  {
        return if (exoPlayerControlListener != null) {
            val position = exoPlayerControlListener!!.getCurrentPosition()
            val duration = exoPlayerControlListener!!.getDuration()
            if (duration > 0) {
                val percent = 1000L * position / duration
                seekBar.progress = percent.toInt()
            }

            val percent1 = exoPlayerControlListener!!.getBufferPercentage()
            seekBar.secondaryProgress = percent1 * 10
            totalTime.text = stringForTime(duration)
            currentTime.text = stringForTime(position)

            Log.e(TAG, "currentPlayTime = " + stringForTime(position))
            position
        } else {
            0
        }
    }


    private fun updatePausePlay() {
        when {
            isFinish -> {
                pause.setImageResource(R.drawable.ic_round_replay_24)
            }
            exoPlayerControlListener?.isPlaying()?: false -> {
                pause.setImageResource(R.drawable.ic_round_pause_24)
            }
            else -> {
                pause.setImageResource(R.drawable.ic_round_play_arrow_24)
            }
        }
    }


    override fun onClick(v: View) {
        when (v) {
            back -> {
                exoPlayerControlListener?.doBack()
            }

            pause -> {
                if (isFinish) {
                    doRestart()
                } else {
                    doPauseResume()
                }
                show()
            }

            fullscreen -> {
                if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    exoPlayerControlListener?.doHorizontalScreen()
                } else {
                    exoPlayerControlListener?.doVerticalScreen()
                }
                show()
            }
        }
    }


    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val duration = exoPlayerControlListener?.getDuration()?: 0
            val newPosition = duration * progress.toLong() / 1000L

            if (isFinish) {
                setFinish(false)
            }
            exoPlayerControlListener?.seekTo(newPosition)
            currentTime.text = stringForTime(newPosition)
        }
    }


    override fun onStartTrackingTouch(seekBar: SeekBar) {
        isDragSeekBar = true
    }


    override fun onStopTrackingTouch(seekBar: SeekBar) {
        isDragSeekBar = false
    }


    private inner class MessageHandler constructor(view: ExoPlayerControlLayout) : Handler() {

        private val mView: WeakReference<ExoPlayerControlLayout> = WeakReference(view)

        override fun handleMessage(msg: Message) {
            val view = mView.get()
            when (msg.what) {
                1 -> view?.hide()
                2 -> {
                    sendMessageDelayed(obtainMessage(2), 1000 - view?.setProgress()!! % 1000)
                    setProgress()
                }
            }
        }
    }


    private fun doPauseResume() {
        exoPlayerControlListener?.apply {
            if (isPlaying()) {
                mHandler.removeMessages(2)
                exoPlayerControlListener?.pause()
            } else {

                if (isFinish) {
                    setFinish(false)
                }
                exoPlayerControlListener?.start()
            }
        }
    }


    private fun doRestart() {
        if (isFinish) {
            setFinish(false)
        }
        exoPlayerControlListener?.restart()
    }


    fun setupListener(listener: ExoPlayerControlListener?) {
        exoPlayerControlListener = listener
    }


    private fun setFinish(flag: Boolean) {
        Log.e(TAG, "setFinish(), isFinish = $flag")

        isFinish = flag

        if (flag) {
            mHandler.removeMessages(2)

            seekBar.progress = 1000
            setProgress()
        }
    }


    private fun setError(flag: Boolean) {
        Log.e(TAG, "setError(), isError = $flag")
        isError = flag

        if (flag) {

            mHandler.removeMessages(2)
            controlBottomLayout.visibility = INVISIBLE
        } else {
            controlBottomLayout.visibility = VISIBLE
        }
    }


    fun setTitleName(titleName: String?) {
        title.text = titleName
    }


    fun releaseControl() {
        mHandler.removeMessages(1)
        mHandler.removeMessages(2)
        isDragSeekBar = false
        isFinish = false
        isError = false
        exoPlayerControlListener = null
    }


    fun onPlayerStateBack(state: Int) {
        when (state) {
            Player.STATE_READY -> {
                if (isError) {
                    setError(false)
                }
                mHandler.sendEmptyMessage(2)
                show()
            }

            Player.STATE_ENDED -> {
                setFinish(true)
                show()
            }
        }
    }


    fun onPlayerError() {
        setError(true)
        show()
    }


    fun onClickPlayerView() {
        if (isHide) {
            show()
        } else {
            hide()
        }
    }


    private fun show() {
        Log.e(TAG, "show()")
        showAnim()
        updateConfigurationViews()
        updatePausePlay()
        mHandler.removeMessages(1)
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), hideTime.toLong())
    }


    private fun hide() {
        Log.e(TAG, "hide()")
        //是否正在拖动SeekBar
        if (isDragSeekBar) {
            return
        }
        mHandler.removeMessages(1)
        hideAnim()
    }


    private fun showAnim() {
        if (isAnimationRun || !isHide) {
            return
        }
        if (viewAnimation == null) {
            viewAnimation = AlphaAnimation(0f, 1f)

            viewAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    isAnimationRun = true
                    visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {
                    isHide = false
                    isAnimationRun = false
                    viewAnimation = null
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            viewAnimation!!.duration = 400
        }
        if (viewAnimation != null) {
            startAnimation(viewAnimation)
        }
    }


    private fun hideAnim() {
        if (isAnimationRun || isHide) {
            return
        }
        if (viewAnimation == null) {
            viewAnimation = AlphaAnimation(1f, 0f)

            viewAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    isAnimationRun = true
                }

                override fun onAnimationEnd(animation: Animation) {
                    isHide = true
                    visibility = View.GONE
                    isAnimationRun = false
                    viewAnimation = null
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            viewAnimation!!.duration = 400
        }
        if (viewAnimation != null) {
            startAnimation(viewAnimation)
        }
    }
}