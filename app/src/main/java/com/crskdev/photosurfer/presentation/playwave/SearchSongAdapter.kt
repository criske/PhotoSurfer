package com.crskdev.photosurfer.presentation.playwave

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.item_list_search_song.view.*

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class SearchSongAdapter(private val inflater: LayoutInflater,
                        private val action: (SearchSongAction) -> Unit) : PagedListAdapter<SongUI, SongVH>(
        object : DiffUtil.ItemCallback<SongUI>() {
            override fun areItemsTheSame(oldItem: SongUI, newItem: SongUI): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SongUI, newItem: SongUI): Boolean =
                    oldItem == newItem
        }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH =
            SongVH(inflater.inflate(R.layout.item_list_search_song, parent, false), action)

    override fun onBindViewHolder(holder: SongVH, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        } ?: holder.unBind()
    }
}

sealed class SearchSongAction {
    class Play(val song: SongUI) : SearchSongAction()
    class Add(val song: SongUI) : SearchSongAction()
}

class SongVH(view: View, action: (SearchSongAction) -> Unit) : BindViewHolder<SongUI>(view) {

    init {
        with(itemView) {
            imageBtnSongAdd.setOnClickListener { _ ->
                model?.let { action(SearchSongAction.Add(it)) }
            }
            imageBtnSongPlay.setOnClickListener { _ ->
                model?.let { action(SearchSongAction.Play(it)) }
            }
        }
    }


    override fun onBindModel(model: SongUI) {
        with(itemView) {
            textSongArtist.text = model.artist
            textSongTitle.text = model.title
            textSongDuration.text = model.duration
        }
    }

    override fun unBind() {

    }
}

