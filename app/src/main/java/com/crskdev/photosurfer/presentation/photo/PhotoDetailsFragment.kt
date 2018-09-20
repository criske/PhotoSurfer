package com.crskdev.photosurfer.presentation.photo


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
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
import com.crskdev.photosurfer.services.permission.AppPermissionsHelper
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.deparcelize
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.services.permission.HasAppPermissionAwareness
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.setStatusBarColor
import com.crskdev.photosurfer.util.dpToPx
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import com.crskdev.photosurfer.util.livedata.filter
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_photo_details.*
import kotlinx.android.synthetic.main.progress_layout.*

class PhotoDetailsFragment : Fragment(), HasUpOrBackPressedAwareness, HasAppPermissionAwareness {

    private lateinit var viewModel: PhotoDetailViewModel
    private var progSlideDownAnimation: ViewPropertyAnimator? = null
    private var progSlideUpAnimation: ViewPropertyAnimator? = null

    private lateinit var photo: Photo
    private val glide by lazy { Glide.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this) {
            PhotoDetailViewModel(context!!.dependencyGraph().photoRepository)
        }
        photo = PhotoDetailsFragmentArgs.fromBundle(arguments)
                .photo.deparcelize()
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

    override fun onPermissionsGranted(permissions: List<String>, enqueuedActionArg: String?) {
        viewModel.download(photo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToViewModel(view)
        displayPhoto()

        setLikeButton(photo.likedByMe)

        fabDownload.setOnClickListener { v ->
            if (!AppPermissionsHelper.hasStoragePermission(v.context)) {
                AppPermissionsHelper.requestStoragePermission(activity!!)
            } else
                viewModel.download(photo)
        }

        imgBtnDownloadCancel.setOnClickListener {
            viewModel.cancelDownload(photo.id)
        }

        btnPhotoLike.setOnClickListener {
            viewModel.like(photo.copy(likedByMe = !photo.likedByMe))
        }
    }

    private fun setLikeButton(like: Boolean) {
        val color = if (like) {
            ContextCompat.getColor(context!!, R.color.colorLike)
        } else {
            Color.WHITE
        }
        btnPhotoLike.setColorFilter(color)
    }

    private fun displayPhoto() {
        glide.asBitmap()
                .load(photo.urls[ImageType.FULL])
                .apply(RequestOptions().centerCrop())
                .addListener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?,
                                                 model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        setPalette(this@PhotoDetailsFragment.photo.id, resource)
                        viewModel.photoDisplayedLiveData.value = true
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?,
                                              target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        view?.let {
                            viewModel.photoDisplayedLiveData.value = true
                            Snackbar.make(it, e?.message
                                    ?: "Unknown Error", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Retry") { _ ->
                                        viewModel.photoDisplayedLiveData.value = false
                                        displayPhoto()
                                    }
                                    .show()
                        }
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
        viewModel.isDownloadedLiveData.observe(this, Observer {
            Toast.makeText(this.context, "Photo already downloaded!", Toast.LENGTH_SHORT).show()
        })
        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this.context, it.message, Toast.LENGTH_SHORT).show()
        })
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
                })
        viewModel.downloadStateLiveData.observe(this, Observer {
            if (it == PhotoDetailViewModel.DOWNLOADING) {
                progSlideDownAnimation = cardProgressDownload.animate().translationY(50f.dpToPx(resources)).apply { start() }
            } else if (cardProgressDownload.y > 0) {
                progSlideUpAnimation = cardProgressDownload.animate().translationY((-200f).dpToPx(resources))
                        .apply { start() }
            }
            with(it!= PhotoDetailViewModel.DOWNLOADING){
                (fabDownload as View).isVisible = this
                btnPhotoLike.isVisible = this
            }

        })
        viewModel.photoDisplayedLiveData.observe(this, Observer { displayed ->
            val enabledActions = PhotoDetailsFragmentArgs.fromBundle(arguments).enabledActions
            progressBarLoading.isVisible = !displayed
            with(displayed && enabledActions) {
                (fabDownload as View).isVisible = this
                btnPhotoLike.isVisible = this
            }
        })
        viewModel.likeLiveData.observe(this, Observer { liked ->
            setLikeButton(liked)
            photo = photo.copy(likedByMe = liked)
            arguments?.putParcelable("photo", photo.parcelize())
        })
        viewModel.needsAuthLiveData.observe(this, Observer {
            view.context.dependencyGraph().authNavigatorMiddleware.navigateToLogin(activity!!)
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

    override fun onDestroyView() {
        progSlideUpAnimation?.cancel()
        progSlideDownAnimation?.cancel()
        super.onDestroyView()
    }

}

class PhotoDetailViewModel(
        private val photoRepository: PhotoRepository) : ViewModel() {

    companion object {
        const val IDLE = 0
        const val DOWNLOADING = 1
    }

    val errorLiveData = SingleLiveEvent<Throwable>()

    val isDownloadedLiveData = SingleLiveEvent<Unit>()

    val likeLiveData = MutableLiveData<Boolean>()

    val needsAuthLiveData = SingleLiveEvent<Unit>()

    val paletteLiveData = MutableLiveData<Map<String, Palette>>().apply {
        value = emptyMap()
    }

    val downloadLiveData = MutableLiveData<DownloadProgress>()

    val photoDisplayedLiveData = MutableLiveData<Boolean>().apply {
        value = false
    }

    val downloadStateLiveData = MediatorLiveData<Int>().apply {
        value = IDLE
        addSource(downloadLiveData) {
            if (it.isStaringValue)
                value = DOWNLOADING
            else if (it.doneOrCanceled)
                value = IDLE
        }
        addSource(errorLiveData) {
            value = IDLE
        }
    }

    fun updatePalette(id: String, palette: Palette) {
        val old = paletteLiveData.value
        paletteLiveData.value = mutableMapOf<String, Palette>().apply {
            old?.let { putAll(it) }
            put(id, palette)
        }
    }


    fun download(photo: Photo) {
        val isDownloaded = photoRepository.isDownloaded(photo.id)
        if (isDownloaded) {
            isDownloadedLiveData.value = Unit
        } else {
            photoRepository.download(photo, object : Repository.Callback<DownloadProgress> {
                override fun onSuccess(data: DownloadProgress, extras: Any?) {
                    downloadLiveData.value = data
                }

                override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                    errorLiveData.value = error
                }
            })
        }

    }

    fun like(photo: Photo) {
        photoRepository.like(photo, object : Repository.Callback<Boolean> {
            override fun onSuccess(data: Boolean, extras: Any?) {
                likeLiveData.value = data
            }

            override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                if (!isAuthenticationError) {
                    errorLiveData.value = error
                } else {
                    needsAuthLiveData.value = Unit
                }
            }
        })
    }


    fun cancelDownload(id: String) {
        photoRepository.cancel()
    }

}
