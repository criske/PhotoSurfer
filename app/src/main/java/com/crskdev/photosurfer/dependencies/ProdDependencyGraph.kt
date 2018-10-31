package com.crskdev.photosurfer.dependencies

import android.content.Context
import android.os.Environment
import com.crskdev.photosurfer.BuildConfig
import com.crskdev.photosurfer.data.local.*
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.photo.external.ExternalDirectory
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDBImpl
import com.crskdev.photosurfer.data.local.playwave.song.SongDAOImpl
import com.crskdev.photosurfer.data.local.search.SearchTermTrackerImpl
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.*
import com.crskdev.photosurfer.data.remote.auth.*
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.PhotoDownloaderImpl
import com.crskdev.photosurfer.data.remote.download.ProgressListenerRegistrarImpl
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.user.UserAPI
import com.crskdev.photosurfer.data.repository.collection.CollectionRepositoryImpl
import com.crskdev.photosurfer.data.repository.photo.PhotoRepositoryImpl
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepositoryImpl
import com.crskdev.photosurfer.data.repository.user.UserRepositoryImpl
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.services.NetworkCheckServiceImpl
import com.crskdev.photosurfer.services.executors.*
import com.crskdev.photosurfer.services.messaging.DevicePushMessageManagerImpl
import com.crskdev.photosurfer.services.messaging.remote.FCMTokeProviderImpl
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.services.messaging.remote.messagingRetrofit
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayerImpl
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayerProvider
import com.crskdev.photosurfer.services.schedule.ScheduledWorkManagerImpl
import com.crskdev.photosurfer.services.schedule.WorkQueueBookKeeper
import com.crskdev.photosurfer.util.Listenable
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

@Suppress("LeakingThis")
open class ProdDependencyGraph(context: Context) : VariantDependencyGraph {

    //THREADS
    override val threadCallChecker = AndroidThreadCallChecker()
    override val uiThreadExecutor = UIThreadExecutor(threadCallChecker)
    override val diskThreadExecutor = DiskThreadExecutor()
    override val ioThreadExecutor = IOThreadExecutor()
    override val executorManager = ExecutorsManager(EnumMap<ExecutorType, KExecutor>(ExecutorType::class.java).apply {
        put(ExecutorType.DISK, diskThreadExecutor)
        put(ExecutorType.NETWORK, ioThreadExecutor)
        put(ExecutorType.UI, uiThreadExecutor)
    })
    override val apiCallDispatcher = APICallDispatcher(threadCallChecker)

    private val preferences = context.getSharedPreferences("photo_surfer_prefs", Context.MODE_PRIVATE)

    //SCHEDULE
    override val workQueueBookKeeper = WorkQueueBookKeeper(context)

    override val scheduledWorkManager = ScheduledWorkManagerImpl.withDefaultSchedulers(workQueueBookKeeper)
    //NETWORK
    override val networkCheckService = NetworkCheckServiceImpl(context)
    override val authTokenStorage = AuthTokenStorageImpl(preferences)
    override val listenableAuthState: Listenable<AuthToken> = authTokenStorage
    //authTokenStorage = InMemoryAuthTokenStorage()

    private val persistentCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
    private val retrofitClient = RetrofitClient(NetworkClient(
            authTokenStorage,
            APIKeys(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY, BuildConfig.REDIRECT_URI),
            persistentCookieJar
    ))
    private val retrofit = retrofitClient.retrofit!!
    override val progressListenerRegistrar = ProgressListenerRegistrarImpl(retrofitClient)

    //messaging
    override val messagingAPI: MessagingAPI = messagingRetrofit(false, FCMTokeProviderImpl(FirebaseInstanceId.getInstance()), authTokenStorage)
            .create()
    override val devicePushMessagingManager = DevicePushMessageManagerImpl(context, messagingAPI,
            ioThreadExecutor,
            diskThreadExecutor,
            authTokenStorage as ObservableAuthTokenStorage)

    //db
    private val db = PhotoSurferDB.create(context, false)
    override val staleDataTrackSupervisor = StaleDataTrackSupervisor.install(networkCheckService, db)
    private val transactionRunner = TransactionRunnerImpl(db)
    override val daoManager = DaoManager(DatabaseOpsImpl(db, transactionRunner),
            mapOf(
                    Contract.TABLE_PHOTOS to db.photoDAO(),
                    Contract.TABLE_LIKE_PHOTOS to db.photoLikeDAO(),
                    Contract.TABLE_USER_PHOTOS to db.photoUserDAO(),
                    Contract.TABLE_SEARCH_PHOTOS to db.photoSearchDAO(),
                    Contract.TABLE_USERS to db.userDAO(),
                    Contract.TABLE_COLLECTIONS to db.collectionsDAO(),
                    Contract.TABLE_COLLECTION_PHOTOS to db.collectionPhotoDAO()
            ))

    //external
    override val externalPhotosDirectory = ExternalDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
    override val externalDb = ExternalPhotoGalleryDBImpl(context, externalPhotosDirectory)
    override val externalPhotoGalleryDAO = externalDb.dao()

    //photo
    override val photoAPI = retrofit.create(PhotoAPI::class.java)
    override val photoDownloader = PhotoDownloaderImpl(apiCallDispatcher, photoAPI)
    //      override val   downloadManager = DownloadManager.MOCK
    override val downloadManager = DownloadManager(progressListenerRegistrar, photoDownloader, externalPhotoGalleryDAO)
    override val photoDAOFacade = PhotoDAOFacade(daoManager)
    override val photoRepository = PhotoRepositoryImpl(
            executorManager,
            photoDAOFacade,
            externalPhotoGalleryDAO,
            apiCallDispatcher,
            photoAPI,
            downloadManager,
            scheduledWorkManager
    )

    override val collectionsAPI = retrofit.create(CollectionsAPI::class.java)
    override val collectionsRepository = CollectionRepositoryImpl(
            executorManager,
            daoManager,
            photoDAOFacade,
            scheduledWorkManager,
            apiCallDispatcher,
            collectionsAPI,
            authTokenStorage,
            devicePushMessagingManager
    )

    //search
    override val searchTermTracker = SearchTermTrackerImpl(preferences)

    override val userRepository = UserRepositoryImpl(
            executorManager,
            daoManager,
            scheduledWorkManager,
            staleDataTrackSupervisor,
            apiCallDispatcher,
            retrofit.create(UserAPI::class.java),
            retrofit.create(AuthAPI::class.java),
            authTokenStorage,
            PersistentSessionClearable(persistentCookieJar))

    override val authNavigatorMiddleware = AuthNavigatorMiddleware(authTokenStorage)

    //playwave
    override val playwaveRepository: PlaywaveRepository =
            PlaywaveRepositoryImpl(executorManager, transactionRunner,
                    photoDAOFacade,
                    SongDAOImpl(context.contentResolver), db.playwaveDAO())
    override val playwaveSoundPlayerProvider: PlaywaveSoundPlayerProvider =
            object : PlaywaveSoundPlayerProvider {
                override fun create(): PlaywaveSoundPlayer = PlaywaveSoundPlayerImpl()
            }

}