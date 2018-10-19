package com.crskdev.photosurfer.presentation.playwave

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.annotation.IntRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.crskdev.photosurfer.R
import kotlinx.android.synthetic.main.player_layout.view.*
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 19.10.2018.
 */
class PlayerView : ConstraintLayout {

    private var listener: PlayerListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.player_layout, this, true)
        imgBtnPlayerClose.setOnClickListener {
            listener?.onAction(Action.Close)

        }
        imgBtnPlayerPause.setOnClickListener {
            listener?.onAction(Action.Pause)
        }
        imgBtnPlayerPlayStop?.setOnClickListener { v ->
            (v.tag as Boolean?)?.let {
                val isPlaying = it
                listener?.onAction(if (isPlaying) Action.Stop else Action.Play)
            }
        }
        seekBarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = Unit

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listener?.onAction(Action.JumpTo(seekBar.progress))
            }
        })
    }

    fun changeState(state: PlayingSongState) {
        when (state) {
            is PlayingSongState.Playing -> playing(state.song!!.fullInfo, state.position, state.total)
            is PlayingSongState.Paused -> pause(state.position, state.total)
            is PlayingSongState.Stopped -> stop()
            else -> close()
        }
    }

    private fun playing(info: String, position: Long, total: Long) {
        if (!isVisible)
            isVisible = true
        textPlayerSongInfo.text = info
        //TODO move this to view-model
        val progress = ((position / total.toFloat()) * 100).roundToInt()
        seekBarPlayer.progress = progress
        imgBtnPlayerPlayStop.apply {
            tag = true // mark this as playing
            setImageResource(R.drawable.ic_stop_white_24dp)
        }
        imgBtnPlayerPause.isVisible = true
    }

    private fun pause(position: Long, total: Long) {
        val progress = ((position / total.toFloat()) * 100).roundToInt()
        seekBarPlayer.progress = progress
        imgBtnPlayerPlayStop.apply {
            tag = true //mark this as stopped/paused
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }
        imgBtnPlayerPause.isVisible = false
    }

    private fun stop() {
        pause(0, 100)
    }


    fun onActionListener(closeListener: PlayerListener) {
        this.listener = closeListener
    }

    private fun close() {
        stop()
        isVisible = false
    }

    sealed class Action {
        object Play : Action()
        object Stop : Action()
        object Pause : Action()
        object Close : Action()
        class JumpTo(@IntRange(from = 0, to = 100) val percent: Int) : Action()
    }

    interface PlayerListener {
        fun onAction(action: Action)
    }


}

