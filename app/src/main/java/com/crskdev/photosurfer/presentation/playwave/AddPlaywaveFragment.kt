package com.crskdev.photosurfer.presentation.playwave

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_add_playwave.*

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class AddPlaywaveFragment : Fragment(), HasUpOrBackPressedAwareness {

    private lateinit var viewModel: UpsertPlaywaveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this model will be shared
        viewModel = viewModelFromProvider(activity!!) {
            val graph = context!!.dependencyGraph()
            UpsertPlaywaveViewModel(graph.diskThreadExecutor, graph.playwaveRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_add_playwave, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() };
        imgBtnAddPlaywaveSearch.setOnClickListener {
            navController.navigate(AddPlaywaveFragmentDirections.actionAddPlaywaveFragmentToSearchSongFragment(),
                    defaultTransitionNavOptions())
        }
        toolbarAddPlaywave.apply {
            setNavigationOnClickListener {
                navController.popBackStack()
            }
        }
        viewModel.selectedSongLiveData.observe(this, Observer {
            textAddPlaywaveSongTitle.text = it?.title
            textAddPlaywaveSongArtist.text = it?.artist
            textAddPlaywaveSongDuration.text = it?.duration
        })
    }

    override fun onBackOrUpPressed() {
        viewModel.clear()
    }

}