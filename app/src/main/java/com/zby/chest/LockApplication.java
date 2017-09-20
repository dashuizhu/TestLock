package com.zby.chest;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.zby.chest.agreement.BroadcastString;
import com.zby.chest.agreement.CmdPackage;
import com.zby.chest.agreement.ConnectBroadcastReceiver;
import com.zby.chest.bluetooth.BleBin;
import com.zby.chest.bluetooth.BluetoothLeServiceMulp;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.model.DeviceSqlService;
import com.zby.chest.utils.Myhex;
import com.zby.chest.utils.SetupData;
import com.zby.chest.utils.Tools;
import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class LockApplication extends Application {

  //当前start状态的activity个数
  private int count;

  public static final String TAG = LockApplication.class.getSimpleName();

  public static final String ACTION_NAME_CHANGE = "com.lock.name_change";

  private List<DeviceBean> list;

  private DeviceSqlService deviceSql;

  private BluetoothLeServiceMulp mBluetoothLeService;

  private MyComparator mComparator = new MyComparator();

  //private Map<String,DeviceBean> unlockSet =new HashMap<String,DeviceBean>();

  private boolean isRegister = false;

  /**
   * 上次操作时间
   */
  public static long mLastOptionTime = 0L;

  /**
   * 自动断开
   */
  private Subscription mLostSubscription;

  /**
   * 协议数据 广播接受
   */
  private ConnectBroadcastReceiver cmdreceiver;

  private boolean isAutoLost = false;

  private boolean isInBack = false;

  List<Activity> mActivityList = new ArrayList<Activity>();

  @Override public void onCreate() {
    // TODO Auto-generated method stub
    String curPack = Tools.getCurProcessName(this);
    if (curPack.equals(getPackageName())) {
      bindService();
      if (!isRegister) {
        cmdreceiver = new ConnectBroadcastReceiver(mHandler);
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
      Fabric.with(this, new Crashlytics());
    }

    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mActivityList.add(activity);
      }

      @Override public void onActivityStarted(Activity activity) {
        if (isInBack) {
          isAutoLost = false;
          Log.v("application", ">>>>>>>>>>>>>>>>>>>切到前台 ");
        }
        isInBack = false;
      }

      @Override public void onActivityResumed(Activity activity) {
        //if (count == 0) {
        //	Log.v("application", ">>>>>>>>>>>>>>>>>>>切到前台 "+count);
        //	isAutoLost = false;
        //}
        //count++;

      }

      @Override public void onActivityPaused(Activity activity) {
      }

      @Override public void onActivityStopped(Activity activity) {
        //if (!Tools.isApplicationInFornt(getApplicationContext())) {
        //	isInBack = true;
        //	Log.v("application", ">>>>>>>>>>>>>>>>>>>切到后台 "+count);
        //}
        //count--;
        //if (count == 0) {
        //只有app最后一个页面，才调停止
        //}
      }

      @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

      }

      @Override public void onActivityDestroyed(Activity activity) {
        mActivityList.remove(activity);
      }
    });

    mLastOptionTime = System.currentTimeMillis();
    startAutoLostBlueSubscripiton();
    super.onCreate();
  }

  @Override public void onTerminate() {
    // TODO Auto-generated method stub
    if (isRegister) {
      unregisterReceiver(receiver);
      unregisterReceiver(mGattUpdateReceiver);
      unbindService(serviceConnection);
      unregisterReceiver(cmdreceiver);
      isRegister = false;
    }
    if (mBluetoothLeService != null) {
      mBluetoothLeService.stopReadThread();
      mBluetoothLeService.closeAll();
    }
    unSubscription();
    super.onTerminate();
  }

  public void setHandler(Handler mHandler) {
    Log.e("tag", hashCode() + " application init  handler " + mHandler + " cmdRe" + cmdreceiver);
    if (cmdreceiver != null) {
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
   */
  public void addDeviceBin(final DeviceBean bin) {
    DeviceBean db;
    synchronized (list) {
      for (int i = 0; i < list.size(); i++) {
        db = list.get(i);
        if (bin.getMac().equals(db.getMac())) {
          //list.remove(i);
          //list.add(i, db);
          return;
        }
      }
      if (bin.getConnectionInterface() == null) {
        BleBin bleBin = new BleBin(this, mHandler, mBluetoothLeService);
        bin.setConnectionInterface(bleBin, this);
      }
      if (bin.getModeType() == 0) {
        new Thread(new Runnable() {

          @Override public void run() {
            // TODO Auto-generated method stub
            bin.writeNoresponse(CmdPackage.getStatus());
            try {
              Thread.sleep(1200);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            if (bin.getModeType() == 0) {
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

  public void removeDeviceBin(String mac) {
    // TODO Auto-generated method stub
    DeviceBean db;
    synchronized (list) {
      for (int i = 0; i < list.size(); i++) {
        db = list.get(i);
        if (mac.equals(db.getMac())) {
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
      for (int i = 0; i < list.size(); i++) {
        db = list.get(i);
        if (mac.equals(db.getMac())) {
          final DeviceBean now = db;
          new Thread(new Runnable() {

            @Override public void run() {
              // TODO Auto-generated method stub
              try {
                Thread.sleep(1000);
                now.write(CmdPackage.getStopLink());
                Thread.sleep(1000);
                if (now != null && now.isLink()) {
                  now.write(CmdPackage.getStopLink());
                  Thread.sleep(1000);
                  if (now != null && now.isLink()) {
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
          if (showToast) {
            Toast.makeText(this, db.getName() + getString(R.string.verify_error), Toast.LENGTH_LONG)
                    .show();
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
      for (int i = 0; i < list.size(); i++) {
        db = list.get(i);
        if (mac.equals(db.getMac())) {
          final DeviceBean now = db;
          new Thread(new Runnable() {

            @Override public void run() {
              // TODO Auto-generated method stub
              try {
                Thread.sleep(1000);
                now.write(CmdPackage.getStopLink());
                Thread.sleep(1000);
                if (now != null && now.isLink()) {
                  now.write(CmdPackage.getStopLink());
                  Thread.sleep(1000);
                  if (now != null && now.isLink()) {
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
          if (showToast) {
            Toast.makeText(this, db.getName() + getString(R.string.binds_fail), Toast.LENGTH_LONG)
                    .show();
          }
          break;
        }
      }
    }
    compareArray(list);
  }

  /**
   * 通过mac获得类型
   */
  public DeviceBean getDeviceBean(String mac) {
    DeviceBean db;
    synchronized (list) {
      for (int i = 0; i < list.size(); i++) {
        db = list.get(i);
        //Log.e("tag", "广播比较mac " + mac + "  dbin.mac " + db.getMac() + " " + (mac.equals(db.getMac())));
        if (mac.equals(db.getMac())) {
          //list.remove(i);
          //list.add(i, db);
          return db;
        }
      }
    }
    return null;
  }

  public List<DeviceBean> getDeviceList() {
    return list;
  }

  private Handler mHandler = new Handler() {
  };

  public List<DeviceBean> getDeviceListLinked() {
    List<DeviceBean> linkList = new ArrayList<DeviceBean>();
    DeviceBean dbin;
    for (int i = 0; i < list.size(); i++) {
      dbin = list.get(i);
      if (dbin.isLinkSuccess()) {
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
    for (int i = 0; i < list.size(); i++) {
      dbin = list.get(i);
      if (dbin.isLinkSuccess()) {
        Log.d("tag", "get Link " + dbin.getName());
        linkList.add(dbin);
      }
    }
    compareArray(linkList);
    return linkList;
  }

  /**
   * 排序
   */
  public List<DeviceBean> compareArray(List<DeviceBean> list) {
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
      if (intent.getAction().equals(BluetoothLeServiceMulp.ACTION_BLUETOOTH_FOUND)) {//发现了蓝牙设备
        String name = intent.getStringExtra("name");
        String mac = intent.getStringExtra("mac");
        int rssi = intent.getIntExtra("rssi", 100);
        int type = intent.getIntExtra("type", 1);
        boolean onOff = intent.getBooleanExtra("onOff", false);
        autoLink(mac, name, rssi, type, onOff);
      }
    }

    ;
  };

  private void autoLink(String mac, String name, int rssi, int type, boolean onOff) {
    DeviceBean bean;
    synchronized (list) {
      for (int i = 0; i < list.size(); i++) {
        bean = list.get(i);
        Log.d(TAG, "bean.mac: "
                + bean.getMac()
                + "  "
                + mac
                + "  "
                + bean.getMac().equals(mac)
                + "  "
                + bean.isLink());
        if (bean.getMac().equals(mac)) {
          if (bean.getConnectionInterface() == null) {
            BleBin bleBin = new BleBin(this, mHandler, mBluetoothLeService);
            bean.setConnectionInterface(bleBin, this);
          }
          if (!bean.getName().equals(name)) {
            bean.setName(name);
            deviceSql.insert(bean);
            sendNameBroadcase();
          }
          if (bean.isLink()) { //连上了，就判断强度， 是否要断开
            //if(bean.getModeType()==DeviceBean.LockMode_auto) {
            //							if(rssi<100 && !bean.isOnOff()) {
            //								bean.write(CmdPackage.getLockOff(0));
            //							}
            //						}
          } else { //没连上，就要连上
            //如果是5分钟不操作自动断开，就不再重连
            if (!isAutoLost) {
              bean.setModeType(0);
              //						bean.setOnOff(onOff);
              bean.connect();
            }
          }
          return;
        }
      }
    }
  }

  private void sendNameBroadcase() {
    Intent intent = new Intent(ACTION_NAME_CHANGE);
    sendBroadcast(intent);
  }

  ServiceConnection serviceConnection;

  private void bindService() {
    serviceConnection = new ServiceConnection() {

      @Override public void onServiceDisconnected(ComponentName service) {
        // TODO Auto-generated method stub
        mBluetoothLeService = null;
      }

      @Override public void onServiceConnected(ComponentName arg0, IBinder service) {
        // TODO Auto-generated method stub
        mBluetoothLeService = ((BluetoothLeServiceMulp.LocalBinder) service).getService();
        if (!mBluetoothLeService.initialize()) {
        }
      }
    };
    Intent gattServiceIntent = new Intent(this, BluetoothLeServiceMulp.class);
    bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
  }

  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      final String mac = intent.getStringExtra("mac");
      Log.d("tag", "接受广播" + mac + "  " + action);
      if (BluetoothLeServiceMulp.ACTION_GATT_CONNECTED.equals(action)) {
        compareArray(list);
      } else if (BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED.equals(action)) {
        compareArray(list);
      } else if (BluetoothLeServiceMulp.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
        //蓝牙连接成功就自动检验密码
        new Thread(new Runnable() {

          @Override public void run() {
            // TODO Auto-generated method stub
            DeviceBean dbin = getDeviceBean(mac);
            Log.d("tag", "接受广播1 " + list.size() + " mac =" + mac + " dbin" + (dbin == null));
            if (dbin != null) {
              try {
                Thread.sleep(300);
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              int count = 0;
              while (dbin.getModeType() == 0) {
                count++;
                if (count > 2) {
                  Log.e("tag", "发送数据 失败" + count);
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
              Log.d("tag", "发送" + dbin.getMac() + "记忆的密码" + dbin.getPairPassword());
              if (dbin.getPairPassword() != null) {
                dbin.write(CmdPackage.verifyPassword(dbin.getPairPassword()));
              }
              try {
                Thread.sleep(1100);
                String defaultAdminPsd = SetupData.getSetupData(getApplicationContext())
                        .read(AppString.KEY_ADMIN_PASSWORD + dbin.getMac(),
                                AppConstants.DEFAULT_ADMIN_PASSWORD);

                dbin.write(CmdPackage.verifyAdminPassword(defaultAdminPsd));
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }

              try {
                Thread.sleep(1100);
                String defaultAdminPsd = SetupData.getSetupData(getApplicationContext())
                        .read(AppString.KEY_ADMIN_PASSWORD + dbin.getMac(),
                                AppConstants.DEFAULT_ADMIN_PASSWORD);

                dbin.write(CmdPackage.verifyAdminPassword(defaultAdminPsd));
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              //unlockSet.remove(mac);
            }
          }
        }).start();
        // Show all the supported services and characteristics on the
        // user interface.
      } else if (BluetoothLeServiceMulp.ACTION_DATA_AVAILABLE.equals(action)) { //解析数据
        String buffer = intent.getStringExtra(BluetoothLeServiceMulp.EXTRA_DATA);
        Log.e("tag", mac + "接受数据" + buffer);
        if (mBluetoothLeService != null) {
          DeviceBean bean = getDeviceBean(mac);
          if (bean != null) {
            bean.parseData(Myhex.hexStringToByte(buffer));
          }
          //					byte[] buff = Myhex.hexStringToByte(buffer);
          //					Message msg = handler.obtainMessage();
          //					msg.what = MESSAGE_READ;
          //					msg.obj = buff;
          //					handler.sendMessage(msg);
        }
      } else if (BluetoothLeServiceMulp.ACTION_RSSI_ACTION.equals(action)) {
      }
    }
  };

  private static IntentFilter makeGattUpdateIntentFilter() {
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_CONNECTED);
    intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_DISCONNECTED);
    intentFilter.addAction(BluetoothLeServiceMulp.ACTION_GATT_SERVICES_DISCOVERED);
    intentFilter.addAction(BluetoothLeServiceMulp.ACTION_DATA_AVAILABLE);
    intentFilter.addAction(BluetoothLeServiceMulp.ACTION_RSSI_ACTION);
    return intentFilter;
  }

  Set<String> maclist = new HashSet<String>();

  public Set<String> getDeviceMacList() {
    // TODO Auto-generated method stub
    if (maclist.size() == 0) {
      DeviceBean dbin;
      for (int i = 0; i < list.size(); i++) {
        dbin = list.get(i);
        maclist.add(dbin.getMac());
      }
    }
    return maclist;
  }

  /**
   * 5分钟不操作 自动断开蓝牙
   */
  private void startAutoLostBlueSubscripiton() {
    if (mLostSubscription == null) {
      mLostSubscription = Observable.interval(5, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
        @Override public void call(Long aLong) {
          long delayTime = System.currentTimeMillis() - mLastOptionTime;
          if (delayTime > AppConstants.auto_lost_time) {
            closeApp();
          }
        }
      }, new Action1<Throwable>() {
        @Override public void call(Throwable throwable) {
          throwable.printStackTrace();
        }
      });
    }
  }

  private void closeApp() {
    new Thread(new Runnable() {
      @Override public void run() {
        Log.d(TAG, " time auto lost");
        isAutoLost = true;
        mBluetoothLeService.closeAll();
        mLastOptionTime = System.currentTimeMillis();

        if (SetupData.getSetupData(getApplicationContext()).readBoolean(AppString.exit_closeBT)) {
          BluetoothManager mBluetoothManager = null;
          BluetoothAdapter mBluetoothAdapter;
          if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
              Log.e(TAG, "Unable to initialize BluetoothManager.");
            } else {
              mBluetoothAdapter = mBluetoothManager.getAdapter();
              if (mBluetoothAdapter != null) {
                mBluetoothAdapter.disable();
              }
            }
          }
        }

        for (int i = 0; i < mActivityList.size(); i++) {
          mActivityList.get(i).finish();
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
        System.exit(0);
      }
    }).start();
  }

  private void unSubscription() {
    if (mLostSubscription != null) {
      if (!mLostSubscription.isUnsubscribed()) {
        mLostSubscription.unsubscribe();
      }
      mLostSubscription = null;
    }
  }
}
