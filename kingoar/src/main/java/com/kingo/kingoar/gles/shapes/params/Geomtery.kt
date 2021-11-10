package com.kingo.kingoar.gles.shapes.params

/**
 * @author Xuwl
 * @date 2021/11/8
 *
 */
class Geomtery {

    /**
     * 坐标点的数据
     */
    class Point(val x: Float, val y: Float, val z: Float) {
        /**
         * 向x平移
         * @param distance 距离
         * @return 返回移动后的新对象
         */
        fun translatX(distance: Float): Point = translation(distance, 0f, 0f)

        /**
         * 向Y平移
         * @param distance 距离
         * @return 返回移动后的新对象
         */
        fun translatY(distance: Float): Point = translation(0f, distance, 0f)

        /**
         * 向z平移
         * @param distance 距离
         * @return 返回移动后的新对象
         */
        fun translatZ(distance: Float): Point = translation(0f, 0f, distance)

        /**
         * 平移
         * @param dx x移动距离
         * @param dy y移动距离
         * @param dz z移动距离
         * @return 返回移动后的新对象
         */
        fun translation(dx: Float, dy: Float, dz: Float): Point = Point(x + dx, y + dy, z + dz)
    }

    class Angle(val x: Float, val y: Float, val z: Float) {

    }
}