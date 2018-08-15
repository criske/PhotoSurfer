package com.crskdev.photosurfer.presentation

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.util.SingleLiveEvent
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_list_photos.*
import kotlinx.android.synthetic.main.item_list_photos.view.*
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

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

        val authNavigatorMiddleware = view.context.dependencyGraph().authNavigatorMiddleware

        toolbarListPhotos.apply {
            inflateMenu(R.menu.menu_list_photos)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_account -> {
                        authNavigatorMiddleware.navigate(
                                findNavController(),
                                ListPhotosFragmentDirections.actionFragmentListPhotosToUserProfileFragment(""))
                    }
                }
                true
            }
        }


        val glide = Glide.with(this)
        recyclerListPhotos.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior() as CoordinatorLayout.Behavior<*>
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ListPhotosAdapter(LayoutInflater.from(context), glide) { what, photo ->
                val navController = view.findNavController()
                when (what) {
                    ActionWhat.PHOTO_DETAIL -> {
                        navController.navigate(
                                ListPhotosFragmentDirections
                                        .actionFragmentListPhotosToFragmentPhotoDetails(photo.parcelize()),
                                defaultTransitionNavOptions())
                    }
                    ActionWhat.AUTHOR -> {
                        navController.navigate(
                                ListPhotosFragmentDirections
                                        .actionFragmentListPhotosToUserProfileFragment(photo.authorUsername),
                                defaultTransitionNavOptions())
                    }
                }
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
        model.errorData.observe(this, Observer {
            Toast.makeText(context!!, it.message, Toast.LENGTH_SHORT).show()
        })
        refreshListPhotos.setOnClickListener {
            model.refresh()
        }

    }
}

class ListPhotosAdapter(private val layoutInflater: LayoutInflater,
                        private val glide: RequestManager,
                        private val action: (ActionWhat, Photo) -> Unit) : PagedListAdapter<Photo, ListPhotosVH>(
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

enum class ActionWhat {
    PHOTO_DETAIL, AUTHOR
}

class ListPhotosVH(private val glide: RequestManager,
                   view: View,
                   private val action: (ActionWhat, Photo) -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {


    private var photo: Photo? = null

    init {
        itemView.imagePhoto.setOnClickListener { _ -> photo?.let { action(ActionWhat.PHOTO_DETAIL, it) } }
        itemView.textAuthor.setOnClickListener { _ -> photo?.let { action(ActionWhat.AUTHOR, it) } }
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
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        itemView.textError.text = e?.message
                        return true
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean = false

                })
                .into(itemView.imagePhoto)
    }

    fun clear() {
        photo = null
        with(itemView) {
            textAuthor.text = null
            textId.text = null
            textOrder.text = null
            textError.text = null
            glide.clear(imagePhoto)
            imagePhoto.setImageDrawable(null)
        }
    }

}

class ListPhotosViewModel(private val ioExecutor: Executor,
                          private val backgroundThreadExecutor: Executor,
                          private val repository: PhotoRepository) : ViewModel() {

    val errorData = SingleLiveEvent<Throwable>()

    val photosData = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPrefetchDistance(10)
            .setPageSize(10)
            .build()
            .let {
                LivePagedListBuilder<Int, Photo>(repository.getPhotos(), it)
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
                                        repository.insertPhotos(page, object : Repository.Callback<Unit> {
                                            override fun onError(error: Throwable) {
                                                errorData.postValue(error)
                                            }
                                        })
                                        isLoading.compareAndSet(true, false)
                                    }
                                }
                            }
                        })
                        .build()
            }

    fun refresh() {
        ioExecutor.execute {
            repository.refresh()
        }
    }

    fun cancel() {
        repository.cancel()
    }

}


