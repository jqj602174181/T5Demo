package com.centerm.t5demolibrary.transfer;

import com.centerm.t5demolibrary.bluetooth.BluetoothEnabler;
import com.centerm.t5demolibrary.bluetooth.CachedBluetoothDevice;
import com.centerm.t5demolibrary.interfaces.EndOfRead;
import com.centerm.t5demolibrary.interfaces.OnMesBackListener;
import com.centerm.t5demolibrary.utils.ErrorUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

public class TransControl{

	private static final String TAG = "TransControl";

	public static final int DEV_USBHID = 1;
	public static final int DEV_BLUETOOTH = 2;

	private int mDevType = DEV_USBHID;
	private int timeout = 5000;
	private int tempTimeout = 5;

	private static final int MSG_TYPE_BLUETOOTH_PAIR = 11;
	private static final int MSG_TYPE_FIND_USB = 12;
	private static final int MSG_TYPE_BLUETOOTH_ENABLE = 14;

	private UsbHidTrans mUsbHid = null;
	private BlueToothTrans mBlueTooth = null;
	private UsbConnReceiver mUsbReceiver = null;
	private BluetoothConnReceiver mBluetoothReceiver = null;
	private boolean isShortConnect = false;

	private UpdateHandler mHandler;
	private BluetoothEnabler mBluetoothEnabler;
	private OnMesBackListener mOnMesBackListener;
	private Context mContext;

	private static TransControl instance;

	private String remoteMac = null;

	private TransControl() {

	}

	public static TransControl getInstance()
	{
		if (instance == null)
		{
			instance = new TransControl();
		}
		return instance;
	}

	/**
	 * 是否短连接
	 * @return
	 */
	public boolean isShortConnect()
	{
		return isShortConnect;
	}

	/**
	 * 设置连接方式  false长连接,  true短连接
	 * @param isShortConnect
	 */
	public void setShortConnect(boolean isShortConnect)
	{
		this.isShortConnect = isShortConnect;
	}

	/**
	 * 设备初始化，传递上下文Context, OnMesBackListener回调
	 * 
	 * @param context 上下文 
	 * @param listener OnMesBackListener回调
	 */
	public void init(Context context, @NonNull OnMesBackListener listener)
	{
		mContext = context;
		mOnMesBackListener = listener;
		mUsbHid = new UsbHidTrans(context);
		mBlueTooth = new BlueToothTrans(context);
		HandlerThread mWayHandlerThread = new HandlerThread(TAG);
		mWayHandlerThread.start();
		mHandler = new UpdateHandler(mContext, mWayHandlerThread.getLooper());
		mBluetoothEnabler = new BluetoothEnabler(mContext);
		initDevice();
	}

	/**
	 * 设备资源释放
	 */
	public void unInit(){
		if (mBluetoothReceiver != null)
			mContext.unregisterReceiver(mBluetoothReceiver);

		if (mUsbReceiver != null)
			mContext.unregisterReceiver(mUsbReceiver);
	}

	/**
	 * 获取当前连接类型
	 * @return mDevType
	 */
	public int getDevType()
	{
		return mDevType;
	}

	/**
	 * 设置当前连接类型
	 * @param mDevType
	 */
	public void setDevType(int mDevType)
	{
		this.mDevType = mDevType;
	}

	/**
	 * 设置超时时间
	 * @param nTimeOut
	 */
	public void setTimeOut(int nTimeOut)
	{
		this.timeout = ((nTimeOut+2) * 1000);
		this.tempTimeout = nTimeOut;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout()
	{
		return tempTimeout;
	}

	/**
	 *  设置Vid
	 * @param _nVID
	 */
	public void setVID(int _nVID)
	{
		mUsbHid.setVID(_nVID);
	}

	/**
	 * 设置Pid
	 * @param _nPID
	 */
	public void setPID(int _nPID)
	{
		mUsbHid.setPID(_nPID);
	}

	public void setPKG_SIZE(int m_nPKG_SIZE)
	{
		mUsbHid.setPKG_SIZE(m_nPKG_SIZE);
	}

	/**
	 * 当前通信是否连接
	 * @return true or false
	 */
	public boolean isConected()
	{
		boolean isConnected = false;
		switch (mDevType)
		{
		case DEV_USBHID:
			isConnected = mUsbHid.isConnected();
			break;
		case DEV_BLUETOOTH:
			isConnected = mBlueTooth.isConnected();
			break;
		default:
			break;
		}

		return isConnected;
	}

	public int readData(byte[] byRes, int nResLen, EndOfRead condition)
	{
		int ret = 0;
		switch (mDevType)
		{
		case DEV_USBHID:
			ret = mUsbHid.readHid(byRes, nResLen, condition, timeout);
			break;
		case DEV_BLUETOOTH:
			ret = mBlueTooth.readBluetooth(byRes, nResLen, condition, timeout);
			break;
		default:
			break;
		}

		return ret;
	}

	/**
	 * 发送接收
	 * 
	 * @param byReq
	 * @param byRes
	 * @param condition
	 * @return
	 */
	public int Transfer(byte[] byReq, int nReqLen, byte[] byRes, int nResLen, EndOfRead condition)
	{
		int ret = 0;

		// 打开设备
		ret = openDev();
		if (ErrorUtil.RET_SUCCESS != ret)
		{
			return ret;
		}

		switch (mDevType)
		{
		case DEV_USBHID:
			ret = mUsbHid.Transfer(byReq, nReqLen, byRes, nResLen, condition, timeout);
			break;
		case DEV_BLUETOOTH:
			ret = mBlueTooth.Transfer(byReq, nReqLen, byRes, nResLen, condition, timeout);
			break;
		default:
			break;
		}

		closeDev();
		return ret;
	}

	/**
	 * 发送接收
	 * 
	 * @param byReq
	 * @param byRes
	 * @param condition
	 * @return
	 */
	public int Transfer(byte[] byReq, int nReqLen)
	{
		int ret = 0;

		// 打开设备
		ret = openDev();
		if (ErrorUtil.RET_SUCCESS != ret)
		{
			return ret;
		}

		switch (mDevType)
		{
		case DEV_USBHID:
			ret = mUsbHid.Transfer(byReq, nReqLen, timeout);
			break;
		case DEV_BLUETOOTH:
			ret = mBlueTooth.Transfer(byReq, nReqLen, timeout);
			break;
		default:
			break;
		}

		// 关闭设备
		closeDev();
		return ret;
	}

	/**
	 *  数据接收是否中断
	 * @param userCancel
	 */
	public void setUserCancel(boolean userCancel)
	{
		switch (mDevType)
		{
		case DEV_USBHID:
			mUsbHid.setUserCancel(userCancel);
			break;
		case DEV_BLUETOOTH:
			mBlueTooth.setUserCancel(userCancel);
			break;
		default:
			break;
		}
	}

	private int openDev()
	{
		int ret = 0;

		boolean isConected = isConected();

		if (ErrorUtil.LOG_DEBUG)
		{
			Log.v(TAG, "isConected=" + isConected);
			Log.v(TAG, "mDevType=" + mDevType);
		}

		// 如果是短连接，要记得关闭
		if (!isConected)
		{
			ret = realOpenDev();
		}

		return ret;
	}

	private int realOpenDev()
	{

		int ret = 0;

		switch (mDevType)
		{
		case DEV_USBHID:
			ret = openUsbHid();
			break;
		case DEV_BLUETOOTH:
			ret = openBlueTooth(remoteMac);
			break;
		default:
			break;
		}

		return ret;
	}

	/**
	 * 打开USB HID
	 * 
	 * @return
	 */
	private int openUsbHid()
	{
		int ret = 0;

		ret = mUsbHid.findUsbDevice();
		if (0 != ret)
		{
			return ret;
		}

		ret = mUsbHid.findInterface();
		if (ret != 0)
		{
			Log.e(TAG, "findInterface error");
			return ErrorUtil.RET_FIND_DEVICE;
		}

		ret = mUsbHid.openDevice();
		if (ret != 0)
		{
			Log.e(TAG, "openDevice error");
			return ErrorUtil.RET_OPEN_FAILED;
		}

		return 0;
	}

	/**
	 * 打开蓝牙
	 * 
	 * @param mac
	 * @return
	 */
	private int openBlueTooth(String mac)
	{
		int nTry = 3, ret = 0;

		// 判断蓝牙是否关闭、未配对
		ret = mBlueTooth.findDevice(mac);
		if (ret != 0)
		{
			return ret;
		}

		for (int i = 0; i < nTry; i++)
		{
			ret = mBlueTooth.openDevice();
			if (ret == 0)
			{
				break;
			}

			ret = ErrorUtil.RET_OPEN_FAILED;
			try
			{
				Thread.sleep(300);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		return ret;
	}

	/**
	 * 关闭设备
	 * 
	 * @return
	 */
	private void closeDev()
	{
		if (isShortConnect)
		{
			realCloseDev();
		}
	}

	private void realCloseDev()
	{
		if (isConected())
		{
			switch (mDevType)
			{
			case DEV_USBHID:
				mUsbHid.closeDevice();
				break;

			case DEV_BLUETOOTH:
				mBlueTooth.closeDevice();
				break;
			default:
				break;
			}
		}
	}

	/******************************************蓝牙模块相关代码****************************************************/

	/**
	 *  通过设备蓝牙mac连接
	 * @param mac
	 */
	public void connectByBluetooth(String mac) {
		// 蓝牙通信设置是否可用
		remoteMac = mac;
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			if (ErrorUtil.LOG_DEBUG) {
				Log.i(TAG, "bluetoothAdapter == null");
			}
		} else {
			mHandler.sendEmptyMessage(MSG_TYPE_BLUETOOTH_PAIR);
		}		
	}

	private void setBluetoothEnable()
	{
		boolean isEnable = true;
		mBluetoothEnabler.setBluetoothStatus(isEnable);
	}

	private void handleBluetoothPairing()
	{
		mHandler.sendEmptyMessage(MSG_TYPE_BLUETOOTH_PAIR);
	}

	private class UpdateHandler extends Handler
	{

		private Context mContext;

		public UpdateHandler(Context context, Looper looper_)
		{
			super(looper_);
			this.mContext = context;
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case MSG_TYPE_FIND_USB:
				findUsbHid();
				break;
			case MSG_TYPE_BLUETOOTH_PAIR:
				findBlueTooth();
				break;
			case MSG_TYPE_BLUETOOTH_ENABLE:
				setBluetoothEnable();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	// 不能更新UI
	private void findBlueTooth()
	{
		String result;
		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG, "findBlueTooth++++");
		int nRet = mBluetoothEnabler.pairing(remoteMac);
		switch (nRet) {
		case -1:
			result = "请先打开蓝牙!";
			mHandler.sendEmptyMessage(MSG_TYPE_BLUETOOTH_ENABLE);
			break;
		case -2:
			result = "蓝牙MAC错误";
			break;
		case 1:
			result = "正在配对...";
			break;
		default:
			result = "配对成功";
			break;
		}

		if(mOnMesBackListener != null){
			mOnMesBackListener.setBluetoothStatus(result);
		}
	};

	/**
	 * 找USB设备:不能更新UI
	 * 
	 * @return
	 */
	private void findUsbHid()
	{
		String result = null;
		if (mUsbHid == null)
			return;
		int nRet = mUsbHid.tryGetUsbPermission();
		switch (nRet) {
		case ErrorUtil.RET_FIND_DEVICE:
			result = "不支持USB!";
			break;

		case ErrorUtil.RET_FIND_INTERFACE:
			result = "USB设备未连接!";
			break;

		case ErrorUtil.RET_HAVE_PERMISSION:
			result = "USB授权成功";
			break;

		default:
			result = "正在进行USB授权...";
			break;
		}

		if(mOnMesBackListener != null){
			mOnMesBackListener.setUsbStatus(result);
		}
		return;
	}

	/***********************************************USB hid处理************************************************/

	/**
	 * 通过hid连接设备
	 */
	public void connectByHid() {
		mHandler.sendEmptyMessage(MSG_TYPE_FIND_USB);
	}

	private void initDevice()
	{
		// USB插拔监听处理
		mUsbReceiver = new UsbConnReceiver();
		IntentFilter mUSBFilter = new IntentFilter(UsbHidTrans.ACTION_USB_PERMISSION);
		mUSBFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		mUSBFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mUSBFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		mContext.registerReceiver(mUsbReceiver, mUSBFilter);

		// 蓝牙状态监听处理
		mBluetoothReceiver = new BluetoothConnReceiver();
		IntentFilter mBluetoothFilter;
		mBluetoothFilter = new IntentFilter(
				BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mBluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mBluetoothFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
		mContext.registerReceiver(mBluetoothReceiver, mBluetoothFilter);
	}

	/**
	 * @Fields mUsbReceiver : usb权限接收器
	 */
	private class UsbConnReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String strUsbStat = null;
			String action = intent.getAction();
			if (UsbHidTrans.ACTION_USB_PERMISSION.equals(action))
			{
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
					strUsbStat = "USB授权成功";
				}else{
					strUsbStat = "USB授权失败";
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)
					|| UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
				if (ErrorUtil.LOG_DEBUG){
					Log.i(TAG, "ACTION_USB_DEVICE_DETACHED++监听USB线拔出");
				}
				UsbAccessory access = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (null == access){
					strUsbStat = "USB设备未连接!";
				}
			} else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				if (ErrorUtil.LOG_DEBUG){
					Log.i(TAG, "ACTION_USB_DEVICE_ATTACHED++监听USB线插入");
				}
				UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null) {
					if(mDevType == DEV_USBHID){
						strUsbStat = "USB设备已连接，点击授权";
						int msgType = MSG_TYPE_FIND_USB;
						mHandler.sendEmptyMessage(msgType);
					}
				}
			}

			if(mOnMesBackListener != null){
				mOnMesBackListener.setUsbStatus(strUsbStat);
			}
		}
	};

	private class BluetoothConnReceiver extends BroadcastReceiver
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
				if(mOnMesBackListener != null){
					mOnMesBackListener.setBluetoothStatus(result);
				}
			} else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				state = btDevice.getBondState();
				result = updateBluetoothBondStateChange(state);
				if(mOnMesBackListener != null){
					mOnMesBackListener.setBluetoothStatus(result);
				}
			} else if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
				setPairPin(intent);
			}
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
			} else if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
				if (ErrorUtil.LOG_DEBUG)
					Log.d(TAG, "setPairPin+++开始密钥配对");
				CachedBluetoothDevice.setPairingConfirmation(btDevice, true);
			} else {
				if (ErrorUtil.LOG_DEBUG){
					Log.d(TAG, "setPairPin+++无法与\"" + address + "\"进行配对:未支持的配对方式");
				}
			}
		}

		private String updateBluetoothBondStateChange(int state)
		{
			String result = null;
			switch (state) {
			case BluetoothDevice.BOND_BONDING:
				result = "正在配对...";
				break;
			case BluetoothDevice.BOND_BONDED:
				result = "配对成功";
				break;
			case BluetoothDevice.BOND_NONE:
				result = "取消配对或配对失败";
			default:
				break;
			}
			return result;
		}

		private String updateBluetoothStateChange(int state)
		{
			String result = null;
			switch (state) {
			case BluetoothAdapter.STATE_TURNING_ON:
				result = "正在打开...";
				break;
			case BluetoothAdapter.STATE_ON:
				result = "蓝牙已打开";
				handleBluetoothPairing();
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
	};
}