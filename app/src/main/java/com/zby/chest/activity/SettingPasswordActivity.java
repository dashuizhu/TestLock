package com.zby.chest.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.zby.chest.AppString;
import com.zby.chest.DeviceManager;
import com.zby.chest.LockApplication;
import com.zby.chest.R;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceSqlService;
import com.zby.chest.utils.SetupData;

/**
 * @author Administrator
 *
 */
public class SettingPasswordActivity extends BaseActivity {

	public final static int TYPE_PASSWORD_ADMIN = 2;//管理员密码
	public final static int TYPE_PASSWORD = 1;//开锁密码
	public final static int TYPE_PASSWORD_PAIR = 0;//配对密码

	private DeviceBean dbin;
	private EditText et_old, et_new , et_confirm;
	private TextView tv_name, tv_small_title;
	private int type;
	private DeviceSqlService devSql;
	//showback  false 表示第一次绑定，修改配对密码
	private boolean showBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		setContentView(R.layout.activity_setting_password);
		initViews();
		initHandler();
	}
	
	public void initViews() {
		initBaseViews(this);
		layout_back.setVisibility(View.VISIBLE);
		dbin = DeviceManager.getInstance().getDbin();
		et_old = (EditText) findViewById(R.id.editText_old);
		et_new = (EditText) findViewById(R.id.editText_new);
		et_confirm = (EditText) findViewById(R.id.editText_confirm);
		tv_name = (TextView) findViewById(R.id.textView_name);
		tv_small_title = (TextView) findViewById(R.id.textView_title_small);
		tv_name.setText(dbin.getName());
		type = getIntent().getIntExtra("type", 0);
		//showback 表示第一次绑定，修改配对密码
		showBack = getIntent().getBooleanExtra("showBack", true);
		if (!showBack) {
			tv_back.setVisibility(View.GONE);
		}
		if (type == TYPE_PASSWORD_PAIR) {//修改配对密码
			tv_small_title.setText(R.string.password_pair_modify);
			et_old.setVisibility(View.GONE);
			et_new.requestFocus();
		} else if (type == TYPE_PASSWORD) {//修改控制密码
			tv_small_title.setText(R.string.password_user_modify);
			et_old.requestFocus();
		} else if (type == TYPE_PASSWORD_ADMIN) { //管理员密码
			tv_small_title.setText(R.string.password_admin_modify);
			et_old.requestFocus();
		}
	}
	
	void initHandler() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case BroadcastString.Broad_Cmd:
					switch(msg.arg1)  {
					case CmdDataParse.type_password_unlock_success:
						showToast(R.string.password_modify_success);
						setResult(Activity.RESULT_OK);
						finish();
						break;
					case CmdDataParse.type_password_error:
						showToast(R.string.password_error);
						et_old.setText("");
						et_old.requestFocus();
						break;
					case CmdDataParse.type_password_modify_success:
						showToast(R.string.password_modify_verify_success);
						setResult(Activity.RESULT_OK);
						finish();
						break;
					case CmdDataParse.type_password_admin_modify_fail:
						showToast(R.string.password_error);
						et_old.setText("");
						et_old.requestFocus();
						break;
					case CmdDataParse.type_password_admin_modify_success:
						showToast(R.string.password_modify_admin_success);
						String newPsd = et_new.getText().toString().trim();
						SetupData.getSetupData(SettingPasswordActivity.this).save(AppString.KEY_ADMIN_PASSWORD+DeviceManager.getInstance().getDbin().getMac(), newPsd);
						setResult(Activity.RESULT_OK);
						finish();
						break;

					}
					break;
				}
			}
		};
	}
	
	public void onClick(View v) {
		String old = et_old.getText().toString().trim();
		String newPsd = et_new.getText().toString().trim();
		String conPsd = et_confirm.getText().toString().trim();
		if(type==1 && old.length()!=6) {//配对密码不需要旧密码
			showToast(R.string.password_input);
			et_old.requestFocus();
			return;
		}
		if(newPsd.length()!=6) {
			showToast(R.string.password_input);
			et_new.requestFocus();
			return;
		}
		if(conPsd.length()!=6) {
			showToast(R.string.password_input);
			et_confirm.requestFocus();
			return;
		}
		//只有管理员密码 ，才需要判断为非0
		if(type == TYPE_PASSWORD_PAIR && newPsd.equals("000000")) {
			showToast(R.string.password_input2);
			et_new.setText("");
			et_new.requestFocus();
			return;
		}
		if(!newPsd.equals(conPsd)) {
				showToast(R.string.password_unlike);
				et_old.requestFocus();
				return;
		}
		if(dbin.isLink()) {
			switch (type) {
				case TYPE_PASSWORD_PAIR://修改配对密码
					dbin.write(CmdPackage.modifyVerifyPassword9(newPsd));
					dbin.setPairPassword(newPsd);
					if(devSql==null) {
						devSql = new DeviceSqlService(this);
					}
					devSql.insert(dbin);
					break;
				case TYPE_PASSWORD://修改用户滑动开锁密码
					dbin.write(CmdPackage.getPasswordChange(old, newPsd));
					break;
				case TYPE_PASSWORD_ADMIN:
					dbin.write(CmdPackage.getPasswordAdminChange(old, newPsd));
					break;
			}
		} else {
			showToast(R.string.unlink);
		}
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
						if (!showBack) {
							((LockApplication) getApplication()).removeDeviceBinNotStopBandingFail(mac, false);
						}
						finish();
						showToast(R.string.link_lost);
					}
				}
			}
	}};
	
	@Override
	protected void onStart() {
		registerReceiver(br, new IntentFilter(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED));
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		unregisterReceiver(br);
		super.onStop();
	}
}
