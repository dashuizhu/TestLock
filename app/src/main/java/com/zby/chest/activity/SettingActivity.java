package com.zby.chest.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.zby.chest.AppString;
import com.zby.chest.DeviceManager;
import com.zby.chest.LockApplication;
import com.zby.chest.R;
import com.zby.chest.adapter.DeviceAdapter;
import com.zby.chest.adapter.DeviceSettingAdapter;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.agreement.ConnectBroadcastReceiver;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.view.AlertDialogService;
import com.zby.chest.view.MyListView;

public class SettingActivity extends BaseActivity{
	
	private List<DeviceBean> list;
	private ListView listView;
	private DeviceSettingAdapter adapter;
	private LockApplication app;

	private final int activity_detail = 11;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initViews();
		initHandle();
		actvivityname = "setting";
		if (br==null) {
			br = new BroadcastReceiver(){

				@Override
				public void onReceive(Context arg0, Intent arg1) {
					// TODO Auto-generated method stub
					String action = arg1.getAction();
					if(action.equals(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED)) {
						list = app.getDeviceListLinked2();
						adapter.setList(list);
						adapter.notifyDataSetChanged();
					}
			}};
			IntentFilter filter = new IntentFilter(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED);
			filter.addAction(BluetoothLeServiceMulp.ACTION_GATT_CONNECTED);
			registerReceiver(br, filter);
		}
	};

	private void initViews() {
			listView = (ListView) findViewById(R.id.listView);
			// TEST/
			app = (LockApplication) getApplication();
			list = app.getDeviceListLinked();
			adapter = new DeviceSettingAdapter(this, list);
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					DeviceBean dbin = list.get(arg2);
					if(dbin.isLinkSuccess()) {
						Intent intent = new Intent(SettingActivity.this, SettingDetailActivity.class);
						startActivityForResult(intent, activity_detail);
						DeviceManager.getInstance().setDbin(dbin);
					} else if (dbin.isLink()) {
						dbin.write(CmdPackage.getStatus());
					}else {
						showToast(R.string.unlink);
					}
				}
			});
	}
	
	private void initHandle() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case BroadcastString.Broad_Cmd:
					switch(msg.arg1) {
					case CmdDataParse.type_lock_mode:
						adapter.notifyDataSetChanged();
						break;
					case CmdDataParse.type_status:
						list = app.getDeviceListLinked();
						adapter.setList(list);
						adapter.notifyDataSetChanged();
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
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case activity_detail:
			if(resultCode == Activity.RESULT_OK) {
				onStart();
			}else {
				super.onStart();
			}
			break;
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		list = app.getDeviceListLinked();
		adapter.setList(list);
		adapter.notifyDataSetChanged();
		
		super.onStart();
	}
	
	private BroadcastReceiver br;
	
	@Override
	protected void onStop() {
		super.onStop();
	};


	@Override
	void initHandler() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(br!=null) {
			unregisterReceiver(br);
			br = null;
		}
		super.onDestroy();
	}
	
}
