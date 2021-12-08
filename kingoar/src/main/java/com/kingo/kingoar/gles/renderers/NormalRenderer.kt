package com.kingo.kingoar.gles.renderers

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.Matrix
import com.kingo.kingoar.R
import com.kingo.kingoar.gles.helpers.MatrixHelper
import com.kingo.kingoar.gles.helpers.TextureUtil
import com.kingo.kingoar.gles.params.Location
import com.kingo.kingoar.gles.params.Position
import com.kingo.kingoar.gles.programs.DistanceShaderProgram
import com.kingo.kingoar.gles.shapes.DistancePrompt
import com.kingo.kingoar.gles.shapes.params.Geomtery
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

    private lateinit var mDistancePrompt: DistancePrompt
    private lateinit var mDistanceShaderProgram: DistanceShaderProgram

    var angleX = 0f
    var angleY = 0f
    private var textureId: Int = 0
    override fun initShapeAndProgram() {
        mDistancePrompt =
            DistancePrompt(Position(Location(0.0, 0.0, 0.0)).apply {
                coordinate = Geomtery.Point(0.5f, 0.5f, 0f)
            },
                BitmapFactory.decodeResource(mContext.resources, R.mipmap.bg_tip),
                BitmapFactory.decodeResource(mContext.resources, R.mipmap.icon_distance))
        mDistanceShaderProgram = DistanceShaderProgram(mContext)
        textureId = TextureUtil.loadTexture(mContext.resources, R.mipmap.test1)
    }

    override fun initMatrix(width: Int, height: Int) {
        super.initMatrix(width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        MatrixHelper.createBaseOrthoM(mProjectionMatrix, width, height)
//        MatrixHelper.perspectiveM(mProjectionMatrix, 45f, aspectRatio, 0f, 40f)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, 80f, 1f, 1f, 0f)
//        Matrix.translateM(modelMatrix, 0, -0.5f, -1f, 5f)
        Matrix.multiplyMM(modelMatrix, 0, mProjectionMatrix, 0, modelMatrix, 0)

        mDistanceShaderProgram.useProgram()
        mDistanceShaderProgram.setUniforms(modelMatrix, textureId, 1)
        mDistancePrompt.bindData(mDistanceShaderProgram.aPositionLocation,
            mDistanceShaderProgram.aTextureCoordinatesLocation)
        mDistancePrompt.draw()
    }
}