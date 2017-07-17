package com.zby.chest.model;

/**
 * @author Administrator
 *	监听事件的滑动  和点击
 */
public interface DeviceListener {
	void onDeviceScroll(DeviceBean dbin);
	void onDeviceItemClick(DeviceBean dbin);
	void onDeviceLongClick(DeviceBean deviceBean);
	void onDeviceLongLongClick(DeviceBean deviceBean);
}
