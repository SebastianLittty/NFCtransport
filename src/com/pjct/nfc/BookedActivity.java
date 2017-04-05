package com.pjct.nfc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pjct.nfc.R;
import com.pjct.nfc.service.ServiceHandler;
import com.pjct.nfc.service.Train;
import com.pjct.nfc.service.TrainBookedAdapter;
import com.pjct.nfc.util.ActivityUtil;
import com.pjct.nfc.util.SessionManager;

public class BookedActivity extends ListActivity {
	private static final String TAG = BookedActivity.class.getSimpleName();
	//private String URL_GET_BOOKED_TRAINS = "http://192.168.1.94:8080/nfctransport/getBookedTrainsServlet";
	private String URL_GET_BOOKED_TRAINS = "http://nfctransport.j.layershift.co.uk/getBookedTrainsServlet";
	ProgressDialog pDialog;
	private ArrayList<Train> bookedTrainsList;
	TrainBookedAdapter trainBookedAdapter;
	SessionManager sessionManager;
	String user_email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booked);

		new GetBookedTrains().execute();
		trainBookedAdapter = new TrainBookedAdapter(bookedTrainsList,
				NFCTransportPro.getAppContext());
		if (trainBookedAdapter.isEmpty()) {
			Toast.makeText(NFCTransportPro.getAppContext(),
					"Server couldn't find any Reservations", Toast.LENGTH_SHORT)
					.show();
		}
		setListAdapter(trainBookedAdapter);
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView trainPnr = (TextView) findViewById(R.id.train_pnr_number);
				String pnr = trainPnr.getText().toString();
				Log.d(TAG, "Train PNR :-->" + pnr);
				Bundle detailBundle = new Bundle();
				detailBundle.putString("pnr", pnr);
				/*
				 * detailBundle.putString("journeyDate", getIntent()
				 * .getStringExtra("jDate"));
				 */
				ActivityUtil.switchActivity(BookedActivity.this,
						BookedDetailsActivity.class, detailBundle, true);
				finish();
			}
		});
		setupActionBar();
	}

	

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class GetBookedTrains extends AsyncTask<String, Void, List<Train>> {
		SessionManager sessionManager = new SessionManager(
				NFCTransportPro.getAppContext());
		HashMap<String, String> user = sessionManager.getUserDetails();
		String user_email = user.get(SessionManager.KEY_EMAIL);

		@Override
		protected List<Train> doInBackground(String... params) {
			List<Train> result = new ArrayList<Train>();
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("user_email", user_email));
			Log.d(TAG, "Params :--->" + nvp.toString());
			String json = serviceClient.makeServiceCall(URL_GET_BOOKED_TRAINS,
					ServiceHandler.GET, nvp);
			Log.d("Booked Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray trainArray = jsonObj.getJSONArray("booked");
						Log.d(TAG, "bookedArray" + trainArray);
						for (int i = 0; i < trainArray.length(); i++) {
							result.add(convertTrain(trainArray.getJSONObject(i)));
						}
						return result;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("JSON Data", "Didn't receive any Train data from server!");
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Train> result) {
			super.onPostExecute(result);
			trainBookedAdapter.setItemList(result);
			trainBookedAdapter.notifyDataSetChanged();
			// populateTrainsListView();
		}

		private Train convertTrain(JSONObject jsonObject) throws JSONException {
			String trainPnrNo = jsonObject.getString("pnrno");
			String trainJDate = jsonObject.getString("jdate");
			String trainPrice = jsonObject.getString("price");
			return new Train(trainPnrNo, trainJDate, trainPrice);
		}
	}

}
