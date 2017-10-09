package com.centerm.t5demolibrary.device.idcard;

import com.centerm.t5demolibrary.device.t5.T5BaseCmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.CharacterUtil;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;

import android.util.Log;

class IDCardCmd extends T5BaseCmd{

	private static final String TAG = "IDCardCmd";
	public static final int TYPE_IDCARD_2nd = 2;
	public static final int TYPE_IDCARD_3nd = 3;

	private int m_nIDType = TYPE_IDCARD_2nd;
	private boolean m_HasFingerprint = false;

	protected IDCardCmd(){
		
	}

	/**
	 * LRC校验
	 * 
	 * @param byBuf
	 * @param len
	 * @return
	 */
	private byte calcXor(byte[] byBuf, int len)
	{
		byte byLrc = 0;

		for (int i = 0; i < len; i++)
		{
			byLrc = (byte) (byLrc ^ byBuf[i]);
		}

		return byLrc;
	}

	private int getPkgLen(byte[] buf)
	{
		if (buf == null)
		{
			return -1;
		}

		if (buf.length < 7)
		{
			return -1;
		}

		int high = (buf[5] << 8) & 0xff00;
		int low = buf[6] & 0xff;
		int nPkgLen = 7 + high + low;

		return nPkgLen;
	}

	public void setIDType(int nType)
	{
		m_nIDType = nType;
	}

	/**
	 * @return the m_nIDType
	 */
	public boolean hasFingerprint()
	{
		return m_HasFingerprint;
	}

	public void setSwCode(int ret)
	{
		m_bySwInfo[0] = (byte) ret;
	}

	public void cleanSwInfo()
	{
		for (int i = 0; i < m_bySwInfo.length; i++)
		{
			m_bySwInfo[i] = 0x00;
		}
	}

	/**
	 * 交互一条指令，返回值为读取数据长度,时间单位ms
	 * 
	 * @param byReq
	 * @param nReqLen
	 * @param byRes
	 * @param nResLen
	 * @param nTimeOut
	 * @return
	 */
	private int sendCmd(byte[] byReq, int nReqLen, byte[] byRes, int nResLen)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		nRet = TransControl.getInstance().Transfer(byReq, nReqLen, byRes, nResLen, cond_id);
		if (ErrorUtil.LOG_DEBUG)
			Log.d(TAG, "sendCmd++nRet:" + nRet);
		if (nRet < 5)
		{
			return nRet;
		}

		nRet = getPkgLen(byRes);

		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG, "接收数据成功, nRet=" + nRet);

		if (nRet > nResLen)
		{
			return ErrorUtil.RET_OUTOF_ARRAYCOPY;
		}

		// 校验和计算并判断
		byte[] byData = new byte[nRet - 6];
		System.arraycopy(byRes, 5, byData, 0, nRet - 6);

		byte byLrc = calcXor(byData, byData.length);

		if (byLrc != byRes[nRet - 1])
		{
			nRet = ErrorUtil.RET_DATA_ERROR;
		}

		return nRet;
	}

	/**
	 * 找卡
	 * 
	 * @param nTimeOut
	 * @return
	 */
	public int searchCard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 找卡的指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x20, 0x01, 0x22 };
		System.arraycopy(byCmd, 0, byReqData, 5, 5);

		// 发送寻卡指令
		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "search card faild, nRet=" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否有卡
		if (byResData[9] == (byte) 0x9F)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			Log.e(TAG, "search card faild");
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_SEARCH_FAILED;
		}
	}

	/**
	 * 选卡
	 * 
	 * @param nTimeOut
	 * @return
	 */
	public int selectCard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x20, 0x02, 0x21 };
		System.arraycopy(byCmd, 0, byReqData, 5, 5);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "select card faild, nRet=" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否有卡
		if (byResData[9] == (byte) 0x90)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_SELECT_FAILED;
		}
	}

	/**
	 * 读卡
	 * 
	 * @param nTimeOut
	 * @param byCardData
	 * @return
	 */
	public int readPersonMsg(byte[] byCardData)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[4096];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		if (TYPE_IDCARD_2nd == m_nIDType)
		{
			byte[] byCmd = { 0x00, 0x03, 0x30, 0x01, 0x32 };
			System.arraycopy(byCmd, 0, byReqData, 5, 5);
		}
		else
		{
			byte[] byCmd3 = { 0x00, 0x03, 0x30, 0x10, 0x23 };
			System.arraycopy(byCmd3, 0, byReqData, 5, 5);
		}

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "read card faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否有卡
		if (byResData[9] == (byte) 0x90)
		{
			System.arraycopy(byResData, 10, byCardData, 0, nRet - 11);
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_READID_FAILED;
		}
	}

	/**
	 * 解析性别
	 * 
	 * @param sexCode
	 * @param sex
	 */
	private void getSex(byte[] sexCode, byte[] sex)
	{
		if (sexCode[0] == 0x31)
		{
			byte[] bySex = new String("男").getBytes();
			System.arraycopy(bySex, 0, sex, 0, bySex.length);
		}
		else if (sexCode[0] == 0x32)
		{
			byte[] bySex = new String("女").getBytes();
			System.arraycopy(bySex, 0, sex, 0, bySex.length);
		}
		else
		{
			byte[] bySex = new String("未知").getBytes();
			System.arraycopy(bySex, 0, sex, 0, bySex.length);
		}
	}

	/**
	 * 解析民族
	 * 
	 * @param nationCode
	 * @param nation
	 */
	private void getNation(byte[] nationCode, byte[] nation)
	{
		int high = (nationCode[0] - 0x30);
		int low = nationCode[1] - 0x30;
		int nCode = (high * 10) + low;

		String[] nationName = { "汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗", "瑶", "白", "土家", "哈尼",
				"哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬", "羌", "布朗",
				"撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌孜别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔",
				"独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺" };

		if (nCode > 0 && nCode < 57)
		{
			byte[] byNation = nationName[nCode - 1].getBytes();
			System.arraycopy(byNation, 0, nation, 0, byNation.length);
		}
		else
		{
			byte[] byNation = new String("其他").getBytes();
			System.arraycopy(byNation, 0, nation, 0, byNation.length);
		}
	}

	/**
	 * 读二代证信息
	 * 
	 * @param pInfo
	 * @param byData
	 */
	private void analyzePersonInfo(PersonInfo pInfo, byte[] byData)
	{
		int nWLen = (byData[0] << 8) + byData[1];
		int nPLen = (byData[2] << 8) + byData[3];

		byte[] byWInfo = new byte[nWLen]; // 文字信息
		byte[] byPInfo = new byte[nPLen]; // 图片数据

		if (TYPE_IDCARD_2nd == m_nIDType)
		{
			System.arraycopy(byData, 4, byWInfo, 0, nWLen);
			System.arraycopy(byData, 4 + nWLen, byPInfo, 0, nPLen);
			pInfo.fingerdata = null;
		}
		else
		{
			System.arraycopy(byData, 6, byWInfo, 0, nWLen);
			System.arraycopy(byData, 6 + nWLen, byPInfo, 0, nPLen);

			int nFingerLen = (byData[4] << 8) + byData[5];
			m_HasFingerprint = false;
			if (0 < nFingerLen)
			{
				m_HasFingerprint = true;
				byte[] byFingerInfo = new byte[nFingerLen]; // 指纹数据
				System.arraycopy(byData, 6 + nWLen + nPLen, byFingerInfo, 0, nFingerLen);
				System.arraycopy(byFingerInfo, 0, pInfo.fingerdata, 0, nFingerLen);
			}
		}

		int pos = 0;
		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 30, pInfo.name, 0, pInfo.name.length);
		StringUtil.TrimByte(pInfo.name, pInfo.name.length);
		pos += 30;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 2, pInfo.sexCode, 0, pInfo.sexCode.length);
		StringUtil.TrimByte(pInfo.sexCode, pInfo.sexCode.length);
		pos += 2;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 4, pInfo.nationCode, 0, pInfo.nationCode.length);
		StringUtil.TrimByte(pInfo.nationCode, pInfo.nationCode.length);
		pos += 4;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 16, pInfo.birthday, 0, pInfo.birthday.length);
		StringUtil.TrimByte(pInfo.birthday, pInfo.birthday.length);
		pos += 16;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 70, pInfo.address, 0, pInfo.address.length);
		StringUtil.TrimByte(pInfo.address, pInfo.address.length);
		pos += 70;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 36, pInfo.cardId, 0, pInfo.cardId.length);
		StringUtil.TrimByte(pInfo.cardId, pInfo.cardId.length);
		pos += 36;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 30, pInfo.police, 0, pInfo.police.length);
		StringUtil.TrimByte(pInfo.police, pInfo.police.length);
		pos += 30;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 16, pInfo.validStart, 0, pInfo.validStart.length);
		StringUtil.TrimByte(pInfo.validStart, pInfo.validStart.length);
		pos += 16;

		CharacterUtil.UnicodeToUFT8(byWInfo, pos, 16, pInfo.validEnd, 0, pInfo.validEnd.length);
		StringUtil.TrimByte(pInfo.validEnd, pInfo.validEnd.length);
		pos += 16;

		// 设置性别和民族
		getSex(pInfo.sexCode, pInfo.sex);
		getNation(pInfo.nationCode, pInfo.nation);

		System.arraycopy(byPInfo, 0, pInfo.photo, 0, nPLen);
		pInfo.photosize = nPLen;
	}

	/**
	 * 读二代证接口
	 * 
	 * @param pInfo
	 * @param timeout
	 * @return
	 */
	public int getPersonMsg(PersonInfo pInfo)
	{
		int nRet = 0;
		byte[] byCardData = new byte[4096];

		// 二代证指令操作
		nRet = readPersonMsg(byCardData);
		if (nRet == ErrorUtil.RET_SUCCESS)
		{
			analyzePersonInfo(pInfo, byCardData);
		}

		return nRet;
	}

	/**
	 * 获取设备信息
	 * 
	 * @param nTimeOut
	 * @return 成功 > 0；失败<=0
	 */
	public int readSamNo(byte[] bySamNo)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE };
		System.arraycopy(byCmd, 0, byReqData, 5, 5);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "get SAM编号 faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否正确
		if (byResData[9] == (byte) 0x90)
		{
			byte[] tmp = new byte[nRet - 15];
			System.arraycopy(byResData, 10, tmp, 0, nRet - 15);

			int len = transformDeviceSnToTargetFormat(tmp, nRet - 15, bySamNo);
			return len;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}

	/**
	 * 读取追加信息
	 * 
	 * @return 成功 > 0；失败<=0
	 */
	public int readAddMsg(byte[] addMsg)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x30, (byte) 0x03 };
		System.arraycopy(byCmd, 0, byReqData, 5, 4);
		byReqData[9] = calcXor(byCmd, 4);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "get 追加信息 faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否有卡
		if (byResData[9] == (byte) 0x90)
		{
			int nLen = nRet - 11;
			System.arraycopy(byResData, 10, addMsg, 0, nLen);
			return nLen;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}

	/**
	 * 设置SAM_A与射频模块一帧通信数据的最大字节数
	 * 
	 * @return 成功 > 0；失败<=0
	 */
	public int setByte(int nByte)
	{
		int nReqLen = 0;
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[11];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);
		nReqLen = 5;

		byte[] byCmd = new byte[5];
		byCmd[0] = 0x00;
		byCmd[1] = 0x04;
		byCmd[2] = 0x61;
		byCmd[3] = (byte) 0xFF;
		byCmd[4] = (byte) (nByte & 0xFF);

		System.arraycopy(byCmd, 0, byReqData, 5, 5);
		nReqLen += 5;
		byReqData[nReqLen++] = calcXor(byCmd, 5);

		nRet = sendCmd(byReqData, nReqLen, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "setByte faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否成功
		if (byResData[9] == (byte) 0x90)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}

	public int setBaud(int nBaud)
	{
		int nReqLen = 0;
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);
		nReqLen += 5;

		byte[] byCmd = new byte[4];
		byCmd[0] = 0x00;
		byCmd[1] = 0x03;
		byCmd[2] = 0x60;

		switch (nBaud)
		{
		case 9600:
			byCmd[3] = 0x04;
			break;
		case 19200:
			byCmd[3] = 0x03;
			break;
		case 38400:
			byCmd[3] = 0x02;
			break;
		case 57600:
			byCmd[3] = 0x01;
			break;
		case 115200:
			byCmd[3] = 0x00;
			break;
		default:
			byCmd[3] = 0x04;
			break;
		}

		System.arraycopy(byCmd, 0, byReqData, 5, 4);
		nReqLen += 4;
		byReqData[nReqLen++] = calcXor(byCmd, 4);

		nRet = sendCmd(byReqData, nReqLen, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "setBaud faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否成功
		if (byResData[9] == (byte) 0x90)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}

	/**
	 * 复位
	 * 
	 * @return 成功 > 0；失败<=0
	 */
	public int resetIDCard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x10, (byte) 0xFF, (byte) 0xEC };
		System.arraycopy(byCmd, 0, byReqData, 5, 5);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "reset faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否成功
		if (byResData[9] == (byte) 0x90)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}

	/**
	 * 状态检测
	 * 
	 * @return 成功 > 0；失败<=0
	 */
	public int checkStatus()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// 指令
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x11, (byte) 0xFF };
		System.arraycopy(byCmd, 0, byReqData, 5, 4);
		byReqData[9] = calcXor(byCmd, 4);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "checkStatus faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// 判断是否成功
		if (byResData[9] == (byte) 0x90)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byResData, 7, m_bySwInfo, 1, 3);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}

	// 把设备内SN号转成目标格式
	private static int transformDeviceSnToTargetFormat(byte[] SAMVNumber, int SAMVNumberLen, byte[] result)
	{
		byte[] buf = new byte[256];
		int len = 0;

		hexDataToDecAscii(SAMVNumber, 0, 2, buf, len, 2);
		len += 2;

		hexDataToDecAscii(SAMVNumber, 2, 2, buf, len, 2);
		len += 2;

		hexDataToDecAscii(SAMVNumber, 4, 4, buf, len, 8);
		len += 8;

		hexDataToDecAscii(SAMVNumber, 8, 4, buf, len, 10);
		len += 10;

		System.arraycopy(buf, 0, result, 0, len);

		return len;

	}

	private static void hexDataToDecAscii(byte[] hex, int hexPos, int hexLen, byte[] dec, int desPos, int decLen)
	{
		int tmp = 0;

		// 采用小端模式
		for (int i = hexLen - 1 + hexPos; i >= hexPos; i--)
		{
			tmp = tmp * 256 + StringUtil.unsignedByteToInt(hex[i]);
		}

		for (int i = decLen - 1 + desPos; i >= desPos; i--)
		{
			if (tmp > 0)
			{
				dec[i] = (byte) (tmp % 10 + 0x30);
				tmp /= 10;
			}
			else
			{
				dec[i] = 0x30;
			}
		}

		return;
	}

}
