package ng.dat.ar.helper;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Xuwl
 * @date 2021/7/9
 */
public class RealTimeLocation {
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    private List<AMapLocationListener> listeners = new ArrayList<>();

    private RealTimeLocation() {
    }

    private volatile static RealTimeLocation instance;

    public static RealTimeLocation getInstance() {
        if (null == instance) {
            synchronized (RealTimeLocation.class) {
                if (null == instance) {
                    instance = new RealTimeLocation();
                }
            }
        }
        return instance;
    }

    public void startLocation(Context context) {
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(context);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(aMapLocation -> {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Log.e("定位信息",
                                "来源:" + aMapLocation.getLocationType()//获取当前定位结果来源，如网络定位结果，详见定位类型表
                                        + "纬度" + aMapLocation.getLatitude()//获取纬度
                                        + "经度" + aMapLocation.getLongitude()//获取经度
                                        +"海拔" + aMapLocation.getAltitude()//海拔
                                        +"精度" + aMapLocation.getAccuracy()//获取精度信息
                                        +"时间" + df.format(new Date(aMapLocation.getTime()))//定位时间
                        );
                        //回调
                        for (AMapLocationListener listener : listeners) {
                            listener.onLocationChanged(aMapLocation);
                        }
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            });
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(2000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        }
        if (!mlocationClient.isStarted()) {
            mlocationClient.startLocation();
        }
    }

    public void stopLocation() {
        mlocationClient.stopLocation();
    }

    public void addListener(AMapLocationListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(AMapLocationListener listener) {
        this.listeners.remove(listener);
    }

    public void setListener(List<AMapLocationListener> listeners) {
        this.listeners = listeners;
    }
}
