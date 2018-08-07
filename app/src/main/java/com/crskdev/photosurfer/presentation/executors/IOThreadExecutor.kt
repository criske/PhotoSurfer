package com.crskdev.photosurfer.presentation.executors

import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class IOThreadExecutor : Executor {
    private val executorService = Executors.newSingleThreadExecutor()

    override fun execute(command: Runnable) {
        executorService.execute(command)
    }
}