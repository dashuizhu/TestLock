package com.zby.chest.agreement;

import com.zby.chest.utils.CrcUtils;
import java.io.UnsupportedEncodingException;

import android.bluetooth.BluetoothClass.Device;

import com.zby.chest.AppConstants;
import com.zby.chest.model.DeviceBean;
import com.zby.chest.utils.MyByte;
import com.zby.chest.utils.Myhex;

public class CmdPackage {

	/**
	 * @param mode
	 *            1 自动开锁 2一键开锁
	 * @return
	 */
	public static byte[] getLockOff(int mode) {
		byte[] buff = new byte[5];
		buff[0] = (byte) 0xCC;
		buff[1] = (byte) 0x01;
		buff[2] = (byte) 0x01;
			buff[3] = (byte) 0xE1;
			buff[4] = (byte) 0xE1;
		return buff;
	}

	/**
	 *            0 自动开锁 1一键开锁
	 * @return
	 */
	public static byte[] getLockOff(String password) {
		byte[] buff = new byte[10];
		buff[0] = (byte) 0xCC;
		buff[1] = (byte) 0x01;
		buff[2] = (byte) 0x06;
		byte[] buffpsd = MyByte.string2bufferO(password);
		System.arraycopy(buffpsd, 0, buff, 3, buffpsd.length);
		return buff;
	}

	public static byte[] getPasswordChange(String password, String newPassword) {
		byte[] buff = new byte[4 + password.length() + newPassword.length()];
		buff[0] = (byte) 0xCC;
		buff[1] = (byte) 0x03;
		buff[2] = (byte) 0x0C;
		//buff[buff.length - 1] = (byte) 0x0F;
		byte[] oldPsd = MyByte.string2bufferO(password);
		byte[] newPsd = MyByte.string2bufferO(newPassword);
		System.arraycopy(oldPsd, 0, buff, 3, oldPsd.length);
		System.arraycopy(newPsd, 0, buff, 3 + oldPsd.length, newPsd.length);

		byte crc = CrcUtils.calcCrc8(buff, 1, buff.length-2);
		buff[buff.length - 1] = crc;
		return buff;
	}

	public static byte[] getLockSet(int type) {
		byte[] buff = new byte[5];
		buff[0] = (byte) 0xCC;
		buff[1] = (byte) 0x10;
		buff[2] = (byte) 0x01;
		switch (type) {
		case DeviceBean.LockMode_scroll:
			buff[3] = (byte) 0xB0;
			buff[4] = (byte) 0xA1;
			break;
		case DeviceBean.LockMode_password:
			buff[3] = (byte) 0xD0;
			buff[4] = (byte) 0xC1;
			break;
		}
		return buff;
	}

	public static byte[] modifyName(String name) {
		byte[] buffer = null;
		try {
			byte[] nameBuff = name.getBytes(AppConstants.CharSet);
			buffer = new byte[20];
			buffer[0] = (byte) 0xCC;
			buffer[19] = (byte) 0x10;
			System.arraycopy(nameBuff, 0, buffer, 1, nameBuff.length);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer;
	}


	/**
	 * 检测连接密码
	 * 
	 * @param password
	 * @return
	 */
	public static byte[] verifyPassword(String password) {
		byte[] buffer = new byte[10];
		buffer[0] = (byte) 0xDD;
		buffer[1] = (byte) 0x01;
		buffer[2] = (byte) 0x06;
		buffer[9] = (byte) 0x07;
		byte[] passBuff = MyByte.string2bufferO(password);
		System.arraycopy(passBuff, 0, buffer, 3, passBuff.length);
		return buffer;
	}

	/**
	 * 修改配对密码
	 * 
	 * @param password
	 * @return
	 */
	public static byte[] modifyVerifyPassword9(String password) {
		byte[] buffer = new byte[10];
		buffer[0] = (byte) 0xDD;
		buffer[1] = (byte) 0x02;
		buffer[2] = (byte) 0x06;
		buffer[9] = (byte) 0x03;
		byte[] passBuff = MyByte.string2bufferO(password);
		System.arraycopy(passBuff, 0, buffer, 3, passBuff.length);
		//byte crc =CrcUtils.calcCrc8(buffer, 1, buffer.length-2);
		//buffer[9] = crc;
		return buffer;
	}

	/**
	 * 解除绑定
	 * 
	 * @param mac
	 * @return
	 */
	public static byte[] removeBindMac(String mac) {
		byte[] buffer = new byte[10];
		buffer[0] = (byte) 0xAA;
		buffer[1] = (byte) 0x02;
		buffer[2] = (byte) 0x06;
		buffer[9] = (byte) 0x03;
		byte[] macBuff = Myhex.hexStringToByte(mac);
		System.arraycopy(macBuff, 0, buffer, 3, macBuff.length);
		return buffer;
	}

	public static byte[] getStopLink() {
		// TODO Auto-generated method stub
		byte[] buff = new byte[6];
		buff[0] = (byte) 0xCC;
		buff[1] = (byte) 0x04;
		buff[2] = (byte) 0x5A;
		buff[3] = (byte) 0x5A;
		buff[4] = (byte) 0xA5;
		buff[5] = (byte) 0xA5;
		return buff;
	}

	public static byte[] getStatus() {
		// TODO Auto-generated method stub
		byte[] buff = new byte[5];
		buff[0] = (byte) 0xCC;
		buff[1] = (byte) 0xA1;
		buff[2] = (byte) 0x01;
		buff[3] = (byte) 0x13;
		buff[4] = (byte) 0x14;
		return buff;
	}

	/**
	 * 获得管理员密码
	 * @param password
	 * @param newPassword
	 * @return
	 */
  public static byte[] getPasswordAdminChange(String password, String newPassword) {
	  byte[] buff = new byte[4 + password.length() + newPassword.length()];
	  buff[0] = (byte) 0xCC;
	  buff[1] = (byte) 0x50;
	  buff[2] = (byte) 0x0C;
	  //buff[buff.length - 1] = (byte) 0x0F;
	  byte[] oldPsd = MyByte.string2bufferO(password);
	  byte[] newPsd = MyByte.string2bufferO(newPassword);
	  System.arraycopy(oldPsd, 0, buff, 3, oldPsd.length);
	  System.arraycopy(newPsd, 0, buff, 3 + oldPsd.length, newPsd.length);
	  byte crc8 = CrcUtils.calcCrc8(buff, 1, buff.length-2);
	  buff[buff.length-1] = crc8;
	  return buff;
  }

	/**
	 * 认证管理员密码
	 * @return
	 */
	public static byte[] verifyAdminPassword(String adminPsd) {
		byte[] buffer = new byte[10];
		buffer[0] = (byte) 0xCC;
		buffer[1] = (byte) 0x30;
		buffer[2] = (byte) 0x06;
		buffer[9] = (byte) 0x00;
       byte[] passBuff = MyByte.string2bufferO(adminPsd);
		System.arraycopy(passBuff, 0, buffer, 3, passBuff.length);
		return buffer;
	}
}
