package com.centerm.t5demolibrary.device.finger;

import android.os.SystemClock;

import com.centerm.t5demolibrary.device.t5.T5Cmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.StringUtil;

/**
 * ָ����
 * @author JQJ
 *
 */
public class FingerFunc{

	private FingerCmd mFingerCmd = null;
	private static FingerFunc instance = null;
	private static final int INT_TYPE = 4;

	private FingerFunc()
	{
		this.mFingerCmd = new FingerCmd();
		TransControl.getInstance();
	}

	public static FingerFunc getInstance(){
		if(instance == null){
			synchronized (FingerFunc.class) {
				instance = new FingerFunc();
			}
		}
		return instance;
	}

	public String getSwInfo(){
		return mFingerCmd.getSwInfo();
	}

	public int openFinger(){
		int nRet = T5Cmd.getInstance().start_mv((byte) INT_TYPE);
		return nRet;
	}

	public int closeFinger(){
		int nRet = T5Cmd.getInstance().stop_mv((byte) INT_TYPE);
		return nRet;
	}

	/**
	 * ��ȡָ��������ֵ
	 * @param timeOut ��ʱʱ��
	 * @return �������
	 */
	public int readFingerFeature(int timeOut){
		byte[] timeout = StringUtil.intToBytes(timeOut);
		int nRet = T5Cmd.getInstance().start_mv((byte) INT_TYPE);
		TransControl.getInstance().setTimeOut((timeOut+2) * 1000);
		if(nRet >= 0){
			SystemClock.sleep(1500);
			nRet = mFingerCmd.readFingerFeature(timeout);
		}
		nRet = T5Cmd.getInstance().stop_mv((byte) INT_TYPE);

		return nRet;
	}

	/**
	 * ָ��ģ��Ǽ�
	 * @param timeOut ��ʱʱ��
	 * @return �������
	 */
	public int registerFingerFeature(int timeOut){
		byte[] timeout = StringUtil.intToBytes(timeOut);
		int nRet = T5Cmd.getInstance().start_mv((byte) INT_TYPE);
		TransControl.getInstance().setTimeOut((timeOut+2) * 1000);
		if(nRet >= 0){
			SystemClock.sleep(1500);
			nRet = mFingerCmd.registerFingerFeature(timeout);
		}
		nRet = T5Cmd.getInstance().stop_mv((byte) INT_TYPE);

		return nRet;
	}

}