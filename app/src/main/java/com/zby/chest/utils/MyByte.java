package com.zby.chest.utils;

import java.util.ArrayList;
import java.util.List;

public class MyByte {
	public static String[] byte2String;

	//buffer[] 2 String
	public static String buffer2String(byte[] buffer) {
		if(buffer==null){
			return "";
		}
		//List<String> list = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (byte b : buffer) {
			sb.append(trim(Integer.toHexString(b)).toUpperCase()+" ");
		}
		
		return sb.toString();
	}
	
	public static String byte2String(byte b) {
		return trim(Integer.toHexString(b)).toUpperCase();
	}
	
	public static String byte2String(int b) {
		return trim(Integer.toHexString(b)).toUpperCase();
	}
	private static String trim(String str) {
		if (str.length() == 8) {// 去掉补位的f
			str = str.substring(6);
		}
		if (str.length() == 1) {
			str = "0" + str;// 补0
		}
		return str;
	}
	public static void main(String[] args) {
		byte[] buffer=int2TwoByte(2012);
	//	System.out.println(ShowBuffer.showByte(buffer));
		byte b=(byte) (2012%256);
		System.out.println(b);
		byte[] buffer2=new byte[]{
				1,1,1,1,2,0,0,0,0,
		};
		byte[] buffer3=forIndex(buffer2, 5);
		for(byte b1:buffer3){
			System.out.print(b1+",");
		}
	}
	
	public static byte[] int2TwoByte(int i){
		byte[] buffer=new byte[2];
		buffer[0]=(byte) (i/256);
		buffer[1]=(byte) (i%256);
		return buffer;
	}
	public static byte[] forIndex(byte[] buffer, int index ){
		if(index<1){
			return null;
		}
		byte[] buffer2=new byte[index];
		for(int i=0;i<index;i++){
			buffer2[i]=buffer[i];
		}
		return buffer2;
	}
	public static int byteToInt(byte b) {
		int iout = 0;
		if (b >= 0) {
			iout = b;
		} else {
			iout = b + 256;
		}
		return iout;

	}

	public static int byteToInt(int b) {
		
		int iout = 0;
		if (b >= 0) {
			iout = b;
		} else {
			iout = b + 256;
		}
		return iout;
	}
	public static List<Integer> getList(int i) {
		List<Integer> temp = new ArrayList<Integer>();
		temp.add(i % 2);
		temp.add(i % 4 / 2);
		temp.add(i % 8 / 4);
		temp.add(i % 16 / 8);
		temp.add(i % 32 / 16);
		temp.add(i % 64 / 32);
		temp.add(i % 128 / 64);
		temp.add(i % 256 / 128);
		return temp;
	}
	public static int getInt(List<Integer> isCheckedList) {
		int d = 0;
		int i = 1;
		for (int n : isCheckedList) {
			d += i * n;
			i = i * 2;
		}
		return d;
	}
	//0为最顶为 7为最高位
	public static int getOne(int data,int index){
		List<Integer> list=getList(data);
		return list.get(index);
	}
	public static int setOne(int data,int set,int index){
		List<Integer> list=getList(data);
		list.set(index, set);
		return getInt(list);
	}
	public static int two2One(byte b, byte c) {
		int i=byteToInt(b);
		int i2=byteToInt(c);
		return i*256+i2;
	}
	public static byte[] list2buffer(List<Byte> list) {
		byte[] buffer=new byte[list.size()];
		for(int i=0;i<list.size();i++){
			buffer[i]=list.get(i);
		}
		return buffer;
	}
	public static String list2buffer2(List<Integer> list) {
		byte[] buffer=new byte[list.size()];
		for(int i=0;i<list.size();i++){
			int i2=list.get(i);
			byte b=(byte) i2;
			buffer[i]=b;
		}
		
		return buffer2String(buffer);
	}
	
	
	/**
	 * 10进制 直接转换, '123'  = 0x01 0x02 0x03
	 * @param str 
	 * @return
	 */
	public static byte[]  string2bufferO(String str) {
		if(str ==null) 
			return new byte[]{};
		byte[]  buffer = new byte[str.length()];
		for(int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			buffer[i] = (byte) Integer.parseInt(c+"");
		}
		return buffer;
  	}

	public static byte[]  string2buffer16(String str) {
		if(str ==null)
			return new byte[]{};
		byte[]  buffer = new byte[str.length()];
		for(int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			buffer[i] = (byte) c;
		}
		return buffer;
	}


}
