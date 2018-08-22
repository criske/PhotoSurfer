package com.crskdev.photosurfer.presentation.photo

import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
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
import com.crskdev.photosurfer.data.remote.auth.ObservableAuthState
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.photosPageListConfigLiveData
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.presentation.AuthStateLiveData
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter
import com.crskdev.photosurfer.presentation.photo.listadapter.ListPhotosAdapter.ActionWhat
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import com.crskdev.photosurfer.util.livedata.distinctUntilChanged
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
                        viewModel.logout()
                    }
                    R.id.menu_action_likes -> {
                        viewModel.changePageListingType(FilterVM(FilterVM.Type.LIKES, R.string.likes))
                    }
                    R.id.menu_action_trending -> {
                        viewModel.changePageListingType(FilterVM(FilterVM.Type.TRENDING, R.string.trending))
                    }
                }
                true
            }
        }

        val glide = Glide.with(this)
        recyclerUserListPhotos.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior() as CoordinatorLayout.Behavior<*>
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
                }
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(0, 4, 0, 4)
                }
            })
        }

        viewModel.authStateLiveData.observe(this, Observer {
            val isLoggedIn = it.isNotEmpty()
            toolbarListPhotos.menu.clear()
            if (isLoggedIn) {
                toolbarListPhotos.inflateMenu(R.menu.menu_user_profile_me)
            }else{
                viewModel.changePageListingType(FilterVM(FilterVM.Type.TRENDING, R.string.trending))
            }
            toolbarListPhotos.inflateMenu(R.menu.menu_list_photos)
        })

        viewModel.filterLiveData.observe(this, Observer {
            toolbarListPhotos.setSubtitle(it.title)
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

class ListPhotosViewModel(vmFilter: FilterVM,
                          private val ioExecutor: Executor,
                          diskExecutor: Executor,
                          private val userRepository: UserRepository,
                          private val photoRepository: PhotoRepository,
                          observableAuthState: ObservableAuthState) : ViewModel() {

    val authStateLiveData = AuthStateLiveData(observableAuthState)

    val errorLiveData = SingleLiveEvent<Throwable>()

    val filterLiveData = MutableLiveData<FilterVM>().apply {
        value = vmFilter
    }

    private val choosablePhotoDataSourceFactory: ChoosablePhotoDataSourceFactory =
            ChoosablePhotoDataSourceFactory(photoRepository, toDataSourceFilter(vmFilter))

    val photosLiveData = photosPageListConfigLiveData(
            authStateLiveData,
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
        userRepository.logout()
    }

    fun changePageListingType(vmFilter: FilterVM) {
        val dataSourceFilter = toDataSourceFilter(vmFilter)
        choosablePhotoDataSourceFactory.changeFilter(dataSourceFilter)
        photosLiveData.value?.dataSource?.invalidate()
        filterLiveData.value = vmFilter
    }

    private fun toDataSourceFilter(vmFilter: FilterVM): DataSourceFilter =
            when (vmFilter.type) {
                FilterVM.Type.TRENDING -> DataSourceFilter.RANDOM
                FilterVM.Type.LIKES -> DataSourceFilter.LIKED_PHOTOS
                FilterVM.Type.SEARCH -> DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.SEARCH_PHOTOS, vmFilter.data)
            }

}


