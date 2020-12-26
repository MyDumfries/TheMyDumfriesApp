package com.mydumfries.themydumfriesapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class WhatsOn extends Activity {
	int stripe=0;
    private EventDataSQLHelper placesData;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dumfriespeople);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo==null || !networkInfo.isConnected()) {
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
					WhatsOn.this.finish();
					return;
				}
			})	                	     
			.show();
		}
		Button search=(Button) findViewById(R.id.submitsearch);
		final EditText searchtext=(EditText) findViewById(R.id.searchterm);
		TextView Title=(TextView) findViewById(R.id.textview1);
		Title.setText("WHAT'S ON");
		final TextView AddEvent=(TextView) findViewById(R.id.AddMe);
		AddEvent.setText("|ADD EVENT!");
		ViewGroup.LayoutParams params = AddEvent.getLayoutParams();
		params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		AddEvent.setLayoutParams(params);
		remoteDatabase("",1);
		AddEvent.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	startActivity(new Intent(WhatsOn.this,
						NewWhatsOnActivity.class));
	        }
	    });
		search.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	hideSoftKeyBoard();
	        	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	    		eventsTable.removeAllViews();
	    		remoteDatabase(searchtext.getText().toString(),1);
	        }
	    });
	}
	void remoteDatabase (final String searchtext,final int type)
	{
		Thread thread = new Thread(new Runnable(){
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
				try {
					if (type==1)
					{
					xmlUrl = new URL(
							"http://www.mydumfries.com/WhatsOnMobileApp.php?searchtext="+searchtext);
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
						processEvents(xpp);	
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
	private void processEvents(XmlPullParser events)
			throws XmlPullParserException, IOException {
		int eventType = events.getEventType();
		boolean bFoundScores = false;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String strName = events.getName();
				if (strName.equals("marker")) {
					bFoundScores = true;
					final String what = events.getAttributeValue(null, "what");
					final String when = events.getAttributeValue(null, "when");
					final String until = events.getAttributeValue(null, "until");
					final String where = events.getAttributeValue(null, "where");
					final String cat = events.getAttributeValue(null, "cat");
					final String link = events.getAttributeValue(null, "link");
					runOnUiThread(new Runnable() {
						public void run() {
							// stuff that updates ui
							insertEventRow(what,when,until,where,cat,link);
						}
					});
					
				}
			}
			eventType = events.next();
		}
		// Handle no scores available
		if (bFoundScores == false) {

		}
	}
	private void insertEventRow(final String what, final String when, final String until, final String where, final String cat, final String link) {		
		TableLayout eventTable = (TableLayout) findViewById(R.id.tableLayout1);
		final TableRow newRow = new TableRow(this);
		SimpleDateFormat curFormater = new SimpleDateFormat("yyy-MM-dd");
		Date dateObj = null;
		try {
			dateObj = curFormater.parse(when);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final Date dateObj2=dateObj;
		SimpleDateFormat postFormater = new SimpleDateFormat(
				"dd MMM, yyyy");
		String newDateStr = postFormater.format(dateObj);
		addTextToRowWithValues(newRow, "   ");
		addTextToRowWithValues(newRow, newDateStr);
		addTextToRowWithValues(newRow, "    ");
		addTextToRowWithValues(newRow, what);
		if (stripe==0) {
			newRow.setBackgroundColor(Color.parseColor("#B3B3B3"));
		}
		else
		{
			newRow.setBackgroundColor(Color.parseColor("#FFF333"));
		}
		stripe=stripe+1;
		if (stripe==2) stripe=0;
		newRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SimpleDateFormat postFormater2 = new SimpleDateFormat(
						"EEEE, dd MMM, yyyy");
				String newDateStr2 = postFormater2.format(dateObj2);
				CharSequence text1 =newDateStr2;
				if (!when.substring(11).equals("00:00:00")) text1=text1+" at "+when.substring(11,16);
				if (!when.equals(until))
				{
					SimpleDateFormat curFormater = new SimpleDateFormat("yyy-MM-dd");
					Date dateObj = null;
					try {
						dateObj = curFormater.parse(until);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					postFormater2 = new SimpleDateFormat(
							"EEEE, dd MMM, yyyy");
					newDateStr2 = postFormater2.format(dateObj);
					text1=text1+"\nUntil "+newDateStr2;
				}
				text1=text1+"\n"+what;
				text1=text1+"\nAt "+where;
				text1=text1+"\nLink: "+link;
				  final SpannableString s = 
				               new SpannableString(text1);
				  Linkify.addLinks(s, Linkify.WEB_URLS);
				final AlertDialog alertDialog1 = new AlertDialog.Builder(
						WhatsOn.this).create();
				alertDialog1.setTitle("Event Details");
				alertDialog1.setMessage(s);
//				alertDialog1.setView(message);
				alertDialog1.setButton(DialogInterface.BUTTON_POSITIVE,"OK",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int which) {
								alertDialog1.dismiss();
							}
						});
                alertDialog1.setButton(DialogInterface.BUTTON_NEGATIVE,"Map",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                Intent newIntent = new Intent(WhatsOn.this,
                                        PlaceFinder.class);
                                newIntent.putExtra("searchtext", where);
                                startActivity(newIntent);
                            }
                        });
				alertDialog1.show();
				((TextView)alertDialog1.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		});
		
		eventTable.addView(newRow);
	}
	private void addTextToRowWithValues(final TableRow tableRow, String text) {
		TextView textView = new TextView(this);
		textView.setTextSize(18);
		textView.setTextColor(getResources().getColor(R.color.purple));
		textView.setHeight(50);
		textView.setText(text);
		tableRow.addView(textView);
	}
	private void hideSoftKeyBoard() {
	    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	    if(imm.isAcceptingText()) { // verify if the soft keyboard is open                      
	        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	    }
	}

}
