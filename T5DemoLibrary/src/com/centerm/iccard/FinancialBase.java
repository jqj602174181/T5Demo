package com.centerm.iccard;

import com.centerm.t5demolibrary.interfaces.EndOfRead;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.RetUtil;
import com.centerm.t5demolibrary.utils.StringUtil;

import android.util.Log;

public abstract class FinancialBase
{

	private static final String TAG = "FinancialBase";
	protected final static int SUCCESS = 0;
	protected final static String sRight = "0";
	protected final static String sError = "1";
	protected ReadThread readThread;
	protected SendThread sendThread;

	protected final int packLength = 1024;

	protected abstract int readData(byte[] data, int len, int timeOut);

	protected abstract int writeData(byte[] data, int len);

	protected final static String split = "@";

	protected boolean isSendThreadStop = false;
	protected boolean isReadThreadStop = false;
	protected boolean isSendLock = false;
	protected final static int timeDelay = 5;

	protected boolean isSendQuit = false;// 用于判断发送是否结束
	protected boolean isReadQuit = false;// 用于判断读取是否结束

	protected static final int T5_STARTPLAYDEMO_ERROR = -302; // 开启卡机动画失败
	protected static final int T5_CLOSEPLAYDEMO_ERROR = -303; // 关闭卡机动画失败

	public FinancialBase()
	{

	}

	protected String[] getParamErr()
	{

		String[] errList = new String[2];
		errList[0] = sError;
		errList[1] = RetUtil.Param_Err;

		return errList;
	}

	protected int getRealReadedLength(byte[] byRes, int byResLength)
	{
		if (TransControl.getInstance().getDevType() == TransControl.DEV_BLUETOOTH)
		{// 蓝牙通信
			return byResLength;
		} else if (TransControl.getInstance().getDevType() == TransControl.DEV_USBHID)
		{
			// hid,每次获取的数据都是1024个字节,要从最后一个字节往前算，直到不是空字符的时候就代表有效数据的长度
			int realReadedLength = byRes.length;
			for (int i = byRes.length - 1; i >= 0; i--)
			{
				if (byRes[i] == (byte) 0x00)
				{
					realReadedLength--;
				} else
				{
					return realReadedLength;
				}
			}
			return realReadedLength;
		}

		return byResLength;

	}

	public abstract byte[] getFinancialOpenCommad();

	public abstract byte[] getFinancialCloseCommad();

	protected boolean isSuccess(int len, byte[] data)
	{
		// Log.e( "isSuccess","len= "+ len +
		// " ,data:"+StringUtil.bytesToHexString(data ) );

		if (len == 3 && data[0] == 0x02 && data[1] == (byte) 0xAA
				&& data[2] == 0x03)
		{
			return true;
		}

		return false;
	}

	protected int getTime(String sTime)
	{
		int time = -1;
		try
		{
			time = Integer.parseInt(sTime);
		} catch (NumberFormatException e)
		{

		}

		return time;

	}

	protected String[] getUnknownErr()
	{
		// return sError+split+RetUtil.Unknown_Err;

		String str = sError + split + RetUtil.Unknown_Err;
		return str.split(split);
	}

	/*
	 * 读取的线程
	 */
	protected class ReadThread extends Thread
	{

		public ReadThread()
		{
			isReadThreadStop = false;
		}

		public void run()
		{
			TransControl.getInstance().setUserCancel(false);
			super.run();
			isReadQuit = false;
			isReadThreadStop = false;
			ReadThreadRun();
			isReadQuit = true;

		}

		@Override
		public void interrupt()
		{
			isReadThreadStop = true;
			super.interrupt();
		}

		public void quitThread()
		{
			interrupt();
		}
	}

	protected void sendCommData(byte[] data, int len)
	{

		byte data1[] = new byte[len];
		System.arraycopy(data, 0, data1, 0, len);
		TransControl.getInstance().Transfer(data1, len);
	}

	protected class SendThread extends Thread
	{

		public SendThread()
		{
			isSendLock = false;
			isSendThreadStop = false;
		}

		public void run()
		{
			super.run();
			TransControl.getInstance().setUserCancel(false);
			isSendThreadStop = false;
			isSendQuit = false;
			isSendLock = false;
			byte cmdData[] = getFinancialOpenCommad();
			if (cmdData != null)
			{
				sendCommData(cmdData, cmdData.length);
				isSendLock = true;
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			} else
			{
				isSendLock = false;
			}

			SendThreadRun();

			if (!isSendQuit)
			{
				sendEnd();
			}

			cmdData = getFinancialCloseCommad();
			if (cmdData != null)
			{
				sendCommData(cmdData, cmdData.length);

			}
			isSendQuit = true;
			// Log.e("quit","send quit");
		}

		@Override
		public void interrupt()
		{
			isSendThreadStop = true;
			super.interrupt();
		}

		public void quitThread()
		{
			interrupt();
		}
	}

	/**
	 * 写线程的执行函数，子类根据情况进行重写
	 * 
	 */
	protected void SendThreadRun()
	{
		byte data[] = new byte[packLength];
		while (!isSendThreadStop)
		{

			// Log.e(TAG,
			// "SendThreadRun: readData begin("+System.currentTimeMillis()+")");
			int len = readData(data, data.length, 10);
			// Log.e(TAG,
			// "SendThreadRun: readData end("+System.currentTimeMillis()+")");
			if (len == 0)
			{
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				continue;
			}
			if (!isSendLock)
			{

				isSendLock = true;
			}
			if (len > 0)
			{
				if (len == 3 && data[0] == 0x02 && data[1] == (byte) 0x0f
						&& data[2] == 0x03)
				{ // 有从jni中读到数据，要往通信接口发送
					readThread.quitThread();
					isSendThreadStop = true;
					isReadThreadStop = true;
					isSendQuit = true;
					break;
				} else
				{
					// Log.e(TAG,
					// "SendThreadRun: sendCommData begin("+System.currentTimeMillis()+")");
					if (ErrorUtil.LOG_DEBUG)
						Log.i(TAG,
								"handleMessage++接收数据:"
										+ StringUtil.HexToStringA(data, len)
										+ "长度为" + len);
					sendCommData(data, len);
					// Log.e(TAG,
					// "SendThreadRun: sendCommData end("+System.currentTimeMillis()+")");
				}
			}
		}
	}

	protected void sendEnd()
	{

	}

	protected int readCommData(byte[] data)
	{
		int length = TransControl.getInstance().readData(data, data.length, cond);
		return getRealReadedLength(data, length);
	}

	/*
	 * 读线程的执行函数，子类根据情况进行重写
	 */
	protected void ReadThreadRun()
	{
		byte[] data = new byte[packLength];
		while (!isReadThreadStop)
		{

			// Log.e(TAG,
			// "ReadThreadRun: readCommData begin("+System.currentTimeMillis()+")");
			int len = readCommData(data);
			// Log.e(TAG,"read_len:"+len);
			// Log.e(TAG,
			// "ReadThreadRun: readCommData end("+System.currentTimeMillis()+")");

			if (len == -1)
			{
				return;
			}

			if (len > 0)
			{
				// Log.e(TAG,
				// "ReadThreadRun: writeData begin("+System.currentTimeMillis()+")");
				Log.e(TAG, "ReadThreadRun++writeData len:" + len);
				int l = writeData(data, len);// 从通信接口读到数据,要写jni中

				// Log.e(TAG,
				// "ReadThreadRun: writeData end("+System.currentTimeMillis()+")");
			}
		}
		// Log.e("quit","read quit");
	}

	public void start()

	{
		// Log.e("Dev","start in FinancialBase" );
		if (sendThread != null)
		{
			sendThread.quitThread();
			sendThread = null;
		}

		if (readThread != null)
		{
			readThread.quitThread();
			readThread = null;
		}

		readThread = new ReadThread();
		sendThread = new SendThread();

		readThread.start();
		sendThread.start();
	}

	public void quit()
	{
		TransControl.getInstance().setUserCancel(true);
		try
		{
			readThread.quitThread();
			sendThread.quitThread();
			readThread = null;
			sendThread = null;
			quitWait();
		} catch (Exception e)
		{

		} finally
		{
			TransControl.getInstance().setUserCancel(false);
		}
	}

	/*
	 * 退出时要再延时5秒
	 */
	protected void quitWait()
	{
		int time = timeDelay;
		while (true)
		{
			if (isSendQuit && isReadQuit)
			{// 如果写线程和读线程都退出，就不要再等待了
				break;
			}
			time--;
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if (time == 0)
			{
				break;
			}
		}
	}

	private EndOfRead cond = new EndOfRead()
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			for (int i = 0; i < len; i++)
			{
				if (buf[i] == 0x03)
				{
					return true;
				}
			}
			return false;
		}
	};
}
