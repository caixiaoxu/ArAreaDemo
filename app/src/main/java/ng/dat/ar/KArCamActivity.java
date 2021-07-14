package ng.dat.ar;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import ng.dat.ar.helper.GPSTransformUtil;
import ng.dat.ar.helper.RealTimeLocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.model.LatLng;
import com.kingo.ar.fragment.KingoArFragment;
import com.kingo.ar.util.location.BeyondarLocationManager;
import com.kingo.ar.world.GeoObject;
import com.kingo.ar.world.World;

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
        world.setGeoPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 2);
        Log.d(TAG, "Configure_AR: LOCATION" + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        world.setDefaultImage(R.drawable.ar_sphere_default);

        arFragmentSupport = (KingoArFragment) getSupportFragmentManager().findFragmentById(
                R.id.ar_cam_fragment);

        for (int i = 0; i < mLatLngList.size(); i++) {
            ArrayList<LatLng> latLngs = mLatLngList.get(i);
            for (int j = 0; j < latLngs.size(); j++) {
                GeoObject polyGeoObj = new GeoObject(j);
                polyGeoObj.setGeoPosition(latLngs.get(j).latitude, latLngs.get(j).longitude, 0);
                polyGeoObj.setImageResource(R.drawable.ar_sphere_150x);
                polyGeoObj.setName("arObj" + j);
                world.addBeyondarObject(polyGeoObj, i, 0 == i ? 0 : 1);
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
