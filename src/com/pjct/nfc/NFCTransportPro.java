package com.pjct.nfc;

import android.app.Application;
import android.content.Context;

public class NFCTransportPro extends Application {
	private static Context mAppContext;

	public NFCTransportPro() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mAppContext = getApplicationContext();
	}

	/**
	 * Returns the application's context. Useful for classes that need a Context
	 * but don't inherently have one.
	 * 
	 * @return application context
	 */

	public static Context getAppContext() {
		return mAppContext;
	}

}
