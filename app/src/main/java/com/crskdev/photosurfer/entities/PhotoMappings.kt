package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.collections.CollectionPhotoEntity
import com.crskdev.photosurfer.data.local.photo.LikePhotoEntity
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.local.photo.SearchPhotoEntity
import com.crskdev.photosurfer.data.local.photo.UserPhotoEntity
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.photo.AuthorJSON
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
                transformStrMapToUrls(urls),
                description,
                categories?.split(ENTRY_DELIM)?.toList() ?: emptyList(),
                collections?.let { toCollectionsFromLiteStr(it) } ?: emptyList(),
                likes,
                likedByMe,
                views,
                authorId,
                authorUsername,
                PagingData(this.total ?: -1, this.curr
                        ?: -1, this.prev, this.next),
                indexInResponse + 1
        )

fun PhotoJSON.toDbEntity(pagingData: PagingData, nextIndex: Int): PhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, PhotoEntity::class.java)

fun PhotoJSON.toUserPhotoDbEntity(pagingData: PagingData, nextIndex: Int): UserPhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, UserPhotoEntity::class.java)

fun PhotoJSON.toLikePhotoDbEntity(pagingData: PagingData, nextIndex: Int): LikePhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, LikePhotoEntity::class.java)

fun PhotoJSON.toSearchPhotoDbEntity(pagingData: PagingData, nextIndex: Int): SearchPhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, SearchPhotoEntity::class.java)

fun PhotoJSON.toCollectionPhotoDbEntity(pagingData: PagingData, nextIndex: Int): CollectionPhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, CollectionPhotoEntity::class.java)

fun Photo.toLikePhotoDbEntity(nextIndex: Int): LikePhotoEntity =
        PhotoToEntityMappingReflect.toDbEntity(this, pagingData, nextIndex, LikePhotoEntity::class.java)

fun Photo.toJSON(): PhotoJSON =
        PhotoJSON().apply {
            id = this@toJSON.id
            createdAt = this@toJSON.createdAt
            updatedAt = this@toJSON.updatedAt
            width = this@toJSON.width
            height = this@toJSON.height
            colorString = this@toJSON.colorString
            urls = this@toJSON.urls
            description = this@toJSON.description
            categories = this@toJSON.categories
            collections = this@toJSON.collections.map { it.toLiteJSON() }
            likes = this@toJSON.likes
            likedByMe = this@toJSON.likedByMe
            views = this@toJSON.views
            author = AuthorJSON().apply {
                id = this@toJSON.id
                username = this@toJSON.authorUsername
            }
        }

fun Photo.parcelize(): ParcelizedPhoto = ParcelizedPhoto(id,
        createdAt, updatedAt,
        width, height,
        colorString,
        urls.entries.fold(mutableMapOf()) { a, c -> a.apply { put(c.key.toString(), c.value) } },
        description,
        categories,
        collectionsAsLiteStr(collections),
        likes, likedByMe, views, authorId, authorUsername,
        pagingData?.total, pagingData?.curr, pagingData?.prev, pagingData?.next)

fun ParcelizedPhoto.deparcelize(): Photo =
        Photo(id,
                createdAt, updatedAt,
                width, height,
                colorString,
                urls.entries.fold(EnumMap<ImageType, String>(ImageType::class.java))
                { a, c -> a.apply { put(ImageType.valueOf(c.key.toUpperCase()), c.value) } },
                description,
                categories,
                toCollectionsFromLiteStr(collections),
                likes, likedByMe, views, authorId, authorUsername,
                PagingData(total ?: -1, curr ?: -1, prev, next))

