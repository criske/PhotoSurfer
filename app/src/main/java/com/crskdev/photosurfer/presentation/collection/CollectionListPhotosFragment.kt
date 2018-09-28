package com.crskdev.photosurfer.presentation.collection


import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.paging.LivePagedListBuilder
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.GenericBoundaryCallback
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter
import com.crskdev.photosurfer.util.dpToPx
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.tintIcons
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_collection_list_photos.*
import java.util.concurrent.Executor

class CollectionListPhotosFragment : Fragment() {

    private lateinit var viewModel: CollectionListPhotosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val collectionId = CollectionListPhotosFragmentArgs.fromBundle(arguments).collectionId
        val graph = context!!.dependencyGraph()
        viewModel = viewModelFromProvider(this) {
            CollectionListPhotosViewModel(collectionId,
                    graph.collectionsRepository,
                    graph.photoRepository,
                    graph.diskThreadExecutor)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection_list_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbarCollectionListPhotos.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        recyclerCollectionListPhotos.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior()
            val actionHelper = ListPhotosAdapter.actionHelper(
                    view.findNavController(),
                    context.dependencyGraph().authNavigatorMiddleware) { viewModel.like(it) }
            adapter = ListPhotosAdapter(LayoutInflater.from(context), Glide.with(this@CollectionListPhotosFragment), actionHelper)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                val margin = 2.dpToPx(resources).toInt()
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(margin, margin, margin, margin)
                }
            })
        }

        viewModel.photosLiveData.observe(this, Observer { it ->
            it?.let { page->
                recyclerCollectionListPhotos.post {
                    (recyclerCollectionListPhotos.adapter as ListPhotosAdapter).submitList(page)
                }
            }
        })

        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(context!!, it.message, Toast.LENGTH_SHORT).show()
        })

        viewModel.collectionLiveData.observe(this, Observer {
            val size = it.totalPhotos.toString()
            toolbarCollectionListPhotos.title = it.title
            toolbarCollectionListPhotos.subtitle = "($size)"
        })

    }

    override fun onResume() {
        super.onResume()
        toolbarCollectionListPhotos.tintIcons()
    }
}

class CollectionListPhotosViewModel(collectionId: Int,
                                    collectionRepository: CollectionRepository,
                                    private val photoRepository: PhotoRepository,
                                    diskThreadExecutor: Executor) : ViewModel() {

    val errorLiveData = SingleLiveEvent<Throwable>()

    val photosLiveData = LivePagedListBuilder(collectionRepository.getCollectionPhotos(collectionId), defaultPageListConfig())
            .setFetchExecutor(diskThreadExecutor)
            .setBoundaryCallback(GenericBoundaryCallback<Photo> {
                collectionRepository.fetchAndSaveCollectionPhotos(collectionId, it, object : Repository.Callback<Unit> {
                    override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                        errorLiveData.value = error
                    }
                })
            })
            .build()

    val collectionLiveData = collectionRepository.getCollectionLiveData(collectionId)

    fun like(photo: Photo) {
        photoRepository.like(photo, object : Repository.Callback<Boolean> {
            override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                errorLiveData.value = error
            }
        })
    }

}
