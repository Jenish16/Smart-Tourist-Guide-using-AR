package com.tour.ar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import com.tour.ar.helper.LocationHelper;
import com.tour.ar.model.ARPoint;


public class AROverlayView extends View {

    Context context;
   private double lat;
    FirebaseDatabase database;
    DatabaseReference list;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private Location c;
    private List<ARPoint> arPoints;
    private double clan;
    private double clon;
    public double cameravector2;
    double min_dis=0.1;
    int min_dis_i=-1;
    public AROverlayView(Context context) {
        super(context);


        arPoints = new ArrayList<ARPoint>();
        this.context = context;
        database = FirebaseDatabase.getInstance();
        list = database.getReference("Points");
        list.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postdatasnapshot:dataSnapshot.getChildren())
                {
                    String name=postdatasnapshot.child("name").getValue(String.class);
                    lat=postdatasnapshot.child("lat").getValue(Double.class);
                    double lon=postdatasnapshot.child("lon").getValue(Double.class);
                    double altitude=postdatasnapshot.child("altitude").getValue(Double.class);
                    arPoints.add(new ARPoint(name,lat,lon,altitude));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }
    public double getdistance(double clat,double clon,double plat,double plon)
    {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(plat-clat);  // deg2rad below
        double dLon = deg2rad(plon-clon);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(clat)) * Math.cos(deg2rad(plat)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }
    public double deg2rad(double deg)
    {
        return deg * (Math.PI/180);
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }


        min_dis=0.1;
        min_dis_i=-1;

        for (int i = 0; i < arPoints.size(); i ++) {
            double distance=getdistance(currentLocation.getLatitude(),currentLocation.getLongitude(),arPoints.get(i).getLat(),arPoints.get(i).getLon());

            if(distance<0.03)
            {

                final int radius = 30;
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(60);
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
                float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

                if (distance<min_dis)
                {
                    min_dis_i=i;
                    min_dis = distance;
                    cameravector2=cameraCoordinateVector[2];
                }
                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
                // z > 0, point display opposite
                if (cameraCoordinateVector[2] < 0)
                {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                    canvas.drawCircle(x, y, radius, paint);
                    canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 70, paint);
                }
            }
            else if(distance<0.06)
            {
                final int radius = 20;
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(40);
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
                float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);


                if (cameraCoordinateVector[2] < 0)
                {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                    canvas.drawCircle(x, y, radius, paint);
                    canvas.drawText(arPoints.get(i).getName(), x - (20 * arPoints.get(i).getName().length() / 2), y - 40, paint);
                }
            }
            else if(distance<0.09)
            {
                final int radius = 10;
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(20);
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
                float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
                // z > 0, point display opposite
                if (cameraCoordinateVector[2] < 0)
                {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                    canvas.drawCircle(x, y, radius, paint);
                    canvas.drawText(arPoints.get(i).getName(), x - (10 * arPoints.get(i).getName().length() / 2), y - 20, paint);

                }

            }

        }
        //for

        if(min_dis!=0.1)
        {
            if(cameravector2<0)
            {

            }
        }

    }
}
