package com.kingo.kingoar.gles.programs

import android.content.Context
import android.opengl.GLES20.*

/**
 * 相机着色器程序
 * @author Xuwl
 * @date 2021/11/2
 *
 */
class CameraShaderProgram(context: Context) : BaseShaderProgram(context,
    "vshader/camera_vertex_shader.glsl",
    "fshader/camera_fragment_shader.glsl") {
    val aPositionLocation: Int
    val aCoordinatesLocation: Int

    private val uMatrixLocation: Int
    private val uTextureLocation: Int
    private val uTextureMatrixLocation: Int

    init {
        aPositionLocation = glGetAttribLocation(mProgram, "a_Position")
        aCoordinatesLocation = glGetAttribLocation(mProgram, "a_Coordinates")

        uMatrixLocation = glGetUniformLocation(mProgram, "u_Matrix")
        uTextureLocation = glGetUniformLocation(mProgram, "u_Texture")
        uTextureMatrixLocation = glGetUniformLocation(mProgram, "u_TextureMatrix")
    }

    fun setUniforms(
        matrix: FloatArray,
        textureMatrix: FloatArray,
        textureId: Int,
        textureIndex: Int,
    ) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, textureMatrix, 0)

        //绑定纹理
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(uTextureLocation, textureIndex)
    }
}