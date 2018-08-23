package com.crskdev.photosurfer.presentation.photo


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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.local.photo.ChoosablePhotoDataSourceFactory
import com.crskdev.photosurfer.data.local.photo.DataSourceFilter
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.Term
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.RepositoryAction
import com.crskdev.photosurfer.data.repository.photo.photosPageListConfigLiveData
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.presentation.SearchTermTrackerLiveData
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import kotlinx.android.synthetic.main.fragment_list_photos.*
import java.util.concurrent.Executor

class UserListPhotosFragment : Fragment() {

    private lateinit var viewModel: UserListPhotosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val graph = context!!.dependencyGraph()
                @Suppress("UNCHECKED_CAST")
                return UserListPhotosViewModel(
                        UserListPhotosFragmentArgs.fromBundle(arguments).username,
                        graph.searchTermTracker,
                        graph.ioThreadExecutor,
                        graph.diskThreadExecutor,
                        graph.photoRepository
                ) as T
            }
        }).get(UserListPhotosViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile_list_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val glide = Glide.with(this)

        recyclerUserListPhotos.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ListPhotosAdapter(LayoutInflater.from(context), glide) { what, photo ->
                val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (what) {
                    ListPhotosAdapter.ActionWhat.PHOTO_DETAIL -> {
                        navController.navigate(
                                R.id.fragment_photo_details, bundleOf("photo" to photo.parcelize()),
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
        viewModel.photosData.observe(this, Observer { it ->
            it?.let {
                (recyclerUserListPhotos.adapter as ListPhotosAdapter).submitList(it)
            }
        })
    }

}

class UserListPhotosViewModel(userName: String,
                              searchTermTracker: SearchTermTracker,
                              private val ioExecutor: Executor,
                              diskExecutor: Executor,
                              private val photoRepository: PhotoRepository) : ViewModel() {

    val errorLiveData = SingleLiveEvent<Throwable>()

    val photosData = photosPageListConfigLiveData(
            diskExecutor,
            ioExecutor,
            ChoosablePhotoDataSourceFactory(photoRepository,
                    DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.USER_PHOTOS, userName)),
            errorLiveData)


    private val searchTermTrackerLiveData = SearchTermTrackerLiveData(searchTermTracker)

    init {
        searchTermTrackerLiveData.observeForever {
            //we have a new user in accessed so we clear the old uset photos table
            if (it.first != it.second && it.second != null) {
                diskExecutor.execute {
                    photoRepository.clear(RepositoryAction(RepositoryAction.Type.USER_PHOTOS))
                }
            }
        }
        searchTermTracker.setTerm(Term(SearchTermTracker.Type.USER_ACCESSED_TERM, userName))
    }


    fun refresh() {
        ioExecutor.execute {
            photoRepository.refresh()
        }
    }

    fun cancel() {
        photoRepository.cancel()
    }


}
