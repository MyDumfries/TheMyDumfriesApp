package com.mydumfries.themydumfriesapp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class HelpActivity extends Activity {
	/** Called when the activity is first created. */
	public static final String DEBUG_TAG = "My Diary Log";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		int source = getIntent().getIntExtra("source", 1);

		// Read raw file into string and populate TextView
		InputStream iFile = null;
		if (source==1) iFile = getResources().openRawResource(R.raw.twitterhelp);
		if (source==2) iFile = getResources().openRawResource(R.raw.messageboardhelp);
		if (source==3) iFile = getResources().openRawResource(R.raw.placeshelp);
		if (source==4) iFile = getResources().openRawResource(R.raw.whatsonhelp);
		if (source==5) iFile = getResources().openRawResource(R.raw.peoplehelp);
		if (source==6) iFile = getResources().openRawResource(R.raw.addplacehelp);
		if (source==7) iFile = getResources().openRawResource(R.raw.dumfriestodayhelp);
		try {
			TextView helpText = (TextView) findViewById(R.id.TextView_HelpText);
			String strFile = inputStreamToString(iFile);
			helpText.setText(strFile);
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "InputStreamToString failure", e);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Intent newIntent=new Intent(HelpActivity.this,MyDiaryActivity.class);
		// newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(newIntent);
		HelpActivity.this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Converts an input stream to a string
	 * 
	 * @param is
	 *            The {@code InputStream} object to read from
	 * @return A {@code String} object representing the string for of the input
	 * @throws IOException
	 *             Thrown on read failure from the input
	 */
	public String inputStreamToString(InputStream is) throws IOException {
		StringBuffer sBuffer = new StringBuffer();
		DataInputStream dataIO = new DataInputStream(is);
		String strLine = null;

		while ((strLine = dataIO.readLine()) != null) {
			sBuffer.append(strLine + "\n");
		}

		dataIO.close();
		is.close();

		return sBuffer.toString();
	}
}
