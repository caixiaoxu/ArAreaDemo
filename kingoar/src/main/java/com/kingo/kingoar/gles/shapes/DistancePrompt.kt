package com.kingo.kingoar.gles.shapes

import android.graphics.*
import android.opengl.GLES20
import android.text.TextPaint
import com.kingo.kingoar.gles.arrays.VertexArray
import com.kingo.kingoar.gles.helpers.TextureUtil
import com.kingo.kingoar.gles.params.Position
import com.kingo.kingoar.gles.shapes.base.Shape
import java.lang.String
import kotlin.math.abs
import kotlin.math.max

/**
 * @author Xuwl
 * @date 2021/12/3
 *
 */
class DistancePrompt(var mCenter: Position, val bgBitmap: Bitmap, val iconBitmap: Bitmap) :
    Shape() {
    private val POSITION_COMPONENT_COUNT = 3
    private val POSITION_STRIDE: Int = POSITION_COMPONENT_COUNT * Float.SIZE_BYTES
    private val TEXTURE_COMPONENT_COUNT = 2
    private val TEXTURE_STRIDE: Int = TEXTURE_COMPONENT_COUNT * Float.SIZE_BYTES

    private var vertexArray: VertexArray? = null
    private val textureArray: VertexArray

    var showDistance: Double = 0.0
    private var distanceBitmap: Bitmap? = null
    private var textureId: Int = 0

    init {
        updateCetner(mCenter)

        val textureData = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
        )
        textureArray = VertexArray(textureData)
    }


    /**
     * 更新图形中心点位置
     */
    fun updateCetner(center: Position) {
        this.mCenter = center
        var bound = 12.0f
        mCenter.coordinate?.let {
            val vertexData = floatArrayOf(
                -4.0f, 3.0f, 0f,    //左上角
                -4.0f, -3.0f, 0f,   //左下角
                4.0f, 3.0f, 0f,     //右上角
                4.0f, -3.0f, 0f     //右下角
//                -1.0f, 1.0f, it.z,    //左上角
//                -1.0f, -1.0f, it.z,   //左下角
//                1.0f, 1.0f, it.z,     //右上角
//                1.0f, -1.0f, it.z     //右下角
//                it.x - bound, it.y, it.z + bound/4*3,    //左上角
//                it.x - bound, it.y, it.z - bound/4*3,   //左下角
//                it.x + bound, it.y, it.z + bound/4*3,     //右上角
//                it.x + bound, it.y, it.z - bound/4*3    //右下角
//                it.x - 1f, it.y + 1f, it.z,    //左上角
//                it.x - 1f, it.y - 1f, it.z,   //左下角
//                it.x + 1f, it.y + 1f, it.z,     //右上角
//                it.x + 1f, it.y - 1f, it.z     //右下角
//                it.x - 1f, it.y + 1f, 0f,    //左上角
//                it.x - 1f, it.y - 1f, 0f,   //左下角
//                it.x + 1f, it.y + 1f, 0f,     //右上角
//                it.x + 1f, it.y - 1f, 0f     //右下角
            )
            vertexArray = VertexArray(vertexData)
        }
    }

    /**
     * 绑定数据
     * @param textureProgram 纹理程序
     */
    override fun bindData(vararg attribLocation: Int) {
        vertexArray?.setVertexAttribPointer(0,
            attribLocation[0],
            POSITION_COMPONENT_COUNT,
            POSITION_STRIDE)
        textureArray.setVertexAttribPointer(0,
            attribLocation[1],
            TEXTURE_COMPONENT_COUNT,
            TEXTURE_STRIDE)
    }

    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }


    //可显示图形的最小、最大、和阀值
    private val minDst = 2.0  //可显示图形的最小、最大、和阀值
    private val maxDst = 32.0  //可显示图形的最小、最大、和阀值
    private val fa = 17.0

    /**
     * 获取距离的textureId（如果显示的距离与实现的距离相差大于0.01，更新显示）
     */
    fun getDistanceTextureId(): Int {
        if (0.0 == showDistance || abs(mCenter.distance - showDistance) >= 0.01) {
            distanceBitmap = getDistanceBitmap()
            textureId = TextureUtil.loadTexture(distanceBitmap!!)
            showDistance = mCenter.distance
        }
        return textureId
    }

    /**
     * 绘制距离图片
     *
     * @param beyondarObject
     * @return
     */
    fun getDistanceBitmap(): Bitmap {
        //距离内容
        val context = String.format("%.2f", mCenter.distance)
        val p: Paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        //先通过测试内容的宽高，来获取总的大小
        p.textSize = 36f
        var metrics = p.fontMetricsInt
        val titleheight = metrics.bottom - metrics.top

        p.textSize = 80f
        //获取距离高度
        metrics = p.fontMetricsInt
        val txtheight = metrics.bottom - metrics.top
        val txtWidth = p.measureText(context)

        p.textSize = 20f
        val unitWith = p.measureText(context)

        val marginLR = 50f
        val marginTB = 30f
        val txtMR = 15f
        val txtMT = 15f

        val width = marginLR + txtWidth + txtMR + unitWith + txtMR + iconBitmap.width + marginLR
        val iconHeight = (titleheight + txtMT + txtheight + txtMT)
        val height = marginTB + iconHeight + marginTB
        //根据文字高度宽度生成bitmap
        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        bitmap.setHasAlpha(true)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        //背景
        val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
        val dstRectf = RectF(0f, 0f, width, height)
        canvas.drawBitmap(bgBitmap, srcRect, dstRectf, Paint(Paint.ANTI_ALIAS_FLAG))
        p.color = Color.BLACK

        // 在中心点绘制文字
        p.textSize = 36f
        p.isFakeBoldText = true
        //获取标题高度
        metrics = p.fontMetricsInt
        var baseLine =
            marginTB + titleheight / 2f + (metrics.descent - metrics.ascent) / 2f - metrics.descent
        canvas.drawText("距离图斑", marginLR, baseLine, p)

        //距离
        p.textSize = 80f
        p.color = Color.RED
        //获取距离高度
        metrics = p.fontMetricsInt
        baseLine =
            marginTB + titleheight + txtMT + txtheight / 2f + (metrics.descent - metrics.ascent) / 2f - metrics.descent
        canvas.drawText(context, marginLR, baseLine, p)

        //单位
        p.textSize = 20f
        canvas.drawText("M", marginLR + txtWidth + txtMR, baseLine, p)

        //图标
        val iconSrc = Rect(0, 0, iconBitmap.width, iconBitmap.height)
        val iconRight = width - marginLR
        val iconBottom = height - marginTB - 20f
        val iconDst =
            RectF(iconRight - iconBitmap.width, iconBottom - iconHeight, iconRight, iconBottom)
        canvas.drawBitmap(iconBitmap, iconSrc, iconDst, Paint(Paint.ANTI_ALIAS_FLAG))
        return bitmap
    }
}