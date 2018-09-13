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

class ListPhotosAdapter(private val layoutInflater: LayoutInflater,
                        private val glide: RequestManager,
                        private val action: (ActionWhat, Photo) -> Unit) : PagedListAdapter<Photo, ListPhotosVH>(
        object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        }) {

    companion object {
        inline fun actionHelper(navController: NavController, authNavigatorMiddleware: AuthNavigatorMiddleware,
                                crossinline likeAction: (Photo) -> Unit):
                (ActionWhat, Photo) -> Unit {
            return { what, photo ->
                when (what) {
                    ActionWhat.PHOTO_DETAIL -> {
                        navController.navigate(R.id.fragment_photo_details, bundleOf(
                                "photo" to photo.parcelize()
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
                        navController.navigate(R.id.fragment_add_to_collection, bundleOf(
                                "photo" to photo.parcelize()
                        ), defaultTransitionNavOptions())
//                        authNavigatorMiddleware.navigate(
//                                navController,
//                                ListPhotosFragmentDirections.actionFragmentListPhotosToFragmentAddToCollection(photo.parcelize()))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListPhotosVH =
            ListPhotosVH(glide, layoutInflater.inflate(R.layout.item_list_photos, parent, false), action)


    override fun onBindViewHolder(viewHolder: ListPhotosVH, position: Int) {
        getItem(position)
                ?.let { viewHolder.bind(it) }
                ?: viewHolder.clear()
    }

    override fun onViewRecycled(holder: ListPhotosVH) {
        holder.clear()
    }

    enum class ActionWhat {
        PHOTO_DETAIL, AUTHOR, LIKE, COLLECTION
    }

}