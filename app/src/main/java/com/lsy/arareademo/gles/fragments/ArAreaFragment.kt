package com.lsy.arareademo.gles.fragments

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
import com.lsy.arareademo.R
import com.lsy.arareademo.gles.helpers.LogHelper
import com.lsy.arareademo.gles.helpers.SensorHelper
import com.lsy.arareademo.gles.params.Location
import com.lsy.arareademo.gles.params.MultiPosition
import com.lsy.arareademo.gles.params.Position
import com.lsy.arareademo.gles.renderers.NormalRenderer
import com.lsy.arareademo.gles.renderers.WorldRenderer
import com.lsy.arareademo.gles.shapes.params.Geomtery
import com.lsy.arareademo.gles.views.BaseGLSurfaceView
import kotlin.math.ceil
import kotlin.math.round

/**
 * @author Xuwl
 * @date 2021/11/1
 *
 */
class ArAreaFragment : Fragment() {

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
            glSurfaceView.setEGLContextClientVersion(2)
//            glSurfaceView.setRenderer(NormalRenderer(requireContext()))
            val multiPosition = MultiPosition(Location(30.275126, 119.990152, 1.0),
                arrayListOf(
                    Position(Location(30.275394, 119.99076, 0.0)),
                    Position(Location(30.275609, 119.991661, 0.0)),
                    Position(Location(30.274686, 119.991645, 0.0)),
                    Position(Location(30.274696, 119.990837, 0.0)),
                )
            )
            glSurfaceView.setRenderer(WorldRenderer(requireContext(),
                multiPosition, startSensor()) { refreshRenderer() })
            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
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
        LogHelper.logE("方位角：${Math.toDegrees(orientationAngles[0].toDouble())}," +
                "倾侧角：${Math.toDegrees(orientationAngles[1].toDouble())}," +
                "俯仰角：${Math.toDegrees(orientationAngles[2].toDouble())}")
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