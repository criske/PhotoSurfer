package com.crskdev.photosurfer.presentation.photo.listadapter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.Photo
import kotlinx.android.synthetic.main.item_list_photos.view.*

class ListPhotosVH(private val glide: RequestManager,
                   view: View,
                   private val action: (ListPhotosAdapter.ActionWhat, Photo) -> Unit) : RecyclerView.ViewHolder(view) {


    private var photo: Photo? = null

    init {
        itemView.imagePhoto.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.PHOTO_DETAIL, it) } }
        itemView.textAuthor.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.AUTHOR, it) } }
        itemView.imgLike.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.LIKE, it.copy(likedByMe = !it.likedByMe)) } }
        itemView.imgCollection.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.COLLECTION, it) } }
    }

    fun bind(photo: Photo) {
        this.photo = photo
        if (photo.likedByMe) {
            itemView.imgLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorLike))
        }
        itemView.textAuthor.text = "@${photo.authorUsername}"
        glide.asDrawable()
                .load(photo.urls[ImageType.SMALL])
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_logo)
                        .transforms(CenterCrop(), RoundedCorners(8)))
                .transition(DrawableTransitionOptions().crossFade())
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        itemView.textError.text = e?.message
                        return true
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean = false

                })
                .into(itemView.imagePhoto)
    }

    fun clear() {
        photo = null
        with(itemView) {
            textAuthor.text = null
            textError.text = null
            imgLike.clearColorFilter()
            glide.clear(imagePhoto)
            imagePhoto.setImageDrawable(null)
        }
    }

}