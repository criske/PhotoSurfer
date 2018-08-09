package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.toDbEntity
import com.crskdev.photosurfer.entities.toPhoto

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository {

    fun getPhotos(): DataSource.Factory<Int, Photo>

    fun insertPhotos(page: Int)

    fun clear()
}

class PhotoRepositoryImpl(
        private val api: PhotoAPI,
        private val dao: PhotoDAO
) : PhotoRepository {

    override fun getPhotos(): DataSource.Factory<Int, Photo> = dao.getPhotos()
            .mapByPage { page -> page.map { it.toPhoto() } }


    override fun insertPhotos(page: Int) {
        val response = api.getPhotos(page).execute()
        val pagingData = PhotoPagingData.createFromHeader(response.headers())
        response.body()
                ?.map {
                    it.toDbEntity(pagingData, dao.getNextIndex())
                }?.apply {
                    dao.insertPhotos(this)
                }
    }

    override fun clear() {
        dao.clear()
    }

}