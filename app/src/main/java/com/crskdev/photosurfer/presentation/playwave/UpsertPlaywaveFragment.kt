package com.crskdev.photosurfer.presentation.playwave

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.util.attachNavGraph

class UpsertPlaywaveFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment: NavHostFragment
        if (savedInstanceState == null) {
            navHostFragment = NavHostFragment()
            childFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_upsert_playwave_fragment, navHostFragment)
                    .setPrimaryNavigationFragment(navHostFragment)
                    .commitNow()
        } else {
            navHostFragment = childFragmentManager.primaryNavigationFragment!! as NavHostFragment
        }
        navHostFragment.navController.attachNavGraph(R.navigation.nav_play_wave) {
            startDestination = UpsertPlaywaveFragmentArgs.fromBundle(arguments).upsertType
            arguments?.let { addDefaultArguments(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upsert_playwave, container, false)
    }

}
