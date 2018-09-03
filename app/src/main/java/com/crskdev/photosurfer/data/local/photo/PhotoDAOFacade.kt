package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.DataAccessor
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO

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
    private val daoCollections: CollectionsDAO = daoManager.getDao(Contract.TABLE_COLLECTIONS)
    private val transactional: TransactionRunner = daoManager.transactionRunner()

    fun getPhotos(table: String): DataSource.Factory<Int, out PhotoEntity> {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getPhotos()
            Contract.TABLE_PHOTOS -> daoPhotos.getPhotos()
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.getPhotos()
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getPhotos()
            else -> throw Error("Dao for table $table not found or not supported")
        }
    }

    fun insertPhotos(table: String, photos: List<PhotoEntity>, clearFirst: Boolean = false) {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.insertPhotos(photos.map { it as UserPhotoEntity })
            Contract.TABLE_PHOTOS -> daoPhotos.insertPhotos(photos)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.insertPhotos(photos.map { it as LikePhotoEntity })
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.insertPhotos(photos.map { it as SearchPhotoEntity })
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
            }
        } else {
            when (table) {
                Contract.TABLE_USER_PHOTOS -> daoUserPhotos.clear()
                Contract.TABLE_PHOTOS -> daoPhotos.clear()
                Contract.TABLE_LIKE_PHOTOS -> daoLikes.clear()
                Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.clear()
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
            else -> throw Error("Dao for table $table not found")
        }
    }

    fun isEmpty(table: String): Boolean {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.isEmpty()
            Contract.TABLE_PHOTOS -> daoPhotos.isEmpty()
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.isEmpty()
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.isEmpty()
            else -> throw Error("Dao for table $table not found")
        }
    }

    fun getPhoto(table: String, id: String): PhotoEntity? {
        return when (table) {
            Contract.TABLE_USER_PHOTOS -> daoUserPhotos.getPhoto(id)
            Contract.TABLE_PHOTOS -> daoPhotos.getPhoto(id)
            Contract.TABLE_LIKE_PHOTOS -> daoLikes.getPhoto(id)
            Contract.TABLE_SEARCH_PHOTOS -> daoSearchDAO.getPhoto(id)
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
            val photo = daoPhotos.getPhoto(id)?.apply { likedByMe = liked }
            photo?.let { daoPhotos.like(photo) }
            val userPhoto = daoUserPhotos.getPhoto(id)?.apply { likedByMe = liked }
            userPhoto?.let { daoUserPhotos.like(userPhoto) }
            val searchedPhoto = daoSearchDAO.getPhoto(id)?.apply { likedByMe = liked }
            searchedPhoto?.let { daoSearchDAO.like(it) }
            if (!daoLikes.isEmpty()) { // we doing nothing unless there already fetched the likes from server
                val likePhoto = (photo ?: userPhoto ?: searchedPhoto)?.let {
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
                likePhoto?.let {
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
                    } else {
                        daoLikes.unlike(it)
                    }
                }
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