package com.lsy.arareademo.gles.shapes.base

import android.opengl.GLES20.*
import com.lsy.arareademo.gles.arrays.VertexArray
import com.lsy.arareademo.gles.programs.WorldShaderProgram

/**
 * @author Xuwl
 * @date 2021/11/4
 *
 */
class Triangle : Shape() {
    private val POSITION_COMPONENT_COUNT = 3
    private val COLOR_COMPONENT_COUNT = 4
    private val STRIDE: Int = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Float.SIZE_BYTES

    private val vertexArray: VertexArray

    init {
        val vertexData = floatArrayOf(
            0f, 0.5f, 0f, 1f, 0f, 0f, 1f,
            -0.5f, -0.5f, 0f, 0f, 1f, 0f, 1f,
            0.5f, -0.5f, 0f, 0f, 0f, 1f, 1f
        )
        vertexArray = VertexArray(vertexData)
    }

    /**
     * 绑定数据
     */
    override fun bindData(vararg attribLocation: Int) {
        vertexArray.setVertexAttribPointer(0, attribLocation[0], POSITION_COMPONENT_COUNT, STRIDE)
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
            attribLocation[1], COLOR_COMPONENT_COUNT, STRIDE)
    }

    /**
     * 绘制
     */
    override fun draw() {
        glDrawArrays(GL_TRIANGLES, 0, 3)
    }
}