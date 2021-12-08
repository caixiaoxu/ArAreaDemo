package com.kingo.kingoar.gles.listeners

import android.graphics.Bitmap

/**
 * OpenGl ES 绘制保存监听
 * @author Xuwl
 * @date 2021/12/6
 *
 */
interface RendererTaskListener {

    /**
     * 保存图片
     */
    fun takeBitmap(bitmap: Bitmap)
}