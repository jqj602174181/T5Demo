package com.centerm.t5demolibrary.device.finger;

import com.centerm.t5demolibrary.device.t5.T5BaseCmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;

class FingerCmd extends T5BaseCmd{

	private static final String TAG = "FingerCmd";

	private byte[] featureData;

	protected FingerCmd()
	{
		
	}

	public byte[] getFeatureData()
	{
		return featureData;
	}

	/**
	 * 获取指纹仪特征值
	 */
	public int readFingerFeature(byte[] timeOut)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		m_bySwInfo = new byte[1025];
		byte[] byCmdReq = { 0x02, 0x30, 0x30, 0x30, 0x34, 0x30, 0x3C, 0x31,
				0x34, 0x30, 0x30, 0x30, 0x30, 0x31, 0x3C, 0x03 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN, cond);

		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		m_bySwInfo[0] = ErrorUtil.RET_T5_FINGER_SUCCESS;
		System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length);
		return nRet;
	}

	/**
	 * 获取指纹仪特征值
	 */
	public int registerFingerFeature(byte[] timeOut)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		m_bySwInfo = new byte[1025];
		byte[] byCmdReq = { 0x02, 0x30, 0x30, 0x30, 0x34, 0x30, 0x3B, 0x31,
				0x35, 0x30, 0x30, 0x30, 0x30, 0x31, 0x3A, 0x03 }; 
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN, cond);

		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		m_bySwInfo[0] = ErrorUtil.RET_T5_FINGER_SUCCESS;
		System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length);
		return nRet;
	}
}