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
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.presentation.executors.BackgroundThreadExecutor
import com.crskdev.photosurfer.presentation.executors.IOThreadExecutor
import com.crskdev.photosurfer.presentation.executors.UIThreadExecutor
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

    //NETWORK
    val authTokenStorage: AuthTokenStorage = AuthTokenStorage.NONE
    val retrofit = RetrofitClient(NetworkClient(authTokenStorage,
            APIKeys(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY)))
            .retrofit

    //repositories
    lateinit var photoRepository: PhotoRepository

    fun init(context: Context) {
        if (isInit) return

        db = PhotoSurferDB.create(context, false)
        photoRepository = PhotoRepositoryImpl(
                TransactionRunnerImpl(db),
                retrofit.create(PhotoAPI::class.java),
                db.photoDAO()
        )

        isInit = true
    }

}

fun Context.injectDependencyGraph() = DependencyGraph.init(this)

fun Context.dependencyGraph(): DependencyGraph = if (DependencyGraph.isInit)
    DependencyGraph else DependencyGraph.apply { init(this@dependencyGraph) }