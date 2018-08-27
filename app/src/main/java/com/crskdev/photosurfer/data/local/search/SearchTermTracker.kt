package com.crskdev.photosurfer.data.local.search

import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Cristian Pela on 23.08.2018.
 */
interface SearchTermTracker {
    enum class Type {
        PHOTO_TERM, USER_TERM, USER_ACCESSED_TERM
    }

    interface ChangeListener {
        fun onChange(old: Term?, new: Term?)
    }

    fun setTerm(term: Term)

    fun getTerm(type: Type): Term?

    fun addListener(listener: ChangeListener)

    fun removeListener(listener: ChangeListener)
}

data class Term(val type: SearchTermTracker.Type, val data: String)

class SearchTermTrackerImpl(private val prefs: SharedPreferences) : SearchTermTracker {

    private val listeners = CopyOnWriteArrayList<SearchTermTracker.ChangeListener>()

    override fun setTerm(term: Term) {
        val old = getTerm(term.type)
        prefs.edit {
            putString(term.type.toString(), term.data.toLowerCase())
        }
        notifyListeners(old, term)

    }

    override fun getTerm(type: SearchTermTracker.Type): Term? {
        return prefs.getString(type.toString(), null)?.let {
            Term(type, it)
        }
    }

    override fun addListener(listener: SearchTermTracker.ChangeListener) {
        listeners.add(listener)
        SearchTermTracker.Type.values().forEach {
            val existentTerm = getTerm(it)
            notifyListeners(existentTerm, existentTerm)
        }

    }

    override fun removeListener(listener: SearchTermTracker.ChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(old: Term?, new: Term?) {
        listeners.forEach {
            it.onChange(old, new)
        }
    }
}