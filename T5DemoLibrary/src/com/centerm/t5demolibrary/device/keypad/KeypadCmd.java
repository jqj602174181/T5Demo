package com.centerm.t5demolibrary.device.keypad;

import com.centerm.t5demolibrary.device.t5.T5BaseCmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.TransDataFormat;

class KeypadCmd extends T5BaseCmd{

	protected KeypadCmd(){
		
	}

	/**
	 * 实达非加密指令(请输入密码)
	 */
	public int enterKey()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { (byte) 0x82 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN * 32];
		m_bySwInfo = new byte[1025];

		// 发送获取设备信息指令
		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN * 32, cond_keypad);
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}
		m_bySwInfo = new byte[1025];
		m_bySwInfo[0] = ErrorUtil.RET_T5_KEY_SUCCESS;
		if (TransControl.getInstance().getDevType() == TransControl.DEV_USBHID)
		{
			for (int i = 0; i < 32; i++)
			{
				m_bySwInfo[i + 1] = byCmdRes[1024 * i];
			}
		} else
		{
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 1024);
		}
		return nRet;
	}

	/**
	 * 实达非加密指令(请再次输入密码)
	 */
	public int enterKeyAgain()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { (byte) 0x81 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN * 32];
		m_bySwInfo = new byte[1025];
		// 发送获取设备信息指令
		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN * 32, cond_keypad);
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		m_bySwInfo = new byte[1025];
		m_bySwInfo[0] = ErrorUtil.RET_T5_KEY_SUCCESS;
		if (TransControl.getInstance().getDevType() == TransControl.DEV_USBHID)
		{
			for (int i = 0; i < 32; i++)
			{
				m_bySwInfo[i + 1] = byCmdRes[1024 * i];
			}
		} else
		{
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 1024);
		}

		return nRet;
	}

	/**
	 * 实达非加密指令(关闭密码键盘)
	 */
	public int closeKeyboard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] cmd = { (byte) 0x83 };

		// 发送指令
		nRet = TransControl.getInstance().Transfer(cmd, 1);
		m_bySwInfo = new byte[1025];
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * 设置密码键盘超时时间
	 * 
	 * @param strTimeout
	 */
	public int setTimeout(byte[] strTimeout)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		m_bySwInfo = new byte[1025];
		byte[] byReqData = new byte[8];

		byte[] byCmd = { 0x1B, 0x5B, 0x31, 0x3B };
		System.arraycopy(byCmd, 0, byReqData, 0, 4);
		nReqLen = 4;
		System.arraycopy(strTimeout, 0, byReqData, nReqLen, strTimeout.length);
		nReqLen += strTimeout.length;

		byte[] byCmdAdd = { 0x54 };
		System.arraycopy(byCmdAdd, 0, byReqData, nReqLen, 1);
		nReqLen += 1;

		// 发送指令
		nRet = TransControl.getInstance().Transfer(byReqData, byReqData.length);

		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * 设置密码长度
	 * 
	 */
	public int setPwdLen(byte[] pwdlen)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		byte[] byReqData = new byte[8];

		byte[] byCmd = { 0x1B, 0x5B, 0x31, 0x3B };
		System.arraycopy(byCmd, 0, byReqData, 0, 4);
		nReqLen = 4;
		System.arraycopy(pwdlen, 0, byReqData, nReqLen, pwdlen.length);
		nReqLen += pwdlen.length;

		byte[] byCmdAdd = { 0x4C };
		System.arraycopy(byCmdAdd, 0, byReqData, nReqLen, 1);
		nReqLen += 1;

		// 发送指令
		nRet = TransControl.getInstance().Transfer(byReqData, byReqData.length);
		m_bySwInfo = new byte[1025];
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * 建行启动密码键盘
	 * 
	 */
	public int enterPassword_JH(byte voiceNum, byte mKeyLen, byte mtimeoutKey)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		byte[] byReqData = new byte[PKG_SIZE];
		byte[] byCmdReq = { 0x10, 0x23 };
		System.arraycopy(byCmdReq, 0, byReqData, 0, 2);
		nReqLen += 2;
		byReqData[nReqLen] = voiceNum;
		nReqLen += 1;
		byReqData[nReqLen] = mKeyLen;
		nReqLen += 1;
		byReqData[nReqLen] = mtimeoutKey;
		nReqLen += 1;
		// 0x04,0x16,0x19,(byte) 0xB0,0x79,0x4C,(byte) 0xF5,0x60};

		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		// 发送指令
		nRet = sendCmd_JH(byReqData, nReqLen, byCmdRes, byCmdRes.length);

		m_bySwInfo = new byte[1025];
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}
		if (byCmdRes[0] == 0x00)
		{
			m_bySwInfo[0] = ErrorUtil.RET_T5_JHPWD_SUCCESS;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length - 1);
			return nRet;
		} else
		{
			m_bySwInfo[0] = ErrorUtil.RET_T5_FAILED;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length - 1);
			nRet = ErrorUtil.RET_T5_FAILED;
			return nRet;
		}
	}

	/**
	 * 建行下载工作密钥
	 * 
	 * @param mMinakey
	 * @param mKeyLength
	 * @param mworkKey
	 * @return
	 */
	public int downLoadKey_JH(byte mMinakey, byte mKeyLength, byte[] mworkKey)
	{

		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		byte[] byReqData = new byte[PKG_SIZE];
		byte[] byCmdReq = { 0x10, 0x22 };
		System.arraycopy(byCmdReq, 0, byReqData, 0, 2);
		nReqLen += 2;
		byReqData[nReqLen] = mMinakey;
		nReqLen += 1;
		byReqData[nReqLen] = mKeyLength;
		nReqLen += 1;
		System.arraycopy(mworkKey, 0, byReqData, nReqLen, mworkKey.length);// 0x01
		nReqLen += mworkKey.length;

		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		// 发送指令
		nRet = sendCmd_JH(byReqData, nReqLen, byCmdRes, byCmdRes.length);

		m_bySwInfo = new byte[1025];
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}
		if (byCmdRes[0] == 0x00)
		{
			return ErrorUtil.RET_SUCCESS;
		} else
		{
			m_bySwInfo[0] = ErrorUtil.RET_T5_FAILED;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length - 1);
			nRet = ErrorUtil.RET_T5_FAILED;
			return nRet;
		}
	}


	/**
	 * 交互一条指令，返回值为读取数据长度,时间单位ms
	 * 
	 * @param byReq
	 * @param nReqLen
	 * @param byRes
	 * @param nResLen
	 * @param nTimeout
	 * @return
	 */
	private int sendCmd_JH(byte[] byReq, int nReqLen, byte[] byRes, int nResLen)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		int byCmdReqLen = 0;
		byte[] byCmdReq = new byte[PKG_SIZE];
		byte[] byCmdRes = new byte[PKG_SIZE];

		byCmdReqLen = TransDataFormat.DataFormat(SOH, EOT, byReq, nReqLen,
				byCmdReq, byCmdReq.length);

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen,
				byCmdRes, byCmdRes.length, cond);
		if (nRet < 0)
		{
			return nRet;
		}

		// 截取到0x03
		int nPkgLen = getPkgLen(byCmdRes, nRet, EOT);
		nRet = TransDataFormat.CheckAndGetResponse(SOH, EOT, byCmdRes, nPkgLen,
				byRes, nResLen);

		return nRet;
	}
}
