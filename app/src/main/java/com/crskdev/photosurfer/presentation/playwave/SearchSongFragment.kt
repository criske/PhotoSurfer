package com.crskdev.photosurfer.presentation.playwave


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import kotlinx.android.synthetic.main.fragment_add_playwave.*
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
                    viewModel.setPlaywaveSong(it.song)
                    navController.popBackStack()
                }
                is SearchSongAction.Play -> {
                    viewModel.selectSongToPlay(it.song)
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

        viewModel.playingSongStateLiveData.observe(this, Observer {
            playerSearchSongs.changeState(it)
        })

        playerSearchSongs.setOnPlayerListener(object : PlayerView.PlayerListener {
            override fun onAction(action: PlayerView.Action) {
                when (action) {
                    is PlayerView.Action.Close -> viewModel.justStop()
                    is PlayerView.Action.PlayOrStop -> viewModel.playOrStopSong()
                    is PlayerView.Action.Pause -> viewModel.pausePlayingSong()
                    is PlayerView.Action.SeekTo -> viewModel.seekTo(action.position.toLong(), action.confirmedToPlay)
                }
            }
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

