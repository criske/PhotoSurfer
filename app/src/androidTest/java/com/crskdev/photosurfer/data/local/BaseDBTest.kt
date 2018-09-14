package com.crskdev.photosurfer.data.local

import android.support.test.InstrumentationRegistry
import androidx.annotation.CallSuper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.crskdev.photosurfer.services.executors.KExecutor
import org.junit.After
import org.junit.Before
import java.util.concurrent.Executor

/**
 * Created by Cristian Pela on 02.09.2018.
 */
abstract class BaseDBTest{

    protected val ctx = InstrumentationRegistry.getContext()

    protected  lateinit var db: PhotoSurferDB

    protected val emptyExecutor = object: KExecutor{
        override fun execute(command: Runnable?) {
            command?.run()
        }
    }

    init {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
            override fun postToMainThread(runnable: Runnable) = runnable.run()
        })
    }

    @CallSuper
    @Before
    open fun onBefore(){
        db = PhotoSurferDB.createForTestEnvironment(ctx)
    }

    @CallSuper
    @After
    open fun onClear(){
        db.close()
    }

}