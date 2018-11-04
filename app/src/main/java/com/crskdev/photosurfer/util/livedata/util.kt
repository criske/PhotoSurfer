package com.crskdev.photosurfer.util.livedata

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.crskdev.photosurfer.services.schedule.NowTimeProvider
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


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

fun <T> LiveData<T>.interval(duration: Long, unit: TimeUnit, nowTimeProvider: NowTimeProvider = object : NowTimeProvider {}): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var lastTime = 0L
        val intervalMillis = unit.toMillis(duration)

        override fun onChanged(t: T) {
            val now = nowTimeProvider.now()
            val delta = now - lastTime
            if (delta > intervalMillis) {
                mutableLiveData.value = t
                lastTime = now
            }
        }
    })
    return mutableLiveData
}

fun <T> LiveData<T>.interval(itemThreshold: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var emitted = 0
        override fun onChanged(t: T) {
            if (emitted >= itemThreshold) {
                mutableLiveData.value = t
                emitted = 0
            }
            emitted += 1
        }
    })
    return mutableLiveData
}

fun <T> merge(list: List<LiveData<T>>): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    list.forEach { ld ->
        mutableLiveData.addSource(ld) {
            mutableLiveData.value = it
        }
    }
    return mutableLiveData
}


inline fun <T, R> LiveData<T>.splitAndMerge(block: LiveData<T>.() -> List<LiveData<R>>): LiveData<R> {
    return merge(this.block())
}

inline fun <T, R> LiveData<T>.scan(initialValue: R, crossinline mapper: (R, T) -> R): LiveData<R> {
    val mutableLiveData: MediatorLiveData<R> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var accValue = initialValue
        override fun onChanged(t: T) {
            accValue = mapper(accValue, t)
            mutableLiveData.value = accValue
        }
    })
    return mutableLiveData
}

inline fun <T> LiveData<T>.onNext(crossinline block: (T) -> Unit): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) { t ->
        mutableLiveData.value = t
        block(t)
    }
    return mutableLiveData
}

@Suppress("UNCHECKED_CAST")
inline fun <reified R : Any> LiveData<*>.cast(): LiveData<R> {
    val mutableLiveData: MediatorLiveData<R> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<Any> {
        override fun onChanged(item: Any) {
            mutableLiveData.value = item as R
        }
    })
    return mutableLiveData
}

fun <T, V> LiveData<T>.switchMap(block: (T) -> LiveData<V>): LiveData<V> =
        Transformations.switchMap(this, block)

fun <T, V> LiveData<T>.map(block: (T) -> V): LiveData<V> =
        Transformations.map(this, block)

fun <T> empty() = MutableLiveData<T>()

fun <T> just(item: T) = MutableLiveData<T>().apply {
    value = item
}

//View MODEL

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

//PAGE LIST

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




