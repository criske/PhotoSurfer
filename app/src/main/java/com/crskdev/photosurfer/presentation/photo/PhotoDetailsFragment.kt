package com.crskdev.photosurfer.presentation.photo


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
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
import kotlinx.android.synthetic.main.fragment_photo_details.*
import kotlinx.android.synthetic.main.progress_layout.*
import java.util.concurrent.Executor

/**
 * A simple [Fragment] subclass.
 *
 */
class PhotoDetailsFragment : Fragment(), HasUpOrBackPressedAwareness {

    companion object {
        private const val KEY_IS_DOWNLOADING = "KEY_IS_DOWNLOADING"
    }

    private lateinit var viewModel: PhotoDetailViewModel
    private var progSlideDownAnimation: ViewPropertyAnimator? = null
    private var progSlideUpAnimation: ViewPropertyAnimator? = null
    private var isDownloadShowing: Boolean = false

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val defaultPrimary = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            activity!!.window.statusBarColor = defaultPrimary
        }
        super.onDestroy()
    }

    override fun onBackOrUpPressed() {
        viewModel.cancelDownload(PhotoDetailsFragmentArgs.fromBundle(arguments).photo.id)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fabDownload.hide()

        val photo = PhotoDetailsFragmentArgs.fromBundle(arguments).photo
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



        isDownloadShowing = savedInstanceState?.getBoolean(KEY_IS_DOWNLOADING, false) ?: false
        (fabDownload as View).visibility = if (isDownloadShowing) View.GONE else View.VISIBLE

        var isAnimatingSlide = false
        viewModel.downloadLiveData
                .filter { it != DownloadProgress.NONE }
                .observe(this, Observer {
                    val isIndeterminated = it.percent == -1
                    progressBarDownload.isIndeterminate = isIndeterminated
                    if (isIndeterminated) {
                        textDownloadProgress.text = "?"

                    } else {
                        progressBarDownload.progress = it.percent
                        textDownloadProgress.text = "${it.percent}%"
                    }
                    if ((!isDownloadShowing && it.isStaringValue)
                            || it.isStaringValue
                            || (!isAnimatingSlide && cardProgressDownload.y < 0)) {
                        isAnimatingSlide = true
                        progSlideDownAnimation = cardProgressDownload.animate().translationY(50f.dpToPx(resources))
                                .onEnded {
                                    isAnimatingSlide = false
                                    (fabDownload as View).visibility = View.GONE
                                    btnTestDownload.visibility = View.GONE
                                    isDownloadShowing = true
                                }.apply { start() }
                        (fabDownload as View).visibility = View.GONE

                    }
                    if (it.doneOrCanceled && isDownloadShowing && !isAnimatingSlide) {
                        isAnimatingSlide = true
                        progSlideUpAnimation = cardProgressDownload.animate().translationY((-200f).dpToPx(resources))
                                .onEnded {
                                    isAnimatingSlide = false
                                    (fabDownload as View).visibility = View.VISIBLE
                                    btnTestDownload.visibility = View.VISIBLE
                                    isDownloadShowing = false
                                }.apply { start() }
                    }

                })

        fabDownload.setOnClickListener { v ->
            id?.let {
                if (!AppPermissions.hasStoragePermission(v.context)) {
                    AppPermissions.requestStoragePermission(activity!!)
                } else
                    viewModel.download(it)
            }
        }

        imgBtnDownloadCancel.setOnClickListener {
            id?.let { viewModel.cancelDownload(it) }
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
        outState.putBoolean(KEY_IS_DOWNLOADING, isDownloadShowing)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        fabDownload.hide()
        fabDownload.cancelPendingInputEvents()
        progSlideUpAnimation?.cancel()
        progSlideDownAnimation?.cancel()
        super.onDestroyView()
    }


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


    @Volatile
    private var isDownloadCanceled: Boolean = false

    private val lock = Any()

    fun download(id: String) {
        backgroundExecutor.execute {
            photoRepository.download(id, object : PhotoRepository.Callback {
                override fun onSuccess(data: Any?) {
                    val downloadProgress = data as DownloadProgress
                    if(downloadProgress.doneOrCanceled){
                        uiExecutor.execute { // make sure the done value is consumed
                            downloadLiveData.value = downloadProgress
                        }
                    }else {
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
        synchronized(lock) {
            isDownloadCanceled = true
        }
    }

    override fun onCleared() {
        super.onCleared()
    }


}
