package com.zby.chest.adapter;

import java.util.List;

import com.zby.chest.R;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceListener;
import com.zby.chest.utils.ScreenUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceAdapter extends BaseAdapter {
	
	private String TAG = DeviceAdapter.class.getSimpleName();
	
	private List<DeviceBean> list;
	private Context mContext;
	private boolean showDislink = true;
	
	public DeviceAdapter(Context mContext, List<DeviceBean> list ) {
		this.mContext = mContext;
		this.list = list;
		this.showDislink = true;
	}
	public DeviceAdapter(Context mContext, List<DeviceBean> list, boolean showDislink ) {
		this.mContext = mContext;
		this.list = list;
		this.showDislink = showDislink;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		DeviceBean bin = list.get(arg0);
//		if(arg1 == null) {
//			LayoutInflater inflater = LayoutInflater.from(mContext);
//			arg1 = inflater.inflate(com.zby.lock.R.layout.device_list_item, null);
//			mHolder = new Holder();
//			mHolder.tv_lock = (TextView) arg1.findViewById(R.id.textView_lock);
//			mHolder.tv_switch = (TextView) arg1.findViewById(R.id.textView_switch);
//			arg1.setTag(mHolder);
//		} else {
//			mHolder = (Holder) arg1.getTag();
//		}
//		mHolder.tv_lock.setText(bin.getName());
//		Drawable drawable = null;
//		int switchRes = 0;
//		switch(bin.getModeType()) {
//		case DeviceBin.LockMode_auto:
//			drawable = mContext.getResources().getDrawable(R.drawable.btn_lock_auto);
//			switchRes = R.drawable.cb_lock_switch_auto;
//			break;
//		case DeviceBin.LockMode_onekey:
//			drawable = mContext.getResources().getDrawable(R.drawable.btn_lock_password);
//			switchRes = R.drawable.cb_lock_switch_password;
//			break;
//		case DeviceBin.LockMode_scroll:
//			drawable = mContext.getResources().getDrawable(R.drawable.btn_lock_scroll);
//			switchRes = R.drawable.cb_lock_switch_scroll;
//			break;
//		}
//		drawable.setBounds(0, 0, ScreenUtils.dp2sp(mContext,50), ScreenUtils.dp2sp(mContext, 50));
//		mHolder.tv_switch.setBackgroundResource(switchRes);
//		mHolder.tv_lock.setCompoundDrawables(drawable, null,  null, null);
//		mHolder.tv_lock.setSelected(bin.isOnOff());
//		mHolder.tv_switch.setSelected(bin.isOnOff());
//		return arg1;
		//Log.d(TAG, " " + bin.getName() + " " + bin.getModeType());
		if(deviceListener!=null) {
			bin.setDeviceListener(deviceListener);
		}
//		bin.setDeviceListener(new DeviceListener() {
//			
//			@Override
//			public void onDeviceScroll(DeviceBean dbin) {
//				// TODO Auto-generated method stub
//				if(!dbin.isOnOff()) {
//					dbin.write(CmdPackage.getLockOff(DeviceBean.LockMode_scroll));
//				}
//				Log.d(TAG, "deviceListener.onScroll " + dbin.getName() + " " + dbin.getModeType());
//			}
//			
//			@Override
//			public void onDeviceItemClick(DeviceBean dbin) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		return bin.getView(mContext, this);
	}
	
	
	
	public void setList(List<DeviceBean> list) {
		this.list = list;
	}
	
//	GestureDetector mGestureDetector  = new GestureDetector(new OnGestureListener() {
//		
//		@Override
//		public boolean onSingleTapUp(MotionEvent arg0) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//		
//		@Override
//		public void onShowPress(MotionEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//		@Override
//		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
//				float arg3) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//		
//		@Override
//		public void onLongPress(MotionEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//		@Override
//		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//				float velocityY) {
//			// TODO Auto-generated method stub
//			 if (e1.getX()-e2.getX() > FLING_MIN_DISTANCE    
//	                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {    
//	                // Fling left    
//	                Log.d("TAG",  "左边手势");
//	            } else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE    
//	                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {    
//	                // Fling right    
//	            	Log.d("TAG", "右边边手势");
//	            	list.get(arg0).enableOnoff();
//	            	notifyDataSetChanged();
//	            }    
//			return false;
//		}
//		
//		@Override
//		public boolean onDown(MotionEvent arg0) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//	}); ;
	
	Holder mHolder;
	private  class Holder  {
		private TextView tv_lock;
		private TextView tv_switch;
	}
	
	private DeviceListener deviceListener;
	public void setDeviceListener(DeviceListener deviceListener) {
		// TODO Auto-generated method stub
		this.deviceListener = deviceListener;
	}

}
