package com.crskdev.photosurfer.services.executors

import java.util.concurrent.Executors

class IOThreadExecutor : KExecutor {

    override val name: String = ExecutorType.NETWORK.toString()

    private val executorService = Executors.newSingleThreadExecutor { r -> Thread(r, name) }

    override fun execute(command: Runnable) {
        if (Thread.currentThread().name != name) {
            executorService.execute(command)
        } else {
            command.run()
        }
    }
}