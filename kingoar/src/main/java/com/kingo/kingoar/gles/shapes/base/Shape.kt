package com.kingo.kingoar.gles.shapes.base

/**
 * 图形基类
 * @author Xuwl
 * @date 2021/11/4
 *
 */
abstract class Shape {

    /**
     * 绑定顶点数据
     * @param attribLocation 需要填充数据的EGL参数
     */
    abstract fun bindData(vararg attribLocation: Int)

    /**
     * 绘制
     */
    abstract fun draw()

}