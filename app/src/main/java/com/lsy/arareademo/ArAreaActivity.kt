package com.lsy.arareademo

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps2d.model.LatLng
import com.kingo.kingoar.gles.fragments.ArAreaFragment
import com.kingo.kingoar.gles.params.Location

class ArAreaActivity : AppCompatActivity() {
    private lateinit var flControl: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_area)

        val curLoc = intent.getParcelableExtra<LatLng>("location")
        val tagLocs = intent.getParcelableArrayListExtra<LatLng>("geometry")
        if (null != curLoc && null != tagLocs) {
            val arAreaFragment = ArAreaFragment()
            arAreaFragment.arguments = Bundle().apply {
                putBoolean(ArAreaFragment.PARAM_SHOW_ALTITUDE_CONTROL, true)
                putBoolean(ArAreaFragment.PARAM_ISOPENCAMERA, true)
                putParcelable(ArAreaFragment.PARAM_CURLOC,
                    Location(curLoc.latitude, curLoc.longitude, 1.0))

                val locs = ArrayList<Location>()
                tagLocs.forEach {
                    locs.add(Location(it.latitude, it.longitude, 0.0))
                }
                putParcelableArrayList(ArAreaFragment.PARAM_TAGLOCS, locs)

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
        }
    }
}