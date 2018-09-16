package com.crskdev.photosurfer.data.local.photo.external

import android.content.ContentResolver
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.photo.PhotoEntity

/**
 * Created by Cristian Pela on 16.09.2018.
 */
class ExternalPhotoGalleryDataSourceFactory(
        private val contentResolver: ContentResolver,
        private val directory: ExternalDirectory

): DataSource.Factory<Int, PhotoEntity>(){

    override fun create(): DataSource<Int, PhotoEntity> = ExternalPhotoGalleryDataSource(
            contentResolver, directory)

}