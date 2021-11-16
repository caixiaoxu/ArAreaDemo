package com.kingo.kingoar.gles.helpers

import android.content.res.Resources
import android.opengl.GLES20.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

/**
 * 着色器工具类
 * 功能：
 *      1、从文件中读取着色器代码
 *      2、编译着色器
 *      3、与程序关联
 *      4、校验状态
 * @author Xuwl
 * @date 2021/10/29
 *
 */
object ShaderHelper {

    /**
     * 从资源文件中读取着色器
     * @param res 资源类
     * @param rId 文件名
     */
    fun readShaderFileFromResource(res: Resources, fname: String): String {
        LogHelper.logI("2.从资源文件中读取着色器")
        val body = StringBuilder()
        try {
            val bReader = BufferedReader(InputStreamReader(res.assets.open(fname)))
            var nextLine: String?
            while (null != bReader.readLine().also { nextLine = it }) {
                body.append(nextLine).append('\n')
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not open resource : $fname", e)
        } catch (nfe: FileNotFoundException) {
            throw RuntimeException("Could not found : $fname", nfe)
        }
        LogHelper.logI("3.读取完成")
        return body.toString()
    }

    /**
     * 编译顶点着色器
     * @param sourceCode 顶点着色器源码
     */
    fun compileVertexShaper(sourceCode: String): Int {
        LogHelper.logI("4.编译顶点着色器")
        return compileShaper(GL_VERTEX_SHADER, sourceCode).apply {
            LogHelper.logI("5.编译顶点着色器完成")
        }
    }


    /**
     * 编译片元着色器
     * @param sourceCode 片元着色器源码
     */
    fun compileFragmentShaper(sourceCode: String):Int{
        LogHelper.logI("6.编译片元着色器")
        return compileShaper(GL_FRAGMENT_SHADER, sourceCode).apply {
            LogHelper.logI("7.编译片元着色器完成")
        }
    }

    /**
     * 编译着色器
     * 步骤：
     *      1、创建着色器ID
     *      2、关联源码
     *      3、编译着色器
     *      4、检查编译状态，失败回收着色器，成功返回着色器ID
     * @param type 着色器类型(顶点着色器 GL_VERTEX_SHADER / 片元着色器 GL_FRAGMENT_SHADER)
     * @param sourceCode 着色器源码
     */
    private fun compileShaper(type: Int, sourceCode: String): Int {
        //根据着色器类型创建一个着色器ID
        val shaderId = glCreateShader(type)
        if (0 == shaderId) {
            LogHelper.logE("Could not create new shaper.")
            return 0
        }

        //关联源码
        glShaderSource(shaderId, sourceCode)
        //编译
        glCompileShader(shaderId)
        //检查编译状态
        val status = IntArray(1).also { arr -> checkShaderCompile(shaderId, arr) }
        //编译失败
        if (0 == status[0]) {
            LogHelper.logE("Compilation of shader failed.")
            printShaderInfo(shaderId, status[0])
            //回收着色器Id
            glDeleteShader(shaderId)
            return 0
        }
        return shaderId
    }

    /**
     * 检测着色器的编译状态
     * @param shaderId 着色器ID
     * @param status 返回状态
     */
    private fun checkShaderCompile(shaderId: Int, status: IntArray) {
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, status, 0)
    }

    /**
     * 打印着色器日志
     */
    private fun printShaderInfo(shaderId: Int, status: Int) {
        LogHelper.logE("Results of compiling source: $status \n-->${glGetShaderInfoLog(shaderId)}")
    }

    /**
     * 关联着色器和程序
     * 步骤：
     *      1、创建程序ID
     *      2、附上着色器并关联
     *      3、检查关联状态，失败回收程序，成功返回程序ID
     * @param vertexShaderId 顶点着色器ID
     * @param fragmentShaderId 片元着色器ID
     */
    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        LogHelper.logI("8.关联着色器和程序")
        if (0 == vertexShaderId || 0 == fragmentShaderId) {
            return 0
        }
        //创建程序ID
        val programId = glCreateProgram()
        if (0 == programId) {
            LogHelper.logE("Could not create new program")
            return 0
        }
        //附上着色器
        glAttachShader(programId, vertexShaderId)
        glAttachShader(programId, fragmentShaderId)
        glLinkProgram(programId)
        //检查编译状态
        val status = IntArray(1).also { arr -> checkProgramLink(programId, arr) }
        //编译失败
        if (0 == status[0]) {
            LogHelper.logE("Linking of program failed.")
            printProgramLinkInfo(programId, status[0])
            //回收程序
            glDeleteProgram(programId)
            return 0
        }
        LogHelper.logI("9.关联成功")
        return programId
    }

    /**
     * 检测着色器的编译状态
     * @param programId 程序ID
     * @param status 返回状态
     */
    private fun checkProgramLink(programId: Int, status: IntArray) {
        glGetProgramiv(programId, GL_LINK_STATUS, status, 0)
    }

    /**
     * 绑定程序
     * @param vertexShaper 顶点着色器源码
     * @param fragmentShaper 片元着色器源码
     */
    fun buildProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
        //编译着色器
        val vertexId = compileVertexShaper(vertexShaderSource)
        val fragmentId = compileFragmentShaper(fragmentShaderSource)
        //链接程序
        return linkProgram(vertexId, fragmentId).apply {
            validateProgram(this)
        }
    }

    /**
     * 程序是否有效
     * @param programId 程序ID
     */
    private fun validateProgram(programId: Int): Boolean {
        //校验程序
        glValidateProgram(programId)
        //校验程序状态
        val status = IntArray(1).also { arr ->
            glGetProgramiv(programId, GL_VALIDATE_STATUS, arr, 0)
        }
        //打印检验程序日志
        printProgramLinkInfo(programId, status[0])
        return status[0] != 0
    }

    /**
     * 打印创建程序日志
     * @param programId 程序ID
     * @param status 状态码
     */
    private fun printProgramLinkInfo(programId: Int, status: Int) {
        LogHelper.logE("Results of Linking program: $status \n-->${glGetProgramInfoLog(programId)}")
    }
}