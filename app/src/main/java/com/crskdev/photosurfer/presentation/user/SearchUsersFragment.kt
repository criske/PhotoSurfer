package com.crskdev.photosurfer.presentation.user


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.repository.GenericBoundaryCallback
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.presentation.SearchTermTrackerLiveData
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import com.crskdev.photosurfer.util.livedata.filter
import com.crskdev.photosurfer.util.livedata.skipFirst
import java.util.concurrent.Executor


class SearchUsersFragment : Fragment() {

    private lateinit var viewModel: SearchUsersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_users, container, false)
    }

}

class SearchUsersViewModel(
        private val ioExecutor: Executor,
        private val diskExecutor: Executor,
        private val userRepository: UserRepository,
        searchTermTracker: SearchTermTracker) : ViewModel() {

    private val searchTermTrackerLiveData = SearchTermTrackerLiveData(searchTermTracker)
            .filter { it.second != null && it.second?.type == SearchTermTracker.Type.USER_TERM }

    private val errorLiveData = SingleLiveEvent<Throwable>()

    init {
        searchTermTrackerLiveData
                .observeForever {
                    if (it.first != it.second && it.second != null) {
                        diskExecutor.execute {
                            userRepository.clear()
                        }
                    }
                }
    }

    private val pageListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPrefetchDistance(10)
            .setPageSize(10)
            .build()

    val usersLiveData = Transformations
            .switchMap(searchTermTrackerLiveData.skipFirst()) {
                val term = it.second!!
                LivePagedListBuilder<Int, User>(userRepository.getUsers(), pageListConfig)
                        .setFetchExecutor(diskExecutor)
                        .setBoundaryCallback(GenericBoundaryCallback<User>(ioExecutor) { page ->
                            userRepository.searchUsers(term.data, page, object : Repository.Callback<Unit> {
                                override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                                    errorLiveData.postValue(error)
                                }
                            })
                        })
                        .build()
            }


}



