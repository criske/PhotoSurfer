package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.DataAccessor
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoDAO
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoEntity
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.entities.collectionsLiteStrAdd
import com.crskdev.photosurfer.entities.collectionsLiteStrRemove

/**
 * Created by Cristian Pela on 22.08.2018.
 */
class PhotoDAOFacade(daoManager: DaoManager
) : DataAccessor {

    //add collection photo table support
    private val daoPhotos: PhotoDAO = daoManager.getDao(Contract.TABLE_PHOTOS)
    private val daoLikes: PhotoLikeDAO = daoManager.getDao(Contract.TABLE_LIKE_PHOTOS)
    private val daoUserPhotos: PhotoUserDAO = daoManager.getDao(Contract.TABLE_USER_PHOTOS)
    private val daoSearchDAO: PhotoSearchDAO = daoManager.getDao(Contract.TABLE_SEARCH_PHOTOS)
    private val daoCollectionPhoto: CollectionPhotoDAO = daoManager.getDao(Contract.TABLE_COLLECTION_PHOTOS)
    private val transactional: TransactionRunner = daoManager.transactionRunner()

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

    fun insertPhotos(table: String, photos: List<PhotoEntity>, clearFirst: Boolean = false) {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.insertPhotos(photos.map { it as UserPhotoEntity })
            Contract.TABLE_PHOTOS -> daoPhotos.insertPhotos(photos)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.insertPhotos(photos.map { it as LikePhotoEntity })
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.insertPhotos(photos.map { it as SearchPhotoEntity })
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.insertPhotos(photos.map { it as CollectionPhotoEntity })
            else -> throw Error("Dao for table $table not found or not supported")
        }
    }

    fun updateForAllTables(photo: PhotoEntity) {
        transactional {
            try {
                Contract.TABLES.forEach {
                    update(it, photo)
                }
            } catch (ex: Exception) {
                //no-op
            }
        }
    }

    fun update(table: String, photo: PhotoEntity) {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.update(photo)
            Contract.TABLE_PHOTOS -> daoPhotos.update(photo)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.update(photo)
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.update(photo)
            Contract.TABLE_COLLECTION_PHOTOS -> daoCollectionPhoto.update(photo)
            else -> throw Error("Dao for table $table not found")
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
            else -> throw Error("Dao for table $table not found")
        }
    }

    fun getPhotoFromAllTables(id: String): List<PhotoEntity> {
        val list = mutableListOf<PhotoEntity>()
        transactional {
            try {
                Contract.TABLES.forEach {
                    getPhoto(it, id)?.let { e -> list.add(e) }
                }
            } catch (ex: Exception) {
                //no-op
            }
        }
        return list
    }

    fun like(id: String, liked: Boolean) {
        transactional {
            val photos = getPhotoFromAllTables(id)
            photos.forEach {
                it.likedByMe = liked
                updateForAllTables(it)
            }
            if (!daoLikes.isEmpty() && photos.isNotEmpty()) { // we doing nothing unless there already fetched the likes from server
                val likePhoto = photos.first().let {
                    LikePhotoEntity().apply {
                        this.id = it.id
                        this.createdAt = it.createdAt
                        this.updatedAt = it.updatedAt
                        this.width = it.width
                        this.height = it.height
                        this.colorString = it.colorString
                        this.urls = it.urls
                        this.likedByMe = it.likedByMe
                        this.authorUsername = it.authorUsername
                        this.authorId = it.authorId
                    }
                }
                likePhoto.let {
                    if (liked) {
                        with(it) {
                            val lastLiked = daoLikes.getLastPhoto()
                            indexInResponse = lastLiked.indexInResponse + 1
                            curr = lastLiked.curr
                            next = lastLiked.next
                            prev = lastLiked.prev
                            total = lastLiked.total
                        }
                        daoLikes.like(it)
                    }else{
                        daoLikes.unlike(it)
                    }

                }
            }
        }
    }


    fun addCollection(id: String, collectionStr: String){
        transactional{
            val photos = getPhotoFromAllTables(id)
            photos.forEach {
                it.collections = it.collections?.let { c -> collectionsLiteStrAdd(c, collectionStr) }
                updateForAllTables(it)
            }
        }
    }

    fun removePhotoFromCollection(id: String, collectionStr: String){
        transactional{
            val photos = getPhotoFromAllTables(id)
            photos.forEach {
                it.collections = it.collections?.let { c -> collectionsLiteStrRemove(c, collectionStr) }
                updateForAllTables(it)
            }
        }
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