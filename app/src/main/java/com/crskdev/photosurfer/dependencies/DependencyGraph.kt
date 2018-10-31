@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.crskdev.photosurfer.dependencies

import android.content.Context
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.photo.external.ExternalDirectory
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDAO
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDB
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.track.IStaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.PhotoDownloader
import com.crskdev.photosurfer.data.remote.download.ProgressListenerRegistrar
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.data.repository.photo.PhotoRepository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.services.NetworkCheckService
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.executors.ThreadCallChecker
import com.crskdev.photosurfer.services.messaging.DevicePushMessagingManager
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayerProvider
import com.crskdev.photosurfer.services.schedule.IWorkQueueBookKeeper
import com.crskdev.photosurfer.services.schedule.ScheduledWorkManager
import com.crskdev.photosurfer.util.Listenable
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Cristian Pela on 09.08.2018.
 */
object DependencyGraph {

    internal var isInit: AtomicBoolean = AtomicBoolean(false)

    //EXECUTORS
    lateinit var threadCallChecker: ThreadCallChecker
    lateinit var uiThreadExecutor: KExecutor
    lateinit var diskThreadExecutor: KExecutor
    lateinit var ioThreadExecutor: KExecutor
    lateinit var executorManager: ExecutorsManager

    //DB
    lateinit var staleDataTrackSupervisor: IStaleDataTrackSupervisor
        private set
    lateinit var daoManager: DaoManager
        private set
    lateinit var externalDb: ExternalPhotoGalleryDB
        private set
    lateinit var externalPhotoGalleryDAO: ExternalPhotoGalleryDAO
        private set
    lateinit var externalPhotosDirectory: ExternalDirectory
        private set

//    //serialization
//    //todo unify this moshi with the one from retrofit converter
//    lateinit var moshi: Moshi
//        private set

    //NETWORK
    lateinit var apiCallDispatcher: APICallDispatcher
        private set

    lateinit var networkCheckService: NetworkCheckService
        private set
    lateinit var authTokenStorage: AuthTokenStorage
        private set
    lateinit var listenableAuthState: Listenable<AuthToken>
        private set
    lateinit var progressListenerRegistrar: ProgressListenerRegistrar
        private set
    lateinit var downloadManager: DownloadManager
        private set
    lateinit var photoDownloader: PhotoDownloader
        private set

    //MESSAGING
    lateinit var devicePushMessagingManager: DevicePushMessagingManager
        private set

    //SCHEDULE
    lateinit var scheduledWorkManager: ScheduledWorkManager
        private set
    lateinit var workQueueBookKeeper: IWorkQueueBookKeeper
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
    lateinit var playwaveRepository: PlaywaveRepository
        private set

    //nav
    lateinit var authNavigatorMiddleware: AuthNavigatorMiddleware
        private set

    //sound player
    lateinit var playwaveSoundPlayerProvider: PlaywaveSoundPlayerProvider


    fun install(provider: () -> VariantDependencyGraph) {
        if (isInit.get()) return

        val graph = provider()

        //THREADS
        threadCallChecker = graph.threadCallChecker
        uiThreadExecutor = graph.uiThreadExecutor
        diskThreadExecutor = graph.diskThreadExecutor
        ioThreadExecutor = graph.ioThreadExecutor
        executorManager = graph.executorManager

        // val preferences = context.getSharedPreferences("photo_surfer_prefs", Context.MODE_PRIVATE)

        //SCHEDULE
        workQueueBookKeeper = graph.workQueueBookKeeper
        scheduledWorkManager = graph.scheduledWorkManager


        //NETWORK
        networkCheckService = graph.networkCheckService
        authTokenStorage = graph.authTokenStorage

        //authTokenStorage = InMemoryAuthTokenStorage()
       // retrofit = graph.retrofit
        apiCallDispatcher = graph.apiCallDispatcher
        progressListenerRegistrar = graph.progressListenerRegistrar
        listenableAuthState = graph.listenableAuthState

        //messaging
        messagingAPI = graph.messagingAPI
        devicePushMessagingManager = graph.devicePushMessagingManager

        //db
        staleDataTrackSupervisor = graph.staleDataTrackSupervisor
        daoManager = graph.daoManager


        //external
        externalPhotosDirectory = graph.externalPhotosDirectory
        externalDb = graph.externalDb
        externalPhotoGalleryDAO = graph.externalPhotoGalleryDAO

        //scheduled
        scheduledWorkManager = graph.scheduledWorkManager
        workQueueBookKeeper = graph.workQueueBookKeeper

        //photo
        photoAPI = graph.photoAPI
        photoDownloader = graph.photoDownloader
//        downloadManager = DownloadManager.MOCK
        downloadManager = graph.downloadManager
        photoDAOFacade = graph.photoDAOFacade
        photoRepository = graph.photoRepository

        collectionsAPI = graph.collectionsAPI
        collectionsRepository = graph.collectionsRepository

        //playwave
        playwaveRepository = graph.playwaveRepository
        playwaveSoundPlayerProvider = graph.playwaveSoundPlayerProvider

        //search
        searchTermTracker = graph.searchTermTracker

        //user and auth
        userRepository = graph.userRepository

        authNavigatorMiddleware = graph.authNavigatorMiddleware

        isInit.compareAndSet(false, true)
    }

}

fun Context.dependencyGraph(): DependencyGraph {
    if (!DependencyGraph.isInit.get()) { // safe guard - in case of schedulers call without install
        DependencyGraph.install {
            ProdDependencyGraph(this)
        }
    }
    return DependencyGraph
}