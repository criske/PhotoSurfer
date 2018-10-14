package com.crskdev.photosurfer.data.local.photo.external

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import androidx.paging.PositionalDataSource
import com.crskdev.photosurfer.data.local.ContentResolverDataSource
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.transformMapUrls
import java.util.*

/**
 * Created by Cristian Pela on 16.09.2018.
 */
class ExternalPhotoGalleryDataSource(
        contentResolver: ContentResolver,
        directory: ExternalDirectory) : ContentResolverDataSource<PhotoEntity>(contentResolver,
        Config(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA,
                Config.Where(MediaStore.Images.Media.DATA + " like ? ", "%${directory.name}%"))) {

    override fun convertRow(cursor: Cursor): PhotoEntity {
        /*
    lateinit var id: String

    lateinit var createdAt: String
    lateinit var updatedAt: String
    var width: Int = -1
    var height: Int = -1
    lateinit var colorString: String
    /**
     * unwind link map key-values and concat them with ";"
     */
    //TODO use type converters
    lateinit var urls: String
    var description: String? = null
    /*
    unwind values and concat them with ";"
     */
    //TODO use type converters
    var categories: String? = null
    //TODO use type converters
    lateinit var collections: List<CollectionLite>
    var likes: Int = 0
    var likedByMe: Boolean = false
    var views: Int = 0
    lateinit var authorId: String
    lateinit var authorUsername: String
         */
        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
        return PhotoEntity().apply {
            id = path
            createdAt = ""
            updatedAt = ""
            colorString = "#ffffff"
            urls = transformMapUrls(ImageType.values().fold(EnumMap<ImageType, String>(ImageType::class.java)) { acc, curr ->
                acc.apply {
                    acc[curr] = path
                }
            })
            collections = emptyList()
            authorId = ""
            authorUsername = ""
            authorFullName = ""
        }
    }

}
