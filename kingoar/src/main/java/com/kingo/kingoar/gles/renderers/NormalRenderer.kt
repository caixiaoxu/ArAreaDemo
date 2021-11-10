package com.kingo.kingoar.gles.renderers

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.kingo.kingoar.gles.helpers.MatrixHelper
import com.kingo.kingoar.gles.programs.NormalShaderProgram
import com.kingo.kingoar.gles.shapes.base.Lines
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Xuwl
 * @date 2021/10/29
 *
 */
class NormalRenderer(val mContext: Context) : BaseRenderer() {
    protected val mProjectionMatrix = FloatArray(16)
    protected val mModelMatrix = FloatArray(16)
    protected val mModelProjectionMatrix = FloatArray(16)

    private lateinit var mLines: Lines
    private lateinit var mNormalShaderProgram: NormalShaderProgram

    var angleX = 0f
    var angleY = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)

//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun initShapeAndProgram() {
        mLines = Lines(floatArrayOf(
            0f, 0f, 0f,
            0f, 0.5f, 0f
        ))
        mNormalShaderProgram = NormalShaderProgram(mContext)
    }

    override fun initMatrix(width: Int, height: Int) {
        super.initMatrix(width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        MatrixHelper.createBaseOrthoM(mProjectionMatrix, width, height)
//        MatrixHelper.perspectiveM(mProjectionMatrix, 45f, aspectRatio, 0f, 40f)
    }

    override fun onDrawFrame(gl: GL10?) {
//        super.onDrawFrame(gl)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        Matrix.setIdentityM(mModelMatrix, 0)
//        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -2f)
        Matrix.rotateM(mModelMatrix, 0, angleX, 0f, 1f, 0f)
        Matrix.rotateM(mModelMatrix, 0, angleY, 1f, 0f, 0f)
        Matrix.multiplyMM(mModelProjectionMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0)

        mNormalShaderProgram.useProgram()
        mNormalShaderProgram.setUniforms(mModelProjectionMatrix)
        mLines.bindData(mNormalShaderProgram.aPositionLocation)
//        mLines.bindData(mNormalShaderProgram.aPositionLocation,
//            mNormalShaderProgram.aColorLocation)
        mLines.draw()
    }
}