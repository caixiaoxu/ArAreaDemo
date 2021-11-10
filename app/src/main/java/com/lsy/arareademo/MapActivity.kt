package com.lsy.arareademo

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.PermissionRequest


class MapActivity : AppCompatActivity(), PermissionCallbacks {
    private lateinit var mMapView: MapView
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mLocationClient: LocationClient
    private val FILL_COLOR = Color.argb(10, 0, 0, 180)
    private val STROKE_COLOR = Color.argb(180, 3, 145, 255)

    private var curLocation: LatLng? = null
    private val mLatLngList: ArrayList<LatLng> = ArrayList()
    private lateinit var mOverlayOptions: OverlayOptions
    private var polygon: Overlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mMapView = findViewById(R.id.mapView)
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(this, savedInstanceState)
        initPermission()
        initMap()
    }

    private fun initPermission() {
        val builder = PermissionRequest.Builder(this, PERMISSION_REQUESTCODE, *PERMISSIONS)
        EasyPermissions.requestPermissions(builder.build())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        mBaiduMap.isMyLocationEnabled = false
        mMapView.onDestroy()
    }

    override fun onPermissionsGranted(i: Int, list: List<String>) {}

    override fun onPermissionsDenied(i: Int, list: List<String>) {}
    private fun initMap() {
        mBaiduMap = mMapView.map
        //普通地图 ,mBaiduMap是地图控制器对象
//        mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
        mBaiduMap.isMyLocationEnabled = true
        //定位初始化
        mLocationClient = LocationClient(this)

        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.isOnceLocation = true

        mLocationClient.locOption = option

        mBaiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng) {
                mLatLngList.add(p0)
                polygon?.remove()

                if (mLatLngList.size > 1) {
                    //设置折线的属性
                    mOverlayOptions =
                        if (mLatLngList.size < 3) PolylineOptions().width(4).points(mLatLngList)
                        else PolygonOptions().fillColor(Color.TRANSPARENT)
                            .stroke(Stroke(4, Color.BLACK))
                            .points(mLatLngList)
                    polygon = mBaiduMap.addOverlay(mOverlayOptions)
                }
            }

            override fun onMapPoiClick(p0: MapPoi) {
            }
        })

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(19f))

        val myLocationListener = MyLocationListener()
        val myLocationConfiguration =
            MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
                true, BitmapDescriptorFactory.fromResource(R.drawable.gps_point),
                FILL_COLOR, STROKE_COLOR)
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration)
        mLocationClient.registerLocationListener(myLocationListener)
        mLocationClient.start()
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return
            }
            val locData = MyLocationData.Builder()
                .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.direction).latitude(location.latitude)
                .longitude(location.longitude).build()
            mBaiduMap.setMyLocationData(locData)

            curLocation = LatLng(location.latitude, location.longitude)
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(curLocation))
        }
    }

    fun clear(view: View) {
        mLatLngList.clear()
        polygon?.remove()
        polygon = null
    }

    fun finish1(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putParcelableArrayListExtra("geometry", mLatLngList)
        intent.putExtra("location", curLocation)
        startActivity(intent);
    }

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
}