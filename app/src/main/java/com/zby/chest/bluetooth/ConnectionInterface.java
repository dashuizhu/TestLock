package com.zby.chest.bluetooth;

import java.util.ArrayList;
import java.util.List;

import com.zby.chest.agreement.CmdDataParse;



public interface ConnectionInterface {
	
	/**
	 * 保存搜索到的所有设备
	 */
	
	static String linkIp="";
	
	/**
	 * 找到一个IP
	 */
	public final static int FindIp = 0;
	
	
	/**
	 * 连接成功
	 */
	public final static int LinkSuccess = 1;
	
	/**
	 * 连接失败
	 */
	public final static int LinkFailure = 2;
	
	/**
	 * 得到异或数据
	 */
	public final static int GetData = 3;// 得到数据;
	
	public final static int linkLost = 33;
	
	/**
	 * 查找IP标志位
	 */
	public final static int startFindIp=4;//查找IP标志位
	
	/**
	 * 搜索完成
	 */
	public final static int FindOk=5;//搜索完成
	
	/**
	 * 连接ip
	 */
	public final static int LinkIp=6;//连接IP
	
	/**
	 * 失去连接
	 */
	public final static int LostLink = 7;//失去连接
	
	/**
	 * 正在连接
	 */
	public final static int Link=8;//正在连接
	
	/**
	 * 是否启用密匙
	 */
	public final static boolean IsA5 = true;//是否启用密匙
	
	/**
	 * 原始数据
	 */
	public final static int RawData = 9;
	public final static int PwdError = 10;
	public final static int ChangeNameOk = 11;
	
	/**
	 * 没有蓝牙
	 */
	public static  final int NO_BLUETOOTH = 12; 
	
	/**
	 * 没有连接到设备
	 */
	public static final int NO_LINK_DEVICE = 13;
	
	
	/**
	 * deviceList发生了变化
	 */
	public static final int List_Changed = 14;
	
	/**
	 * 没有ip地址
	 */
	public static final int WifiError = 101;

	
	/**
	 * 连接设备 
	 * @param address 连接的地址，局域网内的ip 或者 蓝牙mac
	 * @param pwd 建立连接的密码
	 */
	void connect(String address,String pwd);// 连接
	
	
	/**
	 * 停止连接
	 */
	void stopConncet();// 停止连接
	
	void closeAll();
	
	/**
	 * 直接发送数据
	 * @param buffer
	 */
	void write(byte[] buffer);
	
	void writeNoresponse(byte[] buffer) ;
	
	/**
	 * 将命令生成协议后发送
	 * @param buffer
	 */
	void writeAgreement(byte[] buffer);
	
	/**
	 * 读取数据，数据
	 * @param buffer
	 */
	//void read(byte[] buffer);//读取数据，处理一些和通信有关的数据
	
	/**
	 * 是否已经连接到设备
	 * @return 
	 *   如果连上设备，返回true
	 */
	boolean isLink();//是否已连接
	
	/**
	 * 是否正在连接中
	 * @return
	 */
	boolean isConnecting();
	
	void Reconnect();
	
	
	void onBleDestory();

	void setDataParse(CmdDataParse cmdParse);


	void stopReadThread(String mac);


	void startReadThread(String mac);
}
