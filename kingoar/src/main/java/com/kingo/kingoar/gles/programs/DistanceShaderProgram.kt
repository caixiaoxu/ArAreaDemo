package com.kingo.kingoar.gles.programs

import android.content.Context
import android.opengl.GLES20

/**
 * @author Xuwl
 * @date 2021/12/3
 *
 */
class DistanceShaderProgram(context: Context) : BaseShaderProgram(context,
    "vshader/distance_vertex_shader.glsl",
    "fshader/distance_fragment_shader.glsl"
) {
    val uMatrixLocation: Int
    val uTextureUnitLocation: Int

    val aPositionLocation: Int
    val aTextureCoordinatesLocation: Int

    init {
        //获取Uniform变量
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_Matrix")
        uTextureUnitLocation = GLES20.glGetUniformLocation(mProgram, "u_TextureUnit")

        //获取attribute变量
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "a_Position")
        aTextureCoordinatesLocation = GLES20.glGetAttribLocation(mProgram, "a_TextureCoordinates")
    }

    fun setUniforms(
        matrix: FloatArray,
        textureId: Int,
        textureIndex: Int,
    ) {
        //矩阵参数
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureIndex)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureUnitLocation, textureIndex)
    }
}