package com.crskdev.photosurfer.data.local.photo.external

import android.content.Context

/**
 * Created by Cristian Pela on 16.09.2018.
 */

interface ExternalPhotoGalleryDB {
    fun dao(): ExternalPhotoGalleryDAO

}

class ExternalPhotoGalleryDBImpl(
        private val context: Context,
        private val directory: ExternalDirectory) : ExternalPhotoGalleryDB {

    override fun dao(): ExternalPhotoGalleryDAO = ExternalPhotoGalleryDAOImpl(context, directory)

}