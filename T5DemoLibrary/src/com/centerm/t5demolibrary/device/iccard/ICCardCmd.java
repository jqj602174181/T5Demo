package com.centerm.t5demolibrary.device.iccard;

import com.centerm.iccard.IcCard;
import com.centerm.t5demolibrary.device.t5.T5BaseCmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;
import com.centerm.t5demolibrary.utils.TransDataFormat;

import android.util.Log;

class ICCardCmd extends T5BaseCmd{

	private static final String TAG = "ICCardCmd";
	private byte[] m_byICData;
	private boolean m_bHexCon = true, m_bEstimate = true;
	private String icCardMsg = "";// 读IC卡号错误信息及卡号

	protected ICCardCmd(){
		
	}

	public void cleanSwInfo()
	{
		for (int i = 0; i < m_bySwInfo.length; i++)
		{
			m_bySwInfo[i] = 0x00;
		}
	}

	/**
	 * 读取ic卡
	 * @param data
	 * @return 
	 */
	public int getICCardNum(ICCardData data)
	{
		IcCard iccard = new IcCard();
		iccard.start();
		int nRet = 0;
		final String[] dataList = iccard.cycleGetICCardInfo(
				data.getCardStyle(), data.tag, data.list, data.timeOut);

		if (null != dataList)
		{
			if (dataList[dataList.length - 1].toString().contains("A")
					&& !dataList[dataList.length - 1].toString().equals("A000"))
			{
				icCardMsg = dataList[dataList.length - 1].toString();
				nRet = ErrorUtil.RET_SUCCESS;
			} else
			{
				icCardMsg = dataList[dataList.length - 1].toString();
				nRet = ErrorUtil.RET_OPERATE_FAILED;
			}
		}

		try
		{
			Thread.sleep(100);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		iccard.quit();
		return nRet;
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
	private int sendCmd(byte[] byReq, int nReqLen, byte[] byRes, int nResLen)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		int byCmdReqLen = 0;
		byte[] byCmdReq = new byte[PKG_SIZE];
		byte[] byCmdRes = new byte[PKG_SIZE];

		byCmdReqLen = TransDataFormat.DataFormat(SOH, EOT, byReq, nReqLen, byCmdReq, byCmdReq.length);

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen, byCmdRes, byCmdRes.length, cond);
		if (nRet < 0)
		{
			return nRet;
		}

		// 截取到0x03
		int nPkgLen = getPkgLen(byCmdRes, nRet);
		nRet = TransDataFormat.CheckAndGetResponse(SOH, EOT, byCmdRes, nPkgLen, byRes, nResLen);

		if (m_bEstimate)
		{
			if (byRes[0] != 0x00 || byRes[1] != 0x00)
			{
				Log.e(TAG, "IC卡指令操作返回失败");
				return ErrorUtil.RET_OPERATE_FAILED;
			}
		}

		return nRet;
	}

	private byte getBaud(int nIndex)
	{
		byte byBaud = 0x00;

		switch (nIndex)
		{
		case 0:
			byBaud = 0x00;
			break;

		case 1:
			byBaud = 0x01;
			break;

		case 2:
			byBaud = 0x02;
			break;

		case 3:
			byBaud = 0x03;
			break;

		case 4:
			byBaud = 0x04;
			break;

		default:
			byBaud = 0x00;
			break;
		}

		return byBaud;
	}

	/**
	 * 
	 * 从指定数组的copy一个子数组并返回
	 * 
	 * @param org
	 *            of type byte[] 原数组
	 * @param to
	 *            合并一个byte[]
	 * 
	 * @return 合并的数据
	 */

	public static byte[] append(byte[] org, byte[] to)
	{
		byte[] newByte = new byte[org.length + to.length];

		System.arraycopy(org, 0, newByte, 0, org.length);
		System.arraycopy(to, 0, newByte, org.length, to.length);

		return newByte;
	}

	/*
	 * 软复位
	 */
	public int cardReset()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = { 0x31, 0x12 };
		byte[] byResData = new byte[PKG_SIZE];

		// 发送指令
		nRet = sendCmd(byReqData, 2, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/*
	 * 设置通信参数
	 */
	public int cardSetPara(int nTypeIndex, int nBaudIndex)
	{
		int nRet = ErrorUtil.RET_SUCCESS, nReqLen = 0;

		byte[] byReqData = new byte[4];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x30, 0x01 };
		System.arraycopy(byCmdHead, 0, byReqData, 0, 2);
		nReqLen = 2;

		// 卡片类型参数设置
		if (0 == nTypeIndex)
			byReqData[nReqLen++] = 0x00;
		else if (1 == nTypeIndex)
			byReqData[nReqLen++] = 0x01;

		byReqData[nReqLen++] = getBaud(nBaudIndex);
		byte[] byDest = new byte[18];
		TransDataFormat.DataFormat((byte) 0x02, (byte) 0x03, byReqData, byReqData.length, byDest, 18);
		// 发送指令
		nRet = TransControl.getInstance().Transfer(byDest, byDest.length);

		if (nRet < 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/*
	 * 查看读写器版本信息
	 */
	public int cardReadInfo()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = { 0x31, 0x11 };
		byte[] byResData = new byte[PKG_SIZE];

		// 发送指令
		nRet = sendCmd(byReqData, 2, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		m_bHexCon = false;
		m_byICData = new byte[byResData.length];
		System.arraycopy(byResData, 0, m_byICData, 0, byResData.length);

		return nRet;
	}

	/*
	 * 判断卡片状态
	 */
	public int cardCheckStatus(String strSlotNum)
	{
		int nRet = 0;

		byte[] byReqData = new byte[3];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x32, 0x21 };
		System.arraycopy(byCmdHead, 0, byReqData, 0, 2);

		int nSlotNum = Integer.valueOf(strSlotNum, 16);
		byReqData[2] = (byte) nSlotNum;

		// 发送指令
		m_bEstimate = false;
		nRet = sendCmd(byReqData, byReqData.length, byResData, byResData.length);
		if (0 >= nRet)
		{
			Log.e(TAG, "判断卡片状态失败：" + nRet);
			return nRet;
		}

		m_byICData = new byte[nRet];
		System.arraycopy(byResData, 0, m_byICData, 0, nRet);
		return nRet;
	}

	/*
	 * 应用命令层传输
	 */
	public int cardCAPDU(String strC_APDU, String strSlotNum)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		int nReqLen = 0;
		byte[] byReqData = new byte[PKG_SIZE];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x32, 0x26 };
		System.arraycopy(byCmdHead, 0, byReqData, 0, byCmdHead.length);
		nReqLen = 2;

		int nSlotNum = Integer.valueOf(strSlotNum, 16);
		byReqData[nReqLen++] = (byte) nSlotNum;

		// 不定长
		byte[] byC_APDU = StringUtil.HexStringToByte(strC_APDU);
		System.arraycopy(byC_APDU, 0, byReqData, nReqLen, byC_APDU.length);
		nReqLen += strC_APDU.length() / 2;

		if (ErrorUtil.LOG_DEBUG)
		{
			Log.i(TAG, "****cardCAPDU:" + StringUtil.HexToStringA(byReqData, nReqLen));
			Log.i(TAG, "****cardCAPDU, nReqLen:" + nReqLen);
		}

		// 发送指令
		nRet = sendCmd(byReqData, nReqLen, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		m_byICData = new byte[nRet - 2];
		System.arraycopy(byResData, 2, m_byICData, 0, nRet - 2);
		return nRet;
	}

	/*
	 * 卡片上电
	 */
	public int cardPowerOn(String strDelayTime, String strSlotNum)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = new byte[5];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x32, 0x22 };

		// 组合
		byte[] byDelayTime = StringUtil.HexStringToByte(strDelayTime);
		byte[] byBuf = append(byCmdHead, byDelayTime);
		System.arraycopy(byBuf, 0, byReqData, 0, byBuf.length);

		int nSlotNum = Integer.valueOf(strSlotNum, 16);
		byReqData[4] = (byte) nSlotNum;

		// 发送指令
		nRet = sendCmd(byReqData, byReqData.length, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		m_byICData = new byte[nRet - 2];
		System.arraycopy(byResData, 2, m_byICData, 0, nRet - 2);
		return nRet;
	}

	/*
	 * 卡片下电
	 */
	public int cardPowerOff(String strSlotNum)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = new byte[3];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x32, 0x23 };
		int nSlotNum = Integer.valueOf(strSlotNum, 16);

		// 组合
		System.arraycopy(byCmdHead, 0, byReqData, 0, byCmdHead.length);
		byReqData[2] = (byte) nSlotNum;

		// 发送指令
		nRet = sendCmd(byReqData, byReqData.length, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/*
	 * 激活非接卡
	 */
	public int cardPiccPowerOn(String strDelayTime)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = new byte[4];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x32, 0x24 };
		byte[] byDelayTime = StringUtil.HexStringToByte(strDelayTime);

		// 组合
		byte[] byBuf = append(byCmdHead, byDelayTime);
		System.arraycopy(byBuf, 0, byReqData, 0, byBuf.length);

		// 发送指令
		nRet = sendCmd(byReqData, byReqData.length, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		m_byICData = new byte[nRet - 2];
		System.arraycopy(byResData, 2, m_byICData, 0, nRet - 2);
		return nRet;
	}

	/*
	 * 设置Halt状态
	 */
	public int cardPiccPowerOff(String strDelayTime)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = new byte[4];
		byte[] byResData = new byte[PKG_SIZE];

		// 命令头
		byte[] byCmdHead = { 0x32, 0x25 };
		byte[] byDelayTime = StringUtil.HexStringToByte(strDelayTime);

		// 组合
		byte[] byBuf = append(byCmdHead, byDelayTime);
		System.arraycopy(byBuf, 0, byReqData, 0, byBuf.length);

		// 发送指令
		nRet = sendCmd(byReqData, byReqData.length, byResData, byResData.length);
		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error:" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 0, m_bySwInfo, 1, 2);
			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	public String getICData()
	{
		return icCardMsg;
		//		String result = null;
		//
		//		if(m_byICData == null){
		//			return null;
		//		}
		//
		//		if (m_bHexCon)
		//		{
		//			if (m_bEstimate)
		//				result = StringUtil.HexToStringA(m_byICData, m_byICData.length);
		//			else
		//			{
		//				String format = "%02XH, %02XH";
		//				result = String.format(format, m_byICData[0] & 0xFF, m_byICData[1] & 0xFF);
		//				m_bEstimate = true;
		//			}
		//		}
		//		else
		//			// result = new String(m_byICData, 0, m_byICData.length);
		//		{
		//			String formata = "%02XH, %02XH, %02XH, %02XH, %02XH," + " %02XH,%02XH, %02XH, %02XH, %02XH, "
		//					+ " %02XH,%02XH, %02XH, %02XH, %02XH, " + " %02XH,%02XH, %02XH, %02XH, %02XH, "
		//					+ " %02XH,%02XH, %02XH, %02XH, %02XH, " + " %02XH,%02XH, %02XH, %02XH, %02XH";
		//
		//			result = String.format(formata, m_byICData[0] & 0xFF, m_byICData[1] & 0xFF, m_byICData[2] & 0xFF,
		//					m_byICData[3] & 0xFF, m_byICData[4] & 0xFF, m_byICData[5] & 0xFF, m_byICData[6] & 0xFF,
		//					m_byICData[7] & 0xFF, m_byICData[8] & 0xFF, m_byICData[9] & 0xFF, m_byICData[10] & 0xFF,
		//					m_byICData[11] & 0xFF, m_byICData[12] & 0xFF, m_byICData[13] & 0xFF, m_byICData[14] & 0xFF,
		//					m_byICData[15] & 0xFF, m_byICData[16] & 0xFF, m_byICData[17] & 0xFF, m_byICData[18] & 0xFF,
		//					m_byICData[19] & 0xFF, m_byICData[20] & 0xFF, m_byICData[21] & 0xFF, m_byICData[22] & 0xFF,
		//					m_byICData[23] & 0xFF, m_byICData[24] & 0xFF, m_byICData[25] & 0xFF, m_byICData[26] & 0xFF,
		//					m_byICData[27] & 0xFF, m_byICData[28] & 0xFF, m_byICData[29] & 0xFF, m_byICData[30] & 0xFF);
		//		}
		//		m_bHexCon = true;
	}
}
