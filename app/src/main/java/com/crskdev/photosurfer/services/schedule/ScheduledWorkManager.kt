package com.crskdev.photosurfer.services.schedule

import androidx.work.*
import com.crskdev.photosurfer.services.schedule.worker.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 02.10.2018.
 */
abstract class ScheduledWorkManager(private val schedulers: EnumMap<WorkType, ScheduledWork>) {

    fun schedule(workData: WorkData) {
        schedulers[workData.tag.type]?.schedule(workData)
    }

    abstract fun cancel(tag: Tag?)

}

class ScheduledWorkManagerImpl(schedulers: EnumMap<WorkType, ScheduledWork>) : ScheduledWorkManager(schedulers) {

    companion object {
        fun withDefaultSchedulers(bookKeeper: WorkQueueBookKeeper): ScheduledWorkManager =
                ScheduledWorkManagerImpl(
                        EnumMap<WorkType, ScheduledWork>(WorkType::class.java).apply {
                            put(WorkType.CREATE_COLLECTION, CreateCollectionScheduledWork(bookKeeper))
                            put(WorkType.DELETE_COLLECTION, DeleteCollectionScheduledWork(bookKeeper))
                            put(WorkType.EDIT_COLLECTION, EditCollectionScheduledWork(bookKeeper))
                            put(WorkType.LIKE, LikeScheduledWork(bookKeeper))
                        }
                )
    }

    private val workManager: WorkManager by lazy(LazyThreadSafetyMode.NONE) { WorkManager.getInstance() }

    override fun cancel(tag: Tag?) {
        val instance = workManager
        if (tag == null) {
            instance.cancelAllWork()
        } else {
            instance.cancelAllWorkByTag(tag.toString())
        }
    }

}

object AndroidScheduleUtils {

    inline fun <reified W : Worker> defaultRequest(workData: WorkData): OneTimeWorkRequest {
        return OneTimeWorkRequest.Builder(W::class.java)
                .setInputData(toData(workData))
                .addTag(workData.tag.toString())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
    }

    fun toData(workData: WorkData): Data {
        val dataBuilder = Data.Builder()
        workData.extras.forEach {
            val any = it.second
            when (any) {
                is String -> dataBuilder.putString(it.first, any)
                is Boolean -> dataBuilder.putBoolean(it.first, any)
                is Int -> dataBuilder.putInt(it.first, any)
                else -> throw Error("Format not supported yet")
            }
        }
        return dataBuilder.build()
    }

}


interface ScheduledWork {
    fun schedule(workData: WorkData)
}

abstract class AndroidScheduledWork(protected val workQueueBookKeeper: WorkQueueBookKeeper) : ScheduledWork {

    protected val workManager by lazy(LazyThreadSafetyMode.NONE) {
        WorkManager.getInstance()
    }

}

