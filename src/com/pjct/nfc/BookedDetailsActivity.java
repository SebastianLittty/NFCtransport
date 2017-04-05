package com.pjct.nfc;

import java.util.ArrayList;
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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.pjct.nfc.R;
import com.pjct.nfc.service.ServiceHandler;
import com.pjct.nfc.service.Train;
import com.pjct.nfc.service.TrainBookedDetailAdapter;

public class BookedDetailsActivity extends ListActivity {
	private static final String TAG = BookedDetailsActivity.class
			.getSimpleName();
	//private String URL_CANCEL_BOOKED_TICKET = "http://192.168.1.94:8080/nfctransport/CancelReservationServlet";
	//private String URL_GET_BOOKED_TICKET_DETAIL = "http://192.168.1.94:8080/nfctransport/GetReservationDetailServlet";
	private String URL_CANCEL_BOOKED_TICKET = "http://nfctransport.j.layershift.co.uk/CancelReservationServlet";
	private String URL_GET_BOOKED_TICKET_DETAIL = "http://nfctransport.j.layershift.co.uk/GetReservationDetailServlet";

	ProgressDialog pDialog;
	TrainBookedDetailAdapter bookedDetailAdapter;
	private ArrayList<Train> bookedDetailTrainsList;
	String trainPnr;
	Button btnCancelReservation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booked_detail);
		btnCancelReservation = (Button) findViewById(R.id.btnCancelTicket);
		Bundle bundle = getIntent().getExtras();
		trainPnr = bundle.getString("pnr").toString();
		getListView();
		new GetReservationDetail().execute();
		bookedDetailAdapter = new TrainBookedDetailAdapter(
				bookedDetailTrainsList, NFCTransportPro.getAppContext());
		if (!bookedDetailAdapter.isEmpty()) {
			Toast.makeText(NFCTransportPro.getAppContext(), ".Success",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(NFCTransportPro.getAppContext(),
					"Server couldn't find any Data", Toast.LENGTH_SHORT).show();
		}
		setListAdapter(bookedDetailAdapter);
		setupActionBar();
		btnCancelReservation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// showCancelTicketDialog();
				new CancelTicketTask().execute();
			}
		});
	}

	private void showCancelTicketDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getApplicationContext());
		alertDialogBuilder
				.setTitle(R.string.alert_cancel_reseve_title)
				.setMessage(R.string.alert_cancel_reseve_message)
				.setIcon(R.drawable.ic_action_warning)
				// Add action buttons
				.setPositiveButton(R.string.alert_text_postive,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// new TicketCancelTask.execute(pnr);

							}
						})
				.setNegativeButton(R.string.alert_text_negative,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private class CancelTicketTask extends AsyncTask<Void, Void, Boolean> {
		String response;

		@Override
		protected Boolean doInBackground(Void... params) {
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("train_pnr", trainPnr));
			Log.d(TAG, "Params :--->" + nvp.toString());
			String json = serviceClient.makeServiceCall(
					URL_CANCEL_BOOKED_TICKET, ServiceHandler.GET, nvp);
			Log.d("Trains cancel Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray responseArray = jsonObj
								.getJSONArray("result");
						Log.d(TAG, "cancelarray" + responseArray);
						// if (response == "SUCCESS")
						for (int i = 0; i < responseArray.length(); i++) {
							JSONObject jsonObject = (JSONObject) responseArray
									.get(i);
							response = jsonObject.getString("response");
							Log.d(TAG, "Cancel Response-->" + response);
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
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				Toast.makeText(NFCTransportPro.getAppContext(),
						R.string.success_cancel, Toast.LENGTH_LONG).show();
				Intent moveIntent = new Intent(NFCTransportPro.getAppContext(),
						TravelPlannerActivity.class);
				startActivity(moveIntent);
				finish();
			} else {
				Toast.makeText(NFCTransportPro.getAppContext(),
						R.string.error_cancel, Toast.LENGTH_LONG).show();
				Intent movesIntent = new Intent(
						NFCTransportPro.getAppContext(),
						TravelPlannerActivity.class);
				startActivity(movesIntent);
				finish();
			}
		}

	}

	private class GetReservationDetail extends
			AsyncTask<String, Void, List<Train>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(BookedDetailsActivity.this);
			pDialog.setMessage("Fetching Reservation Details..");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected List<Train> doInBackground(String... arg0) {
			List<Train> result = new ArrayList<Train>();
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("train_pnr", trainPnr));
			Log.d(TAG, "Params :--->" + params.toString());
			String json = serviceClient.makeServiceCall(
					URL_GET_BOOKED_TICKET_DETAIL, ServiceHandler.GET, params);
			Log.d("Trains Details Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray trainArray = jsonObj.getJSONArray("details");
						Log.d(TAG, "trainArray" + trainArray);
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
			if (pDialog.isShowing())
				pDialog.dismiss();
			bookedDetailAdapter.setItemList(result);
			bookedDetailAdapter.notifyDataSetChanged();

		}

		private Train convertTrain(JSONObject jsonObject) throws JSONException {
			String trainNo = jsonObject.getString("trainNo");
			String trainBerth = jsonObject.getString("trainBerth");
			String trainPrice = jsonObject.getString("trainPrice");
			String trainPnr = jsonObject.getString("trainPnr");
			String trainJdate = jsonObject.getString("trainjDate");
			return new Train(trainNo, trainBerth, trainPrice, trainPnr,
					trainJdate);
		}

	}

}
