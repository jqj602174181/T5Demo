package com.centerm.t5demolibrary.utils;

public class ErrorUtil
{
	public static final boolean LOG_DEBUG = true;

	// 通信错误码
	public static final int RET_SUCCESS = 0; // 成功
	public static final int RET_OPEN_FAILED = -1; // 打开设备失败
	public static final int RET_WRITEDEVICE_FAILED = -2; // 发送指令失败
	public static final int RET_READDEVICE_FAILED = -3; // 蓝牙接收数据失败
	public static final int RET_PACKAGE_ERROR = -4; // 接收到的报文格式错误
	public static final int RET_TIMEOUT = -5; // 超时
	public static final int RET_CANCELED = -6; // 用户取消
	public static final int RET_DATA_ERROR = -8; // 数据不正确
	public static final int RET_PARAM_ERROR = -9; // 参数不正确
	public static final int RET_COMMUNICATE = -10; // 通信失败
	public static final int RET_OPERATE_FAILED = -11; // 操作失败
	public static final int RET_OUTOF_BUFFER = -12; // 数据越界
	public static final int RET_OUTOF_ARRAYCOPY = -13; // system.arraycopy越界

	// 二代证错误码
	public static final int RET_SEARCH_FAILED = -80; // 寻卡失败
	public static final int RET_SELECT_FAILED = -81;
	public static final int RET_READID_FAILED = -82;

	// USB错误码
	public static final int RET_HAVE_PERMISSION = 201;
	public static final int RET_FIND_DEVICE = -90;
	public static final int RET_FIND_INTERFACE = -91;
	public static final int RET_NO_PERMISSION = -92;
	public static final int RET_GET_EPIN = -93;
	public static final int RET_GET_EPOUT = -94;
	public static final int RET_CLAIMED_FAILED = -95;

	// 蓝牙错误码
	public static final int RET_BLUETOOTH_MAC_FAILED = -100; // MAC地址格式错误
	public static final int RET_BLUETOOTH_CLOSED = -101; // 连接关闭
	public static final int RET_BLUETOOTH_PAIR_CANCLED = -102; // 配对失败
	public static final int RET_BLUETOOTH_ADAPTER_NOT_FIND = -103; // 本机无蓝牙适配器
	public static final int RET_BLUETOOTH_DEVICE_NOT_FIND = -104; // 没找到相应蓝牙设备
	public static final int RET_BLUETOOTH_DATA_OUTOF_BUFFER = -105; // 接收到的数据大于定义的数据

	// 实达IC卡错误码
	public static final int RET_SDICCARD_OPERATE_FAILED = -70; // 操作失败
	public static final int RET_SDICCARD_RESPONSE_FAILED = -71; // 蓝牙读取数组越界

	// 磁条卡错误码
	public static final int RET_READTRACK_FAILED = -110;
	public static final int RET_READSTATUS_FAILED = -111;
	public static final int RET_READDBTRACK_FAILED = -112;
	public static final int RET_READTRACK3_FAILED = -113;
	public static final int RET_READSTATUS_SUCCESS = 114;

	// 位图打印错误码
	public static final int RET_IMAGTYPE_ERROR = -120;
	public static final int RET_IMAG_HEIGHT_ERROR = -121;

	// 打印错误码
	public static final int RET_NOVALUE_ERROR = -124; // 未设置值
	public static final int RET_SYSTEM_ERROR = -122; // 系统错误
	public static final int RET_OUTOFVALUE_ERROR = -123; // 间距值超出长度

	// T5错误码
	public static final byte RET_T5_SAFE_SUCCESS = 111;
	public static final byte RET_T5_FINGER_DATA = 112;
	public static final byte RET_T5_CHECK_FINGER = 113;
	public static final byte RET_T5_JHPWD_SUCCESS = 114;
	public static final byte RET_T5_SIGN_SUCCESS = 115;

	public static final int RET_SAFE_SUCCESS = 122; // 安全认证
	public static final int RET_DEV_SUCCESS = 123; // 设备
	public static final int RET_T5_KEY_SUCCESS = 124; // 未设置值
	public static final int RET_KEY_BLUETOOTH = 125; // 未设置值
	
	public static final int RET_T5_FINGER_SUCCESS = 126; // 指纹特征值获取陈功

	public static final int RET_T5_SAFE_FAILED = -77;
	public static final int RET_T5_FAILED = -60;
	public static final int RET_DEV_FAILED = -61;

	// 注意：错误码大小范围为-128—127

}
