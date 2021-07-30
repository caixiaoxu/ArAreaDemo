package ng.dat.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ng.dat.ar.helper.LocationHelper;
import ng.dat.ar.model.ARPoint;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;
    Paint paint;


    public AROverlayView(Context context) {
        super(context);

        this.context = context;
        //Demo points
        /*arPoints = new ArrayList<ARPoint>() {{
         *//*add(new ARPoint("1", 30.275060, 119.987749, 20));
            add(new ARPoint("2", 30.273901, 119.987749, 20));
            add(new ARPoint("3", 30.273901, 119.990613, 20));
            add(new ARPoint("4", 30.275060, 119.990613, 20));*//*
            add(new ARPoint("1", 30.275794, 119.990668, 20));
            add(new ARPoint("2", 30.275885, 119.991690, 20));
            add(new ARPoint("3", 30.275148, 119.991758, 20));
            add(new ARPoint("4", 30.275069, 119.990875, 20));

        }};*/
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);
        paint.setStrokeWidth(10);
    }

    public void setPoints(List<ARPoint> arPoints) {
        this.arPoints = arPoints;
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null || arPoints == null) {
            return;
        }
        List<PointF> pointFList = new ArrayList<>();
        for (int i = 0; i < arPoints.size(); i++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            Location location = arPoints.get(i).getLocation();
            location.setAltitude(currentLocation.getAltitude() + location.getAltitude());
            float[] pointInECEF = LocationHelper.WSG84toECEF(location);
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();
                pointFList.add(new PointF(x, y));
            }
        }
        if (pointFList.isEmpty()) {
            return;
        }
        int length = pointFList.size();
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                canvas.drawLine(pointFList.get(i).x, pointFList.get(i).y, pointFList.get(0).x, pointFList.get(0).y, paint);
            } else {
                canvas.drawLine(pointFList.get(i).x, pointFList.get(i).y, pointFList.get(i + 1).x, pointFList.get(i + 1).y, paint);
            }
        }
    }
}
