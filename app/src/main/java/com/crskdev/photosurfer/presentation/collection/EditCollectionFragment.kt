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

/**
 * A simple [Fragment] subclass.
 *
 */
class EditCollectionFragment : Fragment() {

    private lateinit var viewModel: EditCollectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val collectionId = EditCollectionFragmentArgs.fromBundle(arguments).id
        val collectionRepository = context!!.dependencyGraph().collectionsRepository
        viewModel = viewModelFromProvider(this) {
            EditCollectionViewModel(collectionId, collectionRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upsert_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbarCollection.apply {
            setTitle(R.string.edit_collection)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            menu.add("Save").apply {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp)
                setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnMenuItemClickListener {
                    viewModel.submit(
                            editCollectionTitle.text?.toString(),
                            editCollectionDescription.text?.toString(),
                            checkCollectionPrivate.isChecked)
                    true
                }
            }
        }

        viewModel.editCollectionLiveData.observe(this, Observer {
            editCollectionTitle.setText(it.title)
            editCollectionDescription.setText(it.description)
            checkCollectionPrivate.isChecked = it.private
        })

        viewModel.successLiveData.observe(this, Observer {
            editCollectionTitleLayout.error = null
            Toast.makeText(context, getString(R.string.message_collection_edited), Toast.LENGTH_SHORT).show()
        })

        viewModel.errorLiveData.observe(this, Observer {
            editCollectionTitleLayout.error = getString(R.string.error_collection_title)
        })


    }
}

class EditCollectionViewModel(
        collectionId: Int,
        collectionRepository: CollectionRepository) : ViewModel() {

    private val upsertCollectionDelegate = UpsertCollectionPresentationDelegate(collectionRepository, collectionId)

    val successLiveData = upsertCollectionDelegate.successLiveData

    val errorLiveData = upsertCollectionDelegate.errorLiveData

    val editCollectionLiveData = upsertCollectionDelegate.editingCollectionLiveData

    fun submit(title: String?, description: String? = null, private: Boolean) {
        upsertCollectionDelegate.submit(title, description, private, true)
    }

}