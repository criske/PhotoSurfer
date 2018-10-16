package com.crskdev.photosurfer.presentation.playwave


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration

import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.PlaywavePhoto
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_playwaves.*
import java.util.concurrent.TimeUnit

class PlaywavesFragment : Fragment() {

    private lateinit var viewModel: PlaywavesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val graph = context!!.dependencyGraph()
        viewModel = viewModelFromProvider(this) {
            PlaywavesViewModel(graph.playwaveRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playwaves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val context = view.context
        val playwavesAdapter = PlaywavesAdapter(LayoutInflater.from(context)) {
            when (it) {
                is PlaywaveAction.Play -> {
                    Toast.makeText(context, "TODO: Show play photo wave", Toast.LENGTH_SHORT).show()
                }
                is PlaywaveAction.Error -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        with(recyclerPlaywaves) {
            adapter = playwavesAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.playwavesLiveData.observe(this, Observer {
            playwavesAdapter.submitList(it)
        })
    }
}


class PlaywavesViewModel(
        playwavesRepository: PlaywaveRepository) : ViewModel() {

    val playwavesLiveData = Transformations.map(playwavesRepository.getPlaywaves()) { l ->
        l.asSequence().map { p ->
            //TODO refactor this mapping into a utility
            val songInfo =
                    p.song.let { it ->
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(it.duration)
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(it.duration) - TimeUnit.MINUTES.toSeconds(minutes)
                        val minutesFormat = if (minutes < 10) "%02d" else "%d"
                        val secondsFormat = if (seconds < 10) "%02d" else "%d"
                        val duration = String.format("$minutesFormat:$secondsFormat", minutes, seconds)
                        "${it.artist} - ${it.title} ($duration)"
                    }
            val hasError = !p.song.exists
            PlaywaveUI(p.id, p.title, songInfo, 0, hasError, p.photos)
        }.toList()
    }
}

data class PlaywaveUI(val id: Int,
                      val title: String,
                      val songInfo: String,
                      val size: Int,
                      val hasError: Boolean,
                      val photos: List<PlaywavePhoto>)
