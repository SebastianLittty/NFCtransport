package com.pjct.nfc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pjct.nfc.R;
import com.pjct.nfc.service.Destination;
import com.pjct.nfc.service.ServiceHandler;
import com.pjct.nfc.service.Source;
import com.pjct.nfc.util.ActivityUtil;
import com.pjct.nfc.util.ConnectionDetector;
import com.pjct.nfc.util.SessionManager;

public class TravelPlannerActivity extends Activity implements
		OnItemSelectedListener {
	private static final String TAG = TravelPlannerActivity.class
			.getSimpleName();
	// Session Manager Class
	SessionManager session;
	/* For Spinner */
	//private String URL_SOURCES = "http://192.168.1.94:8080/nfctransport/getSourceServlet";
	//private String URL_DSTNS = "http://192.168.1.94:8080/nfctransport/getDestinationServlet";
	private String URL_SOURCES = "http://nfctransport.j.layershift.co.uk/getSourceServlet";
	private String URL_DSTNS = "http://nfctransport.j.layershift.co.uk/getDestinationServlet";
	private ArrayList<Source> sourcesList;
	private ArrayList<Destination> dstnList;
	ProgressDialog pDialog;
	private String sourceSelection, dstnSelection, dtJourney;
	private Spinner sourceSpinner, dstnSpinner;
	private Button planYourTravel;
	private EditText etJourneyDate;
	protected int mYear, mMonth, mDay;

	/* For Spinner */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_travel_planner);
		// Session class instance
		session = new SessionManager(NFCTransportPro.getAppContext());
		Toast.makeText(NFCTransportPro.getAppContext(),
				"User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG)
				.show();
		/**
		 * Call this function whenever you want to check user login This will
		 * redirect user to LoginActivity ,when not logged in
		 * */
		session.checkLogin();
		/* For Spinner */
		sourcesList = new ArrayList<Source>();
		dstnList = new ArrayList<Destination>();
		sourceSpinner = (Spinner) findViewById(R.id.sourceSpinner);
		dstnSpinner = (Spinner) findViewById(R.id.dstnSpinner);
		etJourneyDate = (EditText) findViewById(R.id.etJourneyDate);
		sourceSpinner.setOnItemSelectedListener(this);
		dstnSpinner.setOnItemSelectedListener(this);
		etJourneyDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// To show current date in the datepicker
				Calendar mcurrentDate = Calendar.getInstance();
				mYear = mcurrentDate.get(Calendar.YEAR);
				mMonth = mcurrentDate.get(Calendar.MONTH);
				mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog mDatePicker = new DatePickerDialog(
						TravelPlannerActivity.this, new OnDateSetListener() {
							public void onDateSet(DatePicker datepicker,
									int selectedyear, int selectedmonth,
									int selectedday) {
								etJourneyDate.setText(selectedday + "/"
										+ selectedmonth + "/" + selectedyear);
							}
						}, mYear, mMonth, mDay);
				mDatePicker.setTitle("Select date");
				mDatePicker.show();
			}
		});
		// Get Source Stations AsyncTask Execute
		new GetSources().execute();
		/* For Spinner */
		planYourTravel = (Button) findViewById(R.id.plan_travel_button);
		planYourTravel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sourceSelection = sourceSpinner.getSelectedItem().toString();
				dstnSelection = dstnSpinner.getSelectedItem().toString();
				dtJourney = etJourneyDate.getText().toString();
				Log.d(TAG, "Source Station :" + sourceSelection);
				Log.d(TAG, "Destination Station :" + dstnSelection);
				Log.d(TAG, "Journey Date :" + dtJourney);
				Bundle fromToBundle = new Bundle();
				fromToBundle.putString("from", sourceSelection);
				fromToBundle.putString("to", dstnSelection);
				fromToBundle.putString("jDate", dtJourney);
				ActivityUtil.switchActivity(TravelPlannerActivity.this,
						TrainsListActivity.class, fromToBundle, true);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.travel_planner, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_log_out:
			session.logoutUser();
			break;
		case R.id.view_history:
			Intent bookedIntent = new Intent(NFCTransportPro.getAppContext(),
					BookedActivity.class);
			startActivity(bookedIntent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Adding Source spinner data
	 */
	private void populateSrcSpinner() {
		List<String> srclables = new ArrayList<String>();

		for (int i = 0; i < sourcesList.size(); i++) {
			srclables.add(sourcesList.get(i).getSource());
		}
		// Creating adapter for source spinner
		ArrayAdapter<String> srcSpinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, srclables);
		// Drop down layout style - list view with radio button
		srcSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attaching data adapter to source spinner
		sourceSpinner.setAdapter(srcSpinnerAdapter);
		new GetDestinations().execute();
	}

	/* Adding Destination Spinner Data */
	private void populateDstnSpinner() {
		List<String> dstnlabels = new ArrayList<String>();
		// Destination Spinner
		for (int i = 0; i < dstnList.size(); i++) {
			dstnlabels.add(dstnList.get(i).getDestination());
		}
		// Creating adapter for Destination spinner
		ArrayAdapter<String> dstnSpinnerAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, dstnlabels);
		// Drop down layout style - list view with radio button
		dstnSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attaching data adapter to Destination spinner
		dstnSpinner.setAdapter(dstnSpinnerAdapter);
	}

	/* Source Spinner AsyncTask */
	private class GetSources extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(TravelPlannerActivity.this);
			pDialog.setMessage("Fetching Source Stations..");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {

			ServiceHandler jsonParser = new ServiceHandler();
			String json = jsonParser.makeServiceCall(URL_SOURCES,
					ServiceHandler.GET);

			Log.e("GetSources Response: ", "--> " + json);

			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray categories = jsonObj.getJSONArray("sources");

						for (int i = 0; i < categories.length(); i++) {
							JSONObject srcObj = (JSONObject) categories.get(i);
							Source cat = new Source(srcObj.getString("value"));
							sourcesList.add(cat);
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
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing())
				pDialog.dismiss();
			// populateSpinner();
			populateSrcSpinner();
		}

	}

	/* Destination Spinner AsyncTask */
	private class GetDestinations extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(TravelPlannerActivity.this);
			pDialog.setMessage("Fetching Destination Stations..");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			ServiceHandler jsonParser = new ServiceHandler();
			String json = jsonParser.makeServiceCall(URL_DSTNS,
					ServiceHandler.GET);

			Log.e("GetDestinations Response: ", "--> " + json);

			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray categories = jsonObj
								.getJSONArray("destinations");

						for (int i = 0; i < categories.length(); i++) {
							JSONObject srcObj = (JSONObject) categories.get(i);
							Destination dstn = new Destination(
									srcObj.getString("value"));
							dstnList.add(dstn);
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
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing())
				pDialog.dismiss();
			populateDstnSpinner();
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		/*
		 * Toast.makeText(getApplicationContext(),
		 * parent.getItemAtPosition(position).toString() + " Selected",
		 * Toast.LENGTH_LONG).show();
		 */

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
}
