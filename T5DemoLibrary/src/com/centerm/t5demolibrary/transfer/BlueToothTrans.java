package com.centerm.t5demolibrary.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import com.centerm.t5demolibrary.bluetooth.CachedBluetoothDevice;
import com.centerm.t5demolibrary.bluetooth.LocalBluetoothAdapter;
import com.centerm.t5demolibrary.interfaces.EndOfRead;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * Bluetooth传输
 * 
 * @author JQJ
 *
 */
class BlueToothTrans
{
	private static final String TAG = "BlueToothTrans";
	private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private static final int MSG_TYPE_EVENT_READ = 1002;
	private static final int MSG_TYPE_EVENT_ENABLE = 1003;
	private static final int MSG_TYPE_EVENT_PAIRING = 1004;
	private static final int MSG_TYPE_EVENT_USERCANCEL = 1005;

	private BluetoothDevice mDevice;
	private BluetoothSocket btSocket;
	private OutputStream os;
	private InputStream is;

	private int mReaded = ErrorUtil.RET_READDEVICE_FAILED;
	private int mConneced = ErrorUtil.RET_BLUETOOTH_CLOSED;
	private byte[] mResBuffer = null;
	private boolean userCancel = false;

	private Token mToken;
	private EnableBluetoothThread mEnableThread;
	private PairingThread mPairingThread;
	private TransHandler mHandler;

	BlueToothTrans(Context context)
	{
		mToken = new Token();
		HandlerThread mTransHandlerThread = new HandlerThread(TAG);
		mTransHandlerThread.start();
		Log.d("threadid", "TAG:" + TAG + " id:" + mTransHandlerThread.getId());
		mHandler = new TransHandler(mTransHandlerThread.getLooper());
	}

	private class Token
	{
		private String flag;

		public Token()
		{
			setFlag(null);
		}

		public void setFlag(String flag)
		{
			this.flag = flag;
		}

		public String getFlag()
		{
			return flag;
		}
	}

	private class TransHandler extends Handler
	{
		public TransHandler(Looper looper)
		{
			super(looper);
		}

		@Override
		public void handleMessage(Message msg)
		{
			String str = null;

			switch (msg.what)
			{
			case MSG_TYPE_EVENT_READ:
				str = "read end";
				mReaded = msg.arg1;

				if (0 < mReaded)
				{
					mResBuffer = new byte[mReaded];
					Arrays.fill(mResBuffer, (byte) 0xff);
					System.arraycopy(msg.obj, 0, mResBuffer, 0, mReaded);

					if (ErrorUtil.LOG_DEBUG)
						Log.i(TAG, "handleMessage++接收数据:" + StringUtil.HexToStringA(mResBuffer, mReaded));
				}
				break;

			case MSG_TYPE_EVENT_USERCANCEL:
				str = "user cancel";
				mReaded = msg.arg1;
				if (ErrorUtil.LOG_DEBUG)
					Log.i(TAG, "handleMessage++用户取消");
				break;

			case MSG_TYPE_EVENT_ENABLE:
				str = "openBluetooth end";
				break;

			case MSG_TYPE_EVENT_PAIRING:
				str = "pair end";
				break;
			}

			synchronized (mToken)
			{
				mToken.setFlag(str);
				mToken.notifyAll();
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 判断蓝牙是否关闭、未配对
	 * 
	 * @param mac
	 * @return
	 */
	protected int findDevice(String mac)
	{
		// 找到蓝牙适配器
		LocalBluetoothAdapter mLocalAdapter = LocalBluetoothAdapter.getInstance();
		if (mLocalAdapter == null)
		{
			Log.i(TAG, "没找到本机蓝牙适配器!");
			return ErrorUtil.RET_BLUETOOTH_ADAPTER_NOT_FIND;

		}
		else if (!BluetoothAdapter.checkBluetoothAddress(mac))
		{
			Log.i(TAG, "蓝牙MAC错误!");
			return ErrorUtil.RET_BLUETOOTH_MAC_FAILED;
		}

		// 开启蓝牙
		if (!mLocalAdapter.isEnabled())
		{
			if (mLocalAdapter.enable())
			{
				while (mLocalAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON
						|| mLocalAdapter.getState() != BluetoothAdapter.STATE_ON)
				{
					SystemClock.sleep(100);
				}
			}
			else
			{
				Log.e(TAG, "请先打开蓝牙!");

				// 异步打开
				mConneced = ErrorUtil.RET_BLUETOOTH_CLOSED;
				mHandler.post(mEnableThread);
				if (ErrorUtil.RET_SUCCESS != waitForThread("enable bluetooth", 2000))
				{
					return ErrorUtil.RET_BLUETOOTH_CLOSED;
				}
			}
		}

		// 根据指定MAC进行蓝牙配对
		try
		{
			mDevice = mLocalAdapter.getRemoteDevice(mac);
			if (mDevice == null)
			{
				Log.e(TAG, "没找到相应蓝牙设备");
				return ErrorUtil.RET_BLUETOOTH_DEVICE_NOT_FIND;
			}
			else
			{
				if (mLocalAdapter.isDiscovering())// 取消扫描，否则匹配会不稳定
				{
					mLocalAdapter.cancelDiscovery();
				}

				if (mDevice.getBondState() == BluetoothDevice.BOND_NONE
						|| mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
				{
					Log.i(TAG, "请先进行蓝牙配对!");

					// 异步配对
					mConneced = ErrorUtil.RET_BLUETOOTH_PAIR_CANCLED;
					mHandler.post(mPairingThread);
					if (ErrorUtil.RET_SUCCESS != waitForThread("pairing bluetooth", 50000))
					{
						return ErrorUtil.RET_BLUETOOTH_PAIR_CANCLED;
					}
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			Log.i(TAG, "查找不到对应MAC地址的设备");
			return ErrorUtil.RET_BLUETOOTH_DEVICE_NOT_FIND;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * 打开设备
	 * 
	 * @return
	 */
	protected int openDevice()
	{
		if (mDevice == null)
		{
			return -1;
		}

		int nRet = 0;

		// 如果已经连接，避免重复连接导致的bug
		if (btSocket != null && btSocket.isConnected())
		{
			return nRet;
		}

		try
		{
			UUID uuid = UUID.fromString(SPP_UUID);
			btSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
			if (btSocket != null)
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.i(TAG, "开始连接...");

				btSocket.connect();
				os = btSocket.getOutputStream();
				is = btSocket.getInputStream();
			}
			else
			{
				Log.e(TAG, "createRfcommSocketToServiceRecord error!");
				nRet = ErrorUtil.RET_BLUETOOTH_CLOSED;
			}
		}
		catch (IOException e)
		{
			nRet = ErrorUtil.RET_OPEN_FAILED;
			e.printStackTrace();
		}

		if(0 != nRet)
			closeDevice();

		return nRet;
	}

	protected int waitForThread(String str, int timeout)
	{
		synchronized (mToken)
		{
			try
			{
				mToken.setFlag(str);
				if (ErrorUtil.LOG_DEBUG)
				{
					Log.i(TAG, "openDevice+++the mToken flag value is " + mToken.getFlag());
				}

				mToken.wait(timeout);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Log.e(TAG, "openDevice++连接超时");
				mConneced = ErrorUtil.RET_OPEN_FAILED;
			}
		}

		if (ErrorUtil.LOG_DEBUG)
		{
			Log.i(TAG, "openDevice+++the mToken flag value is " + mToken.getFlag());
		}

		return mConneced;
	}

	/**
	 * 关闭设备
	 * 
	 * @return
	 */
	protected int closeDevice()
	{
		try
		{
			if (is != null)
			{
				is.close();
				is = null;
			}

			if (os != null)
			{
				os.close();
				os = null;
			}

			if (btSocket != null)
			{
				btSocket.close();
				btSocket = null;
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	protected int readBluetooth(byte[] byRes, int nResLen, EndOfRead condition, int timeout)
	{
		int nReaded = 0, nTransferDataLen = 0, nAvailable = 0;
		byte[] byTransferData = null;
		long diff = 0;
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
				return ErrorUtil.RET_CANCELED;
			}

			// 超时控制
			endDate = new Date(System.currentTimeMillis());
			diff = endDate.getTime() - curDate.getTime();
			if (timeout > 0 && diff > timeout)
			{
				return ErrorUtil.RET_TIMEOUT;
			}

			// 接收数据
			nTransferDataLen = 0;
			try
			{
				nAvailable = is.available();
				if (0 >= nAvailable)
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

				byTransferData = null;
				byTransferData = new byte[nAvailable];
				nTransferDataLen = is.read(byTransferData, 0, nAvailable);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				Log.e(TAG, "蓝牙读取数据失败:" + nTransferDataLen);
				nReaded = ErrorUtil.RET_READDEVICE_FAILED;
				break;
			}

			if (nTransferDataLen + nReaded > nResLen)
			{
				Log.e(TAG, "接收数据超过预定长度!");
				return ErrorUtil.RET_SDICCARD_RESPONSE_FAILED;
			}

			if(0 < nTransferDataLen)
			{
				System.arraycopy(byTransferData, 0, byRes, nReaded, nTransferDataLen);
				nReaded += nTransferDataLen;
			}

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

	/**
	 * 发送接收
	 * 
	 * @param byReq
	 * @param byRes
	 * @param condition
	 * @return
	 */
	protected int Transfer(byte[] byReq, int nReqLen, byte[] byRes, int nResLen, EndOfRead condition, int timeout)
	{
		int ret = 0;

		if (btSocket == null || os == null || is == null)
		{
			return ErrorUtil.RET_COMMUNICATE;
		}

		if (ErrorUtil.LOG_DEBUG)
			Log.d(TAG, "write data:" + StringUtil.HexToStringA(byReq, nReqLen));

		try
		{
			os.write(byReq);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeDevice();
			return ErrorUtil.RET_BLUETOOTH_CLOSED;
		}

		ret = readBluetooth(byRes, nResLen, condition, timeout);
		return ret;
	}

	private class EnableBluetoothThread implements Runnable
	{
		@Override
		public void run()
		{
			// 打开蓝牙
			LocalBluetoothAdapter mLocalAdapter;
			mLocalAdapter = LocalBluetoothAdapter.getInstance();
			if (mLocalAdapter != null)
			{
				mLocalAdapter.setBluetoothEnabled(true);
				mConneced = ErrorUtil.RET_SUCCESS;
			}
		}
	}

	private class PairingThread implements Runnable
	{
		@Override
		public void run()
		{
			if (ErrorUtil.LOG_DEBUG)
				Log.i(TAG, "******Pairing++正在配对...");
			if (!CachedBluetoothDevice.createBond(mDevice))
			{
				Log.e(TAG, "createBond失败!");
				mHandler.postDelayed(mEnableThread, 100);
			}
		}
	}

	private class BlutoothConnReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String result = null;
			int state = 0;

			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
			{
				state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				result = updateBluetoothStateChange(state);
			}
			else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
			{
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				state = btDevice.getBondState();
				result = updateBluetoothBondStateChange(state);
			}
			else if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST))
			{
				setPairPin(intent);
			}

			Log.i(TAG, "onReceive+++result:" + result);
		}

		private void setPairPin(Intent intent)
		{
			// 获得设备, 配对类型、随机配对密码
			BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			// 地址
			String address = btDevice.getAddress();
			int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

			if (ErrorUtil.LOG_DEBUG)
				Log.i(TAG, "setPairPin+++type:" + type);

			if (type == BluetoothDevice.PAIRING_VARIANT_PIN)
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++设置配对Pin");

				String pin = "002396";
				byte[] byPin = pin.getBytes();
				btDevice.setPin(byPin);
			}
			else if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION)
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++开始密钥配对");

				CachedBluetoothDevice.setPairingConfirmation(btDevice, true);
			}
			else
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++无法与\"" + address + "\"进行配对:未支持的配对方式");
			}
		}

		private String updateBluetoothBondStateChange(int state)
		{
			String result = null;
			switch (state)
			{
			case BluetoothDevice.BOND_BONDING:
				result = "正在配对...";
				break;
			case BluetoothDevice.BOND_BONDED:
				result = "配对成功";
				mConneced = ErrorUtil.RET_SUCCESS;
				mHandler.sendEmptyMessage(MSG_TYPE_EVENT_PAIRING);
				break;
			case BluetoothDevice.BOND_NONE:
				result = "配对失败";
				mConneced = ErrorUtil.RET_BLUETOOTH_PAIR_CANCLED;
				mHandler.sendEmptyMessage(MSG_TYPE_EVENT_PAIRING);
			default:
				break;
			}

			return result;
		}

		private String updateBluetoothStateChange(int state)
		{
			String result = null;

			switch (state)
			{
			case BluetoothAdapter.STATE_TURNING_ON:
				result = "正在打开...";
				break;
			case BluetoothAdapter.STATE_ON:
				result = "蓝牙已打开";
				// 通知打开完成
				mHandler.sendEmptyMessage(MSG_TYPE_EVENT_ENABLE);
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				result = "正在关闭...";
				break;
			case BluetoothAdapter.STATE_OFF:
				result = "蓝牙已关闭";
				break;
			default:
				break;
			}

			return result;
		}
	}

	protected int Transfer(byte[] byReq, int nReqLen, int timeout)
	{
		int ret = 0;

		if (btSocket == null || os == null || is == null)
		{
			return ErrorUtil.RET_COMMUNICATE;
		}

		try
		{
			os.write(byReq);
			if (TransControl.getInstance().isShortConnect())
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.e(TAG, "短连接等待");

				SystemClock.sleep(1000);
			}

			ret = ErrorUtil.RET_SUCCESS;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeDevice();
			return ErrorUtil.RET_WRITEDEVICE_FAILED;
		}

		return ret;
	};

	protected void setUserCancel(boolean userCancel)
	{
		this.userCancel = userCancel;
	}

	protected boolean isConnected()
	{
		if ((btSocket != null && os != null && is != null && btSocket.isConnected()))
		{
			BluetoothAdapter btAda = BluetoothAdapter.getDefaultAdapter();
			Boolean noConnect = btAda.getState() == BluetoothAdapter.STATE_TURNING_OFF
					|| btAda.getState() == BluetoothAdapter.STATE_OFF
					|| btAda.getState() == BluetoothAdapter.STATE_DISCONNECTED;
			if (noConnect)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
}
