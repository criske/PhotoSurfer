package com.crskdev.photosurfer.presentation.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.User

/**
 * Created by Cristian Pela on 27.08.2018.
 */
class SearchUsersAdapter(private val inflater: LayoutInflater,
                         private val glide: RequestManager,
                         private val action: (User) -> Unit) :
        PagedListAdapter<User, SearchUsersVH>(
                object : DiffUtil.ItemCallback<User>() {
                    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
                    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
                }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUsersVH =
            SearchUsersVH(inflater.inflate(R.layout.item_list_users, parent, false), glide, action)

    override fun onBindViewHolder(holder: SearchUsersVH, position: Int) {
        getItem(position)?.let { holder.bind(it) } ?: holder.clear()
    }

    override fun onViewRecycled(holder: SearchUsersVH) {
        holder.clear()
    }
}


class SearchUsersVH(view: View,
                    private val glide: RequestManager,
                    private val action: (User) -> Unit) : RecyclerView.ViewHolder(view) {

    private val imageListUser: ImageView = view.findViewById(R.id.imageListUser)!!

    private lateinit var user: User

    init {
        imageListUser.setOnClickListener { action(user) }
    }

    fun bind(user: User) {
        this.user = user
        glide.asDrawable()
                .load(user.profileImageLinks[ImageType.MEDIUM])
                .apply(RequestOptions()
                        .error(R.drawable.ic_avatar_placeholder)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .transforms(CenterCrop(), RoundedCorners(8)))
                .into(imageListUser)
    }

    fun clear() {
        glide.clear(imageListUser)
    }

}