package com.zby.chest.utils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Myhex {
	public static void main(String[] args) {
		String senddata = "A1b2";
		byte[] buffer2 = hexStringToByte(senddata);
		showByte(buffer2);
	}
	
	// 将十六进制类型字符串 转换为byte[]
	public static byte[] hexStringToByte(String hex) {
		hex = hex.replace(" ", "");// 去空格
		hex = hex.toUpperCase();// 改为大写
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	private static void showByte(byte[] buffer) {

		List<String> list = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		for (byte b : buffer) {
			list.add(trim(Integer.toHexString(b)).toUpperCase());
			list2.add(b + "");
		}
		// MyHelper.myLog(list.toString());
		// MyHelper.myLog(list2.toString());
		System.out.println(list.toString());
		System.out.println(list2.toString());

	}

	// 整理16进制数
	private static String trim(String str) {
		if (str.length() == 8) {// 去掉补位的f
			str = str.substring(6);
		}
		if (str.length() == 1) {
			str = "0" + str;// 补0
		}
		return str;
	}

	public static boolean vali16Str(String str) {
		str=str.toUpperCase();
		str = str.replace(" ", "");// 去空格
		String s = "0123456789ABCDEF";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int d = s.indexOf(c);
			if (d < 0) {
				return false;
			}

		}
		return true;
	}
	public static String buffer2String(byte[] buffer) {
		if(buffer==null){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		//List<String> list = new ArrayList<String>();
		for (byte b : buffer) {
			sb.append(trim(Integer.toHexString(b)).toUpperCase()+" ");
			//list.add(trim(Integer.toHexString(b)).toUpperCase());
		}
		return sb.toString();
	}
	
	public static String buffer2String(byte[] buffer, int length) {
		if(buffer==null){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		List<String> list = new ArrayList<String>();
		for (int i=0; i<length; i++) {
			sb.append(trim(Integer.toHexString(buffer[i])).toUpperCase()+" ");
			//list.add(trim(Integer.toHexString(b)).toUpperCase());
		}
		return sb.toString();
	}
	
	/**
	 * 去除string中的  空格 换行
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {        
		String dest = "";       
		if (str!=null) {           
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");    
			Matcher m = p.matcher(str);         
			dest = m.replaceAll("");      
			}     
		return dest;  
		}
	
	
	public static int string2int(String str) {
		byte[] buff = hexStringToByte(str);
		if(buff==null) return 0;
		int number;
		if(buff.length==0) {
			number =0;
		} else if(buff.length == 1) {
			number = MyByte.byteToInt(buff[0]);
		} else {
			number = MyByte.byteToInt(buff.length-2) * 256 + MyByte.byteToInt(buff.length-1);
		}
		return number;
	}

}
