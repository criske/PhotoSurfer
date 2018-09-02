package com.crskdev.photosurfer.services.executors

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class IOThreadExecutor : KExecutor {

    private val executorService = Executors.newSingleThreadExecutor { r -> Thread(r, "PhotoSurfer - IO Thread") }

    override fun execute(command: Runnable) {
        executorService.execute(command)
    }
}