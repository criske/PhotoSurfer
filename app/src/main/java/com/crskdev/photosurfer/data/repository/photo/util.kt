package com.crskdev.photosurfer.data.repository.photo

import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.Photo
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Cristian Pela on 17.08.2018.
 */

internal fun photosPageListConfigLiveData(userName: String?,
                                          pagingBackgroundExecutor: Executor,
                                          networkExecutor: Executor,
                                          photoRepository: PhotoRepository,
                                          errorLiveData: MutableLiveData<Throwable>) =

        PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build()
                .let {
                    LivePagedListBuilder<Int, Photo>(photoRepository.getPhotos(userName), it)
                            .setFetchExecutor(pagingBackgroundExecutor)
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
                                            photoRepository.insertPhotos(userName, page, object : Repository.Callback<Unit> {
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