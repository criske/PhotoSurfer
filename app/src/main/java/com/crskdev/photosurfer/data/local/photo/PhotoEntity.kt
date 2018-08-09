package com.crskdev.photosurfer.data.local.photo

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Entity(tableName = "photos")
class PhotoEntity {
    @PrimaryKey
    lateinit var id: String
    var indexInResponse: Int = -1
    lateinit var createdAt: String
    lateinit var updatedAt: String
    var width: Int = -1
    var height: Int = -1
    lateinit var colorString: String
    /**
     * unwind link map key-values and concat them with ";"
     */
    lateinit var urls: String
    /*
        unwind values and concat them with ";"
     */
    var categories: String? = null
    var likes: Int = 0
    var likedByMe: Boolean = false
    var views: Int = 0
    lateinit var authorId: String
    lateinit var authorUsername: String

    //paging data
    var total: Int? = null
    var curr: Int? = null
    var prev: Int? = null
    var next: Int? = null
}

/*
data class Photo(val id: String, val createdAt: Long, val updatedAt: Long,
                 val width: Int, val height: Int, val colorString: String,
                 val urls: Map<String, String>,
                 val categories: List<String>,
                 val likes: Int, val likedByMe: Boolean, val views: Int,
                 val authorId: String,
                 val authorUsername: String,
                 val pagingData: PhotoPagingData? = null,
                 val extras: Any? = null)
 */