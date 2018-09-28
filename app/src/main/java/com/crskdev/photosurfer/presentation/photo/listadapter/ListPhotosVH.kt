package com.crskdev.photosurfer.presentation.photo.listadapter

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.util.IntentUtils
import kotlinx.android.synthetic.main.item_list_photos.view.*

class ListPhotosVH(private val glide: RequestManager,
                   view: View,
                   private val action: (ListPhotosAdapter.ActionWhat, Photo, Boolean) -> Unit) : RecyclerView.ViewHolder(view) {


    private var photo: Photo? = null

    private var enabledActions: Boolean = true

    init {
        itemView.txtUnsplash.setOnClickListener { _ ->
            itemView.context.startActivity(IntentUtils.webIntentUnsplash())
        }
        itemView.imagePhoto.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.PHOTO_DETAIL, it, enabledActions) } }
        itemView.textAuthor.setOnClickListener { _ ->
            photo?.let {
                //action(ListPhotosAdapter.ActionWhat.AUTHOR, it, enabledActions)
                itemView.context.startActivity(IntentUtils.webIntentUnsplashPhotographer(it.authorUsername))
            }
        }
        itemView.imgLike.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.LIKE, it.copy(likedByMe = !it.likedByMe), enabledActions) } }
        itemView.imgCollection.setOnClickListener { _ -> photo?.let { action(ListPhotosAdapter.ActionWhat.COLLECTION, it, enabledActions) } }
    }

    fun bind(photo: Photo, enabledActions: Boolean) {
        this.photo = photo
        this.enabledActions = enabledActions

        itemView.imgLike.isVisible = enabledActions
        itemView.imgCollection.isVisible = enabledActions
        itemView.textAuthor.isVisible = enabledActions

        if (photo.likedByMe) {
            itemView.imgLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorLike))
        }
        itemView.textAuthor.text = photo.authorFullName
        glide.asDrawable()
                .load(photo.urls[ImageType.SMALL])
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_logo)
                        .transforms(CenterCrop(), RoundedCorners(8)))
                //.transition(DrawableTransitionOptions().crossFade())
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