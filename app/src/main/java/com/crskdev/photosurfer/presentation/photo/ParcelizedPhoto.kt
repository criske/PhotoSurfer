package com.crskdev.photosurfer.presentation.photo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Parcelize
class ParcelizedPhoto(
        val id: String,
        val createdAt: String,
        val updatedAt: String,
        val width: Int, val height: Int, val colorString: String,
        val urls: Map<String, String>,
        val description: String?,
        val categories: List<String>,
        val collections: String,
        val likes: Int, val likedByMe: Boolean, val views: Int,
        val authorId: String,
        val authorUsername: String,
        val total: Int?, val curr: Int?, val prev: Int?, val next: Int?) : Parcelable {
}
