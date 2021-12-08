package com.kingo.kingoar.gles.params

/**
 * 绘制点封装
 * @author Xuwl
 * @date 2021/11/9
 *
 * @param curReal 当前定位
 * @param positions 绘制点的封装集合
 * @param centerLocation 图形的中心点
 */
class MultiPosition(
    var curReal: Location,
    val positions: ArrayList<ArrayList<Position>>,
    val centerLocation: ArrayList<Position>,
) {
}