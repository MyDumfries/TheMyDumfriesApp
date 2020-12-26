package com.mydumfries.themydumfriesapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendEmail extends Activity {
	private Button SendEmailButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_tweet);
		final String Name=getIntent().getStringExtra("name");
		final String Email=getIntent().getStringExtra("email");
		final EditText EmailText = (EditText) findViewById(R.id.message);
		TextView Message = (TextView) findViewById(R.id.posttweettext);
		Message.setText("Enter your message to "+Name+" below. Remember to include your name and e-mail address if you want a reply.");
		TextView Remaining = (TextView) findViewById(R.id.Remaining);
		TextView Edittext = (TextView) findViewById(R.id.editText2);
		Remaining.setVisibility(View.GONE);
		Edittext.setVisibility(View.GONE);
		Button CancelButton= (Button) findViewById(R.id.button_cancel);
		CancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SendEmail.this,
						DumfriesPeople.class));
				finish();
			}
		});
		SendEmailButton = (Button) findViewById(R.id.button_post);
		SendEmailButton.setText("Send");
		SendEmailButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String Text = EmailText.getText().toString();
				composewifi(Text,Email);
				finish();
			}
		});
	}

	private void composewifi(String Text, String Email)
	{
		final ConnectionDetector cd;
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			new AlertDialog.Builder(this)
			.setTitle("No Internet Connection") 
			.setMessage("Please establish a connection and re-try.") 
			.setCancelable(false) 	                	   
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
				@Override
				public void onClick(DialogInterface dialog, int id) { 
					return;
				} 
			})  
			.show(); 
		}
		else
		{
    		try {
				Text = URLEncoder.encode(Text, "utf-8");
				Email = URLEncoder.encode(Email, "utf-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            new UpdateSite()
			.execute("http://www.mydumfries.com/RemoteEmail.php?email="+Email+"&text="+Text);
            Toast.makeText(SendEmail.this, "Your message has been sent. Unfortunately, there is no way to confirm if the e-mail has been received.", Toast.LENGTH_LONG).show();
		}
	}
	private class UpdateSite extends AsyncTask<String, Void, Boolean> {
		protected Boolean doInBackground(String... strings) {
			String string = strings[0];
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
	}
}
