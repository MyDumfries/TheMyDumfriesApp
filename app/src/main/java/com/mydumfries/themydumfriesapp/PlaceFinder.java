package com.mydumfries.themydumfriesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class PlaceFinder extends Activity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;
    EventDataSQLHelper placesData;
    GPSTracker gps = null;
    boolean refreshing=FALSE;
    int stripe = 0;
    double latitude = 0;
    double longitude = 0;
    Location currentlocation;
    int places;
    ArrayList<Place> placesarray;
    GoogleApiClient mGoogleApiClient;
    int searchdistance=0;
    boolean localdatabase=FALSE;
    boolean remotedatabase=FALSE;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.placefinder);
        final TextView whatdb = (TextView) findViewById(R.id.whatdatabase);
        whatdb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (localdatabase)
                {
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null )
                    {
                        if (networkInfo.isConnected()) {
                            remotedatabase = TRUE;
                            localdatabase=FALSE;
                            whatdb.setText("REMOTE");
                        }
                    }
                    if (remotedatabase==FALSE)
                    {
                        CharSequence text3 = "Please establish a Network Connection and try again.";
                        new AlertDialog.Builder(PlaceFinder.this)
                                .setTitle("No Network Connection.")
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
                }
                else if (remotedatabase)
                {
                    final SQLiteDatabase db = placesData.getReadableDatabase();
                    Cursor cur = db.rawQuery("SELECT * FROM " + EventDataSQLHelper.TABLE, null);
                    if (cur != null) {
                        cur.moveToFirst();
                        int debug=cur.getCount();
                        if (debug > 5) {                // Zero count means empty table.
                            localdatabase=TRUE;
                            remotedatabase=FALSE;
                            whatdb.setText("LOCAL");
                        }
                    }
                    if (localdatabase==FALSE)
                    {
                        CharSequence text3 = "Would you like to download it?";
                        new AlertDialog.Builder(PlaceFinder.this)
                                .setTitle("No Local Database Available.")
                                .setMessage(text3)
                                .setCancelable(false)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(PlaceFinder.this, "DataBase Will Be Downloaded in Background", Toast.LENGTH_LONG).show();
                                        downloadDatabase();
                                        return;
                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        return;
                                    }
                                })
                                .show();
                    }
                    cur.close();
                }
            }
        });
        placesarray = new ArrayList<Place>();
        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        currentlocation = new Location("");
        SingleShotLocationProvider.requestSingleUpdate(PlaceFinder.this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(PlaceFinder.SingleShotLocationProvider.GPSCoordinates location) {
                        latitude = location.latitude;
                        longitude = location.longitude;
                        if (latitude < 55.27755086530207 && latitude > 54.85210585589739 && longitude < (-3.0239868164025) && longitude > (-4.08416748046875)) {
                            currentlocation.setLatitude(latitude);
                            currentlocation.setLongitude(longitude);
                        }
                    }
                });
        placesData = new EventDataSQLHelper(this);
        final SQLiteDatabase db = placesData.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + EventDataSQLHelper.TABLE, null);
        if (cur != null) {
            cur.moveToFirst();
            int debug=cur.getCount();
            if (debug > 5) {
               localdatabase=TRUE;
            }
        }
        cur.close();
        String sql = "create table if not exists " + EventDataSQLHelper.TABLE3
                + "( " + BaseColumns._ID + " integer primary key, "
                + EventDataSQLHelper.PLACENAME + " text, "
                + EventDataSQLHelper.LNG + " text, " + EventDataSQLHelper.LAT
                + " text);";
        db.execSQL(sql);
        final String SearchText = getIntent().getStringExtra("searchtext");
        if (!TextUtils.isEmpty(SearchText)) {
            hideSoftKeyBoard();
            TableLayout booksTable = (TableLayout) findViewById(R.id.tableLayout1);
            booksTable.removeAllViews();
            placesarray.clear();
            db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
            remoteDatabase(SearchText, 1);
        }
        final Button search = (Button) findViewById(R.id.submitsearch);
        ImageView refresh = (ImageView) findViewById(R.id.Refreshbutton);
        ImageView map = (ImageView) findViewById(R.id.Mapbutton);
        ImageView downloaddb = (ImageView) findViewById(R.id.Databasebutton);
        ImageView newplace = (ImageView) findViewById(R.id.NewPlacebutton);
        ImageView PlaceFinderHelp = (ImageView) findViewById(R.id.findplaceinfobutton);
        PlaceFinderHelp.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PlaceFinder.this, HelpActivity.class);
                intent.putExtra("source", 3);
                startActivity(intent);
            }
        });
        final EditText searchtext = (EditText) findViewById(R.id.searchterm);
        final SharedPreferences Settings = getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        final Long nextupdate = Settings.getLong("nextupdate", 0);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        final long now = new Date().getTime();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null )
        {
            if (networkInfo.isConnected()) {
                 remotedatabase = TRUE;
             }
        }
        if (localdatabase==TRUE && remotedatabase==TRUE)
        {
            new AlertDialog.Builder(PlaceFinder.this)
                    .setTitle("Which DataBase Do You Want to Use?")
                    .setMessage("Please select the DataBase to use.")
                    .setCancelable(false)
                    .setPositiveButton("Local", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            remotedatabase=FALSE;
                            whatdb.setText("LOCAL");
                            return;
                        }
                    })
                    .setNegativeButton("Remote", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            localdatabase=FALSE;
                            whatdb.setText("REMOTE");
                            return;
                        }
                    })
                    .show();
        }
        if (localdatabase==FALSE && remotedatabase==FALSE)
        {
                CharSequence text3 = "No Database available. Please establish a Network Connection and try again.";
                new AlertDialog.Builder(this)
                        .setTitle("No Data Available.")
                        .setMessage(text3)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                whatdb.setText("NONE AVAILABLE");
                                return;
                            }
                        })
                        .show();
        }
        if (localdatabase) whatdb.setText("LOCAL");
        if (remotedatabase) whatdb.setText("REMOTE");
        if (nextupdate < now && localdatabase) {
            new AlertDialog.Builder(PlaceFinder.this)
                    .setTitle("Download DataBase.")
                    .setMessage("You haven't updated the DataBase for a while. Would you like to do it now?")
                    .setCancelable(false)
                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(PlaceFinder.this, "DataBase Will Be Downloaded in Background", Toast.LENGTH_LONG).show();
                            downloadDatabase();
                            return;
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Long anextupdate = now + (DateUtils.WEEK_IN_MILLIS * 8);
                            Editor editor = Settings.edit();
                            editor.putLong("nextupdate", anextupdate);
                            editor.commit();
                            return;
                        }
                    })
                    .show();
        }
        map.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent location = new Intent(PlaceFinder.this, PlaceFinderGoogleMapsAPI.class);
                if (places == 0) location.putExtra("display", 1);
                startActivity(location);
            }
        });
        newplace.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(PlaceFinder.this,
                        DumfriesPlaceFinderActivity.class));
            }
        });
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner catspinner = (Spinner) findViewById(R.id.category);
        catspinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyBoard();
                return false;
            }
        });
        catspinner.setAdapter(adapter);
        search.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (searchtext.getText().toString().length() < 2) return;
                SingleShotLocationProvider.requestSingleUpdate(PlaceFinder.this,
                        new SingleShotLocationProvider.LocationCallback() {
                            @Override
                            public void onNewLocationAvailable(PlaceFinder.SingleShotLocationProvider.GPSCoordinates location) {
                                latitude = location.latitude;
                                longitude = location.longitude;
                                if (latitude < 55.27755086530207 && latitude > 54.85210585589739 && longitude < (-3.0239868164025) && longitude > (-4.08416748046875)) {
                                    currentlocation.setLatitude(latitude);
                                    currentlocation.setLongitude(longitude);
                                }
                            }
                        });
                hideSoftKeyBoard();
                catspinner.setSelection(0);
                TableLayout booksTable = (TableLayout) findViewById(R.id.tableLayout1);
                booksTable.removeAllViews();
                placesarray.clear();
                final SQLiteDatabase db = placesData.getReadableDatabase();
                db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
                if (remotedatabase) {
                    remoteDatabase(searchtext.getText().toString(), 1);
                }
                if (localdatabase) {
                    localDatabase(searchtext.getText().toString(), 1);
                }
            }
        });
        final Button catbutton = (Button) findViewById(R.id.submitcat);
        catbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                hideSoftKeyBoard();
                searchtext.setText("");
                SingleShotLocationProvider.requestSingleUpdate(PlaceFinder.this,
                        new SingleShotLocationProvider.LocationCallback() {
                            @Override
                            public void onNewLocationAvailable(PlaceFinder.SingleShotLocationProvider.GPSCoordinates location) {
                                latitude = location.latitude;
                                longitude = location.longitude;
                                if (latitude < 55.27755086530207 && latitude > 54.85210585589739 && longitude < (-3.0239868164025) && longitude > (-4.08416748046875)) {
                                    currentlocation.setLatitude(latitude);
                                    currentlocation.setLongitude(longitude);
                                }
                            }
                        });
                placesarray.clear();
                final String category = catspinner.getSelectedItem().toString();
                TableLayout booksTable = (TableLayout) findViewById(R.id.tableLayout1);
                booksTable.removeAllViews();
                final SQLiteDatabase db = placesData.getReadableDatabase();
                db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
                int type=2;
                if (category.equals("Within XXXm") && refreshing)
                {
                    type=1;
                }
                if (category.equals("Within XXXm") && !refreshing)
                {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View layout = inflater.inflate(R.layout.distance_dialog,
                            (ViewGroup) findViewById(R.id.root));
                    final EditText search = (EditText) layout
                            .findViewById(R.id.EditText_Distance);

                    // ... other required overrides do nothing
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlaceFinder.this);
                    builder.setView(layout);
                    // Now configure the AlertDialog
                    builder.setTitle("Set Distance");
                    builder.setMessage("Show All Places Within XXXm");
                    builder.setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                }
                            });
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String sdist = search.getText().toString();
                                    searchdistance=Integer.parseInt(sdist);
                                    int type=1;
                                    if (remotedatabase) {
                                        remoteDatabase(category, type);
                                    }
                                    if (localdatabase)
                                    {
                                        localDatabase(category, type);
                                    }
                                }
                            });

                    // Create the AlertDialog and return it
                    AlertDialog searchDialog = builder.create();
                    searchDialog.show();
                }
                else {
                    if (remotedatabase) {
                        remoteDatabase(category, type);
                    }
                    if (localdatabase)
                    {
                        localDatabase(category, type);
                    }
                }
                refreshing=FALSE;
            }
        });
        refresh.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            public void onClick(View v) {
                final String category = catspinner.getSelectedItem().toString();
                if (!category.equals("Categories"))
                {
                    refreshing=TRUE;
                    catbutton.callOnClick();
                }
                else if (!searchtext.equals(""))
                {
                    search.callOnClick();
                }
            }
        });
        downloaddb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(PlaceFinder.this)
                        .setTitle("Download DataBase.")
                        .setMessage("Are you sure you want to download the DataBase. This will use up a small amount of space on your device?")
                        .setCancelable(false)
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(PlaceFinder.this, "DataBase Will Be Downloaded in Background", Toast.LENGTH_LONG).show();
                                downloadDatabase();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        placesData = new EventDataSQLHelper(this);
        final SQLiteDatabase db = placesData.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + EventDataSQLHelper.TABLE3);
        PlaceFinder.this.finish();
    }

    private void processPlaces(XmlPullParser places)
            throws XmlPullParserException, IOException {
        int eventType = places.getEventType();
        boolean bFoundScores = false;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String strName = places.getName();
                if (strName.equals("marker")) {
                    bFoundScores = true;
                    final String lat = places.getAttributeValue(null, "lat");
                    final String lng = places.getAttributeValue(null, "lng");
                    final String html = places.getAttributeValue(null, "html");
                    final String desc = places.getAttributeValue(null, "desc");
                    final String addr = places.getAttributeValue(null, "addr");
                    insertPlaceRow(lat, lng, html, desc, addr);
                }
            }
            eventType = places.next();
        }
        // Handle no scores available
        if (bFoundScores == false) {
            insertPlaceRow("0", "0", "No Results Found", "", "");
        }
        Collections.sort(placesarray);
        for (Place place : placesarray) {
            float distance = place.distance;
            final String html2 = place.html2;
            final String addr = place.addr;
            final String desc = place.desc;
            final String lat = place.lat;
            final String lng = place.lng;
            String html;
            if (distance > 0 && distance < 500) {
                String dist = String.valueOf(distance);
                dist = dist.substring(0, 4);
                html = html2 + "\n" + desc + "\n" + addr + "\n" + dist + "km";
            } else {
                html = html2 + "\n" + desc + "\n" + addr;
            }
            final TableLayout booksTable = (TableLayout) findViewById(R.id.tableLayout1);
            final TableRow newRow = new TableRow(this);
            final TableRow newRow2 = new TableRow(this);
            newRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.FILL_PARENT, 1.0f));
            newRow2.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.FILL_PARENT, 1.0f));
            addTextToRowWithValues(newRow2, "____________________________________");
            addTextToRowWithValues(newRow, html);
            newRow.setPadding(0, 0, 0, 10);
            if (stripe == 0) {
                newRow.setBackgroundColor(Color.parseColor("#B3B3B3"));
            } else {
                newRow.setBackgroundColor(Color.parseColor("#FFF333"));
            }
            stripe = stripe + 1;
            if (stripe == 2) stripe = 0;
            newRow.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(PlaceFinder.this)
                            .setTitle("Place Details.")
                            .setMessage(html2 + "\n" + addr + "\n" + desc)
                            .setCancelable(false)
                            .setPositiveButton("View on Map", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    ContentValues values = new ContentValues();
                                    values.put(EventDataSQLHelper.LAT, lat);
                                    values.put(EventDataSQLHelper.LNG, lng);
                                    values.put(EventDataSQLHelper.PLACENAME, html2);
                                    final SQLiteDatabase db = placesData.getReadableDatabase();
                                    db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
                                    db.insert(EventDataSQLHelper.TABLE3, null, values);
                                    startActivity(new Intent(PlaceFinder.this,
                                            PlaceFinderGoogleMapsAPI.class));
                                    return;
                                }
                            })
                            .setNeutralButton("Report", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("message/rfc822");
                                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"stuart@mydumfries.com"});
                                    i.putExtra(Intent.EXTRA_SUBJECT, html2);
                                    i.putExtra(Intent.EXTRA_TEXT, "I think there may be something wrong with the above place. Please take a look.");
                                    try {
                                        startActivity(Intent.createChooser(i, "Send mail..."));
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toast.makeText(PlaceFinder.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    return;
                                }
                            })
                            .show();
                }
            });
            runOnUiThread(new Runnable() {
                public void run() {
                    // stuff that updates ui
                    booksTable.addView(newRow);
                }
            });
        }
        runOnUiThread(new Runnable() {
            public void run() {
                // stuff that updates ui
                hideSoftKeyBoard();
            }
        });
    }

    private void insertPlaceRow(final String lat, final String lng, final String html2, final String desc, final String addr) {
        float distance = 0;
        Place place = new Place();
        Location thislocation = new Location("");
        double latitude = 0;
        double longitude = 0;
        try {
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);
        } catch (NumberFormatException e) {
            // p did not contain a valid double
        }
        thislocation.setLongitude(longitude);
        thislocation.setLatitude(latitude);
        String html = null;
        if (currentlocation != null) {
            distance = currentlocation.distanceTo(thislocation) / 1000;
            place.distance = distance;
            place.html2 = html2;
            place.addr = addr;
            place.desc = desc;
            place.lat = lat;
            place.lng = lng;
            String dist = String.valueOf(distance);
            dist = dist.substring(0, 4);
            if (distance < 500) {
                html = html2 + "\n" + desc + "\n" + addr + "\n" + dist + "km";
            } else {
                html = html2;
            }
        } else {
            place.distance = distance;
            place.html2 = html2;
            place.addr = addr;
            place.desc = desc;
            place.lat = lat;
            place.lng = lng;
        }
        if (searchdistance>0)
        {
            if (distance*1000<searchdistance)
            {
                placesarray.add(place);
                ContentValues values = new ContentValues();
                values.put(EventDataSQLHelper.LAT, lat);
                values.put(EventDataSQLHelper.LNG, lng);
                values.put(EventDataSQLHelper.PLACENAME, html2);
                final SQLiteDatabase db = placesData.getReadableDatabase();
                db.insert(EventDataSQLHelper.TABLE3, null, values);
            }
        }
        else {
            placesarray.add(place);
            ContentValues values = new ContentValues();
            values.put(EventDataSQLHelper.LAT, lat);
            values.put(EventDataSQLHelper.LNG, lng);
            values.put(EventDataSQLHelper.PLACENAME, html2);
            final SQLiteDatabase db = placesData.getReadableDatabase();
            db.insert(EventDataSQLHelper.TABLE3, null, values);
        }
    }

    void remoteDatabase(final String searchtext, final int type) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            CharSequence text3 = "Please establish a Network Connection and try again.";
            new AlertDialog.Builder(this)
                    .setTitle("No Network Connection.")
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //do network action in this function
                XmlPullParserFactory factory = null;
                try {
                    factory = XmlPullParserFactory.newInstance();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                factory.setNamespaceAware(true);
                XmlPullParser xpp = null;
                try {
                    xpp = factory.newPullParser();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                URL xmlUrl = null;
                String searchtext2 = null;
                try {
                    searchtext2 = URLEncoder.encode(searchtext, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    if (type == 1) {
                        xmlUrl = new URL(
                                "http://www.mydumfries.com/placefinderMobileApp.php?searchtext=" + searchtext2);
                    }
                    if (type == 2) {
                        xmlUrl = new URL(
                                "http://www.mydumfries.com/placefinderMobileApp.php?category=" + searchtext2);
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // set the input for the parser using an InputStreamReader
                try {
                    xpp.setInput(xmlUrl.openStream(), null);
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//					booksData = new EventDataSQLHelper(this);
                try {
                    processPlaces(xpp);
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void addTextToRowWithValues(final TableRow tableRow, String text) {
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(R.color.purple));
//		textView.setHeight(50);
        textView.setText(text);
        textView.setPadding(0, 0, 0, 5);
        tableRow.addView(textView);
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                5
        ));
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));

        tableRow.addView(v);
        places++;
    }

    private void downloadDatabase() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            CharSequence text3 = "Please establish a Network Connection and try again.";
            new AlertDialog.Builder(this)
                    .setTitle("No Network Connection.")
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
        final SQLiteDatabase db = placesData.getReadableDatabase();
        db.execSQL("delete from " + EventDataSQLHelper.TABLE);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //do network action in this function
                XmlPullParserFactory factory = null;
                try {
                    factory = XmlPullParserFactory.newInstance();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                factory.setNamespaceAware(true);
                XmlPullParser xpp = null;
                try {
                    xpp = factory.newPullParser();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                URL xmlUrl = null;
                SharedPreferences Settings = getSharedPreferences("settings",
                        Context.MODE_PRIVATE);
                String timestamp = Settings.getString("timestamp", "0000-00-00 00:00:00");
                try {
                    xmlUrl = new URL(
                            "http://www.mydumfries.com/placefinderMobileApp.php?timestamp=" + timestamp);
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // set the input for the parser using an InputStreamReader
                try {
                    xpp.setInput(xmlUrl.openStream(), null);
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//					booksData = new EventDataSQLHelper(this);
                try {
                    processPlacesDataBase(xpp);
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // stuff that updates ui
                            Toast.makeText(PlaceFinder.this, "Download of DataBase Failed. Please report by clicking the e-mail link.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // stuff that updates ui
                            Toast.makeText(PlaceFinder.this, "Download of DataBase Failed. Please report by clicking the e-mail link.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(PlaceFinder.this,
                                    MainActivity.class));
                            finish();
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private void processPlacesDataBase(XmlPullParser places)
            throws XmlPullParserException, IOException {
        int eventType = places.getEventType();
        boolean bFoundScores = false;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String strName = places.getName();
                if (strName.equals("marker")) {
                    bFoundScores = true;
                    String id = places.getAttributeValue(null, "id");
                    String name = places.getAttributeValue(null, "name");
                    String addr1 = places.getAttributeValue(null, "addr1");
                    String addr2 = places.getAttributeValue(null, "addr2");
                    String postcode = places.getAttributeValue(null, "postcode");
                    String cat1 = places.getAttributeValue(null, "cat1");
                    String cat2 = places.getAttributeValue(null, "cat2");
                    String cat3 = places.getAttributeValue(null, "cat3");
                    String cat4 = places.getAttributeValue(null, "cat4");
                    String cat5 = places.getAttributeValue(null, "cat5");
                    String description = places.getAttributeValue(null, "desc");
                    String url = places.getAttributeValue(null, "url");
                    String more = places.getAttributeValue(null, "more");
                    String lat = places.getAttributeValue(null, "lat");
                    String lng = places.getAttributeValue(null, "lng");
                    insertPlaceRow(id, name, addr1, addr2, postcode, cat1, cat2, cat3, cat4, cat5, description, url, more, lat, lng);
                }
            }
            eventType = places.next();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        long now = new Date().getTime();
        long nextupdate = now + (DateUtils.WEEK_IN_MILLIS * 8);
        String timestamp = dateFormat.format(date);
        SharedPreferences Settings = getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        Editor editor = Settings.edit();
        editor.putString("timestamp", timestamp);
        editor.putLong("nextupdate", nextupdate);
        editor.commit();
        runOnUiThread(new Runnable() {
            public void run() {
                // stuff that updates ui
                Toast.makeText(PlaceFinder.this, "Download of DataBase Complete.", Toast.LENGTH_LONG).show();
            }
        });
        // Handle no scores available
        if (bFoundScores == false) {

        }
    }

    private void insertPlaceRow(String id, String name, String addr1,
                                String addr2, String postcode, String cat1, String cat2,
                                String cat3, String cat4, String cat5, String description, String url, String more, String lat, String lng) {
        EventDataSQLHelper placesData;
        placesData = new EventDataSQLHelper(this);
        SQLiteDatabase db = placesData.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BaseColumns._ID, id);
        values.put(EventDataSQLHelper.NAME, name);
        values.put(EventDataSQLHelper.ADDR1, addr1);
        values.put(EventDataSQLHelper.ADDR2, addr2);
        values.put(EventDataSQLHelper.POSTCODE, postcode);
        values.put(EventDataSQLHelper.CAT1, cat1);
        values.put(EventDataSQLHelper.CAT2, cat2);
        values.put(EventDataSQLHelper.CAT3, cat3);
        values.put(EventDataSQLHelper.CAT4, cat4);
        values.put(EventDataSQLHelper.CAT5, cat5);
        values.put(EventDataSQLHelper.DESCRIPTION, description);
        values.put(EventDataSQLHelper.URL, url);
        values.put(EventDataSQLHelper.MORE, more);
        values.put(EventDataSQLHelper.LAT, lat);
        values.put(EventDataSQLHelper.LNG, lng);
        long debug = db.insert(EventDataSQLHelper.TABLE, null, values);
        if (debug == -1) {
            db.update(EventDataSQLHelper.TABLE, values, BaseColumns._ID + "=?", new String[]{id});
        }
    }

    void localDatabase(final String searchtext, final int type) {
        String SearchFor = null;
        if (type == 1) {
            if (searchtext.equals("Within XXXm"))
            {
                SearchFor = "SELECT _id,name,lat,lng,description,addr1,addr2,postcode FROM places";
            }
            else {
                SearchFor = "SELECT _id,name,lat,lng,description,addr1,addr2,postcode FROM places where cat1 LIKE '%" + searchtext + "%' or cat2 LIKE '%" + searchtext + "%' or cat3 LIKE '%" + searchtext + "%' or cat4 LIKE '%" + searchtext + "%' or cat5 LIKE '%" + searchtext + "%' or name LIKE '%" + searchtext + "%' or addr1 LIKE '%" + searchtext + "%' or addr2 LIKE '%" + searchtext + "%' or postcode LIKE '%" + searchtext + "%' or description LIKE '%" + searchtext + "%' ORDER BY lat";
            }
        }
        if (type == 2) {
                SearchFor = "SELECT _id,name,lat,lng,description,addr1,addr2,postcode FROM places where cat1='" + searchtext + "' or cat2='" + searchtext + "' or cat3='" + searchtext + "' or cat4='" + searchtext + "' or cat5='" + searchtext + "' ORDER BY lat";
        }
        try {
            placesData = new EventDataSQLHelper(this);
            TableLayout placesTable = (TableLayout) findViewById(R.id.tableLayout1);
            Cursor cursor = getPlaces(SearchFor, null);
            showPlaces(placesTable, cursor);
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private Cursor getPlaces(String search, String sortorder) {
        SQLiteDatabase db = placesData.getReadableDatabase();
//		$query = "SELECT id,name,lat,lng,description,addr1,addr2,postcode FROM placefinder where cat1=\"$category\" or cat2=\"$category\" or cat3=\"$category\" or cat4=\"$category\" or cat5=\"$category\" or name LIKE \"$searchtext\" or addr1 LIKE \"$searchtext\" or addr2 LIKE \"$searchtext\" or postcode LIKE \"$searchtext\" or description LIKE \"$searchtext\" ORDER BY lat";
        Cursor cursor = db.rawQuery(search, null);
        startManagingCursor(cursor);
        return cursor;
    }

    private void showPlaces(final TableLayout placesTable, Cursor cursor)
            throws ParseException {
        hideSoftKeyBoard();
        while (cursor.moveToNext()) {
            final String lat = cursor.getString(2);
            final String lng = cursor.getString(3);
            final String html = cursor.getString(1);
            final String desc = cursor.getString(4);
            final String addr = cursor.getString(5);
            insertPlaceRow(lat, lng, html, desc, addr);
        }
        if (placesarray.isEmpty()) {
            insertPlaceRow("0", "0", "No Results Found", "", "");
        }
        Collections.sort(placesarray);
        for (Place place : placesarray) {
            float distance = place.distance;
            final String html2 = place.html2;
            final String addr2 = place.addr;
            final String desc2 = place.desc;
            final String lat2 = place.lat;
            final String lng2 = place.lng;
            String html3;
            if (distance > 0 && distance < 500) {
                String dist = String.valueOf(distance);
                dist = dist.substring(0, 4);
                html3 = html2 + "\n" + desc2 + "\n" + addr2 + "\n" + dist + "km";
            } else {
                html3 = html2 + "\n" + desc2 + "\n" + addr2;
            }
            final TableLayout booksTable = (TableLayout) findViewById(R.id.tableLayout1);
            final TableRow newRow = new TableRow(this);
            final TableRow newRow2 = new TableRow(this);
            newRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.FILL_PARENT, 1.0f));
            newRow2.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.FILL_PARENT, 1.0f));
            addTextToRowWithValues(newRow2, "____________________________________");
            addTextToRowWithValues(newRow, html3);
            newRow.setPadding(0, 0, 0, 10);
            if (stripe == 0) {
                newRow.setBackgroundColor(Color.parseColor("#B3B3B3"));
            } else {
                newRow.setBackgroundColor(Color.parseColor("#FFF333"));
            }
            stripe = stripe + 1;
            if (stripe == 2) stripe = 0;
            newRow.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(PlaceFinder.this)
                            .setTitle("Place Details.")
                            .setMessage(html2 + "\n" + addr2 + "\n" + desc2)
                            .setCancelable(false)
                            .setPositiveButton("View on Map", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    ContentValues values = new ContentValues();
                                    values.put(EventDataSQLHelper.LAT, lat2);
                                    values.put(EventDataSQLHelper.LNG, lng2);
                                    values.put(EventDataSQLHelper.PLACENAME, html2);
                                    final SQLiteDatabase db = placesData.getReadableDatabase();
                                    db.execSQL("delete from " + EventDataSQLHelper.TABLE3);
                                    db.insert(EventDataSQLHelper.TABLE3, null, values);
                                    startActivity(new Intent(PlaceFinder.this,
                                            PlaceFinderGoogleMapsAPI.class));
                                    return;
                                }
                            })
                            .setNeutralButton("Report", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("message/rfc822");
                                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"stuart@mydumfries.com"});
                                    i.putExtra(Intent.EXTRA_SUBJECT, html2);
                                    i.putExtra(Intent.EXTRA_TEXT, "I think there may be something wrong with the above place. Please take a look.");
                                    try {
                                        startActivity(Intent.createChooser(i, "Send mail..."));
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toast.makeText(PlaceFinder.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    return;
                                }
                            })
                            .show();
                }
            });
            runOnUiThread(new Runnable() {
                public void run() {
                    // stuff that updates ui
                    booksTable.addView(newRow);
                }
            });
        }
        hideSoftKeyBoard();
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public class Place implements Comparable<Place> {
        float distance;
        String html2;
        String addr;
        String desc;
        String lat;
        String lng;

        @Override
        public int compareTo(Place arg0) {
            // TODO Auto-generated method stub
            return (int) ((this.distance * 1000) - (arg0.distance * 1000));
        }
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        }
        return false;
    }
    public static class SingleShotLocationProvider {

        public interface LocationCallback {
            public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location);
        }

        // calls back to calling thread, note this is for low grain: if you want higher precision, swap the
        // contents of the else and if. Also be sure to check gps permission/settings are allowed.
        // call usually takes <10ms
        @SuppressLint("MissingPermission")
        public static void requestSingleUpdate(final Context context, final SingleShotLocationProvider.LocationCallback callback) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);

                locationManager.requestSingleUpdate(criteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        callback.onNewLocationAvailable(new SingleShotLocationProvider.GPSCoordinates(location.getLatitude(), location.getLongitude()));
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
                            callback.onNewLocationAvailable(new SingleShotLocationProvider.GPSCoordinates(location.getLatitude(), location.getLongitude()));
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