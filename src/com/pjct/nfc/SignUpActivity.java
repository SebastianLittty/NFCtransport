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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
public class SignUpActivity extends Activity {

	//public static final String SIGN_UP_URL = "http://192.168.1.94:8080/nfctransport/ClientSignUpServlet";
	public static final String SIGN_UP_URL = "http://nfctransport.j.layershift.co.uk/ClientSignUpServlet";
	public static final String TAG = SignUpActivity.class.getSimpleName();
	// Session Manager Class
	SessionManager session;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserSignUpTask mSignUpTask = null;

	// Values for email and password at the time of the login attempt.
	private String mFullName;
	private String mEmail;
	private String mPassword;
	private String mNfcTagId;
	// UI references.
	private EditText mFullNameView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mNfcTagIdView;
	private View mSignUpFormView;
	private View mSignUpStatusView;
	private TextView mSignUpStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sign_up);

		TextView signInScreen = (TextView) findViewById(R.id.link_to_signin);
		signInScreen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent signInScreenIntent = new Intent(NFCTransportPro
						.getAppContext(), LoginActivity.class);
				startActivity(signInScreenIntent);
			}
		});

		mFullNameView = (EditText) findViewById(R.id.fullname);
		// Set up the login form.
		// mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mNfcTagIdView = (EditText) findViewById(R.id.nfc_tag_id);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptSignUp();
							return true;
						}
						return false;
					}
				});

		mSignUpFormView = findViewById(R.id.sign_up_form);
		mSignUpStatusView = findViewById(R.id.signup_status);
		mSignUpStatusMessageView = (TextView) findViewById(R.id.signup_status_message); 

		findViewById(R.id.sign_up_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptSignUp();
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
	 * super.onCreateOptionsMenu(menu);
	 * getMenuInflater().inflate(R.menu.sign_up, menu); return true; }
	 */

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptSignUp() {
		if (mSignUpTask != null) {
			return;
		}

		// Reset errors.
		mFullNameView.setError(null);
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mNfcTagIdView.setError(null);
		// Store values at the time of the login attempt.
		mFullName = mFullNameView.getText().toString().trim();
		mEmail = mEmailView.getText().toString().trim();
		mPassword = mPasswordView.getText().toString().trim();
		mNfcTagId = mNfcTagIdView.getText().toString().trim();

		boolean cancel = false;
		View focusView = null;
		// Check for Valid Full name
		if (TextUtils.isEmpty(mFullName)) {
			mFullNameView.setError(getString(R.string.error_field_required));
			focusView = mFullNameView;
			cancel = true;
		}
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

		if (TextUtils.isEmpty(mNfcTagId)) {
			mNfcTagIdView.setError(getString(R.string.error_field_required));
			focusView = mNfcTagIdView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mSignUpStatusMessageView
					.setText(R.string.signup_progress_signing_up);
			showProgress(true);
			mSignUpTask = new UserSignUpTask();
			// mAuthTask.execute((Void) null);
			mSignUpTask.execute();
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

			mSignUpStatusView.setVisibility(View.VISIBLE);
			mSignUpStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSignUpStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mSignUpFormView.setVisibility(View.VISIBLE);
			mSignUpFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSignUpFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mSignUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous registration task used for the user.
	 */
	public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {
		String response;

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "Name is :" + mFullName);
			Log.d(TAG, "Email is :" + mEmail);
			Log.d(TAG, "Password is :" + mPassword);
			Log.d(TAG, "NFC Tag Id is :" + mNfcTagId);
			Log.d(TAG, "signUpUrl :" + SIGN_UP_URL);
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> lstNameValuePair = new ArrayList<NameValuePair>();
			lstNameValuePair.add(new BasicNameValuePair("fullname", mFullName));
			lstNameValuePair.add(new BasicNameValuePair("email", mEmail));
			lstNameValuePair.add(new BasicNameValuePair("password", mPassword));
			lstNameValuePair.add(new BasicNameValuePair("nfcid", mNfcTagId));
			String json = serviceClient.makeServiceCall(SIGN_UP_URL,
					ServiceHandler.GET, lstNameValuePair);
			Log.d("Sign Up Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray responseArray = jsonObj
								.getJSONArray("result");
						Log.d(TAG, "signup array" + responseArray);
						// if (response == "SUCCESS")
						for (int i = 0; i < responseArray.length(); i++) {
							JSONObject jsonObject = (JSONObject) responseArray
									.get(i);
							response = jsonObject.getString("response");
							Log.d(TAG, "Signup Response-->" + response);
							if (response.equals("success")) {
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
			mSignUpTask = null;
			showProgress(false);
			if (success) {
				// session.createLoginSession(mEmail, null);
				Intent dashbIntent = new Intent(getApplicationContext(),
						TravelPlannerActivity.class);
				startActivity(dashbIntent);
				// finish();
			} else {
				Toast.makeText(NFCTransportPro.getAppContext(),
						R.string.error_signup, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mSignUpTask = null;
			showProgress(false);
		}
	}
}
