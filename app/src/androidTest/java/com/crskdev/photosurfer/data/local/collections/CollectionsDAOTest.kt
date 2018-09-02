package com.crskdev.photosurfer.data.local.collections

import android.support.test.runner.AndroidJUnit4
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.local.BaseDBTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Cristian Pela on 02.09.2018.
 */
@RunWith(value = AndroidJUnit4::class)
class CollectionsDAOTest : BaseDBTest() {

    private lateinit var dao: CollectionsDAO


    override fun onBefore() {
        super.onBefore()
        dao = db.collectionsDAO()
    }

    @Test
    fun testInsertingPhotosCollectionAndGettingBack() {
        val collection1 = CollectionEntity().apply {
            id = 1
            title = ""
            description = ""
            publishedAt = ""
            updatedAt = ""
            sharedKey = ""
            ownerId = ""
            ownerUsername = ""
            links = ""
        }
        val collection2 = CollectionEntity().apply {
            id = 2
            title = ""
            description = ""
            publishedAt = ""
            updatedAt = ""
            sharedKey = ""
            ownerId = ""
            ownerUsername = ""
            links = ""
        }
        val photo1 = CollectionPhotoEntity().apply {
            id = "1"
            createdAt = ""
            updatedAt = ""
            colorString = ""
            urls = ""
            authorId = ""
            authorUsername = ""
        }
        val photo2 = CollectionPhotoEntity().apply {
            id = "2"
            createdAt = ""
            updatedAt = ""
            colorString = ""
            urls = ""
            authorId = ""
            authorUsername = ""
        }
        db.runInTransaction {
            dao.insertCollections(listOf(collection1, collection2))
            dao.insertCollectionPhotos(listOf(photo1, photo2))
            //add photo 1 2 to collection 1
            dao.addPhotoToCollection(CollectionsCollectionPhotoEntity().apply {
                collectionId = collection1.id
                photoId = photo1.id
            })
            dao.addPhotoToCollection(CollectionsCollectionPhotoEntity().apply {
                collectionId = collection1.id
                photoId = photo2.id
            })
            //add photo 2 to collection 2
            dao.addPhotoToCollection(CollectionsCollectionPhotoEntity().apply {
                collectionId = collection2.id
                photoId = photo2.id
            })
            dao.updateCollection(collection1.apply { totalPhotos = 2 })
            dao.updateCollection(collection2.apply { totalPhotos = 1 })
        }

        assertEquals(2, dao.getCollection(collection1.id)?.totalPhotos)

        var photosIdsInCollection1 = photos(dao.getCollectionPhotos(1).create())
                .sortAndMapById()
        assertEquals(listOf("1", "2"), photosIdsInCollection1)

        val photosIdsInCollection2 = photos(dao.getCollectionPhotos(2).create())
                .sortAndMapById()
        assertEquals(listOf("2"), photosIdsInCollection2)


        db.runInTransaction {
            val removed = dao.removePhotoFromCollection(collection1.id, photo1.id)
            assertTrue(removed == 1)
            dao.updateCollection(collection1.apply { totalPhotos = 1 })
        }

        assertEquals(1, dao.getCollection(collection1.id)?.totalPhotos)

        photosIdsInCollection1 = photos(dao.getCollectionPhotos(1).create())
                .sortAndMapById()
        assertEquals(listOf("2"), photosIdsInCollection1)
    }

    private fun photos(dataSource: DataSource<Int, CollectionPhotoEntity>): List<CollectionPhotoEntity> {
        return PagedList.Builder<Int, CollectionPhotoEntity>(dataSource, 5)
                .setNotifyExecutor(emptyExecutor)
                .setFetchExecutor(emptyExecutor)
                .build()
                .toList()
    }

    private fun List<CollectionPhotoEntity>.sortAndMapById() = this.sortedBy { it.id }.map { it.id }


}