
package com.zby.chest.activity;

import com.zby.chest.AppConstants;
import com.zby.chest.AppString;
import com.zby.chest.R;
import com.zby.chest.utils.MyLog;
import com.zby.chest.utils.SetupData;
import com.zby.chest.utils.Tools;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class LogoActivity extends Activity {

    /**
     * 读取本地记录
     */
    private SetupData mSetupData;

    // handler
    private static final int handler_ap_linking = 10;
    private static final int handler_ap_scan = 11;
    private static final int handler_ap_notfound = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_logo);
        mSetupData = SetupData.getSetupData(this);
        guideViewActivity();
        // 屏幕宽度
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
//        MyLog.d("asdfasdf", "nnnnnnnnnasdfsafd");
//        Integer.parseInt("asdf");
//        finish();
        Tools.getMacAddress(this);
        if(mSetupData.readBoolean(AppString.firstLauncher, true)) {
        	mSetupData.saveboolean(AppString.firstLauncher, false);
        	int  type = Tools.getLocalLanguage(this);
        	Log.e("tag", " language type save " + type);
        	SetupData.getSetupData(this).saveInt(AppString.language,
        			type);
        	Tools.switchLanguage(this, type);
        } else {
        	int type = mSetupData.readInt(AppString.language, AppConstants.language_default);
        	Tools.switchLanguage(this, type);
        }

    }

    public Handler handler = new Handler() {
        // public void handleMessage(Message msg) {
        // switch(msg.what) {
        // case ConnectionInterface.LinkSuccess:
        // //在这个界面收到 ，连接成功， 那就只有 在 AP直连Mai下 ，连接成功
        // finish();
        // break;
        // case handler_ap_linking:
        // ToastNew.makeText(LoginActivity.this, R.string.ap_linking, 3).show();
        // break;
        // case handler_ap_scan:
        // ToastNew.makeText(LoginActivity.this, R.string.ap_scan, 3).show();
        // break;
        // case handler_ap_notfound:
        // ToastNew.makeText(LoginActivity.this,
        // getString(R.string.ap_notfind)+AppConstants.AP_SSID, 3).show();
        // break;
        // }
        // }
    };

    private void guideViewActivity() {
        BluetoothManager mBluetoothManager;
        BluetoothAdapter mBluetoothAdapter;
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if(mBluetoothAdapter != null) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
            }
        }

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent intent = new Intent(LogoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public void onBackPressed() {
    }
}
