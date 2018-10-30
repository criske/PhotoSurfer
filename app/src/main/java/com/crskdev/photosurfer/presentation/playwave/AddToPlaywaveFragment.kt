package com.crskdev.photosurfer.presentation.playwave


import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorFilter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.PlaywaveForPhoto
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import com.crskdev.photosurfer.util.recyclerview.BindViewHolder
import kotlinx.android.synthetic.main.fragment_add_to_playwave.*
import kotlinx.android.synthetic.main.item_list_playwaves_lite.view.*

class AddToPlaywaveFragment : Fragment() {

    private lateinit var viewModel: AddToPlaywaveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelFromProvider(this) {
            val repository = context!!.dependencyGraph()
                    .playwaveRepository
            val photoId = AddToPlaywaveFragmentArgs.fromBundle(arguments).photoId
            AddToPlaywaveViewModel(photoId, repository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_to_playwave, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = AddToPlaywaveAdapter(LayoutInflater.from(view.context)) {
            when (it) {
                is AddToPlaywaveVHAction.AddToPlaywave -> viewModel.addToPlaywave(it.playwave)
                is AddToPlaywaveVHAction.Play -> findNavController()
                        .navigate(AddToPlaywaveFragmentDirections
                                .actionAddToPlaywaveFragmentToPlaywaveSlideShowFragment(it.playwave.playwaveId))
            }
        }

        recyclerAddToPlaywave.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            this.adapter = adapter
        }

        btnAddToPlaywaveCreate.setOnClickListener {
            findNavController().navigate(AddToPlaywaveFragmentDirections
                            .ActionAddToPlaywaveFragmentToUpsertPlaywaveFragment(R.id.addPlaywaveFragment)
                            .setPhotoId(AddToPlaywaveFragmentArgs.fromBundle(arguments).photoId),
                            defaultTransitionNavOptions())
        }

        viewModel.playwaves.observe(this, Observer {
            adapter.submitList(it)
        })
    }
}


class AddToPlaywaveViewModel(
        private val photoId: String,
        private val playwaveRepository: PlaywaveRepository) : ViewModel() {

    val playwaves = playwaveRepository.getPlaywavesForPhoto(photoId)

    fun addToPlaywave(playwaveForPhoto: PlaywaveForPhoto) {
        //toggle
        val isAdding = !playwaveForPhoto.hasPhoto
        if (isAdding) {
            playwaveRepository.addPhotoToPlaywave(playwaveForPhoto.playwaveId, photoId)
        } else {
            playwaveRepository.removePhotoFromPlaywave(playwaveForPhoto.playwaveId, photoId)
        }
    }
}

class AddToPlaywaveAdapter(private val inflater: LayoutInflater,
                           private val action: (AddToPlaywaveVHAction) -> Unit) : ListAdapter<PlaywaveForPhoto, AddToPlaywaveVH>(
        object : DiffUtil.ItemCallback<PlaywaveForPhoto>() {
            override fun areItemsTheSame(oldItem: PlaywaveForPhoto, newItem: PlaywaveForPhoto): Boolean =
                    oldItem.playwaveId == newItem.playwaveId

            override fun areContentsTheSame(oldItem: PlaywaveForPhoto, newItem: PlaywaveForPhoto): Boolean =
                    oldItem == newItem

        }) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).playwaveId.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddToPlaywaveVH =
            AddToPlaywaveVH(inflater.inflate(R.layout.item_list_playwaves_lite, parent, false), action)

    override fun onBindViewHolder(holder: AddToPlaywaveVH, position: Int) {
        holder.bind(getItem(position))
    }

}

class AddToPlaywaveVH(view: View, action: (AddToPlaywaveVHAction) -> Unit) : BindViewHolder<PlaywaveForPhoto>(view) {

    init {
        with(itemView) {
            btnAddToPlaywave.setOnClickListener { _ ->
                model?.let {
                    action(AddToPlaywaveVHAction.AddToPlaywave(it))
                }
            }
            setOnClickListener { _ ->
                model?.let {
                    action(AddToPlaywaveVHAction.Play(it))
                }
            }
        }
    }


    override fun onBindModel(model: PlaywaveForPhoto) {
        with(itemView) {
            textAddToPlaywaveTitle.text = model.playwaveTitle
            textAddToPlaywaveCount.text = model.playwaveSize.toString()
            btnAddToPlaywave.apply {
                colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(if (model.hasPhoto) R.color.colorAccent else R.color.colorPrimaryLight)
                setImageResource(if (model.hasPhoto) R.drawable.ic_remove_white_24dp else R.drawable.ic_add_white_24dp)
            }
        }

    }

    override fun unBind() = Unit

}

sealed class AddToPlaywaveVHAction {
    class AddToPlaywave(val playwave: PlaywaveForPhoto) : AddToPlaywaveVHAction()
    class Play(val playwave: PlaywaveForPhoto) : AddToPlaywaveVHAction()
}



