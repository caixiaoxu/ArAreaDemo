package com.kingo.kingoar.gles.renderers

import android.content.Context
import android.graphics.*
import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import android.opengl.Matrix
import com.kingo.kingoar.R
import com.kingo.kingoar.gles.helpers.*
import com.kingo.kingoar.gles.listeners.RendererTaskListener
import com.kingo.kingoar.gles.params.Location
import com.kingo.kingoar.gles.params.MultiPosition
import com.kingo.kingoar.gles.params.Position
import com.kingo.kingoar.gles.programs.CameraShaderProgram
import com.kingo.kingoar.gles.programs.DistanceShaderProgram
import com.kingo.kingoar.gles.programs.WorldShaderProgram
import com.kingo.kingoar.gles.shapes.Camera
import com.kingo.kingoar.gles.shapes.DistancePrompt
import com.kingo.kingoar.gles.shapes.base.Lines
import com.kingo.kingoar.gles.shapes.params.Geomtery
import java.nio.IntBuffer
import java.util.concurrent.atomic.AtomicBoolean
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
    private val isOpenCamera: Boolean,
    val mSensorHelper: SensorHelper,
    val callback: () -> Unit,
) :
    BaseRenderer(), SurfaceTexture.OnFrameAvailableListener {
    private lateinit var mCamera2Helper: Camera2Helper
    private lateinit var mCamera: Camera
    private lateinit var mCameraShaderProgram: CameraShaderProgram

    private var mTextureId: Int = 0
    private var mSurfaceTexture: SurfaceTexture? = null

    private lateinit var mLines: ArrayList<Lines>
    private lateinit var worldShaderProgram: WorldShaderProgram

    private lateinit var mDistancePrompts: ArrayList<DistancePrompt>
    private lateinit var mDistanceShaderProgram: DistanceShaderProgram

    //归一化矩阵
    protected val mProjectionMatrix = FloatArray(16)

    //归一化矩阵
    protected val mProjectionMatrix1 = FloatArray(16)

    //模型矩阵
    protected val mModelMatrix = FloatArray(16)

    //相机矫正矩阵
    protected val mCameraMatrix = FloatArray(16)

    //相机纹理矩阵
    private val mTextureMatrix = FloatArray(16)

    override fun initClearColor(): FloatArray = floatArrayOf(0f, 0f, 0f, 0f)

    override fun initShapeAndProgram() {
        //相机
        mCamera = Camera()
        mCameraShaderProgram = CameraShaderProgram(mContext)

        //区域线
        initLines()
        worldShaderProgram = WorldShaderProgram(mContext)

        //距离显示
        initDistance()
        mDistanceShaderProgram = DistanceShaderProgram(mContext)
    }

    /**
     * 初始化多图斑线条
     */
    private fun initLines() {
        mLines = ArrayList(multiPosition.positions.size)
        multiPosition.positions.forEach {
            mLines.add(Lines(initLines(it)))
        }
    }

    /**
     * 计算线的坐标并创建线的对象
     */
    private fun initLines(tags: ArrayList<Position>): FloatArray {
        val vertexData = FloatArray(tags.size * Lines.TOTAL_COMPONENT_COUNT * 2)
        tags.forEachIndexed { index, tagLoc ->
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

            //顶点坐标
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
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index] =
                    coor.x
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 1] =
                    coor.y
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 2] =
                    coor.z
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 3] =
                    0f
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 4] =
                    0f
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 5] =
                    1f
                vertexData[tags.size * Lines.TOTAL_COMPONENT_COUNT + Lines.TOTAL_COMPONENT_COUNT * index + 6] =
                    1f
                //计算相对中心点的角度
                tagLoc.angle =
                    PositionHelper.calcAngleFaceToCamera(Geomtery.Point(0f, 0f, 0f), coor)
            }
        }
        return vertexData
    }

    /**
     * 初始化距离显示
     */
    private fun initDistance() {
        val bgBitmap = BitmapFactory.decodeResource(mContext.resources, R.mipmap.bg_tip)
        val iconBitmap = BitmapFactory.decodeResource(mContext.resources, R.mipmap.icon_distance)
        mDistancePrompts = ArrayList(multiPosition.centerLocation.size)
        multiPosition.centerLocation.forEach {
            initCenter(it)
            mDistancePrompts.add(DistancePrompt(it, bgBitmap, iconBitmap))
        }
    }

    private fun initCenter(position: Position) {
        position.apply {
            //计算两点间的距离，单位米
            distance = PositionHelper.calculateDistanceMeters(
                multiPosition.curReal.latitude, multiPosition.curReal.longitude,
                real.latitude, real.longitude)
            //把经纬度转换成绘制坐标点
            coordinate = PositionHelper.convertGPStoPosition(
                multiPosition.curReal.latitude,
                multiPosition.curReal.longitude,
                multiPosition.curReal.altitude,
                real.latitude,
                real.longitude,
                real.altitude)
            coordinate?.let {
                angle =
                    PositionHelper.calcAngleFaceToCamera(Geomtery.Point(0f, 0f, 0f), it)
            }
        }
        require(null != position.coordinate) { "经纬度转换成屏幕坐标失败." }
    }

    /**
     * 更新当前的位置信息（当前位置改变后，线的顶点坐标也需要重新计算）
     */
    fun changeCurLocation(curReal: Location) {
        multiPosition.curReal = curReal
        mLines.forEachIndexed { index, line ->
            line.updateVertexData(initLines(multiPosition.positions[index]))
        }
        multiPosition.centerLocation.forEach {
            initCenter(it)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        if (isOpenCamera) {
            initTextureId()
            //打开相机
            mSurfaceTexture?.let {
                //CameraHelper.startCamera(mSurfaceTexture!!)
                mCamera2Helper = Camera2Helper(mContext)
                mCamera2Helper.startCameraPreview(it)
            }
        }
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

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        mSurfaceTexture?.setDefaultBufferSize(width, height)
    }

    override fun initMatrix(width: Int, height: Int) {
        super.initMatrix(width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        MatrixHelper.createBaseOrthoM(mProjectionMatrix1, width, height)
        //透视投影
        MatrixHelper.perspectiveM(mProjectionMatrix, 45f, aspectRatio, 0.1f, 400f)

        //相机矩阵
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.scaleM(mCameraMatrix, 0, -1f, 1f, 1f)
        Matrix.rotateM(mCameraMatrix, 0, 90f, 0f, 0f, 1f)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

//        if (isOpenCamera) {
//            drawCamera()
//        }
        drawPoint()
        drawDistancePrompt()

        saveToBitmap()
    }

    /**
     * 绘制相机纹理
     */
    private fun drawCamera() {
        // 更新纹理
        mSurfaceTexture?.updateTexImage()
        mSurfaceTexture?.getTransformMatrix(mTextureMatrix)

        mCameraShaderProgram.useProgram()
        mCameraShaderProgram.setUniforms(mCameraMatrix, mTextureMatrix, mTextureId, 0)
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

        worldShaderProgram.useProgram()
        worldShaderProgram.setUniforms(rotationProjectionMatrix)
        mLines.forEach { line ->
            line.bindData(worldShaderProgram.aPositionLocation, worldShaderProgram.aColorLocation)
            line.draw()
        }
    }

    /**
     * 绘制距离
     */
    private fun drawDistancePrompt() {
        //设备旋转矩阵
        val rotationMatrix = mSensorHelper.getRotationMatrix()
        val rotationProjectionMatrix = FloatArray(16)
        Matrix.multiplyMM(rotationProjectionMatrix, 0, mProjectionMatrix, 0, rotationMatrix, 0)
        //调整位置和角度
        val modelMatrix3 = FloatArray(16)
        val modelMatrix4 = FloatArray(16)

        mDistanceShaderProgram.useProgram()
        mDistancePrompts.forEachIndexed { index, prompt ->
            val centerLocation = multiPosition.centerLocation[index]
            val coordinate = centerLocation.coordinate
            coordinate?.let {
                Matrix.setIdentityM(modelMatrix4, 0)
                Matrix.translateM(modelMatrix4, 0, coordinate.x, coordinate.y, coordinate.z)
                centerLocation.angle?.let { angle ->
                    Matrix.rotateM(modelMatrix4, 0, -angle.z, 0f, 0f, 1f)
                    Matrix.rotateM(modelMatrix4,
                        0, if (angle.x > 0) 180 - angle.x else angle.x - 180, 1f, 0f, 0f)
                }
                val sf = getScaleValueForDst(centerLocation.distance)
                Matrix.scaleM(modelMatrix4, 0, sf, sf, 1f)
                Matrix.multiplyMM(modelMatrix3, 0, rotationProjectionMatrix, 0, modelMatrix4, 0)
                mDistanceShaderProgram.setUniforms(modelMatrix3, prompt.getDistanceTextureId(), 0)
                prompt.bindData(mDistanceShaderProgram.aPositionLocation,
                    mDistanceShaderProgram.aTextureCoordinatesLocation)
                prompt.draw()
            }
        }
    }

    /**
     * 根据距离缩放
     * @param dst 距离
     */
    private fun getScaleValueForDst(dst: Double): Float {
        return if (dst < 100) {
            (dst * 10 / 100).toInt() / 10f
        } else {
            (dst * 10 / 100).toInt() / 10f
        }
    }

    private var needTakeBitmap: AtomicBoolean = AtomicBoolean(false)
    private var mCallback: RendererTaskListener? = null

    /**
     * 获取绘制的缓存图片
     */
    fun getDrawCacheBitmap(callback: RendererTaskListener) {
        needTakeBitmap.set(true)
        mCallback = callback
    }

    /**
     * 保存成bitmap
     */
    private fun saveToBitmap() {
        if (needTakeBitmap.get()) {
            val b = IntArray(mWidth * mHeight)
            val ib = IntBuffer.wrap(b)
            ib.position(0)
            glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib)
            val glbitmap = Bitmap.createBitmap(b, mWidth, mHeight, Bitmap.Config.ARGB_8888)
            val cmVals = floatArrayOf(0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f,
                0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(ColorMatrix(cmVals))
            val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(glbitmap, 0f, 0f, paint)
            glbitmap.recycle()
            val matrix = android.graphics.Matrix()
            matrix.preScale(1.0f, -1.0f)
            val result =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            mCallback?.takeBitmap(result)
            needTakeBitmap.set(false)
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        callback.invoke()
    }
}