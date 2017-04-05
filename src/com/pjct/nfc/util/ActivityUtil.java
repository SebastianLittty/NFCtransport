/**
 * Copyright 2012-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pjct.nfc.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.pjct.nfc.R;

/**
 * This is just a class that contains static methods to help us with some
 * methods that are bound to Activity
 */
public class ActivityUtil {

	/**
	 * Method to switch to another activity
	 * 
	 * @param from
	 *            the Activity of your current activity
	 * @param to
	 *            the Activity Class that you want to switch to e.x.
	 *            TodoActivity.class
	 * @param bundle
	 *            data Bundle
	 * @param withAnimation
	 *            true if you want to switch activity with an animation current
	 *            activity will slide out to left next activity will slide in
	 *            from right
	 */
	public static void switchActivity(Activity from, Class<?> to,
			Bundle bundle, boolean withAnimation) {
		Intent i = new Intent(from.getApplicationContext(), to);
		i.putExtras(bundle);
		if (!withAnimation) {
			i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		}
		from.startActivity(i);
		if (withAnimation) {
			/**
			 * first param is for the next activity second param is for your
			 * current activity
			 */
			from.overridePendingTransition(R.anim.slide_in_from_right,
					R.anim.slide_out_to_left);
		}
	}

	/**
	 * Method to finish an activity with an animation Current activity will
	 * finish with an animation of sliding out to right Prev activity (in the
	 * stack) will resume with an animation of sliding in from right
	 * 
	 * @param act
	 *            current activity
	 */
	public static void finishActivity(Activity act) {
		act.finish();
		/**
		 * first param is for the next activity second param is for your current
		 * activity
		 */
		act.overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
	}

	/**
	 * Method to show a Toast (with Toast.LENGTH_LONG by default)
	 * 
	 * @param act
	 *            Activity where this method is called from e.x.
	 *            TodoActivity.this
	 * @param msg
	 *            message to show
	 */
	public static void showToast(final Activity act, final String msg) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(act.getBaseContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	/**
	 * Function to display simple Alert Dialog
	 * 
	 * @param context
	 *            - application context
	 * @param title
	 *            - alert dialog title
	 * @param message
	 *            - alert message
	 * @param status
	 *            - success/failure (used to set icon)
	 * */
	public static void showAlertDialog(Context context, String title,
			String message, Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		// Setting Dialog Title
		alertDialog.setTitle(title);
		// Setting Dialog Message
		alertDialog.setMessage(message);
		// Setting alert dialog icon
		alertDialog.setIcon((status) ? R.drawable.ic_success_icon : R.drawable.ic_fail_icon);
		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		// Showing Alert Message
		alertDialog.show();
	}
}
