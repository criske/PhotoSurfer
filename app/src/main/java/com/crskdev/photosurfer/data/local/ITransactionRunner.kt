package com.crskdev.photosurfer.data.local

import androidx.room.RoomDatabase

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface TransactionRunner {

    fun transaction(body: () -> Unit)

    operator fun invoke(body: () -> Unit) = transaction(body)
}

class TransactionRunnerImpl(@PublishedApi internal val db: RoomDatabase) : TransactionRunner {

    override fun transaction(body: () -> Unit) = db.runInTransaction {
        body()
    }


}