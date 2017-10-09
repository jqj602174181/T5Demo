package com.centerm.t5demolibrary.device.t5;

import java.util.Arrays;

import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.DesUtil;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;
import com.centerm.t5demolibrary.utils.TransDataFormat;

import android.util.Log;

public class T5Cmd extends T5BaseCmd{

	private static final String TAG = "T5Cmd";
	private static T5Cmd instance;

	private T5Cmd(){}

	// 创建实例
	public static T5Cmd getInstance()
	{
		if (instance == null)
		{
			synchronized (T5Cmd.class) {
				instance = new T5Cmd();
			}
		}
		return instance;
	}

	public void setSwInfo(int ret)
	{
		m_bySwInfo[0] = (byte) ret;
	}

	public byte[] getSwData()
	{
		return m_bySwInfo;
	}

	public int bytesToInt(byte[] src, int offset)
	{
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
				| ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
		return value;
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
	private int sendCmd_Voice(byte[] byReq, int nReqLen, byte[] byRes,
			int nResLen)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		int byCmdReqLen = 0;
		byte[] byCmdReq = new byte[PKG_SIZE];
		byte[] byCmdRes = new byte[PKG_SIZE];

		byCmdReqLen = TransDataFormat.DataFormat(SOH_v, EOT_v, byReq, nReqLen,
				byCmdReq, byCmdReq.length);

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReqLen,
				byCmdRes, byCmdRes.length, cond_voice);
		if (nRet < 0)
		{
			return nRet;
		}

		// 截取到0x03
		int nPkgLen = getPkgLen(byCmdRes, nRet, EOT_v);
		nRet = TransDataFormat.CheckAndGetResponse(SOH_v, EOT_v, byCmdRes, nPkgLen,
				byRes, nResLen);

		return nRet;
	}

	/**
	 * 启动动画
	 */
	public int start_mv(byte type)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x1B, 0x5B, 0x38, 0x4F, 0x50, 0x45, 0x4E, 0x43,
				0x48, 0x41, 0x4E, 0x4E, 0x45, 0x4C, 0x02, (byte) (0x30 | type),
				0x03 };
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

		return nRet;
	}

	/**
	 * 关闭动画
	 */
	public int stop_mv(byte type)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x1B, 0x5B, 0x38, 0x43, 0x4C, 0x4F, 0x53, 0x45,
				0x43, 0x48, 0x41, 0x4E, 0x4E, 0x45, 0x4C, 0x02,
				(byte) (0x30 | type), 0x03 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length);

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

		return nRet;
	}

	/**
	 * 启动卡机动画
	 */
	public int play_mv()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x1B, 0x5B, 0x38, 0x4F, 0x50, 0x45, 0x4E, 0x43,
				0x48, 0x41, 0x4E, 0x4E, 0x45, 0x4C, 0x02, (byte) (0x30 | 0x01),
				0x03 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN, cond);
		m_bySwInfo = new byte[1025];
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

		return nRet;

	}

	/**
	 * 登记指纹
	 */
	public int checkFingerData(byte timeout)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x0B, timeout, 0x00, 0x00 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		m_bySwInfo = new byte[1025];

		// 发送指令
		nRet = sendCmd(byCmdReq, byCmdReq.length, byCmdRes, byCmdRes.length);

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
			m_bySwInfo[0] = ErrorUtil.RET_T5_CHECK_FINGER;
			System.arraycopy(byCmdRes, 2, m_bySwInfo, 1, 384);
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
	 * 安全认证
	 */
	public int safe(byte[] sourceKey)
	{
		int nRet = ErrorUtil.RET_SUCCESS;
		String sourceKeyStr = DesUtil.bytesToHexString(sourceKey);
		Log.e(TAG, "Source key is" + sourceKeyStr);

		byte[] encryKey = DesUtil.encrypt(sourceKey, DesUtil.NORMAL_KEY);
		String encryKeyStr = DesUtil.bytesToHexString(encryKey);
		Log.e(TAG, "encrypt key is " + Arrays.toString(encryKey));

		byte[] workKey = conver_string_param(encryKeyStr);

		int index = 0;
		byte[] byCmdReq = new byte[6 + workKey.length];
		byCmdReq[index] = 0x1B;
		index++;
		byCmdReq[index] = 0x5B;
		index++;
		byCmdReq[index] = 0x30;
		index++;
		byCmdReq[index] = 0x44;
		index++;
		byCmdReq[index] = 0x02;
		index++;
		System.arraycopy(workKey, 0, byCmdReq, index, workKey.length);
		index += workKey.length;
		byCmdReq[index] = 0x03;
		index++;
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN, cond);

		m_bySwInfo = new byte[1025];
		// 设置通信错误码
		if (nRet < 0)
		{
			m_bySwInfo[0] = (byte) nRet;
			m_bySwInfo[1] = 0x00;
			m_bySwInfo[2] = 0x00;

			return nRet;
		}
		if (byCmdRes[1] == 0x55)
		{
			nRet = ErrorUtil.RET_T5_FAILED;
			m_bySwInfo[0] = (byte) nRet;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, 3);
			return nRet;
		}

		// 添加一个截去FF
		int receiveLength = StringUtil.GetVailArrayLen(byCmdRes, (byte) 0x03);
		nRet = StringUtil.Merge(byCmdRes, 2, receiveLength - 2, m_bySwInfo, 1,
				1024);

		// 选择密钥解密数据
		byte[] returnResult = new byte[(receiveLength - 2) / 2];
		boolean isRandKey = false;
		if (((sourceKey[0] ^ sourceKey[9]) == (byte) 0xff)
				&& ((sourceKey[3] ^ sourceKey[11]) == (byte) 0xff))
		{
			isRandKey = true;
		}
		System.arraycopy(m_bySwInfo, 1, returnResult, 0, returnResult.length);
		byte[] decodeData = decodeData(isRandKey, returnResult);

		// 异或还原key1值
		byte[] checkStr = { 0x43, 0x45, 0x4E, 0x54, 0x45, 0x52, 0x4D, 0x3D,
				0x3D, 0x5A, 0x4E, 0x5A, 0x44, 0x43, 0x50, 0x42 }; // CENTERM==ZNZDCPB
		// ASCII码
		byte[] checkData = new byte[decodeData.length];
		for (int i = 0; i < decodeData.length; i++)
		{
			checkData[i] = (byte) (decodeData[i] ^ checkStr[i]);
		}

		// 验证是否认证成功
		String calcKeyStr = DesUtil.bytesToHexString(checkData);
		if (calcKeyStr.equals(sourceKeyStr))
		{
			m_bySwInfo[0] = ErrorUtil.RET_T5_SAFE_SUCCESS;
			return ErrorUtil.RET_T5_SAFE_SUCCESS;
		} else
		{
			m_bySwInfo[0] = ErrorUtil.RET_T5_SAFE_FAILED;
			return ErrorUtil.RET_T5_SAFE_FAILED;
		}

	}

	private byte[] decodeData(boolean isRandKey, byte[] data)
	{
		if (isRandKey)
		{
			return DesUtil.decrypt(data, DesUtil.RANDOM_KEY);
		} else
		{
			return DesUtil.decrypt(data, DesUtil.NORMAL_KEY);
		}
	}

	public byte[] conver_string_param(String str)
	{
		byte[] result = new byte[str.length()];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = (byte) (0x30 | StringUtil.ToByte(str.charAt(i)));
		}
		return result;
	}

	/**
	 * @return
	 */
	public int enterPassword_SD()
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x1B, 0x5B, 0x38, 0x4F, 0x50, 0x45, 0x4E, 0x43,
				0x48, 0x41, 0x4E, 0x4E, 0x45, 0x4C, 0x02, (byte) (0x30 | 0x01),
				0x03 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = TransControl.getInstance().Transfer(byCmdReq, byCmdReq.length,
				byCmdRes, MAX_RECV_LEN, cond);

		m_bySwInfo = new byte[1025];
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

		return nRet;
	}

	/**
	 * @return
	 */
	public int enterFingerData(byte timeout)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x0c, timeout, 0x00, 0x00 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = sendCmd(byCmdReq, byCmdReq.length, byCmdRes, MAX_RECV_LEN);

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
			m_bySwInfo[0] = ErrorUtil.RET_T5_FINGER_DATA;
			System.arraycopy(byCmdRes, 2, m_bySwInfo, 1, 384);
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
	 * @param conver_string_param
	 */
	public int getDeviceInfo(byte deviceType, byte[] data)
	{
		int nRet = ErrorUtil.RET_SUCCESS;

		byte[] byCmdReq = { 0x09, 0x00, deviceType, 0x00 };
		byte[] byCmdRes = new byte[MAX_RECV_LEN];

		nRet = sendCmd(byCmdReq, byCmdReq.length, byCmdRes, MAX_RECV_LEN);

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
			m_bySwInfo[0] = ErrorUtil.RET_DEV_SUCCESS;
			System.arraycopy(byCmdRes, 2, m_bySwInfo, 1, 64);
			return nRet;
		} else
		{
			m_bySwInfo[0] = ErrorUtil.RET_DEV_FAILED;
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length - 1);
			nRet = ErrorUtil.RET_DEV_FAILED;
			return nRet;
		}
	}

	/**
	 * 蜂鸣器控制
	 * 
	 * @param mMinakey
	 * @param mKeyLength
	 * @param mworkKey
	 * @return
	 */
	public int setVoiceController(byte[] delayTime, byte times)
	{

		int nRet = ErrorUtil.RET_SUCCESS;
		int nReqLen = 0;
		byte[] byReqData = new byte[PKG_SIZE];

		byte[] byCmdReq = { (byte) 0x80, 0x15 };
		System.arraycopy(byCmdReq, 0, byReqData, 0, 2);
		nReqLen += 2;
		System.arraycopy(delayTime, 0, byReqData, nReqLen, delayTime.length);
		nReqLen += delayTime.length;
		byReqData[nReqLen] = times;
		nReqLen += 1;

		byte[] byCmdRes = new byte[MAX_RECV_LEN];
		// 发送指令
		nRet = sendCmd_Voice(byReqData, nReqLen, byCmdRes, byCmdRes.length);

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
			System.arraycopy(byCmdRes, 0, m_bySwInfo, 1, byCmdRes.length - 1);
			return ErrorUtil.RET_OPERATE_FAILED;
		}
	}
}
