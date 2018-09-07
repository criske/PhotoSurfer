package com.crskdev.photosurfer.services.executors

import java.util.concurrent.Executor

/**
 * Created by Cristian Pela on 02.09.2018.
 */
interface KExecutor : Executor {

    operator fun invoke(run: () -> Unit) {
        this.execute(run)
    }

}