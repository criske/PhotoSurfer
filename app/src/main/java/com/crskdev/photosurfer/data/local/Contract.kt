package com.crskdev.photosurfer.data.local

/**
 * Created by Cristian Pela on 20.08.2018.
 */
object Contract {

    const val TABLE_PHOTOS = "photos"
    const val TABLE_USER_PHOTOS = "user_photos"
    const val TABLE_LIKE_PHOTOS = "like_photos"
    const val TABLE_SEARCH_PHOTOS = "search_photos"

    const val TABLE_USERS = "users"

    const val TABLE_COLLECTIONS = "collections"
    const val TABLE_COLLECTION_PHOTOS = "collection_photos"
    const val TABLE_COLLECTIONS_COLLECTION_PHOTOS = "collections_collection_photos"

    val TABLES = arrayOf(TABLE_PHOTOS, TABLE_USER_PHOTOS, TABLE_LIKE_PHOTOS, TABLE_USERS,
            TABLE_COLLECTIONS, TABLE_COLLECTION_PHOTOS)

}