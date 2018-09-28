package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData
import java.util.*

data class Photo(val id: String,
                 val createdAt: String,
                 val updatedAt: String,
                 val width: Int, val height: Int, val colorString: String,
                 val urls: EnumMap<ImageType, String>,
                 val description: String? = null,
                 val categories: List<String>,
                 val collections: List<CollectionLite>,
                 val likes: Int,
                 val likedByMe: Boolean,
                 val views: Int,
                 val authorId: String,
                 val authorUsername: String,
                 val authorFullName: String,
                 override val pagingData: PagingData? = null,
                 val extras: Any? = null) : BaseEntity()