package com.crskdev.photosurfer

import android.content.Context
import com.crskdev.photosurfer.data.local.PhotoSurferDB
import com.crskdev.photosurfer.data.local.TransactionRunnerImpl
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepositoryImpl
import com.crskdev.photosurfer.data.remote.NetworkClient
import com.crskdev.photosurfer.data.remote.RetrofitClient
import com.crskdev.photosurfer.data.remote.download.*
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.presentation.executors.BackgroundThreadExecutor
import com.crskdev.photosurfer.presentation.executors.IOThreadExecutor
import com.crskdev.photosurfer.presentation.executors.UIThreadExecutor
import com.crskdev.photosurfer.data.local.photo.ExternalPhotoGalleryDAOImpl
import com.crskdev.photosurfer.data.local.photo.ExternalPhotoGalleryDAO
import com.crskdev.photosurfer.data.remote.auth.*
import com.crskdev.photosurfer.data.remote.user.UserAPI
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.data.repository.user.UserRepositoryImpl
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.services.JobService
import com.crskdev.photosurfer.services.JobServiceImpl
import retrofit2.Retrofit
import java.util.concurrent.Executor
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.PersistentCookieJar


/**
 * Created by Cristian Pela on 09.08.2018.
 */
object DependencyGraph {

    internal var isInit: Boolean = false

    //EXECUTORS
    val uiThreadExecutor: Executor = UIThreadExecutor()
    val backgroundThreadExecutor: Executor = BackgroundThreadExecutor()
    val ioThreadExecutor: Executor = IOThreadExecutor()

    //DB
    lateinit var db: PhotoSurferDB
        private set

    lateinit var externalPhotoGalleryDAO: ExternalPhotoGalleryDAO
        private set

    //NETWORK
    lateinit var authTokenStorage: AuthTokenStorage
        private set
    lateinit var observableAuthState: ObservableAuthState
        private set
    lateinit var retrofit: Retrofit
        private set
    lateinit var progressListenerRegistrar: ProgressListenerRegistrar
        private set
    lateinit var downloadManager: DownloadManager
        private set
    lateinit var photoDownloader: PhotoDownloader
        private set

    //APIs
    lateinit var photoAPI: PhotoAPI
        private set

    //repositories
    lateinit var photoRepository: PhotoRepository
        private set
    lateinit var userRepository: UserRepository
        private set

    //nav
    lateinit var authNavigatorMiddleware: AuthNavigatorMiddleware
        private set

    fun init(context: Context) {
        if (isInit) return

        val preferences = context.getSharedPreferences("photo_surfer_prefs", Context.MODE_PRIVATE)

        //NETWORK
        authTokenStorage = AuthTokenStorageImpl(preferences).apply {
            observableAuthState = this
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

        //photo
        photoAPI = retrofit.create(PhotoAPI::class.java)
        externalPhotoGalleryDAO = ExternalPhotoGalleryDAOImpl(context)
        photoDownloader = PhotoDownloaderImpl(photoAPI)
        downloadManager = DownloadManager(progressListenerRegistrar, photoDownloader, externalPhotoGalleryDAO)
        photoRepository = PhotoRepositoryImpl(
                TransactionRunnerImpl(db),
                photoAPI,
                db.photoDAO(),
                db.photoLikeDAO(),
                db.photoUserDAO(),
                downloadManager,
                JobServiceImpl()
        )

        //user and auth
        val userAPI = retrofit.create(UserAPI::class.java)
        val authAPI: AuthAPI = retrofit.create(AuthAPI::class.java)
        userRepository = UserRepositoryImpl(userAPI, authAPI, authTokenStorage)

        authNavigatorMiddleware = AuthNavigatorMiddleware(authTokenStorage)

        isInit = true
    }

}

fun Context.injectDependencyGraph() = DependencyGraph.init(this)

fun Context.dependencyGraph(): DependencyGraph = if (DependencyGraph.isInit)
    DependencyGraph else DependencyGraph.apply { init(this@dependencyGraph) }