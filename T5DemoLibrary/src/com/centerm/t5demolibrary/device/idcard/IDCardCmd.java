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
	 * LRCУ��
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
	 * ����һ��ָ�����ֵΪ��ȡ���ݳ���,ʱ�䵥λms
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
			Log.i(TAG, "�������ݳɹ�, nRet=" + nRet);

		if (nRet > nResLen)
		{
			return ErrorUtil.RET_OUTOF_ARRAYCOPY;
		}

		// У��ͼ��㲢�ж�
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
	 * �ҿ�
	 * 
	 * @param nTimeOut
	 * @return
	 */
	public int searchCard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// �ҿ���ָ��
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x20, 0x01, 0x22 };
		System.arraycopy(byCmd, 0, byReqData, 5, 5);

		// ����Ѱ��ָ��
		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "search card faild, nRet=" + nRet);
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// �ж��Ƿ��п�
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
	 * ѡ��
	 * 
	 * @param nTimeOut
	 * @return
	 */
	public int selectCard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// ָ��
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

		// �ж��Ƿ��п�
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
	 * ����
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

		// ָ��
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

		// �ж��Ƿ��п�
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
	 * �����Ա�
	 * 
	 * @param sexCode
	 * @param sex
	 */
	private void getSex(byte[] sexCode, byte[] sex)
	{
		if (sexCode[0] == 0x31)
		{
			byte[] bySex = new String("��").getBytes();
			System.arraycopy(bySex, 0, sex, 0, bySex.length);
		}
		else if (sexCode[0] == 0x32)
		{
			byte[] bySex = new String("Ů").getBytes();
			System.arraycopy(bySex, 0, sex, 0, bySex.length);
		}
		else
		{
			byte[] bySex = new String("δ֪").getBytes();
			System.arraycopy(bySex, 0, sex, 0, bySex.length);
		}
	}

	/**
	 * ��������
	 * 
	 * @param nationCode
	 * @param nation
	 */
	private void getNation(byte[] nationCode, byte[] nation)
	{
		int high = (nationCode[0] - 0x30);
		int low = nationCode[1] - 0x30;
		int nCode = (high * 10) + low;

		String[] nationName = { "��", "�ɹ�", "��", "��", "ά���", "��", "��", "׳", "����", "����", "��", "��", "��", "��", "����", "����",
				"������", "��", "��", "����", "��", "�", "��ɽ", "����", "ˮ", "����", "����", "����", "�¶�����", "��", "���Ӷ�", "����", "Ǽ", "����",
				"����", "ë��", "����", "����", "����", "����", "������", "ŭ", "���α��", "����˹", "���¿�", "�°�", "����", "ԣ��", "��", "������",
				"����", "���״�", "����", "�Ű�", "���", "��ŵ" };

		if (nCode > 0 && nCode < 57)
		{
			byte[] byNation = nationName[nCode - 1].getBytes();
			System.arraycopy(byNation, 0, nation, 0, byNation.length);
		}
		else
		{
			byte[] byNation = new String("����").getBytes();
			System.arraycopy(byNation, 0, nation, 0, byNation.length);
		}
	}

	/**
	 * ������֤��Ϣ
	 * 
	 * @param pInfo
	 * @param byData
	 */
	private void analyzePersonInfo(PersonInfo pInfo, byte[] byData)
	{
		int nWLen = (byData[0] << 8) + byData[1];
		int nPLen = (byData[2] << 8) + byData[3];

		byte[] byWInfo = new byte[nWLen]; // ������Ϣ
		byte[] byPInfo = new byte[nPLen]; // ͼƬ����

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
				byte[] byFingerInfo = new byte[nFingerLen]; // ָ������
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

		// �����Ա������
		getSex(pInfo.sexCode, pInfo.sex);
		getNation(pInfo.nationCode, pInfo.nation);

		System.arraycopy(byPInfo, 0, pInfo.photo, 0, nPLen);
		pInfo.photosize = nPLen;
	}

	/**
	 * ������֤�ӿ�
	 * 
	 * @param pInfo
	 * @param timeout
	 * @return
	 */
	public int getPersonMsg(PersonInfo pInfo)
	{
		int nRet = 0;
		byte[] byCardData = new byte[4096];

		// ����ָ֤�����
		nRet = readPersonMsg(byCardData);
		if (nRet == ErrorUtil.RET_SUCCESS)
		{
			analyzePersonInfo(pInfo, byCardData);
		}

		return nRet;
	}

	/**
	 * ��ȡ�豸��Ϣ
	 * 
	 * @param nTimeOut
	 * @return �ɹ� > 0��ʧ��<=0
	 */
	public int readSamNo(byte[] bySamNo)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// ָ��
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE };
		System.arraycopy(byCmd, 0, byReqData, 5, 5);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "get SAM��� faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// �ж��Ƿ���ȷ
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
	 * ��ȡ׷����Ϣ
	 * 
	 * @return �ɹ� > 0��ʧ��<=0
	 */
	public int readAddMsg(byte[] addMsg)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// ָ��
		System.arraycopy(byPreamble, 0, byReqData, 0, 5);

		byte[] byCmd = { 0x00, 0x03, 0x30, (byte) 0x03 };
		System.arraycopy(byCmd, 0, byReqData, 5, 4);
		byReqData[9] = calcXor(byCmd, 4);

		nRet = sendCmd(byReqData, 10, byResData, byResData.length);
		if (nRet < 0)
		{
			Log.e(TAG, "get ׷����Ϣ faild");
			m_bySwInfo[0] = (byte) nRet;
			return nRet;
		}

		// �ж��Ƿ��п�
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
	 * ����SAM_A����Ƶģ��һ֡ͨ�����ݵ�����ֽ���
	 * 
	 * @return �ɹ� > 0��ʧ��<=0
	 */
	public int setByte(int nByte)
	{
		int nReqLen = 0;
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[11];
		byte[] byResData = new byte[1024];

		// ָ��
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

		// �ж��Ƿ�ɹ�
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

		// ָ��
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

		// �ж��Ƿ�ɹ�
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
	 * ��λ
	 * 
	 * @return �ɹ� > 0��ʧ��<=0
	 */
	public int resetIDCard()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// ָ��
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

		// �ж��Ƿ�ɹ�
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
	 * ״̬���
	 * 
	 * @return �ɹ� > 0��ʧ��<=0
	 */
	public int checkStatus()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] byReqData = new byte[10];
		byte[] byResData = new byte[1024];

		// ָ��
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

		// �ж��Ƿ�ɹ�
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

	// ���豸��SN��ת��Ŀ���ʽ
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

		// ����С��ģʽ
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
