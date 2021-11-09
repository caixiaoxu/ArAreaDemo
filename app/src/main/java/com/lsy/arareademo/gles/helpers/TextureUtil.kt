package com.lsy.arareademo.gles.helpers

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

/**
 * 纹理工具类
 * @author Xuwl
 * @date 2021/10/22
 *
 */
object TextureUtil {
    /**
     * 加载纹理
     * @param res 资源管理
     * @param rId 资源Id
     */
    fun loadTexture(res: Resources, rId: Int): Int {
        //生成一个ID，赋值给变量
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        if (0 == textureIds[0]) {
            LogHelper.logE("Could not generate a new OpenGL texture object.")
            return 0
        }
        //加载无压缩原始数据图片
        val bitmap = BitmapFactory.Options().apply { inScaled = false }.run {
            BitmapFactory.decodeResource(res, rId, this)
        }
        if (null == bitmap) {
            LogHelper.logE("Resourece ID $rId could not be decoded.")
            GLES20.glDeleteTextures(1, textureIds, 0)
            return 0
        }
        //返回处理好的纹理Id
        return textureIds[0].also {
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, it)
            //设置纹理过滤参数
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            //加载纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            //生成MIP贴图
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            //解绑纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }
    }

    /**
     * 加载立方体贴图
     * @param res 资源管理器
     * @param cubeIds 立方体资源
     */
    fun loadCubeMap(res: Resources, cubeIds: IntArray): Int {
        if (cubeIds.size < 6) {
            LogHelper.logE("Need last 6 of cubeIds.")
            return 0
        }

        val textureObjectIds = IntArray(1)
        GLES20.glGenTextures(1, textureObjectIds, 0)
        if (0 == textureObjectIds[0]) {
            LogHelper.logE("Could not generate a new OpenGL texture object.")
            return 0
        }
        val options = BitmapFactory.Options().apply { inScaled = false }
        val cubeBitmaps = arrayOfNulls<Bitmap>(cubeIds.size)
        for (i in cubeIds.indices) {
            cubeBitmaps[i] = BitmapFactory.decodeResource(res, cubeIds[i], options)
            if (null == cubeBitmaps[i]) {
                LogHelper.logE("Resource ID ${cubeIds[i]} could not be decoded.")
                GLES20.glDeleteTextures(1, textureObjectIds, 0)
                return 0
            }
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureObjectIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        //左右
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0)

        //上下
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0)

        //前后
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0)
        cubeBitmaps.forEach { it?.recycle() }
        return textureObjectIds[0]
    }
}