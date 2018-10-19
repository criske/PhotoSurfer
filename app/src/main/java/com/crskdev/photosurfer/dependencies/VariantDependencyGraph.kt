package com.crskdev.photosurfer.dependencies

import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.PhotoSurferDB
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.photo.external.ExternalDirectory
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDAO
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDB
import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
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
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.services.schedule.ScheduledWorkManager
import com.crskdev.photosurfer.services.schedule.WorkQueueBookKeeper
import com.crskdev.photosurfer.util.Listenable
import retrofit2.Retrofit

interface VariantDependencyGraph {
    //EXECUTORS
    val threadCallChecker: ThreadCallChecker

    val uiThreadExecutor: KExecutor
    val diskThreadExecutor: KExecutor
    val ioThreadExecutor: KExecutor
    val executorManager: ExecutorsManager

    //DB
    val db: PhotoSurferDB

    val staleDataTrackSupervisor: StaleDataTrackSupervisor

    val daoManager: DaoManager

    val externalDb: ExternalPhotoGalleryDB

    val externalPhotoGalleryDAO: ExternalPhotoGalleryDAO

    val externalPhotosDirectory: ExternalDirectory

    val photoDAOFacade: PhotoDAOFacade


//    //serialization
//    //todo unify this moshi with the one from retrofit converter
//     val moshi: Moshi
//

    //NETWORK
    val apiCallDispatcher: APICallDispatcher

    val networkCheckService: NetworkCheckService

    val authTokenStorage: AuthTokenStorage

    val listenableAuthState: Listenable<AuthToken>

    val retrofit: Retrofit

    val progressListenerRegistrar: ProgressListenerRegistrar

    val downloadManager: DownloadManager

    val photoDownloader: PhotoDownloader


    //MESSAGING
    val devicePushMessagingManager: DevicePushMessagingManager


    //SCHEDULE
    val scheduledWorkManager: ScheduledWorkManager

    val workQueueBookKeeper: WorkQueueBookKeeper


    //APIs
    val photoAPI: PhotoAPI

    val collectionsAPI: CollectionsAPI

    val messagingAPI: MessagingAPI


    //repositories
    val photoRepository: PhotoRepository

    val userRepository: UserRepository

    val collectionsRepository: CollectionRepository

    val searchTermTracker: SearchTermTracker

    val playwaveRepository: PlaywaveRepository


    //nav
    val authNavigatorMiddleware: AuthNavigatorMiddleware

    //play wave
    val playwaveSoundPlayer: PlaywaveSoundPlayer

}
