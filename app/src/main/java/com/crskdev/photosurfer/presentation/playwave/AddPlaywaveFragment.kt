package com.crskdev.photosurfer.presentation.playwave

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.util.*
import com.crskdev.photosurfer.util.glide.onError
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_add_playwave.*
import kotlin.math.roundToInt

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
            inflateTintedMenu(R.menu.menu_single_add)
            setNavigationOnClickListener {
                popNavigationBackStack()
            }
            setOnMenuItemClickListener {
                if (it.itemId == R.id.menu_action_add) {
                    activity?.hideSoftKeyboard()
                    viewModel.upsertPlaywave(editAddPlaywaveTitle.text.toString())
                }
                true
            }
        }
        imgBtnAddPlaywaveSearch.setOnClickListener { v ->
            val isSelected = (v.tag as Boolean?) ?: false
            if (isSelected) {
                viewModel.removePlaywaveSong()
            } else {
                navController.navigate(AddPlaywaveFragmentDirections.actionAddPlaywaveFragmentToSearchSongFragment(),
                        defaultTransitionNavOptions())
            }
        }
        imgBtnAddPlaywavePlay.setOnClickListener { v ->
            playerAddPlaywave.isVisible = true
            v.isVisible = false
            (v.tag as SongUI?)?.let {
                viewModel.selectSongToPlay(it)
            }
        }

        viewModel.playwaveLiveData.observe(this, Observer {
            val hasSong = it.song != null
            imgBtnAddPlaywaveSearch.apply {
                setImageResource(
                        if (hasSong)
                            R.drawable.ic_close_white_24dp
                        else
                            R.drawable.ic_add_white_24dp
                )
                tag = hasSong
            }
            imgBtnAddPlaywavePlay.tag = it.song
            imgBtnAddPlaywavePlay.isVisible = hasSong
            textAddPlaywaveSongTitle.text = it?.song?.title
            textAddPlaywaveSongArtist.text = it?.song?.artist
            Glide.with(this)
                    .asDrawable()
                    .load(it.song?.albumPath)
                    .apply(RequestOptions().transform(RoundedCorners(5.dpToPx(resources).roundToInt())).centerCrop())
                    .into(imageSongAlbumArt)
        })

        viewModel.playingSongStateLiveData.observe(this, Observer {
            playerAddPlaywave.changeState(it)
        })

        playerAddPlaywave.setOnPlayerListener(object : PlayerView.PlayerListener {
            override fun onAction(action: PlayerView.Action) {
                when (action) {
                    is PlayerView.Action.Close -> {
                        val isSelected = (imgBtnAddPlaywaveSearch.tag as Boolean?) ?: false
                        if (isSelected) {
                            imgBtnAddPlaywavePlay.isVisible = true
                        }
                        viewModel.justStop()
                    }
                    is PlayerView.Action.PlayOrStop -> viewModel.playOrStopSong()
                    is PlayerView.Action.Pause -> viewModel.pausePlayingSong()
                    is PlayerView.Action.SeekTo -> viewModel.seekTo(action.position, action.confirmedToPlay)
                }
            }
        })

    }

    override fun onBackOrUpPressed() {
        viewModel.clearPlayingSong()
    }

}

