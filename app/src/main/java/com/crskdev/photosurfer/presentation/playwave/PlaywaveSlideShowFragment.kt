package com.crskdev.photosurfer.presentation.playwave


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.setStatusBarColor
import com.crskdev.photosurfer.util.glide.GlideApp
import com.crskdev.photosurfer.util.livedata.interval
import com.crskdev.photosurfer.util.livedata.map
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_playwave_slide_show.*
import java.util.concurrent.TimeUnit

class PlaywaveSlideShowFragment : Fragment() {

    private lateinit var viewModel: PlaywaveSlideShowViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this) {
            val graph = context!!.dependencyGraph()
            val playwaveId = PlaywaveSlideShowFragmentArgs.fromBundle(arguments).playwaveId
            PlaywaveSlideShowViewModel(playwaveId,
                    graph.playwaveRepository,
                    graph.playwaveSoundPlayer)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playwave_slide_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.setStatusBarColor(Color.TRANSPARENT)
        val adapter = PlaywaveSlideShowAdapter(LayoutInflater.from(context),
                GlideApp.with(this))
        recyclerSlideShow.apply {
           // PagerSnapHelper().attachToRecyclerView(this)
            this.adapter = adapter
            setOnTouchListener { _, _ -> true }
        }
        viewModel.playwaveLiveData.observe(this, Observer {
            adapter.submit(it.photos)
        })
        viewModel.playerLiveData.observe(this, Observer {
            playerSlideShow.changeState(it)
        })
        viewModel.slideShowTickLiveData.observe(this, Observer {
            val nextPosition = (recyclerSlideShow.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition() +1
            recyclerSlideShow.scrollToPosition(nextPosition)
        })
    }

}

class PlaywaveSlideShowViewModel(playwaveId: Int,
                                 playwaveRepository: PlaywaveRepository,
                                 playwaveSoundPlayer: PlaywaveSoundPlayer) : ViewModel() {

    private val playerController = PlayingSongStateController(playwaveSoundPlayer)

    val playerLiveData = playerController.getStateLiveData()

    val slideShowTickLiveData: LiveData<Unit> = MediatorLiveData<Unit>().apply {
        addSource(playerLiveData.interval(5, TimeUnit.SECONDS)) {
            value = Unit
        }
    }

    val playwaveLiveData = playwaveRepository.getPlaywave(playwaveId).map {
        //using map as side effect here
        playerController.loadAndPlay(it.song.toUI())
        it
    }

    override fun onCleared() {
        playerController.release()
    }
}
