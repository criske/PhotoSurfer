package com.crskdev.photosurfer.util.livedata

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.paging.PagedList
import java.text.DecimalFormat


/**
 * Created by Cristian Pela on 17.08.2018.
 */
inline fun <T> LiveData<T>.filter(crossinline predicate: (T?) -> Boolean): LiveData<T> =
        MediatorLiveData<T>().apply {
            addSource(this@filter) {
                if (predicate(it)) {
                    value = it
                }
            }
        }


fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var lastValue: T? = null
        override fun onChanged(t: T) {
            if (lastValue != t) {
                mutableLiveData.value = t
                lastValue = t
            }
        }
    })
    return mutableLiveData
}

inline fun <T> LiveData<T>.distinctUntilChanged(crossinline predicate: (T, T) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var lastValue: T? = null
        override fun onChanged(t: T) {
            val prevT = lastValue
            if (prevT == null || predicate(prevT, t)) {
                mutableLiveData.value = t
                lastValue = t
            }
        }
    })
    return mutableLiveData
}

fun <T> LiveData<T>.skip(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var valueCount = 0
        override fun onChanged(t: T) {
            if (valueCount >= count) {
                mutableLiveData.value = t
            }
            valueCount++
        }
    })
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

fun PagedList.Config.Builder.defaultConfigBuild() = this
        .setEnablePlaceholders(true)
        .setPrefetchDistance(10)
        .setPageSize(10)

fun defaultPageListConfig(): PagedList.Config = PagedList.Config.Builder().defaultConfigBuild().build()

fun Long.suffixFormat(): String {
    if (this < 1000) {
        return this.toString()
    }
    val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
    val value = Math.floor(Math.log10(this.toDouble())).toInt()
    val base = value / 3
    return if (value >= 3 && base < suffix.size) {
        DecimalFormat("#0.0").format(this / Math.pow(10.0, (base * 3).toDouble())) + suffix[base]
    } else {
        DecimalFormat("#,##0").format(this)
    }
}

fun <T> empty() = MutableLiveData<T>()

fun <T> just(item: T) = MutableLiveData<T>().apply {
    value = item
}
