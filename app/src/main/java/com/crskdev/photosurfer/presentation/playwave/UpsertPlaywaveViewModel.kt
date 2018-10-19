package com.crskdev.photosurfer.presentation.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig
import kotlin.math.roundToLong

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class UpsertPlaywaveViewModel(
        private val executor: KExecutor,
        private val playwaveRepository: PlaywaveRepository) : ViewModel() {


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
        stopPlayerIfNeeded()
    }

    fun stopPlayerIfNeeded() {
        playState().value = PlayingSongState.None
    }

    fun playSong(songUI: SongUI) {
        selectedSong().value = songUI
        playState().value = PlayingSongState.Playing(songUI, 0, songUI.durationLong)
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
                playState.value = PlayingSongState.Paused(it, (playState.value as PlayingSongState.Playing).position, it.durationLong)
            }
        }
    }

    fun playSelectedSong(jumpToPercent: Int? = null) {
        selectedSong().value?.let {
            val playState = playState()
            if (jumpToPercent == null && playState.value is PlayingSongState.Paused) {
                playState.value = PlayingSongState.Playing(it, (playState.value as PlayingSongState.Paused).position,
                        it.durationLong)
            } else if (jumpToPercent != null) {
                playState.value = PlayingSongState.Playing(it, ((jumpToPercent / 100f) * it.durationLong).roundToLong(),
                        it.durationLong)
            }
        }

    }

    fun selectSong(song: SongUI) {
        selectedSong().value = song
        playState().value = PlayingSongState.Stopped(song)

    }

    fun removeSelectedSong() {
        val currentSong = selectedSongLiveData.value
        selectedSong().value = null
        val state = playState()
        if (state.value !is PlayingSongState.None && state.value?.song?.id == currentSong?.id) {
            state.value = PlayingSongState.None
        }
    }

    private fun playState() = (playingSongStateLiveData as MutableLiveData)

    private fun selectedSong() = (selectedSongLiveData as MutableLiveData)

    override fun onCleared() {
        stopPlayerIfNeeded()
    }
}

sealed class PlayingSongState(val song: SongUI?) {
    object None : PlayingSongState(null)
    class Stopped(song: SongUI) : PlayingSongState(song)
    class Playing(song: SongUI, val position: Long, val total: Long) : PlayingSongState(song)
    class Paused(song: SongUI, val position: Long, val total: Long) : PlayingSongState(song)
}