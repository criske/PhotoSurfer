package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData
import java.util.*

/**
 * Created by Cristian Pela on 31.08.2018.
 */
data class Collection(
        val id: Int,
        val title: String,
        val description: String? = null,
        val publishedAt: String,
        val updatedAt: String,
        val curated: Boolean = false,
        val totalPhotos: Int = 0,
        val private: Boolean = false,
        val sharedKey: String,
        val coverPhotoId: String? = null,
        val coverPhotoUrls: EnumMap<ImageType, String>? = null,
        val coverPhotoAuthorUsername: String? = null,
        val coverPhotoAuthorFullName: String? = null,
        val ownerId: String,
        val ownerUsername: String,
        val links: Map<String, String>,
        override val pagingData: PagingData? = null
) : BaseEntity() {
    companion object {
        fun just(title: String, description: String? = null, private: Boolean): Collection =
                Collection(-1, title, description ?: "", "", "", false,
                        0, private, "", null, null,
                        null, null,"", "", emptyMap())
    }
}