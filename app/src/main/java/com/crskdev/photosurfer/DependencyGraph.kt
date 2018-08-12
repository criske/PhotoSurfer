package com.crskdev.photosurfer

import android.content.Context
import com.crskdev.photosurfer.data.local.PhotoSurferDB
import com.crskdev.photosurfer.data.local.TransactionRunnerImpl
import com.crskdev.photosurfer.data.local.photo.PhotoRepository
import com.crskdev.photosurfer.data.local.photo.PhotoRepositoryImpl
import com.crskdev.photosurfer.data.remote.NetworkClient
import com.crskdev.photosurfer.data.remote.RetrofitClient
import com.crskdev.photosurfer.data.remote.auth.APIKeys
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.download.*
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.presentation.executors.BackgroundThreadExecutor
import com.crskdev.photosurfer.presentation.executors.IOThreadExecutor
import com.crskdev.photosurfer.presentation.executors.UIThreadExecutor
import com.crskdev.photosurfer.services.GalleryPhotoSaver
import com.crskdev.photosurfer.services.PhotoSaver
import java.util.concurrent.Executor

/**
 * Created by Cristian Pela on 09.08.2018.
 */
object DependencyGraph {

    internal var isInit: Boolean = false

    //EXECUTORS
    var uiThreadExecutor: Executor = UIThreadExecutor()
    var backgroundThreadExecutor: Executor = BackgroundThreadExecutor()
    var ioThreadExecutor: Executor = IOThreadExecutor()

    //DB
    lateinit var db: PhotoSurferDB

    lateinit var photoSaver: PhotoSaver

    //NETWORK
    val authTokenStorage: AuthTokenStorage = AuthTokenStorage.NONE
    private val retrofitClient = RetrofitClient(NetworkClient(authTokenStorage,
            APIKeys(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY)))
    val retrofit = retrofitClient.retrofit
    val progressListenerRegistrar: ProgressListenerRegistrar = ProgressListenerRegistrarImpl(retrofitClient)
    lateinit var downloadManager: DownloadManager
    lateinit var photoDownloader: PhotoDownloader

    //repositories
    lateinit var photoRepository: PhotoRepository

    fun init(context: Context) {
        if (isInit) return
        db = PhotoSurferDB.create(context, false)
        val photoAPI = retrofit.create(PhotoAPI::class.java)

        photoSaver = GalleryPhotoSaver(context)
        photoDownloader = PhotoDownloaderImpl(photoAPI)
        downloadManager = DownloadManager(progressListenerRegistrar, photoDownloader, photoSaver)
        photoRepository = PhotoRepositoryImpl(
                TransactionRunnerImpl(db),
                photoAPI,
                db.photoDAO(),
                DownloadManager.MOCK//TODO use real downloadManager in prod
        )

        isInit = true
    }

}

fun Context.injectDependencyGraph() = DependencyGraph.init(this)

fun Context.dependencyGraph(): DependencyGraph = if (DependencyGraph.isInit)
    DependencyGraph else DependencyGraph.apply { init(this@dependencyGraph) }