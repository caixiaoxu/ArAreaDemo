package com.kingo.kingoar.gles.fragments

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kingo.kingoar.R
import com.kingo.kingoar.gles.helpers.LogHelper
import com.kingo.kingoar.gles.helpers.SensorHelper
import com.kingo.kingoar.gles.params.Location
import com.kingo.kingoar.gles.params.MultiPosition
import com.kingo.kingoar.gles.params.Position
import com.kingo.kingoar.gles.renderers.WorldRenderer
import com.kingo.kingoar.gles.views.BaseGLSurfaceView
import java.util.ArrayList
import kotlin.math.round

/**
 * Ar展示界面
 * 功能：
 *      1、接收坐标数据转换成OpenGL数据
 *      2、加载GLSurfaceView，并初始化
 *      3、开启传感器，接收传感器数组并通过GLSUrfaceView刷新界面
 *
 * @author Xuwl
 * @date 2021/11/1
 *
 */
class ArAreaFragment : Fragment() {

    companion object {
        const val PARAM_CURLOC = "param_curLoc"
        const val PARAM_TAGLOCS = "param_tagLocs"
    }

    private lateinit var tvAzimuth: TextView
    private lateinit var glSurfaceView: BaseGLSurfaceView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_ar_area, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvAzimuth = view.findViewById(R.id.tv_azimuth)
        glSurfaceView = view.findViewById(R.id.glsurfaceview)

        //是否支持OpenGl Es 2.0
        if (null != context && (context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.reqGlEsVersion >= 0x2000) {
            val curLoc: Location? = arguments?.getParcelable(PARAM_CURLOC)
            val tags: ArrayList<Location>? = arguments?.getParcelableArrayList(PARAM_TAGLOCS)
            val tagLocs = ArrayList<Position>()
            tags?.forEach {
                tagLocs.add(Position(it))
            }

            if (null != curLoc && tagLocs.size > 0) {
                glSurfaceView.setEGLContextClientVersion(2)
//            glSurfaceView.setRenderer(NormalRenderer(requireContext()))

                val multiPosition = MultiPosition(curLoc, tagLocs)
                glSurfaceView.setRenderer(WorldRenderer(requireContext(),
                    multiPosition, startSensor()) { refreshRenderer() })
                glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }
        }
    }

    /**
     * 开启传感器
     */
    private fun startSensor(): SensorHelper {
        return SensorHelper.Builder(requireContext(), this) {
            showSensorInfo(it)
            refreshRenderer()
        }.run {
            addSensors(listOf(SENSOR_ACCELEROMETER, SENSOR_INCLINATION, SENSOR_MAGNETIC))
            build()
        }
    }

    private fun showSensorInfo(rotationMatrix: FloatArray) {
        //设备旋转矩阵
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
//        LogHelper.logE("方位角：${Math.toDegrees(orientationAngles[0].toDouble())}," +
//                "倾侧角：${Math.toDegrees(orientationAngles[1].toDouble())}," +
//                "俯仰角：${Math.toDegrees(orientationAngles[2].toDouble())}")
        val sb =
            SpannableStringBuilder("方位角：${round(Math.toDegrees(orientationAngles[0].toDouble()) * 10).toFloat() / 10}")
        sb.setSpan(ForegroundColorSpan(Color.WHITE), 0, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        sb.setSpan(ForegroundColorSpan(Color.RED), 4, sb.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tvAzimuth.text = sb
    }

    private fun refreshRenderer() {
        glSurfaceView.requestRender()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}