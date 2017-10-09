package com.centerm.t5demolibrary.utils;

import java.util.Arrays;

public class StringUtil
{
	public static final String TAG = "StringUtil";

	public StringUtil()
	{

	}

	public static String FormatDate(String strDate)
	{
		String strYear = strDate.substring(0, 4);
		String strMoth = strDate.substring(4, 6);
		String strDay = strDate.substring(6, 8);

		return strYear + "." + strMoth + "." + strDay;
	}

	public static String ByteToString(byte[] byBuf)
	{
		int len = GetVailArrayLen(byBuf);
		String strBuf = new String(byBuf, 0, len);
		return strBuf;
	}

	public static int GetVailArrayLen(byte[] byBuf)
	{
		for (int i = 0; i < byBuf.length; i++)
		{
			if (byBuf[i] == 0)
				return i;
		}
		return byBuf.length;
	}

	public static int GetVailArrayLen(byte[] byBuf, byte end)
	{
		for (int i = 0; i < byBuf.length; i++)
		{
			if (byBuf[i] == end)
				return i;
		}
		return byBuf.length;
	}

	public static int GetVailArrayLen(byte[] byBuf, byte end, int count)
	{
		int nCount = 0;
		for (int i = 0; i < byBuf.length; i++)
		{
			if (byBuf[i] == end)
				nCount++;
			if (nCount == count)
			{
				return i;
			}
		}
		return byBuf.length;
	}

	// 1->2
	public static String HexToStringA(byte[] s, int len)
	{
		String str = "";
		for (int i = 0; i < len; i++)
		{
			int ch = s[i];
			String strAsc = Integer.toHexString(ch);
			if (strAsc.length() == 1)
			{
				strAsc = "0" + strAsc;
			}
			else if (strAsc.length() > 2)
			{
				strAsc = strAsc.substring(strAsc.length() - 2, strAsc.length());
			}

			str = str + strAsc;
		}

		return str.toUpperCase();
	}

	// 2->1
	public static byte[] StringToHexA(byte[] byAsc, int nAscLen)
	{
		String s = new String(byAsc, 0, nAscLen);
		byte[] baKeyword = new byte[s.length() / 2];

		for (int i = 0; i < baKeyword.length; i++)
		{
			try
			{
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return baKeyword;
	}

	/**
	 * ASCII码字符串转数字字符串
	 * 
	 * @param String
	 *            ASCII字符串
	 * @return 字符串
	 */
	public static String AsciiToString(byte[] data)
	{
		int len = StringUtil.GetVailArrayLen(data);
		char[] result = new char[len];

		for (int i = 0; i < len; i++)
		{
			result[i] = (char) data[i];
		}
		String strs = new String(result);

		return strs;

	}

	/** */
	/**
	 * 把16进制字符串转换成字节数组
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] HexStringToByte(String hex)
	{
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();

		for (int i = 0; i < len; i++)
		{
			int pos = i * 2;
			result[i] = (byte) (ToByte(achar[pos]) << 4 | ToByte(achar[pos + 1]));
		}

		return result;
	}

	public static byte ToByte(char c)
	{
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static int TrimByte(byte[] buf, int len)
	{

		int st = 0;
		int en = len;

		while ((st < len) && (buf[st] >= 0 && buf[st] <= ' '))
		{
			st++;
		}

		while ((st < len) && (buf[en - 1] >= 0 && buf[en - 1] <= ' '))
		{
			en--;
		}

		int new_len = en - st;
		byte[] newBuf = new byte[new_len];

		System.arraycopy(buf, st, newBuf, 0, new_len);
		Arrays.fill(buf, (byte) 0x00);

		System.arraycopy(newBuf, 0, buf, 0, new_len);
		return (en - st);
	}

	public static int TrimByByte(byte[] buf, int len, byte c)
	{

		int en = len - 1;

		while ((en--) > 0)
		{
			if (buf[en] == c)
			{
				buf[en] = 0x00;
			}
			else
			{
				break;
			}
		}

		return en;
	}

	public static int SplitChar(byte[] bySrc, int mSrcPos, int nSrcLen, byte[] byDest, int nDestLen)
	{
		int nRet = 0;
		if (bySrc != null && byDest != null && nDestLen >= nSrcLen * 2)
		{
			int i = 0;
			for (i = 0; i < nSrcLen; i++)
			{
				int c = bySrc[i] & 0xff;
				byDest[nRet] = (byte) (c / 0x10 + 0x30);
				nRet++;
				byDest[nRet] = (byte) (c % 0x10 + 0x30);
				nRet++;
			}
		}
		return nRet;
	}

	public static int Merge(byte[] bySrc, int mSrcPos, int nSrcLen, byte[] byDest, int nDestLen)
	{
		int nRet = 0;
		if (bySrc != null && byDest != null && nDestLen >= (nSrcLen + 1) / 2)
		{
			int i = 0;
			for (i = mSrcPos; i < mSrcPos + nSrcLen; i += 2)
			{
				byte high = (byte) ((byte) (bySrc[i] & 0x0F) * 0x10);
				byte low = (byte) (bySrc[i + 1] & 0x0F);
				byte c = (byte) (high + low);
				byDest[nRet] = c;
				nRet++;
			}
		}

		return nRet;
	}

	public static int Merge(byte[] bySrc, int mSrcPos, int nSrcLen, byte[] byDest, int mDesPos, int nDestLen)
	{
		int nRet = mDesPos;
		if (bySrc != null && byDest != null && nDestLen >= (nSrcLen + 1) / 2)
		{
			int i = 0;
			for (i = mSrcPos; i < mSrcPos + nSrcLen; i += 2)
			{
				byte high = (byte) ((byte) (bySrc[i] & 0x0F) * 0x10);
				byte low = (byte) (bySrc[i + 1] & 0x0F);
				byte c = (byte) (high + low);
				byDest[nRet] = c;
				nRet++;
			}
		}

		return nRet;
	}

	public static byte BaudToAscii(int baud)
	{
		byte baudToAscii = 0x00;

		switch (baud)
		{
		case 115200:
			baudToAscii = 0x31;
			break;
		case 38400:
			baudToAscii = 0x32;
			break;
		case 19200:
			baudToAscii = 0x33;
			break;
		case 9600:
			baudToAscii = 0x34;
			break;
		default:
			break;
		}

		return baudToAscii;
	}

	public static byte[] BCDReverse(String str)
	{
		byte[] baKeyword = new byte[str.length() / 2];

		for (int i = 0; i < baKeyword.length; i++)
		{
			try
			{
				baKeyword[baKeyword.length - i
						- 1] = (byte) (0xff & Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return baKeyword;
	}

	public static byte[] StringToHexAscii(String str)
	{
		char[] temp = str.toCharArray();
		byte[] result = new byte[temp.length];
		for (int i = 0; i < temp.length; i++)
		{
			result[i] = (byte) temp[i];
		}
		return result;
	}

	/**
	 * byte数组中取int数值，本方法适用于小端的顺序.
	 * 
	 * @param src
	 *            byte数组
	 * @param offset
	 *            从数组的第offset位开始
	 * @return int数值
	 */
	public static int bytesToInt(byte[] src)
	{
		int value = 0;
		for (int i = 0; i < src.length; i++)
		{
			if (i == 0)
			{
				value = src[i];
			}
			else
			{
				value = ((src[i] & 0xFF) << i * 8) | value;
			}
		}
		return value;
	}

	/*
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序
	 * 
	 * @param src byte数组
	 * 
	 * @param offset 从数组的第offset位开始
	 * 
	 * @return int数值
	 */
	public static int bytesToInt(byte[] src, int offset)
	{
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 3] & 0xFF) << 24));
		return value;
	}

	/**
	 * byte数组中取int数值，本方法适用于大端的顺序。
	 */
	public static int bytesToInt2(byte[] src)
	{
		int value = 0;
		for (int i = src.length - 1; i < src.length; i--)
		{
			if (i == 0)
			{
				value = src[i];
			}
			else
			{
				value = ((src[i] & 0xFF) << i * 8) | value;
			}
		}
		return value;
	}

	public static String stringBytesToString(String[] str, int len)
	{
		String strFormat = "";
		for (int i = 0; i < len; i++)
		{
			strFormat += str[i];
		}
		return strFormat;

	}

	// byte转无符号的int值，保证正数
	public static int unsignedByteToInt(byte b)
	{
		return (int) b & 0xFF;
	}

	public static byte[] intToBytes(Integer value)
	{

		if (value >= 100)
		{
			byte[] result = new byte[3];
			result[0] = (byte) (0x30 | (value / 100));
			value = value % 100;
			result[1] = (byte) (0x30 | (value / 10));
			value = value % 10;
			result[2] = (byte) (0x30 | value);
			return result;
		}
		else if (value >= 10)
		{
			byte[] result = new byte[2];
			result[0] = (byte) (0x30 | (value / 10));
			value = value % 10;
			result[1] = (byte) (0x30 | value);
			return result;
		}
		else
		{
			byte[] result = new byte[2];
			result[0] = 0x30;
			result[1] = (byte) (0x30 | value);
			return result;
		}
	}

	public byte[] stringToBytes(String str)
	{
		byte[] result = new byte[str.length()];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = (byte) (0x30 | ToByte(str.charAt(i)));
		}
		return result;
	}

	/**
	 * 将int数值转换为占{length}个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。
	 */
	public static byte[] BigEndBytes(int value, byte length)
	{
		byte[] src = new byte[length];
		for (int i = 0; i < length; i++)
		{
			src[length - i - 1] = (byte) ((value >> ((i) * 8)) & 0xFF);
		}
		return src;
	}

	/**
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。
	 * 
	 * @param value
	 *            要转换的int值
	 * @return byte数组
	 */
	public static byte[] SmallEndBytes(int value, byte length)
	{
		byte[] src = new byte[length];
		for (int i = length; i > 0; i--)
		{
			src[i - 1] = (byte) ((value >> ((i - 1) * 8)) & 0xFF);
		}
		return src;
	}
}
