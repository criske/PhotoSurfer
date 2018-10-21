package com.crskdev.photosurfer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.crskdev.photosurfer.presentation.playwave.UpsertPlaywaveFragment

/**
 * Created by Cristian Pela on 19.10.2018.
 */
class MockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mock_activity_layout)
        if (savedInstanceState == null) {
            val upsertPlaywaveFragment = UpsertPlaywaveFragment().apply {
                arguments = bundleOf("upsertType" to R.id.addPlaywaveFragment)
            }
            supportFragmentManager.beginTransaction()
                    .replace(R.id.mock_fragment_container, upsertPlaywaveFragment)
                    .commitNow()
        }
    }

}