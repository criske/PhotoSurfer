package com.crskdev.photosurfer.presentation.photo

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.local.photo.ChoosablePhotoDataSourceFactory
import com.crskdev.photosurfer.data.local.photo.DataSourceFilter
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.Term
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.RepositoryAction
import com.crskdev.photosurfer.data.repository.photo.photosPageListConfigLiveData
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.presentation.SearchTermTrackerLiveData
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter
import com.crskdev.photosurfer.presentation.photo.listadapter.PhotoInfoSheetDisplayHelper
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.permission.AppPermissionsHelper
import com.crskdev.photosurfer.services.permission.HasAppPermissionAwareness
import com.crskdev.photosurfer.util.Listenable
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.glide.GlideApp
import com.crskdev.photosurfer.util.livedata.*
import com.crskdev.photosurfer.util.recyclerview.HorizontalSpaceDivider
import com.crskdev.photosurfer.util.tintIcons
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_list_photos.*
import kotlinx.android.synthetic.main.fragment_list_photos.view.*

/**
 * Created by Cristian Pela on 01.08.2018.
 */
class ListPhotosFragment : Fragment(), HasAppPermissionAwareness {

    companion object {
        private const val KEY_CURRENT_FILTER = "KEY_CURRENT_FILTER"
    }

    private lateinit var viewModel: ListPhotosViewModel

    private lateinit var currentFilter: FilterVM

    private val authNavigatorMiddleware by lazy(LazyThreadSafetyMode.NONE) {
        view!!.context.dependencyGraph().authNavigatorMiddleware
    }

    private var infoSheetDisplayHelper =
        PhotoInfoSheetDisplayHelper(object : PhotoInfoSheetDisplayHelper.ActionsListener {

            override fun onClose() {
                viewModel.clearShowInfo()
            }

            override fun onRemoveFromCollection(collectionId: Int, photoId: String) {
                viewModel.removeFromCollection(collectionId, photoId)
            }

            override fun displayCollection(collectionId: Int) {
                authNavigatorMiddleware.navigate(
                    findNavController(),
                    ListPhotosFragmentDirections.actionFragmentListPhotosToCollectionListPhotosFragment(
                        collectionId
                    ),
                    defaultTransitionNavOptions()
                )
            }
        })

    private val glide by lazy {
        GlideApp.with(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFilter = savedInstanceState
            ?.getParcelable<ParcelableFilter>(KEY_CURRENT_FILTER)
            ?.deparcelize()
                ?: FilterVM(FilterVM.Type.TRENDING, R.string.trending)
        viewModel = viewModelFromProvider(this) {
            val graph = context!!.dependencyGraph()
            ListPhotosViewModel(
                currentFilter,
                graph.diskThreadExecutor,
                graph.userRepository,
                graph.photoRepository,
                graph.collectionsRepository,
                graph.searchTermTracker,
                graph.listenableAuthState
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CURRENT_FILTER, currentFilter.parcelize())
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_photos, container, false)
    }


    private fun adapterFactory(): RecyclerView.Adapter<*> {
        val actionHelper = ListPhotosAdapter.actionHelper(
            view!!.findNavController(),
            authNavigatorMiddleware,
            { viewModel.showInfo(it.id) },
            { viewModel.delete(it) },
            { viewModel.like(it) })
        return ListPhotosAdapter(LayoutInflater.from(context), glide, actionHelper)
    }

    override fun onPause() {
        super.onPause()
        infoSheetDisplayHelper.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val authNavigatorMiddleware = view.context.dependencyGraph().authNavigatorMiddleware

        recyclerUserListPhotos.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).behavior =
                    AppBarLayout.ScrollingViewBehavior()
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = adapterFactory()
            addItemDecoration(HorizontalSpaceDivider.withDpOf(2, context))
        }

        searchPhotosView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.changePageListingType(
                    FilterVM(
                        FilterVM.Type.SEARCH,
                        R.string.search_title,
                        query?.trim()?.toLowerCase()
                    )
                )
                searchPhotosView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })

        collapsingToolbarListPhotos.isTitleEnabled = false
        // "recyclerUserListPhotos" as synthetic inside toolbarListPhotos#apply#setOnMenuItemClickListener is null(bug?)
        // so I need to keep a reference outside the scope
        val rvRef = recyclerUserListPhotos
        toolbarListPhotos.apply {
            setOnMenuItemClickListener {
                val navController = toolbarListPhotos.findNavController()
                when (it.itemId) {
                    R.id.menu_item_account -> {
                        authNavigatorMiddleware.navigate(
                            navController,
                            ListPhotosFragmentDirections.actionFragmentListPhotosToUserProfileFragment(
                                viewModel.authStateLiveData.value ?: ""
                            )
                        )
                    }
                    R.id.menu_action_logout -> {
                        viewModel.changePageListingType(
                            FilterVM(
                                FilterVM.Type.TRENDING,
                                R.string.trending
                            )
                        )
                        viewModel.logout()
                    }
                    R.id.menu_action_likes -> {
                        rvRef.swapAdapter(adapterFactory(), false)
                        viewModel.changePageListingType(
                            FilterVM(
                                FilterVM.Type.LIKES,
                                R.string.likes,
                                viewModel.authStateLiveData.value
                            )
                        )
                    }
                    R.id.menu_action_trending -> {
                        rvRef.swapAdapter(adapterFactory(), false)
                        viewModel.changePageListingType(
                            FilterVM(
                                FilterVM.Type.TRENDING,
                                R.string.trending
                            )
                        )
                    }
                    R.id.menu_saved -> {
                        if (AppPermissionsHelper.hasStoragePermission(context)) {
                            rvRef.swapAdapter(adapterFactory(), false)
                            viewModel.changePageListingType(
                                FilterVM(
                                    FilterVM.Type.SAVED,
                                    R.string.saved_photos
                                )
                            )
                        } else {
                            AppPermissionsHelper.requestStoragePermission(activity!!)
                        }
                    }
                    R.id.menu_item_search_users -> {
                        navController.navigate(
                            R.id.fragment_search_users, null,
                            defaultTransitionNavOptions()
                        )
                    }
                    R.id.menu_action_collections -> {
                        authNavigatorMiddleware.navigate(navController, R.id.fragment_collections)
                    }
                    R.id.menu_item_play_waves -> {
                        navController.navigate(
                            ListPhotosFragmentDirections.actionFragmentListPhotosToPlaywaves(),
                            defaultTransitionNavOptions()
                        )
                    }
                }
                true
            }
        }

        viewModel.authStateLiveData.observe(this, Observer {
            val isLoggedIn = it.isNotEmpty()
            toolbarListPhotos.menu.clear()
            if (isLoggedIn) {
                toolbarListPhotos.inflateMenu(R.menu.menu_user_logged)
            }
            toolbarListPhotos.inflateMenu(R.menu.menu_list_photos)
        })

        viewModel.filterLiveData.observe(this, Observer {
            //if filter is set to saved we disable actions or navs for "like" or "collections"
            (recyclerUserListPhotos.adapter as ListPhotosAdapter).setType(it.type != FilterVM.Type.SAVED)
            val title = if (it.type == FilterVM.Type.SEARCH) {
                val term = if (it.data?.isNotEmpty() == true) " ${it.data}" else ""
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

        viewModel.photoInfoLiveData.observe(this, Observer {
            if (it != Photo.EMPTY)
                infoSheetDisplayHelper.displayInfoBottomSheet(context!!, it)
        })

    }

    override fun onResume() {
        super.onResume()
        toolbarListPhotos.tintIcons()
    }

    override fun onPermissionsGranted(permissions: List<String>, enqueuedActionArg: String?) {
        recyclerUserListPhotos.swapAdapter(adapterFactory(), false)
        viewModel.changePageListingType(FilterVM(FilterVM.Type.SAVED, R.string.saved_photos))
    }

    private fun FilterVM.parcelize(): ParcelableFilter = ParcelableFilter(type.ordinal, title, data)

    private fun ParcelableFilter.deparcelize(): FilterVM =
        FilterVM(FilterVM.Type.values()[type], title, data)
}


data class FilterVM(val type: FilterVM.Type, @StringRes val title: Int, val data: String? = null) {
    enum class Type {
        TRENDING, LIKES, SEARCH, SAVED
    }
}

@Parcelize
data class ParcelableFilter(val type: Int, @StringRes val title: Int, val data: String? = null) :
    Parcelable

class ListPhotosViewModel(initialFilterVM: FilterVM,
                          diskExecutor: KExecutor,
                          private val userRepository: UserRepository,
                          private val photoRepository: PhotoRepository,
                          private val collectionRepository: CollectionRepository,
                          private val searchTermTracker: SearchTermTracker,
                          listenableAuthState: Listenable<AuthToken>) : ViewModel() {


    private val searchTermTrackerLiveData = SearchTermTrackerLiveData(searchTermTracker)
        .filter { it?.second != null && it.second?.type == SearchTermTracker.Type.PHOTO_TERM }

    init {
        searchTermTrackerLiveData.observeForever {
            //clear the search table when user changes the query term
            if (it.first != it.second && it.second != null) {
                photoRepository.clear(RepositoryAction(RepositoryAction.Type.SEARCH))
            }
        }
    }

    val authStateLiveData = ListenableLiveData(listenableAuthState).map {
        it.username
    }

    val needsAuthLiveData = SingleLiveEvent<Unit>()

    val errorLiveData = SingleLiveEvent<Throwable>()

    val filterLiveData = MutableLiveData<FilterVM>().apply {
        value = initialFilterVM
    }

    private val choosablePhotoDataSourceFactory: ChoosablePhotoDataSourceFactory =
        ChoosablePhotoDataSourceFactory(photoRepository, toDataSourceFilter(initialFilterVM))

    val photosLiveData = photosPageListConfigLiveData(
        diskExecutor,
        choosablePhotoDataSourceFactory,
        errorLiveData
    )


    private val photoInfoLiveDataHelper = PhotoInfoLiveDataHelper {
        photoRepository.getPhotoLiveData(it)
    }
    val photoInfoLiveData = photoInfoLiveDataHelper()

    fun showInfo(photoId: String) {
        photoInfoLiveDataHelper.setPhotoId(photoId)
    }

    fun clearShowInfo() {
        photoInfoLiveDataHelper.clearPhotoId()
    }

    fun cancel() {
        photoRepository.cancel()
    }

    fun logout() {
        userRepository.logout()
    }

    fun changePageListingType(vmFilter: FilterVM) {
        //todo cleanup
        var filter = vmFilter
        if (vmFilter.type == FilterVM.Type.SEARCH) {
            if (vmFilter.data != null)
                searchTermTracker.setTerm(
                    Term(
                        SearchTermTracker.Type.PHOTO_TERM,
                        vmFilter.data.trim()
                    )
                )
            else
                filter =
                        vmFilter.copy(data = searchTermTracker.getTerm(SearchTermTracker.Type.PHOTO_TERM)?.data)
        }
        val dataSourceFilter = toDataSourceFilter(filter)
        choosablePhotoDataSourceFactory.changeFilter(dataSourceFilter)
        photosLiveData.value?.dataSource?.invalidate()
        filterLiveData.value = filter
    }

    fun like(photo: Photo) {
        photoRepository.like(photo, object : Repository.Callback<Boolean> {
            override fun onSuccess(data: Boolean, extras: Any?) {}
            override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                if (!isAuthenticationError) {
                    errorLiveData.value = error
                } else {
                    needsAuthLiveData.value = Unit
                }
            }
        })
    }

    private fun toDataSourceFilter(vmFilter: FilterVM): DataSourceFilter =
        when (vmFilter.type) {
            FilterVM.Type.TRENDING -> DataSourceFilter.RANDOM
            FilterVM.Type.LIKES -> DataSourceFilter(
                ChoosablePhotoDataSourceFactory.Type.LIKED_PHOTOS,
                vmFilter.data!!
            )
            FilterVM.Type.SEARCH -> DataSourceFilter(
                ChoosablePhotoDataSourceFactory.Type.SEARCH_PHOTOS,
                vmFilter.data!!
            )
            FilterVM.Type.SAVED -> DataSourceFilter.SAVED
        }

    fun delete(photo: Photo) {
        photoRepository.delete(photo)
    }

    fun removeFromCollection(collectionId: Int, photoId: String) {
        collectionRepository.removePhotoFromCollection(collectionId, photoId)
    }
}


