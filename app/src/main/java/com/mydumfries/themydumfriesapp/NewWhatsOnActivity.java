package com.mydumfries.themydumfriesapp;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker.OnDateChangedListener;

public class NewWhatsOnActivity extends Activity {
	static final int ALARM_DIALOG_ID = 0;
	private DatePicker dpResult;
	private TimePicker tpResult;
	private int year;
	private int month;
	private int day;
	int whichdialog;
	final Calendar c = Calendar.getInstance();
	int minYear = c.get(Calendar.YEAR);
    int minMonth = c.get(Calendar.MONTH);
    int minDay = c.get(Calendar.DAY_OF_MONTH);
    int minHour=c.get(Calendar.HOUR_OF_DAY);
    int minMinute=c.get(Calendar.MINUTE)+5;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newwhatson);
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
				int success=CreateNewEvent();
				if (success==2) NewWhatsOnActivity.this.finish();
			}
		});
		Button Cancelbutton = (Button) findViewById(R.id.CancelButton);
		Cancelbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				NewWhatsOnActivity.this.finish();
			}
		});
		EditText when=(EditText) findViewById(R.id.EditText_When);
		EditText until=(EditText) findViewById(R.id.EditText_Until);
		when.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setCurrentDateOnView();
				whichdialog=1;
				showDialog(ALARM_DIALOG_ID);
			}
		});
		when.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					setCurrentDateOnView();
					whichdialog = 1;
					showDialog(ALARM_DIALOG_ID);
				}
			}
		});
		until.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setCurrentDateOnView();
				whichdialog=2;
				showDialog(ALARM_DIALOG_ID);
			}
		});
		until.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					setCurrentDateOnView();
					whichdialog = 2;
					showDialog(ALARM_DIALOG_ID);
				}
			}
		});
		final Spinner spinner = (Spinner) findViewById(R.id.Spinner_Cat);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add("Other");
		adapter.add("Sport");
		adapter.add("Film");
		adapter.add("Theatre");
		adapter.add("Music");
		adapter.add("Entertainment");
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		NewWhatsOnActivity.this.finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		NewWhatsOnActivity.this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public int CreateNewEvent() {
		try {
			final EditText When = (EditText) findViewById(R.id.EditText_When);
			final EditText Until = (EditText) findViewById(R.id.EditText_Until);
			final EditText What = (EditText) findViewById(R.id.EditText_What);
			final EditText Where = (EditText) findViewById(R.id.EditText_Where);
			final EditText Link = (EditText) findViewById(R.id.EditText_Link);
			final Spinner Cat = (Spinner) findViewById(R.id.Spinner_Cat);
			String when = When.getText().toString();
			when = URLEncoder.encode(when, "utf-8");
			String until = Until.getText().toString();
			until = URLEncoder.encode(until, "utf-8");
			String what = What.getText().toString();
			what = URLEncoder.encode(what, "utf-8");
			String where = Where.getText().toString();
			where = URLEncoder.encode(where, "utf-8");
			String link = Link.getText().toString();
			link = URLEncoder.encode(link, "utf-8");
			String cat = (String) Cat.getItemAtPosition(Cat
					.getSelectedItemPosition());
            cat=URLEncoder.encode(cat,"utf-8");
			if (what.isEmpty()||when.isEmpty())
			{
				CharSequence text3 = "Required field missing. Please re-enter.";
				final AlertDialog alertDialog3 = new AlertDialog.Builder(
						NewWhatsOnActivity.this).create();
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
			insertEventRow(when,until,what,where,link,cat);
			Context context = getApplicationContext();
			CharSequence text = "Details Added";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return 2;
		} catch (Exception e) {
			Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
			return 2;
		}
	}

	private void insertEventRow(String when, String until, String what,
			String where, String link, String cat) {
		new UpdateSite()
		.execute("http://www.mydumfries.com/WhatsOnAddRemote.php?when="+when+"&until="+until+"&what="+what+"&where="+where+"&link="+link+"&cat="+cat);
	}
	private class UpdateSite extends AsyncTask<String, Void, Boolean> {
		protected Boolean doInBackground(String... strings) {
			String string = strings[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(string);
			try { 
                // Execute HTTP Post Request 
                HttpResponse debugresponse = httpclient.execute(httpget);
                String debug = debugresponse.toString();
                debug.concat("a");
            } catch (ClientProtocolException e) { 
                // TODO Auto-generated catch block 
            } catch (IOException e) { 
                // TODO Auto-generated catch block 
            }

			return true;
		}
	}
	public void setCurrentDateOnView() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.date_picker,
				(ViewGroup) findViewById(R.id.root));
		dpResult = (DatePicker) layout.findViewById(R.id.alarmdate);
		tpResult= (TimePicker) layout.findViewById(R.id.alarmtime);
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		// set current date into datepicker
		dpResult.init(year, month, day, new OnDateChangedListener()
        {

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            if (year < minYear)
                view.updateDate(minYear, minMonth, minDay);
                if (monthOfYear < minMonth && year == minYear)
                view.updateDate(minYear, minMonth, minDay);
                if (dayOfMonth < minDay && year == minYear && monthOfYear == minMonth)
                view.updateDate(minYear, minMonth, minDay);
        }});
		tpResult.setCurrentHour(minHour);
		tpResult.setCurrentMinute(minMinute);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ALARM_DIALOG_ID:
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.date_picker,
					(ViewGroup) findViewById(R.id.root));
			tpResult = (TimePicker) layout.findViewById(R.id.alarmtime);
			dpResult = (DatePicker) layout.findViewById(R.id.alarmdate);
			// ... other required overrides do nothing
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			// Now configure the AlertDialog
			builder.setTitle("Pick Date");
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// We forcefully dismiss and remove the Dialog, so
							// it
							// cannot be used again (no cached info)
							NewWhatsOnActivity.this.removeDialog(ALARM_DIALOG_ID);
						}
					});
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int month = dpResult.getMonth() + 1;
							String Smonth = String.valueOf(month);
							if (month < 10) {
								Smonth = "0" + Smonth;
							}
							int day = dpResult.getDayOfMonth();
							String Sday = String.valueOf(day);
							if (day < 10) {
								Sday = "0" + Sday;
							}
							int year = dpResult.getYear();
							String Syear = String.valueOf(year);
							int hour = tpResult.getCurrentHour();
							String Shour = String.valueOf(hour);
							if (hour < 10) {
								Shour = "0" + Shour;
							}
							int minute = tpResult.getCurrentMinute();
							String Sminute = String.valueOf(minute);
							if (minute < 10) {
								Sminute = "0" + Sminute;
							}
							String theDate = Syear + "-" + Smonth + "-" + Sday
									+ " " + Shour + ":" + Sminute+":00";
							if (whichdialog==1)
							{
							EditText when=(EditText) findViewById(R.id.EditText_When);
							when.setText(theDate);
							}
							if (whichdialog==2)
							{
							EditText until=(EditText) findViewById(R.id.EditText_Until);
							until.setText(theDate);
							}
							NewWhatsOnActivity.this.removeDialog(ALARM_DIALOG_ID);
						}
					});

			// Create the AlertDialog and return it
			AlertDialog alarmDialog = builder.create();
			return alarmDialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case ALARM_DIALOG_ID:
			// Handle any Password Dialog initialization here
			// Since we don't want to show old password dialogs, just set new
			// ones, we need not do anything here
			// Because we are not "reusing" password dialogs once they have
			// finished, but removing them from
			// the Activity Dialog pool explicitly with removeDialog() and
			// recreating them as needed.
			return;
		}
	}
	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			try {
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
}
