package com.lsy.arareademo

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps2d.*
import com.amap.api.maps2d.model.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.PermissionRequest


class MapActivity : AppCompatActivity(), PermissionCallbacks, AMap.OnMyLocationChangeListener {
    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN)
        private const val PERMISSION_REQUESTCODE = 1
    }

    private lateinit var mMapView: MapView
    private lateinit var mAMap: AMap
    private var mPolyline: Polyline? = null
    private var mPolygon: Polygon? = null
    private var mPolylineOptions: PolylineOptions = PolylineOptions().width(4f)
    private var mPolygonOptions: PolygonOptions =
        PolygonOptions().fillColor(Color.TRANSPARENT).strokeWidth(4f).strokeColor(Color.BLACK)
    private val FILL_COLOR = Color.argb(10, 0, 0, 180)
    private val STROKE_COLOR = Color.argb(180, 3, 145, 255)

    private var curLocation: LatLng? = null
    private val mLatLngList: ArrayList<LatLng> = ArrayList()
    private val mLatLngLists: ArrayList<ArrayList<LatLng>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
        setContentView(R.layout.activity_map)
        mMapView = findViewById(R.id.mapView)
        //???activity??????onCreate?????????mMapView.onCreate(savedInstanceState)???????????????
        mMapView.onCreate(savedInstanceState)
        initPermission()
        initMap()
    }

    private fun initPermission() {
        val builder = PermissionRequest.Builder(this, PERMISSION_REQUESTCODE, *PERMISSIONS)
        EasyPermissions.requestPermissions(builder.build())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //???activity??????onSaveInstanceState?????????mMapView.onSaveInstanceState (outState)??????????????????????????????
        mMapView.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        //???activity??????onResume?????????mMapView.onResume ()???????????????????????????
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //???activity??????onPause?????????mMapView.onPause ()????????????????????????
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAMap.isMyLocationEnabled = false
        mMapView.onDestroy()
    }

    override fun onPermissionsGranted(i: Int, list: List<String>) {}

    override fun onPermissionsDenied(i: Int, list: List<String>) {}

    private fun initMap() {
        mAMap = mMapView.map
        //???????????????
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.showMyLocation(true)
        // ??????????????????????????????????????????
        myLocationStyle.strokeColor(STROKE_COLOR)
        //??????????????????????????????????????????
        myLocationStyle.strokeWidth(5f)
        // ???????????????????????????
        myLocationStyle.radiusFillColor(FILL_COLOR)
        mAMap.setMyLocationStyle(myLocationStyle)
        mAMap.setOnMyLocationChangeListener(this)
        mAMap.isMyLocationEnabled = true
        mAMap.uiSettings.isMyLocationButtonEnabled = true // ????????????????????????????????????

        mAMap.setOnMapClickListener {
            mLatLngList.add(it)
            mPolyline?.remove()
            mPolygon?.remove()

            if (mLatLngList.size > 1) {
                //?????????????????????
                if (mLatLngList.size < 3) {
                    mPolylineOptions.points.clear()
                    mPolyline = mAMap.addPolyline(mPolylineOptions.addAll(mLatLngList))
                } else {
                    mPolygonOptions.points.clear()
                    mPolygon = mAMap.addPolygon(mPolygonOptions.addAll(mLatLngList))
                }
            }
        }
    }

    fun clear(view: View?) {
        mLatLngLists.clear()
        clearCur()
    }

    private fun clearCur() {
        mLatLngList.clear()
        mPolyline?.remove()
        mPolygon?.remove()
        mAMap.invalidate()
    }

    fun finish1(view: View?) {
        if (mLatLngList.size > 0){
            mLatLngLists.add(mLatLngList)
        }

        curLocation = LatLng(mAMap.myLocation.latitude, mAMap.myLocation.longitude)
        val intent = Intent(this, ArAreaActivity::class.java)
//        mLatLngList.clear()
//        mLatLngList.add(LatLng(curLocation!!.latitude,curLocation!!.longitude + 0.0001))
//        mLatLngList.add(LatLng(curLocation!!.latitude + 0.0001,curLocation!!.longitude + 0.0002))
//        mLatLngList.add(LatLng(curLocation!!.latitude + 0.0002,curLocation!!.longitude + 0.0002))
//        mLatLngList.add(LatLng(curLocation!!.latitude ,curLocation!!.longitude + 0.0005))
        intent.putExtra("geometrys", mLatLngLists)
        intent.putExtra("location", curLocation)
        startActivity(intent);
    }

    fun addRect(view: View?) {
        mLatLngLists.add(ArrayList(mLatLngList))
        clearCur()
    }

    override fun onMyLocationChange(location: Location?) {
        if (location != null) {
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,
                location.longitude), 17f))
        }
    }
}