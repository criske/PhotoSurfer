package com.crskdev.photosurfer.presentation.playwave

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.PlaywavePhoto
import kotlinx.android.synthetic.main.item_slide_show.view.*

/**
 * Created by Cristian Pela on 31.10.2018.
 */
class PlaywaveSlideShowPagerAdapter(private val layoutInflater: LayoutInflater,
                                    private val glide: RequestManager,
                                    private val items: List<PlaywavePhoto>) : PagerAdapter() {

    init {
        assert(items.isNotEmpty()){
            "Playwave Photo list for pager adapter is empty"
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = Int.MAX_VALUE

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = layoutInflater.inflate(R.layout.item_slide_show, container, false)
        val realPosition = position.rem(items.size)
        bind(itemView, items[realPosition])
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val itemView = `object` as View
        unbind(itemView)
        container.removeView(itemView)
    }

    private fun bind(itemView: View, item: PlaywavePhoto) {
        with(itemView) {
            glide.asDrawable()
                    .load(item.urls[ImageType.FULL])
                    .apply(RequestOptions()
                            .error(R.drawable.ic_logo)
                            .transforms(CenterInside()))
                    .into(imageSlideShow)

        }
    }

    private fun unbind(itemView: View) {
        glide.clear(itemView.imageSlideShow)
    }
}