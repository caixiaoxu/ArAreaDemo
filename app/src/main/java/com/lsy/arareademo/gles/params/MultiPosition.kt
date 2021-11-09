package com.lsy.arareademo.gles.params

/**
 * 绘制点封装
 * @author Xuwl
 * @date 2021/11/9
 *
 * @param curReal 当前定位
 * @param positions 绘制点的封装集合
 */
class MultiPosition(val curReal: Location, val positions: MutableList<Position>) {

    /**
     * 获取距离最近的点
     */
    fun getNearestPosition(): Position? {
        var nearest: Position? = null
        positions.forEach { p ->
            if (null == nearest || p.distance < nearest!!.distance) {
                nearest = p
            }
        }
        return nearest
    }
}