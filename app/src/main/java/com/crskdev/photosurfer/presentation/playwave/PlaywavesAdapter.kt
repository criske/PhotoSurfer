package com.crskdev.photosurfer.presentation.playwave

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorFilter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.item_playwave.view.*

/**
 * Created by Cristian Pela on 16.10.2018.
 */
class PlaywavesAdapter(private val layoutInflater: LayoutInflater,
                       private val action: (PlaywaveAction) -> Unit) : ListAdapter<PlaywaveUI, PlaywavesVH>(
        object : DiffUtil.ItemCallback<PlaywaveUI>() {
            override fun areItemsTheSame(oldItem: PlaywaveUI, newItem: PlaywaveUI): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PlaywaveUI, newItem: PlaywaveUI): Boolean = oldItem == newItem
        }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaywavesVH =
            PlaywavesVH(layoutInflater.inflate(R.layout.item_playwave, parent, false), action)

    override fun onBindViewHolder(holder: PlaywavesVH, position: Int) {
        holder.bind(getItem(position))
    }
}

sealed class PlaywaveAction {
    class Play(val playwaveId: Int) : PlaywaveAction()
    class Error(val message: String) : PlaywaveAction()
}

class PlaywavesVH(view: View, action: (PlaywaveAction) -> Unit) : BindViewHolder<PlaywaveUI>(view) {

    init {
        with(itemView) {
            imageBtnPlaywavePlay.setOnClickListener { _ ->
                model?.let {
                    val play = if (it.hasError) {
                        PlaywaveAction.Error(resources.getString(R.string.error_playwave_no_song_in_media))
                    } else {
                        PlaywaveAction.Play(it.id)
                    }
                    action(play)
                }
            }
        }
    }

    override fun onBindModel(model: PlaywaveUI) {
        with(itemView) {
            textPlaywaveTitle.text = model.title
            textPlaywaveSong.text = model.songInfo
            textPlaywaveSize.text = model.size.toString()
            if (model.hasError) {
                textPlaywaveSong.setTextColor(ContextCompat.getColor(context, R.color.colorLike))
                imageBtnPlaywavePlay.colorFilter = PorterDuff.Mode.SRC_IN.toColorFilter(Color.DKGRAY)
            }
        }
    }

    override fun unBind() = Unit

}