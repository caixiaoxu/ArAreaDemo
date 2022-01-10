package com.kingo.kingoar.gles.fragments

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.fragment.app.Fragment
import com.kingo.kingoar.R
import com.kingo.kingoar.gles.helpers.SensorHelper
import com.kingo.kingoar.gles.listeners.RendererTaskListener
import com.kingo.kingoar.gles.params.Location
import com.kingo.kingoar.gles.params.MultiPosition
import com.kingo.kingoar.gles.params.Position
import com.kingo.kingoar.gles.renderers.WorldRenderer
import com.kingo.kingoar.gles.views.BaseGLSurfaceView
import java.util.*
import kotlin.collections.ArrayList

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
        const val PARAM_CENTERLOCS = "param_centerLocs"
        const val PARAM_TAGLOCS = "param_tagLocs"
        const val PARAM_IS_OPEN_CAMERA = "param_open_camera"
        const val PARAM_SHOW_ALTITUDE_CONTROL = "param_show_altitude_control"
    }

    private lateinit var flAllViews: FrameLayout
    private lateinit var glSurfaceView: BaseGLSurfaceView
    private lateinit var mSeekBar: AppCompatSeekBar
    private lateinit var mRlSeekBar: RelativeLayout
    private lateinit var mTvCurHigh: TextView
    private var worldRenderer: WorldRenderer? = null
    private var curLoc: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_ar_area, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flAllViews = view.findViewById(R.id.fl_AllViews)
        glSurfaceView = view.findViewById(R.id.glsurfaceview)
        mRlSeekBar = view.findViewById(R.id.rl_seekBar)
        mSeekBar = view.findViewById(R.id.sb_altitude)
        mTvCurHigh = view.findViewById(R.id.tv_current_high)
        glSurfaceView.setZOrderMediaOverlay(true)

        val showAltitudeControl = arguments?.getBoolean(PARAM_SHOW_ALTITUDE_CONTROL, false) ?: false
        if (showAltitudeControl) {
            mRlSeekBar.visibility = View.VISIBLE
            mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    mTvCurHigh.text = "当前位置：${progress}M"
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
            val centerLoc:ArrayList<Location>? = arguments?.getParcelableArrayList(PARAM_CENTERLOCS)
            val tags: ArrayList<ArrayList<Location>>? =
                arguments?.getSerializable(PARAM_TAGLOCS) as? ArrayList<ArrayList<Location>>
            val isOpenCamera: Boolean = arguments?.getBoolean(PARAM_IS_OPEN_CAMERA, true) ?: true

            val tagLists = ArrayList<ArrayList<Position>>()
            tags?.forEach {list->
                val tagLocs = ArrayList<Position>()
                list.forEach {
                    tagLocs.add(Position(it))
                }
                tagLists.add(tagLocs)
            }

            val centerList = ArrayList<Position>()
            centerLoc?.forEach {
                centerList.add(Position(it))
            }

            if (null != curLoc && null != centerLoc && tagLists.size > 0) {
                glSurfaceView.setEGLContextClientVersion(2)
//                glSurfaceView.setRenderer(NormalRenderer(requireContext()))

                if (0.0 == curLoc!!.altitude) {
                    curLoc!!.altitude = mSeekBar.progress.toDouble()
                } else {
                    mSeekBar.progress = curLoc!!.altitude.toInt()
                }
                val multiPosition = MultiPosition(curLoc!!, tagLists, centerList)

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

    /**
     * 刷新OpenGL ES
     */
    private fun refreshRenderer() {
        glSurfaceView.requestRender()
    }

    /**
     * 更新当前的位置信息（模拟使用）
     * @param latitude 纬度
     * @param longitude 经度
     */
    fun updateCurLocation() {
        curLoc?.let {
            it.longitude += 0.0001
            worldRenderer?.changeCurLocation(it)
            refreshRenderer()
        }
    }

    /**
     * 更新当前的位置信息
     * @param latitude 纬度
     * @param longitude 经度
     */
    fun updateCurLocation(latitude: Double, longitude: Double) {
        curLoc?.let {
            it.latitude = latitude
            it.longitude = longitude
            worldRenderer?.changeCurLocation(it)
            refreshRenderer()
        }
    }

    /**
     * 更新当前的位置信息
     * @param latitude 纬度
     * @param longitude 经度
     * @param altitude 海拔
     */
    fun updateCurLocation(latitude: Double, longitude: Double, altitude: Double) {
        curLoc?.let {
            it.latitude = latitude
            it.longitude = longitude
            it.altitude = altitude
            worldRenderer?.changeCurLocation(it)
            refreshRenderer()
        }
    }

    /**
     * 获取绘制的图片
     */
    fun getRenderDrawBitmap(callback: RendererTaskListener) {
        worldRenderer?.getDrawCacheBitmap(callback)
        refreshRenderer()
    }

    /**
     * 显示AR控件
     */
    fun startRenderingAR() {
        try {
            flAllViews.visibility = View.VISIBLE
            glSurfaceView.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 隐藏显示AR控件
     */
    fun stopRenderingAR() {
        try {
            flAllViews.visibility = View.GONE
            glSurfaceView.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        worldRenderer?.onDestory()
    }
}