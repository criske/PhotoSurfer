package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.entities.Photo

/**
 * Created by Cristian Pela on 20.08.2018.
 */
class ChoosablePhotoDataSourceFactory(
        val photoRepository: PhotoRepository,
        initialType: Type) : DataSource.Factory<Int, Photo>() {

    enum class Type {
        LIKED_PHOTOS, SEARCH_PHOTOS, RANDOM_PHOTOS
    }

    var currentType: Type = initialType
        private set

    override fun create(): DataSource<Int, Photo> {
        return when (currentType) {
            Type.RANDOM_PHOTOS -> photoRepository.getPhotos(null).create()
            Type.LIKED_PHOTOS -> photoRepository.getLikedPhotos().create()
            else -> TODO()
        }
    }

    fun changeType(type: Type) {
        currentType = type
    }

}
