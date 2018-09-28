package com.crskdev.photosurfer.util.recyclerview

import android.graphics.Bitmap
import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Cristian Pela on 28.09.2018.
 */

abstract class BindViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(model: T)

    abstract fun unBind()

}


abstract class PaletteViewHolder<T>(
        private val paletteManager: PaletteManager,
        view: View) : BindViewHolder<T>(view) {

    abstract fun id(): String

    abstract fun onBindPalette(palette: Palette)

    protected fun registerPalette(bitmap: Bitmap) {
        val id = id()
        if (!paletteManager.hasPalette(id)) {
            Palette.from(bitmap).generate {
                if (it != null) {
                    paletteManager.registerPalette(id(), it)
                }
            }
        }
    }
}


class PaletteManager {

    private val holders = mutableMapOf<String, Wrapper>()

    private class Wrapper {
        var palette: Palette? = null
        var holder: PaletteViewHolder<*>? = null
    }


    fun <M> bindHolder(model: M, paletteViewHolder: PaletteViewHolder<M>) {
        paletteViewHolder.bind(model)
        val id = paletteViewHolder.id()
        val paletteHolder = holders[id]
        if (paletteHolder == null) {
            holders[id] = Wrapper().apply {
                holder = paletteViewHolder
            }
        } else {
            paletteHolder.holder = paletteViewHolder
            paletteHolder.palette?.let {
                paletteViewHolder.onBindPalette(it)
            }
        }
    }

    fun unbindHolder(paletteViewHolder: PaletteViewHolder<*>) {
        holders[paletteViewHolder.id()]?.let {
            it.holder?.unBind()
            it.holder = null
        }
    }

    fun registerPalette(id: String, palette: Palette) {
        val paletteHolder = holders[id]
        if (paletteHolder == null) {
            holders[id] = Wrapper().apply {
                this.palette = palette
            }
        } else {
            paletteHolder.palette = palette
            paletteHolder.holder?.onBindPalette(palette)
        }
    }

    fun hasPalette(id: String) = holders[id]?.palette != null

}