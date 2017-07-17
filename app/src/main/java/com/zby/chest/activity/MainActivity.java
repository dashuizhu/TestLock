package com.zby.chest.activity;

import java.util.ArrayList;
import java.util.List;

import com.zby.chest.AppString;
import com.zby.chest.R;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActivityGroup {

	public  ViewPager viewPager;
	LinearLayout layout;
	private ArrayList<View> pageViews;
	
	private TextView tv_home, tv_add, tv_setting, tv_help;
	
	private BroadcastReceiver br = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			if(action.equals(AppString.ACTION_LANGUAGE)) {
				initTextData();
			} else if(action.equals(AppString.ACTION_VIEWPAGER)) {
				int item = arg1.getIntExtra("item", -1);
				if(item>=0&& item<viewPager.getChildCount()) {
					viewPager.setCurrentItem(item);
				} else {
					viewPager.setCurrentItem(viewPager.getCurrentItem());
				}
			}
		}};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		initViewPager();
		initItem();
		layout.getChildAt(0).setSelected(true);
		lastItem = 0;
		registerBroadcast();
	}
	
	private void registerBroadcast() {
		IntentFilter filter = new  IntentFilter(AppString.ACTION_LANGUAGE);
		filter.addAction(AppString.ACTION_VIEWPAGER);
		registerReceiver(br, filter);
	}

	private void initViews() {
		layout = (LinearLayout) findViewById(R.id.linearlayout);
		viewPager = (ViewPager) findViewById(R.id.viewpaper);

		tv_home = (TextView) findViewById(R.id.textView_home);
		tv_help = (TextView) findViewById(R.id.textView_help);
		tv_add = (TextView) findViewById(R.id.textView_scan);
		tv_setting = (TextView) findViewById(R.id.textView_setting);
		
		// 构造一个新的ArrayList实例对象
		pageViews = new ArrayList<View>();
		/**
		 * 开始一个新的活动中运行的组织。 每一个活动你开始必须有一个独一无二的字符串标识与其相关联
		 */
		View view1 = getLocalActivityManager().startActivity("0",
				new Intent(this, HomeActivity.class)).getDecorView();
		View view2 = getLocalActivityManager().startActivity("1",
				new Intent(this, SettingActivity.class)).getDecorView();
		View view3 = getLocalActivityManager().startActivity("2",
				new Intent(this, ScanActivity.class)).getDecorView();
		View view4 = getLocalActivityManager().startActivity("3",
				new Intent(this, HelpActivity.class)).getDecorView();
		


		// 添加指定的对象在文章末尾的ArrayList。
		pageViews.add(view1);
		pageViews.add(view2);
		pageViews.add(view3);
		pageViews.add(view4);
	}
	
	
	private void initTextData() {
		tv_home.setText(R.string.home);
		tv_help.setText(R.string.help);
		tv_setting.setText(R.string.setting);
		tv_add.setText(R.string.add);
	}

	private void initViewPager() {
		viewPager.setAdapter(new PagerAdapter() {
			// 得到数目
			public int getCount() {
				return pageViews.size();
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}

			@Override
			public int getItemPosition(Object object) {
				// TODO Auto-generated method stub
				return super.getItemPosition(object);
			}

			@Override
			public void destroyItem(View view, int id, Object arg2) {
				// TODO Auto-generated method stub
				((ViewPager) view).removeView(pageViews.get(id));
			}

			// 获取每一个item的id
			@Override
			public Object instantiateItem(View view, int id) {
				((ViewPager) view).addView(pageViews.get(id));
				return pageViews.get(id);
			}

		});
		// 页面改变时候的监听事件
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				selectItem(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// int width = (int)(mScreenWidth * arg1)/modules.length +
				// arg0*mScreenWidth/modules.length;
				// mIndicator.setPadding(width, 0, 0, 0);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	
	private int lastItem = -1;

	private void selectItem(int item) {
		layout.getChildAt(lastItem).setSelected(false);
		layout.getChildAt(item).setSelected(true);
		
		BaseActivity ba =  (BaseActivity) (getLocalActivityManager().getActivity(""+lastItem));
		if(ba!=null) {
				ba.onStop();
		}
		lastItem=item;
		BaseActivity baNow =  (BaseActivity) (getLocalActivityManager().getActivity(""+item));
		if(baNow!=null) {
			baNow.onStart();
		}
	}
	
	
	
//	private void skipActivity(int item) {
//		Intent intent = new Intent();
//		switch(item) {
//		case 0:
//			intent.setClass(this, HomeActivity.class);
//			break;
//		case 1:
//			intent.setClass(this, ScanActivity.class);
//			break;
//		case 2:
//			intent.setClass(this, SettingActivity.class);
//			break;
//		case 3:
//			intent.setClass(this, HelpActivity.class);
//			break;
//		}
//		startActivity(intent);
//	}

	private void initItem() {
		for (int i = 0; i < layout.getChildCount(); i++) {
			layout.getChildAt(i).setSelected(false);
			final int item = i;
			layout.getChildAt(i).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					viewPager.setCurrentItem(item);
//					skipActivity(item);
//					selectItem(item);
				}
			});
			if(i>0) {//初次进来，把其他几个界面 onstop掉
				BaseActivity baNow =  (BaseActivity) (getLocalActivityManager().getActivity(""+i));
				if(baNow!=null) {
					baNow.onStop();
				}
			} else {
				BaseActivity baNow =  (BaseActivity) (getLocalActivityManager().getActivity(""+i));
				if(baNow!=null) {
					baNow.onStart();
				}
			}
		}
	}
	
	protected void onStart() {
		Log.w("tag", "main activity onStart()");
		super.onStart();
	};
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(br);
		super.onDestroy();
	}
}
