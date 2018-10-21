package com.crskdev.photosurfer

import android.os.Bundle
import android.app.Activity

class TestViewComponentsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
    }

    companion object {
        var layout: Int = 0
    }


}
