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
                                    SongUI(it.id, it.path, it.title, it.artist, it.prettyDuration(), it.duration, it.exists)
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
        (selectedSongLiveData as MutableLiveData).value = null
        stopPlayerIfNeeded()
    }

    fun stopPlayerIfNeeded() {
        (playingSongStateLiveData as MutableLiveData).value = PlayingSongState.None
    }

    fun playSong(songUI: SongUI) {

    }

    fun selectSong(song: SongUI) {
        (selectedSongLiveData as MutableLiveData).value = song
    }

    override fun onCleared() {
        super.onCleared()
    }
}

sealed class PlayingSongState() {
    object None : PlayingSongState()
    class Stopped(val song: SongUI) : PlayingSongState()
    class Started(val song: SongUI, val position: Long, val total: Long) : PlayingSongState()
    class Paused(val song: SongUI, val position: Long, val total: Long) : PlayingSongState()
}