package com.zby.chest.activity;

import com.zby.chest.R;
import com.zby.chest.utils.ScreenUtils;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class HelpQrcodeActivity extends BaseActivity {
	
	private ImageView iv_qrcode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_qrcode);
		initBaseViews(this);
		layout_back.setVisibility(View.VISIBLE);
		
		iv_qrcode = (ImageView) findViewById(R.id.imageView_qrcode);
		LayoutParams params = iv_qrcode.getLayoutParams();
		params.width = (ScreenUtils.getPhoneWidth(this)) /2;
		params.height = params.width;
		iv_qrcode.setLayoutParams(params);
	}

	@Override
	void initHandler() {
		// TODO Auto-generated method stub
		
	}

}
