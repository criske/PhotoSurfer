package com.crskdev.photosurfer.presentation.playwave

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.crskdev.photosurfer.R
import kotlinx.android.synthetic.main.player_layout.view.*

/**
 * Created by Cristian Pela on 19.10.2018.
 */
class PlayerView : CardView {

    private var listener: PlayerListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {

        LayoutInflater.from(context).inflate(R.layout.player_layout, this, true)

        imgBtnPlayerClose.setOnClickListener {
            listener?.onAction(Action.Close)
        }
        imgBtnPlayerPause.setOnClickListener {
            listener?.onAction(Action.Pause)
        }
        imgBtnPlayerPlayStop?.setOnClickListener { _ ->
            listener?.onAction(Action.PlayOrStop)
        }
        seekBarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var startManualSeek = false

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (startManualSeek) {
                    listener?.onAction(Action.SeekTo(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                startManualSeek = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listener?.onAction(Action.SeekTo(seekBar.progress, true))
                startManualSeek = false
            }
        })
    }

    fun changeState(state: PlayingSongState) {
        when (state) {
            is PlayingSongState.None -> reset()
            is PlayingSongState.Prepare -> prepare(state.song!!)
            is PlayingSongState.Ready -> ready(state)
            is PlayingSongState.Playing -> playing(state)
            is PlayingSongState.Seeking -> seeking(state)
            is PlayingSongState.Paused -> pause(state)
            is PlayingSongState.Completed,
            is PlayingSongState.Stopped -> stopOrComplete(state)
        }
    }

    private fun ready(state: PlayingSongState) {
        restore(state)
        seekBarPlayer.isEnabled = true
        imgBtnPlayerPlayStop.isEnabled = true
    }

    private fun prepare(song: SongUI) {
        isVisible = true
        textPlayerSongInfo.text = song.fullInfo
        textPlayerSeekPosition.text = null
        imgBtnPlayerPause.isVisible = false
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
            isEnabled = false
        }
        seekBarPlayer.apply {
            progress = 0
            isEnabled = false
            max = song.durationInt
        }
    }

    private fun restore(state: PlayingSongState) {
        if (!isVisible) {
            isVisible = true
        }
        val song = state.song
        textPlayerSongInfo.text = song?.fullInfo
        seekBarPlayer.apply {
            isEnabled = true
            max = song?.durationInt ?: 0
        }
    }

    private fun seeking(state: PlayingSongState.Seeking) {
        restore(state)
        seekBarPlayer.apply {
            progress = state.position
        }
        textPlayerSeekPosition.text = state.positionDisplay
    }


    private fun playing(state: PlayingSongState.Playing) {
        restore(state)
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_stop_white_24dp)
        }
        imgBtnPlayerPause.isVisible = true
        seekBarPlayer.progress = state.position
        textPlayerSeekPosition.text = state.positionDisplay
    }

    private fun pause(state: PlayingSongState.Paused) {
        restore(state)
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }
        imgBtnPlayerPause.isVisible = false
        seekBarPlayer.progress = state.position
        textPlayerSeekPosition.text = state.positionDisplay
    }


    private fun stopOrComplete(state: PlayingSongState) {
        restore(state)
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }
        imgBtnPlayerPause.isVisible = false
        seekBarPlayer.progress = 0
        textPlayerSeekPosition.text = null
    }

    private fun reset() {
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
            isEnabled = false
        }
        imgBtnPlayerPause.isVisible = false
        seekBarPlayer.apply {
            progress = 0
            isEnabled = false
        }
        textPlayerSongInfo.text = null
        textPlayerSeekPosition.text = null
        isVisible = false
    }

    fun setOnPlayerListener(listener: PlayerListener) {
        this.listener = listener
    }

    sealed class Action {
        object PlayOrStop : Action()
        object Pause : Action()
        object Close : Action()
        class SeekTo(val position: Int, val confirmedToPlay: Boolean = false) : Action()
    }

    interface PlayerListener {
        fun onAction(action: Action)
    }

}

