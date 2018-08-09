package com.crskdev.photosurfer.presentation

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.BuildConfig
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.local.photo.PhotoRepository
import com.crskdev.photosurfer.data.remote.NetworkClient
import com.crskdev.photosurfer.data.remote.RetrofitClient
import com.crskdev.photosurfer.data.remote.auth.APIKeys
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.entities.toPhoto
import com.crskdev.photosurfer.presentation.executors.BackgroundThreadExecutor
import com.crskdev.photosurfer.presentation.executors.IOThreadExecutor
import com.crskdev.photosurfer.presentation.executors.UIThreadExecutor
import com.crskdev.photosurfer.presentation.photo.PhotoDetailViewModel
import com.crskdev.photosurfer.safeSet
import com.crskdev.photosurfer.services.GalleryPhotoSaver
import kotlinx.android.synthetic.main.fragment_list_photos.*
import kotlinx.android.synthetic.main.item_list_photos.view.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Cristian Pela on 01.08.2018.
 */
class ListPhotosFragment : Fragment() {

    private lateinit var model: ListPhotosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(activity!!, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val graph = context!!.dependencyGraph()
                @Suppress("UNCHECKED_CAST")
                return ListPhotosViewModel(
                        graph.uiThreadExecutor,
                        graph.ioThreadExecutor,
                        graph.backgroundThreadExecutor,
                        graph.photoRepository
                ) as T
            }
        }).get(ListPhotosViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val glide = Glide.with(this)
        recyclerListPhotos.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ListPhotosAdapter(LayoutInflater.from(context), glide) {
                view.findNavController().navigate(
                        ListPhotosFragmentDirections
                                .actionFragmentListPhotosToFragmentPhotoDetails(it.parcelize()))
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(0, 4, 0, 4)
                }
            })
        }
        model.photosData.observe(this, Observer { it ->
            it?.let {
                (recyclerListPhotos.adapter as ListPhotosAdapter).submitList(it)
            }
        })
        refreshListPhotos.setOnClickListener {
            model.refresh()
        }

    }
}

class ListPhotosAdapter(private val layoutInflater: LayoutInflater,
                        private val glide: RequestManager,
                        private val action: (Photo) -> Unit) : PagedListAdapter<Photo, ListPhotosVH>(
        object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListPhotosVH =
            ListPhotosVH(glide, layoutInflater.inflate(R.layout.item_list_photos, parent, false), action)


    override fun onBindViewHolder(viewHolder: ListPhotosVH, position: Int) {
        getItem(position)
                ?.let { viewHolder.bind(it) }
                ?: viewHolder.clear()
    }

    override fun onViewRecycled(holder: ListPhotosVH) {
        holder.clear()
    }
}


class ListPhotosVH(private val glide: RequestManager,
                   view: View,
                   private val action: (Photo) -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    private var photo: Photo? = null

    init {
        itemView.imagePhoto.setOnClickListener { photo?.let { action(it) } }
    }

    fun bind(photo: Photo) {
        this.photo = photo
        itemView.textAuthor.text = photo.authorUsername
        itemView.textId.text = photo.id
        itemView.textOrder.text = "#" + photo.extras?.toString()
        glide.asDrawable()
                .load(photo.urls["thumb"])
                .apply(RequestOptions()
                        .transforms(CenterCrop(), RoundedCorners(8)))
                .into(itemView.imagePhoto)
    }

    fun clear() {
        photo = null
        itemView.textAuthor.text = null
        itemView.textId.text = null
        itemView.textOrder.text = null
        glide.clear(itemView.imagePhoto)
        itemView.imagePhoto.setImageDrawable(null)
    }

}

class ListPhotosViewModel(private val uiExecutor: Executor,
                          private val ioExecutor: Executor,
                          private val backgroundThreadExecutor: Executor,
                          private val repo: PhotoRepository
) : ViewModel() {

    val photosData = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPrefetchDistance(10)
            .setPageSize(10)
            .build()
            .let {
                LivePagedListBuilder<Int, Photo>(repo.getPhotos(), it)
                        .setFetchExecutor(backgroundThreadExecutor)
                        .setBoundaryCallback(object : PagedList.BoundaryCallback<Photo>() {

                            val isLoading = AtomicBoolean(false)

                            override fun onItemAtFrontLoaded(itemAtFront: Photo) {
                                tryLoadMore(itemAtFront.pagingData?.prev)
                            }

                            override fun onItemAtEndLoaded(itemAtEnd: Photo) {
                                tryLoadMore(itemAtEnd.pagingData?.next)
                            }

                            override fun onZeroItemsLoaded() {
                                tryLoadMore(1)
                            }

                            fun tryLoadMore(page: Int?) {
                                if (page != null && !isLoading.get()) {
                                    ioExecutor.execute {
                                        isLoading.compareAndSet(false, true)
                                        backgroundThreadExecutor.execute {
                                            repo.insertPhotos(page)
                                            isLoading.compareAndSet(true, false)
                                        }
                                    }
                                }
                            }
                        })
                        .build()
            }

    fun refresh() {
        backgroundThreadExecutor.execute {
            repo.clear()
        }
    }

    private fun invalidateDataSource() {
        photosData.value?.dataSource?.invalidate()
    }


    private class PhotoSourceFactory(private val repositoryInMemory: InMemoryPhotoRepository) : DataSource.Factory<Int, Photo>() {
        override fun create(): DataSource<Int, Photo> = PhotosDataSource(repositoryInMemory)
    }

    private class PhotosDataSource(private val repositoryInMemory: InMemoryPhotoRepository) : PageKeyedDataSource<Int, Photo>() {

        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Photo>) {
            val photos = repositoryInMemory.getCurrentPagePhotos()
            if (photos.isNotEmpty()) {
                val pagingData = photos.first().pagingData ?: throw Error("Paging data not present")
                callback.onResult(photos, pagingData.prev, pagingData.next)
            } else {
                callback.onResult(emptyList(), 0, 0, null, null)
            }
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
            val photos = repositoryInMemory.getPagePhotos(params.key)
            if (photos.isNotEmpty()) {
                callback.onResult(photos, photos.first().pagingData!!.next)
            } else {
                callback.onResult(emptyList(), null)
            }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
            val photos = repositoryInMemory.getPagePhotos(params.key)
            if (photos.isNotEmpty()) {
                callback.onResult(photos, photos.first().pagingData!!.prev)
            } else {
                callback.onResult(emptyList(), null)
            }
        }

    }

}

class InMemoryPhotoRepository {

    private val count = AtomicInteger(0)
    private val currentPage = AtomicInteger(1)
    private val pagedPhotosDb = ConcurrentHashMap<Int, List<Photo>>()

    fun getPagePhotos(page: Int): List<Photo> {
        val filter = pagedPhotosDb[page] ?: emptyList()
        if (filter.isNotEmpty())
            filter.first().pagingData?.curr?.let { currentPage.safeSet(it) }
        return filter
    }

    fun getCurrentPagePhotos(): List<Photo> {
        return pagedPhotosDb[currentPage.get()] ?: emptyList()
    }

    fun insertPhotos(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            photos.first().pagingData?.curr?.let {
                pagedPhotosDb[it] = photos.map { it.copy(extras = count.incrementAndGet()) }
            }
        }

    }

    fun total(): Int = pagedPhotosDb.values.sumBy { it.size }

    fun getIndexOf(photo: Photo): Int = (photo.extras as Int?) ?: -1

    fun clear() {
        pagedPhotosDb.clear()
    }

}


