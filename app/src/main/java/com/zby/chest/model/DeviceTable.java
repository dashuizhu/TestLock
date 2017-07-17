package com.zby.chest.model;

public class DeviceTable {
	
	protected static final String Table_Name= "SceneModeTable";
	
	
	protected static final String Id = "id";
	protected static final String Name="name";//名字
	protected static final String Mac = "mac";//所属于的设备
	protected static final String Password = "password";//开锁密码
	protected static final String PairPassword = "pairPassword";//配对密码
	
	protected static final String LockType = "lockType";
	
	
	/**
	 * @return sql 建表语句
	 */
	protected static final String getTable() {
		String table = "create Table " + Table_Name + " (" +
				Id + " integer primary key autoincrement ," +
				Name +" text," +
				Mac + " text,"+
				LockType + " integer, " +
				PairPassword + " text," +
				Password + " text)";
		return table;
	}
}
