package com.lsy.arareademo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps2d.model.LatLng
import com.kingo.kingoar.gles.fragments.ArAreaFragment
import com.kingo.kingoar.gles.listeners.RendererTaskListener
import com.kingo.kingoar.gles.params.Location
import java.io.FileOutputStream

class ArAreaActivity : AppCompatActivity() {

    private lateinit var arAreaFragment: ArAreaFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_area)

        val curLoc = intent.getParcelableExtra<LatLng>("location")
        val tagLocs = intent.getSerializableExtra("geometrys") as? ArrayList<ArrayList<LatLng>>
        if (null != curLoc && null != tagLocs) {
            arAreaFragment = ArAreaFragment()
            arAreaFragment.arguments = Bundle().apply {
                putBoolean(ArAreaFragment.PARAM_SHOW_ALTITUDE_CONTROL, true)
                putBoolean(ArAreaFragment.PARAM_IS_OPEN_CAMERA, true)
                putParcelable(ArAreaFragment.PARAM_CURLOC,
                    Location(curLoc.latitude, curLoc.longitude, 2.0))

                val locLists = ArrayList<ArrayList<Location>>()
                val centers = ArrayList<Location>()
                tagLocs.forEach { list ->
                    val locs = ArrayList<Location>()
                    list.forEach {
                        locs.add(Location(it.latitude, it.longitude, 0.0))
                    }
                    locLists.add(locs)
                    centers.add(locs[0])
                }
                putSerializable(ArAreaFragment.PARAM_TAGLOCS, locLists)
                putParcelableArrayList(ArAreaFragment.PARAM_CENTERLOCS, centers)

//                putParcelable(ArAreaFragment.PARAM_CURLOC, Location(30.275126, 119.990152, 1.0))
//                putParcelableArrayList(ArAreaFragment.PARAM_TAGLOCS, arrayListOf(
//                    Location(30.275394, 119.99076, 0.0),
//                    Location(30.275609, 119.991661, 0.0),
//                    Location(30.274686, 119.991645, 0.0),
//                    Location(30.274696, 119.990837, 0.0),
//                ))
            }
            supportFragmentManager.beginTransaction().replace(R.id.fl_control, arAreaFragment)
                .commit()
            arAreaFragment.startRenderingAR()
        }

        initTest()
    }

    fun changePosition(view: View) {
        arAreaFragment.updateCurLocation()
    }

    fun initTest() {
        val testBtn = findViewById<Button>(R.id.ar_test)
        testBtn.setOnClickListener {
            arAreaFragment.getRenderDrawBitmap(object : RendererTaskListener {
                override fun takeBitmap(bitmap: Bitmap) {
                    bitmap2Path(bitmap, "sdcard/a.png")
                }
            })
        }
    }

    /**
     * 将bitmap转换为本地的图片
     *
     * @param bitmap
     * @return
     */
    fun bitmap2Path(bitmap: Bitmap?, path: String) {
        try {
            val os = FileOutputStream(path)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}