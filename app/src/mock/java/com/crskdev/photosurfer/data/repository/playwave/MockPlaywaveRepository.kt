package com.crskdev.photosurfer.data.repository.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.Playwave
import com.crskdev.photosurfer.entities.PlaywavePhoto
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 16.10.2018.
 */
class MockPlaywaveRepository : PlaywaveRepository {

    override fun getAvailableSongs(filterSearch: String?): DataSource.Factory<Int, Song> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlaywaves(includePhotos: Boolean): LiveData<List<Playwave>> =
            MutableLiveData<List<Playwave>>().apply {
                postValue(listOf(
                        Playwave(1, "Play wave test title", Song(
                                1, "Foo", "Some Title Song", "DJ NaN",  TimeUnit.MINUTES.toMillis(5), true
                        ), emptyList()),
                        Playwave(1, "Play wave test title", Song(
                                1, "Foo", "Some Title Song", "DJ NaN", 2342429, false
                        ), emptyList())))
            }

    override fun getPlaywave(playwaveId: Int): LiveData<Playwave> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createPlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updatePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deletePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addPhotoToPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removePhotoFromPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}