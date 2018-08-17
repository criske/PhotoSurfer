package com.crskdev.photosurfer.presentation.user


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.defaultTransitionNavOptionsBuilder
import kotlinx.android.synthetic.main.fragment_user_profile.*

class UserProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupChildGraphAndToolbar(view)
    }

    private fun setupChildGraphAndToolbar(view: View) {
        val navHostFragment = NavHostFragment.create(R.navigation.nav_user_profile_graph)
        childFragmentManager.beginTransaction()
                .replace(R.id.nav_host_user_profile_container, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitNow()

        val parentNavController = view.findNavController()
        val navController = navHostFragment.navController
        val navOptions = defaultTransitionNavOptionsBuilder().setLaunchSingleTop(true).build()
        toolbarProfile.apply {
            inflateMenu(R.menu.menu_user_profile)
            setOnMenuItemClickListener {

                when (it.itemId) {
                    R.id.menu_action_user_photos ->
                        if (!navController.popBackStack(R.id.fragment_user_profile_photos, false))
                            navController.navigate(R.id.fragment_user_profile_photos, arguments?.copy(),
                                    navOptions)
                    R.id.menu_action_user_details ->
                        if (!navController.popBackStack(R.id.fragment_user_profile_details, false))
                            navController.navigate(R.id.fragment_user_profile_details, arguments?.copy(),
                                    navOptions)
                }
                true
            }
            setNavigationOnClickListener {
                if (!navController.navigateUp())//if child backstack is depleted - exit to parent
                    parentNavController.navigateUp()
            }
        }
        navController.navigate(R.id.fragment_user_profile_details, arguments?.copy(),
                navOptions)
    }


    private fun Bundle.copy() = Bundle().apply { putAll(this@copy) }
}

class UserDetailsViewModel()