package com.lsy.arareademo.gles.helpers

import android.opengl.Matrix
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

/**
 * @author Xuwl
 * @date 2021/11/4
 *
 */
object MatrixHelper {
    /**
     * 创建基础的正交投影
     * @param matrix 矩阵变量
     * @param width 窗口的宽
     * @param height 窗口的高
     */
    fun createBaseOrthoM(matrix: FloatArray, width: Int, height: Int) {
        //计算比例，(宽高)最大值/(宽高)最小值
        val aspectRatio = max(width, height) / min(width, height).toFloat()
        if (width > height) {
            //横屏幕，调整x
            Matrix.orthoM(matrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            //竖屏，调整y
            Matrix.orthoM(matrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    /**
     * 透视投影
     * @param matrix 矩阵
     * @param yFovInDegrees 视角(角度)
     * @param aspect 屏幕宽高比
     * @param near 距离最近距离
     * @param far 距离最远距离
     */
    fun perspectiveM(
        matrix: FloatArray,
        yFovInDegrees: Float, aspect: Float,
        near: Float, far: Float,
    ) {
        //计算焦距
        val angleInRadians = yFovInDegrees * Math.PI / 180.0
        val a = (1.0 / tan(angleInRadians / 2.0)).toFloat()

        matrix[0] = a / aspect
        matrix[1] = 0f
        matrix[2] = 0f
        matrix[3] = 0f

        matrix[4] = 0f
        matrix[5] = a
        matrix[6] = 0f
        matrix[7] = 0f

        matrix[8] = 0f
        matrix[9] = 0f
        matrix[10] = -((far + near) / (far - near))
        matrix[11] = -1f

        matrix[12] = 0f
        matrix[13] = 0f
        matrix[14] = -((2f * far * near) / (far - near))
        matrix[15] = 0f
    }
}