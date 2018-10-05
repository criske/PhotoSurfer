package com.crskdev.photosurfer.util.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Cristian Pela on 28.09.2018.
 */

abstract class BindViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    protected var model: T? = null

    fun isBound(): Boolean = model != null

    fun bind(model: T) {
        this.model = model
        onBindModel(model)
    }

    abstract fun onBindModel(model: T)

    abstract fun unBind()

}
