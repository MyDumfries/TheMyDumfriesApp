package com.mydumfries.themydumfriesapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.Manifest;

import static android.os.Environment.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class DumfriesToday extends Activity {
    static final String pathtosdcard = getExternalStorageDirectory().getPath();
    static final String sdpath = pathtosdcard + "/themydumfriesapp/";
    private static final int CAMERA_REQUEST = 1888;
    List<String> ServerfileList = null;
    private int STORAGE_PERMISSION_CODE = 23;
    private int WRITE_PERMISSION_CODE = 24;
    boolean LOCAL=FALSE;
    protected boolean _taken = true;
    String mCurrentPhotoPath;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dumfriestoday);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        ImageButton DumfriesTodayHelp=(ImageButton) findViewById(R.id.DumfriesTodayInfoButton);
        DumfriesTodayHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(DumfriesToday.this,HelpActivity.class);
                intent.putExtra("source", 7);
                startActivity(intent);
            }
        });
        ImageView takePhoto = (ImageView) findViewById(R.id.TakePhotobutton);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String timeStamp = new SimpleDateFormat("ddMMyyyyHHmmssSS").format(new Date());
                String imageFileName = timeStamp;
                File storageDir = getExternalFilesDir(DIRECTORY_PICTURES);
                File image = null;
                try {
                    image = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Save a file: path for use with ACTION_VIEW intents
                mCurrentPhotoPath = image.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(DumfriesToday.this,
                        "com.mydumfries.themydumfriesapp.fileprovider",
                        image);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        if(isReadStorageAllowed()){

        }
        else {
            requestStoragePermission();
        }
        if(isWriteStorageAllowed()){

        }
        else {
            requestStoragePermission();
        }
        checkforphotos();
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            try {
                showphotos();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean isReadStorageAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        //If permission is not granted returning false
        return false;
    }

    private boolean isWriteStorageAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            //If the user has denied the permission previously your code will come to this block
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
            }else{
                //Displaying another toast if permission is not granted
            }
        }
        if(requestCode == WRITE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
            }else{
                //Displaying another toast if permission is not granted
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                File file = new File(mCurrentPhotoPath);
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), Uri.fromFile(file));
                FileOutputStream out = new FileOutputStream(pictureFile);
                Bitmap reSizedBitmap=getResizedBitmap(bitmap, 800);
                bitmap.recycle();
                reSizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                uploadphotos();
                file.delete();
                deleteLastFromDCIM();
            } catch (Exception e) {
                e.getCause();
            }
        } else if (resultCode == 0) {
            return;
            //do nothing
        }
    }

    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    private File getOutputMediaFile() {
        String sdcard = this.getFilesDir()+"/themydumfriesapp/pictures";
        long time= System.currentTimeMillis();
        String SnoFound = getDate(time, "ddMMyyyyHHmmssSSS");
        String file = "/" + SnoFound + ".jpg";
        file = sdcard + file;
        File mediaFile;
        File filesdir = new File(String.valueOf(this.getFilesDir()));
        if (!filesdir.exists()) {
            filesdir.mkdirs();
        }
        File mydfsdir = new File(this.getFilesDir()+"/themydumfriesapp");
        if (!mydfsdir.exists()) {
            mydfsdir.mkdirs();
        }
        File folder = new File(sdcard);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        mediaFile = new File(file);
        return mediaFile;
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    void checkforphotos(){
        String pathtosdcard = String.valueOf(this.getFilesDir());
        final String sdcard = pathtosdcard + "/themydumfriesapp/pictures";
        File folder = new File(sdcard);
        List<File> fileList = getListFiles(folder);
        if (fileList.size() != 0) {
            LOCAL=TRUE;
            new AlertDialog.Builder(this)
                    .setTitle("Found Pictures on Phone.")
                    .setMessage("Would You Like to Upload Them Now?")
                    .setCancelable(false)
                    .setPositiveButton("Upload", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            uploadphotos();
                            return;
                        }
                    })
                    .setNeutralButton("Later", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            return;
                        }
                    })
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                FileUtils.cleanDirectory(new File(sdcard));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    })
                    .show();
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    void showphotos() throws IOException {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("Please establish a connection and re-try. You will still be able to take pictures to upload later.  Please press BACK BUTTON then select Dumfries Today from Menu to re-try")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    })
                    .show();
        } else {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int found = 0;
                    String id = null;
                    long time = System.currentTimeMillis();
                    id = getDate(time, "ddMMyyyy");
                    ServerfileList = new ArrayList<>();
                    int timeout = 7000;
                    HttpPost httppost = null;
                    HttpClient httpclient = new DefaultHttpClient();
                    httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
                    httppost = new HttpPost("http://www.mydumfries.com/Pictures/Today/listfiles.php");
                    HttpResponse response = null;
                    try {
                        response = httpclient.execute(httppost);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (response.getEntity() != null) {
                        HttpEntity entity = response.getEntity();
                        InputStream is = null;
                        try {
                            is = entity.getContent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        StringBuilder sb = new StringBuilder();
                        StringBuilder sbdump = new StringBuilder();
                        try {
                            sbdump.append(reader.readLine() + "-");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String line = "0";
                        try {
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "-");
                            }


                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String result = sb.toString();

                        for (String getFile : result.split("-")) {
                            ServerfileList.add(getFile);
                        }

                        if (ServerfileList != null) { // check if dir is not null
                            for (final String tmpf : ServerfileList) {
                                String filename = tmpf;
                                if (filename.length() > 8) filename = filename.substring(0, 8);
                                if (filename.equals(id)) {
                                    found = found + 1;
                                    if (found == 1) {
                                        final ImageButton pic1 = (ImageButton) findViewById(R.id.imageButton1);
                                        final TextView caption1 = (TextView) findViewById(R.id.caption1);
                                        pic1.setVisibility(View.VISIBLE);
                                        final Bitmap bmp = getBitmapFromURL("http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                        if (bmp.getWidth() > bmp.getHeight()) {
                                            Matrix matrix = new Matrix();
                                            matrix.postRotate(90);
                                            final Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic1.setImageBitmap(rotatedBitmap);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic1.setImageBitmap(bmp);
                                                }
                                            });
                                        }
                                        pic1.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Intent picture = new Intent(DumfriesToday.this, ShowPhoto.class);
                                                picture.putExtra("file", "http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                                startActivity(picture);
                                            }
                                        });
                                        final String caption = getcaption(tmpf);
                                        if (!TextUtils.isEmpty(caption)) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    caption1.setVisibility(View.VISIBLE);
                                                    caption1.setText(caption);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            caption1.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    if (found == 2) {
                                        Runtime.getRuntime().gc();
                                        final ImageButton pic2 = (ImageButton) findViewById(R.id.imageButton2);
                                        final TextView caption2 = (TextView) findViewById(R.id.caption2);
                                        pic2.setVisibility(View.VISIBLE);
                                        final Bitmap bmp = getBitmapFromURL("http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                        if (bmp.getWidth() > bmp.getHeight()) {
                                            Matrix matrix = new Matrix();
                                            matrix.postRotate(90);
                                            final Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic2.setImageBitmap(rotatedBitmap);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic2.setImageBitmap(bmp);
                                                }
                                            });
                                        }
                                        pic2.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Intent picture = new Intent(DumfriesToday.this, ShowPhoto.class);
                                                picture.putExtra("file", "http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                                startActivity(picture);
                                            }
                                        });
                                        final String caption = getcaption(tmpf);
                                        if (!TextUtils.isEmpty(caption)) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    caption2.setVisibility(View.VISIBLE);
                                                    caption2.setText(caption);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            caption2.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    if (found == 3) {
                                        Runtime.getRuntime().gc();
                                        final ImageButton pic3 = (ImageButton) findViewById(R.id.imageButton3);
                                        final TextView caption3 = (TextView) findViewById(R.id.caption3);
                                        pic3.setVisibility(View.VISIBLE);
                                        final Bitmap bmp = getBitmapFromURL("http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                        if (bmp.getWidth() > bmp.getHeight()) {
                                            Matrix matrix = new Matrix();
                                            matrix.postRotate(90);
                                            final Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic3.setImageBitmap(rotatedBitmap);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic3.setImageBitmap(bmp);
                                                }
                                            });
                                        }
                                        pic3.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Intent picture = new Intent(DumfriesToday.this, ShowPhoto.class);
                                                picture.putExtra("file", "http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                                startActivity(picture);
                                            }
                                        });
                                        final String caption = getcaption(tmpf);
                                        if (!TextUtils.isEmpty(caption)) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    caption3.setVisibility(View.VISIBLE);
                                                    caption3.setText(caption);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            caption3.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    if (found == 4) {
                                        Runtime.getRuntime().gc();
                                        final ImageButton pic4 = (ImageButton) findViewById(R.id.imageButton4);
                                        final TextView caption4 = (TextView) findViewById(R.id.caption4);
                                        pic4.setVisibility(View.VISIBLE);
                                        final Bitmap bmp = getBitmapFromURL("http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                        if (bmp.getWidth() > bmp.getHeight()) {
                                            Matrix matrix = new Matrix();
                                            matrix.postRotate(90);
                                            final Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic4.setImageBitmap(rotatedBitmap);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pic4.setImageBitmap(bmp);
                                                }
                                            });
                                        }
                                        pic4.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Intent picture = new Intent(DumfriesToday.this, ShowPhoto.class);
                                                picture.putExtra("file", "http://www.mydumfries.com/Pictures/Today/" + tmpf);
                                                startActivity(picture);
                                            }
                                        });
                                        final String caption = getcaption(tmpf);
                                        if (!TextUtils.isEmpty(caption)) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    caption4.setVisibility(View.VISIBLE);
                                                    caption4.setText(caption);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            caption4.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }
                            }
                        }
                        if (found==0)
                        {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    new AlertDialog.Builder(DumfriesToday.this)
                                            .setTitle("No Pictures")
                                            .setMessage("There have been no pictures of Dumfries added today. Please press the Camera button to upload one!")
                                            .setCancelable(false)
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
                    }
                }
            });
            thread.start();
        }
    }

    String getcaption (String result)
    {
        int timeout = 7000;
        result=result.substring(0,result.length() - 4);
        HttpPost httppost = null;
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
        httppost = new HttpPost("http://www.mydumfries.com/Pictures/Today/getcaption.php?photoid="+result);
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();
        InputStream is = null;
        try {
            is = entity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sbdump = new StringBuilder();
        try {
            sbdump.append(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String caption = sbdump.toString();
        if (caption.equals("null")) caption="";
        return caption;
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if (file.getName().contains(".")) {
                        String debug=file.getName();
                        inFiles.add(file);
                    }
                }
            }
            return inFiles;
        }
        return null;
    }

    void uploadphotos() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("Please establish a connection and re-try. Picture will be stored on your phone meantime.  Please press BACK BUTTON then select Dumfries Today from Menu to re-try")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    })
                    .show();
        } else {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String pathtosdcard = String.valueOf(getFilesDir());
                        String sdcard = pathtosdcard + "/themydumfriesapp/pictures";
                        List<File> fileList = new ArrayList<>();
                        fileList = getListFiles(new File(sdcard));
                        ServerfileList = new ArrayList<>();
                        int timeout = 7000;
                        HttpPost httppost = null;
                        HttpClient httpclient = new DefaultHttpClient();
                        httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
                        httppost = new HttpPost("http://www.mydumfries.com/Pictures/Today/listfiles.php");
//                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                        StringBuilder sb = new StringBuilder();
                        StringBuilder sbdump = new StringBuilder();
                        sbdump.append(reader.readLine() + "-");
                        String line = "0";

                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "-");
                        }

                        is.close();
                        String result = sb.toString();

                        for (String getFile : result.split("-")) {
                            ServerfileList.add(getFile);
                        }
                        for (File file : fileList) {
                            if (!ServerfileList.contains(file.getName())) {
                                new uploadFile().execute(file.getName());
                            }
                            else
                            {
                                file.delete();
                            }
                        }
//delete the local copy of the pictures
//                        FileUtils.cleanDirectory(new File(sdcard));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    private class uploadFile extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... strings) {
            ProgressDialog dialog = null;
            String sourceFileUri = strings[0];
            String upLoadServerUri = "http://www.mydumfries.com/UploadToServer.php";
            if (sourceFileUri.endsWith("jpg"))
            {
                upLoadServerUri = "http://www.mydumfries.com/UploadToServer.php";
            }
            String fileName = sourceFileUri;
            int serverResponseCode = 0;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String pathtosdcard = String.valueOf(getFilesDir());
            String sdcard = pathtosdcard + "/themydumfriesapp/pictures/";
            fileName=sdcard+fileName;
            final File sourceFile = new File(fileName);
            if (!sourceFile.isFile()) {
                return null;
            } else {
                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);
                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                    conn.setRequestProperty("Connection", "close");
                    conn.setRequestProperty("uploaded_file", fileName);
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    if (serverResponseCode == 200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String msg = "Thank You for sending your picture.  If you would like a caption added to your picture, submit it below (remember to include your name if you want to be credited).";
                                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View layout = inflater.inflate(R.layout.search_dialog,
                                        (ViewGroup) findViewById(R.id.root));
                                final EditText search = (EditText) layout
                                        .findViewById(R.id.EditText_Search);
                                final ImageView image=(ImageView) layout.findViewById(R.id.imageView2);
                                Bitmap bitmap = null;
                                try {
                                    bitmap = MediaStore.Images.Media
                                            .getBitmap(getContentResolver(), Uri.fromFile(sourceFile));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Bitmap reSizedBitmap=getResizedBitmap(bitmap, 300);
                                bitmap.recycle();
                                sourceFile.delete();
                                image.setImageBitmap(reSizedBitmap);
                                // ... other required overrides do nothing
                                AlertDialog.Builder builder = new AlertDialog.Builder(DumfriesToday.this);
                                builder.setView(layout);
                                // Now configure the AlertDialog
                                builder.setTitle("Add a Caption");
                                builder.setMessage(msg);
                                builder.setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {

                                            }
                                        });
                                builder.setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                String caption = search.getText().toString();
                                                String pathtosdcard = String.valueOf(getFilesDir());
                                                String sdcard = pathtosdcard + "/themydumfriesapp/pictures";
                                                String photoid=sourceFile.toString();
                                                int sdcardlength=sdcard.length();
                                                photoid=photoid.substring(sdcardlength+1);
                                                UpdateSite(caption,photoid);
                                            }
                                        });

                                // Create the AlertDialog and return it
                                AlertDialog captionDialog = builder.create();
                                captionDialog.show();
                            }
                        });
                    }
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (MalformedURLException ex) {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.getStackTrace();
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                return false;
            } // End else block
        }
    }
    private void UpdateSite(String caption, String photoid) {
                try {
                    caption = URLEncoder.encode(caption, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    photoid = URLEncoder.encode(photoid, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                new UpdateSite()
                        .execute("http://www.mydumfries.com/AddCaption.php?folder=Today&photoid="
                                + photoid + "&caption=" + caption);
            }

    private class UpdateSite extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... strings) {
            String string = strings[0];
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
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
                try {
                    showphotos();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return null;
        }
    }
    private void deleteLastFromDCIM() {
        String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.MIME_TYPE};
        final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if (cursor != null) {
            cursor.moveToFirst();
            String filepath = cursor.getString(1);
            File Picture = new File(filepath);
            Picture.delete();
            Context context = getApplicationContext();
            CharSequence text = "Attempted to delete "+filepath+". You may want to use a file explorer to check it has been deleted.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}