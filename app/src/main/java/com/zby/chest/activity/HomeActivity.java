package com.zby.chest.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

import com.jungly.gridpasswordview.GridPasswordView;
import com.zby.chest.AppConstants;
import com.zby.chest.DeviceManager;
import com.zby.chest.LockApplication;
import com.zby.chest.R;
import com.zby.chest.adapter.DeviceAdapter;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.agreement.ConnectBroadcastReceiver;
import com.zby.chest.bluetooth.BleBin;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceListener;
import com.zby.chest.model.DeviceSqlService;
import com.zby.chest.utils.MyLog;
import com.zby.chest.utils.Myhex;
import com.zby.chest.view.AlertDialogService;
import com.zby.chest.view.AlertDialogService.MyAlertCallback;
import com.zby.chest.view.AlertDialogService.onMyInputListener2;
import com.zby.chest.view.MyGestureListener;
import com.zby.chest.view.MyListView;
import com.zby.chest.view.AlertDialogService.onMyInputListener;

public class HomeActivity extends BaseActivity {
	
	private String TAG = HomeActivity.class.getSimpleName();

	private List<DeviceBean> list;
	private MyListView listView;
	private DeviceAdapter adapter;
	
	private Dialog dialog;
	
	private DeviceSqlService delService;
	private DeviceBean selectDbin;
	private String selectpassword;
	private LockApplication mApp;
	
	private Dialog dialog_delete;
	
	
	private Set<String> showDialogSet = new HashSet<String>();
	
	private final int Activity_setting_detail = 11;
	

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		initViews();
		initHandler();
		actvivityname = " home";
	};
	
	private void initViews() {
		listView = (MyListView) findViewById(R.id.listView);
		// TEST/
		mApp = (LockApplication) getApplication();
		list = mApp.getDeviceList();
		if(AppConstants.isDemo) {
			 for (int i = 0; i < 15; i++) {
			 DeviceBean bin = new DeviceBean();
			 bin.setName("锁锁名字" + i);
			 bin.setMac("00000000000"+i);
			 bin.setOnOff(false);
			 bin.setModeType(DeviceBean.LockMode_scroll);
			 bin.setLink(i%2==0);
			 bin.setConnectionInterface(new BleBin(this, mHandler, null), this);
			 list.add(bin);
//			 new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					try {
//						Thread.sleep(10000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					for(int i=0;i<10;i++) {
//						DeviceBean dbin = list.get(i%3);
//						dbin.parseData(Myhex.hexStringToByte("0xCC 0x40 0x01 0x81 0xC0"));
//					}
//				}
//			}).start();
			 }
		 mApp.compareArray(list);
		}
		adapter = new DeviceAdapter(this, list);
		adapter.setDeviceListener(new DeviceListener() {

			@Override
			public void onDeviceScroll(DeviceBean dbin) {
				// TODO Auto-generated method stub
				if(dbin!=null  && dbin.getModeType() == DeviceBean.LockMode_scroll) {
					//if(!dbin.isOnOff()) {
						dbin.write(CmdPackage.getLockOff(DeviceBean.LockMode_scroll));
					//}
					Log.d(TAG, "deviceListener.onScroll " + dbin.getName() + " " + dbin.getModeType());
				}
			}

			@Override
			public void onDeviceItemClick(final DeviceBean dbin) {
				// TODO Auto-generated method stub
					Log.e(TAG," onDeviceItemClick " + dbin.isLink() + "  type =" + dbin.getModeType());
					if(dbin.isLink() && dbin.getModeType()==0) {
						dbin.write(CmdPackage.getStatus());
						return;
					}
					if(dbin.isLink()&&  dbin.getModeType()==DeviceBean.LockMode_password) {
						//if(dialog==null) {
							//if(dbin.getPassword()==null || dbin.getPassword().equals("")) {
								if(dialog!=null && dialog.isShowing()) {
									dialog.dismiss();
								}
								dialog = AlertDialogService.getInputDialog2(HomeActivity.this, "", getString(R.string.password_offlock_input), new onMyInputListener2() {
									

									@Override
									public void onCancel(Dialog d) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void onClick(Dialog d, String password,
											GridPasswordView grv) {
										// TODO Auto-generated method stub
										Log.e(TAG," onDeviceItemClick " +password);
										if(password.length()!=6) {
											showToast(R.string.password_input);
											return;
										}
										if(dbin.write(CmdPackage.getLockOff(password))){
											selectpassword = password;
											selectDbin = dbin;
											d.dismiss();
										}
									}
								});
								dialog.show();
//							} else {
//								if(dbin.write(CmdPackage.getLockOff(dbin.getPassword()))){
//									selectDbin = dbin;
//									if(dialog!=null && dialog.isShowing()) {
//										dialog.dismiss();
//									}
//								}
//							}
				}
			}

			@Override
			public void onDeviceLongClick(final DeviceBean deviceBean) {
				// TODO Auto-generated method stub
				Log.e(TAG," onDevicelong " );
				if(deviceBean.isLinkSuccess()) {
					DeviceManager.getInstance().setDbin(deviceBean);
					Intent intent = new Intent(HomeActivity.this, SettingDetailActivity.class);
					startActivityForResult(intent, Activity_setting_detail);
				} else {
					dialog_delete = AlertDialogService.getConfirmDialog(HomeActivity.this, getString(R.string.confirm_delete), "", new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							if(dialog_delete!=null) {
								dialog_delete.dismiss();
							}
							if(deviceBean.isLink()) {
								deviceBean.closeConnect();
							}
							mApp.removeDeviceBin(deviceBean.getMac());
							adapter.notifyDataSetChanged();
						}
					});
					if(!isFinishing()) {
						dialog_delete.show();
					}
					//showToast(R.string.unlink);
				}
			}

			@Override
			public void onDeviceLongLongClick(DeviceBean deviceBean) {
				// TODO Auto-generated method stub
//					mApp.removeDeviceBin(deviceBean.getMac());
//					adapter.notifyDataSetChanged();
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
//		listView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				// TODO Auto-generated method stub
//				final DeviceBean dbin = list.get(arg2);
//				if(dbin.isLink()&&  !dbin.isOnOff() && dbin.getModeType()==DeviceBean.LockMode_password) {
//					//if(dialog==null) {
//						dialog = AlertDialogService.getInputPasswordDialog(HomeActivity.this, "", getString(R.string.password_input), new onMyInputListener() {
//							
//							@Override
//							public void onClick(Dialog d, EditText tv) {
//								// TODO Auto-generated method stub
//								String password = tv.getText().toString().trim();
//								if(password.length()!=6) {
//									showToast(R.string.password_input);
//									return;
//								}
//								if(dbin.write(CmdPackage.getLockOff(password))){
//									d.dismiss();
//								}
//							}
//						});
//					//}
//					dialog.show();
//				}
//			}
//		});

	}

	@Override
	void initHandler() {
		Log.e("tag" , "inithandler () ");
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case BroadcastString.Broad_Cmd:
						switch(msg.arg1) {
							case CmdDataParse.type_lock_mode:
								Log.e("tag" , "接收到刷新界面指令");
								mApp.compareArray(list);
								adapter.notifyDataSetChanged();
								break;
							case CmdDataParse.type_lock_onoff:
								adapter.notifyDataSetChanged();
								break;
							case CmdDataParse.type_status:
								mApp.compareArray(list);
								adapter.notifyDataSetChanged();
								break;
							case CmdDataParse.type_password_error:
								showToast(R.string.password_error);
								selectDbin.setPassword("");
								if(delService==null) {
									delService = new DeviceSqlService(HomeActivity.this);
								}
								delService.insert(selectDbin);
								break;
							case CmdDataParse.type_password_success://开锁密码
								adapter.notifyDataSetChanged();
								if(delService==null) {
									delService = new DeviceSqlService(HomeActivity.this);
								}
								selectDbin.setPassword(selectpassword);
								delService.insert(selectDbin);
								break;
							case CmdDataParse.type_password_verify_error://配对密码错误
								
								String mac = (String) msg.obj;
								Log.d("tag_"+TAG, "配对密码错误" + mac);
                                adapter.notifyDataSetChanged();
								break;
							case CmdDataParse.type_binds_fail:
							    String macc = (String) msg.obj;
                                Log.d("tag_"+TAG, "绑定失败错误" + macc);
                                mApp.removeDeviceBinNotStopBandingFail(macc, true);
                                adapter.notifyDataSetChanged();
							    break;
							case CmdDataParse.type_low_alert:
								String[] nameMac= (String[]) msg.obj;
								showAlertDialog(nameMac);
								break;
						}
					break;
					case handler_adapter:
						if(adapter!=null){
							adapter.notifyDataSetChanged();
						}
						break;
				}
			}
		};
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.e("tag", " home on activity back " + hashCode());
		switch(requestCode) {
		case Activity_setting_detail:
			if(resultCode==Activity.RESULT_OK) {
				onStart();
			} else {
				//super 是inithandler
				((LockApplication) getApplication()).setHandler(mHandler);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 第一个为name  第二个string为mac
	 * @param nameMac
	 */
	private synchronized void showAlertDialog(String[] nameMac){
		if(nameMac!=null && nameMac.length>=2) {
			String name = nameMac[0];
			String macadd = nameMac[1];
			if(!showDialogSet.contains(macadd)) {
				if(!isFinishing()) {
					AlertDialogService.getAlertDialog(HomeActivity.this, name, macadd, getString(R.string.low_alert), new MyAlertCallback() {
						
						@Override
						public void onClick(String mac) {
							// TODO Auto-generated method stub
							if(!isFinishing()) {
								showDialogSet.remove(mac);
							}
						}
						
					}).show();
				}
			}
		}
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	boolean isRegister = false;
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		((LockApplication)getApplication()).setHandler(mHandler);
		if(!isRegister){
			isRegister = true;
			registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		}
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
		list = mApp.getDeviceList();
		mApp.compareArray(list);
		adapter.setList(list);
		adapter.notifyDataSetChanged();
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if(isRegister) {
			isRegister = false;
			unregisterReceiver(mGattUpdateReceiver);
		}
		super.onStop();
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(LockApplication.ACTION_NAME_CHANGE);
		return intentFilter;
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			String mac = intent.getStringExtra("mac");
			if (BluetoothLeServiceMulp.ACTION_GATT_CONNECTED.equals(action)) {
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
			} else if (BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
 
			} else if(action.equals(LockApplication.ACTION_NAME_CHANGE)) {
				adapter.notifyDataSetChanged();
			}
		}
	};

	
	
}
