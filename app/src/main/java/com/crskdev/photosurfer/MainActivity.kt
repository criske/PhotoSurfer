package com.crskdev.photosurfer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(BuildConfig.ACCESS_KEY)
        println(BuildConfig.SECRET_KEY)
    }
}
