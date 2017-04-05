package com.pjct.nfc.service;

import java.util.List;

import com.pjct.nfc.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrainBookedAdapter extends ArrayAdapter<Train> {

	Context context;
	List<Train> bookedTrainList;

	public TrainBookedAdapter(List<Train> bookedTrainList, Context ctx) {
		super(ctx, android.R.layout.simple_list_item_1, bookedTrainList);
		this.bookedTrainList = bookedTrainList;
		this.context = ctx;
	}

	@Override
	public int getCount() {
		if (bookedTrainList != null)
			return bookedTrainList.size();
		return 0;
	}

	@Override
	public Train getItem(int position) {
		if (bookedTrainList != null)
			return bookedTrainList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (bookedTrainList != null)
			return bookedTrainList.get(position).hashCode();
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_train_booked, null);
		}
		Train train = bookedTrainList.get(position);
		TextView trainPnrNo = (TextView) view
				.findViewById(R.id.train_pnr_number);
		trainPnrNo.setText(train.getTrain_pnr());
		TextView trainJourneyDate = (TextView) view
				.findViewById(R.id.train_journey_date);
		trainJourneyDate.setText("Journey Date : " + train.getTrain_jdate());
		TextView trainTotalPrice = (TextView) view
				.findViewById(R.id.train_total_price);
		trainTotalPrice.setText("Price : " + train.getTrain_price());
		return view;
	}

	public List<Train> getItemList() {
		return bookedTrainList;
	}

	public void setItemList(List<Train> itemList) {
		this.bookedTrainList = itemList;
	}
}
