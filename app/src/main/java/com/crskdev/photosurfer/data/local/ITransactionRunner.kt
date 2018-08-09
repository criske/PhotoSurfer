package com.crskdev.photosurfer.data.local

import androidx.room.RoomDatabase

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface ITransactionRunner {

    fun transaction(body: Runnable)

}

class TransactionRunner(private val db: RoomDatabase) : ITransactionRunner {

    override fun transaction(body: Runnable) = db.runInTransaction(body)

}