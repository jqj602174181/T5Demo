package com.centerm.t5demolibrary.transfer;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import com.centerm.t5demolibrary.interfaces.EndOfRead;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * Hid传输
 * 
 * @author JQJ
 *
 */
class UsbHidTrans
{
	private static final int PKG_SIZE = 1024; //一次接收1024
	// public static final int VendorID = 0x2B46;
	// public static final int ProductID = 0xBE01;
	// public static final int ProductID = 0xBE55;

	private static final int VendorID = 0x2B46;
	private static final int ProductID = 0xBB01;

	private int m_nVID = VendorID;
	private int m_nPID = ProductID;
	private int m_nPKG_SIZE = PKG_SIZE;
	private boolean userCancel = false;

	private static final String TAG = "UsbHidTrans";
	protected static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private Context mContext;

	private UsbManager mUsbManager;
	private UsbDevice mUsbDevice;
	private UsbInterface mInterface;
	private UsbEndpoint epOut, epIn;
	private PendingIntent mPermissionIntent;
	private UsbDeviceConnection mDeviceConnection;

	// hid每次接收数据间隔为2S
	private int mTimeoutDiffReset = 500;

	UsbHidTrans(Context context)
	{
		mContext = context;
		mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

		mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
	}

	/**
	 * 找USB设备:不能更新UI
	 * 
	 * @return
	 */
	protected int tryGetUsbPermission()
	{
		int ret = 0;

		ret = findUsbDevice();
		if (0 != ret)
		{
			return ret;
		}

		// 判断是否有权限
		if (mUsbManager.hasPermission(mUsbDevice))
		{
			return ErrorUtil.RET_HAVE_PERMISSION;
		}
		SystemClock.sleep(500);
		mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
		return ret;
	}

	protected int findUsbDevice()
	{
		int ret = 0;

		if (mUsbManager == null)
		{
			return ErrorUtil.RET_FIND_DEVICE;
		}

		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		if (deviceList.isEmpty())
		{
			return ErrorUtil.RET_FIND_INTERFACE;
		}

		mUsbDevice = null;
		for (UsbDevice device : deviceList.values())
		{
			if (ErrorUtil.LOG_DEBUG)
				Log.d(TAG, "vid:" + device.getVendorId() + " pid:" + device.getProductId());

			if (device.getVendorId() == m_nVID && device.getProductId() == m_nPID)
			{
				mUsbDevice = device;
			}
		}

		if (mUsbDevice == null)
		{
			return ErrorUtil.RET_FIND_INTERFACE;
		}

		return ret;
	}

	/**
	 * 获取接口
	 * 
	 * @return
	 */
	protected int findInterface()
	{
		int ret = 0;
		if (mUsbDevice == null)
		{
			return ErrorUtil.RET_FIND_DEVICE;
		}

		// find the device interface
		for (int i = 0; i < mUsbDevice.getInterfaceCount();)
		{
			// 获取设备接口，一般都是一个接口，你可以打印getInterfaceCount()方法查看接
			// 口的个数，在这个接口上有两个端点，OUT 和 IN
			UsbInterface intf = mUsbDevice.getInterface(i);
			mInterface = intf;
			break;
		}

		if (mInterface == null)
		{
			ret = ErrorUtil.RET_FIND_INTERFACE;
		}

		return ret;
	}

	/**
	 * 检查权限、打开设备、分配端点
	 * 
	 * @return
	 */
	protected int openDevice()
	{
		int ret = 0;
		UsbDeviceConnection connection = null;

		// 超时还没权限就返回
		if (!mUsbManager.hasPermission(mUsbDevice))
		{
			return ErrorUtil.RET_NO_PERMISSION;
		}

		// 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
		connection = mUsbManager.openDevice(mUsbDevice);
		if (connection == null)
		{
			return ErrorUtil.RET_OPEN_FAILED;
		}

		if (connection.claimInterface(mInterface, true))
		{
			mDeviceConnection = connection;
			ret = getEndpoint(mDeviceConnection, mInterface);
		}
		else
		{
			connection.close();
			ret = ErrorUtil.RET_CLAIMED_FAILED;
		}

		return ret;
	}

	/**
	 * 关闭设备
	 * 
	 * @return
	 */
	protected void closeDevice()
	{
		if (mDeviceConnection != null)
		{
			mDeviceConnection.releaseInterface(mInterface);
			mDeviceConnection.close();
			epOut = epIn = null;
		}
	}

	/**
	 * 获取端点
	 * 
	 * @param connection
	 * @param intf
	 * @return
	 */
	private int getEndpoint(UsbDeviceConnection connection, UsbInterface intf)
	{
		int ret = 0;

		if (intf.getEndpoint(1) != null)
			epOut = intf.getEndpoint(1);
		else
			return ErrorUtil.RET_GET_EPOUT;

		if (intf.getEndpoint(0) != null)
			epIn = intf.getEndpoint(0);
		else
			return ErrorUtil.RET_GET_EPIN;

		return ret;
	}

	/**
	 * 发送接收数据
	 * 
	 * @param byReq
	 * @param byRes
	 * @param condition
	 * @return
	 */
	protected int Transfer(byte[] byReq, int nReqLen, int timeout)
	{
		if (epIn == null || epOut == null)
		{
			return ErrorUtil.RET_CANCELED;
		}

		if (nReqLen < 0)
		{
			return ErrorUtil.RET_PACKAGE_ERROR;
		}

		if (ErrorUtil.LOG_DEBUG)
			Log.d(TAG, "byReqLen:" + nReqLen);

		// if (ErrorUtil.LOG_DEBUG)
		// Log.d(TAG, "write data:" + new String(byReq, 0, nReqLen));

		if (ErrorUtil.LOG_DEBUG)
			Log.d(TAG, "write data:" + StringUtil.HexToStringA(byReq, nReqLen));

		// for (int j = 0; j < nReqLen; j++)
		// {
		// Log.i(TAG,
		// "****write data:, byReq[" + j + "]:"
		// + String.format("%02x", byReq[j]));
		// }

		// 发送数据判断是否成功
		int ret = mDeviceConnection.bulkTransfer(epOut, byReq, nReqLen, timeout);
		if (nReqLen != ret)
		{
			Log.e(TAG, "Transfer++发送数据失败");
			return ErrorUtil.RET_WRITEDEVICE_FAILED;
		}

		return ret;
	}

	/**
	 * 发送接收数据
	 * 
	 * @param byReq
	 * @param byRes
	 * @param condition
	 * @return
	 */
	protected int Transfer(byte[] byReq, int nReqLen, byte[] byRes, int nResLen, EndOfRead condition, int timeout)
	{
		if (epIn == null || epOut == null)
		{
			return ErrorUtil.RET_CANCELED;
		}

		if (nReqLen < 0)
		{
			return ErrorUtil.RET_PACKAGE_ERROR;
		}

		if (ErrorUtil.LOG_DEBUG)
			Log.d(TAG, "byReqLen:" + nReqLen);

		// if (ErrorUtil.LOG_DEBUG)
		// Log.d(TAG, "write data:" + new String(byReq, 0, nReqLen));

		if (ErrorUtil.LOG_DEBUG)
			Log.d(TAG, "write data:" + StringUtil.HexToStringA(byReq, nReqLen));

		// for (int j = 0; j < nReqLen; j++)
		// {
		// Log.i(TAG,
		// "****write data:, byReq[" + j + "]:"
		// + String.format("%02x", byReq[j]));
		// }

		//发送数据前清空缓存
		clearCache();

		// 发送数据判断是否成功
		int ret = mDeviceConnection.bulkTransfer(epOut, byReq, nReqLen, timeout);
		if (nReqLen != ret)
		{
			Log.e(TAG, "Transfer++发送数据失败");
			return ErrorUtil.RET_WRITEDEVICE_FAILED;
		}

		// 接收数据
		ret = readHid(byRes, nResLen, condition, timeout);

		return ret;
	}

	protected void clearCache(){ //清空hid缓存
		if(mDeviceConnection == null || epIn == null){
			return;
		}
		byte[] byTransferData = new byte[m_nPKG_SIZE];
		while(true){
			int nTransferDataLen = mDeviceConnection.bulkTransfer(epIn, byTransferData, m_nPKG_SIZE, mTimeoutDiffReset);
			if(nTransferDataLen <= 0){
				break;
			}
		}
	}

	protected int readHid(byte[] byRes, int nResLen, EndOfRead condition, int timeout)
	{
		int nReaded = 0, nTransferDataLen = 0;
		long diff = 0;
		byte[] byTransferData = new byte[m_nPKG_SIZE];
		Date curDate, endDate;

		curDate = new Date(System.currentTimeMillis());
		Arrays.fill(byRes, (byte) 0xff);
		while (true)
		{
			// 正常退出
			if (nReaded >= nResLen)
				break;

			if (userCancel)
			{
				Log.e(TAG, "用户取消!");
				return ErrorUtil.RET_CANCELED;
			}

			if (epIn == null)
			{
				Log.e(TAG, "epIn error!");
				return ErrorUtil.RET_CANCELED;
			}

			// 超时控制
			endDate = new Date(System.currentTimeMillis());
			diff = endDate.getTime() - curDate.getTime();
			if (timeout > 0 && diff > timeout)
			{
				Log.e(TAG, "Transfer++接收数据超时");
				return ErrorUtil.RET_TIMEOUT;
			}

			// 接收数据
			nTransferDataLen = 0;
			Arrays.fill(byTransferData, (byte) 0xff);
			nTransferDataLen = mDeviceConnection.bulkTransfer(epIn, byTransferData, m_nPKG_SIZE, mTimeoutDiffReset);
			if (0 >= nTransferDataLen)
			{
				if(matchCondition(byRes, nReaded, condition))
				{
					break;
				}
				else
				{
					SystemClock.sleep(500);
					continue;
				}
			}

			System.arraycopy(byTransferData, 0, byRes, nReaded, nTransferDataLen);
			nReaded += nTransferDataLen;

			// 判断是否满足结束条件
			if(matchCondition(byRes, nReaded, condition))
				break;
		}

		// 防止CPU占用过高
		SystemClock.sleep(100);
		return nReaded;
	}

	private boolean matchCondition(byte[] byRes, int nReaded, EndOfRead condition)
	{
		if (condition == null)
			return true;

		if(1 > nReaded)
			return false;

		if (condition.reach(byRes, nReaded))
			return true;
		else
			return false;
	}

	protected int getVID()
	{
		return m_nVID;
	}

	protected void setVID(int _nVID)
	{
		this.m_nVID = _nVID;
	}

	protected int getPID()
	{
		return m_nPID;
	}

	protected void setPID(int _nPID)
	{
		this.m_nPID = _nPID;
	}

	protected int getPKG_SIZE()
	{
		return m_nPKG_SIZE;
	}

	protected void setPKG_SIZE(int m_nPKG_SIZE)
	{
		this.m_nPKG_SIZE = m_nPKG_SIZE;
	}

	protected void setUserCancel(boolean userCancel)
	{
		this.userCancel = userCancel;
	}

	protected boolean isConnected()
	{
		return (epIn != null && epOut != null);
	}
}
