package com.crskdev.photosurfer.presentation.photo.listadapter

import android.graphics.Bitmap
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
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
import com.crskdev.photosurfer.util.recyclerview.PaletteManager
import com.crskdev.photosurfer.util.recyclerview.PaletteViewHolder
import kotlinx.android.synthetic.main.item_list_photos.view.*

class ListPhotosVH(private val glide: RequestManager,
                   paletteManager: PaletteManager,
                   view: View,
                   private val action: (ListPhotosAdapter.ActionWhat, Photo, Boolean) -> Unit) :
        PaletteViewHolder<Photo>(paletteManager, view) {

    private var photo: Photo? = null

    private var enabledActions: Boolean = true

    init {
        itemView.textUnsplash.setOnClickListener { _ ->
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

    override fun bind(model: Photo) {
        this.photo = model
        this.enabledActions = true

        itemView.imgLike.isVisible = enabledActions
        itemView.imgCollection.isVisible = enabledActions
        itemView.textAuthor.isVisible = enabledActions

        if (model.likedByMe) {
            itemView.imgLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorLike))
        }
        itemView.textAuthor.text = model.authorFullName
        glide.asBitmap()
                .load(model.urls[ImageType.SMALL])
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_logo)
                        .transforms(CenterCrop(), RoundedCorners(8)))
                //.transition(DrawableTransitionOptions().crossFade())
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        itemView.textError.text = e?.message
                        return true
                    }

                    override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean{
                        registerPalette(resource)
                        return false
                    }

                })
                .into(itemView.imagePhoto)
    }

    override fun onBindPalette(palette: Palette) {
        val  vibrant = palette.dominantSwatch
        vibrant?.bodyTextColor?.let {
            itemView.textAuthor.setTextColor(it)
            itemView.textUnsplash.setTextColor(it)
        }
    }

    override fun unBind() {
        photo = null
        with(itemView) {
            textAuthor.text = null
            textError.text = null
            imgLike.clearColorFilter()
            glide.clear(imagePhoto)
            imagePhoto.setImageDrawable(null)
        }
    }

    override fun id(): String = photo?.id ?: throw Exception("View Holder is not binded to Photo")

}