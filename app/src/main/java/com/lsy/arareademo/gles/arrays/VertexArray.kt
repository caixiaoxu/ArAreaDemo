package com.lsy.arareademo.gles.arrays

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VertexArray(vertexData: FloatArray) {
    private val floatBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexData.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexData)
            }
        }

    /**
     * 设置顶点参数
     * @param dataOffsset 起始位置
     * @param attributeLocation 着色器变量
     * @param componentCount 一条数据有几个数据
     * @param stride 长度
     */
    fun setVertexAttribPointer(
        dataOffsset: Int, attributeLocation: Int, componentCount: Int, stride: Int,
    ) {
        floatBuffer.position(dataOffsset)
        glVertexAttribPointer(
            attributeLocation, componentCount, GL_FLOAT, false, stride, floatBuffer
        )
        glEnableVertexAttribArray(attributeLocation)
        floatBuffer.position(0)
    }

    /**
     * 更新缓存
     * @param vertexData 顶点数组
     * @param start 开始位置
     * @param count 数量
     */
    fun updateBuffer(vertexData: FloatArray, start: Int, count: Int) {
        floatBuffer.position(start)
        floatBuffer.put(vertexData, start, count)
        floatBuffer.position(0)
    }
}