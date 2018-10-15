package com.crskdev.photosurfer.data.repository.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.playwave.PlaywaveDAO
import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.data.local.playwave.song.SongDAO
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.*
import com.crskdev.photosurfer.services.executors.ExecutorType
import com.crskdev.photosurfer.services.executors.ExecutorsManager

/**
 * Created by Cristian Pela on 14.10.2018.
 */
interface PlaywaveRepository : Repository {

    fun getAvailableSongs(filterSearch: String? = null): DataSource.Factory<Int, Song>

    fun getPlaywaves(includePhotos: Boolean = false): LiveData<List<Playwave>>

    fun getPlaywave(playwaveId: Int): LiveData<Playwave>

    fun createPlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>? = null)

    fun updatePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>? = null)

    fun deletePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>? = null)

    fun addPhotoToPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>? = null)

    fun removePhotoFromPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>? = null)

}

class PlaywaveRepositoryImpl(executorsManager: ExecutorsManager,
                             private val songDAO: SongDAO,
                             private val playwaveDAO: PlaywaveDAO) : PlaywaveRepository {

    private val diskExecutor = executorsManager[ExecutorType.DISK]

    private val uiExecutor = executorsManager[ExecutorType.UI]

    override fun getAvailableSongs(filterSearch: String?): DataSource.Factory<Int, Song> =
            songDAO.getSongs(filterSearch)

    override fun getPlaywaves(includePhotos: Boolean): LiveData<List<Playwave>> =
            if (includePhotos) {
                Transformations.map(playwaveDAO.getPlaywavesWithPhotosLiveData()) { l ->
                    l.asSequence().map { pwp ->
                        val song = songDAO.getSongById(pwp.playwaveEntity.songId)
                        val photos = pwp.playwaveContents.map { it.toPlaywavePhoto() }
                        pwp.toPlaywave(song)
                    }.toList()
                }
            } else {
                Transformations.map(playwaveDAO.getPlaywavesLiveData()) { l ->
                    l.asSequence().map {
                        val song = songDAO.getSongById(it.songId)
                        it.toPlaywave(song)
                    }.toList()
                }
            }

    override fun getPlaywave(playwaveId: Int): LiveData<Playwave> =
            Transformations.map(playwaveDAO.getPlaywaveWithPhotosLiveData(playwaveId)) { pwp ->
                val song = songDAO.getSongById(pwp.playwaveEntity.songId)
                val photos = pwp.playwaveContents.map { it.toPlaywavePhoto() }
                pwp.toPlaywave(song)
            }

    override fun createPlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        diskExecutor {
            val created = playwaveDAO.insert(playwave.toDB()) > 0
            callback?.run {
                uiExecutor {
                    if (created) {
                        onSuccess(playwave)
                    } else {
                        onError(Error("Playwave not created!"))
                    }
                }
            }
        }
    }

    override fun updatePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        diskExecutor {
            val updated = playwaveDAO.update(playwave.toDB()) > 0
            callback?.run {
                uiExecutor {
                    if (updated) {
                        onSuccess(playwave)
                    } else {
                        onError(Error("Playwave not updated!"))
                    }
                }
            }

        }
    }

    override fun deletePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        diskExecutor {
            val deleted = playwaveDAO.delete(playwave.toDB()) > 0
            callback?.run {
                uiExecutor {
                    if (deleted) {
                        onSuccess(playwave)
                    } else {
                        onError(Error("Playwave not deleted!"))
                    }
                }
            }
        }
    }

    override fun addPhotoToPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>?) {
        diskExecutor {
            val added = playwaveDAO.addPhotoToPlaywave(photo.toDb(playwaveId)) > 0
            callback?.run {
                uiExecutor {
                    if (added) {
                        onSuccess(Unit)
                    } else {
                        onError(Error("Photo not added to playwave with id $playwaveId"))
                    }
                }
            }
        }
    }

    override fun removePhotoFromPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>?) {
        diskExecutor {
            val removed = playwaveDAO.removePhotoFromPlaywave(photo.toDb(playwaveId)) > 0
            callback?.run {
                uiExecutor {
                    if (removed) {
                        onSuccess(Unit)
                    } else {
                        onError(Error("Photo not removed from playwave with id $playwaveId"))
                    }
                }
            }
        }
    }

}