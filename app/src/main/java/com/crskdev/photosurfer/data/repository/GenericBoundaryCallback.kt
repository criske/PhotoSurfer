package com.crskdev.photosurfer.data.repository

import androidx.paging.PagedList
import com.crskdev.photosurfer.entities.BaseEntity
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Cristian Pela on 26.08.2018.
 */
class GenericBoundaryCallback<E : BaseEntity>(
        private val onLoadMore: (Int) -> Unit) : PagedList.BoundaryCallback<E>() {

    private val isLoading = AtomicBoolean(false)

    override fun onZeroItemsLoaded() {
        tryLoadMore(1)
    }

    override fun onItemAtEndLoaded(itemAtEnd: E) {
        tryLoadMore(itemAtEnd.pagingData?.next)
    }

    override fun onItemAtFrontLoaded(itemAtFront: E) {
        tryLoadMore(itemAtFront.pagingData?.prev)
    }

    private fun tryLoadMore(page: Int?) {
        if (page != null && !isLoading.get()) {
            isLoading.compareAndSet(false, true)
            onLoadMore(page)
            isLoading.compareAndSet(true, false)
        }
    }
}