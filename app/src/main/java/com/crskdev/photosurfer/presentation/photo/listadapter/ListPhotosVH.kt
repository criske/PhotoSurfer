package com.crskdev.photosurfer.presentation.photo.listadapter

import android.graphics.Rect
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.util.IntentUtils
import com.crskdev.photosurfer.util.glide.*
import com.crskdev.photosurfer.util.livedata.suffixFormat
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.item_list_photos.view.*
import kotlinx.android.synthetic.main.item_saved_list_photos.view.*

class ListPhotosVH(private val glide: RequestManager,
                   view: View,
                   private val action: (ListPhotosAdapter.ActionWhat, Photo) -> Unit) :
        BindViewHolder<Photo>(view) {

    private val authorRegionRect: Rect
    private val unsplashRegionRect: Rect

    init {
        //calculate regions for sampling palettes
        val resources = itemView.resources
        val margin = resources.getDimensionPixelSize(R.dimen.item_photo_text_margin)
        val photoHeight = resources.getDimensionPixelSize(R.dimen.item_photo_height)
        val textHeight = resources.getDimensionPixelSize(R.dimen.item_photo_text_h)
        val textWidth = resources.getDimensionPixelSize(R.dimen.item_photo_text_w)
        unsplashRegionRect = Rect(margin, margin, margin + textWidth, margin + textHeight)
        authorRegionRect = Rect(margin, photoHeight - margin - textHeight, margin + textWidth, photoHeight - margin)

        itemView.textUnsplash.setOnClickListener { _ ->
            itemView.context.startActivity(IntentUtils.webIntentUnsplash())
        }
        itemView.imagePhotoDetails.setOnClickListener { _ -> model?.let { action(ListPhotosAdapter.ActionWhat.PHOTO_FULL_SCREEN, it) } }
        itemView.imagePhotoDetails.setOnLongClickListener { _ ->
            model?.let { action(ListPhotosAdapter.ActionWhat.PHOTO_INFO, it) }
            true
        }

        itemView.textAuthor.setOnClickListener { _ ->
            model?.let {
                //action(ListPhotosAdapter.ActionWhat.AUTHOR, it, enabledActions)
                itemView.context.startActivity(IntentUtils.webIntentUnsplashPhotographer(it.authorUsername))
            }
        }
        itemView.imgPlaywave.setOnClickListener { _-> model?.let { action(ListPhotosAdapter.ActionWhat.PLAYWAVE, it) } }
        itemView.imgLike.setOnClickListener { _ -> model?.let { action(ListPhotosAdapter.ActionWhat.LIKE, it) } }
        itemView.imgCollection.setOnClickListener { _ -> model?.let { action(ListPhotosAdapter.ActionWhat.COLLECTION, it) } }
    }

    override fun onBindModel(model: Photo) {
        if (model.likedByMe) {
            itemView.imgLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorLike))
        }
        itemView.textAuthor.text = model.authorFullName
        itemView.chipLikes.apply {
            isVisible = true
            text = (model.likes).suffixFormat()
        }

        glide.asBitmapPalette()
                .load(model.urls[ImageType.SMALL])
                .setSamplingRegions(mapOf(
                        itemView.textUnsplash.id to unsplashRegionRect,
                        itemView.textAuthor.id to authorRegionRect
                ))
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_logo)
                        .transforms(CenterCrop(), RoundedCorners(8)))
                .onError {
                    itemView.textError.text = it.message
                }
                .into(itemView.imagePhotoDetails) { bp ->
                    bp.paletteSampler[itemView.textUnsplash.id].dominantSwatch?.let {
                        itemView.textUnsplash.setTextColor(it.bodyTextColor)
                    }
                    bp.paletteSampler[itemView.textAuthor.id].dominantSwatch?.let {
                        itemView.textAuthor.setTextColor(it.bodyTextColor)
                    }
                }
    }


    override fun unBind() {
        model = null
        with(itemView) {
            textAuthor.text = null
            textError.text = null
            imgLike.clearColorFilter()
            chipLikes.isVisible = false
            glide.clear(imagePhotoDetails)
        }
    }

}

class SavedListPhotosVH(private val glide: RequestManager,
                        view: View,
                        private val action: (ListPhotosAdapter.ActionWhat, Photo) -> Unit) :
        BindViewHolder<Photo>(view) {

    init {
        itemView.imageSavedPhoto.setOnClickListener { _ ->
            model?.let {
                action(ListPhotosAdapter.ActionWhat.SAVED_PHOTO_FULL_SCREEN, it)
            }
        }
        itemView.btnSavedPhotoDelete.setOnClickListener { _ ->
            model?.let {
                val context = itemView.context
                AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setOnCancelListener { d ->
                            d.dismiss()
                        }
                        .setNegativeButton(context.getString(R.string.cancel)) { d, _ ->
                            d.dismiss()
                        }
                        .setPositiveButton(android.R.string.ok) { d, _ ->
                            action(ListPhotosAdapter.ActionWhat.DELETE_SAVED_PHOTO, it)
                            d.dismiss()

                        }
                        .setMessage(context.getString(R.string.msg_delete_photo))
                        .create()
                        .show()
            }
        }
    }

    override fun onBindModel(model: Photo) {
        glide.asBitmapPalette()
                .load(model.urls[ImageType.SMALL])
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_logo)
                        .transforms(CenterCrop(), RoundedCorners(8))
                )
                //.transition(DrawableTransitionOptions().crossFade())
                .onError {
                    itemView.textSavedError.text = it.message
                }
                .into(itemView.imageSavedPhoto) { bp ->
                    bp.paletteSampler[BitmapPalette.NO_REGIONS_ID].dominantSwatch?.bodyTextColor?.let { it ->
                        itemView.textSavedUnsplash.setTextColor(it)
                    }
                }
    }

    override fun unBind() {
        model = null
        with(itemView) {
            glide.clear(imageSavedPhoto)
        }
    }

}