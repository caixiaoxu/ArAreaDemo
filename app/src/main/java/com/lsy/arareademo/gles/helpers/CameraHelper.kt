package com.lsy.arareademo.gles.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.*
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import java.util.*


/**
 * 相机操作工具类
 * @author Xuwl
 * @date 2021/11/1
 *
 */
object CameraHelper {
    private val TAG = "Camera"

    /**
     * 开启相机
     * @param surfaceTexture
     */
    fun startCamera(surfaceTexture: SurfaceTexture) {
        val cameraInfo = Camera.CameraInfo()
        val cameraCount = Camera.getNumberOfCameras()
        for (i in 0 until cameraCount) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                val mCamera = Camera.open(i)
                mCamera.setPreviewTexture(surfaceTexture)

                //设置分辨率
                val parameters = mCamera.parameters
                parameters.setPreviewSize(1280, 720)
                mCamera.parameters = parameters

                //开始预览
                mCamera.startPreview()
                return
            }
        }
    }
}