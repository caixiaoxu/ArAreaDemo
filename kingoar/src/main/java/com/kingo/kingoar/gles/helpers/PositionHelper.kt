package com.kingo.kingoar.gles.helpers

import com.kingo.kingoar.gles.shapes.params.Geomtery
import kotlin.math.*

/**
 * 计算坐标工具
 * @author Xuwl
 * @date 2021/11/8
 *
 */
object PositionHelper {
    const val EARTH_RADIUS = 6378137.0 // m

    private fun rad(d: Double): Double {
        return d * Math.PI / 180.0
    }

    /**
     * 计算两个经纬度之间的距离（Haversine公式）
     * @param aLat 纬度1
     * @param aLon 经度1
     * @param bLat 纬度2
     * @param bLon 经度2
     * @return 距离，单位米,保留两位小数
     */
    fun calculateDistanceMeters(aLat: Double, aLon: Double, bLat: Double, bLon: Double): Double {
        val radLat1 = rad(aLat)
        val radLat2 = rad(bLat)
        val a = radLat1 - radLat2
        val b = rad(aLon) - rad(bLon)
        var s = 2 * asin(sqrt(sin(a / 2).pow(2) + cos(radLat1) * cos(radLat2) * sin(b / 2).pow(2)))
        return round(s * EARTH_RADIUS * 100) / 100
    }

    const val METERS_TO_GEOPOINT = 107817.51838439942
    const val DEFAULT_DISTANCE_FACTOR = 2
    private fun fastConversionGeopointsToMeters(curGeo: Double, tagGeo: Double): Double {
        return (tagGeo - curGeo) * METERS_TO_GEOPOINT / DEFAULT_DISTANCE_FACTOR
    }

    /**
     * 把经纬度坐标转换成绘制坐标
     * @param curLat 当前位置纬度
     * @param curLon 当前位置经度
     * @param curAlt 当前位置海拨
     * @param tagLat 目标位置纬度
     * @param tagLon 目标位置纬度
     * @param tagAlt 当目标位置海拨
     * @return 绘制坐标点
     */
    fun convertGPStoPosition(
        curLat: Double, curLon: Double, curAlt: Double,
        tagLat: Double, tagLon: Double, tagAlt: Double,
    ): Geomtery.Point {
        val x = fastConversionGeopointsToMeters(curLon, tagLon).toFloat()
        val y = fastConversionGeopointsToMeters(curLat, tagLat).toFloat()
//        val z = fastConversionGeopointsToMeters(curAlt, tagAlt).toFloat()
        val z = (tagAlt - curAlt).toFloat()
//        val z = -1f
        LogHelper.logI("绘制点:$x,$y,$z")
        return Geomtery.Point(x, y, z)
    }

    /**
     * 计算点2相对于点1的角度
     * @param p1 点1
     * @param p2 点2
     */
    fun calcAngleFaceToCamera(p1: Geomtery.Point, p2: Geomtery.Point): Geomtery.Angle {
        var x = Math.toDegrees(atan2(p2.y - p1.y, p2.z - p1.z).toDouble()).toFloat()
        var y = Math.toDegrees(atan2(p2.x - p1.x, p2.z - p1.z).toDouble()).toFloat()
        var z = Math.toDegrees(atan2(p2.x - p1.x, p2.y - p1.y).toDouble()).toFloat()

//        if (((p1.x - p2.x) < 0 && (p1.y - p2.y) < 0) || ((p1.x - p2.x) >= 0 && (p1.y - p2.y) > 0)) {
//            y += 30
//        } else {
//            y -= 30
//        }
        return Geomtery.Angle(x, y, z)
    }
}