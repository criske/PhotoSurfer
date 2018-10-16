package com.crskdev.photosurfer.presentation.playwave


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel

import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider

class PlaywavesFragment : Fragment() {

    private lateinit var viewModel: PlaywavesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this){
            PlaywavesViewModel(TODO())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playwaves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}


class PlaywavesViewModel(playwavesRepository: PlaywaveRepository): ViewModel(){

    val playwavesLiveData = playwavesRepository.getPlaywaves()

}
