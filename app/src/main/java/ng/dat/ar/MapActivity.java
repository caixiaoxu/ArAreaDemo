package ng.dat.ar;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;

import java.util.ArrayList;

import ng.dat.ar.helper.GPSTransformUtil;
import ng.dat.ar.helper.RealTimeLocation;

public class MapActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener {

    private MapView mMapView;
    private AMap mAMap;
    private ArrayList<ArrayList<LatLng>> morLatLngList = new ArrayList<>();
    private ArrayList<LatLng> mLatLngList;
    PolygonOptions polygonOptions;
    private EditText mEditText;
    private Polygon mPolygon;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private Location mLocation;

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private RealTimeLocation mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapView = findViewById(R.id.mapView);
        mEditText = findViewById(R.id.altitude);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initMap();
    }

    private void initMap() {
        mAMap = mMapView.getMap();
        mLatLngList = new ArrayList<>();
        polygonOptions = new PolygonOptions();
        polygonOptions.strokeWidth(10) // 多边形的边框
                .strokeColor(Color.argb(100, 255, 0, 0)) // 边框颜色
                .fillColor(Color.argb(1, 1, 0, 0));   // 多边形的填充色

        mAMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mPolygon != null) {
                    mPolygon.remove();
                }
                double[] values = GPSTransformUtil.gcj02_To_Gps84(latLng.latitude, latLng.longitude);
                mLatLngList.add(new LatLng(values[0], values[1]));
                polygonOptions.add(latLng);
                mPolygon = mAMap.addPolygon(polygonOptions);
            }
        });
        mAMap.setLocationSource(this);// 设置定位监听
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        setupLocationStyle();
        mInstance = RealTimeLocation.getInstance();
        mInstance.addListener(this);
        mInstance.startLocation(getApplicationContext());
    }

    private void setupLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        mAMap.setMyLocationStyle(myLocationStyle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }

        if (null != mInstance) {
            mInstance.removeListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    public void clear(View view) {
        morLatLngList.clear();
        mLatLngList.clear();
        if (mPolygon != null) {
            mPolygon.remove();
        }
        if (polygonOptions != null) {
            polygonOptions.getPoints().clear();
        }
        mAMap.invalidate();
    }

    public void finish(View view) {
        Intent intent = new Intent(this, ARActivity.class);
        /*Bundle bundle = new Bundle();
        bundle.putSerializable("geometry", mLatLngList);
        bundle.putString("altitude", mEditText.getText().toString());*/
        intent.putExtra("geometry", mLatLngList);
        intent.putExtra("altitude", mEditText.getText().toString());
        startActivity(intent);
    }

    public void finish1(View view) {
        if (mLatLngList.size() > 0) {
            morLatLngList.add(mLatLngList);
        }

        mLocation = mAMap.getMyLocation();
        double[] values = GPSTransformUtil.gcj02_To_Gps84(mLocation.getLatitude(), mLocation.getLongitude());
        mLocation.setLatitude(values[0]);
        mLocation.setLongitude(values[1]);
        Intent intent = new Intent(this, KArCamActivity.class);
        /*Bundle bundle = new Bundle();
        bundle.putSerializable("geometry", mLatLngList);
        bundle.putString("altitude", mEditText.getText().toString());*/
        intent.putExtra("geometry", morLatLngList);
        intent.putExtra("location", mLocation);
        startActivity(intent);
        mLocation = null;
    }

    public void addRect(View view) {
        morLatLngList.add(mLatLngList);
        mLatLngList = new ArrayList<>();
        if (mPolygon != null) {
            mPolygon.remove();
        }
        if (polygonOptions != null) {
            polygonOptions.getPoints().clear();
        }
        mAMap.invalidate();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                mAMap.moveCamera(CameraUpdateFactory.zoomTo(18));
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}