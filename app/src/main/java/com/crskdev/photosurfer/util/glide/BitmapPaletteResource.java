package com.crskdev.photosurfer.util.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Cristian Pela on 05.10.2018.
 */
public class BitmapPaletteResource implements Resource<BitmapPalette>{

    private final BitmapPalette bitmapPalette;
    private final BitmapPool bitmapPool;

    /**
     * Returns a new {@link BitmapResource} wrapping the given {@link Bitmap} if the Bitmap is
     * non-null or null if the given Bitmap is null.
     *
     * @param bitmap     A Bitmap.
     * @param bitmapPool A non-null {@link com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool}.
     */
    @Nullable
    public static BitmapResource obtain(@Nullable Bitmap bitmap, @NonNull BitmapPool bitmapPool) {
        if (bitmap == null) {
            return null;
        } else {
            return new BitmapResource(bitmap, bitmapPool);
        }
    }

    public BitmapPaletteResource(@NonNull BitmapPalette bitmapPalette, @NonNull BitmapPool bitmapPool) {
        this.bitmapPalette = Preconditions.checkNotNull(bitmapPalette, "Bitmap must not be null");
        this.bitmapPool = Preconditions.checkNotNull(bitmapPool, "BitmapPool must not be null");
    }

    @NonNull
    @Override
    public Class<BitmapPalette> getResourceClass() {
        return BitmapPalette.class;
    }

    @NonNull
    @Override
    public BitmapPalette get() {
        return bitmapPalette;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(bitmapPalette.getBitmap());
    }

    @Override
    public void recycle() {
        bitmapPool.put(bitmapPalette.getBitmap());
    }
}
