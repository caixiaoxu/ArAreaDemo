package com.lsy.arareademo

import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.kingo.kingoar.gles.helpers.Camera2Helper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, MapActivity::class.java))
        finish()

//        val surface = findViewById<SurfaceView>(R.id.surfaceView)
//        val camera2Helper = Camera2Helper(this)
//        surface.holder.addCallback(object : SurfaceHolder.Callback2{
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                camera2Helper.startCameraPreview(surface.holder.surface)
//            }
//
//            override fun surfaceChanged(
//                holder: SurfaceHolder,
//                format: Int,
//                width: Int,
//                height: Int,
//            ) {
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//            }
//
//            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
//            }
//
//        })
    }
}