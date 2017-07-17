package com.zby.chest.activity;

import com.zby.chest.AppConstants;
import com.zby.chest.R;
import com.zby.chest.utils.MyImage.ScalingLogic;
import com.zby.chest.utils.MyImage;
import com.zby.chest.utils.ScreenUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HelpShowimgActivity extends BaseActivity {
	
	private ImageView iv_qrcode, iv_qrcode2, iv_qrcode3;
	private Bitmap bmp, bmp2, bmp3;
	
	private final int show_img = 11;
	
	private LinearLayout layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_showimg);
		initBaseViews(this);
		layout_back.setVisibility(View.VISIBLE);
		//iv_qrcode = (ImageView) findViewById(R.id.imageView_qrcode);
		layout = (LinearLayout) findViewById(R.id.layout_img);
//		iv_qrcode2 = (ImageView) findViewById(R.id.imageView_qrcode2);
//		iv_qrcode3 = (ImageView) findViewById(R.id.imageView_qrcode3);
//		
//		iv_qrcode.setBackgroundDrawable(com.zby.chest.utils.MyImage
//				.decodeFileBitmapDrawable(getResources(), drawId,
//						1600, 1200, new ScalingLogic()));
		initHandler();
		
		int langType =getIntent().getIntExtra("languageType", AppConstants.language_default);
		boolean isInstall = getIntent().getBooleanExtra("isInstall", false);
		
		if(isInstall) {
			if(langType==AppConstants.language_en) {
				addView(layout, R.mipmap.img_install_en_1);
			} else {
				addView(layout, R.mipmap.img_install_zh_1);
			}
		} else {
			if(langType==AppConstants.language_en) {
				addView(layout, R.mipmap.img_user_en_1);
			} else {
				addView(layout, R.mipmap.img_user_zh_1);
			}	
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(show_img);
			}
		}).start();
	}

	@Override
	void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler(){
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case show_img:
					int langType =getIntent().getIntExtra("languageType", AppConstants.language_default);
					boolean isInstall = getIntent().getBooleanExtra("isInstall", false);
					
					if(isInstall) {
						if(langType==AppConstants.language_en) {
							//addView(layout, R.mipmap.img_install_en_1);
							addView(layout, R.mipmap.img_install_en_2);
							addView(layout, R.mipmap.img_install_en_3);
							addView(layout, R.mipmap.img_install_en_4);
							addView(layout, R.mipmap.img_install_en_5);
							addView(layout, R.mipmap.img_install_en_6);
							addView(layout, R.mipmap.img_install_en_7);
						} else {
							//addView(layout, R.mipmap.img_install_zh_1);
							addView(layout, R.mipmap.img_install_zh_2);
							addView(layout, R.mipmap.img_install_zh_3);
							addView(layout, R.mipmap.img_install_zh_4);
							addView(layout, R.mipmap.img_install_zh_5);
							addView(layout, R.mipmap.img_install_zh_6);
							addView(layout, R.mipmap.img_install_zh_7);
						}
					} else {
						if(langType==AppConstants.language_en) {
							//addView(layout, R.mipmap.img_user_en_1);
							addView(layout, R.mipmap.img_user_en_2);
							addView(layout, R.mipmap.img_user_en_3);
							addView(layout, R.mipmap.img_user_en_4);
							addView(layout, R.mipmap.img_user_en_5);
							addView(layout, R.mipmap.img_user_en_6);
							addView(layout, R.mipmap.img_user_en_7);
							addView(layout, R.mipmap.img_user_en_8);
						} else {
							//addView(layout, R.mipmap.img_user_zh_1);
							addView(layout, R.mipmap.img_user_zh_2);
							addView(layout, R.mipmap.img_user_zh_3);
							addView(layout, R.mipmap.img_user_zh_4);
							addView(layout, R.mipmap.img_user_zh_5);
							addView(layout, R.mipmap.img_user_zh_6);
							addView(layout, R.mipmap.img_user_zh_7);
							addView(layout, R.mipmap.img_user_zh_8);
						}
					}
					//iv_qrcode.setBackgroundResource(drawId);
					
//					bmp = BitmapFactory.decodeResource(getResources(), drawId);
//					bmp = MyImage.decodeResources(HelpShowimgActivity.this, R.drawable.img1, 200, 200, new MyImage.ScalingLogic());
//					bmp = MyImage.zoomBitmap(bmp, phone_width, phone_height);
					//iv_qrcode.setBackgroundResource(drawId);
					
//					for(int i=0; i<20;i++) {
//						ImageView iv = new ImageView(HelpShowimgActivity.this);
//						iv.setBackgroundResource(R.drawable.img2);
//						Bitmap bm = MyImage.decodeResources(HelpShowimgActivity.this, R.drawable.img1, 500, 500, new MyImage.ScalingLogic());
//						bm = MyImage.zoomBitmap(bm, phone_width, phone_height);
//						iv.setImageBitmap(bm);
//						layout.addView(iv);
//					}
					
//					bmp2 = MyImage.decodeResources(HelpShowimgActivity.this, R.drawable.img1, 200, 200, new MyImage.ScalingLogic());
//					bmp2 = MyImage.zoomBitmap(bmp2, phone_width, phone_height);
//					iv_qrcode2.setImageBitmap(bmp2);
//					
//					bmp3 = MyImage.decodeResources(HelpShowimgActivity.this, R.drawable.img1, 200, 200, new MyImage.ScalingLogic());
//					bmp3 = MyImage.zoomBitmap(bmp3, phone_width, phone_height);
//					iv_qrcode3.setImageBitmap(bmp3);
					break;
				}
			}
		};
	}
	
	private void addView(ViewGroup layout, int resId) {
		ImageView iv = new ImageView(HelpShowimgActivity.this);
//		Bitmap bmp = MyImage.decodeResources(HelpShowimgActivity.this, resId, 700, 800, new MyImage.ScalingLogic());
//		bmp = MyImage.zoomBitmap(bmp, phone_width, phone_height);
		iv.setBackgroundResource(resId);
		layout.addView(iv);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.gc();
		super.onDestroy();
	}

}
