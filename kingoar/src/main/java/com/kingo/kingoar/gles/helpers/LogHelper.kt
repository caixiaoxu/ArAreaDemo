package com.kingo.kingoar.gles.helpers

import android.util.Log
import com.kingo.kingoar.BuildConfig

/**
 * 打印异常日志工具类
 * @author Xuwl
 * @date 2021/10/29
 *
 */
object LogHelper {
    var ON = BuildConfig.DEBUG
    private val TAG = "OpenGL ES"

    /**
     * 调试日志
     * @param msg 调试信息
     */
    fun logD(msg: String) {
        logD(TAG, msg)
    }

    /**
     * 调试日志
     * @param tag 标签
     * @param msg 调试信息
     */
    fun logD(tag: String, msg: String) {
        if (ON) Log.d(tag, msg)
    }

    /**
     * 信息日志
     * @param msg 信息
     */
    fun logI(msg: String) {
        logI(TAG, msg)
    }

    /**
     * 信息日志
     * @param tag 标签
     * @param msg 信息
     */
    fun logI(tag: String, msg: String) {
        if (ON) Log.i(tag, msg)
    }

    /**
     * 警告日志
     * @param msg 警告信息
     */
    fun logW(msg: String) {
        logW(TAG, msg)
    }

    /**
     * 警告日志
     * @param tag 标签
     * @param msg 警告信息
     */
    fun logW(tag: String, msg: String) {
        if (ON) Log.w(tag, msg)
    }


    /**
     * 错误日志
     * @param msg 错误信息
     */
    fun logE(msg: String) {
        logE(TAG, msg)
    }

    /**
     * 错误日志
     * @param tag 标签
     * @param msg 错误信息
     */
    fun logE(tag: String, msg: String) {
        if (ON) Log.e(tag, msg)
    }
}