package com.lsy.arareademo.gles.programs

import android.content.Context
import android.opengl.GLES20.*
import com.lsy.arareademo.gles.helpers.LogHelper
import com.lsy.arareademo.gles.helpers.ShaderHelper

/**
 * 着色器程序基类(处理着色器的加载、编译、绑定到程序、使用程序)
 * @author Xuwl
 * @date 2021/10/29
 *
 */
abstract class BaseShaderProgram(
    context: Context,
    vertexShaderResourceName: String, fragmentShaderResourceName: String,
) {
    protected val mProgram: Int

    init {
        LogHelper.logI("1.初始化着色器程序")
        //加载着色器
        val vertexShaper =
            ShaderHelper.readShaderFileFromResource(context.resources, vertexShaderResourceName)
        val fragmentShaper =
            ShaderHelper.readShaderFileFromResource(context.resources, fragmentShaderResourceName)
        mProgram = ShaderHelper.buildProgram(vertexShaper, fragmentShaper)
    }

    /**
     * 使用程序
     */
    fun useProgram() {
        LogHelper.logI("10.使用着色器程序")
        glUseProgram(mProgram)
    }
}