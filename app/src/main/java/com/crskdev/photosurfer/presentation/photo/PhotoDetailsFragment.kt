package com.crskdev.photosurfer.presentation.photo


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
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

import com.crskdev.photosurfer.presentation.executors.BackgroundThreadExecutor
import com.crskdev.photosurfer.presentation.executors.UIThreadExecutor
import com.google.android.material.behavior.SwipeDismissBehavior
import kotlinx.android.synthetic.main.fragment_photo_details.*
import kotlinx.android.synthetic.main.progress_layout.*
import java.util.concurrent.Executor

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
                return PhotoDetailViewModel(UIThreadExecutor(), BackgroundThreadExecutor()) as T
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
                progressBarDownload.progressDrawable.colorFilter = PorterDuff.Mode.SRC_ATOP
                        .toColorFilter(accent)
                imgBtnDownloadCancel.drawable.setColorFilter(
                        palette.getMutedColor(accent), PorterDuff.Mode.SRC_ATOP)
                textDownloadProgress.setTextColor(darkVibrantColor)
            }
        })

        var isAnimatingTranslation = false
        val originalCardX = IntArray(2)
                .apply { cardProgressDownload.getLocationOnScreen(this)}[0]

        viewModel.downloadLiveData
                .filter { it != PhotoDetailViewModel.DownloadProgress.NONE }
                .observe(this, Observer {
                    progressBarDownload.progress = it.percent
                    textDownloadProgress.text = "${it.percent}%"
                    if (it.isStaringValue || (!isAnimatingTranslation && cardProgressDownload.y < 0)) {
                        isAnimatingTranslation = true
                        cardProgressDownload.animate().translationY(50f.dpToPx(resources))
                                .onEnded {
                                    isAnimatingTranslation = false
                                    fabDownload.hide()
                                }
                                .start()
                    }
                    if (it.doneOrCanceled && !isAnimatingTranslation) {
                        isAnimatingTranslation = true
                        cardProgressDownload.animate().translationY((-200f).dpToPx(resources))
                                .onEnded {
                                    isAnimatingTranslation = false
                                    println("Original $originalCardX Curr:  ${cardProgressDownload.x}")
                                    cardProgressDownload.x = 100f
                                    fabDownload.show()
                                }
                                .start()
                    }
                })

        fabDownload.setOnClickListener {
            id?.let { viewModel.download(it) }
        }

        imgBtnDownloadCancel.setOnClickListener {
            id?.let { viewModel.cancelDownload(it) }
        }

        (cardProgressDownload.layoutParams as CoordinatorLayout.LayoutParams).apply {
            behavior = SwipeDismissBehavior<CardView>().apply {
                setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_END_TO_START)
                setDragDismissDistance(200f)
                setListener(object : SwipeDismissBehavior.OnDismissListener {
                    override fun onDismiss(v: View) {
                        println("dimissss")
                        id?.let { viewModel.cancelDownload(it) }
                    }

                    override fun onDragStateChanged(state: Int) {
                    }
                })
            }
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
        private val backgroundExecutor: Executor) : ViewModel() {

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
            var count = 0
            downloadLiveData.postValue(DownloadProgress(count, true, false))
            var lastTime = System.currentTimeMillis()
            synchronized(lock) {
                isDownloadCanceled = false
            }
            while (count <= 100) {
                synchronized(lock) {
                    if (isDownloadCanceled) {
                        downloadLiveData.postValue(DownloadProgress(count, false, true))
                        count = 100
                    }
                }

                val now = System.currentTimeMillis()
                if (now - lastTime >= 100) {
                    downloadLiveData.postValue(DownloadProgress(count, false,
                            count >= 100))
                    count++
                    lastTime = now
                }
            }
            uiExecutor.execute {
                downloadLiveData.value = DownloadProgress.NONE
            }
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
            val NONE = DownloadProgress(-1, false, false)
        }
    }

}
