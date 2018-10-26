package com.crskdev.photosurfer.presentation.playwave

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.PlaywavePhoto
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.item_slide_show.view.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


/**
 * Created by Cristian Pela on 26.10.2018.
 */
class PlaywaveSlideShowAdapter(private val layoutInflater: LayoutInflater,
                               private val glide: RequestManager) : RecyclerView.Adapter<PlaywaveSlideShowVH>() {

    private val items = mutableListOf<PlaywavePhoto>()

    private var currentPositionInAnimation = -1

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
            if (currentPositionInAnimation != realPosition) {
                currentPositionInAnimation = realPosition
                holder.animate()
            }
        }
    }

    override fun onViewRecycled(holder: PlaywaveSlideShowVH) {
        holder.unBind()
    }

    override fun onViewDetachedFromWindow(holder: PlaywaveSlideShowVH) {
        holder.clearAnimation()
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

    fun animate() {
//        val anim = ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//        anim.duration = 1000
//        val anim = AnimationUtils.loadAnimation(itemView.context, android.R.anim.fade_in).apply {
//            duration = 1000
//        }
        //itemView.startAnimation(anim)
    }

    fun clearAnimation() {
        //itemView.clearAnimation()
    }

    override fun unBind() {
        glide.clear(itemView.imageSlideShow)
    }

}