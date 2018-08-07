package com.crskdev.photosurfer.presentation.photo


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorFilter
import androidx.core.view.postDelayed
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
import com.crskdev.photosurfer.data.remote.RetrofitClient
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI

import com.crskdev.photosurfer.presentation.executors.BackgroundThreadExecutor
import com.crskdev.photosurfer.presentation.executors.UIThreadExecutor
import com.crskdev.photosurfer.services.GalleryPhotoSaver
import com.crskdev.photosurfer.services.PhotoSaver
import kotlinx.android.synthetic.main.fragment_photo_details.*
import kotlinx.android.synthetic.main.progress_layout.*
import java.util.concurrent.Executor
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass.
 *
 */
class PhotoDetailsFragment : Fragment() {

    private lateinit var viewModel: PhotoDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PhotoDetailViewModel(
                        UIThreadExecutor(),
                        BackgroundThreadExecutor(),
                        GalleryPhotoSaver(this@PhotoDetailsFragment.activity!!.applicationContext)) as T
            }
        }).get(PhotoDetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fabDownload.hide()

        val id = arguments?.getString("ID")

        arguments?.getString("FULL")?.let { link ->
            Glide.with(this).asBitmap()
                    .load(link)
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
        }
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

        var isAnimatingTranslation = false
        var isDownloadShowing = false
        viewModel.downloadLiveData
                .filter { it != PhotoDetailViewModel.DownloadProgress.NONE }
                .observe(this, Observer {

                    val isIndeterminated = it.percent == -1
                    progressBarDownload.isIndeterminate = isIndeterminated
                    if (isIndeterminated) {
                        textDownloadProgress.text = "?"

                    } else {
                        progressBarDownload.progress = it.percent
                        textDownloadProgress.text = "${it.percent}%"
                    }
                    println(it.doneOrCanceled)

                    if ((!isDownloadShowing && it.isStaringValue)
                            || it.isStaringValue
                            || (!isAnimatingTranslation && cardProgressDownload.y < 0)) {
                        isAnimatingTranslation = true
                        cardProgressDownload.animate().translationY(50f.dpToPx(resources))
                                .onEnded {
                                    isAnimatingTranslation = false
                                    isDownloadShowing = true
                                    fabDownload.hide()
                                }
                                .start()
                    }
                    if (it.doneOrCanceled || it.percent == 100) {
                        isAnimatingTranslation = true
                        cardProgressDownload.animate().translationY((-200f).dpToPx(resources))
                                .onEnded {
                                    isAnimatingTranslation = false
                                    isDownloadShowing = false
                                    fabDownload.show()
                                }
                                .start()
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

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val defaultPrimary = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            activity!!.window.statusBarColor = defaultPrimary
        }
        arguments?.getString("ID")?.let {
            viewModel.cancelDownload(it)
        }

        super.onDestroy()
    }
}

class PhotoDetailViewModel(
        private val uiExecutor: Executor,
        private val backgroundExecutor: Executor,
        private val photoSaver: PhotoSaver) : ViewModel() {


    private val progressListener: (Boolean, Long, Long, Boolean) -> Unit = { isStartingValue, curr, total, done ->
        println("Progress: $curr $total")
        if (total == -1L && isStartingValue) { //indeterminated
            downloadLiveData.postValue(DownloadProgress.INDETERMINATED_START)
        } else {
            val percent = (curr.toFloat() / total * 100).roundToInt()
            if (percent % 10 == 0) // backpressure relief
                downloadLiveData.postValue(DownloadProgress(percent, false, curr == total))
        }
        if (done) {
            if (total == -1L) {
                downloadLiveData.postValue(DownloadProgress.INDETERMINATED_END)
            }
            uiExecutor.execute {
                RetrofitClient.DEFAULT.removeDownloadProgressListener()
            }
        }
    }

    private val photoApi = RetrofitClient.DEFAULT.apply {
        networkClient.addDownloadProgressListener(progressListener)
    }.retrofit.create(PhotoAPI::class.java)

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
        RetrofitClient.DEFAULT.addDownloadProgressListener(progressListener)
        backgroundExecutor.execute {

            downloadLiveData.postValue(DownloadProgress(0, true, false))
            val response = photoApi.download(id).execute()

            response.body()?.source()?.let {
                photoSaver.save(id, it)
            }

            downloadLiveData.postValue(DownloadProgress.NONE)
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

    data class DownloadProgress(val percent: Int, val isStaringValue: Boolean, val doneOrCanceled: Boolean) {
        companion object {
            val NONE = DownloadProgress(Int.MIN_VALUE, false, false)
            val INDETERMINATED_START = DownloadProgress(-1, true, false)
            val INDETERMINATED_END = DownloadProgress(-1, true, false)
        }
    }

}
