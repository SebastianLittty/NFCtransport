package com.pjct.nfc.service;

public class Train {
	private String train_no;
	private String train_name;
	private String train_src;
	private String train_dstn;
	private String train_berth;
	private String train_price;
	private String train_available;
	private String train_pnr;
	private String train_jdate;

	public Train(String train_name, String train_available) {
		super();
		this.train_name = train_name;
		this.train_available = train_available;
	}

	public Train(String train_pnr, String train_jdate, String train_price) {
		super();
		this.train_pnr = train_pnr;
		this.train_jdate = train_jdate;
		this.train_price = train_price;
	}

	public Train(String train_no, String train_name, String train_src,
			String train_dstn, String train_available, String train_price) {
		super();
		this.train_no = train_no;
		this.train_name = train_name;
		this.train_src = train_src;
		this.train_dstn = train_dstn;
		this.train_available = train_available;
		this.train_price = train_price;
	}

	public Train(String train_no, String train_berth, String train_price,
			String train_pnr, String train_jdate) {
		super();
		this.train_no = train_no;
		this.train_berth = train_berth;
		this.train_price = train_price;
		this.train_pnr = train_pnr;
		this.train_jdate = train_jdate;
	}

	public String getTrain_no() {
		return train_no;
	}

	public String getTrain_name() {
		return train_name;
	}

	public String getTrain_src() {
		return train_src;
	}

	public String getTrain_dstn() {
		return train_dstn;
	}

	public String getTrain_berth() {
		return train_berth;
	}

	public String getTrain_price() {
		return train_price;
	}

	public String getTrain_available() {
		return train_available;
	}

	public String getTrain_pnr() {
		return train_pnr;
	}

	public String getTrain_jdate() {
		return train_jdate;
	}

	public void setTrain_no(String train_no) {
		this.train_no = train_no;
	}

	public void setTrain_name(String train_name) {
		this.train_name = train_name;
	}

	public void setTrain_src(String train_src) {
		this.train_src = train_src;
	}

	public void setTrain_dstn(String train_dstn) {
		this.train_dstn = train_dstn;
	}

	public void setTrain_berth(String train_berth) {
		this.train_berth = train_berth;
	}

	public void setTrain_price(String train_price) {
		this.train_price = train_price;
	}

	public void setTrain_available(String train_available) {
		this.train_available = train_available;
	}

	public void setTrain_pnr(String train_pnr) {
		this.train_pnr = train_pnr;
	}

	public void setTrain_jdate(String train_jdate) {
		this.train_jdate = train_jdate;
	}

}
