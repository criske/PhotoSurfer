package com.crskdev.photosurfer.presentation.collection


import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorFilter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.GenericBoundaryCallback
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.Collection
import com.crskdev.photosurfer.entities.PairBE
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.deparcelize
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig
import com.crskdev.photosurfer.util.livedata.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_add_to_collection.*
import kotlinx.android.synthetic.main.item_list_collections_lite.view.*
import java.util.concurrent.Executor

class AddToCollectionFragment : Fragment() {

    private lateinit var viewModel: AddToCollectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val graph = context!!.dependencyGraph()
        val photo = AddToCollectionFragmentArgs.fromBundle(arguments).photo
        viewModel = viewModelFromProvider(this) {
            AddToCollectionViewModel(graph.collectionsRepository,
                    photo.deparcelize(),
                    graph.diskThreadExecutor)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_to_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerCollectionAddCreate.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = CollectionsLiteAdapter(LayoutInflater.from(context), Glide.with(this@AddToCollectionFragment)) {
                val adding = it.right
                val collection = it.left
                if (adding) {
                    viewModel.addToCollection(collection)
                } else {
                    viewModel.removeFromCollection(collection)
                }
            }
        }
        viewModel.collectionsLiveData.observe(this, Observer {
            (recyclerCollectionAddCreate.adapter as CollectionsLiteAdapter).submitList(it)
        })
    }
}

class AddToCollectionViewModel(private val collectionsRepository: CollectionRepository,
                               private val photo: Photo,
                               diskExecutor: Executor) : ViewModel() {

    val collectionsLiveData = LivePagedListBuilder<Int, PairBE<Collection, Boolean>>(
            collectionsRepository.getCollectionsForPhoto(photo.id), defaultPageListConfig())
            .setFetchExecutor(diskExecutor)
            .setBoundaryCallback(GenericBoundaryCallback<PairBE<Collection, Boolean>> {
                collectionsRepository.fetchAndSaveCollection(it)
            })
            .build()

    fun addToCollection(collection: Collection) {
        collectionsRepository.addPhotoToCollection(collection, photo)
    }

    fun removeFromCollection(collection: Collection) {
        collectionsRepository.removePhotoFromCollection(collection, photo)
    }
}

class CollectionsLiteAdapter(
        private val inflater: LayoutInflater,
        private val glide: RequestManager,
        private val action: (PairBE<Collection, Boolean>) -> Unit) : PagedListAdapter<PairBE<Collection, Boolean>, CollectionLiteVH>(
        object : DiffUtil.ItemCallback<PairBE<Collection, Boolean>>() {
            override fun areItemsTheSame(oldItem: PairBE<Collection, Boolean>, newItem: PairBE<Collection, Boolean>): Boolean {
                return oldItem.left.id == newItem.left.id
            }

            override fun areContentsTheSame(oldItem: PairBE<Collection, Boolean>, newItem: PairBE<Collection, Boolean>): Boolean {
                return oldItem.left == newItem.left && oldItem.right == newItem.right
            }
        }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionLiteVH =
            CollectionLiteVH(inflater.inflate(R.layout.item_list_collections_lite, parent, false), glide, action)

    override fun onBindViewHolder(holder: CollectionLiteVH, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        } ?: holder.clear()
    }

    override fun onViewRecycled(holder: CollectionLiteVH) {
        holder.clear()
    }
}

class CollectionLiteVH(view: View,
                       private val glide: RequestManager,
                       private val action: (PairBE<Collection, Boolean>) -> Unit) : RecyclerView.ViewHolder(view) {

    private var collection: PairBE<Collection, Boolean>? = null

    init {
        itemView.btnAddToCollection.setOnClickListener {
            collection?.let { p ->
                val updatePair = PairBE(p.left, !p.right)
                action(updatePair)
            }
        }
    }

    fun bind(collection: PairBE<Collection, Boolean>) {
        this.collection = collection
//        collection.coverPhotoUrls?.get(ImageType.REGULAR)?.let {
//            glide.load(it)
//                    .apply(RequestOptions()
//                            .placeholder(R.drawable.ic_avatar_placeholder)
//                            .centerCrop())
//                    .into(itemView.imageCollectionCover)
//        }
        itemView.textAddToCollectionTitle.text = collection.left.title
        itemView.textAddToCollectionCount.text = collection.left.totalPhotos.toString()
        itemView.btnAddToCollection.colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(if (collection.right) R.color.colorAccent else R.color.colorPrimaryLight)
        itemView.btnAddToCollection.setImageResource(if (collection.right) R.drawable.ic_remove_white_24dp else R.drawable.ic_add_white_24dp)
    }

    fun clear() {
        //glide.clear(itemView.imageCollectionCover)
    }

}