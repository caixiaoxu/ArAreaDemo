package com.kingo.kingoar.gles.programs

import android.content.Context
import android.opengl.GLES20.*

/**
 * 普通的着色器程序
 * @author Xuwl
 * @date 2021/11/2
 *
 */
class NormalShaderProgram(context: Context) : BaseShaderProgram(context,
    "vshader/base_vertex_shader.glsl",
    "fshader/base_fragment_shader.glsl") {
    val aPositionLocation: Int
//    val aColorLocation: Int

    //    val uMatrixLocation: Int
    val uColorLocation: Int

    init {
        aPositionLocation = glGetAttribLocation(mProgram, "a_Position")
//        aColorLocation = glGetAttribLocation(mProgram, "a_Color")
//
//        uMatrixLocation = glGetUniformLocation(mProgram, "u_Matrix")
        uColorLocation = glGetUniformLocation(mProgram, "u_Color")
    }

    fun setUniforms(matrix: FloatArray) {
//        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniform4fv(uColorLocation, 1, floatArrayOf(1f, 0f, 0f, 1f), 0)
    }
}