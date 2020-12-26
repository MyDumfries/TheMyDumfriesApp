package com.mydumfries.themydumfriesapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import com.mydumfries.themydumfriesapp.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class MainActivity extends Activity {
	int x=-1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen2);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo!=null && networkInfo.isConnected()) {
			remoteDatabase();
		}
		TextView tt = (TextView) findViewById(R.id.twitter);
		ImageButton StartButton = (ImageButton) findViewById(R.id.imageButton1);
		StartButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	Intent intent=new Intent(MainActivity.this,MainMenu.class);
            	startActivity(intent);
            	MainActivity.this.finish();
	        }
	    });
		tt.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	Intent intent=null;
	        	try{
	        		getPackageManager().getPackageInfo("com.twitter.android",0);
	        		intent =new Intent(Intent.ACTION_VIEW,Uri.parse("twitter://user?user_id=367086496"));
	        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	}catch (Exception e){
	        		intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/MyDumfries"));
	        	}
	        	startActivity(intent);
	        }
	    });
		TransformFilter filter = new TransformFilter(){ 
            public final String transformUrl(final Matcher match, String url) {
                return match.group();
            }
        };
		Pattern mentionPattern = Pattern.compile("@([A-Za-z0-9_-]+)");
        String mentionScheme = "http://www.twitter.com/";
        Linkify.addLinks(tt, mentionPattern, mentionScheme, null, filter);
	}
	void remoteDatabase ()
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
					xmlUrl = new URL(
							"http://www.mydumfries.com/TweetsMobileApp.php");
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
						String[] textArray2=processEvents(xpp);
						int textViewCount = 5;
				    	final String[] textArray = textArray2;
						runOnUiThread(new Runnable() {
							public void run() {
								// stuff that updates ui
								TextView headline=(TextView) findViewById(R.id.HeadlineText);
								headline.setVisibility(View.VISIBLE);
								final TextSwitcher tweet=(TextSwitcher) findViewById(R.id.LatestTweet);
								tweet.setFactory(new ViewFactory() {
		                            
		                            public View makeView() {
		                                // TODO Auto-generated method stub
		                                // create new textView and set the properties like clolr, size etc
		                                TextView myText = new TextView(MainActivity.this);
		                                myText.setTextSize(12);
		                                myText.setTextColor(Color.BLUE);
		                                return myText;
		                            }
		                        });

		                        // Declare the in and out animations and initialize them  
		                        Animation in = AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.slide_in_left);
		                        Animation out = AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.slide_out_right);
		                        
		                        // set the animation type of textSwitcher
		                        tweet.setInAnimation(in);
		                        tweet.setOutAnimation(out);

								tweet.setText(textArray[0]);
								tweet.setVisibility(View.VISIBLE);
								
								tweet.setOnClickListener(new View.OnClickListener() {
		                            
		                            public void onClick(View v) {
		                                // TODO Auto-generated method stub
		                                x++;
		                                // If index reaches maximum reset it
		                                if(x==5)
		                                    x=0;
		                                tweet.setText(textArray[x]);
		                            }
		                        });
							}
						});
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
	private String[] processEvents(XmlPullParser events)
			throws XmlPullParserException, IOException {
		String tv="";
		int textViewCount = 5;
    	String[] textArray = new String[textViewCount];
		int eventType = events.getEventType();
		int i = 0;
		boolean bFoundScores = false;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String strName = events.getName();
				if (strName.equals("marker")) {
					bFoundScores = true;
					final String date = events.getAttributeValue(null, "date");
					final String user = events.getAttributeValue(null, "user");
					final String message = events.getAttributeValue(null, "message");
					final String icon = events.getAttributeValue(null, "icon");
					tv=insertEventRow(date,user,message,icon);
					textArray[i]=tv;
					i=i+1;
				}
			}
			eventType = events.next();
		}
		// Handle no scores available
		if (bFoundScores == false) {

		}
		return textArray;
	}
	private String insertEventRow(final String date, final String user, final String message, final String icon) {
		SimpleDateFormat curFormater = new SimpleDateFormat("yyy-MM-dd");
		Date dateObj = null;
		try {
			dateObj = curFormater.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SimpleDateFormat postFormater = new SimpleDateFormat(
				"dd MMM, yyyy");
		String newDateStr = postFormater.format(dateObj);
		String text;
		text=user+"\n"+message+"\n"+newDateStr;
		return text;
	}
}
