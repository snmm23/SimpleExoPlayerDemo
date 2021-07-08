package com.sbl.exoplayer.library.control

/**
 * Created by sunbolin on 2016/12/28.
 */
interface ExoPlayerControlListener {

    fun start()

    fun pause()

    fun restart()

    fun getDuration(): Long

    fun getCurrentPosition(): Long

    fun seekTo(var1: Long)

    fun isPlaying(): Boolean

    fun getBufferPercentage(): Int

    fun doHorizontalScreen()

    fun doVerticalScreen()

    fun doBack()
}