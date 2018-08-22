package com.crskdev.photosurfer.data.local.track

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 20.08.2018.
 */
@Dao
interface StaleDataTackDAO: DataAccessor {

    @Query("SELECT COUNT(*) FROM stale_data_track WHERE `table`=:table ")
    fun hasStaleDataTrack(table: String): Int

    @Query("SELECT time FROM stale_data_track WHERE `table`=:table")
    fun getRecordedTime(table: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createRecord(staleDataTrack: StaleDataTrackEntity)

    @Query("DELETE FROM stale_data_track WHERE `table`=:table")
    fun deleteRecord(table: String)

    @Query("SELECT DISTINCT `table` FROM stale_data_track")
    fun getTables(): List<String>

}