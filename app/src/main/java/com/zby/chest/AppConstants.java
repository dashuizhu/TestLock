package com.zby.chest;

import org.apache.http.util.LangUtils;

public class AppConstants {

	/**
	 * 只殴打输入长度
	 */
	public static int password_length = 6;
	public static int name_length = 18;
	
	
	/**
	 * 滑动关锁  距离
	 */
	public static float scrollDistance = 250;
	
	/**
	 * 重发机制，超时时间
	 */
	public static int  ReSendTime = 2000;
	/**
	 *自动连接中，超时次数
	 */
	public static int connecting_count = 10;


	
	/**
	 * 英语
	 */
	public final static int language_en = 1;
	public final static int language_zh = 2;
	public final static int language_default = language_en;

	
	public static final boolean isDemo = true;
	
	/**
	 * 搜索间隔时间
	 */
	public static final int scan_time = 4* 1000;
	
	public static final int readRSSITime  = 1500;
	
	/**
	 * 字符集编码
	 */
	public static final String CharSet = "utf-8";
	
	/**
	 * 长按时间
	 */
	public static final int longClickTime = 1000;
}
