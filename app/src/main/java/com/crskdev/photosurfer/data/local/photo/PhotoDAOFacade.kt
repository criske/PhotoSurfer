@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.crskdev.photosurfer.data.local.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.*
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoEntity
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.entities.CollectionLite
import com.crskdev.photosurfer.entities.UNSPLASH_DATE_FORMATTER
import com.crskdev.photosurfer.entities.formatNow
import com.crskdev.photosurfer.util.livedata.distinctUntilChanged
import com.crskdev.photosurfer.util.livedata.filter

/**
 * Created by Cristian Pela on 22.08.2018.
 */
class PhotoDAOFacade(daoManager: DaoManager) : DataAccessor {

    private val daoPhotos: PhotoDAO                     by lazy { daoManager.getDao<PhotoDAO>(Contract.TABLE_PHOTOS) }
    private val daoLikes: PhotoLikeDAO                  by lazy { daoManager.getDao<PhotoLikeDAO>(Contract.TABLE_LIKE_PHOTOS) }
    private val daoUserPhotos: PhotoUserDAO             by lazy { daoManager.getDao<PhotoUserDAO>(Contract.TABLE_USER_PHOTOS) }
    private val daoSearchDAO: PhotoSearchDAO            by lazy { daoManager.getDao<PhotoSearchDAO>(Contract.TABLE_SEARCH_PHOTOS) }
    private val daoCollectionPhoto: CollectionPhotoDAO  by lazy { daoManager.getDao<CollectionPhotoDAO>(Contract.TABLE_COLLECTION_PHOTOS) }

    private val transactional: TransactionRunner by lazy { daoManager.transactionRunner() }

    fun getPhotos(table: String): DataSource.Factory<Int, out PhotoEntity> {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getPhotos()
            Contract.TABLE_PHOTOS -> daoPhotos.getPhotos()
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.getPhotos()
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getPhotos()
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.getPhotos()
            else -> throw Error("Dao for table $table not found or not supported")
        }
    }

    fun insertPhotos(table: String, photos: List<PhotoEntity>) {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.insertPhotos(photos.map { it as UserPhotoEntity })
            Contract.TABLE_PHOTOS -> daoPhotos.insertPhotos(photos)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.insertPhotos(photos.map { it as LikePhotoEntity })
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.insertPhotos(photos.map { it as SearchPhotoEntity })
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.insertPhotos(photos.map { it as CollectionPhotoEntity })
            else -> throw Error("Dao for table $table not found or not supported")
        }
    }

    fun update(table: String, photo: PhotoEntity): Int {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.update(photo as UserPhotoEntity)
            Contract.TABLE_PHOTOS -> daoPhotos.update(photo)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.update(photo as LikePhotoEntity)
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.update(photo as SearchPhotoEntity)
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.update(photo as CollectionPhotoEntity)
            else -> 0
        }
    }

    fun clear(table: String? = null) {
        if (table == null) {
            transactional {
                daoUserPhotos.clear()
                daoPhotos.clear()
                daoLikes.clear()
                daoSearchDAO.clear()
                daoCollectionPhoto.clear()
            }
        } else {
            when (table) {
                Contract.TABLE_USER_PHOTOS -> daoUserPhotos.clear()
                Contract.TABLE_PHOTOS -> daoPhotos.clear()
                Contract.TABLE_LIKE_PHOTOS -> daoLikes.clear()
                Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.clear()
                Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.clear()
                else -> throw Error("Dao for table $table not found")
            }
        }
    }

    fun getNextIndex(table: String): Int {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getNextIndex()
            Contract.TABLE_PHOTOS -> daoPhotos.getNextIndex()
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.getNextIndex()
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getNextIndex()
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.getNextIndex()
            else -> throw Error("Dao for table $table not found")
        }
    }

    fun isEmpty(table: String): Boolean {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.isEmpty()
            Contract.TABLE_PHOTOS -> daoPhotos.isEmpty()
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.isEmpty()
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.isEmpty()
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.isEmpty()
            else -> throw Error("Dao for table $table not found")
        }
    }

    fun getPhoto(table: String, id: String): PhotoEntity? {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getPhoto(id)
            Contract.TABLE_PHOTOS -> daoPhotos.getPhoto(id)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.getPhoto(id)
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getPhoto(id)
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.getPhoto(id)
            else -> throw Exception("Dao for table $table not found")
        }
    }

    fun getPhotoLiveData(table: String, id: String): LiveData<out PhotoEntity?> {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getPhotoLiveData(id)
            Contract.TABLE_PHOTOS -> daoPhotos.getPhotoLiveData(id)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.getPhotoLiveData(id)
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getPhotoLiveData(id)
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.getPhotoLiveData(id)
            else -> throw Exception("Dao for table $table not found")
        }
    }

    fun getLastPhoto(table: String): PhotoEntity? =
            when (table) {
                Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getLastPhoto()
                Contract.TABLE_PHOTOS -> daoPhotos.getLastPhoto()
                Contract.TABLE_LIKE_PHOTOS -> daoLikes.getLastPhoto()
                Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getLastPhoto()
                Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.getLastPhoto()
                else -> throw Exception("Dao for table $table not found")
            }

    fun getPhotoFromEitherTable(id: String): PhotoEntity? {
        Contract.PHOTO_TABLES.forEach {
            val photo = getPhoto(it, id)
            if (photo != null)
                return photo
        }
        return null
    }

    fun getPhotoFromEitherTableLiveData(id: String): LiveData<PhotoEntity> {
        val mediatorLiveData = MediatorLiveData<PhotoEntity>().apply {
            Contract.PHOTO_TABLES.forEach { table ->
                addSource(getPhotoLiveData(table, id).filter { it != null }) { pe ->
                    value = pe
                }
            }
        }
        return mediatorLiveData.distinctUntilChanged { last, curr ->
            //TODO  you must create a "local" last update field for entities, and use that for comp?
            last.id != curr.id
                    || (curr.id == last.id && last.likedByMe != curr.likedByMe)
                    || (curr.id == last.id && last.collections != curr.collections)
                    || (curr.id == last.id && last.likes != curr.likes)
        }
    }

    fun getPhotoMappedByTable(id: String): Map<String, PhotoEntity> {
        val map = mutableMapOf<String, PhotoEntity>()
        transactional {
            Contract.PHOTO_TABLES.forEach {
                val photo = getPhoto(it, id)
                if (photo != null)
                    map[it] = photo
            }
        }
        return map
    }

    fun getPhotosBelongToCollectionMappedByTable(collectionId: Int): Map<String, List<PhotoEntity>> {
        val likeCollectionId = collectionId.asSearchTermInRecord()
        lateinit var map: Map<String, List<PhotoEntity>>
        transactional {
            map = Contract.PHOTO_TABLES.map {
                when (it) {
                    Contract.TABLE_USER_PHOTOS -> it to daoUserPhotos.getPhotosBelongToCollection(likeCollectionId)
                    Contract.TABLE_PHOTOS -> it to daoPhotos.getPhotosBelongToCollection(likeCollectionId)
                    Contract.TABLE_LIKE_PHOTOS -> it to daoLikes.getPhotosBelongToCollection(likeCollectionId)
                    Contract.TABLE_SEARCH_PHOTOS -> it to daoSearchDAO.getPhotosBelongToCollection(likeCollectionId)
                    Contract.TABLE_COLLECTION_PHOTOS -> it to daoCollectionPhoto.getPhotosBelongToCollection(likeCollectionId)
                    else -> throw Exception("Dao for table $it not found")
                }
            }.filter {
                it.second.isNotEmpty()
            }.fold(mutableMapOf()) { acc, curr ->
                acc.apply {
                    this[curr.first] = curr.second
                }
            }
        }
        return map
    }

    fun like(id: String, liked: Boolean) {
        transactional {
            //(un)like the photo in each photo-table
            val photos = getPhotoMappedByTable(id)
            photos.forEach {
                val photo = it.value.apply {
                    likedByMe = liked
                    likes = if (liked) (likes + 1) else likes - 1
                }
                val table = it.key
                update(table, photo)
            }
            if (!daoLikes.isEmpty()) {
                val pickedPhoto = photos.entries.first().value
                val lastLiked = daoLikes.getLastPhoto()
                pickedPhoto.let {
                    val likePhoto = LikePhotoEntity().apply {
                        this.id = it.id
                        this.createdAt = it.createdAt
                        this.updatedAt = UNSPLASH_DATE_FORMATTER.formatNow()
                        this.width = it.width
                        this.height = it.height
                        this.colorString = it.colorString
                        this.urls = it.urls
                        this.likedByMe = it.likedByMe
                        this.authorUsername = it.authorUsername
                        this.authorId = it.authorId
                        this.authorFullName = it.authorFullName
                        this.collections = it.collections
                        this.categories = it.categories
                        this.indexInResponse = lastLiked?.indexInResponse ?: 0 + 1
                        this.pagingData = PagingData.createNextPagingData(lastLiked?.pagingData)
                    }
                    if (liked) {
                        daoLikes.like(likePhoto)
                    } else {
                        daoLikes.unlike(likePhoto)
                    }

                }
            }
        }
    }


    fun addPhotoToCollection(id: String, collection: CollectionLite, providedPhoto: PhotoEntity? = null): Int {
        var updated = 0
        transactional {
            val photosByTable = getPhotoMappedByTable(id)
            photosByTable.forEach {
                val photo = it.value
                photo.collections = photo.collections + collection
                val table = it.key
                updated += update(table, photo)
            }
            //add photo if current photo collection table has the collection.id
            val lastCollectionPhoto = daoCollectionPhoto.getLastPhoto()
            if (lastCollectionPhoto != null && lastCollectionPhoto.currentCollectionId == collection.id) {
                val pickedPhoto = if (providedPhoto != null) providedPhoto else
                    photosByTable.entries.first().value
                pickedPhoto.let {
                    val collectionPhoto = CollectionPhotoEntity().apply {
                        this.id = it.id
                        this.createdAt = it.createdAt
                        this.updatedAt = it.updatedAt
                        this.width = it.width
                        this.height = it.height
                        this.colorString = it.colorString
                        this.urls = it.urls
                        this.likedByMe = it.likedByMe
                        this.authorUsername = it.authorUsername
                        this.authorFullName = it.authorFullName
                        this.authorId = it.authorId
                        this.indexInResponse = lastCollectionPhoto.indexInResponse
                        this.pagingData = PagingData.createNextPagingData(lastCollectionPhoto.pagingData)
                        this.collections = it.collections
                        this.currentCollectionId = collection.id
                    }
                    daoCollectionPhoto.insertPhotos(listOf(collectionPhoto))
                }
            }
        }
        return updated
    }

    fun removePhotoFromCollection(id: String, collection: CollectionLite): Int {
        var updated = 0
        transactional {
            val photosByTable = getPhotoMappedByTable(id)
            photosByTable.forEach {
                val photo = it.value
                photo.collections = photo.collections - collection
                val table = it.key
                updated += update(table, photo)
            }
            daoCollectionPhoto.delete(id)

        }
        return updated
    }


    fun refresh(table: String) {
        //TODO implement refresh
//        transactional {
//            if (username == null) {
//                if (daoPhotos.isEmpty()) {
//                    //force trigger the db InvalidationTracker.Observer
//                    daoPhotos.insertPhotos(listOf(PhotoRepositoryImpl.EMPTY_PHOTO_ENTITY))
//                    daoPhotos.clear()
//                } else {
//                    daoPhotos.clear()
//                }
//            } else {
//                if (daoUserPhotos.isEmpty()) {
//                    //force trigger the db InvalidationTracker.Observer
//                    daoUserPhotos.insertPhotos(listOf(PhotoRepositoryImpl.emptyUserPhotoEntity(username)))
//                    daoUserPhotos.clear()
//                } else {
//                    daoUserPhotos.clear()
//                }
//            }
//
//        }
    }


}

