package com.crskdev.photosurfer.data.local.playwave

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import androidx.paging.PositionalDataSource
import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.data.local.playwave.song.SongDataSource
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Cristian Pela on 14.10.2018.
 */
@RunWith(AndroidJUnit4::class)
class SongDataSourceTest {

    @Test
    fun convertRow() {
        val context = InstrumentationRegistry.getTargetContext()
        val dataSource = SongDataSource(context.contentResolver, "with")
        val callback = object : PositionalDataSource.LoadInitialCallback<Song>() {

            var results: List<Song>? = null

            override fun onResult(data: MutableList<Song>, position: Int, totalCount: Int) {
                results = data
            }

            override fun onResult(data: MutableList<Song>, position: Int) {
                results = data
            }

        }
        val params = PositionalDataSource
                .LoadInitialParams(0, 10, 10, false)
        dataSource.loadInitial(params, callback)

        callback.results?.forEach {
            println(it)
        }

    }
}