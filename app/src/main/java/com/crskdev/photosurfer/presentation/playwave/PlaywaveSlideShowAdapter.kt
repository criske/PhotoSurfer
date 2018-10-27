package com.crskdev.photosurfer.presentation.playwave

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.PlaywavePhoto
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.item_slide_show.view.*


/**
 * Created by Cristian Pela on 26.10.2018.
 */
class PlaywaveSlideShowAdapter(private val layoutInflater: LayoutInflater,
                               private val glide: RequestManager) : RecyclerView.Adapter<PlaywaveSlideShowVH>() {

    private val items = mutableListOf<PlaywavePhoto>()

    fun submit(newItems: List<PlaywavePhoto>) {
        if (!items.isEmpty()) items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaywaveSlideShowVH =
            PlaywaveSlideShowVH(
                    layoutInflater.inflate(R.layout.item_slide_show, parent, false),
                    glide)

    override fun getItemCount(): Int = Integer.MAX_VALUE

    override fun onBindViewHolder(holder: PlaywaveSlideShowVH, position: Int) {
        if (items.isNotEmpty()) {
            val realPosition = position.rem(items.size)
            holder.bind(items[realPosition])
        }
    }

    override fun onViewRecycled(holder: PlaywaveSlideShowVH) {
        holder.unBind()
    }

    override fun onViewDetachedFromWindow(holder: PlaywaveSlideShowVH) {
        PlaywaveSlideShowVH.previous = null
    }
}

class PlaywaveSlideShowVH(v: View, private val glide: RequestManager) : BindViewHolder<PlaywavePhoto>(v) {

    companion object {
        var previous: Drawable? = null
    }


    override fun onBindModel(model: PlaywavePhoto) {
        with(itemView) {
            glide.asDrawable()
                    .load(model.urls[ImageType.FULL])
                    .apply(RequestOptions()
                            .error(R.drawable.ic_logo)
                            .placeholder(previous))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean = false
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            previous = resource
                            return false
                        }

                    })
                    .into(imageSlideShow)

        }
    }

    override fun unBind() {
        glide.clear(itemView.imageSlideShow)
    }

}