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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorFilter
import androidx.core.view.postDelayed
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
import com.crskdev.photosurfer.data.local.photo.PhotoRepository
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.SingleLiveEvent
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_photo_details.*
import kotlinx.android.synthetic.main.progress_layout.*
import java.util.concurrent.Executor

/**
 * A simple [Fragment] subclass.
 *
 */
class PhotoDetailsFragment : Fragment(), HasUpOrBackPressedAwareness, HasAppPermissionAwareness {

    companion object {
        private const val KEY_UI_STATE = "com.crskdev.photosurfer.presentation.photo.PhotoDetailsFragment:UIState"
    }

    private lateinit var viewModel: PhotoDetailViewModel
    private var progSlideDownAnimation: ViewPropertyAnimator? = null
    private var progSlideUpAnimation: ViewPropertyAnimator? = null
    private var uiState: UIState = UIState()
    private val photo by lazy(LazyThreadSafetyMode.NONE) { PhotoDetailsFragmentArgs.fromBundle(arguments).photo }

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
        uiState = savedInstanceState?.getParcelable(KEY_UI_STATE) ?: uiState
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val defaultPrimary = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            activity!!.window.statusBarColor = defaultPrimary
        }
        super.onDestroy()
    }

    override fun onBackOrUpPressed() {
        viewModel.cancelDownload(PhotoDetailsFragmentArgs.fromBundle(arguments).photo.id)
    }

    override fun onPermissionsGranted(permissions: List<String>) {
        uiState.pendingPermissionForDownloadPhotoId?.let {
            viewModel.download(it)
        }
        uiState = uiState.copy(pendingPermissionForDownloadPhotoId = null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fabDownload.hide()

        val id = photo.id

        Glide.with(this).asBitmap()
                .load(photo.urls["full"])
                .apply(RequestOptions().centerCrop())
                .addListener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?,
                                                 model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        setPalette(id, resource)
                        view.postDelayed(300) {
                            fabDownload.show()
                        }
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?,
                                              target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean = false
                })
                .into(imagePhoto)

        viewModel.paletteLiveData.observe(this, Observer {
            it[id]?.let { palette ->
                val darkVibrantColor = palette.getDarkVibrantColor(ContextCompat
                        .getColor(view.context, R.color.colorPrimaryDark))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity!!.window.statusBarColor = darkVibrantColor
                }
                val accent = ContextCompat.getColor(view.context, R.color.colorAccent)
                fabDownload.backgroundTintList = ColorStateList.valueOf(palette.getMutedColor(accent))
                val progreessColorFilter = PorterDuff.Mode.SRC_ATOP
                        .toColorFilter(accent)
                progressBarDownload.progressDrawable.colorFilter = progreessColorFilter
                progressBarDownload.indeterminateDrawable.colorFilter = progreessColorFilter
                imgBtnDownloadCancel.drawable.setColorFilter(
                        palette.getMutedColor(accent), PorterDuff.Mode.SRC_ATOP)
                textDownloadProgress.setTextColor(darkVibrantColor)
            }
        })


        (fabDownload as View).visibility = if (uiState.isDownloadShowing) View.GONE else View.VISIBLE
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
                                    (fabDownload as View).visibility = View.GONE
                                    uiState = uiState.copy(isDownloadShowing = true)
                                }.apply { start() }
                        (fabDownload as View).visibility = View.GONE

                    }
                    if (it.doneOrCanceled && uiState.isDownloadShowing && !isAnimatingSlide) {
                        isAnimatingSlide = true
                        progSlideUpAnimation = cardProgressDownload.animate().translationY((-200f).dpToPx(resources))
                                .onEnded {
                                    isAnimatingSlide = false
                                    (fabDownload as View).visibility = View.VISIBLE
                                    uiState = uiState.copy(isDownloadShowing = false)
                                }.apply { start() }
                    }

                })

        fabDownload.setOnClickListener { v ->
            if (!AppPermissions.hasStoragePermission(v.context)) {
                uiState = uiState.copy(pendingPermissionForDownloadPhotoId = photo.id)
                AppPermissions.requestStoragePermission(activity!!)
            } else
                viewModel.download(id)
        }

        imgBtnDownloadCancel.setOnClickListener {
            viewModel.cancelDownload(id)
        }

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
        fabDownload.hide()
        fabDownload.cancelPendingInputEvents()
        progSlideUpAnimation?.cancel()
        progSlideDownAnimation?.cancel()
        super.onDestroyView()
    }


    @Parcelize
    data class UIState(val isDownloadShowing: Boolean = false,
                       val pendingPermissionForDownloadPhotoId: String? = null) : Parcelable
}

class PhotoDetailViewModel(
        private val uiExecutor: Executor,
        private val backgroundExecutor: Executor,
        private val photoRepository: PhotoRepository) : ViewModel() {

    val errorLiveData = SingleLiveEvent<Throwable>()

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


    fun download(id: String) {
        backgroundExecutor.execute {
            photoRepository.download(id, object : PhotoRepository.Callback {
                override fun onSuccess(data: Any?) {
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

    fun cancelDownload(id: String) {
        photoRepository.cancel()
    }

    override fun onCleared() {
        super.onCleared()
    }


}
