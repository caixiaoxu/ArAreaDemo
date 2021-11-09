package com.lsy.arareademo.gles.params

import com.lsy.arareademo.gles.shapes.params.Geomtery

/**
 * OpenGl 绘制点的信息封装
 * @author Xuwl
 * @date 2021/11/9
 * @param real 现实中的经纬度
 */
data class Position(val real: Location) {
    var distance: Double = 0.0
    var coordinate: Geomtery.Point? = null
    var angle: Geomtery.Angle? = null
}