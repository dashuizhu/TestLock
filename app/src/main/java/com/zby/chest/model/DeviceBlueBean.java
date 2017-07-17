package com.zby.chest.model;

public class DeviceBlueBean {
	
	private String name;
	private String address;
	private int rssi;
	private int type;
	public String getName() {
		if(name!=null && name.contains("DP151") && name.length()>12) {
			String shortName = name.substring(name.length()-6, name.length());
			return "DP151-"+ shortName;
		}
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	

}
