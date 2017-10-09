package com.centerm.t5demolibrary.device.t5;

import android.util.Log;

import com.centerm.t5demolibrary.interfaces.EndOfRead;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;

public class T5BaseCmd {

	private static final String TAG = "T5BaseCmd";

	protected static final int MAX_RECV_LEN = 1024;
	protected static final int MAX_SEND_LEN = 1024;
	protected static final int PKG_SIZE = 1024;
	protected final byte SOH = 0x02;
	protected final byte EOT = 0x03;
	protected final byte SOH_v = 0x01;
	protected final byte EOT_v = 0x04;
	protected byte[] m_bySwInfo = new byte[1025];
	protected byte[] picSize, picMD5;

	// ����ָ��
	protected static final int CMD_READTRACK1 = 1;
	protected static final int CMD_READTRACK2 = 2;
	protected static final int CMD_READTRACK3 = 3;
	protected static final int CMD_READTRACK12 = 12;
	protected static final int CMD_READTRACK23 = 23;
	protected static final int CMD_UNDEFINE = 0;

	// ���������������
	protected static final byte[] COMMAND_RESET = { (byte) 0x1B, (byte) 0x30 }; // ��λ
	protected static final byte[] COMMAND_STATUS = { (byte) 0x1B, (byte) 0x6A }; // ����ִ�в�����״̬��

	protected static final byte[] COMMAND_RD1 = { (byte) 0x1B, (byte) 0x72 }; // ��1�ŵ�
	protected static final byte[] COMMAND_RD2 = { (byte) 0x1B, (byte) 0x5D }; // ��2�ŵ�
	protected static final byte[] COMMAND_RD3 = { (byte) 0x1B, (byte) 0x54, (byte) 0x5D }; // ��3�ŵ�
	protected static final byte[] COMMAND_RD12 = { (byte) 0x1B, (byte) 0x44, (byte) 0x5D }; // ��12�ŵ�
	protected static final byte[] COMMAND_RD23 = { (byte) 0x1B, (byte) 0x42, (byte) 0x5D }; // ��23�ŵ�

	protected static final byte[] byPreamble = { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69 };

	public String getSwInfo() //�Y���������ؽӿ�
	{
		StringBuffer strBuffer = new StringBuffer();
		String format = null;

		switch (m_bySwInfo[0]) {
		case ErrorUtil.RET_TIMEOUT:
			strBuffer.append("������Ӧ���ݳ�ʱ!");
			break;

		case ErrorUtil.RET_COMMUNICATE:
			strBuffer.append("ͨ��ʧ��,�����ԣ�");
			break;

		case ErrorUtil.RET_FIND_INTERFACE:
			strBuffer.append("�豸�ѶϿ�!");
			break;

		case ErrorUtil.RET_SUCCESS:
			format = "״̬��:%02XH %02XH %02XH";
			strBuffer.append(String.format(format, m_bySwInfo[1] & 0xFF,
					m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF));
			break;

		case ErrorUtil.RET_T5_KEY_SUCCESS:
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo,
					(byte) 0xff); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_SAFE_SUCCESS:
			strBuffer.append("���յ��ļ���������:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_SAFE_FAILED:
			strBuffer.append("���յ��ļ���������:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_JHPWD_SUCCESS:
			strBuffer.append("����������̷���ֵ:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo,
					(byte) 0x00, 3); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_CHECK_FINGER:
			strBuffer.append("ָ������:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo,
					(byte) 0x00, 3); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_FINGER_DATA:
			strBuffer.append("ָ��ģ��:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo,
					(byte) 0x00, 5); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_FAILED:
			strBuffer.append("״̬��:%02XH %02XH %02XH");
			strBuffer.append(String.format(format, m_bySwInfo[1] & 0xFF,
					m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF, m_bySwInfo[0]));
			break;

		case ErrorUtil.RET_DEV_FAILED:
			strBuffer.append("״̬��:%02XH %02XH");
			strBuffer.append(String.format(format, m_bySwInfo[1] & 0xFF,
					m_bySwInfo[2] & 0xFF, m_bySwInfo[0]));
			break;

		case ErrorUtil.RET_DEV_SUCCESS:
			strBuffer.append("������:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo,
					(byte) 0x00, 2); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		case ErrorUtil.RET_T5_SIGN_SUCCESS:
			strBuffer.append("�ļ���С:");
			strBuffer.append(StringUtil.AsciiToString(picSize));
			strBuffer.append("  �ļ�MD5:");
			strBuffer.append(StringUtil.AsciiToString(picMD5));
			break;

		case ErrorUtil.RET_NOVALUE_ERROR:
			strBuffer.append("�����Ϊ��!");
			break;

		case ErrorUtil.RET_OUTOFVALUE_ERROR:
			strBuffer.append("�����������ָ�����ȣ�������32λ�����!");
			break;

		case ErrorUtil.RET_T5_FINGER_SUCCESS:
			strBuffer.append("ָ������:");
			for (int i = 1; i < StringUtil.GetVailArrayLen(m_bySwInfo,
					(byte) 0x00, 3); i++)
			{
				format = "%02XH";
				strBuffer.append(String.format(format, m_bySwInfo[i] & 0xff));
				strBuffer.append(" ");
			}
			break;

		default:
			format = "״̬��:%02XH %02XH %02XH, ������:%d  ";
			strBuffer.append(String.format(format, m_bySwInfo[1] & 0xFF,
					m_bySwInfo[2] & 0xFF, m_bySwInfo[3] & 0xFF, m_bySwInfo[0]));
			break;
		}

		if (ErrorUtil.LOG_DEBUG){
			Log.i(TAG, "getSwInfo+++strInfo:" + strBuffer.toString());
		}

		return strBuffer.toString();
	}

	protected EndOfRead cond_voice = new EndOfRead()
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			for (int i = 0; i < len; i++)
			{
				if (buf[i] == EOT_v)
				{
					return true;
				}
			}

			return false;
		}
	};

	protected EndOfRead condNT_star = new EndOfRead()
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			for (int i = 0; i < len; i++)
			{
				if (buf[i] == 0x70 || buf[i] == 0x71)
				{
					return true;
				}
			}

			return false;
		}
	};

	/**
	 * ����ָ��
	 */
	protected EndOfRead condNT = new EndOfRead()
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			for (int i = 0; i < len; i++)
			{
				if (buf[i] == 0x1C)
				{
					return true;
				}
			}

			return false;
		}
	};

	protected EndOfRead cond_sign = new EndOfRead() //����ǩ��
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			if(len == 1024)
			{
				return false;
			}
			return false;
		}
	};

	protected EndOfRead cond_id = new EndOfRead() //���֤
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			int nCounts = 0;

			if (len > 7)
			{
				nCounts = buf[6] + buf[5] * 256 + 7;
			}

			return (len > 7 && len >= nCounts);
		}

	};

	protected EndOfRead cond_keypad = new EndOfRead() //�������
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{

			for (int i = 0; i < len; i++)
			{
				if (buf[i] == 0x0D)
				{
					return true;
				}
			}
			return false;
		}
	};

	protected EndOfRead cond = new EndOfRead() //ͨ��
	{

		@Override
		public boolean reach(byte[] buf, int len)
		{
			for (int i = 0; i < len; i++)
			{
				if (buf[i] == EOT)
				{
					return true;
				}
			}
			return false;
		}
	};

	protected int getPkgLen(byte[] buf, int len)
	{
		int nPkgLen = 0;
		for (int i = 0; i < len; i++)
		{
			if (buf[i] == EOT)
			{
				nPkgLen = i + 1;
				break;
			}
		}

		return nPkgLen;
	}

	protected int getPkgLen(byte[] buf, int len, byte EOT)
	{
		int nPkgLen = 0;
		for (int i = 0; i < len; i++)
		{
			if (buf[i] == EOT)
			{
				nPkgLen = i + 1;
				break;
			}
		}

		return nPkgLen;
	}

	protected byte[] separateSignData(byte[] res, int resLen, byte begin, byte end)
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

}
