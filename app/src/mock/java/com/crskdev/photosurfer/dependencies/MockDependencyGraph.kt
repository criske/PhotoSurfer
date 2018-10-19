package com.crskdev.photosurfer.dependencies

import android.content.Context
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
import com.crskdev.photosurfer.data.repository.playwave.MockPlaywaveRepository
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.services.NetworkCheckService
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.executors.ThreadCallChecker
import com.crskdev.photosurfer.services.messaging.DevicePushMessagingManager
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.services.playwave.MockPlaywaveSoundPlayer
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.services.schedule.ScheduledWorkManager
import com.crskdev.photosurfer.services.schedule.WorkQueueBookKeeper
import com.crskdev.photosurfer.util.Listenable
import retrofit2.Retrofit

/**
 * Created by Cristian Pela on 16.10.2018.
 */
class MockDependencyGraph(context: Context): ProdDependencyGraph(context) {

    override val playwaveRepository: PlaywaveRepository = MockPlaywaveRepository()

    override val playwaveSoundPlayer: PlaywaveSoundPlayer = MockPlaywaveSoundPlayer()

}

class RealMockDependencyGraph: VariantDependencyGraph{
    override val playwaveSoundPlayer: PlaywaveSoundPlayer
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val threadCallChecker: ThreadCallChecker = ThreadCallChecker.SUPPRESED_CHECK

    override val uiThreadExecutor: KExecutor
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val diskThreadExecutor: KExecutor
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val ioThreadExecutor: KExecutor
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val executorManager: ExecutorsManager
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val db: PhotoSurferDB
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val staleDataTrackSupervisor: StaleDataTrackSupervisor
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val daoManager: DaoManager
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val externalDb: ExternalPhotoGalleryDB
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val externalPhotoGalleryDAO: ExternalPhotoGalleryDAO
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val externalPhotosDirectory: ExternalDirectory
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val photoDAOFacade: PhotoDAOFacade
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val apiCallDispatcher: APICallDispatcher
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val networkCheckService: NetworkCheckService
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val authTokenStorage: AuthTokenStorage
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val listenableAuthState: Listenable<AuthToken>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val retrofit: Retrofit
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val progressListenerRegistrar: ProgressListenerRegistrar
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val downloadManager: DownloadManager
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val photoDownloader: PhotoDownloader
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val devicePushMessagingManager: DevicePushMessagingManager
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val scheduledWorkManager: ScheduledWorkManager
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val workQueueBookKeeper: WorkQueueBookKeeper
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val photoAPI: PhotoAPI
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val collectionsAPI: CollectionsAPI
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val messagingAPI: MessagingAPI
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val photoRepository: PhotoRepository
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val userRepository: UserRepository
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val collectionsRepository: CollectionRepository
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val searchTermTracker: SearchTermTracker
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val playwaveRepository: PlaywaveRepository
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val authNavigatorMiddleware: AuthNavigatorMiddleware
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}