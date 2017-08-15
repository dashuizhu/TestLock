package com.zby.chest.activity;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.jungly.gridpasswordview.GridPasswordView;
import com.zby.chest.AppString;
import com.zby.chest.DeviceManager;
import com.zby.chest.LockApplication;
import com.zby.chest.R;
import com.zby.chest.adapter.BluetoothDeviceAdapter;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceBlueBean;
import com.zby.chest.utils.Myhex;
import com.zby.chest.utils.Tools;
import com.zby.chest.view.AlertDialogService;
import com.zby.chest.view.AlertDialogService.onMyInputListener2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanActivity extends BaseActivity {
	
	private final static String TAG = ScanActivity.class.getSimpleName();
	
	private BluetoothAdapter btAdapter;
	private BluetoothDeviceAdapter adapter;
	private List<DeviceBlueBean> list;
	private ListView listView;
	private LockApplication app;
	
	private Set<String> macList = new HashSet<String>();
	
	private DeviceBean dbin ;
	
	private Dialog dialog;
	
	private final int activity_setting = 11;
	
	private TextView tv_scan;
	
	private EditText et_input;
	private GridPasswordView et_grv;
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		initBaseViews(this);
		initViews();
		initHandler();
		registerBroadcast();
	};
	
	private BroadcastReceiver br = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			if(action.equals(AppString.ACTION_LANGUAGE)) {
				initTextData();
			}
	}};

		
		private void registerBroadcast() {
			registerReceiver(br, new IntentFilter(AppString.ACTION_LANGUAGE));
		}
		
		private void initTextData(){
			tv_scan.setText(R.string.device_found);
		}
		
	
	private void initViews() {
		app = (LockApplication) getApplication();
		list = new ArrayList<DeviceBlueBean>();
		macList = app.getDeviceMacList();
		adapter =new BluetoothDeviceAdapter(this, list);
		listView = (ListView) findViewById(R.id.listView);
		tv_scan = (TextView) findViewById(R.id.textView_scan);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
				if(dbin!=null && dbin.getConnectionInterface()!=null) {
					dbin.stopConnect();
				}
				showToast(R.string.linking);
				DeviceBlueBean dbb = list.get(arg2);
				dbin = app.newDeviceBean(dbb.getAddress(), dbb.getName(), dbb.getType());
				dbin.connect();
			}
		});
//		adapter.setMyClickListener(new MyClickListener() {
//
//			@Override
//			public void onItemClick(String address, String name, int type) {
//				// TODO Auto-generated method stub
//				if(dbin!=null && dbin.getConnectionInterface()!=null) {
//					dbin.stopConnect();
//				}
//				showToast(R.string.linking);
//				dbin = app.newDeviceBean(address, name, type);
//				dbin.connect();
//			}});
	}
	
	@Override
	void initHandler() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case BroadcastString.Broad_Cmd:
					String mac;
					switch(msg.arg1) {
						//					case CmdDataParse.type_binds_success:
						//						mac  = (String) msg.obj;
						//						if(dbin==null) {
						//							Log.d(TAG, " no select deviceBin ");
						//							return;
						//						}
						//						if(dbin.getMac().equals(mac)) {
						//							showToast(R.string.binds_success);
						//							app.addDeviceBin(dbin);
						//							//绑定陈宫后，从这个记录里删除
						//							removeDevice(dbin.getMac());
						//							macList.add(dbin.getMac());
						//							if(dialog!=null && dialog.isShowing()) {
						//								dialog.dismiss();
						//							}
						//							if(dbin.getPairPassword()!=null && dbin.getPairPassword().equals("000000")) {
						////								AlertDialogService.getFirstBindsDialog(ScanActivity.this,"", getString(R.string.first_binds_info), new onMyInputListener() {
						////									@Override
						////									public void onClick(Dialog d, EditText tv) {
						////										// TODO Auto-generated method stub
						//										DeviceManager.getInstance().setDbin(dbin);
						//										Intent intent = new Intent(ScanActivity.this, SettingPasswordActivity.class);
						//											intent.putExtra("type", 0);//0表示修改配对密码
						//											intent.putExtra("showBack", false);
						//											startActivityForResult(intent, activity_setting);
						//											showToast(R.string.first_binds_info);
						////									}
						////								}).show();
						//							} else {//绑定成功直接条首页
						//									Intent intent = new Intent(AppString.ACTION_VIEWPAGER);
						//									intent.putExtra("item", 0);
						//									sendBroadcast(intent);
						//							}
						//						}
						//						break;
						//					case CmdDataParse.type_binds_fail:
						//						mac  = (String) msg.obj;
						//						if(dbin==null) {
						//							Log.d(TAG, " no select deviceBin ");
						//							return;
						//						}
						//						if(dbin.getMac().equals(mac)) {
						//							//Toast.makeText(ScanActivity.this, R.string.binds_fail, 3).show();
						//							showToast(R.string.binds_fail);
						//							dbin.closeConnect();
						//							dbin.stopConnect();
						//							if(dialog!=null && dialog.isShowing()) {
						//								dialog.dismiss();
						//							}
						//						}
						//						break;
					case CmdDataParse.type_password_verify_error:
						mac  = (String) msg.obj;
						if(dbin==null) {
							Log.d(TAG, " no select deviceBin ");
							return;
						}
						if(dbin.getMac().equals(mac)) {
						showToast(R.string.password_verify_error); 
//						if(et_input!=null) {
//							et_input.setText("");
//							et_input.requestFocus();
//						}
						if(et_grv!=null) {
							et_grv.clearPassword();
							//et_grv.clearFocus();
						}
						}
						break;
					case CmdDataParse.type_password_verify_success:
						mac  = (String) msg.obj;
						if(dbin==null) {
							Log.d(TAG, " no select deviceBin ");
							return;
						}
						if(dbin.getMac().equals(mac)) {
							showToast(R.string.password_verify_success);
							//new Thread(new Runnable() {
							//
							//	@Override
							//	public void run() {
							//		// TODO Auto-generated method stub
							//		try {
							//			//发送密码 的回执需要 1秒只发一次数据， 这里等待回执后在发送。
							//			Thread.sleep(1000);
							//		} catch (InterruptedException e) {
							//			// TODO Auto-generated catch block
							//			e.printStackTrace();
							//		}
							//		if(dbin !=null) {
							//			dbin.write(CmdPackage.verifyMac(Tools.getMacAddress(ScanActivity.this)));
							//		}
							//	}
							//}).start();
							app.addDeviceBin(dbin);
							//绑定陈宫后，从这个记录里删除
							removeDevice(dbin.getMac());
							macList.add(dbin.getMac());
							if (dialog != null && dialog.isShowing()) {
								dialog.dismiss();
								}
							if (dbin.getPairPassword() != null && dbin.getPairPassword()
											.equals("000000")) {
								//								AlertDialogService.getFirstBindsDialog(ScanActivity.this,"", getString(R.string.first_binds_info), new onMyInputListener() {
								//									@Override
								//									public void onClick(Dialog d, EditText tv) {
								//										// TODO Auto-generated method stub
								DeviceManager.getInstance().setDbin(dbin);
								Intent intent = new Intent(ScanActivity.this,
												SettingPasswordActivity.class);
								intent.putExtra("type", 0);//0表示修改配对密码
								intent.putExtra("showBack", false);
								startActivityForResult(intent, activity_setting);
								showToast(R.string.first_binds_info);
								//									}
								//								}).show();
							} else {//绑定成功直接条首页
								Intent intent = new Intent(AppString.ACTION_VIEWPAGER);
								intent.putExtra("item", 0);
								sendBroadcast(intent);
							}
						}
						break;
					}
				}
			}
		};
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case activity_setting:
			if(resultCode == Activity.RESULT_OK) {
				Intent intent = new Intent(AppString.ACTION_VIEWPAGER);
				intent.putExtra("item", 0);
				sendBroadcast(intent);
			}
			break;
		}
	}
	
	

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_search:
			list.clear();
			if(adapter!=null) {
				adapter.notifyDataSetChanged();
			}
			if(dbin!=null) {
				dbin.stopConnect();
			}
			break;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
		if(app!=null) {
			macList = app.getDeviceMacList();
		}
		super.onResume();
	}
	
	private synchronized void registerBluetoothBroadcast() {
		IntentFilter interFilter = new IntentFilter(BluetoothLeServiceMulp.ACTION_BLUETOOTH_FOUND);
		interFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_SERVICES_DISCOVERED);
		interFilter.addAction(BluetoothLeServiceMulp.ACTION_DATA_AVAILABLE);
		interFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED);
		
		bluereceiver= new BroadcastReceiver() {
			public void onReceive(android.content.Context arg0, android.content.Intent intent) {
				String mac = intent.getStringExtra("mac");
				if(intent.getAction().equals(BluetoothLeServiceMulp.ACTION_BLUETOOTH_FOUND)) {//发现了蓝牙设备
					Log.e("tag", "发现蓝牙设备 " + mac);
					if(macList.contains(mac)) return;//已经绑定的设备不再显示
					String name =intent.getStringExtra("name");
					int rssi =intent.getIntExtra("rssi", 100);
					int type = intent.getIntExtra("type", 1);
					foundDevice(mac, name, rssi,  type);
				} else if(intent.getAction().equals(BluetoothLeServiceMulp.ACTION_GATT_SERVICES_DISCOVERED)) {//连接成功
					if(dbin==null || !mac.equals(dbin.getMac())) {
						return;
					}
					disDialog();
					dialog = AlertDialogService.getInputDialog2(ScanActivity.this,"", getString(R.string.password_pair_input), new onMyInputListener2() {
						
						@Override
						public void onClick(Dialog d, String password,
								GridPasswordView grv) {
							// TODO Auto-generated method stub
							//et_input = tv;
							et_grv = grv;
							if(password.length()==6) {
								dbin.setPairPassword(password);
								dbin.write(CmdPackage.verifyPassword(password));
							} else {
								showToast(R.string.password_input);
							}
						}

						@Override
						public void onCancel(Dialog d) {
							// TODO Auto-generated method stub
							if(d!=null && d.isShowing()) {
								d.cancel();
							}
							if(dbin!=null) {
								dbin.stopConnect();
							}
						}
					});
					dialog.show();
				} else if (BluetoothLeServiceMulp.ACTION_DATA_AVAILABLE.equals(intent.getAction())) { //解析数据
					if(dbin==null || !mac.equals(dbin.getMac())) {
						return;
					}
					String buffer = intent.getStringExtra(BluetoothLeServiceMulp.EXTRA_DATA);
					Log.d("tag", this.hashCode()+"接受数据2"+ buffer);
					if(dbin!=null) {
						dbin.parseData(Myhex.hexStringToByte(buffer));
					}
				}  else if(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {
					if(dbin==null || !mac.equals(dbin.getMac())) {
						return;
					}
					showToast(R.string.link_lost);
					disDialog();
					
				}
			};
		};
		registerReceiver(bluereceiver, interFilter);
	}
	
	private BroadcastReceiver bluereceiver;
	
	private synchronized void foundDevice(String mac ,String name, int rssi, int type) {
		if (TextUtils.isEmpty(name) || !name.trim().toLowerCase().startsWith("dp151a")) {
			return;
		}
		DeviceBlueBean bBean;
		for(int i=0; i<list.size(); i++) {
			bBean = list.get(i);
			if(bBean.getAddress().equals(mac)) {
				if(Tools.isStringCheck(name, bBean.getName())) {
					list.get(i).setName(name);
					adapter.notifyDataSetChanged();
				}
				return;
			}
		}
		bBean = new DeviceBlueBean();
		bBean.setName(name);
		bBean.setAddress(mac);
		bBean.setRssi(rssi);
		bBean.setType(type);
		list.add(bBean);
		adapter.notifyDataSetChanged();
	}
	
	private synchronized void removeDevice(String mac) {
		DeviceBlueBean bBean;
		for(int i=0; i<list.size(); i++) {
			bBean = list.get(i);
			if(bBean.getAddress().equals(mac)) {
				list.remove(i);
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}
	
	@Override
	protected void onStart() {
		Log.e("tag", "scan  on Start()");
		if(bluereceiver==null) {
			registerBluetoothBroadcast();
		}
		super.onStart();
	};
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if(bluereceiver!=null) {
			unregisterReceiver(bluereceiver);
		}
		bluereceiver=null;
		dbin =null;
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		disDialog();
		unregisterReceiver(br);
		super.onDestroy();
	}
	
	private void disDialog() {
		if(dialog!=null) {
			if(dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}
}
