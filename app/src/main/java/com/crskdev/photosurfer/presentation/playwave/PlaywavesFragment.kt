package com.crskdev.photosurfer.presentation.playwave


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.PlaywavePhoto
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.tintIcons
import kotlinx.android.synthetic.main.fragment_playwaves.*


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

        val navController = view.findNavController()

        toolbarPlaywaves.apply {
            inflateMenu(R.menu.menu_single_add)
            setNavigationOnClickListener {
                navController.popBackStack()
            }
            setOnMenuItemClickListener {
                if (it.itemId == R.id.menu_action_add) {
                    navController.navigate(PlaywavesFragmentDirections.actionFragmentPlaywavesToUpsertPlaywaveFragment(R.id.addPlaywaveFragment),
                            defaultTransitionNavOptions())
                }
                true
            }
        }

        val playwavesAdapter = PlaywavesAdapter(LayoutInflater.from(context)) {
            when (it) {
                is PlaywaveAction.Play -> {
                    Toast.makeText(context, "TODO: Show play photo wave", Toast.LENGTH_SHORT).show()
                }
                is PlaywaveAction.Error -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is PlaywaveAction.Edit -> {
                    Toast.makeText(context, "TODO: edit playwave", Toast.LENGTH_SHORT).show()
                }
                is PlaywaveAction.Delete -> {
                    Toast.makeText(context, "TODO: delete playwave", Toast.LENGTH_SHORT).show()
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


    override fun onResume() {
        super.onResume()
        toolbarPlaywaves.tintIcons()
    }

}


class PlaywavesViewModel(
        playwavesRepository: PlaywaveRepository) : ViewModel() {

    val playwavesLiveData = Transformations.map(playwavesRepository.getPlaywaves()) { l ->
        l.asSequence().map { p ->
            val hasError = !p.song.exists
            PlaywaveUI(p.id, p.title, p.song.toString(), p.size, hasError, p.photos)
        }.toList()
    }
}

data class PlaywaveUI(val id: Int,
                      val title: String,
                      val songInfo: String,
                      val size: Int,
                      val hasError: Boolean,
                      val photos: List<PlaywavePhoto>)
