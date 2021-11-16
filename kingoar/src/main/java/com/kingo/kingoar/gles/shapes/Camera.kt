package com.kingo.kingoar.gles.shapes

import android.opengl.GLES20.*
import com.kingo.kingoar.gles.arrays.VertexArray
import com.kingo.kingoar.gles.shapes.base.Shape

/**
 * 相机
 * @author Xuwl
 * @date 2021/11/5
 *
 */
class Camera : Shape() {
    private val POSITION_COMPONENT_COUNT = 3
    private val POSITION_STRIDE: Int = POSITION_COMPONENT_COUNT * Float.SIZE_BYTES
    private val TEXTURE_COMPONENT_COUNT = 2
    private val TEXTURE_STRIDE: Int = TEXTURE_COMPONENT_COUNT * Float.SIZE_BYTES

    private val vertexArray: VertexArray
    private val textureArray: VertexArray

    init {
        val vertexData = floatArrayOf(
            -1.0f, 1.0f, 0.0f,  // top left
            -1.0f, -1.0f, 0.0f,  // bottom left
            1.0f, -1.0f, 0.0f,  // bottom right
            1.0f, 1.0f, 0.0f  // top right
        )
        vertexArray = VertexArray(vertexData)

        val textureData = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
        textureArray = VertexArray(textureData)
    }

    override fun bindData(vararg attribLocation: Int) {
        vertexArray.setVertexAttribPointer(0,
            attribLocation[0],
            POSITION_COMPONENT_COUNT,
            POSITION_STRIDE)
        textureArray.setVertexAttribPointer(0,
            attribLocation[1],
            TEXTURE_COMPONENT_COUNT,
            TEXTURE_STRIDE)
    }

    override fun draw() {

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        // 解绑纹理
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}