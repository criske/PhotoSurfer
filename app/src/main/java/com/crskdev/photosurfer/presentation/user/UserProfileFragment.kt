package com.crskdev.photosurfer.presentation.user


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.User
import com.crskdev.photosurfer.util.SingleLiveEvent
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.util.concurrent.Executor

class UserProfileFragment : Fragment() {

    private lateinit var viewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dependencies = context!!.dependencyGraph()
                @Suppress("UNCHECKED_CAST")
                return UserProfileViewModel(dependencies.ioThreadExecutor, dependencies.userRepository) as T
            }

        }).get(UserProfileViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val userId = UserProfileFragmentArgs.fromBundle(arguments).id

        viewModel.userLiveData.observe(this, Observer {
            txtUserProfile.text = it.toString()
        })

        viewModel.getUser(userId)

    }
}

class UserProfileViewModel(
        private val ioThreadExecutor: Executor,
        private val userRepository: UserRepository
) : ViewModel() {

    val userLiveData = MutableLiveData<User>()

    val errorLiveData = SingleLiveEvent<Throwable>()

    fun getUser(id: String?) {
        ioThreadExecutor.execute {
            val cb = object : Repository.Callback<User> {
                override fun onSuccess(data: User, extras: Any?) {
                    userLiveData.postValue(data)
                }

                override fun onError(error: Throwable) {
                    errorLiveData.postValue(error)
                }
            }
            if (id == null) {
                userRepository.me(cb)
            } else {
                userRepository.getUser(id, cb)
            }
        }
    }

}
