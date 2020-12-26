package com.mydumfries.themydumfriesapp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import com.google.gson.Gson;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demonstrates how to use a twitter application keys to access a user's timeline
 */
public class TwitterActivity extends ListActivity {
	static final int TWITTER_PHONE_NO = 86444;
	private ListActivity activity;
	final static String ScreenName = "mydumfries";
	final static String LOG_TAG = "rnc";
	ArrayList<Tweet> tweets;
	String lasttweet;
	String previoustweet;
	static final int POST_DIALOG_ID=1;
	private ConnectionDetector cd;
	AlertDialogManager alert = new AlertDialogManager();
	TelephonyManager  tm;  
	int phoneType;  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		tweets = new ArrayList<Tweet>();
		tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		phoneType=tm.getPhoneType();
		registerForContextMenu(getListView());
		downloadTweets();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SharedPreferences Settings = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
    	Editor editor = Settings.edit();
    	if (Settings.contains("last_tweet")) {
    		editor.putString("previous_tweet",Settings.getString("last_tweet", "ERROR") );
    		editor.commit();
		}
		editor.putString("last_tweet",lasttweet );
		editor.commit();
	}

	// download twitter timeline after first checking to see if there is a network connection
	public void downloadTweets() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			String TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=dumfriestweets&count=200&";
			SharedPreferences Settings = getSharedPreferences("settings",
					Context.MODE_PRIVATE);
			lasttweet=Settings.getString("last_tweet", "0");
			previoustweet=Settings.getString("previous_tweet", "0");
	    	if (!lasttweet.equals("0")) {
	    		TwitterStreamURL = TwitterStreamURL +"since_id="+lasttweet+"&";	
			}
			TwitterStreamURL = TwitterStreamURL + "owner_screen_name="+ScreenName;
			new DownloadTwitterTask().execute(TwitterStreamURL);
		} else {
			CharSequence text3=null;
			if (phoneType==TelephonyManager.PHONE_TYPE_NONE)
			{
				text3 = "No Network Connection available and unable to tweet via SMS on this device. Please establish a Network Connection and try again.";
			}
			else
			{
				text3 = "Would You Like to Post a Status Update Via SMS? Otherwise, Please establish a Network Connection and try again.";
			}
			if (phoneType!=TelephonyManager.PHONE_TYPE_NONE)
			{
			new AlertDialog.Builder(this)
			.setTitle("No Network Connection.") 
			.setMessage(text3) 
			.setCancelable(false)
			.setPositiveButton("Post SMS Status", new DialogInterface.OnClickListener() 
			{ 
				@Override
				public void onClick(DialogInterface dialog, int id) 
				{	 
					PostSMS();
					return;
				}
			})
			.setNegativeButton("Close App", new DialogInterface.OnClickListener() { 
				@Override
				public void onClick(DialogInterface dialog, int id) {
					finish();
					return;
				} 
			})  
			.show();
			}
			else
			{
				new AlertDialog.Builder(this)
				.setTitle("No Network Connection.") 
				.setMessage(text3) 
				.setCancelable(false)
				.setNegativeButton("Close App", new DialogInterface.OnClickListener() { 
					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
						return;
					} 
				})  
				.show();
			}
		}
	}

	// Uses an AsyncTask to download a Twitter user's timeline
	private class DownloadTwitterTask extends AsyncTask<String, Void, String> {
		final static String CONSUMER_KEY = "cKquCndTeDdHN8tHzzT55A";
		final static String CONSUMER_SECRET = "Ci8jutTahMQXhUNiFYCyr6MRFfsams2cjakZrmSU";
		final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
		ProgressDialog progressDialog;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progressDialog=ProgressDialog.show(TwitterActivity.this, "Please Wait", "Downloading Selected Dumfries Tweets...");
		}
		
		@Override
		protected String doInBackground(String... screenNames) {
			String result = null;
			result = getTwitterStream(screenNames[0]);
			tweets = jsonToTwitter(result);
			return result;
		}

		// onPostExecute convert the JSON results into a Twitter object (which is an Array list of tweets
		@Override
		protected void onPostExecute(String result) {
//			tweets = jsonToTwitter(result);
			progressDialog.dismiss();
			if (tweets.isEmpty())
			{
				Tweet tweet = new Tweet();
				tweet.content = "No new Tweets to display!";
				tweets.add(tweet);
			}
			TweetListAdaptor adaptor = new TweetListAdaptor(activity,R.layout.list_item, tweets);
	        setListAdapter(adaptor);
		}

		// converts a string of JSON data into a Twitter object
		private ArrayList<Tweet> jsonToTwitter(String result) {
			if (result != null && result.length() > 0) {
				try {
				      JSONArray jsonArray = new JSONArray(result);
				      for (int i = 0; i < jsonArray.length(); i++) {
				        JSONObject jsonObject = jsonArray.getJSONObject(i);
				        JSONObject user = jsonObject.getJSONObject("user");
				        Tweet tweet = new Tweet();
                        tweet.content = jsonObject.getString("text");
                        tweet.idOfStatusToRetweet=jsonObject.getLong("id");
                        tweet.content=tweet.content.replace("&amp;", "&");
                        tweet.author = user.getString("name");
                        tweet.screen = user.getString("screen_name");
                        tweet.image_url=user.getString("profile_image_url");
                        URL url = null;
                		Bitmap avt=null;
                				try {
                					url = new URL(tweet.image_url);
                				} catch (MalformedURLException e1) {
                					// TODO Auto-generated catch block
                					e1.printStackTrace();
                				}
                				try {
                					avt=BitmapFactory.decodeStream(url.openConnection().getInputStream());
                				} catch (IOException e1) {
                					// TODO Auto-generated catch block
                					e1.printStackTrace();
                				}
                			    tweet.avt=avt;
                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                "EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                        dateFormat.toString();
                        dateFormat.setLenient(true);
                        Date created = null;
                        try {
                            created = (Date) dateFormat.parse(jsonObject.getString("created_at"));
                        } catch (Exception e) {
                            System.out.println("Exception: " + e.getMessage());
                            return null;
                        }
                        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        tweet.uformdate=sdf.format(created);
//                        tweet.uformdate=jsonObject.getString("created_at");
                        tweet.date=twitterHumanFriendlyDate(jsonObject.getString("created_at"));
						  if ("Stagecoach West Scotland".equals(tweet.author) || "Dumfries_First".equals(tweet.author))
						  {
							  if (tweet.content.contains("Dumfries")) {
								  tweets.add(tweet);
							  }
						  }
						  else if ("Dumfries Ice Hockey News & Scores".equals(tweet.author))
						  {
							  if (tweet.content.contains("sharks") || tweet.content.contains("Sharks")) {
								  tweets.add(tweet);
							  }
						  }
						  else {
							  tweets.add(tweet);
						  }
                        if (i==0)
                        {
                        	SharedPreferences Settings = getSharedPreferences("settings",
                    				Context.MODE_PRIVATE);
                        	lasttweet=Settings.getString("last_tweet", "0");
                			previoustweet=Settings.getString("previous_tweet", "0");
                        	long l=Long.parseLong(jsonObject.getString("id_str"));
                        	l=l-1;
                        	lasttweet=String.valueOf(l);
                        }				        
				      }
				    } catch (Exception e) {
				      e.printStackTrace();
				    }
			}
			return tweets;
		}

		// convert a JSON authentication object into an Authenticated object
		private Authenticated jsonToAuthenticated(String rawAuthorization) {
			Authenticated auth = null;
			if (rawAuthorization != null && rawAuthorization.length() > 0) {
				try {
					Gson gson = new Gson();
					auth = gson.fromJson(rawAuthorization, Authenticated.class);
				} catch (IllegalStateException ex) {
					// just eat the exception
				}
			}
			return auth;
		}

		private String getResponseBody(HttpRequestBase request) {
			StringBuilder sb = new StringBuilder();
			try {

				DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
				HttpResponse response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				String reason = response.getStatusLine().getReasonPhrase();

				if (statusCode == 200) {

					HttpEntity entity = response.getEntity();
					InputStream inputStream = entity.getContent();

					BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
					String line = null;
					while ((line = bReader.readLine()) != null) {
						sb.append(line);
					}
				} else {
					sb.append(reason);
				}
			} catch (UnsupportedEncodingException ex) {
			} catch (ClientProtocolException ex1) {
			} catch (IOException ex2) {
			}
			return sb.toString();
		}

		private String getTwitterStream(String screenName) {
			String results = null;

			// Step 1: Encode consumer key and secret
			try {
				// URL encode the consumer key and secret
				String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
				String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET, "UTF-8");

				// Concatenate the encoded consumer key, a colon character, and the
				// encoded consumer secret
				String combined = urlApiKey + ":" + urlApiSecret;

				// Base64 encode the string
				String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

				// Step 2: Obtain a bearer token
				HttpPost httpPost = new HttpPost(TwitterTokenURL);
				httpPost.setHeader("Authorization", "Basic " + base64Encoded);
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
				String rawAuthorization = getResponseBody(httpPost);
				Authenticated auth = jsonToAuthenticated(rawAuthorization);

				// Applications should verify that the value associated with the
				// token_type key of the returned object is bearer
				if (auth != null && auth.token_type.equals("bearer")) {

					// Step 3: Authenticate API requests with bearer token
					HttpGet httpGet = new HttpGet(screenName);

					// construct a normal HTTPS request and include an Authorization
					// header with the value of Bearer <>
					httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
					httpGet.setHeader("Content-Type", "application/json");
					// update the results with the body of the response
					results = getResponseBody(httpGet);
				}
			} catch (UnsupportedEncodingException ex) {
			} catch (IllegalStateException ex1) {
			}
			return results;
		}
	}
	public class Tweet {
        String author;
        String screen;
        String content;
        String date;
        String uformdate;
        String image_url;
        long idOfStatusToRetweet;
        Bitmap avt;
	}
	private class TweetListAdaptor extends ArrayAdapter<Tweet> {

        private ArrayList<Tweet> tweets;

        public TweetListAdaptor(Context context,
                                    int textViewResourceId,
                                    ArrayList<Tweet> items) {
                 super(context, textViewResourceId, items);
                 this.tweets = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                        LayoutInflater vi = (LayoutInflater) getSystemService                        
(Context.LAYOUT_INFLATER_SERVICE);
                        v = vi.inflate(R.layout.list_item, null);
                }
                Tweet o = tweets.get(position);
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                TextView ft = (TextView) v.findViewById(R.id.footertext);
                ImageView image = (ImageView) v.findViewById(R.id.avatar);
                bt.setText(o.content);
                tt.setText(o.author + " (@" + o.screen +")");
                ft.setText(o.date);
                image.setImageBitmap(o.avt);
//                new DisplayAvator().execute(o.image_url);
                TransformFilter filter = new TransformFilter() {
                    public final String transformUrl(final Matcher match, String url) {
                        return match.group();
                    }
                };

                Pattern mentionPattern = Pattern.compile("@([A-Za-z0-9_-]+)");
                String mentionScheme = "http://www.twitter.com/";
                Linkify.addLinks(tt, mentionPattern, mentionScheme, null, filter);
                Linkify.addLinks(bt, mentionPattern, mentionScheme, null, filter);

                Pattern hashtagPattern = Pattern.compile("#([A-Za-z0-9_-]+)");
                String hashtagScheme = "http://www.twitter.com/search/";
                Linkify.addLinks(bt, hashtagPattern, hashtagScheme, null, filter);

                Pattern urlPattern = Patterns.WEB_URL;
                Linkify.addLinks(bt, urlPattern, null, null, filter);

                return v;
        }
	}
	public static String twitterHumanFriendlyDate(String dateStr) {
        // parse Twitter date

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        dateFormat.toString();
        dateFormat.setLenient(true);
        Date created = null;
        try {

            created = (Date) dateFormat.parse(dateStr);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }

        // today

        Date today = new Date();
        System.out.println("<<<<<<" + today);
        // how much time since (ms)
        Long duration = today.getTime() - created.getTime();
        System.out.println(">>>>>>>>>>>>>>>" + duration + ">>>>>" + today.getTime() + ">>>>>" + created.getTime());
        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        int day = hour * 24;

        if (duration < second * 7) {

            System.out.println("right now");
            return "right now";
        }

        if (duration < minute) {
            int n = (int) Math.floor(duration / second);
            System.out.println("seconds ago");
            return n + " seconds ago";
        }

        if (duration < minute * 2) {
            System.out.println("about 1 minute ago");
            return "about 1 minute ago";
        }

        if (duration < hour) {
            int n = (int) Math.floor(duration / minute);
            System.out.println(" minutes ago");
            return n + " minutes ago";
        }

        if (duration < hour * 2) {
            System.out.println("about 1 hour ago");
            return "about 1 hour ago";
        }

        if (duration < day) {
            int n = (int) Math.floor(duration / hour);
            System.out.println(" hours ago");
            return n + " hours ago";
        }
        if (duration > day && duration < day * 2) {
            System.out.println(" yesterday");
            return "yesterday";
        }

        if (duration < day * 365) {
            int n = (int) Math.floor(duration / day);
            return n + " days ago";
        } else {
            System.out.println(" over a year ago");
            return "over a year ago";
        }
    }
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info;
		try {
		    info = (AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
		    return;
		}
		long id = getListAdapter().getItemId(info.position);
		menu.setHeaderTitle("Tweets Menu");
		menu.add(0, v.getId(), 0, "Refresh Tweets");
		menu.add(0, v.getId(), 0, "Reload Previous Tweets");
		menu.add(0, v.getId(), 0, "Reload last 100 Tweets");
		if (phoneType!=TelephonyManager.PHONE_TYPE_NONE)
		{
		menu.add(0, v.getId(), 0, "Post a Status Update by SMS");
		}
		menu.add(0, v.getId(), 0, "Post a Status Update by WIFI");
		if (phoneType!=TelephonyManager.PHONE_TYPE_NONE)
		{
		menu.add(0, v.getId(), 0, "Retweet by SMS");
		}
		menu.add(0, v.getId(), 0, "Retweet by WIFI");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		long id = getListAdapter().getItemId(menuInfo.position);
		if (item.getTitle().equals("Refresh Tweets")) {
			RefreshTweets();
		}
		if (item.getTitle().equals("Reload Previous Tweets")) {
			ReloadPreviousTweets();
		}
		if (item.getTitle().equals("Reload last 100 Tweets")) {
			Reload100Tweets();
		}
		if (item.getTitle().equals("Post a Status Update by SMS")) {
			PostSMS();
		}
		if (item.getTitle().equals("Post a Status Update by WIFI")) {
			PostWifi();
		}
		if (item.getTitle().equals("Retweet by WIFI")) {
			RetweetWifi(id);
		}
		if (item.getTitle().equals("Retweet by SMS")) {
			RetweetSMS(id);
		}
		return true;
	}
	void RefreshTweets(){
		tweets.clear();
		downloadTweets();
	}
	void ReloadPreviousTweets(){
		String TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=dumfriestweets&count=200&";
		SharedPreferences Settings = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		tweets.clear();
    	if (Settings.contains("previous_tweet")) {
    		TwitterStreamURL = TwitterStreamURL +"since_id="+Settings.getString("previous_tweet", "0")+"&";	
		}
		TwitterStreamURL = TwitterStreamURL + "owner_screen_name="+ScreenName;
		new DownloadTwitterTask().execute(TwitterStreamURL);
	}
	void Reload100Tweets(){
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		tweets.clear();
		if (networkInfo != null && networkInfo.isConnected()) {
			String TwitterStreamURL = "https://api.twitter.com/1.1/lists/statuses.json?slug=dumfriestweets&count=100&";
			TwitterStreamURL = TwitterStreamURL + "owner_screen_name="+ScreenName;
			new DownloadTwitterTask().execute(TwitterStreamURL);
		} else {
			Log.v(LOG_TAG, "No network connection available.");
			CharSequence text3 = "Please establish a Network Connection and try again.";
			AlertDialog alertDialog3 = new AlertDialog.Builder(
					TwitterActivity.this).create();
			alertDialog3.setTitle("No Network Connection");
			alertDialog3.setMessage(text3);
			alertDialog3.setButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog,
								int which) {
							TwitterActivity.this.finish();;
						}
					});
			alertDialog3.show();
		}
	}

	private class DisplayAvator extends AsyncTask<String, Void, Boolean> {
		URL url = null;
		Bitmap avt;
		protected Boolean doInBackground(String... strings) {
				try {
					url = new URL(strings[0]);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					avt=BitmapFactory.decodeStream(url.openConnection().getInputStream());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			 runOnUiThread(new Runnable() {
			     public void run() {
			    	 ImageView image = (ImageView) findViewById(R.id.avatar);
			    	 image.setImageBitmap(avt);
			     }
			 });
			return null;
		}
	}
	void PostSMS()
	{
		Intent newIntent = new Intent(TwitterActivity.this,
				PostTweet.class);
		newIntent.putExtra("source", 1);
		startActivity(newIntent);
	}
	void PostWifi()
	{
		Intent newIntent = new Intent(TwitterActivity.this,
				PostTweet.class);
		newIntent.putExtra("source", 2);
		startActivity(newIntent);
	}
	void RetweetWifi(long id)
	{
		Tweet o = tweets.get((int) id);
		long idOfStatusToRetweet=o.idOfStatusToRetweet;
		Intent newIntent = new Intent(TwitterActivity.this,
				PostTweet.class);
		newIntent.putExtra("source", 3);
		newIntent.putExtra("idOfStatusToRetweet", idOfStatusToRetweet);
		startActivity(newIntent);
	}
	void RetweetSMS(long id)
	{
		Tweet o = tweets.get((int) id);
		String ReTweet="";
		ReTweet="RT @"+o.screen+": "+o.content;
		ReTweet=ReTweet.substring(0, 140);
		Intent newIntent = new Intent(TwitterActivity.this,
				PostTweet.class);
		newIntent.putExtra("source", 4);
		newIntent.putExtra("ReTweet", ReTweet);
		startActivity(newIntent);
	}
}
