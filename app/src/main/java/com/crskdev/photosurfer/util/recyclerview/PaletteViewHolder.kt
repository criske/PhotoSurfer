package com.crskdev.photosurfer.util.recyclerview

import android.graphics.Bitmap
import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

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


abstract class PaletteViewHolder<T>(
        private val paletteManager: PaletteManager,
        view: View) : BindViewHolder<T>(view) {

    abstract fun id(): String?

    abstract fun onBindPalette(palette: Palette)

    protected fun registerPalette(bitmap: Bitmap) {
        id()?.let { id ->
            if (!paletteManager.hasPalette(id)) {
                Palette.from(bitmap).generate { palette ->
                    if (palette != null) {
                        paletteManager.registerPalette(id, palette)
                    }
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
        paletteViewHolder.id()?.let {
            val paletteHolder = holders[it]
            if (paletteHolder == null) {
                holders[it] = Wrapper().apply {
                    holder = paletteViewHolder
                }
            } else {
                paletteHolder.holder = paletteViewHolder
                paletteHolder.palette?.let { p ->
                    paletteViewHolder.onBindPalette(p)
                }
            }
        }

    }

    fun unbindHolder(paletteViewHolder: PaletteViewHolder<*>) {
        paletteViewHolder.id()?.let { id ->
            holders[id]?.let {
                if (paletteViewHolder == it.holder) {
                    it.holder?.unBind()
                    it.holder = null
                }
            }
        }
    }

    fun unbindAllHoldersLike(clazz: Class<out PaletteViewHolder<*>>) {
        holders.values.forEach {
            if (it.holder?.javaClass == clazz) {
                it.holder?.unBind()
                it.holder = null
            }
        }
    }

    fun unbindAllHolders() {
        holders.values.forEach {
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
            paletteHolder.holder?.apply {
                if (isBound()) {
                    onBindPalette(palette)
                }
            }
        }
    }

    fun hasPalette(id: String) = holders[id]?.palette != null

}