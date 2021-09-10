package ng.dat.ar;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import ng.dat.ar.helper.GPSTransformUtil;
import ng.dat.ar.helper.RealTimeLocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.model.LatLng;
import com.kingoit.ar.fragment.KingoArFragment;
import com.kingoit.ar.util.location.BeyondarLocationManager;
import com.kingoit.ar.world.GeoObject;
import com.kingoit.ar.world.World;

import java.util.ArrayList;

/**
 * Created by Amal Krishnan on 27-03-2017.
 */

public class KArCamActivity extends FragmentActivity implements AMapLocationListener {


    private final static String TAG = "ArCamActivity";

    private KingoArFragment arFragmentSupport;
    private World world;
    private Location mLastLocation;
    ArrayList<ArrayList<LatLng>> mLatLngList;
    private String mType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_camera1);

        initData();
        Configure_AR(); //Configure AR Environment
    }

    private void initData() {

        if (getIntent() != null) {
            mLatLngList = (ArrayList<ArrayList<LatLng>>) getIntent().getSerializableExtra("geometry");
            Location location = getIntent().getParcelableExtra("location");
            mType = getIntent().getStringExtra("altitude");
            String ds = getIntent().getStringExtra("distance");
            try {
                ((KingoArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_cam_fragment)).setDistanceFactor(Float.parseFloat(ds));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (location != null) {
                mLastLocation = location;
                mLastLocation.setAltitude(2);
            }
        }
    }

    public static Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    private void Configure_AR() {
        world = new World(getApplicationContext());
        world.setGeoPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 0);
//        if ("2".equals(mType)) {
//            world.setGeoPosition(30.2774453, 119.98550681, 0);
//        } else {
//            world.setGeoPosition(30.27738939, 119.98550662, 0);
//        }
        Log.d(TAG, "Configure_AR: LOCATION" + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        world.setDefaultImage(R.drawable.ar_sphere_default);

        arFragmentSupport = (KingoArFragment) getSupportFragmentManager().findFragmentById(
                R.id.ar_cam_fragment);

//        mLatLngList.clear();
//        ArrayList<LatLng> list = new ArrayList<>();
//        if ("3".equals(mType)) {
//            list.add(new LatLng(30.27718802, 119.98647098));
//            list.add(new LatLng(30.27688672, 119.98659513));
//            list.add(new LatLng(30.27651789, 119.98663365));
//            list.add(new LatLng(30.27675243, 119.98629613));
//            list.add(new LatLng(30.27714405, 119.98608846));
//            list.add(new LatLng(30.2773477, 119.98593615));
//        } else {
//            list.add(new LatLng(30.27744285, 119.98555389));
//            list.add(new LatLng(30.2775089, 119.98554165));
//            list.add(new LatLng(30.27749052, 119.98541045));
//            list.add(new LatLng(30.27742198, 119.9854239));
//        }
//        mLatLngList.add(list);
        for (int i = 0; i < mLatLngList.size(); i++) {
            ArrayList<LatLng> latLngs = mLatLngList.get(i);
            for (int j = 0; j < latLngs.size(); j++) {
                GeoObject polyGeoObj = new GeoObject(j);
                polyGeoObj.setGeoPosition(latLngs.get(j).latitude, latLngs.get(j).longitude, 0);
                polyGeoObj.setImageResource(R.drawable.ar_sphere_150x);
                polyGeoObj.setName("arObj" + j);
                world.addBeyondarObject(polyGeoObj, i, 1);
            }
        }

        // Send to the fragment
        arFragmentSupport.setWorld(world);

        RealTimeLocation.getInstance().addListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BeyondarLocationManager.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BeyondarLocationManager.enable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RealTimeLocation.getInstance().removeListener(this);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        double[] values = GPSTransformUtil.gcj02_To_Gps84(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        world.setGeoPosition(values[0], values[1], 2);
    }
}
