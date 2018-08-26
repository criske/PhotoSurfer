package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData

/**
 * Created by Cristian Pela on 26.08.2018.
 */
abstract class BaseEntity{
    abstract val pagingData: PagingData?
}