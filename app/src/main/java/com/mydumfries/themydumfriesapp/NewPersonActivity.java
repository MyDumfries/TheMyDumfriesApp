package com.mydumfries.themydumfriesapp;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class NewPersonActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newperson);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			CharSequence text3 = "Please establish a Network Connection and try again.";
			new AlertDialog.Builder(this)
			.setTitle("No Network Connection.") 
			.setMessage(text3) 
			.setCancelable(false) 
			.setPositiveButton("OK", new DialogInterface.OnClickListener() 
			{ 
				@Override
				public void onClick(DialogInterface dialog, int id) 
				{	 
					finish();
					return;
				}
			})	                	     
			.show();
		}
		Button OKbutton = (Button) findViewById(R.id.SaveButton);
		OKbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int success=CreateNewPerson();
				if (success==2) NewPersonActivity.this.finish();
			}
		});
		Button Cancelbutton = (Button) findViewById(R.id.CancelButton);
		Cancelbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				NewPersonActivity.this.finish();
			}
		});
		ImageButton FacebookInfo=(ImageButton) findViewById(R.id.facebookinfo);
		ImageButton TwitterInfo=(ImageButton) findViewById(R.id.twitterinfo);
		FacebookInfo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CharSequence text1 = "Log On at facebook.com. At the top left of the menu, beside your Profile Picture, you will see 'Edit Profile'" +
						". Click on this link, then look for the address in your Browser's address bar. In my case it is" +
						" www.facebook.com/Stuart.McLaren.3/about. The 'Stuart.McLaren.3' is the bit that goes in here.";
				final AlertDialog alertDialog1 = new AlertDialog.Builder(
						NewPersonActivity.this).create();
				alertDialog1.setTitle("Facebook Address");
				alertDialog1.setMessage(text1);
				alertDialog1.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								alertDialog1.dismiss();
							}
						});
				alertDialog1.show();
			}
		});
		TwitterInfo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CharSequence text2 = "This is simply your Twitter address (i.e. @MyDumfries) without the '@' symbol.";
				final AlertDialog alertDialog2 = new AlertDialog.Builder(
						NewPersonActivity.this).create();
				alertDialog2.setTitle("Twitter Address");
				alertDialog2.setMessage(text2);
				alertDialog2.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								alertDialog2.dismiss();
							}
						});
				alertDialog2.show();
			}
		});
		TextView blurb = (TextView) findViewById(R.id.TextView_blurb);
		blurb.setText("I've decided that is all the information you really need as it will be possible to find out more from your Facebook or Twitter profiles." +
				"If you would like to add more information to your profile, you can do that at MyDumfries.com.");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		NewPersonActivity.this.finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		NewPersonActivity.this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public int CreateNewPerson() {
		try {
			final EditText Surname = (EditText) findViewById(R.id.EditText_Surname);
			final EditText FirstName = (EditText) findViewById(R.id.EditText_FirstName);
			final EditText Birth = (EditText) findViewById(R.id.EditText_Birth);
			final EditText Email = (EditText) findViewById(R.id.EditText_email);
			final EditText Facebook = (EditText) findViewById(R.id.EditText_facebook);
			final EditText Twitter = (EditText) findViewById(R.id.EditText_twitter);
			final EditText Password = (EditText) findViewById(R.id.EditText_password);
			final EditText ConfirmPassword = (EditText) findViewById(R.id.EditText_confirmpassword);
			String surname = Surname.getText().toString();
			String firstname = FirstName.getText().toString();
			String birth = Birth.getText().toString();
			String email = Email.getText().toString();
			String facebook = Facebook.getText().toString();
			String twitter = Twitter.getText().toString();
			String password = Password.getText().toString();
			String confirmpassword = ConfirmPassword.getText().toString();
			if (!password.equals(confirmpassword)|| password.isEmpty())
			{
				CharSequence text3 = "Passwords do not match. Please re-enter.";
				final AlertDialog alertDialog3 = new AlertDialog.Builder(
						NewPersonActivity.this).create();
				alertDialog3.setTitle("Password Mismatch");
				alertDialog3.setMessage(text3);
				alertDialog3.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								alertDialog3.dismiss();
							}
						});
				alertDialog3.show();
				return 1;
			}
			else if (surname.isEmpty())
			{
				CharSequence text3 = "Surname is required. Please re-enter.";
				final AlertDialog alertDialog3 = new AlertDialog.Builder(
						NewPersonActivity.this).create();
				alertDialog3.setTitle("Missing Input");
				alertDialog3.setMessage(text3);
				alertDialog3.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								alertDialog3.dismiss();
							}
						});
				alertDialog3.show();
				return 1;
			}
			else
			{
			insertPersonRow(surname, firstname, birth, email, facebook, twitter,
					password);
			Context context = getApplicationContext();
			CharSequence text = "Details Added";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return 2;
			}
		} catch (Exception e) {
			Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
			return 2;
		}
	}

	private void insertPersonRow(String surname, String firstname, String birth,
			String email, String facebook, String twitter, String password) {
		new UpdateSite()
		.execute("http://www.mydumfries.com/DumfriesPeopleAddRemote.php?surname="+surname+"&firstname="+firstname+"&birth="+birth+"&email="+email+"&facebook="+facebook+"&twitter="+twitter+"&password="+password);
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
