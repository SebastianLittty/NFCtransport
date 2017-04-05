package com.pjct.nfc.service;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pjct.nfc.R;

public class TrainListAdapter extends ArrayAdapter<Train> {
	Context context;
	List<Train> trainList;

	public TrainListAdapter(List<Train> trainList, Context ctx) {
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
			view = inflater.inflate(R.layout.list_train, null);
		}
		Train train = trainList.get(position);
		TextView trainName = (TextView) view.findViewById(R.id.train_name);
		trainName.setText(train.getTrain_name());
		TextView trainAvailable = (TextView) view
				.findViewById(R.id.train_availablity);
		trainAvailable.setText(train.getTrain_available());
		return view;
	}

	public List<Train> getItemList() {
		return trainList;
	}

	public void setItemList(List<Train> itemList) {
		this.trainList = itemList;
	}
}
