package com.crskdev.photosurfer.presentation.user


import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat.SHOW_AS_ACTION_ALWAYS
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.Term
import com.crskdev.photosurfer.data.repository.GenericBoundaryCallback
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.presentation.SearchTermTrackerLiveData
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent
import com.crskdev.photosurfer.util.livedata.filter
import com.crskdev.photosurfer.util.livedata.skipFirst
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.setSpanCountByScreenWidth
import kotlinx.android.synthetic.main.fragment_search_users.*
import java.util.concurrent.Executor


class SearchUsersFragment : Fragment() {

    private lateinit var viewModel: SearchUsersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this) {
            val graph = context!!.dependencyGraph()
            SearchUsersViewModel(
                    graph.ioThreadExecutor,
                    graph.diskThreadExecutor,
                    graph.userRepository,
                    graph.searchTermTracker
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val searchView = SearchView(ContextThemeWrapper(view.context,
                R.style.ThemeOverlay_AppCompat_Dark_ActionBar))
                .apply {
                    setIconifiedByDefault(false)
                    setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                        override fun onQueryTextSubmit(query: String): Boolean {
                            viewModel.search(query)
                            return true
                        }
                        override fun onQueryTextChange(newText: String): Boolean = false
                    })
                }

        with(toolbarSearchUsers) {
            menu.add(R.string.search_users).apply {
                actionView = searchView
                icon = ContextCompat.getDrawable(context, R.drawable.ic_search_white_24dp)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW.or(MenuItem.SHOW_AS_ACTION_ALWAYS))
            }
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        with(recyclerSearchUsers) {
            adapter = SearchUsersAdapter(layoutInflater,
                    Glide.with(this@SearchUsersFragment)) {
                //todo do action show user
            }
            layoutManager = GridLayoutManager(context, 1).apply {
                setSpanCountByScreenWidth(resources, 150, 2)
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(2, 2, 2, 2)
                }
            })
        }
        viewModel.usersLiveData.observe(this, Observer {
            (recyclerSearchUsers.adapter as SearchUsersAdapter).submitList(it)
        })
    }
}

class SearchUsersViewModel(
        private val ioExecutor: Executor,
        private val diskExecutor: Executor,
        private val userRepository: UserRepository,
        private val searchTermTracker: SearchTermTracker) : ViewModel() {

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
            .switchMap(searchTermTrackerLiveData) {
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
            }!!

    fun search(query: String) {
        searchTermTracker.setTerm(Term(SearchTermTracker.Type.USER_TERM, query))
    }

}



