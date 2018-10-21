@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.crskdev.photosurfer.data.local.track

import androidx.room.InvalidationTracker
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.PhotoSurferDB
import com.crskdev.photosurfer.services.NetworkCheckService
import java.util.concurrent.TimeUnit

interface IStaleDataTrackSupervisor {
    fun runStaleDataCheck()
    fun runStaleDataCheckForTable(table: String, ignoreNetwork: Boolean = true)
}

/**
 * Created by Cristian Pela on 20.08.2018.
 */
class StaleDataTrackSupervisor private constructor(
        private val networkCheckService: NetworkCheckService,
        private val db: PhotoSurferDB,
        staleThreshold: Long,
        unit: TimeUnit,
        private val nowTimeProvider: NowTimeProvider) : IStaleDataTrackSupervisor {

    interface NowTimeProvider {
        companion object {
            val DEFAULT = object : NowTimeProvider {}
        }

        fun now() = System.currentTimeMillis()
    }

    private var dao: StaleDataTackDAO = db.staleDataTrackDAO()

    internal val staleThresholdMillis = unit.toMillis(staleThreshold)

    companion object {
        fun install(networkCheckService: NetworkCheckService,
                    db: PhotoSurferDB,
                    staleThreshold: Long = 12,
                    unit: TimeUnit = TimeUnit.HOURS,
                    nowTimeProvider: NowTimeProvider = NowTimeProvider.DEFAULT): StaleDataTrackSupervisor =
                StaleDataTrackSupervisor(networkCheckService, db, staleThreshold, unit, nowTimeProvider)

    }

    init {
        db.invalidationTracker.addObserver(object : InvalidationTracker.Observer(Contract.TABLES) {
            override fun onInvalidated(tables: MutableSet<String>) {
                //TODO Activate this when I found a better solution
                //runStaleDataCheck(tables.toList())
            }
        })
    }

    override fun runStaleDataCheck() {
        if (!networkCheckService.isNetworkAvailableAndOnline())
            return
        val tables = dao.getTables()
        runStaleDataCheck(tables)
    }

    private fun runStaleDataCheck(tables: List<String>) {
        if (!networkCheckService.isNetworkAvailableAndOnline())
            return
        db.runInTransaction {
            tables.forEach {
                internalRunStaleDataCheckForTable(it)
            }
        }
    }

    override fun runStaleDataCheckForTable(table: String, ignoreNetwork: Boolean) {
        if (!ignoreNetwork) {
            if (!networkCheckService.isNetworkAvailableAndOnline())
                return
        }
        if (db.inTransaction()) {
            internalRunStaleDataCheckForTable(table)
        } else {
            internalRunStaleDataCheckForTableInTransaction(table)
        }
    }

    private fun internalRunStaleDataCheckForTable(table: String) {
        val thresholdReached = isThresholdReached(table)
        if (thresholdReached) {
            emptyTable(table)
            deleteRecord(table)
        }

        val hasNoStaleDataRecords = !hasStaleDataTrack(table)
        if (hasNoStaleDataRecords) {
            recordStaleDataTrack(table)
        }
    }

    private fun internalRunStaleDataCheckForTableInTransaction(table: String) {
        db.runInTransaction {
            runStaleDataCheckForTable(table)
        }
    }

    private fun deleteRecord(table: String) {
        dao.deleteRecord(table)
    }

    private fun recordStaleDataTrack(table: String) {
        dao.createRecord(StaleDataTrackEntity().apply {
            this.table = table
            time = nowTimeProvider.now()
        })
    }

    private fun isThresholdReached(table: String): Boolean {
        val now = nowTimeProvider.now()
        val recordedTime = dao.getRecordedTime(table)
        return recordedTime > 0 && now - recordedTime >= staleThresholdMillis
    }

    private fun hasStaleDataTrack(table: String): Boolean = dao.hasStaleDataTrack(table) == 1

    private fun emptyTable(table: String) {
        db.openHelper.writableDatabase.execSQL("DELETE FROM $table")
    }

    private fun isEmptyTable(table: String): Boolean {
        val cursor = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count == 0
    }

}