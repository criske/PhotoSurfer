package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.RepositoryAction
import com.crskdev.photosurfer.entities.Photo

/**
 * Created by Cristian Pela on 20.08.2018.
 */
class ChoosablePhotoDataSourceFactory(
        val photoRepository: PhotoRepository,
        initialFilter: DataSourceFilter) : DataSource.Factory<Int, Photo>() {

    enum class Type {
        LIKED_PHOTOS, SEARCH_PHOTOS, TRENDING_PHOTOS, USER_PHOTOS
    }

    var currentFilter: DataSourceFilter = initialFilter
        private set

    override fun create(): DataSource<Int, Photo> {
        return when (currentFilter.type) {
            Type.TRENDING_PHOTOS -> photoRepository.getPhotos(RepositoryAction.TRENDING).create()
            Type.USER_PHOTOS -> photoRepository.getPhotos(RepositoryAction(RepositoryAction.Type.USER_PHOTOS)).create()
            Type.LIKED_PHOTOS -> photoRepository.getLikedPhotos().create()
            else -> TODO()
        }
    }

    fun changeFilter(filter: DataSourceFilter) {
        currentFilter = filter
    }

}


class DataSourceFilter(val type: ChoosablePhotoDataSourceFactory.Type, vararg val extras: Any) {
    companion object {
        val RANDOM = DataSourceFilter(ChoosablePhotoDataSourceFactory.Type.TRENDING_PHOTOS)
    }
}