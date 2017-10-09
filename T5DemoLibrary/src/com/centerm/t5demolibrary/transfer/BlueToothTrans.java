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
 * Bluetooth����
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
						Log.i(TAG, "handleMessage++��������:" + StringUtil.HexToStringA(mResBuffer, mReaded));
				}
				break;

			case MSG_TYPE_EVENT_USERCANCEL:
				str = "user cancel";
				mReaded = msg.arg1;
				if (ErrorUtil.LOG_DEBUG)
					Log.i(TAG, "handleMessage++�û�ȡ��");
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
	 * �ж������Ƿ�رա�δ���
	 * 
	 * @param mac
	 * @return
	 */
	protected int findDevice(String mac)
	{
		// �ҵ�����������
		LocalBluetoothAdapter mLocalAdapter = LocalBluetoothAdapter.getInstance();
		if (mLocalAdapter == null)
		{
			Log.i(TAG, "û�ҵ���������������!");
			return ErrorUtil.RET_BLUETOOTH_ADAPTER_NOT_FIND;

		}
		else if (!BluetoothAdapter.checkBluetoothAddress(mac))
		{
			Log.i(TAG, "����MAC����!");
			return ErrorUtil.RET_BLUETOOTH_MAC_FAILED;
		}

		// ��������
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
				Log.e(TAG, "���ȴ�����!");

				// �첽��
				mConneced = ErrorUtil.RET_BLUETOOTH_CLOSED;
				mHandler.post(mEnableThread);
				if (ErrorUtil.RET_SUCCESS != waitForThread("enable bluetooth", 2000))
				{
					return ErrorUtil.RET_BLUETOOTH_CLOSED;
				}
			}
		}

		// ����ָ��MAC�����������
		try
		{
			mDevice = mLocalAdapter.getRemoteDevice(mac);
			if (mDevice == null)
			{
				Log.e(TAG, "û�ҵ���Ӧ�����豸");
				return ErrorUtil.RET_BLUETOOTH_DEVICE_NOT_FIND;
			}
			else
			{
				if (mLocalAdapter.isDiscovering())// ȡ��ɨ�裬����ƥ��᲻�ȶ�
				{
					mLocalAdapter.cancelDiscovery();
				}

				if (mDevice.getBondState() == BluetoothDevice.BOND_NONE
						|| mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
				{
					Log.i(TAG, "���Ƚ����������!");

					// �첽���
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
			Log.i(TAG, "���Ҳ�����ӦMAC��ַ���豸");
			return ErrorUtil.RET_BLUETOOTH_DEVICE_NOT_FIND;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * ���豸
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

		// ����Ѿ����ӣ������ظ����ӵ��µ�bug
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
					Log.i(TAG, "��ʼ����...");

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
				Log.e(TAG, "openDevice++���ӳ�ʱ");
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
	 * �ر��豸
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
			// �����˳�
			if (nReaded >= nResLen)
				break;

			if (userCancel)
			{
				return ErrorUtil.RET_CANCELED;
			}

			// ��ʱ����
			endDate = new Date(System.currentTimeMillis());
			diff = endDate.getTime() - curDate.getTime();
			if (timeout > 0 && diff > timeout)
			{
				return ErrorUtil.RET_TIMEOUT;
			}

			// ��������
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
				Log.e(TAG, "������ȡ����ʧ��:" + nTransferDataLen);
				nReaded = ErrorUtil.RET_READDEVICE_FAILED;
				break;
			}

			if (nTransferDataLen + nReaded > nResLen)
			{
				Log.e(TAG, "�������ݳ���Ԥ������!");
				return ErrorUtil.RET_SDICCARD_RESPONSE_FAILED;
			}

			if(0 < nTransferDataLen)
			{
				System.arraycopy(byTransferData, 0, byRes, nReaded, nTransferDataLen);
				nReaded += nTransferDataLen;
			}

			// �ж��Ƿ������������
			if(matchCondition(byRes, nReaded, condition))
				break;
		}

		// ��ֹCPUռ�ù���
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
	 * ���ͽ���
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
			// ������
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
				Log.i(TAG, "******Pairing++�������...");
			if (!CachedBluetoothDevice.createBond(mDevice))
			{
				Log.e(TAG, "createBondʧ��!");
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
			// ����豸, ������͡�����������
			BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			// ��ַ
			String address = btDevice.getAddress();
			int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

			if (ErrorUtil.LOG_DEBUG)
				Log.i(TAG, "setPairPin+++type:" + type);

			if (type == BluetoothDevice.PAIRING_VARIANT_PIN)
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++�������Pin");

				String pin = "002396";
				byte[] byPin = pin.getBytes();
				btDevice.setPin(byPin);
			}
			else if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION)
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++��ʼ��Կ���");

				CachedBluetoothDevice.setPairingConfirmation(btDevice, true);
			}
			else
			{
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++�޷���\"" + address + "\"�������:δ֧�ֵ���Է�ʽ");
			}
		}

		private String updateBluetoothBondStateChange(int state)
		{
			String result = null;
			switch (state)
			{
			case BluetoothDevice.BOND_BONDING:
				result = "�������...";
				break;
			case BluetoothDevice.BOND_BONDED:
				result = "��Գɹ�";
				mConneced = ErrorUtil.RET_SUCCESS;
				mHandler.sendEmptyMessage(MSG_TYPE_EVENT_PAIRING);
				break;
			case BluetoothDevice.BOND_NONE:
				result = "���ʧ��";
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
				result = "���ڴ�...";
				break;
			case BluetoothAdapter.STATE_ON:
				result = "�����Ѵ�";
				// ֪ͨ�����
				mHandler.sendEmptyMessage(MSG_TYPE_EVENT_ENABLE);
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				result = "���ڹر�...";
				break;
			case BluetoothAdapter.STATE_OFF:
				result = "�����ѹر�";
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
					Log.e(TAG, "�����ӵȴ�");

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
