package org.wp.activity;

import android.app.Application;

/**
 * 在开发应用时都会和Activity打交道，而Application使用的就相对较少了。
 * Application是用来管理应用程序的全局状态的，比如载入资源文件。
 * 在应用程序启动的时候Application会首先创建，然后才会根据情况(Intent)启动相应的Activity或者Service。
 * 在本文将在Application中注册未捕获异常处理器。
 */
public class CrashApplication extends Application {
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// 异常处理，不需要处理时注释掉这两句即可！  
	    CrashHandler crashHandler = CrashHandler.getInstance();   
	    // 注册crashHandler   
	    crashHandler.init(getApplicationContext());   

	    /**
	     * 这里有个问题， 要判断网络状况，  在这里调用，就只是在启动软件的时候，发送以前的错误信息到服务器，
	     * 如果要及时的  发生错误就发送信息，  就得将这个sendPreviousReportsToServer函数在handlerException中调用
	     */
		//发送错误报告到服务器 
		crashHandler.sendPreviousReportsToServer();
	          
	}
}