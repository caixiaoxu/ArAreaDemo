package com.lsy.arareademo.gles.shapes.base

import android.opengl.GLES20.*
import com.lsy.arareademo.gles.arrays.VertexArray

/**
 * @author Xuwl
 * @date 2021/11/4
 *
 */
class Point(val vertexData: FloatArray) : Shape() {
    companion object {
        const val POSITION_COMPONENT_COUNT = 3
    }

    private val STRIDE: Int = POSITION_COMPONENT_COUNT * Float.SIZE_BYTES

    private val vertexArray: VertexArray

    init {
        vertexArray = VertexArray(vertexData)
    }

    /**
     * 绑定数据
     */
    override fun bindData(vararg attribLocation: Int) {
        vertexArray.setVertexAttribPointer(0, attribLocation[0], POSITION_COMPONENT_COUNT, STRIDE)
    }

    /**
     * 绘制
     */
    override fun draw() {
        vertexData.indices.forEach { index ->
            glDrawArrays(GL_POINTS, index, 1)
        }
    }
}