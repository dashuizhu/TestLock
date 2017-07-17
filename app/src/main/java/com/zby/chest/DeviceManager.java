package com.zby.chest;

import com.zby.chest.model.DeviceBean;

public class DeviceManager {
	
	private DeviceBean dbin;
	
	private static DeviceManager mDeviceManager;
	
	private DeviceManager() {}
	
	public synchronized static DeviceManager getInstance() {
		if(mDeviceManager==null) {
			mDeviceManager = new DeviceManager();
		}
		return mDeviceManager;
	}

	public DeviceBean getDbin() {
		return dbin;
	}

	public void setDbin(DeviceBean dbin) {
		this.dbin = dbin;
	}
	
	

}
