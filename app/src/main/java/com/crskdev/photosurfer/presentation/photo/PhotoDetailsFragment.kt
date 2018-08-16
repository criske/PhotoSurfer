package com.crskdev.photosurfer.presentation.photo


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorFilter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.crskdev.photosurfer.*
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.deparcelize
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.SingleLiveEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_photo_details.*
import kotlinx.android.synthetic.main.progress_layout.*
import java.util.concurrent.Executor
import kotlin.properties.Delegates

class PhotoDetailsFragment : Fragment(), HasUpOrBackPressedAwareness, HasAppPermissionAwareness {

    companion object {
        private const val KEY_UI_STATE = "com.crskdev.photosurfer.presentation.photo.PhotoDetailsFragment:UIState"
    }

    private lateinit var viewModel: PhotoDetailViewModel
    private var progSlideDownAnimation: ViewPropertyAnimator? = null
    private var progSlideUpAnimation: ViewPropertyAnimator? = null

    private var uiState: UIState by Delegates.observable(UIState.INITIAL) { _, _, new ->
        //make sure we have view created
        if (view != null) {
            (fabDownload as View).visibility = if (new.isDownloadShowing || !new.isPhotoDisplayed)
                View.GONE else View.VISIBLE
            progressBarLoading.isVisible = !new.isPhotoDisplayed && !new.hasDisplayingError
        }
    }

    private val photo by lazy(LazyThreadSafetyMode.NONE) { PhotoDetailsFragmentArgs.fromBundle(arguments).photo }
    private val glide by lazy { Glide.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dependencyGraph = context!!.dependencyGraph()
                @Suppress("UNCHECKED_CAST")
                return PhotoDetailViewModel(
                        dependencyGraph.uiThreadExecutor,
                        dependencyGraph.backgroundThreadExecutor,
                        dependencyGraph.photoRepository) as T
            }
        }).get(PhotoDetailViewModel::class.java)
    }

    override fun onDestroy() {
        val defaultPrimary = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
        activity!!.setStatusBarColor(defaultPrimary)
        super.onDestroy()
    }

    override fun onBackOrUpPressed() {
        glide.clear(imagePhoto)
        viewModel.cancelDownload(PhotoDetailsFragmentArgs.fromBundle(arguments).photo.id)
    }

    override fun onPermissionsGranted(permissions: List<String>) {
        uiState.pendingPermissionForDownloadPhotoId?.let {
            viewModel.download(photo.deparcelize())
        }
        uiState = uiState.copy(pendingPermissionForDownloadPhotoId = null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        uiState = savedInstanceState?.getParcelable(KEY_UI_STATE) ?: uiState
        subscribeToViewModel(view)

        displayPhoto()
        fabDownload.setOnClickListener { v ->
            if (!AppPermissions.hasStoragePermission(v.context)) {
                uiState = uiState.copy(pendingPermissionForDownloadPhotoId = photo.id)
                AppPermissions.requestStoragePermission(activity!!)
            } else
                viewModel.download(photo.deparcelize())
        }

        imgBtnDownloadCancel.setOnClickListener {
            viewModel.cancelDownload(photo.id)
        }

    }

    private fun displayPhoto() {
        uiState = uiState.copy(hasDisplayingError = false)
        glide.asBitmap()
                .load(photo.urls["full"])
                .apply(RequestOptions().centerCrop())
                .addListener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?,
                                                 model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        uiState = uiState.copy(isPhotoDisplayed = true)
                        setPalette(this@PhotoDetailsFragment.photo.id, resource)
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?,
                                              target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        view?.let {
                            Snackbar.make(it, e?.message
                                    ?: "Unknown Error", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Retry") { _ -> displayPhoto() }
                                    .show()
                        }
                        uiState = uiState.copy(hasDisplayingError = true)
                        return true
                    }
                })
                .into(imagePhoto)
    }

    private fun subscribeToViewModel(view: View) {
        viewModel.paletteLiveData.observe(this, Observer {
            it[photo.id]?.let { palette ->
                val darkVibrantColor = palette.getDarkVibrantColor(ContextCompat
                        .getColor(view.context, R.color.colorPrimaryDark))
                activity?.setStatusBarColor(darkVibrantColor)
                val accent = ContextCompat.getColor(view.context, R.color.colorAccent)
                fabDownload.backgroundTintList = ColorStateList.valueOf(palette.getMutedColor(accent))
                val progressColorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(accent)
                progressBarDownload.progressDrawable.colorFilter = progressColorFilter
                progressBarDownload.indeterminateDrawable.colorFilter = progressColorFilter
                imgBtnDownloadCancel.drawable.setColorFilter(
                        palette.getMutedColor(accent), PorterDuff.Mode.SRC_ATOP)
                textDownloadProgress.setTextColor(darkVibrantColor)
            }
        })
        var isAnimatingSlide = false
        viewModel.downloadLiveData
                .filter { it != DownloadProgress.NONE }
                .observe(this, Observer {
                    val isIndeterminate = it.percent == -1
                    progressBarDownload.isIndeterminate = isIndeterminate

                    if (isIndeterminate) {
                        textDownloadProgress.text = "?"
                    } else {
                        progressBarDownload.progress = it.percent
                        textDownloadProgress.text = "${it.percent}%"
                    }
                    if ((!uiState.isDownloadShowing && it.isStaringValue)
                            || it.isStaringValue
                            || (!isAnimatingSlide && cardProgressDownload.y < 0)) {
                        isAnimatingSlide = true
                        progSlideDownAnimation = cardProgressDownload.animate().translationY(50f.dpToPx(resources))
                                .onEnded {
                                    isAnimatingSlide = false
                                    uiState = uiState.copy(isDownloadShowing = true)
                                }.apply { start() }
                        (fabDownload as View).visibility = View.GONE

                    }
                    if (it.doneOrCanceled && uiState.isDownloadShowing && !isAnimatingSlide) {
                        isAnimatingSlide = true
                        progSlideUpAnimation = cardProgressDownload.animate().translationY((-200f).dpToPx(resources))
                                .onEnded {
                                    isAnimatingSlide = false
                                    uiState = uiState.copy(isDownloadShowing = false)
                                }.apply { start() }
                    }

                })

        viewModel.isDownloadedLiveData.observe(this, Observer {
            Toast.makeText(this.context, "Photo already downloaded!", Toast.LENGTH_SHORT).show()
        })
        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this.context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    fun setPalette(id: String?, bitmap: Bitmap?) {
        if (id != null && bitmap != null) {
            if (viewModel.paletteLiveData.value?.get(id) == null) {
                Palette.from(bitmap).generate {
                    if (it != null)
                        viewModel.updatePalette(id, it)
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_UI_STATE, uiState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        progSlideUpAnimation?.cancel()
        progSlideDownAnimation?.cancel()
        super.onDestroyView()
    }


    @Parcelize
    data class UIState(val isDownloadShowing: Boolean = false,
                       val isPhotoDisplayed: Boolean = false,
                       val hasDisplayingError: Boolean = false,
                       val pendingPermissionForDownloadPhotoId: String? = null) : Parcelable {
        companion object {
            val INITIAL = UIState()
        }
    }
}

class PhotoDetailViewModel(
        private val uiExecutor: Executor,
        private val backgroundExecutor: Executor,
        private val photoRepository: PhotoRepository) : ViewModel() {

    val errorLiveData = SingleLiveEvent<Throwable>()

    val isDownloadedLiveData = SingleLiveEvent<Unit>()

    val paletteLiveData = MutableLiveData<Map<String, Palette>>().apply {
        value = emptyMap()
    }

    val downloadLiveData = MutableLiveData<DownloadProgress>().apply {
        value = DownloadProgress.NONE
    }

    fun updatePalette(id: String, palette: Palette) {
        val old = paletteLiveData.value
        paletteLiveData.value = mutableMapOf<String, Palette>().apply {
            old?.let { putAll(it) }
            put(id, palette)
        }
    }


    fun download(photo: Photo) {
        backgroundExecutor.execute {
            val isDownloaded = photoRepository.isDownloaded(photo.id)
            if (isDownloaded) {
                isDownloadedLiveData.postValue(Unit)
            } else {
                photoRepository.download(photo, object : Repository.Callback<DownloadProgress> {
                    override fun onSuccess(data: DownloadProgress, extras: Any?) {
                        val downloadProgress = data as DownloadProgress
                        if (downloadProgress.doneOrCanceled) {
                            uiExecutor.execute {
                                // make sure the done value is consumed
                                downloadLiveData.value = downloadProgress
                            }
                        } else {
                            downloadLiveData.postValue(downloadProgress)
                        }
                    }

                    override fun onError(error: Throwable) {
                        errorLiveData.postValue(error)
                    }
                })
            }
        }

    }

    fun cancelDownload(id: String) {
        photoRepository.cancel()
    }

}
