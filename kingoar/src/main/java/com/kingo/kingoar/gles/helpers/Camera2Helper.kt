package com.kingo.kingoar.gles.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import androidx.core.app.ActivityCompat


/**
 * Camera2操作工具类
 * 步骤：
 *      1、获取CameraManager
 *      2、获取相机信息
 *      3、初始化ImageReader 暂缺
 *      4、打开相机设备
 *      5、创建Capture会话
 *      6、创建CaptureRequest
 *      7、预览
 *      8、拍照 暂缺
 *      9、关闭
 * @author Xuwl
 * @date 2021/11/1
 *
 */
class Camera2Helper(private var mContext: Context) {
    private val TAG = "Camera2"

    //得到相机管理器
    private val cameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var mCameraCharacteristics: CameraCharacteristics? = null

    private val mHandler = Handler(Looper.getMainLooper())
    private val childHandler: Handler

    init {
        val handlerThread = HandlerThread("Camera2")
        handlerThread.start()
        childHandler = Handler(handlerThread.looper)
    }

    /**
     * 打开相机预览
     * @param texture 绘制对象
     */
    fun startCameraPreview(texture: SurfaceTexture) {
        startCameraPreview("${CameraCharacteristics.LENS_FACING_FRONT}", texture)
    }

    /**
     * 打开相机预览
     * @param texture 绘制对象
     */
    fun startCameraPreview(surface: Surface) {
        startCameraPreview("${CameraCharacteristics.LENS_FACING_FRONT}", surface)
    }

    /**
     * 打开相机预览
     * @param cameraId 镜头id
     * @param texture 绘制对象
     */
    fun startCameraPreview(cameraId: String, texture: SurfaceTexture) {
        startCameraPreview(cameraId, Surface(texture))
    }

    /**
     * 打开相机预览
     * @param cameraId 镜头id
     * @param surface 绘制对象
     */
    fun startCameraPreview(cameraId: String, surface: Surface) {
        //判断是否有权限
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) return
        mCameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
        //开启相机
        cameraManager.openCamera(cameraId, MyStateCallback(surface), mHandler)
    }

    /**
     * 计算角度
     */
    fun computeRelativeRotation(surfaceRotationDegrees: Int): Int {
        return mCameraCharacteristics?.let {
            val sensorOrientationDegrees = it.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
            val sign =
                if (it.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) 1 else -1
            return (sensorOrientationDegrees - surfaceRotationDegrees * sign + 360) % 360

        } ?: 0
    }

    /**
     * 相机的状态监听
     */
    inner class MyStateCallback(private val surface: Surface) : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            //开启预览
            createCameraPreviewSession(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }

        /**
         * 开始预览
         */
        private fun createCameraPreviewSession(cameraDevice: CameraDevice) {
            try {
                // 创建预览需要的CaptureRequest.Builder
                val previewRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                // 将SurfaceView的surface作为CaptureRequest.Builder的目标
                previewRequestBuilder.addTarget(surface)
                val targets = listOf(surface)
                cameraDevice.createCaptureSession(targets,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            if (null == cameraDevice) return
                            // 当摄像头已经准备好时，开始显示预览
                            try {
                                // 自动对焦
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                // 打开闪光灯
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON)
                                // 显示预览
                                val previewRequest: CaptureRequest = previewRequestBuilder.build()
                                session.setRepeatingRequest(previewRequest,
                                    null, childHandler)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    },
                    mHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}