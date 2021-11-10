package com.kingo.kingoar.gles.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * @author Xuwl
 * @date 2021/10/29
 *
 */
class NormalGLSurfaceView(context: Context?, attrs: AttributeSet? = null) :
    BaseGLSurfaceView(context, attrs) {

    private val TOUCH_SCALE_FACTOR: Float = 180.0f / 360f
    private var previousX: Float = 0f
    private var previousY: Float = 0f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x: Float = e.x
        val y: Float = e.y

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx: Float = x - previousX
                var dy: Float = y - previousY
                if (y > height / 2) dx *= -1

                if (x < width / 2) dy *= -1

                mRenderer?.let {
                    if (dx > dy){
                        it.angleX += dx * TOUCH_SCALE_FACTOR
                    } else {
                        it.angleY += dy * TOUCH_SCALE_FACTOR
                    }
                    requestRender()
                }
            }
        }

        previousX = x
        previousY = y

        return true
    }
}