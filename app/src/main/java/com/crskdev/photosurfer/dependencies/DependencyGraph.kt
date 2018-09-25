package com.crskdev.photosurfer.dependencies

import android.content.Context
import android.os.Environment
import com.crskdev.photosurfer.BuildConfig
import com.crskdev.photosurfer.data.local.*
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepositoryImpl
import com.crskdev.photosurfer.data.remote.NetworkClient
import com.crskdev.photosurfer.data.remote.RetrofitClient
import com.crskdev.photosurfer.data.remote.download.*
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.photo.external.*
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.SearchTermTrackerImpl
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.auth.*
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.create
import com.crskdev.photosurfer.data.remote.user.UserAPI
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.data.repository.collection.CollectionRepositoryImpl
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.data.repository.user.UserRepositoryImpl
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.services.ScheduledWorkServiceImpl
import com.crskdev.photosurfer.services.NetworkCheckService
import com.crskdev.photosurfer.services.NetworkCheckServiceImpl
import com.crskdev.photosurfer.services.ScheduledWorkService
import com.crskdev.photosurfer.services.executors.*
import com.crskdev.photosurfer.services.messaging.PhotoSurferMessageManagerImpl
import com.crskdev.photosurfer.services.messaging.PhotoSurferMessagingManager
import com.crskdev.photosurfer.services.messaging.remote.FCMTokeProviderImpl
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.services.messaging.remote.messagingRetrofit
import com.crskdev.photosurfer.util.Listenable
import retrofit2.Retrofit
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.Moshi
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Cristian Pela on 09.08.2018.
 */
object DependencyGraph {

    internal var isInit: AtomicBoolean = AtomicBoolean(false)

    //EXECUTORS
    val threadCallChecker: ThreadCallChecker = AndroidThreadCallChecker()
    val uiThreadExecutor: KExecutor = UIThreadExecutor(threadCallChecker)
    val diskThreadExecutor: KExecutor = DiskThreadExecutor()
    val ioThreadExecutor: KExecutor = IOThreadExecutor()
    val executorManager: ExecutorsManager = ExecutorsManager(
            EnumMap<ExecutorsManager.Type, KExecutor>(ExecutorsManager.Type::class.java)
                    .apply {
                        put(ExecutorsManager.Type.DISK, diskThreadExecutor)
                        put(ExecutorsManager.Type.NETWORK, ioThreadExecutor)
                        put(ExecutorsManager.Type.UI, uiThreadExecutor)
                    })

    //DB
    lateinit var db: PhotoSurferDB
        private set
    lateinit var staleDataTrackSupervisor: StaleDataTrackSupervisor
        private set
    lateinit var daoManager: DaoManager
        private set
    lateinit var externalDb: ExternalPhotoGalleryDB
        private set
    lateinit var externalPhotoGalleryDAO: ExternalPhotoGalleryDAO
        private set
    lateinit var externalPhotosDirectory: ExternalDirectory

    //serialization
    //todo unify this moshi with the one from retrofit converter
    val moshi: Moshi = Moshi.Builder().build()

    //NETWORK
    val apiCallDispatcher: APICallDispatcher = APICallDispatcher(threadCallChecker)

    lateinit var networkCheckService: NetworkCheckService
        private set
    lateinit var authTokenStorage: AuthTokenStorage
        private set
    lateinit var listenableAuthState: Listenable<AuthToken>
        private set
    lateinit var retrofit: Retrofit
        private set
    lateinit var progressListenerRegistrar: ProgressListenerRegistrar
        private set
    lateinit var downloadManager: DownloadManager
        private set
    lateinit var photoDownloader: PhotoDownloader
        private set
    lateinit var scheduledWorkService: ScheduledWorkService
        private set

    //MESSAGING
    lateinit var photoSurferMessagingManager: PhotoSurferMessagingManager
        private set

    //APIs
    lateinit var photoAPI: PhotoAPI
        private set
    lateinit var collectionsAPI: CollectionsAPI
        private set
    lateinit var messagingAPI: MessagingAPI
        private set

    //repositories
    lateinit var photoRepository: PhotoRepository
        private set
    lateinit var userRepository: UserRepository
        private set
    lateinit var collectionsRepository: CollectionRepository
        private set
    lateinit var searchTermTracker: SearchTermTracker
        private set
    lateinit var photoDAOFacade: PhotoDAOFacade
        private set

    //nav
    lateinit var authNavigatorMiddleware: AuthNavigatorMiddleware
        private set


    fun init(context: Context) {
        if (isInit.get()) return

        val preferences = context.getSharedPreferences("photo_surfer_prefs", Context.MODE_PRIVATE)

        //NETWORK
        scheduledWorkService = ScheduledWorkServiceImpl()
        networkCheckService = NetworkCheckServiceImpl(context)
        authTokenStorage = AuthTokenStorageImpl(preferences).apply {
            listenableAuthState = this
        }
        //authTokenStorage = InMemoryAuthTokenStorage()
        val retrofitClient = RetrofitClient(NetworkClient(
                authTokenStorage,
                APIKeys(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY, BuildConfig.REDIRECT_URI),
                PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        ))
        retrofit = retrofitClient.retrofit
        progressListenerRegistrar = ProgressListenerRegistrarImpl(retrofitClient)

        //db
        db = PhotoSurferDB.create(context, false)
        staleDataTrackSupervisor = StaleDataTrackSupervisor.install(networkCheckService, db)
        daoManager = DaoManager(DatabaseOpsImpl(db, TransactionRunnerImpl(db)),
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
        externalPhotosDirectory = ExternalDirectory(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
        externalDb = ExternalPhotoGalleryDBImpl(context, externalPhotosDirectory)
        externalPhotoGalleryDAO = externalDb.dao()


        //photo
        photoAPI = retrofit.create(PhotoAPI::class.java)
        photoDownloader = PhotoDownloaderImpl(apiCallDispatcher, photoAPI)
//        downloadManager = DownloadManager.MOCK
        downloadManager = DownloadManager(progressListenerRegistrar, photoDownloader, externalPhotoGalleryDAO)
        photoDAOFacade = PhotoDAOFacade(daoManager)
        photoRepository = PhotoRepositoryImpl(
                executorManager,
                photoDAOFacade,
                externalPhotoGalleryDAO,
                authTokenStorage,
                staleDataTrackSupervisor,
                apiCallDispatcher,
                photoAPI,
                downloadManager,
                scheduledWorkService
        )

        collectionsAPI = retrofit.create(CollectionsAPI::class.java)
        collectionsRepository = CollectionRepositoryImpl(
                executorManager,
                daoManager,
                photoDAOFacade,
                scheduledWorkService,
                apiCallDispatcher,
                collectionsAPI,
                authTokenStorage,
                staleDataTrackSupervisor
        )


        //search
        searchTermTracker = SearchTermTrackerImpl(preferences)

        //user and auth
        val userAPI = retrofit.create(UserAPI::class.java)
        val authAPI: AuthAPI = retrofit.create(AuthAPI::class.java)
        userRepository = UserRepositoryImpl(
                executorManager,
                daoManager,
                staleDataTrackSupervisor,
                apiCallDispatcher,
                userAPI,
                authAPI,
                authTokenStorage)

        authNavigatorMiddleware = AuthNavigatorMiddleware(authTokenStorage)

        //messaging
        messagingAPI = messagingRetrofit(false, FCMTokeProviderImpl(FirebaseInstanceId.getInstance()),
                authTokenStorage).create()
        photoSurferMessagingManager = PhotoSurferMessageManagerImpl(context, messagingAPI,
                authTokenStorage as ObservableAuthTokenStorage)

        isInit.compareAndSet(false, true)
    }

}

fun Context.injectDependencyGraph() = DependencyGraph.init(this)

fun Context.dependencyGraph(): DependencyGraph = DependencyGraph