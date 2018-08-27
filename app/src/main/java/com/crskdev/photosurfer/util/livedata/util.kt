package com.crskdev.photosurfer.util.livedata

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlin.reflect.KClass

/**
 * Created by Cristian Pela on 17.08.2018.
 */
inline fun <T> LiveData<T>.filter(crossinline predicate: (T) -> Boolean): LiveData<T> =
        MediatorLiveData<T>().apply {
            addSource(this@filter) {
                if (predicate(it)) {
                    value = it
                }
            }
        }

fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var lastValue: T? = null
    mutableLiveData.addSource(this) {
        if (lastValue != it) {
            mutableLiveData.value = it
            lastValue = it
        }
    }
    return mutableLiveData
}

fun <T> LiveData<T>.skip(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var valueCount = 0
    mutableLiveData.addSource(this) {
        if (valueCount >= count) {
            mutableLiveData.value = it
        }
        valueCount++
    }
    return mutableLiveData
}

fun <T> LiveData<T>.skipFirst(): LiveData<T> = skip(1)

inline fun <reified V : ViewModel> viewModelFromProvider(activity: FragmentActivity, crossinline provider: () -> V): V =
        ViewModelProviders.of(activity, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return provider() as T
            }
        }).get(V::class.java)

inline fun <reified V : ViewModel> viewModelFromProvider(fragment: Fragment, crossinline provider: () -> V): V =
        ViewModelProviders.of(fragment, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return provider() as T
            }
        }).get(V::class.java)