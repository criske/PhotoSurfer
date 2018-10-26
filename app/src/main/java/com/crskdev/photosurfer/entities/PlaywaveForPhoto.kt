package com.crskdev.photosurfer.entities

/**
 * Created by Cristian Pela on 25.10.2018.
 */
data class PlaywaveForPhoto(val playwaveId: Int,
                             val playwaveTitle: String,
                             val playwaveSize: Int,
                             val photoId: String,
                             val hasPhoto: Boolean)