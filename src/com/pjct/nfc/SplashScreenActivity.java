package com.pjct.nfc;

import com.pjct.nfc.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {
	public static final String TAG = SplashScreenActivity.class.getSimpleName();
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent dashFwdIntent = new Intent(NFCTransportPro.getAppContext(),
						TravelPlannerActivity.class);
				startActivityForResult(dashFwdIntent, 1);
			}
		}, 3000);
	}
}
