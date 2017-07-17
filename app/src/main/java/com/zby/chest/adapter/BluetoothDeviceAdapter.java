package com.zby.chest.adapter;

import java.util.List;

import com.zby.chest.R;
import com.zby.chest.model.DeviceBlueBean;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BluetoothDeviceAdapter extends BaseAdapter {
	
	private List<DeviceBlueBean> list;
	private Context mContext;

	public BluetoothDeviceAdapter(Context scanActivity,
			List<DeviceBlueBean> list2) {
		this.mContext = scanActivity;
		this.list = list2;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if(arg1==null) {
			LayoutInflater inlater = LayoutInflater.from(mContext);
			arg1 = inlater.inflate(R.layout.add_list_item, null);
			mHolder = new Holder();
			mHolder.btn_add = (ImageView) arg1.findViewById(R.id.button_add);
			mHolder.tv_mac = (TextView) arg1.findViewById(R.id.textView_mac);
			mHolder.tv_name = (TextView) arg1.findViewById(R.id.textView_name);
			arg1.setTag(mHolder);
		} else {
			mHolder = (Holder) arg1.getTag();
		}
		//final BluetoothDevice bd = list.get(arg0);
		//mHolder.tv_name.setText(bd.getName()==null?"":bd.getName());
		final DeviceBlueBean deviceBlueBin = list.get(arg0);
			mHolder.tv_name.setText(deviceBlueBin.getName());
		mHolder.tv_mac.setText(deviceBlueBin.getAddress());
//		mHolder.btn_add.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				if(listener!=null) {
//					listener.onItemClick(deviceBlueBin.getAddress(), deviceBlueBin.getName(), deviceBlueBin.getType());
//				}
//			}
//		});
		return arg1;
	}
	
	private Holder mHolder;
	private class Holder {
		private TextView tv_name;
		private TextView tv_mac;
		private ImageView btn_add;
	}
	
	
	private MyClickListener listener;
	public void setMyClickListener(MyClickListener listener) {
		this.listener = listener;
	}
	
	public interface MyClickListener {
		public void onItemClick(String address,String name, int type);
	}

}
