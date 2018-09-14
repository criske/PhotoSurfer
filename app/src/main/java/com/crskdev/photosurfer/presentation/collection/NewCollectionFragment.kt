package com.crskdev.photosurfer.presentation.collection


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController

import com.crskdev.photosurfer.R
import kotlinx.android.synthetic.main.fragment_new_collection.*

class NewCollectionFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val withPhotoId = NewCollectionFragmentArgs.fromBundle(arguments).withPhotoId
        toolbarNewCollection.apply {
            menu.add("Save").apply {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp)
                setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnClickListener{
                    Toast.makeText(context, "TODO()", Toast.LENGTH_SHORT).show()
                }
            }
            if (withPhotoId != null) {
                subtitle = "+1 photo"
            }
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
}
