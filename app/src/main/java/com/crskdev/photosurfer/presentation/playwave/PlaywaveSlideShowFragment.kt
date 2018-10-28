package com.crskdev.photosurfer.presentation.playwave


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorFilter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.presentation.HasUpOrBackPressedAwareness
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.setStatusBarColor
import com.crskdev.photosurfer.util.getColorCompat
import com.crskdev.photosurfer.util.glide.GlideApp
import com.crskdev.photosurfer.util.livedata.*
import kotlinx.android.synthetic.main.fragment_playwave_slide_show.*
import kotlin.math.sign

class PlaywaveSlideShowFragment : Fragment(), HasUpOrBackPressedAwareness {

    private lateinit var viewModel: PlaywaveSlideShowViewModel

    private var controlsAnimation: AnimatorSet? = null

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
        toolbarSlideShow.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        val adapter = PlaywaveSlideShowAdapter(LayoutInflater.from(context),
                GlideApp.with(this))
        recyclerSlideShow.apply {
            // PagerSnapHelper().attachToRecyclerView(this)
            this.adapter = adapter
            tag = true
            setOnTouchListener { r, ev ->
                if (ev.action == MotionEvent.ACTION_UP) {
                    val areControlsShowing = (r.tag as Boolean?) ?: false
                    animateControlsVisibility(!areControlsShowing)
                    r.tag = !areControlsShowing //toggle
                }
                true
            }
        }
        btnSlideShow.setOnClickListener {
            (it.tag as Boolean?)?.let { isPlaying ->
                if (isPlaying) {
                    viewModel.pause()
                } else {
                    viewModel.play()
                }
            }
        }
        progressSlideShow.apply {
            progressDrawable.colorFilter = PorterDuff.Mode.SRC_IN.toColorFilter(
                    context.getColorCompat(R.color.colorAccent).let {
                        ColorUtils.setAlphaComponent(it, 255 / 4)
                    }
            )
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                var startManualSeek = false

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (startManualSeek) {
                        viewModel.seekTo(progress, false)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    startManualSeek = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    viewModel.seekTo(seekBar.progress, true)
                    startManualSeek = false
                }

            })
        }
        viewModel.playwaveLiveData.observe(this, Observer {
            textSlideShowTitle.text = it.title
            textSlideShowSong.text = it.song.artist + " - " + it.song.title
            adapter.submit(it.photos)
        })

        viewModel.slideShowResultLiveData.observe(this, Observer {
            when (it) {
                is SlideShowResult.PlayerState -> {
                    when (it.state) {
                        is PlayingSongState.Playing -> {
                            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            btnSlideShow.apply {
                                tag = true
                                setImageResource(R.drawable.ic_pause_white_24dp)
                            }
                            progressSlideShow.apply {
                                max = it.state.song?.durationInt ?: 0
                                progress = it.state.position
                            }
                        }
                        is PlayingSongState.Completed,
                        is PlayingSongState.Paused -> {
                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            btnSlideShow.apply {
                                tag = false
                                setImageResource(R.drawable.ic_play_arrow_white_24dp)
                            }
                        }
                    }
                }
                is SlideShowResult.Tick -> {
                    val nextPosition = (recyclerSlideShow.layoutManager as LinearLayoutManager)
                            .findFirstCompletelyVisibleItemPosition() + it.direction
                    recyclerSlideShow.scrollToPosition(nextPosition)
                }
            }
        })
    }


    private fun animateControlsVisibility(showing: Boolean) {
        if (controlsAnimation?.isRunning == true) {
            return
        }
        fun View.translateYAnimator(toBottomOffscreen: Boolean, showing: Boolean, value: Float): ObjectAnimator {
            val (from, to) = if (showing) {
                if (toBottomOffscreen) {
                    value
                } else {
                    -value
                } to 0f
            } else {
                0f to if (toBottomOffscreen) {
                    value
                } else {
                    -value
                }
            }
            return ObjectAnimator.ofFloat(this, "translationY", from, to)
        }

        fun View.alphaAnimator(show: Boolean, transparency: Float = 0f): ObjectAnimator {
            val (from, to) = if (show) transparency to 1f else 1f to transparency
            return ObjectAnimator.ofFloat(this, "alpha", from, to)
        }

        val playButtonAnimation = btnSlideShow.alphaAnimator(showing).apply {
            duration = 100
        }

        val groupAnimators = listOf(
                toolbarSlideShow.translateYAnimator(false, showing, 200f),
                progressSlideShow.translateYAnimator(true, showing, 200f),
                textSlideShowSong.translateYAnimator(true, showing, 200f),
                textSlideShowTitle.translateYAnimator(true, showing, 200f),
                textSlideShowTitle.alphaAnimator(showing, 0.35f)
        ).map {
            it.apply { duration = 500 }
        }
        controlsAnimation?.cancel()
        controlsAnimation = AnimatorSet().apply {
            playTogether(groupAnimators)
            play(playButtonAnimation).after(groupAnimators.first())
            start()
        }
    }

    override fun onStop() {
        controlsAnimation?.cancel()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onStop()
    }

    override fun onBackOrUpPressed() {
        context?.getColorCompat(R.color.colorPrimary)?.let { activity?.setStatusBarColor(it) }
    }

}

sealed class SlideShowResult {
    class PlayerState(val state: PlayingSongState) : SlideShowResult()
    class Tick(val timePosition: Int, val direction: Int) : SlideShowResult() {
        companion object {
            const val FORWARD = 1
            const val BACKWARD = -1
        }
    }
}

class PlaywaveSlideShowViewModel(playwaveId: Int,
                                 playwaveRepository: PlaywaveRepository,
                                 playwaveSoundPlayer: PlaywaveSoundPlayer) : ViewModel() {

    private val playerController = PlayingSongStateController(playwaveSoundPlayer)

    val playwaveLiveData = playwaveRepository.getPlaywave(playwaveId).onNext {
        playerController.loadAndPlay(it.song.toUI())
    }

    val slideShowResultLiveData: LiveData<SlideShowResult> = playerController
            .getStateLiveData()
            .splitAndMerge {
                listOf<LiveData<SlideShowResult>>(
                        map { SlideShowResult.PlayerState(it) },
                        filter { it is PlayingSongState.Dynamic }
                                .cast<PlayingSongState.Dynamic>()
                                .interval(10)
                                .scan(SlideShowResult.Tick(0, SlideShowResult.Tick.FORWARD) as SlideShowResult) { acc, curr ->
                                    val position = curr.position
                                    val direction = curr.position.minus((acc as SlideShowResult.Tick).timePosition).sign
                                    SlideShowResult.Tick(position, direction)
                                })
            }

    override fun onCleared() {
        playerController.release()
    }

    fun pause() {
        playerController.pause()
    }

    fun play() {
        playerController.playOrStop()
    }

    fun seekTo(position: Int, confirmedToPlayAt: Boolean) {
        playerController.seekTo(position, confirmedToPlayAt)
    }

}
