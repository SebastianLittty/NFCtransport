package com.pjct.nfc;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.pjct.nfc.R;
import com.pjct.nfc.reader.NdefMessageParser;
import com.pjct.nfc.record.ParsedNdefRecord;
import com.pjct.nfc.service.ServiceHandler;
import com.pjct.nfc.service.Train;
import com.pjct.nfc.service.TrainDetailAdapter;
import com.pjct.nfc.util.SessionManager;

public class TrainDetailActivity extends ListActivity {
	public static final String TAG = TrainDetailActivity.class.getSimpleName();
	//private String URL_GET_TRAIN_DETAIL = "http://192.168.1.94:8080/nfctransport/getTrainDetailServlet";
	
	//private String URL_TRAIN_RESERVE = "http://192.168.1.94:8080/nfctransport/MakeReservetionServlet";
private String URL_GET_TRAIN_DETAIL = "http://nfctransport.j.layershift.co.uk/getTrainDetailServlet";
	
private String URL_TRAIN_RESERVE = "http://nfctransport.j.layershift.co.uk/MakeReservetionServlet";
	ProgressDialog pDialog;
	TrainDetailAdapter detailAdapter;
	private ArrayList<Train> trainsList;
	/*---------PayPal-----------*/
	// set to PaymentActivity.ENVIRONMENT_PRODUCTION to move real money.
	// set to PaymentActivity.ENVIRONMENT_SANDBOX to use your test credentials
	// from https://developer.paypal.com
	// set to PaymentActivity.ENVIRONMENT_NO_NETWORK to kick the tires without
	// communicating to PayPal's servers.
	private static final String CONFIG_ENVIRONMENT = PaymentActivity.ENVIRONMENT_SANDBOX;
	// note that these credentials will differ between live & sandbox
	// environments.
	private static final String CONFIG_CLIENT_ID = "AUvI6hC0oCc1j3Y-nbaZ5CO6s5PmqYMCK1PB2ZtnSic3_dd_2SsdWPYS_wbM";
	// when testing in sandbox, this is likely the -facilitator email address.
	private static final String CONFIG_RECEIVER_EMAIL = "nijomon-facilitator@gmail.com";
	/*---------PayPal-----------*/
	/* NFC */
	private static final DateFormat TIME_FORMAT = SimpleDateFormat
			.getDateTimeInstance();
	private LinearLayout mTagContent;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private NdefMessage mNdefPushMessage;
	private AlertDialog mDialog;
	// Session Manager Class
	SessionManager session;
	String user_email, trainName, trainPrice, journeyDate;
	TextView NFContent;
	String oNfcTagId;

	/* NFC */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train_detail);
		// Session class instance
		Bundle bundle = getIntent().getExtras();
		trainName = bundle.getString("trainName").toString();
		journeyDate = bundle.getString("journeyDate").toString();
		// journeyDate = bundle.getString("jDate");
		Log.d(TAG, "OnCreate JourneyDate " + journeyDate);
		session = new SessionManager(NFCTransportPro.getAppContext());
		session.checkLogin();
		// get user data from session
		HashMap<String, String> user = session.getUserDetails();
		// name
		user_email = user.get(SessionManager.KEY_EMAIL);
		/*---------PayPal-----------*/
		Intent intent = new Intent(this, PayPalService.class);

		intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT,
				CONFIG_ENVIRONMENT);
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
		intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL,
				CONFIG_RECEIVER_EMAIL);

		startService(intent);
		/*---------PayPal-----------*/
		/* NFC */
		mTagContent = (LinearLayout) findViewById(R.id.nfc_list);
		resolveIntent(getIntent());

		mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null)
				.create();

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mAdapter == null) {
			showMessage(R.string.error, R.string.no_nfc);
			finish();
			return;
		}

		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		mNdefPushMessage = new NdefMessage(new NdefRecord[] { newTextRecord(
				"Message from NFC Reader :-)", Locale.ENGLISH, true) });
		/* NFC */
		// Show the Up button in the action bar.
		setupActionBar();
		new GetTrainDetail().execute();
		detailAdapter = new TrainDetailAdapter(trainsList,
				NFCTransportPro.getAppContext());
		if (detailAdapter.isEmpty()) {
			Toast.makeText(NFCTransportPro.getAppContext(),
					"Server couldn't find Trains", Toast.LENGTH_SHORT).show();
		}
		setListAdapter(detailAdapter);
		// ListView listView = getListView();
	}

	@Override
	public void onDestroy() {
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdapter != null) {
			if (!mAdapter.isEnabled()) {
				showWirelessSettingsDialog();
			}
			mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
			mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
			// oNfcTagId = (String) findViewById(R.id.text).toString();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
			mAdapter.disableForegroundNdefPush(this);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/*---------PayPal-----------*/
	public void onBuyPressed(View pressed) {
		//new MakeTrainReserve().execute();
		TextView txtVTrainName = (TextView) findViewById(R.id.train_name);
		trainName = txtVTrainName.getText().toString().trim();
		TextView txtTrainPrice = (TextView) findViewById(R.id.train_price);
		trainPrice = txtTrainPrice.getText().toString().trim();
		PayPalPayment thingToBuy = new PayPalPayment(
				new BigDecimal(trainPrice), "USD", "Ticket Reservation :"
						+ trainName);

		/*
		 * PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal("10000"),
		 * "USD", "Ticket Reservation");
		 */
		Intent intent = new Intent(this, PaymentActivity.class);
		intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT,
				CONFIG_ENVIRONMENT);
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
		intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL,
				CONFIG_RECEIVER_EMAIL);

		// It's important to repeat the clientId here so that the SDK has it if
		// Android restarts your
		// app midway through the payment UI flow.
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID,
				"AUvI6hC0oCc1j3Y-nbaZ5CO6s5PmqYMCK1PB2ZtnSic3_dd_2SsdWPYS_wbM");
		intent.putExtra(PaymentActivity.EXTRA_PAYER_ID, "reshma@dieutek.com");
		intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "result code :" + requestCode);
		if (resultCode == Activity.RESULT_OK) {
			PaymentConfirmation confirm = data
					.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
			if (confirm != null) {
				try {
					Log.i(TAG, confirm.toJSONObject().toString(4));
					new MakeTrainReserve().execute();

					// see
					// https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
					// for more details.

				} catch (JSONException e) {
					Log.e(TAG, "an extremely unlikely failure occurred: ", e);
				}
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			Log.i(TAG, "The user canceled.");
		} else if (resultCode == PaymentActivity.RESULT_PAYMENT_INVALID) {
			Log.i(TAG, "An invalid payment was submitted. Please see the docs.");
		}
	}

	/*---------PayPal-----------*/
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.train_detail, menu); return true; }
	 */

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

	/**
	 * Async task to Fetch Train List
	 * */
	private class GetTrainDetail extends AsyncTask<String, Void, List<Train>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(TrainDetailActivity.this);
			pDialog.setMessage("Fetching Train Details..");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected List<Train> doInBackground(String... arg) {
			List<Train> result = new ArrayList<Train>();
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("trainName", trainName));
			Log.d(TAG, "Params :--->" + params.toString());
			String json = serviceClient.makeServiceCall(URL_GET_TRAIN_DETAIL,
					ServiceHandler.GET, params);
			Log.d("Trains Details Response: ", "--> " + json);
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
			detailAdapter.setItemList(result);
			detailAdapter.notifyDataSetChanged();

		}

		private Train convertTrain(JSONObject jsonObject) throws JSONException {
			String trainNo = jsonObject.getString("trainNo");
			String trainName = jsonObject.getString("trainName");
			String source = jsonObject.getString("source");
			String destination = jsonObject.getString("destination");
			String trainavail = jsonObject.getString("available");
			String price = jsonObject.getString("price");
			return new Train(trainNo, trainName, source, destination,
					trainavail, price);
		}
	}

	private class MakeTrainReserve extends AsyncTask<String, Void, String> {

		/*
		 * TextView txtVTrainName = (TextView) findViewById(R.id.train_name);
		 * String trainName = txtVTrainName.getText().toString().trim();
		 */
		TextView txtVTrainSource = (TextView) findViewById(R.id.train_source);
		String trainSource = txtVTrainSource.getText().toString();
		TextView txtVTrainDstn = (TextView) findViewById(R.id.train_destination);
		String trainDstn = txtVTrainDstn.getText().toString().trim();
		TextView txtVTrainNo = (TextView) findViewById(R.id.train_no);
		String trainNo = txtVTrainNo.getText().toString().trim();
		String nfcTagId = getNFCDetails();
		// String nfcTagId ="7UH657T8767YTY67";
		String response;

		/*
		 * @Override protected void onPreExecute() { super.onPreExecute();
		 * pDialog = new ProgressDialog(TrainDetailActivity.this);
		 * pDialog.setMessage("Reserving Ticket"); pDialog.setCancelable(false);
		 * pDialog.show(); }
		 */

		@Override
		protected String doInBackground(String... params) {
			List<Train> result = new ArrayList<Train>();
			ServiceHandler serviceClient = new ServiceHandler();
			// Preparing params to be passed with URL
			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("userId", user_email));
			Log.d(TAG, "user_email :--->" + user_email);
			nvp.add(new BasicNameValuePair("trainNo", trainNo));
			Log.d(TAG, "trainNo :--->" + trainNo);
			nvp.add(new BasicNameValuePair("nfcTagId", nfcTagId));
			Log.d(TAG, "nfcTagId :--->" + nfcTagId);
			nvp.add(new BasicNameValuePair("source", trainSource));
			Log.d(TAG, "trainSource :--->" + trainSource);
			nvp.add(new BasicNameValuePair("destination", trainDstn));
			Log.d(TAG, "trainDstn :--->" + trainDstn);
			nvp.add(new BasicNameValuePair("journeyDate", journeyDate));
			Log.d(TAG, "Journey Date" + journeyDate);
			Log.d(TAG, "Params :--->" + nvp.toString());
			String json = serviceClient.makeServiceCall(URL_TRAIN_RESERVE,
					ServiceHandler.GET, nvp);
			Log.d("Trains Details Response: ", "--> " + json);
			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
					if (jsonObj != null) {
						JSONArray responseArray = jsonObj
								.getJSONArray("result");
						Log.d(TAG, "trainArray" + responseArray);
						// if (response == "SUCCESS")
						for (int i = 0; i < responseArray.length(); i++) {
							JSONObject jsonObject = (JSONObject) responseArray
									.get(i);
							response = jsonObject.getString("response");
						}
						return response;
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
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				Toast.makeText(NFCTransportPro.getAppContext(),
						"Your Ticket has been reserved with PNR " + response,
						Toast.LENGTH_LONG).show();
				Intent returnIntent = new Intent(
						NFCTransportPro.getAppContext(),
						TravelPlannerActivity.class);
				startActivity(returnIntent);
				finish();
			} else {
				Toast.makeText(NFCTransportPro.getAppContext(),
						"Ticket Reservation Failed", Toast.LENGTH_LONG).show();
			}

		}

	}

	private void showWirelessSettingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.nfc_disabled);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(
								Settings.ACTION_WIRELESS_SETTINGS);
						startActivity(intent);
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						finish();
					}
				});
		builder.create().show();
		return;
	}

	/* NFC */
	/*
	 * NDEF (NFC Data Exchange Format) is a light-weight binary format, used to
	 * encapsulate typed data. It is specified by the NFC Forum, for
	 * transmission and storage with NFC, however it is transport agnostic.
	 * 
	 * NDEF defines messages and records. An NDEF Record contains typed data,
	 * such as MIME-type media, a URI, or a custom application payload. An NDEF
	 * Message is a container for one or more NDEF Records.
	 * 
	 * This class represents logical (complete) NDEF Records, and can not be
	 * used to represent chunked (partial) NDEF Records. However
	 * NdefMessage(byte[]) can be used to parse a message containing chunked
	 * records, and will return a message with unchunked (complete) records.
	 * 
	 * A logical NDEF Record always contains a 3-bit TNF (Type Name Field) that
	 * provides high level typing for the rest of the record. The remaining
	 * fields are variable length and not always present:
	 * 
	 * type: detailed typing for the payload id: identifier meta-data, not
	 * commonly used payload: the actual payload
	 */
	private NdefRecord newTextRecord(String text, Locale locale,
			boolean encodeInUtf8) {
		byte[] langBytes = locale.getLanguage().getBytes(
				Charset.forName("US-ASCII"));

		Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset
				.forName("UTF-16");
		byte[] textBytes = text.getBytes(utfEncoding);

		int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		char status = (char) (utfBit + langBytes.length);

		byte[] data = new byte[1 + langBytes.length + textBytes.length];
		data[0] = (byte) status;
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
				textBytes.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], data);
	}

	private void showMessage(int title, int message) {
		mDialog.setTitle(title);
		mDialog.setMessage(getText(message));
		mDialog.show();
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		resolveIntent(intent);
	}

	private void resolveIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage[] msgs;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[0];
				byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
				Parcelable tag = intent
						.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				byte[] payload = dumpTagData(tag).getBytes();
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, id, payload);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
			// Setup the views
			buildTagViews(msgs);
			getNFCDetails();
		}
	}

	/*
	 * Parcelable--> This class represents accessibility events that are sent by
	 * the system when something notable happens in the user interface.
	 */
	private String dumpTagData(Parcelable p) {
		StringBuilder sb = new StringBuilder();
		Tag tag = (Tag) p;
		byte[] id = tag.getId();
		sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
		sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
		sb.append("ID (reversed): ").append(getReversed(id)).append("\n");
		String prefix = "android.nfc.tech.";
		sb.append("Technologies: ");
		for (String tech : tag.getTechList()) {
			sb.append(tech.substring(prefix.length()));
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		for (String tech : tag.getTechList()) {
			if (tech.equals(MifareClassic.class.getName())) {
				sb.append('\n');
				MifareClassic mifareTag = MifareClassic.get(tag);
				String type = "Unknown";
				switch (mifareTag.getType()) {
				case MifareClassic.TYPE_CLASSIC:
					type = "Classic";
					break;
				case MifareClassic.TYPE_PLUS:
					type = "Plus";
					break;
				case MifareClassic.TYPE_PRO:
					type = "Pro";
					break;
				}
				sb.append("Mifare Classic type: ");
				sb.append(type);
				sb.append('\n');

				sb.append("Mifare size: ");
				sb.append(mifareTag.getSize() + " bytes");
				sb.append('\n');

				sb.append("Mifare sectors: ");
				sb.append(mifareTag.getSectorCount());
				sb.append('\n');

				sb.append("Mifare blocks: ");
				sb.append(mifareTag.getBlockCount());
			}

			if (tech.equals(MifareUltralight.class.getName())) {
				sb.append('\n');
				MifareUltralight mifareUlTag = MifareUltralight.get(tag);
				String type = "Unknown";
				switch (mifareUlTag.getType()) {
				case MifareUltralight.TYPE_ULTRALIGHT:
					type = "Ultralight";
					break;
				case MifareUltralight.TYPE_ULTRALIGHT_C:
					type = "Ultralight C";
					break;
				}
				sb.append("Mifare Ultralight type: ");
				sb.append(type);
			}
		}
		return sb.toString();
	}

	private String getHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			int b = bytes[i] & 0xff;
			if (b < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(b));
			if (i > 0) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private long getDec(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = 0; i < bytes.length; ++i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	private long getReversed(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = bytes.length - 1; i >= 0; --i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	void buildTagViews(NdefMessage[] msgs) {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout content = mTagContent;

		// Parse the first message in the list
		// Build views for all of the sub records
		Date now = new Date();
		List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
		final int size = records.size();
		Log.d(TAG, "Parsed NDEF Record Size:" + size);
		for (int i = 0; i < size; i++) {
			TextView timeView = new TextView(this);
			timeView.setText(TIME_FORMAT.format(now));
			content.addView(timeView, 0);
			ParsedNdefRecord record = records.get(i);
			content.addView(record.getView(this, inflater, content, i), 1 + i);
			/*
			 * content.addView( inflater.inflate(R.layout.tag_divider, content,
			 * false), 2 + i);
			 */
		}
	}

	private String getNFCDetails() {
		NFContent = (TextView) findViewById(R.id.text);
		oNfcTagId = NFContent.getText().toString();
		Log.d(TAG, "NFC Data-->" + oNfcTagId);
		return oNfcTagId;
	}

	/* NFC */
}
