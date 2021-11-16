package com.kingo.kingoar.gles.helpers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.*
import kotlin.math.abs

/**
 * 传感器工具类
 * @author Xuwl
 * @date 2021/11/3
 *
 */
class SensorHelper private constructor(
    context: Context,
    owner: LifecycleOwner,
    private val callback: (matrix: FloatArray) -> Unit,
) : SensorEventListener {
    private val TAG = "Sensor"

    private val ALPHA = 0.03f


    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensors: ArrayList<Sensor> = ArrayList(10)

    private val sInclination = FloatArray(16)
    private val mAccelerometerValues = FloatArray(3)
    private val mMagneticValues = FloatArray(3)
    private val mRotationMatrix = FloatArray(16)
    private val mRemappedRotationMatrix = FloatArray(16)

    init {
        //绑定生命周期
        owner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> onResume()
                    Lifecycle.Event.ON_PAUSE -> onPause()
                }
            }
        })
    }

    /**
     * 获取旋转矩阵
     */
    fun getRotationMatrix(): FloatArray {
        //旋转矩阵
        SensorManager.getRotationMatrix(mRotationMatrix,
            sInclination, mAccelerometerValues, mMagneticValues)

        SensorManager.remapCoordinateSystem(mRotationMatrix,
            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRemappedRotationMatrix)
        return mRemappedRotationMatrix
    }

    /**
     * 检查当前类型传感器是否在传感器列表中
     * @param sensor 传感器
     */
    private fun addSensorToList(sensor: Sensor?): Boolean {
        return sensor?.let {
            if (10 == sensors.size) {
                LogHelper.logE(TAG, "most have 10 number of open list.")
                return false
            }

            //循环判断是否有相同传感器
            sensors.forEach { s ->
                if (it.type == s.type) {
                    LogHelper.logE(TAG, "don't have same sensor in open list.")
                    return false
                }
            }
            //没有就加入
            sensors.add(sensor)
            return true
        } ?: false
    }

    /**
     * 地磁倾角
     */
    fun getInclination() {
        SensorManager.getInclination(sInclination)
    }

    /**
     * 开启加速传感器
     */
    fun openAccelerometerSensor(): Boolean {
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return addSensorToList(sensor)
    }

    /**
     * 开启地磁传感器
     */
    fun openMagneticSensor(): Boolean {
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        return addSensorToList(sensor)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {//加速计传感器
//                    LogHelper.logI(TAG, "刷新加速计")
                    filter(event.values, mAccelerometerValues)
                    callback.invoke(getRotationMatrix())
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {//地磁传感器
//                    LogHelper.logI(TAG, "刷新地磁")
                    filter(event.values, mMagneticValues)
                    callback.invoke(getRotationMatrix())
                }
                else -> {
                }
            }
        }
    }

    private var count = 0

    /**
     * 过滤数据
     * @param input 当前输入值
     * @param prev 之前输入值
     */
    private fun filter(input: FloatArray?, prev: FloatArray?): FloatArray {
        if (input == null || prev == null)
            throw NullPointerException("input and prev float arrays must be non-NULL")
        require(input.size == prev.size) { "input and prev must be the same length" }

        if (abs(input[0] - prev[0]) > 0.03) {
            count++
        } else {
            count = 0
        }

        if (0 == count || count >= 3) {
//            val builder1 = StringBuilder()
//            val builder2 = StringBuilder()
            for (i in input.indices) {
//                builder1.append(input[i]).append(",")
//                builder2.append(prev[i]).append(",")
                prev[i] = prev[i] + ALPHA * (input[i] - prev[i])
            }
//            LogHelper.logE("当前输入:$builder1")
//        LogHelper.logE("之前的输入:$builder2")
        }
        return prev
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * onResume 开启监听
     */
    fun onResume() {
        sensors.forEach { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /**
     * onPause 关闭监听
     */
    fun onPause() {
        sensorManager.unregisterListener(this)
    }

    class Builder(
        val context: Context,
        val owner: LifecycleOwner,
        val callback: (matrix: FloatArray) -> Unit,
    ) {
        val SENSOR_ACCELEROMETER = 0
        val SENSOR_INCLINATION = 1
        val SENSOR_MAGNETIC = 2

        private val mSensorTypes = ArrayList<Int>(10)

        fun addSensors(types: List<Int>) {
            mSensorTypes.addAll(types)
        }

        fun build(): SensorHelper {
            return SensorHelper(context, owner, callback).apply {
                mSensorTypes.forEach {
                    when (it) {
                        SENSOR_ACCELEROMETER -> openAccelerometerSensor()
                        SENSOR_INCLINATION -> getInclination()
                        SENSOR_MAGNETIC -> openMagneticSensor()
                    }
                }
            }
        }
    }
}