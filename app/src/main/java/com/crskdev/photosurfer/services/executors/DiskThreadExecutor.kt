package com.crskdev.photosurfer.services.executors

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DiskThreadExecutor : KExecutor {

    private val executorService = Executors.newSingleThreadExecutor { r -> Thread(r, "PhotoSurfer - Disk Thread") }

    override fun execute(command: Runnable) {
        executorService.execute(command)
    }
}