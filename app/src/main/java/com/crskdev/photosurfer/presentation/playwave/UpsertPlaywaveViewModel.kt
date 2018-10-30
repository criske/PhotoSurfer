package com.crskdev.photosurfer.presentation.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.entities.Playwave
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.util.livedata.*
import java.lang.Error

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class UpsertPlaywaveViewModel(
        private val executor: KExecutor,
        private val playwaveRepository: PlaywaveRepository,
        playwaveSoundPlayer: PlaywaveSoundPlayer,
        showPlaywavePhotos: Boolean = false) : ViewModel() {

    companion object {
        private const val NO_PLAYWAVE_ID = -1
    }

    private val searchQueryLiveData: MutableLiveData<String?> = MutableLiveData<String?>().apply {
        value = null
    }

    private val songStateController = PlayingSongStateController(playwaveSoundPlayer)

    val foundSongsLiveData: LiveData<PagedList<SongUI>> = searchQueryLiveData.switchMap { query ->
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

    private val selectedPlaywaveLiveData: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        value = NO_PLAYWAVE_ID
    }
    val playwaveLiveData: LiveData<PlaywaveUI> = selectedPlaywaveLiveData
            .switchMap { id ->
                if (id == NO_PLAYWAVE_ID) {
                    just(NO_PLAYWAVE_ID).map { PlaywaveUI.NONE }
                } else {
                    playwaveRepository.getPlaywave(id, showPlaywavePhotos).map { it.toUI() }
                }
            }

    val messageLiveData = SingleLiveEvent<Message>()

    fun selectPlaywave(id: Int) {
        selectedPlaywaveLiveData.value = id
    }

    fun upsertPlaywave(title: String, withPhotoId: String? = null) {
        val t = title.trim()
        if (t.isNotEmpty()) {
            playwaveLiveData.value?.let {
                if (it.song == null) {
                    messageLiveData.value = Message.ErrorNoSong
                } else {
                    val playwave = it.copy(title = title).toEntity()
                    if (playwave.id == -1) {
                        playwaveRepository.createPlaywave(playwave, withPhotoId, object : Repository.Callback<Playwave> {
                            override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                                messageLiveData.postValue(Message.Error(error))
                            }

                            override fun onSuccess(data: Playwave, extras: Any?) {
                                messageLiveData.value = Message.Added
                                selectedPlaywaveLiveData.value = NO_PLAYWAVE_ID
                            }
                        })
                    } else {
                        playwaveRepository.updatePlaywave(playwave, object : Repository.Callback<Playwave> {
                            override fun onError(error: Throwable, isAuthenticationError: Boolean) =
                                    messageLiveData.postValue(Message.Error(error))

                            override fun onSuccess(data: Playwave, extras: Any?) =
                                    messageLiveData.postValue(Message.Updated)
                        })
                    }
                }

            }
        } else {
            messageLiveData.value = Message.ErrorNoTitle
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
        songStateController.loadAndPlay(song)
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

    sealed class Message {
        object Added : Message()
        object Updated : Message()
        open class Error(val err: Throwable) : Message()
        object ErrorNoSong : Message.Error(java.lang.Error("No Song Provided"))
        object ErrorNoTitle : Message.Error(java.lang.Error("No Title Provided"))
    }
}

