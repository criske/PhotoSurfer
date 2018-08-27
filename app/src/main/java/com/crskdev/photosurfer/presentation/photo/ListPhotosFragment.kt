package com.crskdev.photosurfer.presentation.photo

import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.local.photo.ChoosablePhotoDataSourceFactory
import com.crskdev.photosurfer.data.local.photo.DataSourceFilter
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.Term
import com.crskdev.photosurfer.data.remote.auth.ObservableAuthState
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.RepositoryAction
import com.crskdev.photosurfer.data.repository.photo.photosPageListConfigLiveData
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.presentation.AuthStateLiveData
import com.crskdev.photosurfer.presentation.SearchTermTrackerLiveData
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter.ActionWhat
import com.crskdev.photosurfer.services.ScheduledWorkService
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_list_photos.*
import java.util.concurrent.Executor

/**
 * Created by Cristian Pela on 01.08.2018.
 */
class ListPhotosFragment : Fragment() {

    companion object {
        private const val KEY_CURRENT_FILTER = "KEY_CURRENT_FILTER"
    }

    private lateinit var viewModel: ListPhotosViewModel

    private lateinit var currentFilter: FilterVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFilter = savedInstanceState
                ?.getParcelable<ParcelableFilter>(KEY_CURRENT_FILTER)
                ?.deparcelize()
                ?: FilterVM(FilterVM.Type.TRENDING, R.string.trending)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val graph = context!!.dependencyGraph()
                @Suppress("UNCHECKED_CAST")
                return ListPhotosViewModel(
                        currentFilter,
                        graph.ioThreadExecutor,
                        graph.diskThreadExecutor,
                        graph.userRepository,
                        graph.photoRepository,
                        graph.searchTermTracker,
                        graph.scheduledWorkService,
                        graph.observableAuthState
                ) as T
            }
        }).get(ListPhotosViewModel::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CURRENT_FILTER, currentFilter.parcelize())
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val authNavigatorMiddleware = view.context.dependencyGraph().authNavigatorMiddleware

        collapsingToolbarListPhotos.isTitleEnabled = false
        toolbarListPhotos.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_account -> {
                        authNavigatorMiddleware.navigate(
                                toolbarListPhotos.findNavController(),
                                ListPhotosFragmentDirections.actionFragmentListPhotosToUserProfileFragment(
                                        viewModel.authStateLiveData.value ?: ""))
                    }
                    R.id.menu_action_logout -> {
                        viewModel.changePageListingType(FilterVM(FilterVM.Type.TRENDING, R.string.trending))
                        viewModel.logout()
                    }
                    R.id.menu_action_likes -> {
                        viewModel.changePageListingType(FilterVM(FilterVM.Type.LIKES, R.string.likes, viewModel.authStateLiveData.value))
                    }
                    R.id.menu_action_trending -> {
                        viewModel.changePageListingType(FilterVM(FilterVM.Type.TRENDING, R.string.trending))
                    }
                    R.id.menu_item_search_users ->{
                        toolbarListPhotos.findNavController().navigate(R.id.fragment_search_users, null,
                                defaultTransitionNavOptions())
                    }
                }
                true
            }
        }

        val glide = Glide.with(this)
        recyclerUserListPhotos.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior()
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ListPhotosAdapter(LayoutInflater.from(context), glide) { what, photo ->
                val navController = view.findNavController()
                when (what) {
                    ActionWhat.PHOTO_DETAIL -> {
                        navController.navigate(
                                ListPhotosFragmentDirections.actionFragmentListPhotosToFragmentPhotoDetails(photo.parcelize()),
                                defaultTransitionNavOptions())
                    }
                    ActionWhat.AUTHOR -> {
                        navController.navigate(
                                ListPhotosFragmentDirections.actionFragmentListPhotosToUserProfileFragment(photo.authorUsername),
                                defaultTransitionNavOptions())
                    }
                    ActionWhat.LIKE -> {
                        viewModel.like(photo)
                    }
                }
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(0, 4, 0, 4)
                }
            })
        }

        searchPhotosView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.changePageListingType(FilterVM(FilterVM.Type.SEARCH, R.string.search_title, query?.trim()?.toLowerCase()))
                searchPhotosView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true

        })

        viewModel.authStateLiveData.observe(this, Observer {
            val isLoggedIn = it.isNotEmpty()
            toolbarListPhotos.menu.clear()
            if (isLoggedIn) {
                toolbarListPhotos.inflateMenu(R.menu.menu_user_logged)
            }
            toolbarListPhotos.inflateMenu(R.menu.menu_list_photos)
        })

        viewModel.filterLiveData.observe(this, Observer {
            val title = if (it.type == FilterVM.Type.SEARCH) {
                val term = if (it.data?.isNotEmpty() == true) ": #${it.data}" else ""
                getString(it.title) + term
            } else {
                getString(it.title)
            }
            toolbarListPhotos.subtitle = title
            currentFilter = it
        })

        viewModel.photosLiveData.observe(this, Observer { it ->
            it?.let {
                (recyclerUserListPhotos.adapter as ListPhotosAdapter).submitList(it)
            }
        })

        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(context!!, it.message, Toast.LENGTH_SHORT).show()
        })

        viewModel.needsAuthLiveData.observe(this, Observer {
            authNavigatorMiddleware.navigateToLogin(activity!!)
        })

        refreshListPhotos.setOnClickListener {
            viewModel.refresh()
        }

    }

    private fun FilterVM.parcelize(): ParcelableFilter = ParcelableFilter(type.ordinal, title, data)

    private fun ParcelableFilter.deparcelize(): FilterVM = FilterVM(FilterVM.Type.values()[type], title, data)
}


data class FilterVM(val type: FilterVM.Type, @StringRes val title: Int, val data: String? = null) {
    enum class Type {
        TRENDING, LIKES, SEARCH
    }
}

@Parcelize
data class ParcelableFilter(val type: Int, @StringRes val title: Int, val data: String? = null) : Parcelable

class ListPhotosViewModel(initialFilterVM: FilterVM,
                          private val ioExecutor: Executor,
                          private val diskExecutor: Executor,
                          private val userRepository: UserRepository,
                          private val photoRepository: PhotoRepository,
                          private val searchTermTracker: SearchTermTracker,
                          private val scheduledWorkService: ScheduledWorkService,
                          observableAuthState: ObservableAuthState) : ViewModel() {


    private val searchTermTrackerLiveData = SearchTermTrackerLiveData(searchTermTracker)

    init {
        searchTermTrackerLiveData.observeForever {
            //we have a new user in accessed so we clear the old uset photos table
            if (it.first != it.second && it.second != null) {
                diskExecutor.execute {
                    photoRepository.clear(RepositoryAction(RepositoryAction.Type.SEARCH))
                }
            }
        }
    }

    val authStateLiveData = AuthStateLiveData(observableAuthState)

    val needsAuthLiveData = SingleLiveEvent<Unit>()

    val errorLiveData = SingleLiveEvent<Throwable>()

    val filterLiveData = MutableLiveData<FilterVM>().apply {
        value = initialFilterVM
    }

    private val choosablePhotoDataSourceFactory: ChoosablePhotoDataSourceFactory =
            ChoosablePhotoDataSourceFactory(photoRepository, toDataSourceFilter(initialFilterVM))

    val photosLiveData = photosPageListConfigLiveData(
            diskExecutor,
            ioExecutor,
            choosablePhotoDataSourceFactory,
            errorLiveData
    )

    fun refresh() {
        ioExecutor.execute {
            photoRepository.refresh()
        }
    }

    fun cancel() {
        photoRepository.cancel()
    }

    fun logout() {
        scheduledWorkService.clearAllScheduled()
        diskExecutor.execute {
            userRepository.logout()
        }
    }

    fun changePageListingType(vmFilter: FilterVM) {
        //todo cleanup
        var filter = vmFilter
        if (vmFilter.type == FilterVM.Type.SEARCH) {
            if (vmFilter.data != null)
                searchTermTracker.setTerm(Term(SearchTermTracker.Type.PHOTO_TERM, vmFilter.data.trim()))
            else
                filter = vmFilter.copy(data = searchTermTracker.getTerm(SearchTermTracker.Type.PHOTO_TERM)?.data)
        }
        val dataSourceFilter = toDataSourceFilter(filter)
        choosablePhotoDataSourceFactory.changeFilter(dataSourceFilter)
        photosLiveData.value?.dataSource?.invalidate()
        filterLiveData.value = filter

    }

    fun like(photo: Photo) {
        ioExecutor.execute {
            photoRepository.like(photo, object : Repository.Callback<Boolean> {
                override fun onSuccess(data: Boolean, extras: Any?) {}
                override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                    if (!isAuthenticationError) {
                        errorLiveData.postValue(error)
                    } else {
                        needsAuthLiveData.postValue(Unit)
                    }
                }
            })
        }
    }

    private fun toDataSourceFilter(vmFilter: FilterVM): DataSourceFilter =
            when (vmFilter.type) {
                FilterVM.Type.TRENDING -> DataSourceFilter.RANDOM
                FilterVM.Type.LIKES -> DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.LIKED_PHOTOS, vmFilter.data!!)
                FilterVM.Type.SEARCH -> DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.SEARCH_PHOTOS, vmFilter.data!!)
            }

}


