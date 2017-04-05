package com.pjct.nfc.service;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pjct.nfc.R;

public class TrainBookedDetailAdapter extends ArrayAdapter<Train> {
	Context context;
	List<Train> bookedTrainList;

	public TrainBookedDetailAdapter(List<Train> bookedTrainList, Context context) {
		super(context, android.R.layout.simple_list_item_1, bookedTrainList);
		this.bookedTrainList = bookedTrainList;
		this.context = context;
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

	public List<Train> getItemList() {
		return bookedTrainList;
	}

	public void setItemList(List<Train> itemList) {
		this.bookedTrainList = itemList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_train_booked_detail, null);
		}
		Train train = bookedTrainList.get(position);
		TextView trainbookedpnr = (TextView) view
				.findViewById(R.id.train_booked_pnr);
		trainbookedpnr.setText("PNR No : " + train.getTrain_pnr());
		TextView trainbookedno = (TextView) view
				.findViewById(R.id.train_booked_no);
		trainbookedno.setText("Train No : " + train.getTrain_no());
		TextView trainbookedberth = (TextView) view
				.findViewById(R.id.train_booked_berth);
		trainbookedberth.setText("Berth No : " + train.getTrain_berth());
		TextView trainbookedjdate = (TextView) view
				.findViewById(R.id.train_booked_jdate);
		trainbookedjdate.setText("Journey Date : " + train.getTrain_jdate());
		TextView trainbookedprice = (TextView) view
				.findViewById(R.id.train_booked_price);
		trainbookedprice.setText("Price : " + train.getTrain_price());

		return view;
	}
}
