package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData

/**
 * Created by Cristian Pela on 09.08.2018.
 */

private const val KV_DELIM = "@@"
private const val ENTRY_DELIM = ";"

fun PhotoJSON.toPhoto(photoPagingData: PhotoPagingData? = null): Photo =
        Photo(id, createdAt, updatedAt,
                width, height,
                colorString, urls, categories,
                likes, likedByMe,
                views, author.id,
                author.username,
                photoPagingData)

fun PhotoEntity.toPhoto(): Photo =
        Photo(id, createdAt, updatedAt,
                width, height,
                colorString,
                urls.split(ENTRY_DELIM)
                        .fold(mutableMapOf()) { a, c -> c.split(KV_DELIM).let { a.apply { a[it[0]] = it[1] } } },
                categories?.split(ENTRY_DELIM)?.toList() ?: emptyList(),
                likes, likedByMe,
                views,
                authorId,
                authorUsername,
                PhotoPagingData(this.total ?: -1, this.curr ?: -1, this.prev, this.next),
                indexInResponse + 1
        )

fun Photo.toDbEntity(nextIndex: Int): PhotoEntity =
        PhotoEntity().apply {
            id = this@toDbEntity.id
            createdAt = this@toDbEntity.createdAt
            updatedAt = this@toDbEntity.updatedAt
            width = this@toDbEntity.width
            height = this@toDbEntity.height
            colorString = this@toDbEntity.colorString
            urls = this@toDbEntity.urls.entries.map { "${it.key}$KV_DELIM${it.value}" }.joinToString(ENTRY_DELIM)
            categories = this@toDbEntity.categories.takeIf { it.isNotEmpty() }?.joinToString(ENTRY_DELIM)
            likes = this@toDbEntity.likes
            likedByMe = this@toDbEntity.likedByMe
            views = this@toDbEntity.views
            authorId = this@toDbEntity.authorId
            authorUsername = this@toDbEntity.authorUsername
            total = this@toDbEntity.pagingData?.total
            curr = this@toDbEntity.pagingData?.curr
            next = this@toDbEntity.pagingData?.next
            prev = this@toDbEntity.pagingData?.prev
            indexInResponse = nextIndex

        }

fun PhotoJSON.toDbEntity(pagingData: PhotoPagingData, nextIndex: Int): PhotoEntity =
        PhotoEntity().apply {
            id = this@toDbEntity.id
            createdAt = this@toDbEntity.createdAt
            updatedAt = this@toDbEntity.updatedAt
            width = this@toDbEntity.width
            height = this@toDbEntity.height
            colorString = this@toDbEntity.colorString
            urls = this@toDbEntity.urls.entries.map { "${it.key}$KV_DELIM${it.value}" }.joinToString(ENTRY_DELIM)
            categories = this@toDbEntity.categories.takeIf { it.isNotEmpty() }?.joinToString(ENTRY_DELIM)
            likes = this@toDbEntity.likes
            likedByMe = this@toDbEntity.likedByMe
            views = this@toDbEntity.views
            authorId = this@toDbEntity.author.id
            authorUsername = this@toDbEntity.author.username
            total = pagingData.total
            curr = pagingData.curr
            next = pagingData.next
            prev = pagingData.prev
            indexInResponse = nextIndex
        }

