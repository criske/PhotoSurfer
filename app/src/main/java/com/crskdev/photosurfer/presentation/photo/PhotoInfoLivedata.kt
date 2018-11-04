package com.crskdev.photosurfer.presentation.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.util.livedata.AbsentLiveData
import com.crskdev.photosurfer.util.livedata.filter
import com.crskdev.photosurfer.util.livedata.just
import com.crskdev.photosurfer.util.livedata.switchMap

/**
 * Created by Cristian Pela on 30.10.2018.
 */
class PhotoInfoLiveDataHelper(source: (id: String) -> LiveData<Photo>) {

    companion object {
        const val NO_PHOTO_ID = ""
    }

    private val photoIDLiveData = MutableLiveData<String>()
    private val photoInfoLiveData = photoIDLiveData.switchMap {
        if (it == NO_PHOTO_ID) {
            just(Photo.EMPTY)
        } else {
            source(it)
        }
    }

    fun getLiveData(): LiveData<Photo> = photoInfoLiveData.filter { it != null }

    fun setPhotoId(id: String? = NO_PHOTO_ID) {
        photoIDLiveData.value = id ?: NO_PHOTO_ID
    }

    fun clearPhotoId() {
        photoIDLiveData.value = NO_PHOTO_ID
    }

    operator fun invoke(): LiveData<Photo> = getLiveData()

    operator fun component1() = this

    operator fun component2() = getLiveData()
}