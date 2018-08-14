package com.crskdev.photosurfer.presentation.user


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.crskdev.photosurfer.R
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.data.repository.user.UserRepository
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.util.SingleLiveEvent
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.concurrent.Executor


class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val dependencies = context!!.dependencyGraph()
                return LoginViewModel(
                        dependencies.ioThreadExecutor,
                        dependencies.userRepository
                ) as T
            }
        }).get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnLogin.setOnClickListener {
            viewModel.login(txtEmailLogin.text.toString(), txtPasswordLogin.text.toString())
        }
        viewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
        viewModel.loggedInLiveData.observe(this, Observer {
            Toast.makeText(context, "Successfully Logged In", Toast.LENGTH_SHORT).show()
            view.findNavController().popBackStack()
        })
    }
}

class LoginViewModel(private val ioThreadExecutor: Executor,
                     private val userRepository: UserRepository) : ViewModel() {

    val errorLiveData = SingleLiveEvent<Throwable>()

    val loggedInLiveData = SingleLiveEvent<Unit>()

    fun login(email: String, password: String) {
        if (email.isNotBlank() && password.isNotBlank()) {
            ioThreadExecutor.execute {
                userRepository.login(email, password, object : Repository.Callback<Unit> {
                    override fun onSuccess(data: Unit, extras: Any?) {
                        loggedInLiveData.postValue(Unit)
                    }

                    override fun onError(error: Throwable) {
                        errorLiveData.postValue(error)
                    }
                })
            }
        } else {
            errorLiveData.value = Error("Email or Password is Empty!")
        }
    }

}
