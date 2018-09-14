package com.crskdev.photosurfer.presentation.collection


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController

import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_upsert_collection.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*

class NewCollectionFragment : Fragment() {

    private lateinit var viewModel: NewCollectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this) {
            NewCollectionViewModel(context!!.dependencyGraph().collectionsRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upsert_collection, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val withPhotoId = NewCollectionFragmentArgs.fromBundle(arguments).withPhotoId
        toolbarNewCollection.apply {
            menu.add("Save").apply {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp)
                setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnMenuItemClickListener {
                    viewModel.submit(
                            editNewCollectionTitle.text?.toString(),
                            editNewCollectionDescription.text?.toString(),
                            checkNewCollectionPrivate.isChecked)
                    true
                }
            }
            if (withPhotoId != null) {
                subtitle = "+1 photo"
            }
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        viewModel.successLiveData.observe(this, Observer {
            view.findNavController().popBackStack()
        })

        viewModel.errorLiveData.observe(this, Observer {
            editNewCollectionTitleLayout.error = getString(R.string.error_collection_title)
        })

    }
}

class NewCollectionViewModel(collectionRepository: CollectionRepository) : ViewModel() {

    private val upsertCollectionDelegate = UpsertCollectionPresentationDelegate(collectionRepository)

    val successLiveData = upsertCollectionDelegate.successLiveData

    val errorLiveData = upsertCollectionDelegate.errorLiveData

    fun submit(title: String?, description: String? = null, private: Boolean) {
        upsertCollectionDelegate.submit(title, description, private)
    }

}

