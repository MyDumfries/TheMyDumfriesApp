package com.mydumfries.themydumfriesapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DumfriesPlaceFinderActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    String LOCATION_SERVICE = "location";
    private EventDataSQLHelper placesData;
    double latitude;
    double longitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dfsplcfinder);
        latitude = getIntent().getDoubleExtra("lat", 0);
        longitude = getIntent().getDoubleExtra("lng", 0);
        ImageView ShowOnMap = findViewById(R.id.Mapbutton2);
        Button UpdateDatabase = findViewById(R.id.UpdateDatabase_button);
        final Button UpdateSite = findViewById(R.id.UpdateSite);
        Button UpdateLocation = findViewById(R.id.UpdateLocation_Button);
        Button ClearData = findViewById(R.id.ClearData);
        ImageButton Help = findViewById(R.id.newlocationinfo);
        Help.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(DumfriesPlaceFinderActivity.this, HelpActivity.class);
                intent.putExtra("source", 6);
                startActivity(intent);
            }
        });
        placesData = new EventDataSQLHelper(this);
        final SQLiteDatabase db = placesData.getReadableDatabase();
        String sql = "create table if not exists " + EventDataSQLHelper.TABLE3
                + "( " + BaseColumns._ID + " integer primary key, "
                + EventDataSQLHelper.PLACENAME + " text, "
                + EventDataSQLHelper.LNG + " text, " + EventDataSQLHelper.LAT
                + " text);";
        db.execSQL(sql);
        sql = "create table if not exists " + EventDataSQLHelper.TABLE4 + "( "
                + BaseColumns._ID + " integer primary key, "
                + EventDataSQLHelper.PLACENAME + " text, "
                + EventDataSQLHelper.LNG + " text, " + EventDataSQLHelper.LAT
                + " text);";
        db.execSQL(sql);
        final TextView lat = findViewById(R.id.latitude_textView);
        final TextView lon = findViewById(R.id.longitude_textView);
        lat.setText("Press Update Location Button");
        lon.setText("Press Update Location Button");
        if (latitude != 0) {
            lat.setText(String.valueOf(latitude));
        }
        if (longitude != 0) {
            lon.setText(String.valueOf(longitude));
        }
        ClearData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClearTheData();
            }
        });
        ShowOnMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String mylat = String.valueOf(latitude);
                String mylong = String.valueOf(longitude);
                ContentValues values = new ContentValues();
                values.put(EventDataSQLHelper.LAT, mylat);
                values.put(EventDataSQLHelper.LNG, mylong);
                values.put(EventDataSQLHelper.PLACENAME, "Your Location");
                final SQLiteDatabase db = placesData.getReadableDatabase();
                db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
                db.insert(EventDataSQLHelper.TABLE3, null, values);
                startActivity(new Intent(DumfriesPlaceFinderActivity.this,
                        PlaceFinderGoogleMapsAPI.class));
                DumfriesPlaceFinderActivity.this.finish();
            }
        });
        UpdateDatabase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UpdateDataBase();
            }
        });
        UpdateSite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UpdateSite();
            }
        });
        // show location button click event
        UpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                UpDateData();
            }
        });
        boolean istheredata=CheckData();
        if (istheredata)
        {
            CharSequence text3 = "Found data which has not been uploaded to site.  Please click 'Update Site' to upload, or 'Clear Data' to clear.";
            new AlertDialog.Builder(this)
                    .setTitle("Unprocessed Data")
                    .setMessage(text3)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    })
                    .show();
            DisplayData();
        }
    }

    public void ClearTheData() {
        final SQLiteDatabase db = placesData.getReadableDatabase();
        db.execSQL("delete from " + EventDataSQLHelper.TABLE4);
        db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
        TableLayout eventsTable = findViewById(R.id.tableLayout1);
        eventsTable.removeAllViews();
        Button UpdateSite = findViewById(R.id.UpdateSite);
        Button ClearData = findViewById(R.id.ClearData);
        UpdateSite.setVisibility(View.INVISIBLE);
        ClearData.setVisibility(View.INVISIBLE);
    }

    public void UpDateData()
    {
        if (ActivityCompat.checkSelfPermission(DumfriesPlaceFinderActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DumfriesPlaceFinderActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        SingleShotLocationProvider.requestSingleUpdate(DumfriesPlaceFinderActivity.this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        final TextView lat = findViewById(R.id.latitude_textView);
                        final TextView lon = findViewById(R.id.longitude_textView);
                        latitude = location.latitude;
                        longitude = location.longitude;
                        lat.setText(String.valueOf(latitude));
                        lon.setText(String.valueOf(longitude));
                    }
                });
    }

    public boolean CheckData()
    {
       placesData = new EventDataSQLHelper(this);
       final SQLiteDatabase db = placesData.getReadableDatabase();
       Cursor cursor = db.rawQuery("SELECT COUNT (*) FROM " + EventDataSQLHelper.TABLE4, null);
       if (cursor != null){
           cursor.moveToFirst();
           int count = cursor.getInt(0);
           return count > 0;
       }
       return false;
    }

    public void UpdateDataBase() {
        placesData = new EventDataSQLHelper(this);
        final TextView lat = findViewById(R.id.latitude_textView);
        final TextView lon = findViewById(R.id.longitude_textView);
        final EditText PlaceName = findViewById(R.id.editText_PlaceName);
        String lati = lat.getText().toString();
        String lng = lon.getText().toString();
        String placename = PlaceName.getText().toString();
        String goahead="yes";
        if (lati.equals("Press Update Location Button"))
        {
            goahead="no";
            CharSequence text3 = "Please Press 'Update Location' Button.";
            new AlertDialog.Builder(this)
                    .setTitle("Missing Data")
                    .setMessage(text3)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    })
                    .show();
        }
        if (placename.equals(""))
        {
            CharSequence text3 = "Please Enter Place Name.";
            new AlertDialog.Builder(this)
                    .setTitle("Missing Data")
                    .setMessage(text3)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    })
                    .show();
            goahead="no";
        }
        if (goahead.equals( "yes")) {
            ContentValues values = new ContentValues();
            values.put(EventDataSQLHelper.LAT, lati);
            values.put(EventDataSQLHelper.LNG, lng);
            values.put(EventDataSQLHelper.PLACENAME, placename);
            final SQLiteDatabase db = placesData.getReadableDatabase();
            db.insert(EventDataSQLHelper.TABLE4, null, values);
            Context context = getApplicationContext();
            CharSequence text = "DataBase Updated";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            PlaceName.setText("");
            UpdateSite();
        }
    }

    private Cursor getEvents(String search, String sortorder) {
        SQLiteDatabase db = placesData.getReadableDatabase();
        Cursor cursor = db.query(EventDataSQLHelper.TABLE4, null, search, null,
                null, null, sortorder);
        startManagingCursor(cursor);
        return cursor;
    }

    public void DisplayData() {
        Button UpdateSite = findViewById(R.id.UpdateSite);
        Button ClearData = findViewById(R.id.ClearData);
        UpdateSite.setVisibility(View.VISIBLE);
        ClearData.setVisibility(View.VISIBLE);
        Cursor cursor = getEvents(null, null);
        TableLayout eventsTable = findViewById(R.id.tableLayout1);
        eventsTable.removeAllViews();
        while (cursor.moveToNext() && !cursor.equals(null)) {
            final TableRow newRow = new TableRow(this);
            String lat = cursor.getString(3);
            final String lng = cursor.getString(2);
            final String place = cursor.getString(1);
            addTextToRowWithValues(newRow, " ");
            addTextToRowWithValues(newRow, lat);
            addTextToRowWithValues(newRow, " ");
            addTextToRowWithValues(newRow, lng);
            addTextToRowWithValues(newRow, " ");
            addTextToRowWithValues(newRow, place);
            eventsTable.addView(newRow, 0);
        }
    }

    private void addTextToRowWithValues(final TableRow tableRow, String text) {
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(R.color.purple));
        textView.setHeight(50);
        textView.setText(text);
        tableRow.addView(textView);
    }

    private void UpdateSite() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            CharSequence text3 = "Could not update site. Please click 'Update Site' button when a Network Connection is available.";
            new AlertDialog.Builder(this)
                    .setTitle("No Network Connection.")
                    .setMessage(text3)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            DisplayData();
                            return;
                        }
                    })
                    .show();
        }
        else {
            Cursor cursor = getEvents(null, null);
            while (cursor.moveToNext()) {
                String lat = cursor.getString(3);
                String lng = cursor.getString(2);
                String place = cursor.getString(1);
                try {
                    lat = URLEncoder.encode(lat, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    lng = URLEncoder.encode(lng, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    place = URLEncoder.encode(place, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                new UpdateSite()
                        .execute("http://www.mydumfries.com/PlaceFinderGoogleMapSetUp.php?New=1&referer=Phone&placex="
                                + lat + "&placey=" + lng + "&textfield=" + place);
            }
            Context context = getApplicationContext();
            CharSequence text = "Data Uploaded to site";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            ClearTheData();
        }
    }

    private class UpdateSite extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... strings) {
            String string = strings[0];
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
            Context Context = getApplicationContext();
            if (ni != null) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(string);
                try {
                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httpget);
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return true;
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DumfriesPlaceFinderActivity.this.finish();
    }

    public static class SingleShotLocationProvider {

        public interface LocationCallback {
            void onNewLocationAvailable(GPSCoordinates location);
        }

        // calls back to calling thread, note this is for low grain: if you want higher precision, swap the
        // contents of the else and if. Also be sure to check gps permission/settings are allowed.
        // call usually takes <10ms
        @SuppressLint("MissingPermission")
        public static void requestSingleUpdate(final Context context, final LocationCallback callback) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);

                locationManager.requestSingleUpdate(criteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude()));
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }, null);
            } else {
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isGPSEnabled) {
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    locationManager.requestSingleUpdate(criteria, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude()));
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    }, null);
                }
            }
        }

        // consider returning Location instead of this dummy wrapper class
        public static class GPSCoordinates {
            public float longitude = -1;
            public float latitude = -1;

            public GPSCoordinates(float theLatitude, float theLongitude) {
                longitude = theLongitude;
                latitude = theLatitude;
            }

            public GPSCoordinates(double theLatitude, double theLongitude) {
                longitude = (float) theLongitude;
                latitude = (float) theLatitude;
            }
        }
    }
}