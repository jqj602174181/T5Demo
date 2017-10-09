package com.centerm.t5demolibrary.device.magcard;

import com.centerm.t5demolibrary.device.t5.T5BaseCmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;
import com.centerm.t5demolibrary.utils.TransDataFormat;

import android.util.Log;

class MagCardCmd extends T5BaseCmd{

	private static final String TAG = "MagCardCmd";

	private int which = CMD_READTRACK1;
	private boolean isCT = false;
	private String spilt = "";

	protected MagCardCmd(){
		
	}

	public void setWhich(int which)
	{
		this.which = which;
	}

	public void setCT(boolean isCT)
	{
		this.isCT = isCT;
	}

	public String getSpilt()
	{
		return this.spilt;
	}

	public void cleanSwInfo()
	{
		for (int i = 0; i < m_bySwInfo.length; i++)
		{
			m_bySwInfo[i] = 0x00;
		}
	}

	public String getM_bySwInfo()
	{
		String strInfo = null, format = null;

		switch (m_bySwInfo[0])
		{
		case ErrorUtil.RET_TIMEOUT:
			strInfo = "������Ӧ���ݳ�ʱ!";
			break;

		case ErrorUtil.RET_COMMUNICATE:
			strInfo = "ͨ��ʧ��,�����ԣ�";
			break;

		case ErrorUtil.RET_FIND_INTERFACE:
			strInfo = "�豸�ѶϿ�!";
			break;

		case ErrorUtil.RET_READTRACK_FAILED:
			format = "%02XH %02XH %02XH %02XH %02XH";
			strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF,
					m_bySwInfo[4] & 0xFF, m_bySwInfo[5] & 0xFF);
			break;

		case ErrorUtil.RET_READTRACK3_FAILED:
			format = "%02XH %02XH %02XH %02XH %02XH %02XH";
			strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF,
					m_bySwInfo[4] & 0xFF, m_bySwInfo[5] & 0xFF, m_bySwInfo[6] & 0xFF);
			break;

		case ErrorUtil.RET_READDBTRACK_FAILED:
			format = "%02XH %02XH %02XH %02XH %02XH %02XH %02XH";
			strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF,
					m_bySwInfo[4] & 0xFF, m_bySwInfo[5] & 0xFF, m_bySwInfo[6] & 0xFF, m_bySwInfo[7] & 0xFF);
			break;

		case ErrorUtil.RET_READSTATUS_SUCCESS:
			if (StringUtil.GetVailArrayLen(m_bySwInfo) == 5)
			{
				format = "%02XH %02XH %02XH %02XH";
				strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF,
						m_bySwInfo[4] & 0xFF);
			}
			else if (StringUtil.GetVailArrayLen(m_bySwInfo) == 4)
			{
				format = "%02XH %02XH %02XH";
				strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF);
			}
			else
			{
				format = "%02XH %02XH %02XH";
				strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF);
			}
			break;

		case ErrorUtil.RET_READSTATUS_FAILED:
			if (StringUtil.GetVailArrayLen(m_bySwInfo) == 5)
			{
				format = "%02XH %02XH %02XH %02XH";
				strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF,
						m_bySwInfo[4] & 0xFF);
			}
			else if (StringUtil.GetVailArrayLen(m_bySwInfo) == 4)
			{
				format = "%02XH %02XH %02XH";
				strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF);
			}
			else
			{
				format = "%02XH %02XH %02XH";
				strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF);
			}
			break;

		default:
			format = "״̬��:%02XH %02XH, ������:%d";
			strInfo = String.format(format, m_bySwInfo[1] & 0xFF, m_bySwInfo[2] & 0xFF, m_bySwInfo[0]);
			break;
		}

		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG, "getSwInfo+++strInfo:" + strInfo);

		return strInfo;
	}

	public void setM_bySwInfo(byte[] m_bySwInfo)
	{
		this.m_bySwInfo = m_bySwInfo;
	}

	/**
	 * ������ŵ�
	 * 
	 * @param info
	 * @param which
	 * @return
	 */
	private int readTrack_NT(MagCardInfo info, int which)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = new byte[2];
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		if (which == CMD_UNDEFINE)
		{
			return ErrorUtil.RET_PARAM_ERROR;
		}

		// ����ָ��
		byCmdReq = cmdReq_NT(which, byCmdReq);

		// ����ָ��
		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length, byCmdRes, byCmdRes.length, condNT);

		// ����ͨ�Ŵ�����
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}

		// ����״̬��
		if (which == CMD_READTRACK3)
		{
			if (byCmdRes[3] == 0x7F)
			{
				nRet = ErrorUtil.RET_READTRACK3_FAILED;
				m_bySwInfo[0] = (byte) nRet;
				System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 6);
				return nRet;
			}
		}
		else if (which == CMD_READTRACK2 || which == CMD_READTRACK1)
		{
			if (byCmdRes[2] == 0x7F)
			{
				nRet = ErrorUtil.RET_READTRACK_FAILED;
				m_bySwInfo[0] = (byte) nRet;
				System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 5);
				return nRet;
			}
		}
		else
		{
			if (byCmdRes[2] == 0x7F)
			{
				nRet = ErrorUtil.RET_READDBTRACK_FAILED;
				m_bySwInfo[0] = (byte) nRet;
				System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 7);
				return nRet;
			}
		}

		// ��ȡ�����ôŵ�����
		nRet = doSeperateTrack_NT(which, byCmdRes, info);

		return nRet;
	}

	/**
	 * ��ȡ�ŵ�����
	 * 
	 * @param res
	 * @param resLen
	 * @param begin
	 * @param end
	 * @return
	 */
	public byte[] seperateByTrays(byte[] res, int resLen, byte begin, byte end)
	{
		boolean mark = false;
		byte[] data = new byte[MAX_RECV_LEN];
		int index = 0;

		for (int i = 0; i < resLen; i++)
		{
			if (res[i] == begin)
			{
				mark = true;
				continue;
			}
			if (res[i] == end)
			{
				return data;
			}
			if (mark)
			{
				data[index++] = res[i];
			}
		}

		return data;
	}

	public int reset()
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int byCmdReqLen = 2;
		byte[] byCmdReq = COMMAND_RESET;

		// ����ָ��
		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen);

		// ���ô�����
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}
		else
		{
			nRet = 0;
		}

		return nRet;
	}

	public int readStatus(MagCardInfo info)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		int byCmdReqLen = 2;
		byte[] byCmdReq = COMMAND_STATUS;
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		// ����ָ�������Ӧ����
		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen, byCmdRes, byCmdRes.length, condNT_star);

		// ����ͨ�Ŵ�����
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}
		m_bySwInfo[0] = (byte) nRet;
		m_bySwInfo[5] = 0x00;
		// ��ȡ״̬��
		if (byCmdRes[3] == (byte) 0x70)
		{
			info.setStatus(statusCode(byCmdRes, byCmdRes.length));
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 4);

		}
		else if (byCmdRes[3] == (byte) 0x71)
		{
			info.setStatus(statusCode(byCmdRes, byCmdRes.length));
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 4);
			m_bySwInfo[0] = ErrorUtil.RET_READSTATUS_FAILED;
			return ErrorUtil.RET_READSTATUS_FAILED;
		}
		else if (byCmdRes[2] == (byte) 0x70)
		{
			info.setStatus(statusCode(byCmdRes, byCmdRes.length));
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 3);
			m_bySwInfo[4] = 0x00;
		}
		else if (byCmdRes[2] == (byte) 0x71)
		{
			info.setStatus(statusCode(byCmdRes, byCmdRes.length));
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 3);
			m_bySwInfo[4] = 0x00;
			m_bySwInfo[0] = ErrorUtil.RET_READSTATUS_FAILED;
			return ErrorUtil.RET_READSTATUS_FAILED;
		}
		else
		{
			m_bySwInfo = statusCode(byCmdRes, byCmdRes.length);
			return ErrorUtil.RET_READSTATUS_FAILED;
		}

		if (nRet >= 0)
		{
			m_bySwInfo[0] = ErrorUtil.RET_READSTATUS_SUCCESS;
			nRet = ErrorUtil.RET_SUCCESS;
		}

		return nRet;

	}

	/**
	 * ��ȡ�����ôŵ�����
	 * 
	 * @param which
	 *            ����������
	 * @param byCmdRes
	 *            ��Ӧ����
	 * @param info
	 *            MagInfo
	 * @return
	 */
	private int doSeperateTrack_NT(int which, byte[] byCmdRes, MagCardInfo info)
	{
		byte[] byData = new byte[MAX_RECV_LEN];

		// ��ȡ���洢��ŵ�����
		if (which == CMD_READTRACK12)
		{
			byData = seperateByTrays(byCmdRes, byCmdRes.length, (byte) 0x73, (byte) 0x41);
			info.setTrack2(byData, byData.length);

			byData = seperateByTrays(byCmdRes, byCmdRes.length, (byte) 0x41, (byte) 0x3F);
			info.setTrack1(byData, byData.length);

			// ��ȡ�ŵ��ٶ�

			int lengthVail = StringUtil.GetVailArrayLen(byData);
			if (lengthVail != 0)
			{
				info.setSpeed(byData[lengthVail - 1]);
			}
			return ErrorUtil.RET_SUCCESS;
		}
		else if (which == CMD_READTRACK23)
		{
			byData = seperateByTrays(byCmdRes, byCmdRes.length, (byte) 0x73, (byte) 0x41);
			info.setTrack2(byData, byData.length);

			byData = seperateByTrays(byCmdRes, byCmdRes.length, (byte) 0x41, (byte) 0x3F);
			info.setTrack3(byData, byData.length);

			// ��ȡ�ŵ��ٶ�
			int lengthVail = StringUtil.GetVailArrayLen(byData);
			if (lengthVail != 0)
			{
				info.setSpeed(byData[lengthVail - 1]);
			}

			return ErrorUtil.RET_SUCCESS;
		}

		// ��ȡ���洢���ŵ�����
		if (which == CMD_READTRACK1 || which == CMD_READTRACK2)
			byData = seperateByTrays(byCmdRes, byCmdRes.length, (byte) 0x73, (byte) 0x3F);
		else if (which == CMD_READTRACK3)
		{
			byData = seperateByTrays(byCmdRes, byCmdRes.length, (byte) 0x41, (byte) 0x3F);
		}

		switch (which)
		{
		case CMD_READTRACK1:
			info.setTrack1(byData, byData.length);
			break;
		case CMD_READTRACK2:
			info.setTrack2(byData, byData.length);
			break;
		case CMD_READTRACK3:
			info.setTrack3(byData, byData.length);
			break;

		default:
			break;
		}

		// ��ȡ�ŵ��ٶ�
		int lengthVail = StringUtil.GetVailArrayLen(byData);
		if (lengthVail != 0)
		{
			info.setSpeed(byData[lengthVail - 1]);
		}
		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * ��������ָ��
	 * 
	 * @param which
	 * @param byCmdReq
	 * @param byCmdReqLen
	 * @param check
	 * @return
	 */
	private byte[] cmdReq_NT(int which, byte[] byCmdReq)
	{
		switch (which)
		{
		case CMD_READTRACK1:
			byCmdReq = COMMAND_RD1;
			break;

		case CMD_READTRACK2:
			byCmdReq = COMMAND_RD2;
			break;

		case CMD_READTRACK3:
			byCmdReq = COMMAND_RD3;
			break;

		case CMD_READTRACK12:
			byCmdReq = COMMAND_RD12;
			break;

		case CMD_READTRACK23:
			byCmdReq = COMMAND_RD23;
			break;

		default:
			break;
		}

		return byCmdReq;
	}

	public byte[] statusCode(byte[] res, int resLen)
	{
		boolean mark = false;
		byte[] data = new byte[MAX_RECV_LEN];
		int index = 0;

		for (int i = 0; i < resLen; i++)
		{
			if (res[i] == 0x1B)
			{
				mark = true;
				continue;
			}
			if (res[i] == 0x70)
			{
				return data;
			}
			if (res[i] == 0x71)
			{
				return data;
			}
			if (mark)
			{
				data[index++] = res[i];
			}
		}

		// ������Ч����
		byte[] newdata = new byte[index];
		System.arraycopy(data, 0, newdata, 0, index);
		return newdata;
	}

	/**
	 * ���÷ָ���~
	 * 
	 * @return
	 */
	public int setSepTilde()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] cmd = { 0x20, 0x01, 0x7E };
		byte[] res = new byte[MAX_RECV_LEN];

		// ���ͻ�ȡ�豸��Ϣָ��
		nRet = sendCmd(cmd, 3, res, res.length);

		if (nRet <= 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(res, 0, m_bySwInfo, 1, 2);

			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * ���÷ָ���|
	 * 
	 * @return
	 */
	public int setSepOr()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] cmd = { 0x20, 0x01, 0x7C };
		byte[] res = new byte[MAX_RECV_LEN];

		// ���ͻ�ȡ�豸��Ϣָ��
		nRet = sendCmd(cmd, 3, res, res.length);

		if (nRet <= 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(res, 0, m_bySwInfo, 1, 2);

			return nRet;
		}

		return ErrorUtil.RET_SUCCESS;
	}

	/**
	 * ���ŵ�
	 * 
	 * @param info
	 * @return
	 */
	public int readTrack(MagCardInfo info)
	{
		this.spilt = "";
		if (isCT)
		{
			return readTrack_CT(info);
		}
		else
		{
			return readTrack_NT(info, which);
		}
	}

	/**
	 * �����ڴŵ�����
	 * 
	 * @param info
	 * @return
	 */
	private int readTrack_CT(MagCardInfo info)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		byte[] cmd = { 0x20, 0x02 };
		byte[] res = new byte[MAX_RECV_LEN];

		// ���ͻ�ȡ�豸��Ϣָ��
		nRet = sendCmd(cmd, 2, res, res.length);

		if (nRet <= 0)
		{
			Log.e(TAG, "sendCmd error, nRet:" + nRet);

			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(res, 0, m_bySwInfo, 1, 2);

			return nRet;
		}

		// ��ȡ�����ŵ�����
		seperateByTrays(res.length, res, info);

		return ErrorUtil.RET_SUCCESS;
	}

	public void seperateByTrays(int length, byte[] res, MagCardInfo info)
	{
		int j = 0;
		int track1Length = 0;
		int track2Length = 0;
		int track3Length = 0;

		byte[] track1 = new byte[MAX_RECV_LEN];
		byte[] track2 = new byte[MAX_RECV_LEN];
		byte[] track3 = new byte[MAX_RECV_LEN];

		for (int i = 0; i < res.length; i++)
		{
			if (res[i] == 126)
				this.spilt = "~";
			else if (res[i] == 124)
			{
				this.spilt = "|";
			}

			if (res[i] != 0x7E && res[i] != 0x7C)
			{
				if (j == 0)
				{
					track1[track1Length] = res[i];
					track1Length++;
				}
				else if (j == 1)
				{
					track2[track2Length] = res[i];
					track2Length++;
				}
				else if (j == 2)
				{
					track3[track3Length] = res[i];
					track3Length++;
				}
			}
			else
			{
				j++;
			}
		}

		byte[] track1Data = new byte[MAX_RECV_LEN];
		System.arraycopy(track1, 2, track1Data, 0, track1.length - 2); // ��track1ǰ��λ״̬λ0ȥ��

		// �洢�ŵ�����
		info.setTrack1(track1Data, track1.length - 2);
		info.setTrack2(track2, track2Length);
		info.setTrack3(track3, track3Length);

		// ��ȡ�ŵ��ٶ�
		byte[] speed = new byte[MAX_RECV_LEN];
		System.arraycopy(res, 2, speed, 0, res.length - 2);

		int lengthVail = StringUtil.GetVailArrayLen(speed);
		if (lengthVail != 0)
		{
			info.setSpeed(speed[lengthVail - 1]);
		}
	}

	/**
	 * ����һ��ָ�����ֵΪ��ȡ���ݳ���,ʱ�䵥λms
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
		byte[] byCmdReq = new byte[MAX_SEND_LEN];
		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		// ���״̬����
		this.m_bySwInfo = new byte[1024];

		byCmdReqLen = TransDataFormat.DataFormat((byte) SOH, (byte) EOT, byReq, nReqLen, byCmdReq, byCmdReq.length);

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen, byCmdRes, byCmdRes.length, cond);

		if (nRet < 0)
		{
			return nRet;
		}

		// ��ȡ��0x04
		int nPkgLen = getPkgLen(byCmdRes, nRet);
		nRet = TransDataFormat.CheckAndGetResponse((byte) SOH, (byte) EOT, byCmdRes, nPkgLen, byRes, nResLen);

		if (byRes[0] != 0x00 || byRes[1] != 0x00)
		{
			if (ErrorUtil.LOG_DEBUG)
			{
				Log.e(TAG, "�豸��Ϣ�ϲ�����ʧ��");
			}

			return ErrorUtil.RET_OPERATE_FAILED;
		}

		return nRet;
	}

	public int getSpeed()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		int byCmdReqLen = 0;
		byte[] byCmdReq = new byte[MAX_SEND_LEN];
		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		byte[] byRes = new byte[MAX_RECV_LEN];
		// ���״̬����
		this.m_bySwInfo = new byte[1024];

		byte[] byReq = { 0x20, 0x11, 0x00 };
		byCmdReqLen = TransDataFormat.DataFormat((byte) SOH, (byte) EOT, byReq, byReq.length, byCmdReq,
				byCmdReq.length);

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen, byCmdRes, byCmdRes.length, cond);

		if (nRet < 0)
		{
			return nRet;
		}

		// ��ȡ��0x04
		int nPkgLen = getPkgLen(byCmdRes, nRet);

		nRet = TransDataFormat.CheckAndGetResponse((byte) SOH, (byte) EOT, byCmdRes, nPkgLen, byRes, byRes.length);

		if (byRes[0] == 0x00)
		{
			if (ErrorUtil.LOG_DEBUG)
			{
				Log.d(TAG, "��ȡ�ŵ��ٶȳɹ�");
			}

			return (short) (byRes[2] & 0xff);
		}

		return nRet;
	}

	/**
	 * ���ô�������ʱʱ��(T5)
	 * 
	 * @param mMinakey
	 * @param mKeyLength
	 * @param mworkKey
	 * @return
	 */
	public int setTimeOut(byte timeout)
	{

		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		byte[] byReqData = new byte[MAX_SEND_LEN];
		byte[] byCmdReq = { 0x20, 0x04 };
		System.arraycopy(byCmdReq, 0, byReqData, 0, 2);
		nReqLen += 2;

		byReqData[nReqLen] = timeout;
		nReqLen += 1;

		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		// ����ָ��
		nRet = sendCmd(byReqData, nReqLen, byCmdRes, byCmdRes.length);

		m_bySwInfo = new byte[1025];
		// ����ͨ�Ŵ�����
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 2);
			return nRet;
		}
		if (byCmdRes[0] == 0x00)
		{
			return ErrorUtil.RET_SUCCESS;
		}
		else
		{
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 2);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}
}
