package com.kingo.kingoar.gles.params

import com.kingo.kingoar.gles.shapes.params.Geomtery

/**
 * OpenGl 绘制点的信息封装
 * @author Xuwl
 * @date 2021/11/9
 * @param real 现实中的经纬度
 */
data class Position(val real: Location) {
    var distance: Double = 0.0//与相机所在位置的距离
    var coordinate: Geomtery.Point? = null//绘制坐标
    var angle: Geomtery.Angle? = null//相对中心点的角度
}