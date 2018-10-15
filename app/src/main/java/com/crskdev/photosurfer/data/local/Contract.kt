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

    const val TABLE_PLAYWAVE = "playwaves"

    const val TABLE_PLAYWAVE_CONTENT = "playwave_contents"

    val PHOTO_TABLES = arrayOf(
            TABLE_PHOTOS,
            TABLE_USER_PHOTOS,
            TABLE_LIKE_PHOTOS,
            TABLE_SEARCH_PHOTOS,
            TABLE_COLLECTION_PHOTOS
    )

    val PHOTO_AND_COLLECTIONS_TABLES = PHOTO_TABLES + TABLE_COLLECTIONS

    val TABLES = PHOTO_TABLES +
            arrayOf(TABLE_USERS,
                    TABLE_COLLECTIONS)

}