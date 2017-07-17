package com.zby.chest.bluetooth;



import com.zby.chest.AppConstants;
import com.zby.chest.agreement.CmdDataParse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BleBin implements ConnectionInterface{
	
	private final String TAG = BleBin.class.getSimpleName();
	
	private Context mActivity;
	private Handler mHandler;
	private BluetoothLeServiceMulp mService;
	private BluetoothAdapter mAdapter;
	private String mDeviceAddress;
	
	private boolean isLink;
	
	public BleBin (Context activity, Handler mHandler, BluetoothLeServiceMulp service) {
		this.mActivity = activity;
		this.mHandler = mHandler;
		this.mService = service;
	}
	//连接超时
	private int count =0;
	private long lastLinkTime = 0;
	@Override
	public void connect(String address, String pwd) {
		// TODO Auto-generated method stub
		long nowTime = System.currentTimeMillis();
		if(mService.isConnecting(mDeviceAddress)) {
			if(nowTime - lastLinkTime>2000) {
				lastLinkTime = nowTime;
				count ++;
				if (mDeviceAddress == null) {
					return;
				}
				if(count >AppConstants.connecting_count) {
					mService.close(mDeviceAddress);
					count =0;
				}
			}
			return;
		}
		count =0;
		if(nowTime - lastLinkTime<2000) {
			return;
		}
		lastLinkTime = nowTime;
		Log.e("Bluetooth", " ble  连接"+address);
		isLink = mService.connect(address);
		if(isLink) {
			mDeviceAddress = address;
		}
	}

	@Override
	public void stopConncet() {
		// TODO Auto-generated method stub
		if(mService!=null) {
			mService.disconnect(mDeviceAddress);
		}
	}

	@Override
	public void write(byte[] buffer) {
		// TODO Auto-generated method stub
		mService.writeLlsAlertLevel(mDeviceAddress, buffer);
	}

	@Override
	public void writeAgreement(byte[] buffer) {
		// TODO Auto-generated method stub
		if(buffer!=null) {
			//mService.writeLlsAlertLevel(Encrypt.ProcessCommand(buffer, buffer.length));
			mService.writeLlsAlertLevel(mDeviceAddress, buffer);
		}
	}

	@Override
	public boolean isLink() {
		// TODO Auto-generated method stub
		if(mService==null) {
			Log.d(TAG, "service is null");
			return false;
		}
		return mService.isLink(mDeviceAddress);
	}

	@Override
	public void Reconnect() {
		// TODO Auto-generated method stub
		
	}
	
	public String getDeviceAddress() {
		return mDeviceAddress;
	}

//	@Override
//	public void setDataParse(DataProtocolInterface dataParse) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void onBleDestory() {
		// TODO Auto-generated method stub
		mService.close(mDeviceAddress);
	}


	@Override
	public void setDataParse(CmdDataParse cmdParse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConnecting() {
		// TODO Auto-generated method stub
		if(mService==null) {
			Log.d(TAG, "service is null");
			return false;
		}
		return mService.isConnecting(mDeviceAddress);
	}
	
	public void closeAll() {
		if(mService==null) {
			Log.d(TAG, "service is null");
			return;
		}
		 mService.closeAll();
	}

	@Override
	public void stopReadThread(String mac) {
		// TODO Auto-generated method stub
		if(mService!=null) {
			mService.removeReadThread(mac);
		}
	}

	@Override
	public void startReadThread(String mac) {
		// TODO Auto-generated method stub
		if(mService!=null) {
			mService.startReadRssiThread(mac);
		}
	}

	@Override
	public void writeNoresponse(byte[] buffer) {
		// TODO Auto-generated method stub
		mService.writeLlsAlertLevelWait(mDeviceAddress, buffer);
	}

}
