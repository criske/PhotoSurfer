package com.crskdev.photosurfer.services.executors

import java.util.*
import java.util.concurrent.Executor

/**
 * Created by Cristian Pela on 29.08.2018.
 */
class ExecutorsManager(val types: EnumMap<Type, out Executor>){

    enum class Type{
        UI, DISK, NETWORK
    }

}