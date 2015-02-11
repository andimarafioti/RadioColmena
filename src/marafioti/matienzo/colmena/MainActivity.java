package marafioti.matienzo.colmena;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	public WebView Wv; // for title gif

	/**
	 * help to toggle between play and pause.
	 */
	private boolean playPause = false;
	String url = "rtsp://celulares.arghosted.com:1935/live/7320.stream"; // stream
	MediaPlayer colmena;
	WakeLock wakeLock;
	WifiLock wifiLock;
	Boolean prepared;
	Button btnPlay;

	/**
	 * remain false till media is not completed, inside OnCompletionListener
	 * make it true.
	 */
	private boolean initialStage = true;
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// activity:

		// Do not let the phone go to sleep (kills stream)
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"myWakeLock");
		// Do not let the device shut off wifi.
		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
				.createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

		// initialize title
		Wv = (WebView) findViewById(R.id.webViewTitle);
		String logo = "file:///android_res/drawable/logo_azul.gif";
		int widthLogo = 401; // Lo saqué de la imagen
		Wv.setInitialScale(getScaleW(widthLogo));
		Wv.loadUrl(logo);

		// initialize lower group ('buttons'):
		// metrics for buttons layout:
		Log.d("buttons", "going to buttons");
		LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);

		RelativeLayout.LayoutParams buttonsParams = new RelativeLayout.LayoutParams(
				(int) (getResources().getDisplayMetrics().widthPixels),
				(int) (getResources().getDisplayMetrics().heightPixels * 0.38));
		buttonsParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		buttons.setLayoutParams(buttonsParams);

		// metrics for social buttons
		Log.d("s buttons", "going to s buttons");
		LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.socialbuttons);
		buttonsLayout.setLayoutParams(new LayoutParams((int) (getResources()
				.getDisplayMetrics().widthPixels * 0.3), (int) (getResources()
				.getDisplayMetrics().heightPixels * 0.38)));

		Button btnFB = (Button) findViewById(R.id.button1);
		btnFB.getLayoutParams().height = (int) (getResources()
				.getDisplayMetrics().heightPixels * 0.11);
		btnFB.getLayoutParams().width = (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.25);
		btnFB.setBackgroundResource(R.drawable.layers_fb_button_bg);

		Button btnTW = (Button) findViewById(R.id.button2);
		btnTW.getLayoutParams().height = (int) (getResources()
				.getDisplayMetrics().heightPixels * 0.11);
		btnTW.getLayoutParams().width = (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.25);

		Button btnWeb = (Button) findViewById(R.id.button3);
		btnWeb.getLayoutParams().height = (int) (getResources()
				.getDisplayMetrics().heightPixels * 0.11);
		btnWeb.getLayoutParams().width = (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.25);

		// metrics for playpause button btnPlay = (Button)
		btnPlay = (Button) findViewById(R.id.buttonplay);
		btnPlay.setBackgroundResource(R.drawable.button_play);
		/*btnPlay.getLayoutParams().height = (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.55);
		btnPlay.getLayoutParams().width = (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.55);
		*/
		LayoutParams btnPlayParams = new LayoutParams((int) (getResources()
				.getDisplayMetrics().widthPixels * 0.55), (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.55));
		btnPlayParams.leftMargin = (int) (getResources()
				.getDisplayMetrics().widthPixels * 0.07);
		int topMargin = (int) ((getResources().getDisplayMetrics().heightPixels * 0.38) - 
				(getResources().getDisplayMetrics().widthPixels * 0.55));
		if (topMargin > 0){
			btnPlayParams.topMargin = topMargin/2;	
		}
		
		btnPlay.setLayoutParams(btnPlayParams);
		

		// initialize middle group ('hoy suena'):
		Log.d("hoysuena", "going to hoysuena");
		LinearLayout hoySuenaField = (LinearLayout) findViewById(R.id.hoysuena);
		hoySuenaField.setPadding(0, 32, 0, 32);
		RelativeLayout.LayoutParams hoySuenaParams = new RelativeLayout.LayoutParams(
				(int) (getResources().getDisplayMetrics().widthPixels),
				(int) (getResources().getDisplayMetrics().heightPixels * 0.38));

		hoySuenaParams.addRule(RelativeLayout.ABOVE, R.id.buttons);
		// ahorasuenaParams.addRule(RelativeLayout.BELOW,R.id.webViewTitle);
		hoySuenaField.setLayoutParams(hoySuenaParams);

		Log.d("hoysuenaIm", "going to hoysuena Im");

		// metrics for hoy suena image:
		int widthHS = 114; // de la imagen
		int heightHS = 34;
		int widthHSfield = (int) ((float) (getScaleW(widthHS) * (float) 0.5));
		int heightHSfield = (int) ((widthHSfield * heightHS) / (float) widthHS);

		ImageView hoysuenaimg = (ImageView) findViewById(R.id.hoysuenaimg);
		hoysuenaimg.setImageResource(R.drawable.hoysuena);
		hoysuenaimg.getLayoutParams().width = widthHSfield;
		hoysuenaimg.getLayoutParams().height = heightHSfield;
		
		TextView hoysuenatxt = (TextView) findViewById(R.id.hoysuenatxt);
		hoysuenatxt.setTextSize(24);
		hoysuenatxt.setTypeface(Typeface.MONOSPACE);
		String test = "\t\t17hs Vivo acá \n\t\t19hs El espacio vacío \n\t\t20hs Mochila\n\t\t22hs Mercurio\n\t\t23hs Alza Melaria";
		hoysuenatxt.setText(test);

		// metrics for ahora suena image
		/*
		 * Will be added on a future release int widthAS = 114; // de la imagen
		 * int heightAS = 24;
		 * 
		 * int widthASfield = (int) ((float) (getScaleW(widthAS) * (float)
		 * 0.5)); int heightASfield = (int) ((widthASfield * heightAS) / (float)
		 * widthAS);
		 * 
		 * ImageView ahorasuena = (ImageView) findViewById(R.id.ahorasuenaimg);
		 * ahorasuena.setImageResource(R.drawable.ahora);
		 * ahorasuena.getLayoutParams().width = widthASfield;
		 * ahorasuena.getLayoutParams().height = heightASfield; Will be added on
		 * a future release
		 */
		Log.e("creating", "created");
		
		
		/* I thought of testing the stream to see if it is online, but it takes too long
		try{
			btnPlay.performClick();
		} catch (Exception e) {
			colmena.pause();
			colmena.reset();
			colmena.release();
			initialStage = true;
			playPause = false;
			if (wifiLock.isHeld()) {
				wifiLock.release();
			}
			if (wakeLock.isHeld()) {
				wakeLock.release();
			}
			btnPlay.setBackgroundResource(R.drawable.button_play);
		}
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Set up ShareActionProvider's default share intent
		MenuItem shareItem = menu.findItem(R.id.menu_item_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(shareItem);
		mShareActionProvider.setShareIntent(getDefaultIntent());

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Defines a default (dummy) share intent to initialize the action provider.
	 * However, as soon as the actual content to be used in the intent is known
	 * or changes, you must update the share intent by again calling
	 * mShareActionProvider.setShareIntent()
	 */
	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		return intent;
	}

	private int getScaleW(int PIC) {
		int displayW = getResources().getDisplayMetrics().widthPixels;

		Double val = (Double.valueOf(displayW) / Double.valueOf(PIC + 16)) * 100d;
		Log.d("Scale: ", String.valueOf(val));
		return val.intValue();
	}

	private int getScaleH(int PIC) {
		int displayH = getResources().getDisplayMetrics().heightPixels;

		Double val = (Double.valueOf(displayH) / Double.valueOf(PIC + 16)) * 100d;
		Log.d("Scale: ", String.valueOf(val));
		return val.intValue();
	}

	public void togglepp(View v) {

		if (!playPause) {
			playPause = true;
			colmena = new MediaPlayer();
			colmena.setAudioStreamType(AudioManager.STREAM_MUSIC);
			wakeLock.acquire();
			wifiLock.acquire();
			if (initialStage) {
				new Player().execute(url);
			} else {
				if (!colmena.isPlaying())
					colmena.start();
			}
			btnPlay.setBackgroundResource(R.drawable.button_pause);
		} else {
			colmena.pause();
			colmena.reset();
			colmena.release();
			initialStage = true;
			playPause = false;
			if (wifiLock.isHeld()) {
				wifiLock.release();
			}
			if (wakeLock.isHeld()) {
				wakeLock.release();
			}
			btnPlay.setBackgroundResource(R.drawable.button_play);
		}
	};

	/**
	 * preparing mediaplayer will take sometime to buffer the content so prepare
	 * it inside the background thread and starting it on UI thread.
	 * 
	 * @author piyush
	 * 
	 */

	class Player extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog progress;
		String errorHandling = "OK";

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			Boolean prepared;
			try {
				colmena.setDataSource(params[0]);
				colmena.prepare();
				prepared = true;
				errorHandling = "OK";
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				Log.d("IllegarArgument", e.getMessage());
				prepared = false;
				e.printStackTrace();
				errorHandling = "Illegal Argument";
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				prepared = false;
				e.printStackTrace();
				errorHandling = "Security Exception";
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				prepared = false;
				e.printStackTrace();
				errorHandling = "Illegal State";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				prepared = false;
				e.printStackTrace();
				errorHandling = "IO exception";
			}
			return prepared;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			this.progress.setMessage("Agitando la colmena...");
			this.progress.setCancelable(false);
			this.progress.show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (errorHandling != "OK") {
				Toast.makeText(MainActivity.this, errorHandling,
						Toast.LENGTH_LONG).show();
			}
			if (progress.isShowing()) {
				progress.cancel();
			}
			Log.d("Prepared", "//" + result);
			colmena.start();

			initialStage = false;
		}

		public Player() {
			progress = new ProgressDialog(MainActivity.this);
		}
	}

	/*
	 * @Override protected void onResume() { if (colmena.isPlaying()) {
	 * playPause = true; btn.setBackgroundResource(R.drawable.button_pause); }
	 * };
	 * 
	 * /* Remove this block comment if you want to pause when you leave the app
	 * 
	 * @Override protected void onResume() { intialStage = true; playPause =
	 * false; colmena = new MediaPlayer();
	 * colmena.setAudioStreamType(AudioManager.STREAM_MUSIC); super.onResume();
	 * }
	 * 
	 * @Override protected void onPause() { // TODO Auto-generated method stub
	 * super.onPause(); if (colmena != null) { colmena.pause();
	 * colmena.release(); } }
	 */
	public void openFB(View v) {
		// This button goes to Colmena's FB Page.
		String url = "https://www.facebook.com/radiocolmena.com.ar?fref=ts";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void openTW(View v) {
		// This button goes to Colmena's TW Page.
		String url = "https://twitter.com/radiocolmena";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void openWeb(View v) {
		// This button goes to Colmena's Web Page.
		String url = "http://www.radiocolmena.com.ar/";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_item_share:
			Toast.makeText(this, "Eureka!", Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

}
