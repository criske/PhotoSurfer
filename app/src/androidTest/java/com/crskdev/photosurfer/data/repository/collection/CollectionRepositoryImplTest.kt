package com.crskdev.photosurfer.data.repository.collection

import androidx.test.runner.AndroidJUnit4
import androidx.room.Room
import com.crskdev.photosurfer.data.local.*
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.collections.CollectionJSON
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.services.NetworkCheckService
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.executors.ThreadCallChecker
import com.crskdev.photosurfer.services.executors.ExecutorType
import com.crskdev.photosurfer.services.messaging.DevicePushMessagingManager
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.schedule.*
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Cristian Pela on 14.09.2018.
 */
@RunWith(value = AndroidJUnit4::class)
class CollectionRepositoryImplTest : BaseDBTest() {

    companion object {
        val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }

    private val dateFormatter = SimpleDateFormat(DATE_FORMAT)

    private lateinit var daoManager: DaoManager

    private lateinit var collectionRepository: CollectionRepository


    override fun onBefore() {
        super.onBefore()
        db = Room.inMemoryDatabaseBuilder(ctx, PhotoSurferDB::class.java)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        val transactionRunner = TransactionRunnerImpl(db)
        daoManager = DaoManager(DatabaseOpsImpl(db, transactionRunner), mapOf(
                Contract.TABLE_PHOTOS to db.photoDAO(),
                Contract.TABLE_LIKE_PHOTOS to db.photoLikeDAO(),
                Contract.TABLE_USER_PHOTOS to db.photoUserDAO(),
                Contract.TABLE_SEARCH_PHOTOS to db.photoSearchDAO(),
                Contract.TABLE_USERS to db.userDAO(),
                Contract.TABLE_COLLECTIONS to db.collectionsDAO(),
                Contract.TABLE_COLLECTION_PHOTOS to db.collectionPhotoDAO()
        ))
        val scheduledWorkService = object : ScheduledWorkManager(EnumMap<WorkType, ScheduledWork>(WorkType::class.java)) {
            override fun cancel(tag: Tag?) = Unit
        }
        val threadCallChecker = object : ThreadCallChecker() {
            override fun isOnMainThread(): Boolean = true
        }
        val apiCallDispatcher = APICallDispatcher(threadCallChecker)
        val collectionsAPI = object : CollectionsAPI {

            override fun getCollection(collectionId: Int): Call<CollectionJSON> = MockCall()

            override fun getCollections(username: String, page: Int): Call<List<CollectionJSON>> = MockCall()

            override fun getMyCollections(username: String, page: Int): Call<List<CollectionJSON>> = MockCall()

            override fun getMyCollectionPhotos(collectionId: Int, page: Int): Call<List<PhotoJSON>> = MockCall()

            override fun createCollection(title: String, description: String, private: Boolean): Call<CollectionJSON> =
                    MockCall()

            override fun updateCollection(id: Int, title: String, description: String, private: Boolean): Call<CollectionJSON> = MockCall()

            override fun deleteCollection(id: Int): Call<ResponseBody> = MockCall()

            override fun addPhotoToCollection(collectionIdPath: Int, collectionId: Int, photoId: String): Call<PhotoJSON> = MockCall()

            override fun removePhotoFromCollection(collectionIdPath: Int, collectionId: Int, photoId: String): Call<PhotoJSON> = MockCall()
        }
        val authTokenStorage: AuthTokenStorage = object : AuthTokenStorage {
            override fun token(): AuthToken? = null

            override fun saveToken(token: AuthToken) = Unit

        }
        collectionRepository = CollectionRepositoryImpl(
                ExecutorsManager(
                        EnumMap<ExecutorType, KExecutor>(ExecutorType::class.java)
                                .apply {
                                    put(ExecutorType.DISK, emptyExecutor)
                                    put(ExecutorType.NETWORK, emptyExecutor)
                                    put(ExecutorType.UI, emptyExecutor)
                                }),
                daoManager,
                PhotoDAOFacade(daoManager),
                scheduledWorkService,
                apiCallDispatcher,
                collectionsAPI,
                authTokenStorage,
                object : DevicePushMessagingManager {
                    override fun onReceiveMessage(data: Map<String, String>) = Unit

                    override fun onRegister(token: String) = Unit

                    override fun sendMessage(message: Message) = Unit

                })
    }

    @Test
    fun addPhotoToCollection() {
        //TODO this test might be useless since PhotoEntity uses CollectionLite List for collections
//        //create collection
//        val collection1 = CollectionEntity().apply {
//            id = 1
//            indexInResponse = 0
//            publishedAt = dateFormatter.format(System.currentTimeMillis())
//            updatedAt = dateFormatter.format(System.currentTimeMillis())
//            title = "Foo"
//            description = "FooDesc"
//            sharedKey = ""
//            ownerId = "FooId"
//            ownerUsername = "FooUser"
//            links = ""
//            total = 1
//            curr = 1
//            notPublic = true
//        }
//        val collection2 = CollectionEntity().apply {
//            id = 2
//            indexInResponse = 0
//            publishedAt = dateFormatter.format(System.currentTimeMillis())
//            updatedAt = dateFormatter.format(System.currentTimeMillis())
//            title = "Foo"
//            description = "FooDesc"
//            sharedKey = ""
//            ownerId = "FooId"
//            ownerUsername = "FooUser"
//            links = ""
//            total = 1
//            curr = 1
//        }
//        val collectionsDAO = db.collectionsDAO()
//        val collectionsPhotoDAO = db.collectionPhotoDAO()
//        collectionsDAO.createCollection(collection1)
//
//        with(collectionsDAO.getCollection(1)){
//            assertEquals(collection1.id, this?.id)
//            assertEquals(collection1.title, this?.title)
//            assertEquals(collection1.description, this?.description)
//            assertEquals(collection1.notPublic, this?.notPublic == true)
//        }
//        collectionsDAO.createCollection(collection2)
//
//        val photo = Photo(
//                "1",
//                dateFormatter.format(System.currentTimeMillis()),
//                dateFormatter.format(System.currentTimeMillis()),
//                0, 0, "",
//                EnumMap<ImageType, String>(ImageType::class.java).apply {
//                    ImageType.REGULAR to ImageType.REGULAR.toString() + "1"
//                },
//                null,
//                emptyList(),
//                emptyList(),
//                0,
//                false,
//                0,
//                "FooId",
//                "FooUser")
//
//        //insert empty photo to make sure the table is not empty see addPhotoToCollection dao is not empty check
//        collectionsPhotoDAO.insertPhotos(listOf(CollectionPhotoEntity().apply {
//            id = ""
//            createdAt = ""
//            updatedAt = ""
//            colorString = ""
//            urls = ""
//            authorId = ""
//            authorUsername = ""
//            currentCollectionId = 1
//        }))
//        //also make sure the photo exists in at least one other photo table
//        db.photoDAO().insertPhotos(listOf(photo.toLikePhotoDbEntity(1)))
//
//        collectionRepository.addPhotoToCollection(collection1.toCollection(), photo)
//        with(collectionsPhotoDAO.getPhoto("1")!!) {
//            assertEquals("1", id)
//            assertEquals(1, currentCollectionId)
//            assertTrue(collections?.contains(collection1.asLiteStr()) == true)
//        }
//        collectionRepository.addPhotoToCollection(collection2.toCollection(), photo)
//        with(collectionsPhotoDAO.getPhoto("1")!!) {
//            assertEquals("1", id)
//            assertEquals(2, currentCollectionId)
//            assertTrue(collections?.contains(collection2.asLiteStr()) == true)
//        }
//
//        with(db.photoDAO().getPhoto("1")!!) {
//            assertTrue(collections?.contains(collection1.asLiteStr()) == true)
//            assertTrue(collections?.contains(collection2.asLiteStr()) == true)
//        }
//
//        //remove from collection
//        collectionRepository.removePhotoFromCollection(collection1.toCollection(), photo)
//
//        assertNull(collectionsPhotoDAO.getPhoto("1"))
//
//        with(db.photoDAO().getPhoto("1")!!) {
//            assertTrue(collections?.contains(collection1.asLiteStr()) == false)
//        }
    }


}


class MockCall<T> : Call<T> {

    override fun enqueue(callback: Callback<T>) {}

    override fun isExecuted(): Boolean = true

    override fun clone(): Call<T> = this

    override fun isCanceled(): Boolean = true

    override fun cancel() = Unit

    override fun execute(): Response<T> = Response.success(null)

    override fun request(): Request = Request.Builder().build()

}