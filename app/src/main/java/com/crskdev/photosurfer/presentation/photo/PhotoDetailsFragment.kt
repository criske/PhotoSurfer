package com.crskdev.photosurfer.presentation.photo


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

import com.crskdev.photosurfer.R
import kotlinx.android.synthetic.main.fragment_photo_details.*

/**
 * A simple [Fragment] subclass.
 *
 */
class PhotoDetailsFragment : Fragment() {

    private lateinit var viewModel: PhotoDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(PhotoDetailViewModel::class.java)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val defaultPrimary = ContextCompat.getColor(view.context, R.color.colorPrimaryDark)
                    activity!!.window.statusBarColor = palette.getDarkVibrantColor(defaultPrimary)
                }
                val accent = ContextCompat.getColor(view.context, R.color.colorAccent)
                fabDownload.backgroundTintList = ColorStateList.valueOf(palette.getMutedColor(accent))
            }
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

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val defaultPrimary = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            activity!!.window.statusBarColor = defaultPrimary
        }
        super.onDestroy()
    }
}

class PhotoDetailViewModel : ViewModel() {

    val paletteLiveData = MutableLiveData<Map<String, Palette>>().apply {
        value = emptyMap()
    }

    fun updatePalette(id: String, palette: Palette) {
        val old = paletteLiveData.value
        paletteLiveData.value = mutableMapOf<String, Palette>().apply {
            old?.let { putAll(it) }
            put(id, palette)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
