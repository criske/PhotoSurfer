package com.crskdev.photosurfer.presentation.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig
import java.lang.Exception
import java.lang.IllegalStateException

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class UpsertPlaywaveViewModel(
        private val executor: KExecutor,
        private val playwaveRepository: PlaywaveRepository,
        playwaveSoundPlayer: PlaywaveSoundPlayer) : ViewModel() {

    private val searchQueryLiveData: MutableLiveData<String?> = MutableLiveData<String?>().apply {
        value = null
    }

    private val songStateController = PlayingSongStateController(playwaveSoundPlayer)

    val foundSongsLiveData: LiveData<PagedList<SongUI>> = Transformations.switchMap(searchQueryLiveData) { query ->
        defaultPageListConfig()
                .let { c ->
                    LivePagedListBuilder<Int, SongUI>(playwaveRepository
                            .getAvailableSongs(query)
                            .mapByPage { p ->
                                p.asSequence().map {
                                    SongUI(it.id, it.path, it.title, it.artist, prettySongDuration(it.duration), it.toString(),
                                            it.duration, it.exists)
                                }.toList()
                            }, c)
                            .setFetchExecutor(executor)
                            .build()
                }
    }

    val playingSongStateLiveData: LiveData<PlayingSongState> = songStateController.getStateLiveData()

    var selectedSongLiveData: LiveData<SongUI?> = MutableLiveData<SongUI>()

    fun search(query: String?) {
        searchQueryLiveData.value = query
    }

    fun clear() {
        removeSelectedSong()
    }

    fun pauseSelectedSong() {
        songStateController.pause()
    }

    fun playOrStopSelectedSong() {
        try {
            songStateController.playOrStop()
        } catch (ex: Exception) {
            ex.printStackTrace()
            selectedSong().value?.let {
                songStateController.prepare(it)
            }
        }
    }

    fun seekTo(position: Long, confirmedToPlayAt: Boolean = false) {
        songStateController.seekTo(position, confirmedToPlayAt)
    }

    fun selectSong(song: SongUI) {
        selectedSong().value = song
    }

    fun removeSelectedSong() {
        selectedSong().value = null
        songStateController.clear()
    }

    private fun selectedSong() = (selectedSongLiveData as MutableLiveData)

    override fun onCleared() {
        songStateController.release()
    }

    fun justStop() {
        songStateController.justStop()
    }
}

sealed class PlayingSongState(val song: SongUI?) {
    object None : PlayingSongState(null)
    class Prepare(song: SongUI) : PlayingSongState(song)
    class Ready(song: SongUI) : PlayingSongState(song)
    class Stopped(song: SongUI) : PlayingSongState(song)
    class Playing(song: SongUI, val position: Long, val positionDisplay: String) : PlayingSongState(song)
    class Seeking(song: SongUI, val position: Long, val positionDisplay: String, val stateBeforeSeek: PlayingSongState,
                  val confirmedToPlayAt: Boolean) : PlayingSongState(song)

    class Paused(song: SongUI, val position: Long, val positionDisplay: String) : PlayingSongState(song)
    class Completed(song: SongUI) : PlayingSongState(song)
}

class PlayingSongStateController(private val soundPlayer: PlaywaveSoundPlayer) : PlaywaveSoundPlayer.TrackListener {

    private val state: MutableLiveData<PlayingSongState> = MutableLiveData<PlayingSongState>().apply {
        PlayingSongState.None
    }

    init {
        soundPlayer.setTrackListener(this)
    }

    fun getStateLiveData(): LiveData<PlayingSongState> = state

    fun clear() {
        state.postValue(PlayingSongState.None)
        soundPlayer.unload()
    }

    fun release() = soundPlayer.release()

    fun prepare(song: SongUI) {
        state.postValue(PlayingSongState.Prepare(song))
        soundPlayer.load(song.path, song.durationLong)
    }

    fun pause() {
        assert(state.value is PlayingSongState.Playing) {
            "The state before PAUSE must be PLAYING"
        }
        val s = state.value!! as PlayingSongState.Playing
        state.postValue(PlayingSongState.Paused(s.song!!, s.position, s.positionDisplay))
        soundPlayer.pause()
    }

    fun justStop() {
        state.postValue(PlayingSongState.None)
        soundPlayer.stop()
    }

    fun playOrStop() {
        val s = state.value!!
        val nextState = when (s) {
            is PlayingSongState.Playing -> PlayingSongState.Stopped(s.song!!)
                    .apply {
                        soundPlayer.stop()
                    }
            is PlayingSongState.Stopped,
            is PlayingSongState.Ready,
            is PlayingSongState.Completed -> PlayingSongState.Playing(s.song!!, 0, prettySongPosition(0, s.song.durationLong))
                    .apply {
                        soundPlayer.play()
                    }
            is PlayingSongState.Paused -> PlayingSongState.Playing(s.song!!, s.position, s.positionDisplay)
                    .apply {
                        soundPlayer.play()
                    }
            is PlayingSongState.Seeking -> PlayingSongState.Playing(s.song!!, s.position, s.positionDisplay)
                    .apply {
                        soundPlayer.seekTo(position)
                    }
            else -> throw IllegalStateException("State before PLAY/STOP not allowed: $s") // should not reach this
        }
        state.postValue(nextState)
    }

    fun seekTo(position: Long, confirmedToPlayAt: Boolean) {
        assert(state.value !is PlayingSongState.None
                || state.value !is PlayingSongState.Prepare) {
            "The state before SEEK must not be NONE or PREPARE"
        }
        val s = state.value!!
        if (confirmedToPlayAt) {
            assert(state.value is PlayingSongState.Seeking) {
                "The state before confirmation SEEK must be SEEK"
            }
            val stateBeforeSeek = (s as PlayingSongState.Seeking).stateBeforeSeek
            if (stateBeforeSeek is PlayingSongState.Playing) { // we only seek the system player if the last state before seek was play
                soundPlayer.seekTo(position)
            }
            state.postValue(PlayingSongState.Seeking(s.song!!, position, prettySongDuration(position), stateBeforeSeek,
                    true))
        } else {
            val stateBeforeSeek = if (s is PlayingSongState.Seeking) {
                s.stateBeforeSeek
            } else {
                s
            }
            state.postValue(PlayingSongState.Seeking(s.song!!, position, prettySongDuration(position),
                    stateBeforeSeek, false))
        }
    }


    override fun onReady() {
        assert(state.value is PlayingSongState.Prepare) {
            "The state before READY must be PREPARE"
        }
        state.postValue(PlayingSongState.Ready(state.value!!.song!!))
    }

    override fun onTrack(position: Long) {
        assert(state.value !is PlayingSongState.None) {
            "The state before PLAYING must not be NONE"
        }
        val s = state.value!!
        if (s !is PlayingSongState.Seeking || s.confirmedToPlayAt) { //while seeking and not confirmed from ui don't dispatch playing cues
            val song = s.song!!
            state.postValue(PlayingSongState.Playing(song,
                    position,
                    prettySongDuration(position))
            )
        }
    }

    override fun complete() {
        assert(state.value is PlayingSongState.Playing) {
            "The state before COMPLETED must be PLAYING"
        }
        state.postValue(PlayingSongState.Completed(state.value!!.song!!))
    }

}