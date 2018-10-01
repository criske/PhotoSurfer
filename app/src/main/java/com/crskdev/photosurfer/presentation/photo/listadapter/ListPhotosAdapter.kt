package com.crskdev.photosurfer.presentation.photo.listadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.parcelize
import com.crskdev.photosurfer.presentation.AuthNavigatorMiddleware
import com.crskdev.photosurfer.util.defaultTransitionNavOptions
import com.crskdev.photosurfer.util.recyclerview.PaletteManager
import com.crskdev.photosurfer.util.recyclerview.PaletteViewHolder

class ListPhotosAdapter(private val layoutInflater: LayoutInflater,
                        private val glide: RequestManager,
                        private val action: (ActionWhat, Photo) -> Unit) : PagedListAdapter<Photo, PaletteViewHolder<Photo>>(
        object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        }) {

    private val paletteManager: PaletteManager = PaletteManager()

    private var isRemoteType: Boolean = true


    companion object {

        private const val TYPE_SAVED = 0

        private const val TYPE_REMOTE = 1

        inline fun actionHelper(navController: NavController, authNavigatorMiddleware: AuthNavigatorMiddleware,
                                crossinline deleteAction: (Photo) -> Unit = {},
                                crossinline likeAction: (Photo) -> Unit):
                (ActionWhat, Photo) -> Unit {
            return { what, photo ->
                when (what) {
                    ActionWhat.PHOTO_DETAIL -> {
                        navController.navigate(R.id.fragment_photo_details, bundleOf(
                                "photo" to photo.parcelize(),
                                "enabledActions" to true
                        ), defaultTransitionNavOptions())
//                        navController.navigate(
//                                ListPhotosFragmentDirections.actionFragmentListPhotosToFragmentPhotoDetails(photo.parcelize()))
                    }
                    ActionWhat.AUTHOR -> {
                        navController.navigate(R.id.fragment_user_profile, bundleOf(
                                "username" to photo.authorUsername
                        ), defaultTransitionNavOptions())
//                        navController.navigate(
//                                ListPhotosFragmentDirections.actionFragmentListPhotosToUserProfileFragment(photo.authorUsername))
                    }
                    ActionWhat.LIKE -> {
                        likeAction(photo)
                    }
                    ActionWhat.COLLECTION -> {
                        authNavigatorMiddleware.navigate(navController, R.id.fragment_add_to_collection, bundleOf(
                                "photo" to photo.parcelize()
                        ), defaultTransitionNavOptions())
//                        authNavigatorMiddleware.navigate(
//                                navController,
//                                ListPhotosFragmentDirections.actionFragmentListPhotosToFragmentAddToCollection(photo.parcelize()))
                    }
                    ActionWhat.SAVED_PHOTO_DETAIL -> {

                    }
                    ActionWhat.DELETE_SAVED_PHOTO -> {
                        deleteAction(photo)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isRemoteType) TYPE_REMOTE else TYPE_SAVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaletteViewHolder<Photo> =
            if (viewType == TYPE_REMOTE) {
                ListPhotosVH(
                        glide,
                        paletteManager,
                        layoutInflater.inflate(R.layout.item_list_photos, parent, false),
                        action)
            } else {
                SavedListPhotosVH(
                        glide,
                        paletteManager,
                        layoutInflater.inflate(R.layout.item_saved_list_photos, parent, false),
                        action)
            }


    override fun onBindViewHolder(viewHolder: PaletteViewHolder<Photo>, position: Int) {
        getItem(position)
                ?.let { paletteManager.bindHolder(it, viewHolder) }
                ?: paletteManager.unbindHolder(viewHolder)
    }

    override fun onViewRecycled(holder: PaletteViewHolder<Photo>) {
        paletteManager.unbindHolder(holder)
    }

    fun setType(remote: Boolean) {
        this.isRemoteType = remote
        //if remote evict and unbind all saved list photos vh else list photos vh
        if(remote){
            paletteManager.unbindAllHoldersLike(SavedListPhotosVH::class.java)
        }else{
            paletteManager.unbindAllHoldersLike(ListPhotosVH::class.java)
        }


    }

    enum class ActionWhat {
        PHOTO_DETAIL, SAVED_PHOTO_DETAIL, DELETE_SAVED_PHOTO, AUTHOR, LIKE, COLLECTION
    }

}