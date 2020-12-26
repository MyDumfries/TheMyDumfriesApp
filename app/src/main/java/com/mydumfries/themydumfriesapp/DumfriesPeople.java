package com.mydumfries.themydumfriesapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class DumfriesPeople extends Activity {
	String GlobalFirstName;
	String GlobalSurName;
	String Globalemail;
	String GlobalTwitter;
	String GlobalFacebook;
	int stripe;
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
					finish();
					return;
				}
			})	                	     
			.show();
		}
		Button search=(Button) findViewById(R.id.submitsearch);
		final EditText searchtext=(EditText) findViewById(R.id.searchterm);
		final TextView AddMe=(TextView) findViewById(R.id.AddMe);
		AddMe.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	startActivity(new Intent(DumfriesPeople.this,
						NewPersonActivity.class));
	        }
	    });
		search.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	hideSoftKeyBoard();
	        	TableLayout peopleTable = (TableLayout) findViewById(R.id.tableLayout1);
	    		peopleTable.removeAllViews();
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
							"http://www.mydumfries.com/DumfriesPeopleMobileApp.php?searchtext="+searchtext);
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
						processPeople(xpp);	
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
	private void processPeople(XmlPullParser places)
			throws XmlPullParserException, IOException {
		int eventType = places.getEventType();
		boolean bFoundScores = false;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String strName = places.getName();
				if (strName.equals("marker")) {
					bFoundScores = true;
					final String surname = places.getAttributeValue(null, "surname");
					final String firstname = places.getAttributeValue(null, "firstname");
					final String email = places.getAttributeValue(null, "email");
					final String dateofbirth = places.getAttributeValue(null, "dateofbirth");
					final String twitter = places.getAttributeValue(null, "twitter");
					final String facebook = places.getAttributeValue(null, "facebook");
					runOnUiThread(new Runnable() {
						public void run() {
							// stuff that updates ui
							insertPeopleRow(surname,firstname,email,dateofbirth,twitter,facebook);
						}
					});
					
				}
			}
			eventType = places.next();
		}
		// Handle no scores available
		if (bFoundScores == false) {

		}
	}
	private void insertPeopleRow(final String surname, final String firstname, final String email, final String dateofbirth, final String twitter, final String facebook) {		
		TableLayout peopleTable = (TableLayout) findViewById(R.id.tableLayout1);
		final TableRow newRow = new TableRow(this);
		addTextToRowWithValues(newRow, "   ");
		addTextToRowWithValues(newRow, surname);
		addTextToRowWithValues(newRow, ", ");
		addTextToRowWithValues(newRow, firstname);
		addTextToRowWithValues(newRow, "  ");
		addTextToRowWithValues(newRow, dateofbirth.substring(0, 4));
		newRow.setPadding(0,5,0,10);
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
				GlobalFirstName=firstname;
				GlobalSurName=surname;
				Globalemail=email;
				GlobalTwitter=twitter;
				GlobalFacebook=facebook;
				registerForContextMenu(v);
				v.showContextMenu();
			}
		});
		peopleTable.addView(newRow);
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
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info;
		try {
		    info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
		    return;
		}
//		long id = getListAdapter().getItemId(info.position);
		menu.setHeaderTitle("Contact "+GlobalFirstName+" "+GlobalSurName+" By");
		if (!Globalemail.equals(null)) menu.add(0, v.getId(), 0, "e-mail");
		if (!GlobalTwitter.equals("")) menu.add(0, v.getId(), 0, "Twitter");
		if (!GlobalFacebook.equals("")) menu.add(0, v.getId(), 0, "Facebook");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
//		long id = getListAdapter().getItemId(menuInfo.position);
		if (item.getTitle().equals("e-mail")) {
			Intent newIntent = new Intent(DumfriesPeople.this,
					SendEmail.class);
			newIntent.putExtra("email", Globalemail);
			newIntent.putExtra("name", GlobalFirstName+" "+GlobalSurName);
			startActivity(newIntent);
//			Intent i = new Intent(Intent.ACTION_SEND);
//			i.setType("message/rfc822");
//			i.putExtra(Intent.EXTRA_EMAIL  , new String[]{Globalemail});
//			i.putExtra(Intent.EXTRA_SUBJECT, "A message Via MyDumfries.com");
//			i.putExtra(Intent.EXTRA_TEXT   , "I found your e-mail address on MyDumfries.com.");
//			try {
//			    startActivity(Intent.createChooser(i, "Send mail..."));
//			} catch (android.content.ActivityNotFoundException ex) {
//			    Toast.makeText(DumfriesPeople.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//			}
//			RefreshTweets();
		}
		if (item.getTitle().equals("Twitter")) {
			Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/"+GlobalTwitter));  
			startActivity(intent);
		}
		if (item.getTitle().equals("Facebook")) {
			Intent intent;
			 try {
			        getPackageManager()
			                .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
			        if (GlobalFacebook.contains("="))
			        {
			        intent= new Intent (Intent.ACTION_VIEW,
			                Uri.parse("fb://profile/"+GlobalFacebook.substring(GlobalFacebook.indexOf("=")+1))); //Trys to make intent with FB's URI
			        }
			        else
			        {
			        	 intent= new Intent (Intent.ACTION_VIEW,
					                Uri.parse("fb://profile/"+GlobalFacebook));
			        }
			    } catch (Exception e) {
			    	if (GlobalFacebook.contains("="))
			        {
			        intent= new Intent (Intent.ACTION_VIEW,
			                Uri.parse("https://www.facebook.com/profile.php?id="+GlobalFacebook.substring(GlobalFacebook.indexOf("=")+1))); //Trys to make intent with FB's URI
			        }
			        else
			        {
			        	intent=new Intent(Intent.ACTION_VIEW,
				                Uri.parse("https://www.facebook.com/"+GlobalFacebook)); //catches and opens a url to the desired page
			        }
			    }
			 startActivity(intent);
		}
		return true;
	}
}
