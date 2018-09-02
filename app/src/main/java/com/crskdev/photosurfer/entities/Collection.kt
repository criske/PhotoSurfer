package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData
import java.util.*

/**
 * Created by Cristian Pela on 31.08.2018.
 */
data class Collection(
        val id: Int,
        val title: String,
        val description: String,
        val publishedAt: String,
        val updatedAt: String,
        val curated: Boolean = false,
        val totalPhotos: Int = 0,
        val private: Boolean = false,
        val sharedKey: String,
        val coverPhotoId: String? = null,
        val coverPhotoUrls: EnumMap<ImageType, String>? = null,
        val ownerId: String,
        val ownerUsername: String,
        val links: Map<String, String>,
        val pagingData: PagingData? = null
)