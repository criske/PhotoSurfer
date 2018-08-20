package com.crskdev.photosurfer.data.local.track


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.PhotoSurferDB
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith


/**
 * Created by Cristian Pela on 20.08.2018.
 */
@RunWith(value = AndroidJUnit4::class)
class StaleDataTrackSupervisorTest {

    lateinit var db: PhotoSurferDB
    lateinit var dao: StaleDataTackDAO
    lateinit var tracker: StaleDataTrackSupervisor
    private val mockNowTimeProvider = MockNowTimeProvider()

    private val ctx = InstrumentationRegistry.getContext()

    init {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
            override fun postToMainThread(runnable: Runnable) = runnable.run()
        })
    }

    private val EMPTY_PHOTO_ENTITY = PhotoEntity().apply {
        id = ""
        createdAt = ""
        updatedAt = ""
        colorString = ""
        urls = ""
        authorId = ""
        authorUsername = ""
    }

    @Before
    fun setup() {
        mockNowTimeProvider.now = System.currentTimeMillis()
        db = PhotoSurferDB.createForTestEnvironment(ctx)
        tracker = StaleDataTrackSupervisor.install(db, nowTimeProvider = mockNowTimeProvider)
        dao = db.staleDataTrackDAO()
    }

    @After
    fun clear() {
        db.close()
    }

    @Test
    fun shouldRecordTrackInsertRow() {
        //fresh data test
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))

        tracker.runStaleDataCheck()
        assertTrue(!db.photoDAO().isEmpty())
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))

        EMPTY_PHOTO_ENTITY.id = "1"
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))

        //stale time
        mockNowTimeProvider.now = mockNowTimeProvider.now + tracker.staleThresholdMillis
        EMPTY_PHOTO_ENTITY.id = "2"
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        db.photoDAO().insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
        assertEquals(mockNowTimeProvider.now, dao.getRecordedTime(Contract.TABLE_PHOTOS))
    }

}

class MockNowTimeProvider : StaleDataTrackSupervisor.NowTimeProvider {

    var now = -1L

    override fun now(): Long = now
}