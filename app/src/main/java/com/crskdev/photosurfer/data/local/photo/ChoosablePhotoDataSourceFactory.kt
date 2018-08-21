package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.entities.Photo

/**
 * Created by Cristian Pela on 20.08.2018.
 */
class ChoosablePhotoDataSourceFactory(
        val photoRepository: PhotoRepository,
        initialFilter: DataSourceFilter) : DataSource.Factory<Int, Photo>() {

    enum class Type {
        LIKED_PHOTOS, SEARCH_PHOTOS, RANDOM_PHOTOS
    }

    var currentFilter: DataSourceFilter = initialFilter
        private set

    override fun create(): DataSource<Int, Photo> {
        return when (currentFilter.type) {
            Type.RANDOM_PHOTOS -> photoRepository.getPhotos(null).create()
            Type.LIKED_PHOTOS -> photoRepository.getLikedPhotos().create()
            else -> TODO()
        }
    }

    fun changeFilter(filter: DataSourceFilter) {
        currentFilter = filter
    }

}


class DataSourceFilter(val type: ChoosablePhotoDataSourceFactory.Type, vararg extras: Any?){
    companion object {
        val RANDOM = DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.RANDOM_PHOTOS)
        val LIKED_PHOTOS = DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.LIKED_PHOTOS)
    }
}