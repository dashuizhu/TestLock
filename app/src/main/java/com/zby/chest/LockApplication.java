package com.zby.chest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wp.activity.CrashHandler;

import com.zby.chest.R;
import com.zby.chest.activity.ScanActivity;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.agreement.ConnectBroadcastReceiver;
import com.zby.chest.bluetooth.BleBin;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceSqlService;
import com.zby.chest.utils.Myhex;
import com.zby.chest.utils.ScreenUtils;
import com.zby.chest.utils.Tools;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class LockApplication extends Application {
	
public static final String TAG = LockApplication.class.getSimpleName();
	
	public static final String ACTION_NAME_CHANGE = "com.lock.name_change";

	private List<DeviceBean> list;
	
	private DeviceSqlService deviceSql;
	
	private BluetoothLeServiceMulp mBluetoothLeService;
	
	private MyComparator mComparator = new MyComparator();
	
	//private Map<String,DeviceBean> unlockSet =new HashMap<String,DeviceBean>();
	
	private boolean isRegister = false;
	
	 /**
		 * 协议数据 广播接受
		 */
		private ConnectBroadcastReceiver cmdreceiver;
		
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		String curPack = Tools.getCurProcessName(this);
		if(curPack.equals("com.zby.chest")) {
			bindService();
			if(!isRegister) {
				cmdreceiver = new ConnectBroadcastReceiver( mHandler);
				registerReceiver(cmdreceiver, new IntentFilter(BroadcastString.BROADCAST_ACTION));
				IntentFilter interFilter = new IntentFilter(BluetoothLeServiceMulp.ACTION_BLUETOOTH_FOUND);
				registerReceiver(receiver, interFilter);
				IntentFilter gattFilter = makeGattUpdateIntentFilter();
				registerReceiver(mGattUpdateReceiver, gattFilter);
			}
			
			deviceSql = new DeviceSqlService(this);
			list = deviceSql.selectAll();
			compareArray(list);
			
			//AppConstants.scrollDistance = ScreenUtils.dip2px(this, 20);
			AppConstants.scrollDistance = 10;
			
		}
		
		// 异常处理，不需要处理时注释掉这两句即可！  
//	    CrashHandler crashHandler = CrashHandler.getInstance();   
//	    // 注册crashHandler   
//	    crashHandler.init(getApplicationContext());   
//
//	    /**
//	     * 这里有个问题， 要判断网络状况，  在这里调用，就只是在启动软件的时候，发送以前的错误信息到服务器，
//	     * 如果要及时的  发生错误就发送信息，  就得将这个sendPreviousReportsToServer函数在handlerException中调用
//	     */
//		//发送错误报告到服务器 
//		crashHandler.sendPreviousReportsToServer();
		super.onCreate();
	}
	
	
	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		if(isRegister) {
			unregisterReceiver(receiver);
			unregisterReceiver(mGattUpdateReceiver);
			unbindService(serviceConnection);
			unregisterReceiver(cmdreceiver);
			isRegister = false;
		}
		if(mBluetoothLeService!=null) {
			mBluetoothLeService.stopReadThread();
			mBluetoothLeService.closeAll();
		}
		super.onTerminate();
	}
	
	public void setHandler(Handler mHandler) {
		Log.e("tag", hashCode()+" application init  handler "+ mHandler + " cmdRe"+cmdreceiver);
		if(cmdreceiver!=null) {
			cmdreceiver.setHandler(mHandler);
		}
	}

	public DeviceBean newDeviceBean(String mac, String name, int type) {
		DeviceBean dbin = new DeviceBean();
		dbin.setName(name);
		dbin.setMac(mac);
		dbin.setModeType(type);
		BleBin bleBin = new BleBin(this, mHandler, mBluetoothLeService);
		dbin.setConnectionInterface(bleBin, this);
		return dbin;
	}


	/**
	 * 添加或者更新设备
	 * @param bin
	 */
	public void addDeviceBin(final DeviceBean bin) {
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				if(bin.getMac().equals(db.getMac())) {
					//list.remove(i);
					//list.add(i, db);
					return;
				}
			}
			if(bin.getConnectionInterface()==null) {
				BleBin bleBin = new BleBin(this, mHandler, mBluetoothLeService);
				bin.setConnectionInterface(bleBin, this);
			}
			if(bin.getModeType()==0) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						bin.writeNoresponse(CmdPackage.getStatus());
						try {
							Thread.sleep(1200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(bin.getModeType()==0) {
							bin.write(CmdPackage.getStatus());
						}
					}
				}).start();
			}
			list.add(bin);
			deviceSql.insert(bin);
			maclist.add(bin.getMac());
		}
		compareArray(list);
	}
	
	public void removeDeviceBin(DeviceBean bin) {
		// TODO Auto-generated method stub
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				if(bin.getMac().equals(db.getMac())) {
					list.remove(i);
					maclist.remove(db.getMac());
					break;
				}
			}
		}
		compareArray(list);
	}
	
	
	
	public void removeDeviceBin(String mac) {
		// TODO Auto-generated method stub
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				if(mac.equals(db.getMac())) {
					db.stopConnect();
					deviceSql.delete(db.getId());
					list.remove(i);
					maclist.remove(db.getMac());
					//Toast.makeText(this, db.getName()+getString(R.string.verify_error), 3).show();
					break;
				}
			}
		}
		compareArray(list);
	}

	
	public void removeDeviceBinNotStop(String mac, boolean showToast) {
		// TODO Auto-generated method stub
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				if(mac.equals(db.getMac())) {
				    final DeviceBean now  = db;
				    new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            try {
                                Thread.sleep(1000);
                                now.write(CmdPackage.getStopLink());
                                Thread.sleep(1000);
                                if(now!=null && now.isLink()) {
                                	now.write(CmdPackage.getStopLink());
                                	Thread.sleep(1000);
                                	if(now!=null && now.isLink()) {
                                		now.closeConnect();
                                	}
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
				    }).start();
					deviceSql.delete(db.getId());
					list.remove(i);
					maclist.remove(db.getMac());
					if(showToast) {
						Toast.makeText(this, db.getName()+getString(R.string.verify_error), 3).show();
					}
					break;
				}
			}
		}
		compareArray(list);
	}
	
	public void removeDeviceBinNotStopBandingFail(String mac, boolean showToast) {
		// TODO Auto-generated method stub
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				if(mac.equals(db.getMac())) {
				    final DeviceBean now  = db;
				    new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            try {
                                Thread.sleep(1000);
                                now.write(CmdPackage.getStopLink());
                                Thread.sleep(1000);
                                if(now!=null && now.isLink()) {
                                	now.write(CmdPackage.getStopLink());
                                	Thread.sleep(1000);
                                	if(now!=null && now.isLink()) {
                                		now.closeConnect();
                                	}
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
				    }).start();
					deviceSql.delete(db.getId());
					list.remove(i);
					maclist.remove(db.getMac());
					if(showToast) {
						Toast.makeText(this, db.getName()+getString(R.string.binds_fail), 3).show();
					}
					break;
				}
			}
		}
		compareArray(list);
	}
	
	
	/**
	 * 通过mac获得类型
	 * @param mac
	 * @return
	 */
	public DeviceBean getDeviceBean(String mac) {
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				//Log.e("tag", "广播比较mac " + mac + "  dbin.mac " + db.getMac() + " " + (mac.equals(db.getMac())));
				if(mac.equals(db.getMac())) {
					//list.remove(i);
					//list.add(i, db);
					return db;
				}
			}
		}
		return null;
	}
	
	public DeviceBean getDeviceBeanAutoMode(String mac) {
		DeviceBean db;
		synchronized (list) {
			for(int i=0; i < list.size(); i ++) {
				db = list.get(i);
				if(db.getModeType()==DeviceBean.LockMode_auto) {
					if(mac.equals(db.getMac())) {
						//list.remove(i);
						//list.add(i, db);
						return db;
					}
				}
			}
		}
		return null;
	}
	
	public List<DeviceBean> getDeviceList() {
		return list;
	}
	
	private Handler mHandler = new Handler() {};
	
	
	public List<DeviceBean> getDeviceListLinked() {
		List<DeviceBean> linkList = new ArrayList<DeviceBean>();
		DeviceBean dbin;
		for(int i=0; i<list.size() ; i ++) {
			dbin = list.get(i);
			if(dbin.isLinkSuccess()) {
				Log.d("tag", "get Link " + dbin.getName());
				linkList.add(dbin);
			}
		}
		compareArray(linkList);
		return linkList;
	}
	
	//只判断读取到连接状态， 不做是否读取到锁类型
	public List<DeviceBean> getDeviceListLinked2() {
		List<DeviceBean> linkList = new ArrayList<DeviceBean>();
		DeviceBean dbin;
		for(int i=0; i<list.size() ; i ++) {
			dbin = list.get(i);
			if(dbin.isLinkSuccess()) {
				Log.d("tag", "get Link " + dbin.getName());
				linkList.add(dbin);
			}
		}
		compareArray(linkList);
		return linkList;
	}
	
	/**
	 * 排序
	 * 
	 * @param list
	 */
	public synchronized List<DeviceBean>  compareArray(List<DeviceBean> list) {
		synchronized (list) {
			Collections.sort(list, mComparator);
		}
		return list;
	}
	

	private class MyComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			DeviceBean obj1 = (DeviceBean) o1;
			DeviceBean obj2 = (DeviceBean) o2;
			if (obj1.isLinkSuccess() & obj2.isLinkSuccess()) {
				return obj1.getModeType() >= obj2.getModeType() ? 1 : -1;
			} else {
				return obj1.isLinkSuccess() ? -1 : 1;
			}
		}
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context arg0, android.content.Intent intent) {
			if(intent.getAction().equals(BluetoothLeServiceMulp.ACTION_BLUETOOTH_FOUND)) {//发现了蓝牙设备
				String mac =intent.getStringExtra("mac");
				String name =intent.getStringExtra("name");
				int rssi =intent.getIntExtra("rssi", 100);
				int type = intent.getIntExtra("type", 1);
				boolean onOff= intent.getBooleanExtra("onOff", false);
				autoLink(mac, name, rssi, type, onOff);
			}
		};
	};
	
	private void autoLink(String mac, String name, int rssi, int type, boolean onOff) {
		 DeviceBean bean;
		synchronized (list) {
			for(int i=0; i<list.size(); i++) {
				bean = list.get(i);
				Log.d(TAG, "bean.mac: " + bean.getMac() + "  " + mac + "  " + bean.getMac().equals(mac) + "  "+ bean.isLink());
				if(bean.getMac().equals(mac)) {
					if(bean.getConnectionInterface()==null) {
						BleBin bleBin = new BleBin(this, mHandler, mBluetoothLeService);
						bean.setConnectionInterface(bleBin, this);
					}
					if(!bean.getName().equals(name)) {
						bean.setName(name);
						deviceSql.insert(bean);
						sendNameBroadcase();
					}
					if(bean.isLink()) { //连上了，就判断强度， 是否要断开
						//if(bean.getModeType()==DeviceBean.LockMode_auto) {
//							if(rssi<100 && !bean.isOnOff()) {
//								bean.write(CmdPackage.getLockOff(0));
//							} 
//						}
					} else { //没连上，就要连上
						bean.setModeType(0);
//						bean.setOnOff(onOff);
						bean.connect();
//						if(bean.getModeType()==DeviceBean.LockMode_auto ) {//需要自动解锁
//							if(!unlockSet.containsKey(bean.getMac())) {
//								unlockSet.put(bean.getMac(), bean);
//							}
//						} else {
//						}
					}
					return ;
				}
			}
		}
	}
	
	private void sendNameBroadcase() {
		Intent intent = new Intent(ACTION_NAME_CHANGE);
		sendBroadcast(intent);
	}
	
	ServiceConnection serviceConnection ;
	private void bindService() {
		 serviceConnection  = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName service) {
				// TODO Auto-generated method stub
				mBluetoothLeService = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder service) {
				// TODO Auto-generated method stub
				mBluetoothLeService = ((BluetoothLeServiceMulp.LocalBinder) service)
						.getService();
				if (!mBluetoothLeService.initialize()) {
				}
			}
		};
		Intent gattServiceIntent = new Intent(this, BluetoothLeServiceMulp.class);
		bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
	}
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final String mac = intent.getStringExtra("mac");
			Log.d("tag","接受广播"+mac + "  "+ action);
			if (BluetoothLeServiceMulp.ACTION_GATT_CONNECTED.equals(action)) {
				compareArray(list);
			} else if (BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED.equals(action)) {
				compareArray(list);
			} else if (BluetoothLeServiceMulp.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				//蓝牙连接成功就自动检验密码
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DeviceBean dbin = getDeviceBean(mac);
						Log.d("tag", "接受广播1 " +list.size()+" mac =" + mac + " dbin"  + (dbin==null) );
						if(dbin!=null) {
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							int count =0;
							while(dbin.getModeType()==0) {
								count ++;
								if(count >2) {
									Log.e("tag", "发送数据 失败" + count );
									//dbin.closeConnect();
									dbin.writeNoresponse(CmdPackage.getStatus());
									return; 
								}
								dbin.write(CmdPackage.getStatus());
								try {
									Thread.sleep(1100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									continue;
								}
							}
							Log.d("tag","发送" + dbin.getMac()+"记忆的密码"+dbin.getPairPassword());
							if(dbin.getPairPassword()!=null) {
								dbin.write(CmdPackage.verifyPassword(dbin.getPairPassword()));
							}
                            try {
                                Thread.sleep(1100);
                                dbin.write(CmdPackage.verifyMac(Tools.getMacAddress(getApplicationContext())));
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
							//unlockSet.remove(mac);
						}
						if(dbin!=null && dbin.getModeType() == DeviceBean.LockMode_auto) {
							mBluetoothLeService.startReadRssiThread(dbin.getMac());
						}
					}
				}).start();
				// Show all the supported services and characteristics on the
				// user interface.
			} else if (BluetoothLeServiceMulp.ACTION_DATA_AVAILABLE.equals(action)) { //解析数据
				String buffer = intent.getStringExtra(BluetoothLeServiceMulp.EXTRA_DATA);
				Log.e("tag",mac+ "接受数据"+ buffer);
				if(mBluetoothLeService!=null) {
					DeviceBean bean = getDeviceBean(mac);
					if(bean!=null) {
						bean.parseData(Myhex.hexStringToByte(buffer));
					}
//					byte[] buff = Myhex.hexStringToByte(buffer);
//					Message msg = handler.obtainMessage();
//					msg.what = MESSAGE_READ;
//					msg.obj = buff;
//					handler.sendMessage(msg);
				}
			} else if(BluetoothLeServiceMulp.ACTION_RSSI_ACTION.equals(action)) {
				DeviceBean dbin = getDeviceBeanAutoMode(mac);
				int rssi = intent.getIntExtra("rssi", 100);
				if(dbin!=null) {
					Log.d("tag", "接受"+dbin.getName()+ "  rssi:"+ rssi);
					if(rssi<=-100) {
						//dbin.stopConnect();
					} else if(rssi>=-85) {
						if(!dbin.isOnOff() && dbin.getModeType()==DeviceBean.LockMode_auto) {
							Log.d("tag", "接受"+dbin.getName()+ "  rssi:"+ rssi + "发送");
							dbin.write(CmdPackage.getLockOff(1));
						}
					}
				}
			}
		}
	};
	
	
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeServiceMulp.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeServiceMulp.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeServiceMulp.ACTION_RSSI_ACTION);
		return intentFilter;
	}


	Set<String> maclist = new HashSet<String>();
	public Set<String> getDeviceMacList() {
		// TODO Auto-generated method stub
		if(maclist.size()==0) {
			DeviceBean dbin;
			for(int i =0; i<list.size(); i++) {
				dbin = list.get(i);
				maclist.add(dbin.getMac());
			}
		}
		return maclist;
	}







}
