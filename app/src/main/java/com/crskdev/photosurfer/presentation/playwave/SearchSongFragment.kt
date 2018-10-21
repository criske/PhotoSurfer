package com.crskdev.photosurfer.presentation.playwave


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.addSearch
import com.crskdev.photosurfer.util.hideSoftKeyboard
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.tintIcons
import kotlinx.android.synthetic.main.fragment_search_song.*

class SearchSongFragment : Fragment(), HasUpOrBackPressedAwareness {

    private lateinit var viewModel: UpsertPlaywaveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(parentFragment!!) {
            val graph = context!!.dependencyGraph()
            UpsertPlaywaveViewModel(graph.diskThreadExecutor,
                    graph.playwaveRepository,
                    graph.playwaveSoundPlayer)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
        toolbarSearchSongs.apply {
            menu.addSearch(context, R.string.search_songs, true, onChange = {
                viewModel.search(it)
            })
            setNavigationOnClickListener {
                navController.popBackStack()
            }
        }

        val searchSongsAdapter = SearchSongAdapter(LayoutInflater.from(view.context)) {
            activity?.hideSoftKeyboard()
            when (it) {
                is SearchSongAction.Add -> {
                    viewModel.selectSong(it.song)
                    navController.popBackStack()
                }
                is SearchSongAction.Play -> {
                    viewModel.selectSong(it.song)
                }
            }
        }
        recyclerSearchSongs.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
            adapter = searchSongsAdapter
        }

        viewModel.foundSongsLiveData.observe(this, Observer {
            searchSongsAdapter.submitList(it)
        })

    }

    override fun onBackOrUpPressed() {
        //viewModel.stopPlayerIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        toolbarSearchSongs.tintIcons()
    }

}

data class SongUI(
        val id: Long,
        val path: String,
        val title: String,
        val artist: String,
        val duration: String,
        val fullInfo: String,
        val durationLong: Long,
        val exists: Boolean,
        val albumPath: String? = null)