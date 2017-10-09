package com.centerm.t5demolibrary.device.idcard;

import java.io.Serializable;

public class PersonInfo implements Serializable
{

	private static final long serialVersionUID = -6867892445849581318L;

	public PersonInfo()
	{
	}

	public byte[] name = new byte[32]; // 姓名
	public byte[] sex = new byte[4]; // 性别
	public byte[] nation = new byte[20]; // 国籍
	public byte[] birthday = new byte[12]; // 生日
	public byte[] address = new byte[72]; // 地址
	public byte[] cardId = new byte[20]; // 身份证号
	public byte[] police = new byte[64]; // 签发机关
	public byte[] validStart = new byte[12]; // 有效期起始日期
	public byte[] validEnd = new byte[12]; // 有效期截止日期
	public byte[] sexCode = new byte[4]; // 性别代码
	public byte[] nationCode = new byte[4]; // 民族代码
	public int photosize; // 照片数据大小
	public byte[] photo = new byte[1024]; // 照片数据
	public byte[] fingerdata = new byte[1024]; // 指纹数据
}
