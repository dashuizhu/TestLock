package com.zby.chest.bluetooth;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.zby.chest.AppConstants;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.model.CmdData;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.utils.MyByte;
import com.zby.chest.utils.MyLog;
import com.zby.chest.utils.Myhex;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
/**
 * @author Administrator
 * 在锁的项目， 设备复位后发送断开指令的设备，会重复收到数据。  callback里， 做了广播处理200毫秒没，重复的指令，重复的mac 不广播
 */
@SuppressLint("NewApi")
public class BluetoothLeServiceMulp extends Service {

	private final static String TAG = BluetoothLeServiceMulp.class
			.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;

	private Map<String, BluetoothGatt> gattMaps = new HashMap<String, BluetoothGatt>();
	private Map<String, BluetoothGatt> gattMapsConnting = new HashMap<String, BluetoothGatt>();
	private Map<String, CmdData> dataMaps = new HashMap<String, CmdData>();

	// private String mBluetoothDeviceAddress;
	// private BluetoothGatt mBluetoothGatt;
	// private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;
	
	private Set<String> scanSet = new HashSet<String>();
	
	private byte[] lastBuffer;

	/**
	 * 蓝牙设备过滤广播
	 */
	private final static String filterString = "02 01 06 03 02 F0 FF 05 FF 1C C1 B1 1C";
	
	/**
	 * 发现蓝牙设备广播   name  device  asrc
	 */
	public final static String ACTION_BLUETOOTH_FOUND = "com.example.bluetooth.le.ACTION_BLUETOOTH_FOUND";
	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	
	public final static String ACTION_RSSI_ACTION="com.example.bluetooth.le.rssi";
	
	/**
	 * 数据发送成功的回执
	 */
	public final static String ACTION_SEND_SUCCESS = "com.example.bluetooth.le.ACTION_SEND_SUCCESS";

	public static final UUID SEND_SERVIE_UUID = UUID
			.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
	public static final UUID SEND_CHARACTERISTIC_UUID = UUID
			.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

	public static final UUID RECEIVER_SERVICE = UUID
			.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
	public static final UUID RECEIVER_CHARACTERISTIC = UUID
			.fromString("0000fff3-0000-1000-8000-00805f9b34fb");

	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
			.fromString(CLIENT_CHARACTERISTIC_CONFIG);
	
	
	private Handler mHandler = new Handler();
	

	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	@SuppressLint("NewApi")
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			final String address = gatt.getDevice().getAddress();
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				// mConnectionState = STATE_CONNECTED;
				gattMaps.put(address, gatt);
				MyLog.w(TAG, address+ "  gattMaps .add " + gattMaps.size()+ " " );
				broadcastUpdate(intentAction, address);
				MyLog.w(TAG, address+"  Connected to GATT server." );
				// Attempts to discover services after successful connection.
						MyLog.d("tag",
								 address +" Attempting to start service discovery:"
										+ gattMaps.get(address).discoverServices());
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				// mConnectionState = STATE_DISCONNECTED;
				MyLog.w(TAG,address+ " Disconnected from GATT server." + status + " " + newState);
				removeReadThread(address);
				close(address);
				gattMaps.remove(address);
				broadcastUpdate(intentAction, address);
			}

			gattMapsConnting.remove(address);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			String address = gatt.getDevice().getAddress();
			Log.w(TAG, address +"  onServicesDiscovered received: 广播 " + status + " ");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, address);
				setReceiver(gatt);
			} else {
				Log.w(TAG, address +"  onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, gatt.getDevice().getAddress()+"  onCharacteristicRead");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt.getDevice().getAddress());
			}
		}

		/**
		 * 返回数据。
		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt.getDevice().getAddress());
			// 数据
			Log.i("xiawei", gatt.hashCode()+"  " + characteristic.getValue().toString());
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			try {
				String address=  gatt.getDevice().getAddress();
				Log.w("tag", gatt.getDevice().getAddress()+"   发送的回执发送广播" + status);
				dataMaps.remove(address);
				Intent intent = new Intent(ACTION_SEND_SUCCESS);
				sendBroadcast(intent);
			} catch(Exception e) {
				e.printStackTrace();
			}
			super.onCharacteristicWrite(gatt, characteristic, status);
		}
		
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			Intent intent = new Intent(ACTION_RSSI_ACTION);
			String mac = gatt.getDevice().getAddress();
			intent.putExtra("mac", mac);
			intent.putExtra("rssi", rssi);
			sendBroadcast(intent);
		};

	};


	private void broadcastUpdate(final String action, String mac) {
		final Intent intent = new Intent(action);
		intent.putExtra("mac", mac);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic, String mac) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		final byte[] data = characteristic.getValue();
		String ss = Myhex.buffer2String(data);
		MyLog.w(TAG,mac +" 收到数据发送广播:"+ss);
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {//心率
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
				Log.d(TAG, "Heart rate format UINT16.");
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
				Log.d(TAG, "Heart rate format UINT8.");
			}
			final int heartRate = characteristic.getIntValue(format, 1);
			Log.d(TAG, String.format("Received heart rate: %d", heartRate));
			intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
		} else {
			// For all other profiles, writes the data formatted in HEX.
			if (data != null && data.length > 0) {
				// final StringBuilder stringBuilder = new
				// StringBuilder(data.length);
				// for(byte byteChar : data)
				// stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, ss);
				intent.putExtra("mac", mac);
			}
		}
		sendBroadcast(intent);
		//sendBroadcast(intent,mac, ss);
	}
	
	private String lastMac="";
	private String lastValue="";
	private long lastTime=0l;
	/**
	 * 发送广播数据， 1秒内收到的重复数据不广播处理
	 * @param intent
	 * @param mac
	 * @param value
	 */
	private synchronized void sendBroadcastNo(Intent intent ,String mac, String value) {
		if(mac.equals(lastMac) && value.contains(lastValue)) {
			if(lastTime-System.currentTimeMillis() <200) {
				MyLog.w("tag_"+TAG," 重复的数据不坐广播处理");
				return;
			}
		}
		lastMac = mac;
		lastValue = value;
		lastTime = System.currentTimeMillis();
		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeServiceMulp getService() {
			return BluetoothLeServiceMulp.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		startScanThread(true);
//		resendThread(add);
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		startScanThread(false);
		closeAll();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public synchronized boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		// if (mBluetoothDeviceAddress != null &&
		// address.equals(mBluetoothDeviceAddress)
		// && mBluetoothGatt != null) {
		// Log.d(TAG,
		// "Trying to use an existing mBluetoothGatt for connection.");
		// if (mBluetoothGatt.connect()) {
		// mConnectionState = STATE_CONNECTING;
		// return true;
		// } else {
		// return false;
		// }
		// }

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		if (gattMapsConnting.containsKey(address)) {
			BluetoothGatt ga = gattMapsConnting.get(address);
			ga.close();
			gattMapsConnting.remove(address);
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		BluetoothGatt mBluetoothGatt = device.connectGatt(this, false,
				mGattCallback);
		gattMapsConnting.put(address, mBluetoothGatt);
		MyLog.d(TAG, "Trying to create a new connection.");
		// mBluetoothDeviceAddress = address;
		// mConnectionState = STATE_CONNECTING;
		System.out.println("device.getBondState==" + device.getBondState());
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect(String address) {
		if (gattMaps.containsKey(address)) {
			MyLog.d(TAG, address+"  disconnect connect blue : " );
			BluetoothGatt mBluetoothGatt = gattMaps.get(address);
			if (mBluetoothAdapter == null || mBluetoothGatt == null) {
				MyLog.w(TAG, "BluetoothAdapter not initialized");
				return;
			}
			mBluetoothGatt.disconnect();
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt.close();
			gattMaps.remove(address);
		}
		if (gattMapsConnting.containsKey(address)) {
			MyLog.d(TAG, address +"   disconnect connecting blue : " );
			BluetoothGatt mBluetoothGatt = gattMaps.get(address);
			if (mBluetoothAdapter == null || mBluetoothGatt == null) {
				Log.w(TAG, "BluetoothAdapter not initialized");
				return;
			}
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			gattMapsConnting.remove(address);
		}
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close(String address) {
		if (gattMaps.containsKey(address)) {
			BluetoothGatt mBluetoothGatt = gattMaps.get(address);
			if (mBluetoothGatt == null) {
				Log.w(TAG, "BluetoothAdapter not initialized");
				return;
			}
			// mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			MyLog.d(TAG, " close Bluetooth :" + address);
			mBluetoothGatt.close();
			gattMaps.remove(address);
			mBluetoothGatt = null;
		}
		if (gattMapsConnting.containsKey(address)) {
			BluetoothGatt mBluetoothGatt = gattMapsConnting.get(address);
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			gattMapsConnting.remove(address);
			mBluetoothGatt = null;
		}
	}

	public void closeAll() {
		Iterator<Map.Entry<String, BluetoothGatt>> gattItera = gattMaps.entrySet().iterator();
		while (gattItera.hasNext()) {
			Map.Entry<String, BluetoothGatt>  entry = gattItera.next();
			BluetoothGatt gatt = entry.getValue();
			gatt.disconnect();
			gatt.close();
			MyLog.w(TAG, "close connected: " + entry.getKey());
			gattItera.remove();
		}
		Iterator<Map.Entry<String, BluetoothGatt>> connItera = gattMapsConnting.entrySet().iterator();
		while (connItera.hasNext()) {
			Map.Entry<String, BluetoothGatt>  entry = connItera.next();
			BluetoothGatt gatt = entry.getValue();
			gatt.disconnect();
			gatt.close();
			MyLog.w(TAG, "close connecting: " + entry.getKey());
			connItera.remove();
		}
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(String address,
			BluetoothGattCharacteristic characteristic) {
		if (gattMaps.containsKey(address)) {
			BluetoothGatt mBluetoothGatt = gattMaps.get(address);
			if (mBluetoothAdapter == null || mBluetoothGatt == null) {
				Log.w(TAG, "BluetoothAdapter not initialized");
				return;
			}
			mBluetoothGatt.readCharacteristic(characteristic);
		}
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(String address,
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (gattMaps.containsKey(address)) {
			BluetoothGatt mBluetoothGatt = gattMaps.get(address);

			if (mBluetoothAdapter == null || mBluetoothGatt == null) {
				Log.w(TAG, "BluetoothAdapter not initialized");
				return;
			}
			boolean isEnable = mBluetoothGatt.setCharacteristicNotification(
					characteristic, enabled);
			Log.d(TAG, RECEIVER_CHARACTERISTIC.toString() + " "
					+ characteristic.getUuid().toString());
			if (RECEIVER_CHARACTERISTIC.toString().equals(
					characteristic.getUuid().toString())) {
				characteristic
						.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				BluetoothGattDescriptor descriptor = characteristic
						.getDescriptor(UUID
								.fromString(CLIENT_CHARACTERISTIC_CONFIG));
				if (descriptor != null) {
					descriptor
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				}
				mBluetoothGatt.writeDescriptor(descriptor);
			}
		}
	}
	
	public void setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
			BluetoothGattCharacteristic characteristic, boolean enabled) {
			if (mBluetoothAdapter == null || mBluetoothGatt == null) {
				Log.w(TAG, "BluetoothAdapter not initialized");
				return;
			}
			boolean isEnable = mBluetoothGatt.setCharacteristicNotification(
					characteristic, enabled);
			Log.d(TAG, RECEIVER_CHARACTERISTIC.toString() + " "
					+ characteristic.getUuid().toString());
			if (RECEIVER_CHARACTERISTIC.toString().equals(
					characteristic.getUuid().toString())) {
				characteristic
						.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				BluetoothGattDescriptor descriptor = characteristic
						.getDescriptor(UUID
								.fromString(CLIENT_CHARACTERISTIC_CONFIG));
				if (descriptor != null) {
					descriptor
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				}
				mBluetoothGatt.writeDescriptor(descriptor);
			}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices(String address) {
		if (gattMaps.containsKey(address)) {
			BluetoothGatt mBluetoothGatt = gattMaps.get(address);
			if (mBluetoothGatt == null)
				return null;
			return mBluetoothGatt.getServices();
		}
		return null;
	}

	public void writeLlsAlertLevel(String address, byte[] bb) {
		if (!gattMaps.containsKey(address)) {
			Log.e(TAG, "gatt is null " + address);
			return;
		}
		BluetoothGatt mBluetoothGatt = gattMaps.get(address);
		// Log.i("iDevice", iDevice);
		BluetoothGattService linkLossService = mBluetoothGatt
				.getService(SEND_SERVIE_UUID);
		if (linkLossService == null) {
			showMessage("link loss Alert service not found!" + mBluetoothGatt.getServices().size());
			//close(address);
			return;
		}
		// enableBattNoti(iDevice);
		BluetoothGattCharacteristic alertLevel = null;
		boolean status = false;
		alertLevel = linkLossService
				.getCharacteristic(SEND_CHARACTERISTIC_UUID);
		if (alertLevel == null) {
			showMessage("link loss Alert Level charateristic not found!");
			return;
		}
		int storedLevel = alertLevel.getWriteType();
		Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);

		alertLevel.setValue(bb);

		alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		status = mBluetoothGatt.writeCharacteristic(alertLevel);
		
		lastBuffer = bb;
		//resendThread(address);
		MyLog.e("tag_send",address+ "  发送  " + "  "+ status + " " + Myhex.buffer2String(bb));
		//Log.w(TAG, "writeLlsAlertLevel() - status=" + status);
	}
	
	
	
	public void writeLlsAlertLevelWait(String address, byte[] bb) {
		if (!gattMaps.containsKey(address)) {
			Log.e(TAG, "gatt is null " + address);
			return;
		}
		BluetoothGatt mBluetoothGatt = gattMaps.get(address);
		// Log.i("iDevice", iDevice);
		BluetoothGattService linkLossService = mBluetoothGatt
				.getService(SEND_SERVIE_UUID);
		if (linkLossService == null) {
			showMessage(address+ " link loss Alert service not found!  close  "+mBluetoothGatt.getServices().size()  + " ");
			//close(address);
			return;
		}
		// enableBattNoti(iDevice);
		BluetoothGattCharacteristic alertLevel = null;
		boolean status = false;
		alertLevel = linkLossService
				.getCharacteristic(SEND_CHARACTERISTIC_UUID);
		if (alertLevel == null) {
			showMessage("link loss Alert Level charateristic not found!");
			return;
		}
		int storedLevel = alertLevel.getWriteType();
		Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);

		alertLevel.setValue(bb);

		alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		status = mBluetoothGatt.writeCharacteristic(alertLevel);
		
		lastBuffer = bb;
		//resendThread(address);
		MyLog.e("tag_send",  address+ "  发送送  "+ " " + status + " " + Myhex.buffer2String(bb));
		//Log.w(TAG, "writeLlsAlertLevel() - status=" + status);
	}
	
	Thread resendThread;
	private void resendThread(final String address) {
		resendThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(AppConstants.ReSendTime);
					if(lastBuffer!=null) {
						if (!gattMaps.containsKey(address)) {
							Log.e(TAG, "gatt is null " + address);
							return;
						}
						BluetoothGatt mBluetoothGatt = gattMaps.get(address);
						// Log.i("iDevice", iDevice);
						BluetoothGattService linkLossService = mBluetoothGatt
								.getService(SEND_SERVIE_UUID);
						if (linkLossService == null) {
							showMessage("link loss Alert service not found!");
							return;
						}
						// enableBattNoti(iDevice);
						BluetoothGattCharacteristic alertLevel = null;
						boolean status = false;
						alertLevel = linkLossService
								.getCharacteristic(SEND_CHARACTERISTIC_UUID);
						if (alertLevel == null) {
							showMessage("link loss Alert Level charateristic not found!");
							return;
						}
						int storedLevel = alertLevel.getWriteType();
						Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);
						if(lastBuffer!=null) {
							alertLevel.setValue(lastBuffer);
							
							alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
							status = mBluetoothGatt.writeCharacteristic(alertLevel);
						}
						
						MyLog.v("tag_send", address+"  重新发送发送  " + status + " " + Myhex.buffer2String(lastBuffer));
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		resendThread.start();
	}
	
	private void showMessage(String msg) {
		MyLog.e(TAG, msg);
	}

	/**
	 * 设置回收发的服务
	 */
	public void setReceiver(String address) {
		if (!gattMaps.containsKey(address)) {
			Log.e(TAG, "gatt is null " + address);
			return;
		}
		BluetoothGatt mBluetoothGatt = gattMaps.get(address);

		BluetoothGattService linkLossService = mBluetoothGatt
				.getService(RECEIVER_SERVICE);
		if (linkLossService == null)
			return;
		BluetoothGattCharacteristic characteristic = linkLossService
				.getCharacteristic(RECEIVER_CHARACTERISTIC);
		if (characteristic == null)
			return;

		setCharacteristicNotification(address, characteristic, true);
		readCharacteristic(address, characteristic);
	}
	
	public void setReceiver(BluetoothGatt mBluetoothGatt) {
		BluetoothGattService linkLossService = mBluetoothGatt
				.getService(RECEIVER_SERVICE);
		if (linkLossService == null)
			return;
		BluetoothGattCharacteristic characteristic = linkLossService
				.getCharacteristic(RECEIVER_CHARACTERISTIC);
		if (characteristic == null)
			return;

		setCharacteristicNotification(mBluetoothGatt, characteristic, true);
		//readCharacteristic(address, characteristic);
	}

	protected boolean isLink(String mac) {
		return gattMaps.containsKey(mac);
	}

	public boolean isConnecting(String mDeviceAddress) {
		// TODO Auto-generated method stub
		return gattMapsConnting.containsKey(mDeviceAddress);
	}
	
	
	
	/**
	 * 蓝牙设备搜索 监听
	 */
	private BluetoothAdapter.LeScanCallback scanCallBack = new LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice arg0, int arg1, byte[] arg2) {
			// TODO Auto-generated method stub
			if(scanSet.contains(arg0.getAddress())) {
				return;
			}
			scanSet.add(arg0.getAddress());
			String broadcast = Myhex.buffer2String(arg2);
			if(broadcast.toLowerCase().startsWith(filterString.toLowerCase())) {
				if(arg2.length>=33) {
					byte[] namebuff = new byte[18];
					String name="";
					System.arraycopy(arg2, 15, namebuff, 0, 18);
					try {
						name= new String(namebuff, AppConstants.CharSet);
//						Log.d(TAG,"发现蓝牙设备 all: " +Myhex.buffer2String(arg2));
//						Log.d(TAG,"发现蓝牙设备 : " +Myhex.buffer2String(namebuff));
						Log.w(TAG,"发现蓝牙设备: " +name + " " + arg0.getAddress() );
						foundDevice(arg0, name, arg1, 0, arg2[33]);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
			}
			//***************原来搜索广播解析**************************
//			int nameLenght = MyByte.byteToInt(arg2[7])-1;
//			//arg2[7]是名字长度 
//			//从arg2[9】开始是名字
//			//03 16 固定2个字节， 后面跟 模式  和  开关
//			byte[] bf = new byte[6];
//			System.arraycopy(arg2, 7+nameLenght+2, bf, 0, 6);
//			byte[] nameBuff=  new byte[MyByte.byteToInt(nameLenght)];
//			System.arraycopy(arg2, 9, nameBuff, 0, nameBuff.length);
//			String name="";
//			System.out.println(Myhex.buffer2String(arg2));
//			try {
//				name= new String(nameBuff, AppConstants.CharSet);
//				Log.d(TAG,"发现蓝牙设备: " +name + " " + arg0.getAddress() + " " +arg1 + " 广播内容"+Myhex.buffer2String(bf));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			byte mode = arg2[7+1+nameLenght+2+1];
//			byte onoff = arg2[7+1+nameLenght+2+2];
//					//11是开 1F是关
//			foundDevice(arg0, name, arg1, mode, onoff);
		}
	};

	/**
	 * 广播设备
	 * @param device
	 * @param arg1
	 */
//	private void foundDevice(BluetoothDevice device, int arg1,int type, byte onOff) {
//		if(scanSet.contains(device.getAddress()) || (onOff!=0x11 && onOff!=0x1F)) {//过滤
//			return;
//		}
//		Intent intent = new Intent(ACTION_BLUETOOTH_FOUND);
//		intent.putExtra("mac", device.getAddress());
//		intent.putExtra("name", device.getName());
//		intent.putExtra("rssi", arg1);
//		intent.putExtra("type", type);
//		intent.putExtra("onOff", onOff==0x11);
//		sendBroadcast(intent);
//	}
	
	/**
	 * 广播设备
	 * @param device
	 * @param arg1
	 */
	private void foundDevice(BluetoothDevice device, String name, int arg1,int type, byte onOff) {
//		if(scanSet.contains(device.getAddress()) ) {//过滤
//			return;
//		}
		Intent intent = new Intent(ACTION_BLUETOOTH_FOUND);
		intent.putExtra("mac", device.getAddress());
		intent.putExtra("name", name);
		intent.putExtra("rssi", arg1);
		intent.putExtra("type", type);
		intent.putExtra("onOff", onOff==0x11);
		sendBroadcast(intent);
	}
	
	/**
	 * 蓝牙搜索线程	
	 * @param onOff 开始或 停止搜索
	 */
	private synchronized void startScanThread(boolean onOff) {
		if(onOff) {
			scanThread = new Thread(new ScanRunnable());
			scanThread.start();
		} else {
			if(scanThread!=null) {
				scanThread.interrupt();
				scanThread=null;
			}
		}
	}
	
	Thread scanThread ;
	class ScanRunnable implements Runnable {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean isScan = true;
			Log.v(TAG,"开始搜索线程");
			if(mBluetoothAdapter!=null && !mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.enable();
			}
			while(isScan) {
				if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()) {
					scanSet.clear();
					mBluetoothAdapter.startLeScan(scanCallBack);
				}
				try {
					Thread.sleep(AppConstants.scan_time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					isScan = false;
					break;
				} finally {
					if(mBluetoothAdapter!=null) {
						mBluetoothAdapter.stopLeScan(scanCallBack);
					}
				}
			}
			Log.v(TAG,"搜索线程停止");
		}
	};
	
	private Map<String ,ReadRssiThread> readList = new HashMap<String ,ReadRssiThread>();
	
	public void startReadRssiThread(String address) {
		ReadRssiThread rssThread = new ReadRssiThread(address);
		rssThread.start();
		if(readList.containsKey(address)) {
			readList.get(address).interrupt();
		}
		Log.e("tag", "读取信号" + address);
		readList.put(address, rssThread);
	}
	
	/**
	 * 读取蓝牙RSSi线程
	 */
	private class ReadRssiThread extends Thread{
		
		String address;
		
		public ReadRssiThread(String address) {
			this.address = address;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (gattMaps.containsKey(address)){
				try {
					sleep(AppConstants.readRSSITime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				//如果读取蓝牙RSSi回调成功
				BluetoothGatt gatt = gattMaps.get(address);
				if(gatt!=null) {
					gatt.readRemoteRssi();
				}else {
					break;
				}
				
			}
			readList.remove(this);
			
		}
		
	}
	
	public void removeReadThread(String address) {
		if(readList.containsKey(address)) {
			readList.get(address).interrupt();
		}
		readList.remove(address);
	}

	public void stopReadThread() {
		// TODO Auto-generated method stub
		ReadRssiThread rsiThread;
		for(int i=0;i<readList.size(); i++) {
			rsiThread = readList.get(i);
			if(rsiThread!=null) {
				rsiThread.interrupt();
			}
		}
		
	}
}