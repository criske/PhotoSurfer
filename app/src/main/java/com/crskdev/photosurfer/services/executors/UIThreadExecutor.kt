package com.crskdev.photosurfer.services.executors

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class UIThreadExecutor(private val threadCallChecker: ThreadCallChecker) : KExecutor {

    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        if (threadCallChecker.isOnMainThread()) {
            command.run()
        } else {
            mHandler.post(command)
        }
    }
}