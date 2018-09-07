package com.crskdev.photosurfer.presentation.collection


import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.GenericBoundaryCallback
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.Collection
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.util.dpToPx
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_collections.*
import kotlinx.android.synthetic.main.item_list_collections.view.*
import java.util.concurrent.Executor

/**
 * A simple [Fragment] subclass.
 *
 */
class CollectionsFragment : Fragment() {

    private lateinit var viewModel: CollectionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val graph = context!!.dependencyGraph()
        viewModel = viewModelFromProvider(this) {
            CollectionsViewModel(graph.collectionsRepository, graph.diskThreadExecutor)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerCollections.apply {
            val fragmentCtx = this@CollectionsFragment.context!!
            adapter = CollectionsAdapter(LayoutInflater.from(fragmentCtx), Glide.with(fragmentCtx))
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    val margin = 8.dpToPx(resources).toInt()
                    outRect.set(margin, margin, margin, margin)
                }
            })
        }
        toolbarCollections.apply {
            inflateMenu(R.menu.menu_collections)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menu_action_add_collection -> {
                        Toast.makeText(context, "Add new collection screen", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                true
            }
        }
        viewModel.collectionsLiveData.observe(this, Observer {
            (recyclerCollections.adapter as CollectionsAdapter).submitList(it)
        })
    }
}

class CollectionsViewModel(private val collectionsRepository: CollectionRepository,
                           private val diskExecutor: Executor) : ViewModel() {

    val collectionsLiveData = LivePagedListBuilder<Int, Collection>(collectionsRepository.getCollections(), defaultPageListConfig())
            .setFetchExecutor(diskExecutor)
            .setBoundaryCallback(GenericBoundaryCallback<Collection> {
                collectionsRepository.fetchAndSaveCollection(it)
            })
            .build()
}

class CollectionsAdapter(
        private val inflater: LayoutInflater,
        private val glide: RequestManager) : PagedListAdapter<Collection, CollectionVH>(
        object : DiffUtil.ItemCallback<Collection>() {
            override fun areItemsTheSame(oldItem: Collection, newItem: Collection): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Collection, newItem: Collection): Boolean = oldItem == newItem
        }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionVH =
            CollectionVH(inflater.inflate(R.layout.item_list_collections, parent, false), glide)

    override fun onBindViewHolder(holder: CollectionVH, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    override fun onViewRecycled(holder: CollectionVH) {
        holder.clear()
    }
}

class CollectionVH(view: View, private val glide: RequestManager) : RecyclerView.ViewHolder(view) {

    private var collection: Collection? = null

    fun bind(collection: Collection) {
        this.collection = collection
        collection.coverPhotoUrls?.get(ImageType.REGULAR)?.let {
            glide.load(it)
                    .apply(RequestOptions()
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .centerCrop())
                    .into(itemView.imageCollectionCover)
        }
        itemView.textCollectionTitle.text = collection.title
        itemView.textCollectionSize.text = collection.totalPhotos.toString()
        itemView.textCollectionDescription.text = collection.description.trim().takeIf { it.isNotEmpty() }
                ?: itemView.context.getString(R.string.no_description)
    }

    fun clear() {
        glide.clear(itemView.imageCollectionCover)
    }

}