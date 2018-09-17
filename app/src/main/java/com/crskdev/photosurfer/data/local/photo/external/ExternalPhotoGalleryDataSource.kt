package com.crskdev.photosurfer.data.local.photo.external

import android.content.ContentResolver
import android.database.Cursor
import android.media.ExifInterface
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import androidx.paging.PositionalDataSource
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.transformMapUrls
import java.lang.Exception
import java.util.*

/**
 * Created by Cristian Pela on 16.09.2018.
 */
class ExternalPhotoGalleryDataSource(
        private val contentResolver: ContentResolver,
        directory: ExternalDirectory) : PositionalDataSource<PhotoEntity>() {

    companion object {
        private val CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        private const val WHERE_IN_FOLDER = MediaStore.Images.Media.DATA + " like ? "
    }

    init {
        directory.addWeakListener { _, _ ->
            invalidate()
        }
    }

    private val whereInFolderArg = arrayOf("%${directory.name}%")

    private fun countItems(): Int {
        return ContentResolverCompat.query(contentResolver, CONTENT_URI,
                arrayOf("COUNT(*) AS count"),
                WHERE_IN_FOLDER, whereInFolderArg,
                null,
                null)
                .use {
                    it.moveToFirst()
                    it.getInt(0)
                }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<PhotoEntity>) {
        val totalCount = countItems()
        if (totalCount == 0) {
            callback.onResult(emptyList(), 0, 0)
            return
        }
        // bound the size requested, based on known count
        val firstLoadPosition = PositionalDataSource.computeInitialLoadPosition(params, totalCount)
        val firstLoadSize = PositionalDataSource.computeInitialLoadSize(params, firstLoadPosition, totalCount)

        val list = loadRange(firstLoadPosition, firstLoadSize)
        if (list != null && list.size == firstLoadSize) {
            callback.onResult(list, firstLoadPosition, totalCount)
        } else {
            // null list, or size doesn't match request - DB modified between count and load
            invalidate()
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<PhotoEntity>) {
        val list = loadRange(params.startPosition, params.loadSize)
        if (list != null) {
            callback.onResult(list)
        } else {
            invalidate()
        }
    }

    private fun loadRange(startPosition: Int, loadCount: Int): List<PhotoEntity>? {
        return ContentResolverCompat.query(contentResolver, CONTENT_URI,
                arrayOf(MediaStore.Images.Media.DATA),
                WHERE_IN_FOLDER, whereInFolderArg,
                " ${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $loadCount OFFSET $startPosition", null
        ).use {
            val list = mutableListOf<PhotoEntity>()
            if (it.moveToFirst()) {
                do {
                    list.add(convertRow(it))
                } while (it.moveToNext());
            }
            list
        }
    }


    private fun convertRow(cursor: Cursor): PhotoEntity {
        /*
         @PrimaryKey
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
            id = ""
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
        }
    }

}
