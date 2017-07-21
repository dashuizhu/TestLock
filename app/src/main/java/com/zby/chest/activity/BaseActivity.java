package com.zby.chest.activity;


import android.view.MotionEvent;
import com.zby.chest.AppString;
import com.zby.chest.LockApplication;
import com.zby.chest.R;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.ConnectBroadcastReceiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.locks.Lock;

public abstract class BaseActivity extends Activity {
	
	protected float phone_density;//屏幕密度
	protected int phone_width, phone_height;//屏幕宽高
	
	
	 TextView tv_title;//标题
	 LinearLayout layout_back;//返回layout
	 LinearLayout layout_menu;//右上角按钮layout
	 TextView tv_back , tv_menu;//标题栏中 左边的图片 和 右边的图片
	 
	 private Toast mToast;
	 
	 private boolean  isRegisterConnect = false;
	 
	final static int handler_adapter = 888;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//Log.e("tag", " init  home oncraete() ");
		
		//屏幕宽度
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		phone_width = wm.getDefaultDisplay().getWidth();// 屏幕宽度
		phone_height  = wm.getDefaultDisplay().getHeight();// 屏幕宽度
		phone_density =  getResources().getDisplayMetrics().density; //屏幕密度
	}
	
	protected void initBaseViews(Activity v) {
		//View v = LayoutInflater.from(this).inflate(R.layout.fragment_title, null);
		tv_title = (TextView) v.findViewById(R.id.textView_title);
		layout_back = (LinearLayout) v.findViewById(R.id.layout_back);
		layout_menu = (LinearLayout) v.findViewById(R.id.layout_menu);
		tv_back = (TextView) v.findViewById(R.id.textView_back);
		tv_menu = (TextView) v.findViewById(R.id.textView_menu);
		
		
		
		layout_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	/**
	 * 因为ViewPager中子activity启动， onactivityResult是直接返回到ViewPagerActivity中，这里模拟
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void handleActivityResult(int requestCode, int resultCode ,Intent data) {
		onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 显示toast
	 * @param str
	 */
	protected void showToast(String str) {
		if(mToast ==null) {
			mToast = Toast.makeText(this, str, Toast.LENGTH_LONG);
		}
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.setText(str);
		mToast.show();
	}
	
	/**
	 * 显示toast
	 * @param str
	 */
	protected void showToast(int str) {
		if(mToast ==null) {
			mToast = Toast.makeText(this, str, Toast.LENGTH_LONG);
		}
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.setText(str);
		mToast.show();
	}
	
	
	public void btn_back(View v) {
		finish();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		if(!isRegisterConnect) {
			isRegisterConnect = true;
			registerCmdBroad();
		}
		if(mHandler==null) {
			Intent intent = new Intent(AppString.ACTION_VIEWPAGER);
			intent.putExtra("item", -1);
			sendBroadcast(intent);
		} else {
			((LockApplication) getApplication()).setHandler(mHandler);
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.e("tag", hashCode()+" init  baseactivity  onstop ");
		if(isRegisterConnect) {
			isRegisterConnect = false;
			unregisterCmdBroad();
		}
		//((LockApplication)getApplication()).setHandler(mHandler);
		super.onStop();
	}

	//protected void onMyStart() {
	//	// TODO Auto-generated method stub
	//	if(!isRegisterConnect) {
	//		isRegisterConnect = true;
	//		registerCmdBroad();
	//	}
	//	if(mHandler==null) {
	//		Intent intent = new Intent(AppString.ACTION_VIEWPAGER);
	//		intent.putExtra("item", -1);
	//		sendBroadcast(intent);
	//	} else {
	//		((LockApplication) getApplication()).setHandler(mHandler);
	//	}
	//}
    //
	//protected void onMyStop() {
	//	// TODO Auto-generated method stub
	//	Log.e("tag", hashCode()+" init  baseactivity  onstop ");
	//	if(isRegisterConnect) {
	//		isRegisterConnect = false;
	//		unregisterCmdBroad();
	//	}
	//	//((LockApplication)getApplication()).setHandler(mHandler);
	//}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method s
		unregisterCmdBroad();
		Log.e("tag", hashCode()+" init  baseactivity  ondestory");
		super.onDestroy();
	}

	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	protected  Handler mHandler;
	
	private synchronized void registerCmdBroad() {
//		if(receiver==null) {
//			receiver = new ConnectBroadcastReceiver(this, mHandler);
//			registerReceiver(receiver, new IntentFilter(BroadcastString.BROADCAST_ACTION));
//		}
	}
	
//	public void onRegister() {
//		Log.e("tag", hashCode()+"start init"+mHandler);
//		if(mHandler==null) {
//			initHandler();
//		}
//		Log.e("tag", hashCode()+"start init2"+mHandler);
//		((LockApplication)getApplication()).setHandler(mHandler);
////		if(receiver==null) {
////			receiver = new ConnectBroadcastReceiver(this, mHandler);
////			registerReceiver(receiver, new IntentFilter(BroadcastString.BROADCAST_ACTION));
////		
////		} else {
////			receiver.setHandler(mHandler);
////		}
//	}
	
	void initHandler(){};
	
	private synchronized void unregisterCmdBroad() {
//		if(receiver!=null) {
//			unregisterReceiver(receiver);
//			receiver = null;
//		}
	}

	@Override public boolean dispatchTouchEvent(MotionEvent ev) {
		//有操作，就更新操作时间
		LockApplication.mLastOptionTime = System.currentTimeMillis();
		return super.dispatchTouchEvent(ev);
	}
}
