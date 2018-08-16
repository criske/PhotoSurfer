package com.crskdev.photosurfer.presentation.user


import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.ImageType
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.util.SingleLiveEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_user_profile_details.*
import java.util.concurrent.Executor

/**
 * A simple [Fragment] subclass.
 *
 */
class UserProfileDetailsFragment : Fragment() {

    private lateinit var viewModel: UserProfileDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dependencies = context!!.dependencyGraph()
                @Suppress("UNCHECKED_CAST")
                return UserProfileDetailsViewModel(dependencies.ioThreadExecutor, dependencies.userRepository) as T
            }

        }).get(UserProfileDetailsViewModel::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val username = UserProfileDetailsFragmentArgs.fromBundle(arguments).id

        viewModel.userLiveData.observe(this, Observer {
            displayAvatar(username, it.profileImageLinks[ImageType.LARGE]!!)
            textProfileFullName.text = it.firstName
            textProfileUsername.text = it.userName
            textProfileLocation.text = it.location ?: "?"
        })

        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })

        viewModel.getUser(username)
    }

    private fun displayAvatar(username: String, link: String) {
        Glide.with(this)
                .asBitmap()
                .load(link)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .circleCrop())
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        view?.let { v ->
                            Snackbar.make(v, e?.message
                                    ?: "Unknown Exception", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Retry") {
                                        viewModel.getUser(username)
                                    }
                                    .show()
                        }
                        return true
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                })
                .into(imgProfile)
    }
}

class UserProfileDetailsViewModel(
        private val ioThreadExecutor: Executor,
        private val userRepository: UserRepository
) : ViewModel() {

    val userLiveData = MutableLiveData<User>()

    val errorLiveData = SingleLiveEvent<Throwable>()

    fun getUser(id: String) {
        ioThreadExecutor.execute {
            val cb = object : Repository.Callback<User> {
                override fun onSuccess(data: User, extras: Any?) {
                    userLiveData.postValue(data)
                }

                override fun onError(error: Throwable) {
                    errorLiveData.postValue(error)
                }
            }
            if (id.isEmpty()) {
                userRepository.me(cb)
            } else {
                userRepository.getUser(id, cb)
            }
        }
    }

}