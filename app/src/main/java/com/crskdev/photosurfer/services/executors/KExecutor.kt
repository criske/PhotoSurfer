package com.crskdev.photosurfer.services.executors

import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Future

/**
 * Created by Cristian Pela on 02.09.2018.
 */
interface KExecutor : Executor {

    val name: String

    operator fun invoke(run: () -> Unit) {
        this.execute(run)
    }

    fun <V> call(callable: ()->V): Future<V>

}