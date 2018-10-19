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
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class UpsertPlaywaveViewModel(
        private val executor: KExecutor,
        private val playwaveRepository: PlaywaveRepository,
        private val playwaveSoundPlayer: PlaywaveSoundPlayer) : ViewModel() {

    private val searchQueryLiveData: MutableLiveData<String?> = MutableLiveData<String?>().apply {
        value = null
    }

    val foundSongsLiveData: LiveData<PagedList<SongUI>> = Transformations.switchMap(searchQueryLiveData) { query ->
        defaultPageListConfig()
                .let { c ->
                    LivePagedListBuilder<Int, SongUI>(playwaveRepository
                            .getAvailableSongs(query)
                            .mapByPage { p ->
                                p.asSequence().map {
                                    SongUI(it.id, it.path, it.title, it.artist, it.prettyDuration(), it.toString(),
                                            it.duration, it.exists)
                                }.toList()
                            }, c)
                            .setFetchExecutor(executor)
                            .build()
                }
    }

    val playingSongStateLiveData: LiveData<PlayingSongState> = MutableLiveData()

    var selectedSongLiveData: LiveData<SongUI?> = MutableLiveData<SongUI>()

    fun search(query: String?) {
        searchQueryLiveData.value = query
    }

    fun clear() {
        removeSelectedSong()
    }

    fun stopSelectedSong() {
        selectedSong().value?.let {
            playState().value = PlayingSongState.Stopped(it)
        }
    }

    fun pauseSelectedSong() {
        selectedSong().value?.let {
            val playState = playState()
            if (playState.value is PlayingSongState.Playing) {
                playState.value = PlayingSongState.Paused(it)
                playwaveSoundPlayer.pause()
            }
        }
    }

    fun playSelectedSong(jumpToPercent: Int? = null) {
        selectedSong().value?.let {
            val playState = playState()
            if (jumpToPercent == null && playState.value is PlayingSongState.Paused) {
                playwaveSoundPlayer.start()
            } else if (jumpToPercent != null) {
                playwaveSoundPlayer.seekTo(jumpToPercent * it.durationLong)
            }
        }

    }

    fun selectSong(song: SongUI) {
        selectedSong().value = song
        playState().value = PlayingSongState.Started(song)
        playwaveSoundPlayer.load(song.path)
    }

    fun removeSelectedSong() {
        selectedSong().value = null
        val state = playState()
        state.value = PlayingSongState.None
        playwaveSoundPlayer.unload()
    }

    private fun playState() = (playingSongStateLiveData as MutableLiveData)

    private fun calculateProgressPercent(position: Long, total: Long): Int = ((position / total.toFloat()) * 100).roundToInt()

    private fun selectedSong() = (selectedSongLiveData as MutableLiveData)

    override fun onCleared() {
        playwaveSoundPlayer.release()
    }
}

sealed class PlayingSongState(val song: SongUI?) {
    object None : PlayingSongState(null)
    class Started(song: SongUI) : PlayingSongState(song)
    class Stopped(song: SongUI) : PlayingSongState(song)
    class Playing(song: SongUI, val percent: Int) : PlayingSongState(song)
    class Paused(song: SongUI) : PlayingSongState(song)
}