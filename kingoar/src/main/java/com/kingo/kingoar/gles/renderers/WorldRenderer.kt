package com.kingo.kingoar.gles.renderers

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import android.opengl.Matrix
import com.kingo.kingoar.gles.helpers.*
import com.kingo.kingoar.gles.params.MultiPosition
import com.kingo.kingoar.gles.programs.CameraShaderProgram
import com.kingo.kingoar.gles.programs.WorldShaderProgram
import com.kingo.kingoar.gles.shapes.Camera
import com.kingo.kingoar.gles.shapes.base.Lines
import com.kingo.kingoar.gles.shapes.params.Geomtery
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 真实世界的Renderer
 * @author Xuwl
 * @date 2021/11/4
 *
 */
class WorldRenderer(
    private val mContext: Context,
    private val multiPosition: MultiPosition,
    val mSensorHelper: SensorHelper,
    val callback: () -> Unit,
) :
    BaseRenderer(), SurfaceTexture.OnFrameAvailableListener {
    private lateinit var mCamera: Camera
    private lateinit var mCameraShaderProgram: CameraShaderProgram

    private var mTextureId: Int = 0
    private var mSurfaceTexture: SurfaceTexture? = null

    private lateinit var mLines: Lines
    private lateinit var worldShaderProgram: WorldShaderProgram

    //归一化矩阵
    protected val mProjectionMatrix = FloatArray(16)

    //模型矩阵
    protected val mModelMatrix = FloatArray(16)

    //相机纹理矩阵
    private val mTextureMatrix = FloatArray(16)

    override fun initShapeAndProgram() {
        //相机
        mCamera = Camera()
        mCameraShaderProgram = CameraShaderProgram(mContext)

        val vertexData = FloatArray(multiPosition.positions.size * Lines.TOTAL_COMPONENT_COUNT * 2)
        multiPosition.positions.forEachIndexed { index, tagLoc ->
            //计算两点间的距离，单位米
            tagLoc.distance = PositionHelper.calculateDistanceMeters(
                multiPosition.curReal.latitude, multiPosition.curReal.longitude,
                tagLoc.real.latitude, tagLoc.real.longitude)
            //把经纬度转换成绘制坐标点
            tagLoc.coordinate = PositionHelper.convertGPStoPosition(
                multiPosition.curReal.latitude,
                multiPosition.curReal.longitude,
                multiPosition.curReal.altitude,
                tagLoc.real.latitude,
                tagLoc.real.longitude,
                tagLoc.real.altitude)
            require(null != tagLoc.coordinate) { "经纬度转换成屏幕坐标失败." }

            tagLoc.coordinate?.let { coor ->
                //画线的点
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index] = coor.x
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index + 1] = coor.y
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index + 2] = coor.z
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index + 3] = 1f
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index + 4] = 0f
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index + 5] = 0f
                vertexData[Lines.TOTAL_COMPONENT_COUNT * index + 6] = 1f

                //标出顶点
                vertexData[multiPosition.positions.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index] = coor.x
                vertexData[multiPosition.positions.size* Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 1] = coor.y
                vertexData[multiPosition.positions.size* Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 2] = coor.z
                vertexData[multiPosition.positions.size* Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 3] = 0f
                vertexData[multiPosition.positions.size* Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 4] = 0f
                vertexData[multiPosition.positions.size* Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 5] = 1f
                vertexData[multiPosition.positions.size* Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 6] = 1f
                //计算相对中心点的角度
                tagLoc.angle =
                    PositionHelper.calcAngleFaceToCamera(Geomtery.Point(0f, 0f, 0f), coor)
            }
        }
        //区域线
        mLines = Lines(vertexData)
        worldShaderProgram = WorldShaderProgram(mContext)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        initTextureId()
        //打开相机
        CameraHelper.startCamera(mSurfaceTexture!!)
    }

    /**
     * 生成TextureId并配置好SurfaceTexture
     */
    private fun initTextureId() {
        //创建TextureId
        val ids = IntArray(1)
        glGenTextures(1, ids, 0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, ids[0])
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_MIN_FILTER, GL_LINEAR.toFloat())
        glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_MAG_FILTER, GL_LINEAR.toFloat())
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        mTextureId = ids[0]
        mSurfaceTexture = SurfaceTexture(mTextureId)
        mSurfaceTexture?.setOnFrameAvailableListener(this)
    }

    override fun initMatrix(width: Int, height: Int) {
        super.initMatrix(width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
//        MatrixHelper.createBaseOrthoM(mProjectionMatrix, width, height)
        //透视投影
        MatrixHelper.perspectiveM(mProjectionMatrix, 45f, aspectRatio, 0.1f, 400f)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

//        drawCamera()
        drawPoint()
    }

    /**
     * 绘制相机纹理
     */
    private fun drawCamera() {
        // 更新纹理
        mSurfaceTexture?.updateTexImage()
        mSurfaceTexture?.getTransformMatrix(mTextureMatrix)

        //变换矩阵
        val mMatrix = FloatArray(16)
        Matrix.setIdentityM(mMatrix, 0)
        Matrix.rotateM(mMatrix, 0, 180F, 0F, 0F, 1F)
        Matrix.rotateM(mMatrix, 0, 180F, 0F, 1F, 0F)

        mCameraShaderProgram.useProgram()
        mCameraShaderProgram.setUniforms(mMatrix, mTextureMatrix, mTextureId, 0)
        mCamera.bindData(mCameraShaderProgram.aPositionLocation,
            mCameraShaderProgram.aCoordinatesLocation)
        mCamera.draw()
    }

    /**
     * 绘制点
     * 计算坐标信息
     * 步骤
     *      1、计算两点间的距离，单位米
     *      2、把经纬度转换成绘制坐标点
     *      3、根据绘制坐标点计算角度
     */
    private fun drawPoint() {
        //设备旋转矩阵
        val rotationMatrix = mSensorHelper.getRotationMatrix()
        val rotationProjectionMatrix = FloatArray(16)
        Matrix.multiplyMM(rotationProjectionMatrix, 0, mProjectionMatrix, 0, rotationMatrix, 0)

        val nearestPosition = multiPosition.getNearestPosition()
        //模型矩阵移动位置
        Matrix.setIdentityM(mModelMatrix, 0)
//        nearestPosition?.angle?.x?.let {
//            Matrix.rotateM(mModelMatrix, 0, it, 1f, 0f, 0f)
//            LogHelper.logI("旋转角度:x->$it")
//        }
//        nearestPosition?.angle?.y?.let {
//            Matrix.rotateM(mModelMatrix, 0, it, 0f, 1f, 0f)
//            LogHelper.logI("旋转角度:y->$it")
//        }
//        nearestPosition?.angle?.z?.let {
//            Matrix.rotateM(mModelMatrix, 0, it, 0f, 0f, 1f)
//            LogHelper.logI("旋转角度:z->$it")
//        }
        val viewModelProjectionMatrix = FloatArray(16)
        Matrix.multiplyMM(viewModelProjectionMatrix, 0,
            rotationProjectionMatrix, 0, mModelMatrix, 0)

        worldShaderProgram.useProgram()
        worldShaderProgram.setUniforms(rotationProjectionMatrix)
        mLines.bindData(worldShaderProgram.aPositionLocation, worldShaderProgram.aColorLocation)
        mLines.draw()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        callback.invoke()
    }
}