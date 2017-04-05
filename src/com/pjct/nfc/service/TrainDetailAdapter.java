package com.pjct.nfc.service;

import java.util.List;

import com.pjct.nfc.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrainDetailAdapter extends ArrayAdapter<Train> {
	Context context;
	List<Train> trainList;

	public TrainDetailAdapter(List<Train> trainList, Context ctx) {
		super(ctx, android.R.layout.simple_list_item_1, trainList);
		this.trainList = trainList;
		this.context = ctx;
	}

	@Override
	public int getCount() {
		if (trainList != null)
			return trainList.size();
		return 0;
	}

	@Override
	public Train getItem(int position) {
		if (trainList != null)
			return trainList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (trainList != null)
			return trainList.get(position).hashCode();
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_train_detail, null);
		}
		Train train = trainList.get(position);
		TextView trainNo = (TextView) view.findViewById(R.id.train_no);
		//trainNo.setText("Train No : "+train.getTrain_no());
		trainNo.setText(train.getTrain_no());
		TextView trainName = (TextView) view.findViewById(R.id.train_name);
		//trainName.setText("Train Name :"+train.getTrain_name());
		trainName.setText(train.getTrain_name());
		TextView trainSource = (TextView) view.findViewById(R.id.train_source);
		//trainSource.setText("Source :"+train.getTrain_src());
		trainSource.setText(train.getTrain_src());
		TextView trainDstn = (TextView) view
				.findViewById(R.id.train_destination);
		//trainDstn.setText("Destination :"+train.getTrain_dstn());
		trainDstn.setText(train.getTrain_dstn());
		TextView trainAvailable = (TextView) view
				.findViewById(R.id.train_available);
		//trainAvailable.setText("Availability :"+train.getTrain_available());
		trainAvailable.setText(train.getTrain_available());
		TextView trainPrice = (TextView) view.findViewById(R.id.train_price);
		//trainPrice.setText("Fare :"+train.getTrain_price());
		trainPrice.setText(train.getTrain_price());
		return view;
	}

	public List<Train> getItemList() {
		return trainList;
	}

	public void setItemList(List<Train> itemList) {
		this.trainList = itemList;
	}
}
