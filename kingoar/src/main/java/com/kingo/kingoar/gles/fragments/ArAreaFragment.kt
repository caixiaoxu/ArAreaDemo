package com.kingo.kingoar.gles.fragments

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.fragment.app.Fragment
import com.kingo.kingoar.R
import com.kingo.kingoar.gles.helpers.SensorHelper
import com.kingo.kingoar.gles.params.Location
import com.kingo.kingoar.gles.params.MultiPosition
import com.kingo.kingoar.gles.params.Position
import com.kingo.kingoar.gles.renderers.WorldRenderer
import com.kingo.kingoar.gles.views.BaseGLSurfaceView
import java.util.*

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
        const val PARAM_ISOPENCAMERA = "param_open_camera"
        const val PARAM_SHOW_ALTITUDE_CONTROL = "param_show_altitude_control"
    }

    private lateinit var glSurfaceView: BaseGLSurfaceView
    private lateinit var mSeekBar: AppCompatSeekBar
    private var worldRenderer: WorldRenderer? = null
    private var curLoc: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_ar_area, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        glSurfaceView = view.findViewById(R.id.glsurfaceview)
        mSeekBar = view.findViewById(R.id.sb_altitude)
        glSurfaceView.setZOrderMediaOverlay(true)

        val showAltitudeControl = arguments?.getBoolean(PARAM_SHOW_ALTITUDE_CONTROL, false) ?: false
        if (showAltitudeControl) {
            mSeekBar.visibility = View.VISIBLE
            mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    curLoc?.let {
                        it.altitude = progress.toDouble()
                        worldRenderer?.changeCurLocation(it)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }

        //是否支持OpenGl Es 2.0
        if (null != context && (context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.reqGlEsVersion >= 0x2000) {
            curLoc = arguments?.getParcelable(PARAM_CURLOC)
            val tags: ArrayList<Location>? = arguments?.getParcelableArrayList(PARAM_TAGLOCS)
            val isOpenCamera: Boolean = arguments?.getBoolean(PARAM_ISOPENCAMERA, true) ?: true
            val tagLocs = ArrayList<Position>()
            tags?.forEach {
                tagLocs.add(Position(it))
            }

            if (null != curLoc && tagLocs.size > 0) {
                glSurfaceView.setEGLContextClientVersion(2)
//            glSurfaceView.setRenderer(NormalRenderer(requireContext()))

                if (0.0 == curLoc!!.altitude) {
                    curLoc!!.altitude = mSeekBar.progress.toDouble()
                } else {
                    mSeekBar.progress = curLoc!!.altitude.toInt()
                }
                val multiPosition = MultiPosition(curLoc!!, tagLocs)

                worldRenderer = WorldRenderer(requireContext(),
                    multiPosition, isOpenCamera, startSensor()) { refreshRenderer() }
                glSurfaceView.setRenderer(worldRenderer)
                glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }
        }
    }

    /**
     * 开启传感器
     */
    private fun startSensor(): SensorHelper {
        return SensorHelper.Builder(requireContext(), this) {
            refreshRenderer()
        }.run {
            addSensors(listOf(SENSOR_ACCELEROMETER, SENSOR_INCLINATION, SENSOR_MAGNETIC))
            build()
        }
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