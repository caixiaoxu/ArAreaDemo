package com.kingo.kingoar.gles.programs

import android.content.Context
import android.opengl.GLES20.*

/**
 * 真实世界的着色器程序
 * @author Xuwl
 * @date 2021/11/4
 *
 */
class WorldShaderProgram(context: Context) : BaseShaderProgram(context,
    "vshader/world_vertex_shader.glsl",
    "fshader/world_fragment_shader.glsl") {

    val aPositionLocation: Int
    val aColorLocation: Int

    val uMatrixLocation: Int
//    private val uColorLocation: Int

    init {
        aPositionLocation = glGetAttribLocation(mProgram, "a_Position")
        aColorLocation = glGetAttribLocation(mProgram, "a_Color")

        uMatrixLocation = glGetUniformLocation(mProgram, "u_Matrix")
//        uColorLocation = glGetUniformLocation(mProgram, "u_Color")
    }

    fun setUniforms(matrix: FloatArray) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }

//    fun setUniforms(matrix: FloatArray, color: FloatArray) {
//        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
//        glUniform4fv(uColorLocation, 1, color, 0)
//    }
}