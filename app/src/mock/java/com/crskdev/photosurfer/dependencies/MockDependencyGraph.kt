package com.crskdev.photosurfer.dependencies

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.DatabaseOps
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.local.photo.external.ExternalDirectory
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDAO
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDB
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.Term
import com.crskdev.photosurfer.data.local.track.IStaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.collections.CollectionJSON
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.download.*
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.photo.SearchedPhotosJSON
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.RepositoryAction
import com.crskdev.photosurfer.data.repository.playwave.MockPlaywaveRepository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.entities.Collection
import com.crskdev.photosurfer.entities.PairBE
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.services.NetworkCheckService
import com.crskdev.photosurfer.services.executors.ExecutorType
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.executors.ThreadCallChecker
import com.crskdev.photosurfer.services.messaging.DevicePushMessagingManager
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.messaging.remote.FCMMessage
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.services.messaging.remote.UserDevices
import com.crskdev.photosurfer.presentation.playwave.TrackingPlaywaveSoundPlayer
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayerImpl
import com.crskdev.photosurfer.services.schedule.*
import com.crskdev.photosurfer.util.Listenable
import okhttp3.ResponseBody
import okio.Source
import retrofit2.Call
import java.io.File
import java.util.*

/**
 * Created by Cristian Pela on 16.10.2018.
 */
class MockDependencyGraph(context: Context) : ProdDependencyGraph(context) {

    override val playwaveRepository: PlaywaveRepository = MockPlaywaveRepository()

    override val playwaveSoundPlayer: PlaywaveSoundPlayer = TrackingPlaywaveSoundPlayer()

}

class RealMockDependencyGraph : VariantDependencyGraph {


    private val noExecutor = object : KExecutor {
        override val name: String = "No executor"

        override fun execute(command: Runnable?) {
            command?.run()
        }
    }

    override val playwaveRepository: PlaywaveRepository = MockPlaywaveRepository()

    override val playwaveSoundPlayer: PlaywaveSoundPlayer = PlaywaveSoundPlayerImpl()

    override val threadCallChecker: ThreadCallChecker = ThreadCallChecker.SUPPRESED_CHECK

    override val uiThreadExecutor: KExecutor = noExecutor

    override val diskThreadExecutor: KExecutor = noExecutor

    override val ioThreadExecutor: KExecutor = noExecutor

    override val executorManager: ExecutorsManager = ExecutorsManager(EnumMap<ExecutorType, KExecutor>(ExecutorType::class.java).apply {
        put(ExecutorType.DISK, diskThreadExecutor)
        put(ExecutorType.NETWORK, ioThreadExecutor)
        put(ExecutorType.UI, uiThreadExecutor)
    })

    override val networkCheckService: NetworkCheckService = object : NetworkCheckService {}
    override val staleDataTrackSupervisor: IStaleDataTrackSupervisor = object : IStaleDataTrackSupervisor {
        override fun runStaleDataCheck() {
        }

        override fun runStaleDataCheckForTable(table: String, ignoreNetwork: Boolean) {

        }
    }

    override val externalDb: ExternalPhotoGalleryDB = object : ExternalPhotoGalleryDB {
        override fun dao(): ExternalPhotoGalleryDAO {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val externalPhotoGalleryDAO: ExternalPhotoGalleryDAO = object : ExternalPhotoGalleryDAO {
        override fun save(photo: Photo, source: Source) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isDownloaded(id: String): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getPhotos(): DataSource.Factory<Int, PhotoEntity> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun delete(path: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val externalPhotosDirectory: ExternalDirectory = ExternalDirectory(File(""))

    override val daoManager: DaoManager = DaoManager(object : DatabaseOps {
        override fun clearAll() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun transactionRunner(): TransactionRunner {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }, emptyMap())
    override val photoDAOFacade: PhotoDAOFacade = PhotoDAOFacade(daoManager)


    override val apiCallDispatcher: APICallDispatcher = APICallDispatcher(threadCallChecker)

    override val authTokenStorage: AuthTokenStorage = object : AuthTokenStorage {
        override fun token(): AuthToken? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun saveToken(token: AuthToken) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val listenableAuthState: Listenable<AuthToken> = Listenable<AuthToken>()
    override val progressListenerRegistrar: ProgressListenerRegistrar = object : ProgressListenerRegistrar {
        override var progressListener: ProgressListener?
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
            set(value) {}

    }

    override val downloadManager: DownloadManager = DownloadManager.MOCK
    override val photoDownloader: PhotoDownloader = object : PhotoDownloader {
        override fun data(photo: Photo): Source? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun cancel() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override val devicePushMessagingManager: DevicePushMessagingManager = object : DevicePushMessagingManager {
        override fun onReceiveMessage(data: Map<String, String>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRegister(token: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun sendMessage(message: Message) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val scheduledWorkManager: ScheduledWorkManager = object : ScheduledWorkManager(EnumMap<WorkType, ScheduledWork>(WorkType::class.java)) {
        override fun cancel(tag: Tag?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val workQueueBookKeeper: IWorkQueueBookKeeper = object : IWorkQueueBookKeeper {
        override fun addToQueue(tag: Tag) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun removeFromQueue(tag: Tag) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getAllWithTagLike(pattern: String): List<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getAllWithTagLike(pattern: Regex): List<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val photoAPI: PhotoAPI = object : PhotoAPI {
        override fun getRandom(): Call<PhotoJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getPhoto(photoId: String): Call<PhotoJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getRandomPhotos(page: Int): Call<List<PhotoJSON>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getLikedPhotos(username: String, page: Int): Call<List<PhotoJSON>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getSearchedPhotos(query: String, page: Int): Call<SearchedPhotosJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getUserPhotos(username: String, page: Int): Call<List<PhotoJSON>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun download(id: String): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun like(id: String): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun unlike(id: String): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val collectionsAPI: CollectionsAPI = object : CollectionsAPI {
        override fun getCollections(username: String, page: Int): Call<List<CollectionJSON>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMyCollections(username: String, page: Int): Call<List<CollectionJSON>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMyCollectionPhotos(collectionId: Int, page: Int): Call<List<PhotoJSON>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun createCollection(title: String, description: String, private: Boolean): Call<CollectionJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun updateCollection(id: Int, title: String, description: String, private: Boolean): Call<CollectionJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun deleteCollection(id: Int): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addPhotoToCollection(collectionIdPath: Int, collectionId: Int, photoId: String): Call<PhotoJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun removePhotoFromCollection(collectionIdPath: Int, collectionId: Int, photoId: String): Call<PhotoJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getCollection(collectionId: Int): Call<CollectionJSON> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val messagingAPI: MessagingAPI = object : MessagingAPI {
        override fun registerDevice(username: String): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun unregisterDevice(username: String): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun obtainUserDevices(): Call<UserDevices> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun sendPushMessage(message: FCMMessage): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clear(): Call<ResponseBody> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val photoRepository: PhotoRepository = object : PhotoRepository {
        override fun getPhotos(repositoryAction: RepositoryAction): DataSource.Factory<Int, Photo> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getPhotoLiveData(id: String): LiveData<Photo> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getSavedPhotos(): DataSource.Factory<Int, Photo> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fetchAndSavePhotos(repositoryAction: RepositoryAction, callback: Repository.Callback<Unit>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun refresh(username: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun cancel() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isDownloaded(id: String): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun like(photo: Photo, callback: Repository.Callback<Boolean>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clearAll() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clear(repositoryAction: RepositoryAction) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun delete(photo: Photo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val userRepository: UserRepository = object : UserRepository {
        override fun login(email: String, password: String, callback: Repository.Callback<Unit>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun logout(callback: Repository.Callback<Unit>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun me(callback: Repository.Callback<User>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun meUsername(callback: Repository.Callback<String>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun meLoggedIn(callback: Repository.Callback<Boolean>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getUser(username: String, callback: Repository.Callback<User>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getUsers(): DataSource.Factory<Int, User> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun searchUsers(query: String, page: Int, callback: Repository.Callback<Unit>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun follow(isFollowed: Boolean, callback: Repository.Callback<User>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clear() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val collectionsRepository: CollectionRepository = object : CollectionRepository {
        override fun createCollection(collection: Collection, withPhotoId: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun editCollection(collection: Collection) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun deleteCollection(collectionId: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getCollections(): DataSource.Factory<Int, Collection> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getCollectionPhotos(collectionId: Int): DataSource.Factory<Int, Photo> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getCollectionsForPhoto(photoId: String): DataSource.Factory<Int, PairBE<Collection, Boolean>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fetchAndSaveCollection(callback: Repository.Callback<Unit>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fetchAndSaveCollectionPhotos(collectionId: Int, callback: Repository.Callback<Unit>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addPhotoToCollection(collectionId: Int, photoId: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun removePhotoFromCollection(collectionId: Int, photoId: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getCollectionLiveData(collectionId: Int): LiveData<Collection> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    override val searchTermTracker: SearchTermTracker = object : SearchTermTracker {
        override fun setTerm(term: Term) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getTerm(type: SearchTermTracker.Type): Term? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addListener(listener: SearchTermTracker.ChangeListener) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun removeListener(listener: SearchTermTracker.ChangeListener) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override val authNavigatorMiddleware: AuthNavigatorMiddleware = AuthNavigatorMiddleware(authTokenStorage)

}