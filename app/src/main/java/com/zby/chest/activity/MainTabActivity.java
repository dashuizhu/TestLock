package com.zby.chest.activity;

import com.zby.chest.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

/**
 * @author Administrator
 * 但是在子activity 跳转无法做 onstart处理
 */
public class MainTabActivity extends TabActivity {

	private TabHost tabHost;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_host);
		initTab();
	}

	private void initTab() {
		// 得到他tabhost对象,对TabActivity的 操作通常都有这个这个对象完成
		TabHost tabHost = getTabHost();
		Resources res = getResources();
		tabHost.setup();
	          


		// 生成一个Intent对象，该对象指向另一个加油Activity
		Intent sessionIntent = new Intent();
		sessionIntent.setClass(this, HomeActivity.class);
		// 生成一个TabSpec对象，代表了一个页
		TabHost.TabSpec refuelSpc = tabHost.newTabSpec("tab1");
		// 设置该页的Indiacator
		refuelSpc.setIndicator(getTabView(getString(R.string.home),
				res.getDrawable(R.drawable.btn_home)));
		refuelSpc.setContent(sessionIntent);
		// 将设置好的TabSpec对象添加到TabHost中
		tabHost.addTab(refuelSpc);

		Intent buddylIntent = new Intent(this, ScanActivity.class);
		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator(
						getTabView(getString(R.string.add),
								res.getDrawable(R.drawable.btn_scan)))
				.setContent(buddylIntent));

		Intent taskIntent = new Intent(this, SettingActivity.class);
		tabHost.addTab(tabHost
				.newTabSpec("tab3")
				.setIndicator(
						getTabView(getString(R.string.setting),
								res.getDrawable(R.drawable.btn_setting)))
				.setContent(taskIntent));

		Intent eventIntent = new Intent(this, HelpActivity.class);
		tabHost.addTab(tabHost
				.newTabSpec("tab4")
				.setIndicator(
						getTabView(getString(R.string.help),
								res.getDrawable(R.drawable.btn_help)))
				.setContent(eventIntent));
//		
//		
		 tabHost.setOnTabChangedListener(new OnTabChangeListener(){    
			            @Override  
			             public void onTabChanged(String tabId) {  
			                 if (tabId.equals("tab1")) {   //第一个标签  
			                 }else if (tabId.equals("tab2")) {   //第二个标签  
			                 }else if (tabId.equals("tab3")) {   //第三个标签  
			                 } else if(tabId.equals("tab4")) {
			                 }
			             }              
			         });   


	}

//	private View getTabView(String text, Drawable drawable) {
//		View view1 = LayoutInflater.from(this)
//				.inflate(R.layout.layout_tab, null);
//		TextView tv1 = (TextView) view1.findViewById(R.id.textView_tab);
//		tv1.setText(text);
//		tv1.setCompoundDrawables(null, drawable, null, null);
//		return view1;
//	}
	
	private View getTabView(String text, Drawable drawable){
		View view1 =  LayoutInflater.from(this).inflate(R.layout.tab_style, null);
		TextView tv1 = (TextView) view1.findViewById(R.id.tab_name);
		tv1.setText(text);
		ImageView iv1 = (ImageView) view1.findViewById(R.id.tab_image);
		iv1.setBackgroundDrawable(drawable);
		return view1;
	}

}
