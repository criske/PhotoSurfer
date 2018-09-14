package com.crskdev.photosurfer.data.local.track


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.crskdev.photosurfer.data.local.BaseDBTest
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.PhotoSurferDB
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.services.NetworkCheckService
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith


/**
 * Created by Cristian Pela on 20.08.2018.
 */
@RunWith(value = AndroidJUnit4::class)
class StaleDataTrackSupervisorTest: BaseDBTest() {

    lateinit var dao: StaleDataTackDAO
    lateinit var tracker: StaleDataTrackSupervisor
    private val mockNowTimeProvider = MockNowTimeProvider()

    private val EMPTY_PHOTO_ENTITY = PhotoEntity().apply {
        id = ""
        createdAt = ""
        updatedAt = ""
        colorString = ""
        urls = ""
        authorId = ""
        authorUsername = ""
    }

    override fun onBefore() {
        super.onBefore()
        mockNowTimeProvider.now = System.currentTimeMillis()
        tracker = StaleDataTrackSupervisor.install(object : NetworkCheckService {}, db, nowTimeProvider = mockNowTimeProvider)
        dao = db.staleDataTrackDAO()
    }

    @Test
    fun shouldRecordTrackInsertRow() {
        //fresh data test
        assertTrue(db.photoDAO().isEmpty())
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        tracker.runStaleDataCheckForTable(Contract.TABLE_PHOTOS)
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))

        tracker.runStaleDataCheck()
        assertTrue(!db.photoDAO().isEmpty())
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))

        EMPTY_PHOTO_ENTITY.id = "1"
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        tracker.runStaleDataCheckForTable(Contract.TABLE_PHOTOS)
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))

        //stale time after 12 hours
        mockNowTimeProvider.now = mockNowTimeProvider.now + tracker.staleThresholdMillis
        EMPTY_PHOTO_ENTITY.id = "2"
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        tracker.runStaleDataCheckForTable(Contract.TABLE_PHOTOS) // reset
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))
    }

}

class MockNowTimeProvider : StaleDataTrackSupervisor.NowTimeProvider {

    var now = -1L

    override fun now(): Long = now
}