package com.crskdev.photosurfer.services.executors

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Future

class UIThreadExecutor(private val threadCallChecker: ThreadCallChecker) : KExecutor {

    override val name: String = Looper.getMainLooper().thread.name

    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        if (threadCallChecker.isOnMainThread()) {
            command.run()
        } else {
            mHandler.post(command)
        }
    }

    override fun <V> call(callable: () -> V): Future<V> = throw UnsupportedOperationException("Submitting futures on main not allowed")


}
