package com.centerm.t5demolibrary.device.signature;

import android.util.Log;

import com.centerm.t5demolibrary.device.t5.T5BaseCmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;

class SignatureCmd extends T5BaseCmd{

	private static final String TAG = "SignatureCmd";

	private byte[] fileHead;
	private byte[] picData;
	private byte[] realEncryptData;

	protected SignatureCmd(){

	}

	public byte[] getEncryptData()
	{
		return realEncryptData;
	}

	public byte[] getPicSourceData()
	{
		return picData;
	}

	/**
	 * 设置电子签名超时时间
	 * 
	 * @param strTimeout
	 */
	public int setSignTimeout(byte[] strTimeout)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		m_bySwInfo = new byte[1025];
		byte[] byReqData = new byte[18];

		byte[] byCmd = { 0x1B, 0x5B, 0x32, 0x53, 0x45, 0x54, 0x54, 0x49, 0x4d,
				0x45, 0x4f, 0x55, 0x54, 0x02 };
		System.arraycopy(byCmd, 0, byReqData, 0, 14);
		nReqLen = 14;
		System.arraycopy(strTimeout, 0, byReqData, nReqLen, strTimeout.length);
		nReqLen += strTimeout.length;

		byte[] byCmdAdd = { 0x03 };
		System.arraycopy(byCmdAdd, 0, byReqData, nReqLen, 1);
		nReqLen += 1;

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
	 * 启动电子签名
	 */
	public int signOn()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		m_bySwInfo = new byte[1025];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x53, 0x54, 0x41, 0x52, 0x54,
				0x48, 0x57 };
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

		m_bySwInfo[0] = ErrorUtil.RET_SUCCESS;
		System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 3);
		if (byCmdRes[1] == 0x55)
		{
			nRet = ErrorUtil.RET_T5_FAILED;
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 3);
			return nRet;
		}
		return nRet;
	}

	/**
	 * 获取签名数据
	 */
	public int getSignData(byte[] path)
	{

		int nRet = ErrorUtil.RET_SUCCESS;

		nRet = findSignData(path); // 查询文件大小

		if (nRet >= 0)
		{

			int picReceiveSize = Integer.parseInt(StringUtil
					.AsciiToString(picSize));
			int ilength = 1024 * 10; //单包大小
			int receiveNum = picReceiveSize / ilength;
			if ((picReceiveSize % ilength) > 0)
			{
				receiveNum++;
			}
			picData = new byte[picReceiveSize];
			for (int j = 0; j < receiveNum; j++)
			{
				int offset = j * ilength;
				nRet = getSignData(path, offset, ilength, ilength,
						picReceiveSize);
				if (nRet < 0)
				{
					break;
				}
			}

			// 设置通信错误码
			if (nRet < 0)
			{
				m_bySwInfo[0] = (byte) nRet;
				m_bySwInfo[1] = 0x00;
				m_bySwInfo[2] = 0x00;

				return nRet;
			}

			// 文件头
			fileHead = new byte[4];
			System.arraycopy(picData, 0, fileHead, 0, 4);

			realEncryptData = new byte[picData.length - 4];
			System.arraycopy(picData, 4, realEncryptData, 0,
					realEncryptData.length);
			return ErrorUtil.RET_SUCCESS;
		} else {
			return nRet;
		}
	}

	/**
	 * 查询签名数据
	 * 
	 * @param byCmdRes
	 * @return
	 */
	public int findSignData(byte[] path)
	{

		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;

		byte[] byReqData = new byte[PKG_SIZE];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x53, 0x50, 0x45, 0x43, 0x51,
				0x55, 0x45, 0x52, 0x59, 0x46, 0x49, 0x4C, 0x45, 0x02 };

		System.arraycopy(byCmdReq, 0, byReqData, 0, byCmdReq.length);
		nReqLen += byCmdReq.length;

		System.arraycopy(path, 0, byReqData, nReqLen, path.length);
		nReqLen += path.length;

		byReqData[nReqLen] = 0x03;
		nReqLen += 1;

		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		nRet = TransControl.getInstance().Transfer(byReqData, nReqLen,
				byCmdRes, MAX_RECV_LEN, cond);
		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG,
					"查询签名数据-发送指令:"
							+ StringUtil.HexToStringA(byReqData, nReqLen));
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		m_bySwInfo[0] = ErrorUtil.RET_T5_SIGN_SUCCESS;

		if (byCmdRes[1] == 0x55)
		{
			nRet = ErrorUtil.RET_T5_FAILED;
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 3);
			return nRet;
		}

		picSize = separateSignData(byCmdRes, byCmdRes.length, (byte) 0x02,
				(byte) 0x7C);
		picMD5 = separateSignData(byCmdRes, byCmdRes.length, (byte) 0x7C,
				(byte) 0x03);
		return nRet;
	}

	/**
	 * 获取签名数据
	 */
	private int getSignData(byte[] path, int ioffset, int ilength,
			int packetLength, int allLength)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		byte[] offset = StringUtil.StringToHexAscii(String.valueOf(ioffset));
		if ((ioffset + ilength) > allLength)
		{
			ilength = allLength - ioffset;
		}
		byte[] length = StringUtil.StringToHexAscii(String.valueOf(ilength));

		byte[] byReqData = new byte[PKG_SIZE];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x55, 0x50, 0x4C, 0x4F, 0x41,
				0x44, 0x02 };
		System.arraycopy(byCmdReq, 0, byReqData, 0, byCmdReq.length);
		nReqLen += byCmdReq.length;

		// p1
		System.arraycopy(path, 0, byReqData, nReqLen, path.length);
		nReqLen += path.length;

		// 分隔符
		byReqData[nReqLen] = 0x7C;
		nReqLen += 1;

		// P2
		System.arraycopy(offset, 0, byReqData, nReqLen, offset.length);
		nReqLen += offset.length;

		// 分隔符
		byReqData[nReqLen] = 0x7C;
		nReqLen += 1;

		// P3
		System.arraycopy(length, 0, byReqData, nReqLen, length.length);
		nReqLen += length.length;

		// 结尾
		byReqData[nReqLen] = 0x03;
		nReqLen += 1;

		byte[] byCmdRes = new byte[packetLength];
		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG,
					"获取签名数据-发送指令:"
							+ StringUtil.HexToStringA(byReqData, nReqLen));



		nRet = TransControl.getInstance().Transfer(byReqData, nReqLen,
				byCmdRes, ilength, cond_sign);

		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		m_bySwInfo[0] = ErrorUtil.RET_SUCCESS;
		System.arraycopy(byCmdRes, 0, picData, ioffset, ilength);
		return nRet;
	}

	/**
	 * 关闭电子签名
	 */
	public int signOff()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		m_bySwInfo = new byte[1025];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x43, 0x4C, 0x4F, 0x53, 0x45,
				0x48, 0x57 };

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length);

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
}