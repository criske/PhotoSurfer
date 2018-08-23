package com.crskdev.photosurfer.entities

import android.media.Image
import android.os.Bundle
import androidx.core.os.bundleOf
import com.crskdev.photosurfer.data.local.photo.LikePhotoEntity
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.local.photo.SearchPhotoEntity
import com.crskdev.photosurfer.data.local.photo.UserPhotoEntity
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.presentation.photo.ParcelizedPhoto
import java.util.*

/**
 * Created by Cristian Pela on 09.08.2018.
 */

internal const val KV_DELIM = "@@"
internal const val ENTRY_DELIM = ";"

fun PhotoEntity.toPhoto(): Photo =
        Photo(id, createdAt, updatedAt,
                width, height,
                colorString,
                urls.split(ENTRY_DELIM)
                        .fold(EnumMap<ImageType, String>(ImageType::class.java))
                        { a, c -> c.split(KV_DELIM).let { a.apply { a[ImageType.valueOf(it[0].toUpperCase())] = it[1] } } },
                categories?.split(ENTRY_DELIM)?.toList() ?: emptyList(),
                likes, likedByMe,
                views,
                authorId,
                authorUsername,
                PhotoPagingData(this.total ?: -1, this.curr ?: -1, this.prev, this.next),
                indexInResponse + 1
        )

fun PhotoJSON.toDbEntity(pagingData: PhotoPagingData, nextIndex: Int): PhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, PhotoEntity::class.java)

fun PhotoJSON.toUserPhotoDbEntity(pagingData: PhotoPagingData, nextIndex: Int): UserPhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, UserPhotoEntity::class.java)

fun PhotoJSON.toLikePhotoDbEntity(pagingData: PhotoPagingData, nextIndex: Int): LikePhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, LikePhotoEntity::class.java)

fun PhotoJSON.toSearchPhotoDbEntity(pagingData: PhotoPagingData, nextIndex: Int): SearchPhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, SearchPhotoEntity::class.java)

fun Photo.toLikePhotoDbEntity(nextIndex: Int): LikePhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, LikePhotoEntity::class.java)

fun Photo.parcelize(): ParcelizedPhoto = ParcelizedPhoto(id,
        createdAt, updatedAt,
        width, height,
        colorString,
        urls.entries.fold(mutableMapOf()) { a, c -> a.apply { put(c.key.toString(), c.value) } },
        categories, likes, likedByMe, views, authorId, authorUsername,
        pagingData?.total, pagingData?.curr, pagingData?.prev, pagingData?.next)

fun ParcelizedPhoto.deparcelize(): Photo =
        Photo(id,
                createdAt, updatedAt,
                width, height,
                colorString,
                urls.entries.fold(EnumMap<ImageType, String>(ImageType::class.java))
                { a, c -> a.apply { put(ImageType.valueOf(c.key.toUpperCase()), c.value) } },
                categories, likes, likedByMe, views, authorId, authorUsername,
                PhotoPagingData(total ?: -1, curr ?: -1, prev, next))

