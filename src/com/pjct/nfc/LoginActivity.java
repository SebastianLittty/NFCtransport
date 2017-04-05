package com.pjct.nfc;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pjct.nfc.R;
import com.pjct.nfc.service.ServiceHandler;
import com.pjct.nfc.util.SessionManager;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */

public class LoginActivity extends Activity {
	public static final String TAG = LoginActivity.class.getSimpleName();
	//public static final String FG_PWD_URL = "http://192.168.1.94:8080/nfctransport/ForgotPwdServlet";
	//public static final String LOGIN_URL = "http://192.168.1.94:8080/nfctransport/ClientSignInServlet";
	public static final String FG_PWD_URL = "http://nfctransport.j.layershift.co.uk/ForgotPwdServlet";
	public static final String LOGIN_URL = "http://nfctransport.j.layershift.co.uk/ClientSignInServlet";
	// Session Manager Class
	SessionManager session;

	private final Context thisContext = this;
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mSignInTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		// Session Manager
		session = new SessionManager(getApplicationContext());
		TextView signUpScreen = (TextView) findViewById(R.id.link_to_signup);
		signUpScreen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent signUpScreenIntent = new Intent(NFCTransportPro
						.getAppContext(), SignUpActivity.class);
				startActivity(signUpScreenIntent);
			}
		});

		TextView forgotpwd = (TextView) findViewById(R.id.link_to_forgotpwd);
		forgotpwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showForgotPasswordDialog();
			}
		});
		// Set up the login form.
		// mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							// attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.plan_travel_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		/*
		 * ConnectionDetector detector = new ConnectionDetector(
		 * NFCTransportPro.getAppContext()); Boolean isInternetPresent =
		 * detector.isConnectingToInternet(); // check for Internet status if
		 * (isInternetPresent) { // Internet Connection is Present // make HTTP
		 * requests ActivityUtil
		 * .showAlertDialog(NFCTransportPro.getAppContext(),
		 * "Internet Connection", "You have internet connection", true); } else
		 * { // Internet connection is not present // Ask user to connect to
		 * Internet
		 * ActivityUtil.showAlertDialog(NFCTransportPro.getAppContext(),
		 * "No Internet Connection", "You don't have internet connection.",
		 * false); }
		 */
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * super.onCreateOptionsMenu(menu); getMenuInflater().inflate(R.menu.login,
	 * menu); return true; }
	 */
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mSignInTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString().trim();
		mPassword = mPasswordView.getText().toString().trim();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mSignInTask = new UserLoginTask();
			// mAuthTask.execute((Void) null);
			mSignInTask.execute();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login task used to authenticate the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		String response;

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "Email is " + mEmail);
			Log.d(TAG, "Password is " + mPassword);
			Log.d(TAG, "loginUrl is " + LOGIN_URL);
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			Log.d("service handler: ",  "entered");
			List<NameValuePair> lstNameValuePair = new ArrayList<NameValuePair>();
			lstNameValuePair.add(new BasicNameValuePair("email", mEmail));
			lstNameValuePair.add(new BasicNameValuePair("password", mPassword));
			Log.d("BasicNameValuePair: ",  "entered");
			String json = serviceClient.makeServiceCall(LOGIN_URL,
					ServiceHandler.GET, lstNameValuePair);
			Log.d("serviceClient: ",  "entered");
			Log.d("Login Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray responseArray = jsonObj
								.getJSONArray("result");
						Log.d(TAG, "loginarray" + responseArray);
						// if (response == "SUCCESS")
						for (int i = 0; i < responseArray.length(); i++) {
							JSONObject jsonObject = (JSONObject) responseArray
									.get(i);
							response = jsonObject.getString("response");
							Log.d(TAG, "Login Response-->" + response);
							if (response.equals("success")) {
								session.createLoginSession(mEmail, mPassword);
								return true;
							} else {
								return false;
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("JSON Data", "Didn't receive any data from server!");
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mSignInTask = null;
			showProgress(false);

			if (success) {
				Intent dashIntent = new Intent(getApplicationContext(),
						TravelPlannerActivity.class);
				startActivity(dashIntent);
				finish();
			} else {
				Toast.makeText(NFCTransportPro.getAppContext(),
						R.string.error_login, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mSignInTask = null;
			showProgress(false);
		}
	}

	private void showForgotPasswordDialog() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				thisContext);

		// forgotpwd is for the body of our dialog
		// antRootLayout is the main container of our body dialog (i.e. the root
		// layout for forgotpwd)
		final View view = getLayoutInflater().inflate(R.layout.forgot_pwd,
				(ViewGroup) findViewById(R.id.antRootLayout));
		final EditText mEmailForgotPassword = (EditText) view
				.findViewById(R.id.antEditText); // title edit text

		dialogBuilder.setView(view); // set the AlertDialog to our custom view

		// set onclick event for our dialog
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					mForgotEmail = mEmailForgotPassword.getText().toString();
					new ForgotPwdTask().execute(mForgotEmail);
					break;

				case DialogInterface.BUTTON_NEGATIVE:

					dialog.dismiss();
					break;
				}
			}

		};

		// create our Add New Todo dialog
		final AlertDialog dialog = dialogBuilder.setTitle("Enter your Email")
				.setPositiveButton("Send", dialogClickListener)
				.setNegativeButton("Cancel", dialogClickListener).create();

		// show keyboard
		mEmailForgotPassword
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View view, boolean b) {
						if (b) {
							dialog.getWindow()
									.setSoftInputMode(
											WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
						}
					}
				});

		// finally show our dialog
		dialog.show();
	}

	String mForgotEmail;

	public class ForgotPwdTask extends AsyncTask<String, Void, Boolean> {
		String response;

		@Override
		protected Boolean doInBackground(String... params) {
			String email= params[0];
			ServiceHandler serviceClient = new ServiceHandler();
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("user_email", email));
			String json = serviceClient.makeServiceCall(FG_PWD_URL,
					ServiceHandler.GET, nvp);
			Log.d("Forgot Password Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray responseArray = jsonObj
								.getJSONArray("result");
						Log.d(TAG, "trainArray" + responseArray);
						for (int i = 0; i < responseArray.length(); i++) {
							JSONObject jsonObject = (JSONObject) responseArray
									.get(i);
							response = jsonObject.getString("response");
						}
						if (response == "success") {
							return true;
						} else {
							return false;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				Toast.makeText(NFCTransportPro.getAppContext(),
						"Your Request has been Dispatched to " + mForgotEmail,
						Toast.LENGTH_SHORT).show();
			} else {

				Toast.makeText(NFCTransportPro.getAppContext(),
						"Failed to send Email to " + mForgotEmail,
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
