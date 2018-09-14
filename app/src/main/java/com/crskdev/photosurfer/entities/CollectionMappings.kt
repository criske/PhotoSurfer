package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.collections.CollectionEntity
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.collections.CollectionJSON
import com.crskdev.photosurfer.data.remote.collections.CollectionLiteJSON
import com.crskdev.photosurfer.data.remote.photo.AuthorJSON
import com.squareup.moshi.JsonAdapter

/**
 * Created by Cristian Pela on 31.08.2018.
 */

fun Collection.toCollectionDB() = CollectionEntity().apply {
    this.id = this@toCollectionDB.id
    this.publishedAt = this@toCollectionDB.publishedAt
    this.updatedAt = this@toCollectionDB.updatedAt
    this.coverPhotoUrls = this@toCollectionDB.coverPhotoUrls?.stringify()
    this.title = this@toCollectionDB.title
    this.description = this@toCollectionDB.description
    this.coverPhotoId = this@toCollectionDB.coverPhotoId
    this.curated = this@toCollectionDB.curated
    this.totalPhotos = this@toCollectionDB.totalPhotos
    this.private = this@toCollectionDB.private
    this.sharedKey = this@toCollectionDB.sharedKey
    this.ownerId = this@toCollectionDB.ownerId
    this.ownerUsername = this@toCollectionDB.ownerUsername
    this.links = this@toCollectionDB.links.stringify()
    this.total = this@toCollectionDB.pagingData?.total
    this.curr = this@toCollectionDB.pagingData?.curr
    this.next = this@toCollectionDB.pagingData?.next
    this.prev = this@toCollectionDB.pagingData?.prev
}

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

fun CollectionJSON.toJSONString(jsonAdapter: JsonAdapter<CollectionJSON>): String =
        jsonAdapter.toJson(this)

fun Collection.toJSON(): CollectionJSON =
        CollectionJSON().apply {
            this.id = this@toJSON.id
            this.publishedAt = this@toJSON.publishedAt
            this.updatedAt = this@toJSON.updatedAt
            this.coverPhoto = null
            this.title = this@toJSON.title
            this.description = this@toJSON.description
            this.curated = this@toJSON.curated
            this.totalPhotos = this@toJSON.totalPhotos
            this.private = this@toJSON.private
            this.sharedKey = this@toJSON.sharedKey
            this.owner = AuthorJSON().apply {
                this.id = this@toJSON.ownerId
                this.username = this@toJSON.ownerUsername
            }
            this.links = this@toJSON.links
        }

fun Collection.toLiteJSON(): CollectionLiteJSON = CollectionLiteJSON().apply {
    this.id = this@toLiteJSON.id
    this.publishedAt = this@toLiteJSON.publishedAt
    this.updatedAt = this@toLiteJSON.updatedAt
    this.title = this@toLiteJSON.title
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

fun CollectionLiteJSON.asLiteStr(): String = "$id#$title"

fun Collection.asLiteStr(): String = "$id#$title"

fun CollectionEntity.asLiteStr(): String = "$id#$title"

fun collectionsLiteStrAdd(collectionsStr: String, collectionStr: String): String {
    return if (!collectionsStr.contains(collectionStr)) {
        (collectionsStr.split("@") + collectionStr).joinToString("@")
    } else {
        collectionsStr
    }
}

fun collectionsLiteStrRemove(collectionsStr: String, collectionStr: String): String {
    return collectionsStr.replace("(@?$collectionStr)".toRegex(), "")
}

fun collectionsLiteJSONAsLiteStr(collections: List<CollectionLiteJSON>): String = collections.map { it.asLiteStr() }.joinToString("@")

fun collectionsAsLiteStr(collections: List<Collection>): String = collections.map { it.asLiteStr() }.joinToString("@")

fun toCollectionsFromLiteStr(liteStrList: String): List<Collection> {
    if (liteStrList.isEmpty()) {
        return emptyList()
    }
    return liteStrList.split("@").filter { it.isNotEmpty() }.map {
        val split = it.split("#")
        if (split.size != 2) {
            throw IllegalAccessException("Invalid parse from string to collection list")
        } else {
            Collection(split[0].toInt(), split[1],
                    "", "", "", sharedKey = "", ownerId = "", ownerUsername = "", links = emptyMap())
        }
    }
}

fun extractIdAndTitleFromCollectionStr(collectionStr: String): Pair<Int, String> =
        collectionStr.split("#").let { it[0].toInt() to it[1] }