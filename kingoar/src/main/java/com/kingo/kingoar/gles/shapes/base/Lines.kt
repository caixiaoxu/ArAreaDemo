package com.kingo.kingoar.gles.shapes.base

import android.opengl.GLES20
import com.kingo.kingoar.gles.arrays.VertexArray

/**
 * @author Xuwl
 * @date 2021/11/9
 *
 */
class Lines(val vertexData: FloatArray) : Shape() {
    companion object {
        const val POSITION_COMPONENT_COUNT = 3
        const val COLOR_COMPONENT_COUNT = 4
        const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT
    }

    private val STRIDE: Int = TOTAL_COMPONENT_COUNT * Float.SIZE_BYTES

    private val vertexArray: VertexArray

    init {
        vertexArray = VertexArray(vertexData)
    }

    /**
     * 绑定数据
     */
    override fun bindData(vararg attribLocation: Int) {
        vertexArray.setVertexAttribPointer(0, attribLocation[0], POSITION_COMPONENT_COUNT, STRIDE)
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, attribLocation[1],
            COLOR_COMPONENT_COUNT, STRIDE)
    }

    /**
     * 绘制
     */
    override fun draw() {
        GLES20.glLineWidth(10f)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP,
            0, vertexData.size / TOTAL_COMPONENT_COUNT)
    }
}