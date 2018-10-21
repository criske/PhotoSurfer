package com.crskdev.photosurfer.presentation.playwave

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.popNavigationBackStack
import kotlinx.android.synthetic.main.fragment_add_playwave.*

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class AddPlaywaveFragment : Fragment(), HasUpOrBackPressedAwareness {

    private lateinit var viewModel: UpsertPlaywaveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this model will be shared
        viewModel = viewModelFromProvider(parentFragment!!) {
            val graph = context!!.dependencyGraph()
            UpsertPlaywaveViewModel(
                    graph.diskThreadExecutor,
                    graph.playwaveRepository,
                    graph.playwaveSoundPlayer)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_add_playwave, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = view.findNavController()
        toolbarAddPlaywave.apply {
            setNavigationOnClickListener {
                popNavigationBackStack()
            }
        }
        imgBtnAddPlaywaveSearch.setOnClickListener { v ->
            val isSelected = (v.tag as Boolean?) ?: false
            if (isSelected) {
                viewModel.removeSelectedSong()
            } else {
                navController.navigate(AddPlaywaveFragmentDirections.actionAddPlaywaveFragmentToSearchSongFragment(),
                        defaultTransitionNavOptions())
            }
        }
        imgBtnAddPlaywavePlay.setOnClickListener { v ->
            viewModel.playSelectedSong()
            v.isVisible = false
        }

        viewModel.selectedSongLiveData.observe(this, Observer {
            val isSelected = it != null
            imgBtnAddPlaywaveSearch.apply {
                setImageResource(
                        if (isSelected)
                            R.drawable.ic_close_white_24dp
                        else
                            R.drawable.ic_add_white_24dp
                )

                tag = isSelected
            }
            playerAddPlaywave.isVisible = isSelected
            imgBtnAddPlaywavePlay.isVisible = isSelected
            textAddPlaywaveSongTitle.text = it?.title
            textAddPlaywaveSongArtist.text = it?.artist
        })

        viewModel.playingSongStateLiveData.observe(this, Observer {
            playerAddPlaywave.changeState(it)
        })

        playerAddPlaywave.setOnPlayerListener(object : PlayerView.PlayerListener{
            override fun onAction(action: PlayerView.Action) {
                when(action){
                    is PlayerView.Action.Close ->{
                        val isSelected = (imgBtnAddPlaywaveSearch.tag as Boolean?) ?: false
                        if(isSelected)
                            imgBtnAddPlaywavePlay.isVisible = true
                    }
                    is PlayerView.Action.Play -> viewModel.playSelectedSong()
                    is PlayerView.Action.Pause -> viewModel.pauseSelectedSong()
                    is PlayerView.Action.Stop -> viewModel.stopSelectedSong()
                    is PlayerView.Action.SkipTo -> viewModel.skipTo(action.percent, action.confirmedToPlay)
                }
            }

        })

    }

    override fun onBackOrUpPressed() {
        viewModel.clear()
    }

}

