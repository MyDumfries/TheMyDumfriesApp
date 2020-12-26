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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		final TextView feedback = (TextView) findViewById(R.id.feedback);
		feedback.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"stuart@mydumfries.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Feedback on The MyDumfries App");
				i.putExtra(Intent.EXTRA_TEXT   , "Please send me your feedback.");
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(MainMenu.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		final TextView twitter = (TextView) findViewById(R.id.tweets);
		twitter.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this,
						TwitterActivity.class));
			}
		});
		ImageButton TweetHelp=(ImageButton) findViewById(R.id.tweetsinfo);
		TweetHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent twitter=new Intent(MainMenu.this,HelpActivity.class);
            	twitter.putExtra("source", 1);
            	startActivity(twitter);
			}
		});
		final TextView messageboard = (TextView) findViewById(R.id.messageboard);
		messageboard.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String url = "http://www.mydumfries.com/phpBB3/";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		ImageButton MessageBoardHelp=(ImageButton) findViewById(R.id.messageboardinfo);
		MessageBoardHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent MessageBoard=new Intent(MainMenu.this,HelpActivity.class);
            	MessageBoard.putExtra("source", 2);
            	startActivity(MessageBoard);
			}
		});
		final TextView placefinder = (TextView) findViewById(R.id.places);
		placefinder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this,
						PlaceFinder.class));
			}
		});
		ImageButton PlaceFinderHelp=(ImageButton) findViewById(R.id.placesinfo);
		PlaceFinderHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(MainMenu.this,HelpActivity.class);
            	intent.putExtra("source", 3);
            	startActivity(intent);
			}
		});
		final TextView whatson = (TextView) findViewById(R.id.whatson);
		whatson.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this,
						WhatsOn.class));
			}
		});
		ImageButton WhatsOnHelp=(ImageButton) findViewById(R.id.whatsoninfo);
		WhatsOnHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(MainMenu.this,HelpActivity.class);
            	intent.putExtra("source", 4);
            	startActivity(intent);
			}
		});
		final TextView dumfriestoday = (TextView) findViewById(R.id.today);
		dumfriestoday.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this,
						DumfriesToday.class));
			}
		});
		ImageButton DumfriesTodayHelp=(ImageButton) findViewById(R.id.todayinfo);
		DumfriesTodayHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(MainMenu.this,HelpActivity.class);
				intent.putExtra("source", 7);
				startActivity(intent);
			}
		});
	final TextView dumfriespeople = (TextView) findViewById(R.id.people);
	dumfriespeople.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(MainMenu.this,
					DumfriesPeople.class));
		}
	});
	ImageButton DumfriesPeopleHelp=(ImageButton) findViewById(R.id.peopleinfo);
	DumfriesPeopleHelp.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
			Intent intent=new Intent(MainMenu.this,HelpActivity.class);
        	intent.putExtra("source", 5);
        	startActivity(intent);
		}
	});
}
}
