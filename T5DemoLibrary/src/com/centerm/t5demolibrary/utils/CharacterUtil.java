package com.centerm.t5demolibrary.utils;

public class CharacterUtil
{

	public CharacterUtil()
	{
		
	}

	/**
	 * 
	 * @param theString
	 * @return String
	 */
	public static String unicodeToUtf8(String theString)
	{
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;)
		{
			aChar = theString.charAt(x++);
			if (aChar == '\\')
			{
				aChar = theString.charAt(x++);
				if (aChar == 'u')
				{
					int value = 0;
					for (int i = 0; i < 4; i++)
					{
						aChar = theString.charAt(x++);
						switch (aChar)
						{
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				}
				else
				{
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			}
			else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}

	/**
	 * unicode 转字符串
	 */
	public static String unicode2String(String unicode)
	{

		StringBuffer string = new StringBuffer();

		String[] hex = unicode.split("\\\\u");

		for (int i = 1; i < hex.length; i++)
		{

			// 转换出每一个代码点
			int data = Integer.parseInt(hex[i], 16);

			// 追加成string
			string.append((char) data);
		}

		return string.toString();
	}

	public static int UnicodeToUFT8(byte[] pUnicodeBuf, int nUnicodeBufPos, int nUnicodeBufLen, byte[] pUtfBuf,
			int nUtfBufPos, int nUtfBufSize)
	{
		if ((pUnicodeBuf == null) || (pUtfBuf == null))
		{
			return 0;
		}

		char wch;
		int count = nUtfBufPos;

		for (int i = nUnicodeBufPos; (i < nUnicodeBufPos + nUnicodeBufLen) && (count < (nUtfBufSize - 1)); i += 2)
		{
			// int low = pUnicodeBuf[i] & 0xff;
			// int high = (pUnicodeBuf[i+1] << 8) & 0xff00;
			// int te = low + high;
			wch = (char) ((pUnicodeBuf[i] & 0xff) + ((pUnicodeBuf[i + 1] << 8) & 0xff00));
			if (wch < 0x0080)
			{
				pUtfBuf[count++] = (byte) wch;
			}
			else if (wch < 0x0800)
			{
				pUtfBuf[count++] = (byte) (0xC0 | (byte) (wch >> 6));
				pUtfBuf[count++] = (byte) (0x80 | (byte) (wch & 0x3F));
			}
			else
			{
				pUtfBuf[count++] = (byte) (0xE0 | (byte) (wch >> 12));
				pUtfBuf[count++] = (byte) (0x80 | (byte) ((wch >> 6) & 0x3F));
				pUtfBuf[count++] = (byte) (0x80 | (byte) (wch & 0x3F));
			}
		}

		pUtfBuf[count] = '\0';

		return count;
	}
}
