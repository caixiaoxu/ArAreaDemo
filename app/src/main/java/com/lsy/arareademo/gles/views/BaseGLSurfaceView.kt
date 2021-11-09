package com.lsy.arareademo.gles.views

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.lsy.arareademo.gles.renderers.NormalRenderer

/**
 * @author Xuwl
 * @date 2021/11/9
 *
 */
abstract class BaseGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    protected var mRenderer: NormalRenderer? = null

    fun setRenderer(renderer: NormalRenderer) {
        mRenderer = renderer
        setRenderer(mRenderer)
    }
}