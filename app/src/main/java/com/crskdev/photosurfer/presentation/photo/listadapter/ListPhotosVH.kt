package com.crskdev.photosurfer.presentation.photo.listadapter

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.util.IntentUtils
import com.crskdev.photosurfer.util.getHitRect
import com.crskdev.photosurfer.util.glide.*
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.item_list_photos.view.*
import kotlinx.android.synthetic.main.item_saved_list_photos.view.*

class ListPhotosVH(private val glide: RequestManager,
                   view: View,
                   private val action: (ListPhotosAdapter.ActionWhat, Photo) -> Unit) :
        BindViewHolder<Photo>(view) {


    init {
        itemView.textUnsplash.setOnClickListener { _ ->
            itemView.context.startActivity(IntentUtils.webIntentUnsplash())
        }
        itemView.imagePhotoDetails.setOnClickListener { _ -> model?.let { action(ListPhotosAdapter.ActionWhat.PHOTO_DETAIL, it) } }
        itemView.textAuthor.setOnClickListener { _ ->
            model?.let {
                //action(ListPhotosAdapter.ActionWhat.AUTHOR, it, enabledActions)
                itemView.context.startActivity(IntentUtils.webIntentUnsplashPhotographer(it.authorUsername))
            }
        }
        itemView.imgLike.setOnClickListener { _ -> model?.let { action(ListPhotosAdapter.ActionWhat.LIKE, it.copy(likedByMe = !it.likedByMe)) } }
        itemView.imgCollection.setOnClickListener { _ -> model?.let { action(ListPhotosAdapter.ActionWhat.COLLECTION, it) } }
    }

    override fun onBindModel(model: Photo) {
        if (model.likedByMe) {
            itemView.imgLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorLike))
        }
        itemView.textAuthor.text = model.authorFullName

        itemView.post {
            glide.asBitmapPalette()
                    .load(model.urls[ImageType.SMALL])
                    .setSamplingRegions(mapOf(
                            itemView.textUnsplash.id to itemView.textUnsplash.getHitRect(),
                            itemView.textAuthor.id to itemView.textAuthor.getHitRect()
                    ))
                    .apply(RequestOptions()
                            .placeholder(R.drawable.ic_logo)
                            .transforms(CenterCrop(), RoundedCorners(8)))
                    .onError {
                        itemView.textError.text = it.message
                    }
                    .into(itemView.imagePhotoDetails) { bp ->
                        bp.paletteRegions[itemView.textUnsplash.id]?.dominantSwatch?.let {
                            itemView.textUnsplash.setTextColor(it.bodyTextColor)
                        }
                        bp.paletteRegions[itemView.textAuthor.id]?.dominantSwatch?.let {
                            itemView.textAuthor.setTextColor(it.bodyTextColor)
                        }
                    }
        }
    }


    override fun unBind() {
        model = null
        with(itemView) {
            textAuthor.text = null
            textError.text = null
            imgLike.clearColorFilter()
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
                action(ListPhotosAdapter.ActionWhat.SAVED_PHOTO_DETAIL, it)
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
                    bp.paletteRegions[BitmapPalette.NO_REGIONS_ID]?.dominantSwatch?.bodyTextColor?.let { it ->
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