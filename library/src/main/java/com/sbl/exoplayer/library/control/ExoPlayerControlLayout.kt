package com.sbl.exoplayer.library.control

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
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
    private var controllerLayout: LinearLayout
    private var mProgress: SeekBar
    private var mEndTime: TextView
    private var mCurrentTime: TextView
    private var mPauseButton: ImageView
    private var mFullscreenLand: ImageView

    private var mHandler: Handler
    private var mFormatBuilder: StringBuilder
    private var mFormatter: Formatter

    var isShowing = false
        private set
    private var isFinish = false
    private var isError = false


    init {
        LayoutInflater.from(context).inflate(R.layout.exo_player_control_layout, this)
        back = findViewById(R.id.back)
        title = findViewById(R.id.title)
        controllerLayout = findViewById(R.id.controller_ll)
        mFullscreenLand = findViewById(R.id.fullscreen_land)
        mPauseButton = findViewById(R.id.pause)
        mProgress = findViewById(R.id.mediacontroller_progress)
        mEndTime = findViewById(R.id.time)
        mCurrentTime = findViewById(R.id.time_current)


        back.setOnClickListener(this)
        mFullscreenLand.setOnClickListener(this)
        mPauseButton.setOnClickListener(this)
        mProgress.setOnSeekBarChangeListener(this)
        mProgress.max = 1000
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
            mFullscreenLand.setImageResource(R.drawable.ic_round_fullscreen_24)
        } else {
            mFullscreenLand.setImageResource(R.drawable.ic_round_fullscreen_exit_24)
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


    private fun setProgress(): Long {
        return if (exoPlayerControlListener != null) {
            val position = exoPlayerControlListener!!.getCurrentPosition()
            val duration = exoPlayerControlListener!!.getDuration()
            if (duration > 0) {
                if (!mProgress.isEnabled) {
                    mProgress.isEnabled = true
                }
                val percent = 1000L * position / duration
                mProgress.progress = percent.toInt()
            }
            val percent1 = exoPlayerControlListener!!.getBufferPercentage()
            mProgress.secondaryProgress = percent1 * 10
            mEndTime.text = stringForTime(duration)
            mCurrentTime.text = stringForTime(position)
            Log.e(TAG, "currentPlayTime = " + stringForTime(position))
            position
        } else {
            0
        }
    }


    private fun updatePausePlay() {
        when {
            isFinish -> {
                mPauseButton.setImageResource(R.drawable.ic_round_replay_24)
            }
            exoPlayerControlListener?.isPlaying()?: false -> {
                mPauseButton.setImageResource(R.drawable.ic_round_pause_24)
            }
            else -> {
                mPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24)
            }
        }
    }


    override fun onClick(v: View) {
        when (v) {
            back -> {
                exoPlayerControlListener?.doBack()
            }

            mPauseButton -> {
                if (!isError) {
                    if (isFinish) {
                        doRestart()
                    } else {
                        doPauseResume()
                    }
                }
                show()
            }

            mFullscreenLand -> {
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
            val duration = exoPlayerControlListener!!.getDuration()
            val newPosition = duration * progress.toLong() / 1000L
            exoPlayerControlListener!!.seekTo(newPosition)
            mCurrentTime.text = stringForTime(newPosition)
        }
    }


    override fun onStartTrackingTouch(seekBar: SeekBar) {
        show()
    }


    override fun onStopTrackingTouch(seekBar: SeekBar) {}


    private inner class MessageHandler constructor(view: ExoPlayerControlLayout?) : Handler() {

        private val mView: WeakReference<ExoPlayerControlLayout?> = WeakReference(view)

        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view?.exoPlayerControlListener != null) {
                when (msg.what) {
                    1 -> view.hide()
                    2 -> {
                        Log.e(
                            TAG,
                            "handleMessage isPlaying = " + view.exoPlayerControlListener!!.isPlaying() + ", handleMessage isFinish = " + view.isFinish
                        )
                        if (!view.isFinish && !isError) sendMessageDelayed(
                            obtainMessage(2),
                            1000 - view.setProgress() % 1000
                        )
                    }
                }
            }
        }
    }


    private fun doPauseResume() {
        exoPlayerControlListener?.apply {
            if (isPlaying()) {
                exoPlayerControlListener?.pause()
            } else {
                exoPlayerControlListener?.start()
            }
        }
    }


    private fun doRestart() {
        exoPlayerControlListener?.restart()
    }


    fun setupListener(listener: ExoPlayerControlListener?) {
        exoPlayerControlListener = listener
    }


    fun setFinish(flag: Boolean) {
        Log.e(TAG, "setFinish , isFinish = $flag")
        isFinish = flag
        mHandler.removeMessages(2)
        if (!isFinish) {
            mHandler.sendEmptyMessage(2)
        } else {
            mProgress.progress = 1000
        }
        updatePausePlay()
    }


    fun setError(flag: Boolean) {
        isError = flag
        mProgress.isEnabled = !isError
        if (flag) {
            controllerLayout.visibility = INVISIBLE
        } else {
            controllerLayout.visibility = VISIBLE
        }
        updatePausePlay()
    }


    fun setTitleName(titleName: String?) {
        title.text = titleName
    }


    fun show() {
        Log.e(TAG, "show()")
        if (!isShowing) {
            isShowing = true
            try {
                showControllerAnimation()
            } catch (ignored: Exception) {
            }
        }
        updateConfigurationViews()
        updatePausePlay()
        mHandler.removeMessages(1)
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), hideTime.toLong())
    }


    fun hide() {
        Log.e(TAG, "hide()")
        try {
            hideControllerAnimation(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isShowing = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    isShowing = false
                }

                override fun onAnimationRepeat(animation: Animator) {
                    isShowing = false
                }
            })
        } catch (ignored: Exception) {
            isShowing = false
        }
    }


    fun releaseControl() {
        mHandler.removeMessages(1)
        mHandler.removeMessages(2)
        isShowing = false
        isFinish = false
        isError = false
        exoPlayerControlListener = null
    }


    private fun showControllerAnimation() {
        val objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        objectAnimator.duration = 400
        objectAnimator.start()
    }


    private fun hideControllerAnimation(animationListener: AnimatorListener) {
        val objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        objectAnimator.duration = 400
        objectAnimator.addListener(animationListener)
        objectAnimator.start()
    }
}