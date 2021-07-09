package com.sbl.exoplayer.library.control

/**
 * sunbolin 2021/7/9
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