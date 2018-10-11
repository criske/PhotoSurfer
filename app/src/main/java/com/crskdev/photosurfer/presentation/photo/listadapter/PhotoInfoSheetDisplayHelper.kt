package com.crskdev.photosurfer.presentation.photo.listadapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.DISPLAY_DATE_FORMATTER
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.UNSPLASH_DATE_FORMATTER
import com.crskdev.photosurfer.util.IntentUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Cristian Pela on 08.10.2018.
 */
class PhotoInfoSheetDisplayHelper(private val listener: ActionsListener) {

    interface ActionsListener {

        fun onClose()

        fun onRemoveFromCollection(collectionId: Int, photoId: String)

        fun displayCollection(collectionId: Int)

    }

    private var mSheetDialog: BottomSheetDialog? = null


    private var mLayoutInflater: LayoutInflater? = null

    private var mSheetLayout: View? = null

    @SuppressLint("InflateParams")
    fun displayInfoBottomSheet(context: Context, photo: Photo) {
        if (mSheetDialog == null) {
            mSheetDialog = BottomSheetDialog(context)
                    .apply {
                        setCancelable(true)
                        setCanceledOnTouchOutside(true)
                        setOnCancelListener {
                            dismiss()
                        }
                    }
        }
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(context)
        }
        if (mSheetLayout == null) {
            mSheetLayout = mLayoutInflater!!
                    .inflate(R.layout.item_photo_info_sheet, null, false).apply {
                        findViewById<ImageButton>(R.id.imagePhotoInfoClose).setOnClickListener {
                            dismiss()
                        }
                        findViewById<TextView>(R.id.textPhotoInfoAuthor).setOnClickListener {
                            it.tag?.toString()?.let { username ->
                                context.startActivity(IntentUtils.webIntentUnsplashPhotographer(username))
                            }
                        }
                        findViewById<TextView>(R.id.textPhotoInfoUnsplash).setOnClickListener {
                            context.startActivity(IntentUtils.webIntentUnsplash())
                        }
                    }
            mSheetDialog!!.setContentView(mSheetLayout!!)
        }

        with(mSheetLayout!!) {
            findViewById<TextView>(R.id.textPhotoInfoDescription).text = photo.description.takeIf { d -> !d.isNullOrEmpty() }
                    ?: context.getString(R.string.no_description)
            findViewById<TextView>(R.id.textPhotoInfoAuthor).apply {
                text = photo.authorFullName
                tag = photo.authorUsername
            }
            findViewById<TextView>(R.id.textPhotoInfoSize).text = context.getString(R.string.photo_info_size, photo.width, photo.height)
            val date = UNSPLASH_DATE_FORMATTER.parse(photo.createdAt)

            //TODO test this might not be accurate representation
            findViewById<TextView>(R.id.textPhotoInfoCreationDate).text = context.getString(R.string.photo_info_created, DISPLAY_DATE_FORMATTER.format(date))
            findViewById<Chip>(R.id.chipPhotoInfoColor).apply {
                val color = Color.parseColor(photo.colorString)
                chipBackgroundColor = ColorStateList.valueOf(color)
                text = photo.colorString
            }
            findViewById<TextView>(R.id.textLblPhotoInfoCollections).apply {
                isVisible = photo.collections.isNotEmpty()
                if (isVisible) {
                    val count = photo.collections.size
                    text = resources.getQuantityString(R.plurals.collections_plural, count, count)
                }

            }

            val chipGroupPhotoInfoCategories = findViewById<ChipGroup>(R.id.chipGroupPhotoInfoCollections)
            chipGroupPhotoInfoCategories.removeAllViews()
            photo.collections.forEach { collection ->
                val chip = Chip(ContextThemeWrapper(context, R.style.Widget_MaterialComponents_Chip_Action)).apply {
                    text = collection.title
                    tag = collection.id
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        listener.onRemoveFromCollection(it.tag as Int, photo.id)
                    }
                    setOnClickListener {
                        listener.displayCollection(it.tag as Int)
                    }
                }
                chipGroupPhotoInfoCategories.addView(chip)
            }
        }
        mSheetDialog!!.show()
    }

    fun dismiss() {
        mSheetDialog?.dismiss()
        listener.onClose()
    }

    fun clear() {
        dismiss()
        mSheetDialog = null
        mLayoutInflater = null
        mSheetLayout = null
    }

}