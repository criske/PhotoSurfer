package com.crskdev.photosurfer.data.local.playwave

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import androidx.paging.PositionalDataSource
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

/**
 * Created by Cristian Pela on 14.10.2018.
 */
@RunWith(AndroidJUnit4::class)
class SongDataSourceTest {

    @Test
    fun convertRow() {
        val context = InstrumentationRegistry.getTargetContext()
        val dataSource = SongDataSource(context.contentResolver)
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