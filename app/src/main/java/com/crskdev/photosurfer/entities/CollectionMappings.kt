package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.collections.CollectionEntity
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.collections.CollectionJSON

/**
 * Created by Cristian Pela on 31.08.2018.
 */
fun CollectionJSON.toCollectionDB(pagingData: PagingData): CollectionEntity =
        CollectionEntity().apply {
            this.id = this@toCollectionDB.id
            this.publishedAt = this@toCollectionDB.publishedAt
            this.updatedAt = this@toCollectionDB.updatedAt
            this.coverPhotoUrls = this@toCollectionDB.coverPhoto?.urls?.stringify()
            this.title = this@toCollectionDB.title
            this.description = this@toCollectionDB.description
            this.coverPhotoId = this@toCollectionDB.coverPhoto?.id
            this.curated = this@toCollectionDB.curated
            this.totalPhotos = this@toCollectionDB.totalPhotos
            this.private = this@toCollectionDB.private
            this.sharedKey = this@toCollectionDB.sharedKey
            this.ownerId = this@toCollectionDB.owner.id
            this.ownerUsername = this@toCollectionDB.owner.username
            this.links = this@toCollectionDB.links.stringify()
            this.total = pagingData.total
            this.curr = pagingData.curr
            this.next = pagingData.next
            this.prev = pagingData.prev
        }

fun CollectionEntity.toCollection(): Collection {
    val pagingData = PagingData(total ?: 0, curr ?: 1, prev, next)
    return Collection(
            id,
            title,
            description,
            publishedAt,
            updatedAt,
            curated,
            totalPhotos,
            private,
            sharedKey,
            coverPhotoId,
            coverPhotoUrls?.let { transformStrMapToUrls(it) },
            ownerId,
            ownerUsername,
            transformStrMapToMap(links),
            pagingData
    )
}