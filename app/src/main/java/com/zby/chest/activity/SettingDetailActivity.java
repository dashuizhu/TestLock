package com.zby.chest.activity;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zby.chest.AppConstants;
import com.zby.chest.AppString;
import com.zby.chest.DeviceManager;
import com.zby.chest.LockApplication;
import com.zby.chest.R;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.agreement.ConnectBroadcastReceiver;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceSqlService;
import com.zby.chest.utils.SetupData;
import com.zby.chest.utils.Tools;
import com.zby.chest.view.AlertDialogService;
import com.zby.chest.view.AlertDialogService.SelectCallback;
import com.zby.chest.view.AlertDialogService.onMyInputListener;

public class SettingDetailActivity extends BaseActivity {
	
	private final String TAG = SettingDetailActivity.class.getSimpleName();
	
	private DeviceBean dbin;
	private RelativeLayout layout_password;
	private DeviceSqlService deviceSql;
	private SetupData mSetupData;
	
	private TextView tv_lockName;
	private TextView tv_language;
	private TextView tv_lockMode;
	private TextView tv_lockCount;
	
	private String lockname="";
	private Dialog dialog;
	
	private LockApplication mApp;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_detail);
		initViews();
		initData();
		initHandler();
		actvivityname = "setting detail";
		registerReceiver(br, new IntentFilter(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED));
	};

	private void initViews() {
		initBaseViews(this);
		layout_back.setVisibility(View.VISIBLE);
		dbin=DeviceManager.getInstance().getDbin();
		if(dbin==null) {
			finish();
			return;
		}
		tv_lockName = (TextView) findViewById(R.id.textView_lockName);
		//tv_language = (TextView) findViewById(R.id.textView_language);
		tv_lockMode = (TextView) findViewById(R.id.textView_lockMode);
		tv_lockCount = (TextView) findViewById(R.id.textView_bindCount);
		layout_password = (RelativeLayout) (findViewById(R.id.layout_password));
		layout_password.setEnabled(dbin.getModeType()==DeviceBean.LockMode_password);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				dbin.writeNoresponse(CmdPackage.verifyMac(Tools.getMacAddress(SettingDetailActivity.this)));
			}
		}).start();
	}
	
	private void initData() {
		mApp  = (LockApplication) getApplication();
		if(dbin==null) {
			finish();
			return;
		}
		tv_lockName.setText(dbin.getName());
		tv_lockMode.setText(Tools.getModeName(this, dbin.getModeType()));
		layout_password.setEnabled(dbin.getModeType()==DeviceBean.LockMode_password);
		//tv_language.setText(Tools.getLanguageName(this, SetupData.getSetupData(this).readInt(AppString.language, AppConstants.language_default)));
	}

	
	void initHandler() {
		mHandler = new Handler(){
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case BroadcastString.Broad_Cmd:
					switch(msg.arg1) {
					case CmdDataParse.type_binds_remove_success:
						if(deviceSql==null) {
							deviceSql = new DeviceSqlService(SettingDetailActivity.this);
						}
						//dbin.stopConnect();
						//deviceSql.delete(dbin.getId());
						//((LockApplication)getApplication()).removeDeviceBin(dbin.getMac());
                        mApp.removeDeviceBinNotStop(dbin.getMac(), false);
						
						finish();
						break;
					case CmdDataParse.type_binds_remove_fail:
						showToast(R.string.binds_remove_fail);
						break;
					case CmdDataParse.type_binds_success:
						tv_lockCount.setText(""+dbin.getBindCount());
						break;
					case CmdDataParse.type_lock_mode:
						initData();
						break;
					case CmdDataParse.type_status:
						initData();
						break;
					case CmdDataParse.type_name:
						if(deviceSql==null) {
							deviceSql = new DeviceSqlService(SettingDetailActivity.this);
						}
						dbin.setName(lockname);
						deviceSql.insert(dbin);
						tv_lockName.setText(dbin.getName());
						setResult(Activity.RESULT_OK);
						break;
					}
					
				case handler_adapter:
					initData();
					break;
				}
			}
		};
	}
	
	public void onClick(View v) {
		Intent intent ;
		switch (v.getId()) {
		case R.id.layout_binds_remove:// 解除绑定
			dialog=AlertDialogService.getConfirmDialog(this, getString(R.string.binds_remove), getString(R.string.binds_remove_info), new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
//					if(deviceSql==null) {
//						deviceSql = new DeviceSqlService(SettingDetailActivity.this);
//						dbin.stopConnect();
//						deviceSql.delete(dbin.getId());
//						((LockApplication)getApplication()).removeDeviceBin(dbin);
//					}
					dbin.write(CmdPackage.removeBindMac(Tools.getMacAddress(SettingDetailActivity.this)));
					setResult(Activity.RESULT_OK);
				}});
			dialog.show();
			break;
		case R.id.layout_lock_name:
			dialog=AlertDialogService.getInputDialog(this, dbin.getName(), getString(R.string.name_input), new onMyInputListener() {
				
				@Override
				public void onClick(Dialog d, EditText tv) {
					// TODO Auto-generated method stub
					String name = tv.getText().toString().trim();
					Log.v(TAG,"lock Name: " + name);
					try {
						if(name.equals("")) {
							showToast(R.string.name_empty);
							return;
						} 
						if (name.contains("DP151")) {
							showToast(R.string.name_format_error);
							return;
						}
						if(name.getBytes(AppConstants.CharSet).length<=AppConstants.name_length) {
							dbin.write(CmdPackage.modifyName(name));
							d.dismiss();
							lockname = name;
						}  else {
							showToast(R.string.name_tolong);
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onCancel(Dialog d) {
					// TODO Auto-generated method stub
					
				}
			});
			dialog.show();
			break;
		case R.id.layout_lock_mode:
			dialog=AlertDialogService.getLockMode(this, dbin.getModeType(), new SelectCallback() {
				
				@Override
				public void onSelect(Dialog d,int type) {
					// TODO Auto-generated method stub
					Log.d(TAG, dbin.getModeType() + "  change lockMode type " + type);
					if(dbin.getModeType() == DeviceBean.LockMode_password) {
						if(!dbin.isOnOff()) {
							showToast(R.string.dialog_lock_password_info);
							return;
						}
					}
					if(dbin.write(CmdPackage.getLockSet(type))) {
						//dbin.setModeType(type);
						setResult(Activity.RESULT_OK);
						if(deviceSql==null) {
							deviceSql = new DeviceSqlService(SettingDetailActivity.this);
						}
						deviceSql.insert(dbin);
						initData() ;
						d.dismiss();
					}
				}
			});
			dialog.show();
			break;
		case R.id.layout_password:
			 intent = new Intent(this, SettingPasswordActivity.class);
			intent.putExtra("type", 1);//1表示修改用户密码
			startActivity(intent);
			break;
		case R.id.layout_password_pair:
			 intent = new Intent(this, SettingPasswordActivity.class);
			intent.putExtra("type", 0);//0表示修改配对密码  
			startActivity(intent);
			break;
		case R.id.layout_language:
			if(mSetupData==null) {
				mSetupData = SetupData.getSetupData(SettingDetailActivity.this);
			}
			dialog = AlertDialogService.getLanguage(this, mSetupData.readInt(AppString.language,AppConstants.language_default), new SelectCallback() {
				
				@Override
				public void onSelect(Dialog d,int type) {
					// TODO Auto-generated method stub
					Log.d(TAG, " language type " + type);
					if(mSetupData==null) {
						mSetupData = SetupData.getSetupData(SettingDetailActivity.this);
					}
					mSetupData.saveInt(AppString.language, type);
					initData() ;
					d.dismiss();
				}
			});
			dialog.show();
			break;

		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(dialog!=null) {
			if(dialog.isShowing()) {
				dialog.dismiss();
			}
		}
		unregisterReceiver(br);
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.e("tag", "setting detail onstart " + hashCode() + "  handlerA:" + mHandler.hashCode());
		
		super.onStart();
	}
	
	private BroadcastReceiver br = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			if(action.equals(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED)) {
				if (arg1.hasExtra("mac")) {
					String mac = arg1.getStringExtra("mac");
					if (mac.equals(dbin.getMac())) {
						finish();
						showToast(R.string.link_lost);
					}
				}
			}
	}};
	


}
