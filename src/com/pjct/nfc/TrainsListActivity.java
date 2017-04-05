package com.pjct.nfc;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pjct.nfc.R;
import com.pjct.nfc.service.ServiceHandler;
import com.pjct.nfc.service.Train;
import com.pjct.nfc.service.TrainListAdapter;
import com.pjct.nfc.util.ActivityUtil;

public class TrainsListActivity extends ListActivity {
	private static final String TAG = TrainsListActivity.class.getSimpleName();
//	private String URL_GET_TRAINS ="http://192.168.1.94:8080/nfctransport/getTrainsListServlet";
	private String URL_GET_TRAINS ="http://nfctransport.j.layershift.co.uk/getTrainsListServlet";
	ProgressDialog pDialog;
	private ArrayList<Train> trainsList;
	TrainListAdapter trainListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trains_list);
		new GetTrainList().execute();

		trainListAdapter = new TrainListAdapter(trainsList,
				NFCTransportPro.getAppContext());
		if (trainListAdapter.isEmpty()) {
			Toast.makeText(NFCTransportPro.getAppContext(),
					"Server couldn't find Trains", Toast.LENGTH_SHORT).show();
		}

		setListAdapter(trainListAdapter);

		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView trainName = (TextView) view
						.findViewById(R.id.train_name);
				String name = trainName.getText().toString();
				Log.d(TAG, "Train Name :-->" + name);
				Bundle detailBundle = new Bundle();
				detailBundle.putString("trainName", name);
				detailBundle.putString("journeyDate", getIntent()
						.getStringExtra("jDate"));
				ActivityUtil.switchActivity(TrainsListActivity.this,
						TrainDetailActivity.class, detailBundle, true);
				finish();
			}
		});
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(listView, view, position, id);
		Object object = this.getListAdapter().getItem(position);
		String trainName = object.toString();
		Log.d(TAG, "Train Name :-->" + trainName);
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.trains_list, menu); return true; }
	 */

	/**
	 * Async task to Fetch Train List
	 * */
	private class GetTrainList extends AsyncTask<String, Void, List<Train>> {

		Bundle bundle = getIntent().getExtras();
		String fromStaion = bundle.getString("from").toString();
		String toStation = bundle.getString("to").toString();
		String toDate = bundle.getString("jDate").toString();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(TrainsListActivity.this);
			pDialog.setMessage("Fetching Trains List..");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected List<Train> doInBackground(String... arg) {
			List<Train> result = new ArrayList<Train>();
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("source", fromStaion));
			params.add(new BasicNameValuePair("destination", toStation));
			params.add(new BasicNameValuePair("jdate", toDate));
			Log.d(TAG, "Params :--->" + params.toString());
			String json = serviceClient.makeServiceCall(URL_GET_TRAINS,
					ServiceHandler.GET, params);
			Log.d("Trains List Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray trainArray = jsonObj.getJSONArray("trains");
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
			trainListAdapter.setItemList(result);
			trainListAdapter.notifyDataSetChanged();
			// populateTrainsListView();
		}

		private Train convertTrain(JSONObject jsonObject) throws JSONException {
			String trainName = jsonObject.getString("name");
			String trainavail = jsonObject.getString("available");
			return new Train(trainName, trainavail);
		}
	}
}
