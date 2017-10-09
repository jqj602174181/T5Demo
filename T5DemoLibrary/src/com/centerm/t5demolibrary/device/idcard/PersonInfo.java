package com.centerm.t5demolibrary.device.idcard;

import java.io.Serializable;

public class PersonInfo implements Serializable
{

	private static final long serialVersionUID = -6867892445849581318L;

	public PersonInfo()
	{
	}

	public byte[] name = new byte[32]; // ����
	public byte[] sex = new byte[4]; // �Ա�
	public byte[] nation = new byte[20]; // ����
	public byte[] birthday = new byte[12]; // ����
	public byte[] address = new byte[72]; // ��ַ
	public byte[] cardId = new byte[20]; // ���֤��
	public byte[] police = new byte[64]; // ǩ������
	public byte[] validStart = new byte[12]; // ��Ч����ʼ����
	public byte[] validEnd = new byte[12]; // ��Ч�ڽ�ֹ����
	public byte[] sexCode = new byte[4]; // �Ա����
	public byte[] nationCode = new byte[4]; // �������
	public int photosize; // ��Ƭ���ݴ�С
	public byte[] photo = new byte[1024]; // ��Ƭ����
	public byte[] fingerdata = new byte[1024]; // ָ������
}
