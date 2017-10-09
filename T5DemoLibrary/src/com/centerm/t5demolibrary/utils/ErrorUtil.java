package com.centerm.t5demolibrary.utils;

public class ErrorUtil
{
	public static final boolean LOG_DEBUG = true;

	// ͨ�Ŵ�����
	public static final int RET_SUCCESS = 0; // �ɹ�
	public static final int RET_OPEN_FAILED = -1; // ���豸ʧ��
	public static final int RET_WRITEDEVICE_FAILED = -2; // ����ָ��ʧ��
	public static final int RET_READDEVICE_FAILED = -3; // ������������ʧ��
	public static final int RET_PACKAGE_ERROR = -4; // ���յ��ı��ĸ�ʽ����
	public static final int RET_TIMEOUT = -5; // ��ʱ
	public static final int RET_CANCELED = -6; // �û�ȡ��
	public static final int RET_DATA_ERROR = -8; // ���ݲ���ȷ
	public static final int RET_PARAM_ERROR = -9; // ��������ȷ
	public static final int RET_COMMUNICATE = -10; // ͨ��ʧ��
	public static final int RET_OPERATE_FAILED = -11; // ����ʧ��
	public static final int RET_OUTOF_BUFFER = -12; // ����Խ��
	public static final int RET_OUTOF_ARRAYCOPY = -13; // system.arraycopyԽ��

	// ����֤������
	public static final int RET_SEARCH_FAILED = -80; // Ѱ��ʧ��
	public static final int RET_SELECT_FAILED = -81;
	public static final int RET_READID_FAILED = -82;

	// USB������
	public static final int RET_HAVE_PERMISSION = 201;
	public static final int RET_FIND_DEVICE = -90;
	public static final int RET_FIND_INTERFACE = -91;
	public static final int RET_NO_PERMISSION = -92;
	public static final int RET_GET_EPIN = -93;
	public static final int RET_GET_EPOUT = -94;
	public static final int RET_CLAIMED_FAILED = -95;

	// ����������
	public static final int RET_BLUETOOTH_MAC_FAILED = -100; // MAC��ַ��ʽ����
	public static final int RET_BLUETOOTH_CLOSED = -101; // ���ӹر�
	public static final int RET_BLUETOOTH_PAIR_CANCLED = -102; // ���ʧ��
	public static final int RET_BLUETOOTH_ADAPTER_NOT_FIND = -103; // ����������������
	public static final int RET_BLUETOOTH_DEVICE_NOT_FIND = -104; // û�ҵ���Ӧ�����豸
	public static final int RET_BLUETOOTH_DATA_OUTOF_BUFFER = -105; // ���յ������ݴ��ڶ��������

	// ʵ��IC��������
	public static final int RET_SDICCARD_OPERATE_FAILED = -70; // ����ʧ��
	public static final int RET_SDICCARD_RESPONSE_FAILED = -71; // ������ȡ����Խ��

	// ������������
	public static final int RET_READTRACK_FAILED = -110;
	public static final int RET_READSTATUS_FAILED = -111;
	public static final int RET_READDBTRACK_FAILED = -112;
	public static final int RET_READTRACK3_FAILED = -113;
	public static final int RET_READSTATUS_SUCCESS = 114;

	// λͼ��ӡ������
	public static final int RET_IMAGTYPE_ERROR = -120;
	public static final int RET_IMAG_HEIGHT_ERROR = -121;

	// ��ӡ������
	public static final int RET_NOVALUE_ERROR = -124; // δ����ֵ
	public static final int RET_SYSTEM_ERROR = -122; // ϵͳ����
	public static final int RET_OUTOFVALUE_ERROR = -123; // ���ֵ��������

	// T5������
	public static final byte RET_T5_SAFE_SUCCESS = 111;
	public static final byte RET_T5_FINGER_DATA = 112;
	public static final byte RET_T5_CHECK_FINGER = 113;
	public static final byte RET_T5_JHPWD_SUCCESS = 114;
	public static final byte RET_T5_SIGN_SUCCESS = 115;

	public static final int RET_SAFE_SUCCESS = 122; // ��ȫ��֤
	public static final int RET_DEV_SUCCESS = 123; // �豸
	public static final int RET_T5_KEY_SUCCESS = 124; // δ����ֵ
	public static final int RET_KEY_BLUETOOTH = 125; // δ����ֵ
	
	public static final int RET_T5_FINGER_SUCCESS = 126; // ָ������ֵ��ȡ�¹�

	public static final int RET_T5_SAFE_FAILED = -77;
	public static final int RET_T5_FAILED = -60;
	public static final int RET_DEV_FAILED = -61;

	// ע�⣺�������С��ΧΪ-128��127

}
