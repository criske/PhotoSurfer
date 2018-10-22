package com.crskdev.photosurfer.presentation.playwave

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 22.10.2018.
 */
class UpsertPlaywaveViewModelTest{

    @Test
    fun testConversionsAndDisplay(){
        val tenMinutes = TimeUnit.MINUTES.toMillis(10)
        assertEquals("10:00", prettySongDuration(tenMinutes))
        assertEquals(50, positionPercent(tenMinutes/2, tenMinutes))
        assertEquals("05:00", prettySongDuration(tenMinutes/2))
        assertEquals("05:00", prettySongPosition(50, tenMinutes))
        assertEquals(25, positionPercent(tenMinutes/4, tenMinutes))
        assertEquals("02:30", prettySongDuration(tenMinutes/4))
        assertEquals("02:30", prettySongPosition(25, tenMinutes))

    }
}