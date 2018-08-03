package com.crskdev.photosurfer.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.crskdev.photosurfer.R
import kotlinx.android.synthetic.main.fragment_list_photos.*
import kotlinx.android.synthetic.main.item_list_photos.view.*
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by Cristian Pela on 01.08.2018.
 */
class ListPhotosFragment : Fragment() {

    private lateinit var model: ListPhotosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(activity!!).get(ListPhotosViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val glide = Glide.with(this)
        recyclerListPhotos.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ListPhotosAdapter(LayoutInflater.from(context), glide)
        }
        model.photosData.observe(this, Observer {
            it?.let {
                (recyclerListPhotos.adapter as ListPhotosAdapter).submitList(it)
            }
        })


    }
}

class ListPhotosAdapter(private val layoutInflater: LayoutInflater,
                        private val glide: RequestManager) : PagedListAdapter<Photo, ListPhotosVH>(
        object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListPhotosVH =
            layoutInflater.inflate(R.layout.item_list_photos, parent, false).let {
                ListPhotosVH(glide, it)
            }


    override fun onBindViewHolder(viewHolder: ListPhotosVH, position: Int) {
        getItem(position)
                ?.let { viewHolder.bind(it) }
                ?: viewHolder.clear()
    }

}


class ListPhotosVH(private val glide: RequestManager, view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    private lateinit var photo: Photo

    fun bind(photo: Photo) {
        this.photo = photo
        itemView.textPhotoId.text = photo.id
    }

    fun clear() {
        itemView.textPhotoId.text = null
    }

}

class ListPhotosViewModel : ViewModel() {

    companion object {

        private var ID_GENERATOR = 0

        private fun createPhoto() =
                Photo((ID_GENERATOR++).toString(), 0L, 0L, 300, 300, "",
                        emptyMap(), emptyList(), 1, false, 1, "1", "Foo")

    }

    val photosData = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPrefetchDistance(10)
            .setPageSize(10)
            .build()
            .let {
                LivePagedListBuilder<String, Photo>(PhotoSourceFactory(), it)
                        .setFetchExecutor(BackgroundThreadExecutor())
                        .setBoundaryCallback(object : PagedList.BoundaryCallback<Photo>() {
                            override fun onItemAtEndLoaded(itemAtEnd: Photo) {
                                super.onItemAtEndLoaded(itemAtEnd)
                            }

                            override fun onItemAtFrontLoaded(itemAtFront: Photo) {
                                super.onItemAtFrontLoaded(itemAtFront)
                            }
                        })
                        .build()
            }

    private class PhotoSourceFactory : DataSource.Factory<String, Photo>() {
        @Suppress("UNCHECKED_CAST")
        override fun create(): DataSource<String, Photo> = TiledPhotoDataSource() as DataSource<String, Photo>
    }

    private class TiledPhotoDataSource : PositionalDataSource<Photo>() {

        private var photos = listOf<Photo>()

        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Photo>) {
            Log.d(this.toString(), "${params.loadSize} pos: ${params.startPosition}")
            Thread.sleep(2000)
            val position = params.startPosition
            val remainsToCreate = when {
                position > photos.lastIndex -> (position - photos.lastIndex) * params.loadSize
                photos.lastIndex - position <= params.loadSize -> params.loadSize
                else -> 0
            }
            photos += createNextPhotos(remainsToCreate)
            callback.onResult(photos.subList(position, position + params.loadSize))
        }

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Photo>) {
            photos += createNextPhotos(params.requestedLoadSize)
            val subList = photos.subList(0, params.requestedLoadSize)
            callback.onResult(subList, params.requestedStartPosition, subList.size * params.pageSize)
        }

        private fun createNextPhotos(size: Int): List<Photo> {
            var count = 0
            val list = mutableListOf<Photo>()
            while (count < size) {
                list.add(createPhoto())
                count++
            }
            return list
        }

    }

}

internal class UiThreadExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mHandler.post(command)
    }
}

internal class BackgroundThreadExecutor : Executor {
    private val executorService = Executors.newSingleThreadExecutor()

    override fun execute(command: Runnable) {
        executorService.execute(command)
    }
}



data class Photo(val id: String, val createdAt: Long, val updatedAt: Long,
                 val width: Int, val height: Int, val colorString: String,
                 val links: Map<String, String>,
                 val categories: List<String>,
                 val likes: Int, val likedByMe: Boolean, val views: Int,
                 val authorId: String,
                 val authorUsername: String)

