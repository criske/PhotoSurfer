package com.crskdev.photosurfer.data.repository.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.local.photo.ChoosablePhotoDataSourceFactory
import com.crskdev.photosurfer.data.repository.GenericBoundaryCallback
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.util.livedata.defaultConfigBuild
import java.util.concurrent.Executor

/**
 * Created by Cristian Pela on 17.08.2018.
 */
internal fun photosPageListConfigLiveData(
        diskThreadExecutor: Executor,
        choosablePhotoDataSourceFactory: ChoosablePhotoDataSourceFactory,
        errorLiveData: MutableLiveData<Throwable>): LiveData<PagedList<Photo>> {
    val photoRepository = choosablePhotoDataSourceFactory.photoRepository
    return createLiveData(choosablePhotoDataSourceFactory, diskThreadExecutor,
            photoRepository, errorLiveData) {
        //repository action provider
        when (choosablePhotoDataSourceFactory.currentFilter.type) {
            ChoosablePhotoDataSourceFactory.Type.LIKED_PHOTOS -> RepositoryAction(RepositoryAction.Type.LIKE,
                    *choosablePhotoDataSourceFactory.currentFilter.extras)
            ChoosablePhotoDataSourceFactory.Type.TRENDING_PHOTOS -> RepositoryAction(RepositoryAction.Type.TRENDING)
            ChoosablePhotoDataSourceFactory.Type.SEARCH_PHOTOS -> RepositoryAction(RepositoryAction.Type.SEARCH,
                    *choosablePhotoDataSourceFactory.currentFilter.extras)
            ChoosablePhotoDataSourceFactory.Type.USER_PHOTOS -> RepositoryAction(RepositoryAction.Type.USER_PHOTOS,
                    *choosablePhotoDataSourceFactory.currentFilter.extras)
            ChoosablePhotoDataSourceFactory.Type.SAVED_PHOTOS -> RepositoryAction.NONE
        }
    }
}

private fun createLiveData(dataSourceFactory: DataSource.Factory<Int, Photo>,
                           diskExecutor: Executor,
                           photoRepository: PhotoRepository,
                           errorLiveData: MutableLiveData<Throwable>,
                           repositoryActionProvider: () -> RepositoryAction): LiveData<PagedList<Photo>> =
        PagedList.Config.Builder()
                .defaultConfigBuild()
                .build()
                .let {
                    LivePagedListBuilder<Int, Photo>(dataSourceFactory, it)
                            .setFetchExecutor(diskExecutor)
                            .setBoundaryCallback(GenericBoundaryCallback<Photo> { page ->
                                //TODO might not need page after all, I'm using the last item in repository to get the page
                                val repositoryAction = repositoryActionProvider()
                                if (repositoryAction.type != RepositoryAction.Type.NONE) {
                                    photoRepository.fetchAndSavePhotos(repositoryAction, object : Repository.Callback<Unit> {
                                        override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                                            errorLiveData.postValue(error)
                                        }
                                    })
                                }
                            })
                            .build()
                }