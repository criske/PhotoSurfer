package com.crskdev.photosurfer.presentation.playwave


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.permission.AppPermissionsHelper
import com.crskdev.photosurfer.services.permission.HasAppPermissionAwareness
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.map
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.tintIcons
import kotlinx.android.synthetic.main.fragment_playwaves.*


class PlaywavesFragment : Fragment(), HasAppPermissionAwareness {


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
                    if (AppPermissionsHelper.hasStoragePermission(context)) {
                        navController.navigate(PlaywavesFragmentDirections.actionFragmentPlaywavesToUpsertPlaywaveFragment(R.id.addPlaywaveFragment),
                                defaultTransitionNavOptions())
                    } else {
                        AppPermissionsHelper.requestStoragePermission(activity!!)
                    }
                }
                true
            }
        }

        val playwavesAdapter = PlaywavesAdapter(LayoutInflater.from(context)) {
            when (it) {
                is PlaywaveAction.Play -> {
                    navController.navigate(PlaywavesFragmentDirections
                            .actionFragmentPlaywavesToPlaywaveSlideShowFragment(it.playwaveId),
                            defaultTransitionNavOptions())
                }
                is PlaywaveAction.Error -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is PlaywaveAction.Edit -> {
                    navController.navigate(PlaywavesFragmentDirections
                            .actionFragmentPlaywavesToUpsertPlaywaveFragment(R.id.updatePlaywaveFragment)
                            .setPlaywaveId(it.playwaveId),
                            defaultTransitionNavOptions())
                }
                is PlaywaveAction.Delete -> {
                    //TODO add i18n
                    AlertDialog.Builder(context)
                            .setTitle("Delete this playwave permanently?")
                            .setCancelable(true)
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton("OK") { dialog, _ ->
                                viewModel.deletePlaywave(it.playwaveId)
                                dialog.dismiss()
                            }
                            .create().show()
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

    override fun onPermissionsGranted(permissions: List<String>, enqueuedActionArg: String?) {
        findNavController().navigate(PlaywavesFragmentDirections.actionFragmentPlaywavesToUpsertPlaywaveFragment(R.id.addPlaywaveFragment),
                defaultTransitionNavOptions())
    }

    override fun onResume() {
        super.onResume()
        toolbarPlaywaves.tintIcons()
    }

}

class PlaywavesViewModel(private val playwavesRepository: PlaywaveRepository) : ViewModel() {

    fun deletePlaywave(id: Int) {
        playwavesRepository.deletePlaywave(id)
    }

    val playwavesLiveData = playwavesRepository.getPlaywaves().map { l ->
        l.asSequence().map { p -> p.toUI() }.toList()
    }
}

