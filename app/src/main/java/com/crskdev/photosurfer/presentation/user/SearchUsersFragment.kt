package com.crskdev.photosurfer.presentation.user


import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import com.crskdev.photosurfer.util.dpToPx
import com.crskdev.photosurfer.util.livedata.*
import kotlinx.android.synthetic.main.fragment_search_users.*
import java.util.concurrent.Executor


class SearchUsersFragment : Fragment() {

    private lateinit var viewModel: SearchUsersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this) {
            val graph = context!!.dependencyGraph()
            SearchUsersViewModel(
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
                    maxWidth = Int.MAX_VALUE
                    setIconifiedByDefault(false)
                    val sv = this
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            viewModel.search(query)
                            sv.clearFocus()
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
                expandActionView()
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
            post {
                // call next frame after recycler is measured
                val cellWidth = 150.dpToPx(resources).toInt()
                val spans = width / cellWidth
                val spacing = ((width - cellWidth * spans) / spans) / 2
                layoutManager = GridLayoutManager(context, spans)
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        val column = position % spans
                        outRect.left = if (column == 0) spacing else spacing / 2
                        outRect.right = if (column == spans - 1) spacing else spacing / 2
                        if (position < spans) {
                            outRect.top = spacing
                        }
                        outRect.bottom = spacing
                    }
                })
            }
            Unit
        }
        viewModel.usersLiveData.observe(this, Observer {
            (recyclerSearchUsers.adapter as SearchUsersAdapter).submitList(it)
        })
        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }
}

class SearchUsersViewModel(
        private val diskExecutor: Executor,
        private val userRepository: UserRepository,
        private val searchTermTracker: SearchTermTracker) : ViewModel() {

    private val searchTermTrackerLiveData = SearchTermTrackerLiveData(searchTermTracker)
            .filter { it.second != null && it.second?.type == SearchTermTracker.Type.USER_TERM }

    val errorLiveData = SingleLiveEvent<Throwable>()

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

    private val pageListConfig = PagedList.Config.Builder().defaultConfig().build()

    val usersLiveData = Transformations
            .switchMap(searchTermTrackerLiveData) {
                val term = it.second!!
                LivePagedListBuilder<Int, User>(userRepository.getUsers(), pageListConfig)
                        .setFetchExecutor(diskExecutor)
                        .setBoundaryCallback(GenericBoundaryCallback<User>{ page ->
                            userRepository.searchUsers(term.data, page, object : Repository.Callback<Unit> {
                                override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                                    errorLiveData.value = error
                                }
                            })
                        })
                        .build()
            }!!

    fun search(query: String) {
        searchTermTracker.setTerm(Term(SearchTermTracker.Type.USER_TERM, query))
    }

}



