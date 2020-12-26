package com.mydumfries.themydumfriesapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShowPhoto extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showphoto);
        final ImageView Picture = (ImageView) findViewById(R.id.imageView1);
        final String file = getIntent().getStringExtra("file");
        final String tmpf=file.substring(41);
        if (!file.contains("http://")) {
            try {
                ExifInterface exif = new ExifInterface(file);
            } catch (IOException e) {

            }
            Bitmap bmp = ShrinkBitmap(file, 600, 600);
            Matrix matrix = new Matrix();
            if (bmp.getHeight() > bmp.getWidth()) {
                matrix.postRotate(00);
            } else {
                matrix.postRotate(90);
            }
            Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            Picture.setImageBitmap(rotatedBitmap);
            final TextView caption = (TextView) findViewById(R.id.pictcaption);
            final String captiontext = getcaption(tmpf);
            if (!TextUtils.isEmpty(captiontext)) {
                        caption.setVisibility(View.VISIBLE);
                        caption.setText(captiontext);
            }
        }
        else {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = getBitmapFromURL(file);
                    Matrix matrix = new Matrix();
                    if (bmp.getHeight() > bmp.getWidth()) {
                        matrix.postRotate(00);
                    } else {
                        matrix.postRotate(90);
                    }
                    final Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    final String captiontext = getcaption(tmpf);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Picture.setImageBitmap(rotatedBitmap);
                            final TextView caption = (TextView) findViewById(R.id.pictcaption);
                            if (!TextUtils.isEmpty(captiontext)) {
                                        caption.setVisibility(View.VISIBLE);
                                        caption.setText(captiontext);
                            }
                        }
                    });
                }
            });
            thread.start();
        }
    }

    Bitmap ShrinkBitmap(String file, int width, int height) {
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);
        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }
        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
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
}