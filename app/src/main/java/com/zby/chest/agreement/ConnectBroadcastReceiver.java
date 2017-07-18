package com.zby.chest.agreement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * <p>Description: 广播传递通讯消息 </p>
 * @author zhujiang
 * @date 2014-5-30
 */
public	class ConnectBroadcastReceiver extends BroadcastReceiver {
	
	private static String TAG = "ConnectBroadcast";
	
	
		private Handler mHandler;
		
		
		public ConnectBroadcastReceiver(Handler handler) {
			this.mHandler = handler;
//			this.mActivity = activity;
		}
	
		@Override
		public void onReceive(Context context, Intent intent) {
			int type=  intent.getIntExtra(BroadcastString.BROADCAST_DATA_TYPE, -1);
			int i = intent.getIntExtra(BroadcastString.BROADCAST_DATA_KEY, -1);
			Log.e(TAG,  " initbroad收到 type ="+type + "  " + i);
			if(mHandler!=null) {
				Message msg = mHandler.obtainMessage();
				msg.what = BroadcastString.Broad_Cmd;
				msg.arg1 = type;
				msg.arg2 = i;
				String mac = intent.getStringExtra(BroadcastString.BROADCAST_DEVICE_MAC);
				if(intent.hasExtra("nameMac")) {
					msg.obj = intent.getStringArrayExtra("nameMac");
				} else {
					if(mac!=null) {
						msg.obj = mac;
					}
				}
				mHandler.sendMessage(msg);
			} else {
				Log.e(TAG, hashCode()+"handler ==null , " + type + " " + i);
//				mActivity.onRegister();
			}
		}
		
		public void setHandler(Handler mHandler) {
			this.mHandler = mHandler;
		}
		
		
//		public static void handlerSendBroadcast(Activity activity,int type) {
//			Intent intent = new Intent (ConnectBroadcastReceiver.BROADCAST_ACTION);
//			intent.putExtra(ConnectBroadcastReceiver.BROADCAST_DATA_TYPE, type);
////			intent.putExtra(ConnectBroadcastReceiver.BROADCAST_DATA_KEY, data);
////			Log.d("tag", "MainActivity.sendBroadcast    " + type + " " + data);
//			activity.sendBroadcast(intent);
//		}
	}