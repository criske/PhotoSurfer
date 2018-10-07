package com.crskdev.photosurfer.presentation.photo

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.UNSPLASH_DATE_FORMATTER
import com.crskdev.photosurfer.util.IntentUtils
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.item_photo_info_sheet.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Cristian Pela on 07.10.2018.
 */
class PhotoInfoBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val PHOTO_ID = "PHOTO_ID"

        fun show(photoId: String, fragmentManager: FragmentManager) {
            val sheetFragment = PhotoInfoBottomSheetFragment()
            sheetFragment.arguments = bundleOf(
                    PHOTO_ID to photoId
            )
            sheetFragment
                    .show(fragmentManager, "com.crskdev.photosurfer.presentation.photo.PhotoInfoBottomSheetFragment#TAG")
        }
    }

    private lateinit var viewModel: PhotoInfoBottomSheetViewModel


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val photoId = arguments?.getString(PHOTO_ID)
                ?: throw Exception("Photo ID not passed to fragment")
        viewModel = viewModelFromProvider(this) {
            PhotoInfoBottomSheetViewModel(photoId, context!!.dependencyGraph().photoRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.item_photo_info_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imagePhotoInfoClose.setOnClickListener {
            dismiss()
        }
        textPhotoInfoAuthor.setOnClickListener {
            it.tag?.toString()?.let { username ->
                startActivity(IntentUtils.webIntentUnsplashPhotographer(username))
            }
        }
        textPhotoInfoUnsplash.setOnClickListener {
            startActivity(IntentUtils.webIntentUnsplash())
        }
        viewModel.photoLiveData.observe(this, Observer {
            textPhotoInfoDescription.text = it.description.takeIf { d -> !d.isNullOrEmpty() }
                    ?: getString(R.string.no_description)
            textPhotoInfoAuthor.apply {
                text = it.authorFullName
                tag = it.authorUsername
            }
            textPhotoInfoSize.text = getString(R.string.photo_info_size, it.width, it.height)
            val date = UNSPLASH_DATE_FORMATTER.parse(it.createdAt)
            //TODO test this might not be accurate representation
            textPhotoInfoCreationDate.text = getString(R.string.photo_info_created, dateFormat.format(date))
            chipPhotoInfoColor.apply {
                val color = Color.parseColor(it.colorString)
                chipBackgroundColor = ColorStateList.valueOf(color)
            }
        })
    }

}

class PhotoInfoBottomSheetViewModel(
        photoId: String,
        photoRepository: PhotoRepository) : ViewModel() {

    val photoLiveData: LiveData<Photo> = photoRepository.getPhotoLiveData(photoId)
}