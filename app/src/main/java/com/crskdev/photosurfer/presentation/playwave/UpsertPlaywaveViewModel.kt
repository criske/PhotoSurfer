package com.crskdev.photosurfer.presentation.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.entities.Playwave
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig

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
                                p.asSequence().map { it.toUI() }.toList()
                            }, c)
                            .setFetchExecutor(executor)
                            .build()
                }
    }

    val playingSongStateLiveData: LiveData<PlayingSongState> = songStateController.getStateLiveData()

    val selectedPlaywaveLiveData: LiveData<Int> = MutableLiveData<Int>()

    val playwaveLiveData: LiveData<PlaywaveUI> = Transformations.switchMap(selectedPlaywaveLiveData) { id ->
        Transformations.map(playwaveRepository.getPlaywave(id)) { it.toUI() }
    }

    fun selectPlaywave(id: Int) {
        (selectedPlaywaveLiveData as MutableLiveData).value = id
    }

    fun upsertPlaywave(title: String) {
        val t = title.trim()
        if (t.isNotEmpty()) {
            playwaveLiveData.value?.let {
                val playwave = it.copy(title = title).toEntity()
                if (playwave.id == -1) {
                    playwaveRepository.createPlaywave(playwave)
                } else {
                    playwaveRepository.updatePlaywave(playwave)
                }
            }
        }
    }

    fun search(query: String?) {
        searchQueryLiveData.value = query
    }

    fun clearPlayingSong() {
        songStateController.clear()
    }

    fun pausePlayingSong() {
        songStateController.pause()
    }

    fun playOrStopSong() {
        songStateController.playOrStop()
    }

    fun seekTo(position: Int, confirmedToPlayAt: Boolean = false) {
        songStateController.seekTo(position, confirmedToPlayAt)
    }

    fun selectSongToPlay(song: SongUI) {
        songStateController.prepare(song)
    }

    fun setPlaywaveSong(song: SongUI) {
        val data = playwaveLiveData as MutableLiveData
        data.value = data.value.let {
            PlaywaveUI(it?.id ?: -1, it?.title ?: "", it?.size ?: 0, song, song.exists, it?.photos
                    ?: emptyList())
        }
    }

    fun removePlaywaveSong() {
        val data = playwaveLiveData as MutableLiveData
        data.value = data.value.let {
            PlaywaveUI(it?.id ?: -1, it?.title ?: "", it?.size ?: 0, null, true, it?.photos
                    ?: emptyList())
        }
    }

    override fun onCleared() {
        songStateController.release()
    }

    fun justStop() {
        songStateController.justStop()
    }
}

