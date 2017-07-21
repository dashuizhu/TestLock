package com.zby.chest.model;

import com.zby.chest.utils.MyByte;
import java.security.acl.LastOwnerException;

import com.zby.chest.AppConstants;
import com.zby.chest.R;
import com.zby.chest.agreement.CmdDataParse;
import com.zby.chest.bluetooth.ConnectionInterface;
import com.zby.chest.utils.ScreenUtils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceBean {
	
	public final  String TAG = DeviceBean.class.getSimpleName();

	//public static final int LockMode_auto = 1;
	public static final int LockMode_scroll = 2;
	public static final int LockMode_password = 3;
	private float downX;
	private float downY;
	private long  downTime;
	/**
	 * 用来判断是否正处于一次滑动判断中
	 */
	private boolean isReady = false;

	
	private int id;
	private String mac;
	
	private String name;

	private String password;

	private int modeType;
	
	private int bindCount;//绑定设备个数
	
	private String pairPassword;

	private boolean onOff;
	private boolean isLink;
	private boolean adminVerify; //管理员密码验证
	
	private ConnectionInterface bleBin;
	private CmdDataParse cmdParse;

	/**
	 * 上次密码开锁时间
	 */
	private long mLastPasswordTime;

	public boolean isAdminVerify() {
		return adminVerify;
	}

	public void setAdminVerify(boolean adminVerify) {
		this.adminVerify = adminVerify;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	

	public String getPairPassword() {
		return pairPassword;
	}

	public void setPairPassword(String pairPassword) {
		this.pairPassword = pairPassword;
	}

	public boolean isLink() {
		if(AppConstants.isDemo) {
			return isLink;
		}
		if(bleBin!=null) {
			return bleBin.isLink();
		}
		return false;
	}
	
	public boolean isLinkSuccess() {
		if(AppConstants.isDemo) {
			return isLink;
		}
		if(getModeType()==0) return false;
		if(bleBin!=null) {
			return bleBin.isLink();
		}
		return false;
	}
	
	//只判断是否连接上，不判断是否读取到数据
	public boolean isLinkSuccess2() {
		if(AppConstants.isDemo) {
			return isLink;
		}
		//if(getModeType()==0) return false;
		if(bleBin!=null) {
			return bleBin.isLink();
		}
		return false;
	}
	
	public boolean isLinkING() {
		if(bleBin!=null) {
			return bleBin.isConnecting();
		}
		return false;
	}


	public String getName() {
		if(name!=null && name.contains("DP151") && name.length()>12) {
			String shortName = name.substring(name.length()-6, name.length());
			return "DP151-"+ shortName;
		}
		return name==null? "":name.trim();
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOnOff() {
		return onOff;
	}

	public void setOnOff(boolean onOff) {
		this.onOff = onOff;
	}

	public void enableOnoff() {
		this.onOff = !onOff;
	}
	
	

	public int getBindCount() {
		return bindCount;
	}

	public void setBindCount(int bindCount) {
		this.bindCount = bindCount;
	}

	public int getModeType() {
		if(modeType<0 || modeType>LockMode_password)
			modeType =0;
		return modeType;
	}

	public void setModeType(int modeType) {
		this.modeType = modeType;
	}
	
	

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public ConnectionInterface getConnectionInterface() {
		return bleBin;
	}

	public void setConnectionInterface(ConnectionInterface connectionInterface, Context context) {
		this.bleBin = connectionInterface;
		cmdParse = new CmdDataParse(this, context);
		this.bleBin.setDataParse(cmdParse);
	}
	
	private Handler mHandler  = new Handler();
	
	public void parseData(byte[] buffer) {
		if(cmdParse!=null) {
			cmdParse.parseData(buffer);
		}
	}
	
	public boolean write(byte[] buffer) {
		Log.w("test", "write :" + MyByte.buffer2String(buffer));
		if(bleBin!=null) {
			if(bleBin.isLink()) {
				bleBin.write(buffer);
				return true;
			}
		}
		return false;
	}
	public boolean writeNoresponse(byte[] buffer) {
		Log.w("test", "write :" + MyByte.buffer2String(buffer));
		if(bleBin!=null) {
			if(bleBin.isLink()) {
				bleBin.write(buffer);
				return true;
			}
		}
		return false;
	}

	
	public void stopConnect() {
		// TODO Auto-generated method stub
		if(bleBin!=null) {
				bleBin.stopConncet();
		}
	}
	
	public void closeConnect() {
		if(bleBin!=null) {
			bleBin.onBleDestory();
		}
	}
	
	public void connect() {
		if(bleBin!=null) {
			bleBin.connect(mac, "");
		}
	}





	View view;

	public View getView(Context mContext, final BaseAdapter adapter) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			view = inflater.inflate(R.layout.device_list_item,
					null);
		}
		TextView tv_lock = (TextView) view.findViewById(R.id.textView_lock);
		TextView tv_switch = (TextView) view.findViewById(R.id.textView_switch);
		ImageView iv_lockOff = (ImageView) view.findViewById(R.id.imageView_lockOff);
		tv_lock.setText(getName());
		Drawable drawable = null;
		int switchRes = 0;
		//Log.w("tag", "address: " + getMac() + "  " + getModeType());
		switch (getModeType()) {
			case DeviceBean.LockMode_password:
				drawable = mContext.getResources().getDrawable(
						R.drawable.btn_lock_password);
				switchRes = R.drawable.cb_lock_switch_password;
				break;
			case 0:
			case DeviceBean.LockMode_scroll:
				drawable = mContext.getResources().getDrawable(
						R.drawable.btn_lock_scroll);
				switchRes = R.drawable.cb_lock_switch_scroll;
				break;
		}
		if(drawable!=null) {
			drawable.setBounds(0, 0, ScreenUtils.dp2sp(mContext, 50),
					ScreenUtils.dp2sp(mContext, 50));
		}
		tv_lock.setCompoundDrawables(drawable, null, null, null);
		tv_lock.setSelected(isLinkSuccess());

		Log.v("DeviceBin ", getMac()+ " isOnOff:" + isOnOff() + " " +isLinkSuccess());
		iv_lockOff.setVisibility(View.GONE);
		if(isLinkSuccess()) {
			tv_switch.setText("");
			if(isOnOff()) {
				iv_lockOff.setVisibility(View.VISIBLE);
				tv_switch.setVisibility(View.GONE);
			} else {
				tv_switch.setVisibility(View.VISIBLE);
				tv_switch.setBackgroundResource(switchRes);
				tv_switch.setHeight(ScreenUtils.dp2sp(mContext,80));
				tv_switch.setWidth(ScreenUtils.dp2sp(mContext,100));
			}
			tv_switch.setSelected(isOnOff()&& isLinkSuccess());
//			Drawable switchDrawable =  mContext.getResources().getDrawable(switchRes);
//			switchDrawable.setBounds(0, 0, ScreenUtils.dp2sp(mContext, 100),
//					ScreenUtils.dp2sp(mContext, 60));
//			tv_switch.setCompoundDrawables(null, null, null, null);
		} else {
			tv_switch.setVisibility(View.VISIBLE);
			tv_switch.setBackgroundResource(0);
			tv_switch.setText(R.string.unlink);
			tv_switch.setTextColor(Color.GRAY);
		}
//		if(getModeType() == LockMode_scroll) {
//			view.setOnLongClickListener(new OnLongClickListener() {
//				
//				@Override
//				public boolean onLongClick(View arg0) {
//					// TODO Auto-generated method stub
//					if(listener!=null) {
//						listener.onDeviceLongItemClick(DeviceBean.this);
//					}
//					return false;
//				}
//			});
			view.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
//					if (!isLink())
//						return false;
					System.out.println( mac + " onTouch " + arg1.getAction());
					int action = arg1.getAction();
					float move;
					switch (arg1.getAction()) {
					case MotionEvent.ACTION_DOWN:
						downX = arg1.getX();
						downY = arg1.getY();
						downTime = System.currentTimeMillis();
						isReady = false;
						view.setPressed(true);
						
						mHandler.postDelayed(myLongHandler, AppConstants.longClickTime);
						break;
//					case MotionEvent.ACTION_MOVE:
//						if(getModeType() != LockMode_scroll) {
//							return true;
//						}
//						if (!isReady) {
//							move = arg1.getX() - downX;
//							System.out.println("滑动  "+move  + " nowX:"+arg1.getX() + "    dowxX:"+downX);
//							if (move < -AppConstants.scrollDistance) {
//								Log.d(TAG, "左滑成功");
//								isReady = true;
////								onOff = true;
////								adapter.notifyDataSetChanged();
//							} else if (move > AppConstants.scrollDistance) {
//								Log.d(TAG, "右滑成功");
//								isReady = true;
//								//onOff = false;
//								//adapter.notifyDataSetChanged();
//								if(!isOnOff()) {
//									if(listener!=null) {
//										listener.onDeviceScroll(DeviceBean.this);
//									}
//								}
//								return true;
//							}
//						}
//						break;
					case MotionEvent.ACTION_CANCEL:
						view.setPressed(false);
						move = arg1.getX() - downX;
						mHandler.removeCallbacks(myLongHandler);
						if(getModeType() != LockMode_scroll) {
							if(listener!=null) {
								listener.onDeviceItemClick(DeviceBean.this);
							}
							return true;
						}
						System.out.println("滑动  "+move  + " nowX:"+arg1.getX() + "    dowxX:"+downX);
						if (move < -AppConstants.scrollDistance) {
							Log.d(TAG, "左滑成功");
							isReady = true;
//							onOff = true;
//							adapter.notifyDataSetChanged();
//							bleBin.write(CmdPackage.getLockOff(modeType));
						} else if (move > AppConstants.scrollDistance ) {
							Log.d(TAG, "右滑成功");
							isReady = true;
							//onOff = false;
							//adapter.notifyDataSetChanged();
								if(listener!=null) {
									listener.onDeviceScroll(DeviceBean.this);
								}
							return false;
						}
						break;
					case MotionEvent.ACTION_UP:
						view.setPressed(false);
						move = arg1.getX() - downX;
						long pressTime = System.currentTimeMillis() - downTime;
						Log.d(TAG,"滑动  "+move  + " nowX:"+arg1.getX() + "    dowxX:"+downX + "  time:"+pressTime);
						
						if(Math.abs(move)<3.0) {
//							if(pressTime >= 5000) {
//								if(listener!=null) {
//									listener.onDeviceLongLongClick(DeviceBean.this);
//								}
//								return false;
//							} else if (pressTime>=2000 && pressTime<5000) {
//								if(listener!=null) {
//									listener.onDeviceLongClick(DeviceBean.this);
//								}
//								return false;
//							}
							if (pressTime>= AppConstants.longClickTime) {//长按两秒以上
								return false;
							}
						}
						mHandler.removeCallbacks(myLongHandler);
						if(getModeType() != LockMode_scroll) {
							if(listener!=null) {
								if (pressTime< AppConstants.longClickTime) {//长按两秒以上
									listener.onDeviceItemClick(DeviceBean.this);
								}
							}
							return true;
						}
						break;
					case 2:
						break;
					default :
						mHandler.removeCallbacks(myLongHandler);
						break;
					}
					return true;
				}
			});
//		} else if(getModeType()==LockMode_password) {
//			view.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View arg0) {
//					// TODO Auto-generated method stub
//					if(listener!=null) {
//						listener.onDeviceItemClick(DeviceBean.this);
//					}
//				}
//			});
//		}
		return view;
	}
	
	
	private DeviceListener listener;
	public void setDeviceListener(DeviceListener deviceListener) {
		this.listener = deviceListener;
	}

	public void setLink(boolean b) {
		// TODO Auto-generated method stub
		this.isLink = b;
	}
	
	
	Runnable myLongHandler = new  Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
				Log.e("tag", "myLongHandler");
				if(listener!=null) {
					listener.onDeviceLongClick(DeviceBean.this);
				}
			
		}
		
	};

	public long getLastPasswordTime() {
		return mLastPasswordTime;
	}

	public void setLastPasswordTime(long lastPasswordTime) {
		mLastPasswordTime = lastPasswordTime;
	}

	/**
	 * 是否密码开锁 15秒内
	 * @return
	 */
	public boolean isPasswordLockTimeAllow() {
		long delayTime = System.currentTimeMillis() - mLastPasswordTime;
		return delayTime <15 *1000;
	}
}
