package com.centerm.t5demolibrary.utils;

import com.centerm.t5demolibrary.utils.StringUtil;

import android.util.Log;

public class TransDataFormat
{
	private static final String TAG = "TransDataFormat";
	public static final int PACKET_STX = 1;
	public static final int PACKET_DATALEN = 2;
	public static final int PACKET_LRC = 1;
	public static final int PACKET_ETX = 1;

	public static final int PACKET_PARAMS = (PACKET_STX + ((PACKET_DATALEN + PACKET_LRC) * 2) + PACKET_ETX);

	public TransDataFormat()
	{
		
	}

	public static int DataFormat(byte byHead, byte byTail, byte[] bySrc, int nSrcLen, byte[] byDest, int nDestLen)
	{
		int nRet = 0;
		if (bySrc != null && byDest != null && nDestLen >= (nSrcLen * 2) + PACKET_PARAMS)
		{
			int i = 0;

			// 包头
			byDest[nRet] = byHead;
			nRet++;

			// 提取DataLen
			byte[] byData = new byte[3 + nSrcLen];
			byData[0] = (byte) ((nSrcLen >> 8) & 0xFF);
			byData[1] = (byte) (nSrcLen & 0xFF);
			System.arraycopy(bySrc, 0, byData, 2, nSrcLen);

			byte byLrc = 0x00;
			for (i = 0; i < nSrcLen; i++)
			{
				byLrc = (byte) (byLrc ^ bySrc[i]);
			}

			byData[nSrcLen + 3 - 1] = byLrc;
			byte[] bySplitData = new byte[2 * (3 + nSrcLen)];
			StringUtil.SplitChar(byData, 0, byData.length, bySplitData, bySplitData.length);

			// 拆分数据体
			System.arraycopy(bySplitData, 0, byDest, 1, (nSrcLen + 3) * 2);
			nRet += (nSrcLen + 3) * 2;

			// 包尾
			byDest[nRet] = byTail;
			nRet++;
		}

		return nRet;
	}

	public static int CheckAndGetResponse(byte SOH, byte EOT, byte[] bySrc, int nSrcLen, byte[] byDest, int nDestLen)
	{
		int nRet = 0;
		// 匹配头尾是否为 \x01 \x04
		if (bySrc[0] != SOH || bySrc[nSrcLen - 1] != EOT)
		{
			return 0;
		}

		// 合并01和04之间的数据
		byte[] byResponse = new byte[512];
		int nLen = StringUtil.Merge(bySrc, 1, nSrcLen - 2, byResponse, byResponse.length);
		if (nLen <= 0)
		{
			return 0;
		}

		// 提取长度
		int high = (byResponse[0] << 8) & 0xff00;
		int low = byResponse[1] & 0xff;
		int nDataLen = high + low;
		if (nDataLen != nLen - 3 || nDataLen > nDestLen)
		{
			return 0;
		}

		// 计算校验值
		int i = 0;
		byte byLrc = 0x00;
		for (i = 2; i < nLen - 1; i++)
		{
			byLrc ^= byResponse[i];
		}

		// 判断校验值
		if (byLrc != byResponse[nLen - 1])
		{
			return 0;
		}

		System.arraycopy(byResponse, 2, byDest, 0, nDataLen);
		nRet = nDataLen;
		return nRet;
	}

	public static int SD_DataFormat(byte byHead, byte byTail, byte[] bySrc, int nSrcLen, byte[] byDest, int nDestLen)
	{
		int nRet = 0;
		if (bySrc != null && byDest != null && nDestLen >= (nSrcLen * 2) + PACKET_PARAMS)
		{
			int i = 0, nLrcLen = 0;
			byte byLrc = 0x00;

			nLrcLen = 2 + nSrcLen;
			byte[] byData = new byte[nLrcLen + 1];

			// 包头
			byData[0] = byHead;

			// 提取DataLen
			byData[1] = (byte) (nSrcLen & 0xFF);

			// 数据单元
			System.arraycopy(bySrc, 0, byData, 2, nSrcLen);

			// SOH 、Data_len 、Data 各字节的异或值
			for (i = 0; i < nLrcLen; i++)
			{
				byLrc = (byte) (byLrc ^ byData[i]);
			}

			byData[nLrcLen] = byLrc;

			// 拆分数据体
			int nLen = (nLrcLen + 1) * 2;
			byte[] bySplitData = new byte[nLen];
			bySplitData = StringUtil.HexToStringA(byData, nLrcLen + 1).getBytes();

			System.arraycopy(bySplitData, 0, byDest, 0, nLen);
			nRet += nLen;

			// 包尾
			byDest[nRet++] = byTail;
		}

		return nRet;
	}

	public static int SD_CheckAndGetResponse(byte SOH, byte EOT, byte[] bySrc, int nSrcLen, byte[] byDest, int nDestLen)
	{
		int nRet = 0;

		// 匹配头尾
		if (bySrc[nSrcLen - 1] != EOT)
		{
			Log.e(TAG, "数据包格式错误!");
			return 0;
		}

		// 合并01和04之间的数据
		byte[] byResponse = new byte[512];
		byResponse = StringUtil.StringToHexA(bySrc, nSrcLen - 1);
		int nLen = byResponse.length;
		if (nLen <= 0)
		{
			Log.e(TAG, "接收数据不全:" + nLen);
			return 0;
		}

		// for (int j = 0; j < nLen; j++)
		// {
		// Log.i(TAG, "****SD_CheckAndGetResponse, byResponse[" + j + "]:"
		// + String.format("%02x", byResponse[j]));
		// }

		// 提取长度
		int nDataLen = byResponse[1];
		if (nDataLen != nLen - 3 || nDataLen > nDestLen)
		{
			Log.e(TAG, "数据长度错误, nLen:" + nLen + " nDataLen:" + nDataLen);
			return 0;
		}

		// 计算校验值
		int i = 0;
		byte byLrc = 0x00;
		for (i = 0; i < nLen - 1; i++)
		{
			byLrc ^= byResponse[i];
		}

		// 判断校验值
		if (byLrc != byResponse[nLen - 1])
		{
			Log.e(TAG, "校验值验证错误:" + byLrc + " 返回校验：" + String.format("%02x", byResponse[nLen - 1]));
			return 0;
		}

		System.arraycopy(byResponse, 2, byDest, 0, nDataLen);
		nRet = nDataLen;

		return nRet;
	}

}
