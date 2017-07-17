package com.zby.chest.activity;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.zby.chest.AppConstants;
import com.zby.chest.AppString;
import com.zby.chest.R;
import com.zby.chest.utils.SetupData;
import com.zby.chest.utils.Tools;
import com.zby.chest.view.AlertDialogService;
import com.zby.chest.view.AlertDialogService.SelectCallback;

public class HelpActivity extends BaseActivity {

	private final String TAG = HelpActivity.class.getSimpleName();

	private SetupData mSetupData;

	private TextView tv_language;
	private TextView tv_lawinfo, tv_lockinfo, tv_appinfo, tv_versioninfo, tv_versionName,
			tv_languageChange, tv_installInfo, tv_exit, tv_closeBT, tv_share;
	private CheckBox cb_bt;
	
	private int languageType;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		initViews();
		//initTextData();
	};

	private void initViews() {
		tv_language = (TextView) findViewById(R.id.textView_language);
		tv_languageChange = (TextView) findViewById(R.id.textView_languageChange);
		tv_lawinfo = (TextView) findViewById(R.id.textView_lawinfo);
		tv_lockinfo = (TextView) findViewById(R.id.textView_lock_info);
		// tv_appinfo = (TextView) findViewById(R.id.textView_appinfo);
		tv_versioninfo = (TextView) findViewById(R.id.textView_versioninfo);
		tv_installInfo = (TextView) findViewById(R.id.textView_installinfo);
		tv_versionName = (TextView) findViewById(R.id.textView_versionName);
		cb_bt = (CheckBox) findViewById(R.id.checkBox_closeBT);
		tv_exit = (TextView) findViewById(R.id.textView_exit);
		tv_closeBT = (TextView) findViewById(R.id.textView_exit_choise);
		tv_share = (TextView) findViewById(R.id.textView_qrcode);
		if (mSetupData == null) {
			mSetupData = SetupData.getSetupData(HelpActivity.this);
		}
		languageType = SetupData.getSetupData(this).readInt(AppString.language,
				AppConstants.language_default);
		Log.e("tag", " language type " + languageType);
		boolean readb =mSetupData.readBoolean(AppString.exit_closeBT, true);
		cb_bt.setChecked(readb);
		cb_bt.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				mSetupData.saveboolean(AppString.exit_closeBT, arg1);
			}
		});
		
		tv_versionName.setText("V"+Tools.getVersionName(this));
	}

	private void initTextData() {
		tv_language.setText(R.string.language_default);
		tv_languageChange.setText(R.string.language_change);
		tv_lawinfo.setText(R.string.law_info);
		tv_lockinfo.setText(R.string.lock_info);
		// tv_appinfo.setText(R.string.app_info);
		tv_versioninfo.setText(R.string.version_info);
		tv_installInfo.setText(R.string.installinfo);
		tv_exit.setText(R.string.exit);
		tv_closeBT.setText(R.string.close_bt);
		tv_share.setText(R.string.qrcode);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		System.out.println(event.getX() + " " + event.getAction());
		return true;
	}

	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.layout_language:
			AlertDialogService.getLanguage(
					this,
					languageType,
					new SelectCallback() {

						@Override
						public void onSelect(Dialog d, int type) {
							// TODO Auto-generated method stub
							Log.d(TAG, " language type " + type);
							if (mSetupData == null) {
								mSetupData = SetupData
										.getSetupData(HelpActivity.this);
							}
							languageType= type;
							mSetupData.saveInt(AppString.language, type);
							Tools.switchLanguage(HelpActivity.this, type);
							Intent intent = new Intent(AppString.ACTION_LANGUAGE);
							sendBroadcast(intent);
							initTextData();
							d.dismiss();
						}
					}).show();
			break;
		case R.id.layout_exit:
			if (mSetupData == null) {
				mSetupData = SetupData.getSetupData(HelpActivity.this);
			}
			if (mSetupData.readBoolean(AppString.exit_closeBT)) {
				BluetoothManager mBluetoothManager = null;
				BluetoothAdapter mBluetoothAdapter;
				if (mBluetoothManager == null) {
					mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
					if (mBluetoothManager == null) {
						Log.e(TAG, "Unable to initialize BluetoothManager.");
					}
				}

				mBluetoothAdapter = mBluetoothManager.getAdapter();
				if (mBluetoothAdapter == null) {
					Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
				}
				mBluetoothAdapter.disable();
			}
			finish();
			System.exit(0);
			break;
		case R.id.layout_qrcode:
			intent = new Intent(this, HelpQrcodeActivity.class);
			startActivity(intent);
			break;
		case R.id.layout_installinfo:
			intent = new Intent(this, HelpShowimgActivity.class);
				intent.putExtra("languageType", languageType);
				intent.putExtra("isInstall", true);
			startActivity(intent);
			break;
		case R.id.layout_lawinfo:
			intent = new Intent(this, HelpLawInfoActivity.class);
			startActivity(intent);
			break;
		case R.id.layout_lockinfo:
			intent = new Intent(this, HelpShowimgActivity.class);
				intent.putExtra("languageType", languageType);
				intent.putExtra("isInstall", false);
			startActivity(intent);
			break;
		}
	}

	@Override
	void initHandler() {
		// TODO Auto-generated method stub
		
	}
}
