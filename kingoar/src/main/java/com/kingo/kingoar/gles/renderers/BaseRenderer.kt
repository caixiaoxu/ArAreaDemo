package com.kingo.kingoar.gles.renderers

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.kingo.kingoar.gles.helpers.LogHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Xuwl
 * @date 2021/10/29
 *
 */
abstract class BaseRenderer : GLSurfaceView.Renderer {

    /**
     * 背景色
     */
    private val mClearColor: FloatArray by lazy { initClearColor() }

    /**
     * 方便子类重写
     */
    protected open fun initClearColor(): FloatArray =
        floatArrayOf(0f, 0f, 0f, 1f).apply { LogHelper.logI("Init Clear Color.") }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        LogHelper.logI("onSurfaceCreated.")
        //背景色
        glClearColor(mClearColor[0], mClearColor[1], mClearColor[2], mClearColor[3])

        initShapeAndProgram()
    }

    /**
     * 初始化形状和着色器程序
     */
    protected abstract fun initShapeAndProgram()

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        LogHelper.logI("onSurfaceChanged.")
        //窗口大小
        glViewport(0, 0, width, height)
        //初始化矩阵
        initMatrix(width, height)
    }

    /**
     * 初始化基础矩阵
     * @param width 窗口的宽
     * @param height 窗口的高
     */
    protected open fun initMatrix(width: Int, height: Int) {
        LogHelper.logI("initMatrix.")
    }

    override fun onDrawFrame(gl: GL10?) {
        LogHelper.logI("onDrawFrame.")
        //清屏
        glClear(GL_COLOR_BUFFER_BIT)
    }
}