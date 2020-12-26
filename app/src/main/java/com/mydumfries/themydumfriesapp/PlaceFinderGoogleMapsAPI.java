package com.mydumfries.themydumfriesapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlaceFinderGoogleMapsAPI extends FragmentActivity implements OnMarkerClickListener, OnInfoWindowClickListener, OnMapReadyCallback {
    static final LatLng DUMFRIES = new LatLng(55.0686, -3.611);
    private GoogleMap map;
    private EventDataSQLHelper placesData;
    int places = 0;
    int display = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        display = getIntent().getIntExtra("display", 0);
        return;
    }

    public void ShowLocations() {
        placesData = new EventDataSQLHelper(this);
        final SQLiteDatabase db = placesData.getReadableDatabase();
        Cursor cursor = db.query(EventDataSQLHelper.TABLE3, null, null, null,
                null, null, null);
        map.setOnMarkerClickListener(this);
        map.clear();
        while (cursor.moveToNext()) {
            Double lat = cursor.getDouble(3);
            Double lng = cursor.getDouble(2);
            String reminder = cursor.getString(1) + "\nClick For Directions";
            LatLng latlng = new LatLng(lat, lng);
            if (lat == 0 & lng == 0) {
                latlng = DUMFRIES;
                reminder = "Dumfries";
            }
            Marker marker = map.addMarker(new MarkerOptions().position(latlng)
                    .title(reminder));
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
            places++;
        }
    }

    @Override
    public boolean onMarkerClick(final Marker arg0) {
        // TODO Auto-generated method stub
        final String title = arg0.getTitle();
        arg0.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + arg0.getPosition().latitude + "," + arg0.getPosition().longitude));
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
    }

    public void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DUMFRIES, 18));
        GPSTracker gps = null;
        double latitude = 0;
        double longitude = 0;
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
        map.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng arg0) {
                // TODO Auto-generated method stub
                final double mylat = arg0.latitude;
                final double mylng = arg0.longitude;
                new AlertDialog.Builder(PlaceFinderGoogleMapsAPI.this)
                        .setTitle("Record New Place.")
                        .setMessage("Do you want to return the co-ordinates " + arg0 + " to the Add New Place screen?")
                        .setCancelable(false)
                        .setPositiveButton("Add This Place", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent location = new Intent(PlaceFinderGoogleMapsAPI.this, DumfriesPlaceFinderActivity.class);
                                location.putExtra("lat", mylat);
                                location.putExtra("lng", mylng);
                                startActivity(location);
                                PlaceFinderGoogleMapsAPI.this.finish();
                                return;
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                return;
                            }
                        })
                        .show();
            }
        });
        map.setOnInfoWindowClickListener(new
                                                 OnInfoWindowClickListener() {
                                                     @Override
                                                     public void
                                                     onInfoWindowClick(Marker marker) {
                                                         Intent intent = new Intent(Intent.ACTION_VIEW,
                                                                 Uri.parse("http://maps.google.com/maps?daddr=" + marker.getPosition().latitude + "," + marker.getPosition().longitude));
                                                         startActivity(intent);
                                                     }
                                                 });
        ShowLocations();
        gps = new GPSTracker(PlaceFinderGoogleMapsAPI.this);
        if ((gps.canGetLocation() && places > 1) || (gps.canGetLocation() && display == 1)) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            if (latitude < 55.27755086530207 && latitude > 54.85210585589739 && longitude < (-3.0239868164025) && longitude > (-4.08416748046875)) {
                //they are in Dumfries so centre map on location
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));
            }
            gps.stopUsingGPS();
        }

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }
}
