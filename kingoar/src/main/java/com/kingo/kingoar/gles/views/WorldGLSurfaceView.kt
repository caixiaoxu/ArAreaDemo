package com.kingo.kingoar.gles.views

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet

/**
 * @author Xuwl
 * @date 2021/11/4
 *
 */
class WorldGLSurfaceView(context: Context?, attrs: AttributeSet?) :
    BaseGLSurfaceView(context, attrs) {
    init {
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true);

    }
}