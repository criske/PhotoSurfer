package com.crskdev.photosurfer.services.executors

import java.lang.Exception
import java.util.*

/**
 * Created by Cristian Pela on 29.08.2018.
 */
class ExecutorsManager(val types: EnumMap<ExecutorType, out KExecutor>){

    operator fun get(type: ExecutorType): KExecutor = types[type] ?: throw Exception("Invalid executor for $type")

}