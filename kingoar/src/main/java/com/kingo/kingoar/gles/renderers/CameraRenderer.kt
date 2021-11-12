package com.kingo.kingoar.gles.renderers

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.kingo.kingoar.gles.helpers.CameraHelper
import com.kingo.kingoar.gles.programs.CameraShaderProgram
import com.kingo.kingoar.gles.shapes.Camera
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 相机的Renderer
 * @author Xuwl
 * @date 2021/11/1
 *
 */
class CameraRenderer(val mContext: Context, val glSurfaceView: GLSurfaceView) : BaseRenderer(),
    SurfaceTexture.OnFrameAvailableListener {

    private lateinit var mShader: Camera
    private lateinit var mCameraShaderProgram: CameraShaderProgram

    private var mTextureId: Int = 0
    private var mSurfaceTexture: SurfaceTexture? = null

    private val mTextureMatrix = FloatArray(16)

    override fun initShapeAndProgram() {
        mShader = Camera()
        mCameraShaderProgram = CameraShaderProgram(mContext)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)

        initTextureId()
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

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

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
        mShader.bindData(mCameraShaderProgram.aPositionLocation,
            mCameraShaderProgram.aCoordinatesLocation)
        mShader.draw()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        glSurfaceView.requestRender()
    }
}