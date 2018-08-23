package com.crskdev.photosurfer.presentation

import com.crskdev.photosurfer.data.local.search.SearchTermTracker
import com.crskdev.photosurfer.data.local.search.Term
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent

/**
 * Created by Cristian Pela on 23.08.2018.
 */
class SearchTermTrackerLiveData(private val searchTermTracker: SearchTermTracker) : SingleLiveEvent<Pair<Term?, Term?>>() {

    private val listener = object : SearchTermTracker.ChangeListener {
        override fun onChange(old: Term?, new: Term?) {
            postValue(Pair(old, new))
        }
    }

    override fun onActive() {
        searchTermTracker.addListener(listener)
    }

    override fun onInactive() {
        searchTermTracker.removeListener(listener)
    }
}