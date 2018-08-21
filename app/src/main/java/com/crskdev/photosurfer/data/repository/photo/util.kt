package com.crskdev.photosurfer.data.repository.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.local.photo.ChoosablePhotoDataSourceFactory
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.Photo
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Cristian Pela on 17.08.2018.
 */
internal fun photosPageListConfigLiveData(userName: String,
                                          diskThreadExecutor: Executor,
                                          networkExecutor: Executor,
                                          photoRepository: PhotoRepository,
                                          errorLiveData: MutableLiveData<Throwable>): LiveData<PagedList<Photo>> =
        createLiveData(photoRepository.getPhotos(userName), diskThreadExecutor, networkExecutor,
                photoRepository, errorLiveData) {
            InsertPhotoAction(InsertPhotoAction.Type.USER, userName)
        }

internal fun photosPageListConfigLiveData(
        userNameLiveData: LiveData<String>,
        diskThreadExecutor: Executor,
        networkExecutor: Executor,
        choosablePhotoDataSourceFactory: ChoosablePhotoDataSourceFactory,
        errorLiveData: MutableLiveData<Throwable>): LiveData<PagedList<Photo>> {
    val photoRepository = choosablePhotoDataSourceFactory.photoRepository
    return createLiveData(choosablePhotoDataSourceFactory, diskThreadExecutor, networkExecutor,
            photoRepository, errorLiveData){
        val userName = userNameLiveData.value
        when (choosablePhotoDataSourceFactory.currentFilter.type) {
            ChoosablePhotoDataSourceFactory.Type.LIKED_PHOTOS -> InsertPhotoAction(InsertPhotoAction.Type.LIKE, userName)
            ChoosablePhotoDataSourceFactory.Type.SEARCH_PHOTOS -> TODO()
            ChoosablePhotoDataSourceFactory.Type.RANDOM_PHOTOS -> InsertPhotoAction(InsertPhotoAction.Type.RANDOM)
        }
    }
}

private fun createLiveData(dataSourceFactory: DataSource.Factory<Int, Photo>,
                           diskThreadExecutor: Executor,
                           networkExecutor: Executor,
                           photoRepository: PhotoRepository,
                           errorLiveData: MutableLiveData<Throwable>,
                           insertPhotoActionProvider: () -> InsertPhotoAction): LiveData<PagedList<Photo>> =
        PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build()
                .let {
                    LivePagedListBuilder<Int, Photo>(dataSourceFactory, it)
                            .setFetchExecutor(diskThreadExecutor)
                            .setBoundaryCallback(object : PagedList.BoundaryCallback<Photo>() {

                                val isLoading = AtomicBoolean(false)

                                override fun onItemAtFrontLoaded(itemAtFront: Photo) {
                                    tryLoadMore(itemAtFront.pagingData?.prev)
                                }

                                override fun onItemAtEndLoaded(itemAtEnd: Photo) {
                                    tryLoadMore(itemAtEnd.pagingData?.next)
                                }

                                override fun onZeroItemsLoaded() {
                                    tryLoadMore(1)
                                }

                                fun tryLoadMore(page: Int?) {
                                    if (page != null && !isLoading.get()) {
                                        networkExecutor.execute {
                                            isLoading.compareAndSet(false, true)
                                            photoRepository.insertPhotos(insertPhotoActionProvider(), page, object : Repository.Callback<Unit> {
                                                override fun onError(error: Throwable, isAuthenticationError: Boolean) {
                                                    errorLiveData.postValue(error)
                                                }
                                            })
                                            isLoading.compareAndSet(true, false)
                                        }
                                    }
                                }
                            })
                            .build()

                }