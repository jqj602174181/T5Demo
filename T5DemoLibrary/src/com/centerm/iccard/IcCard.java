package com.centerm.iccard;

import com.centerm.t5demolibrary.utils.RetUtil;

public class IcCard extends FinancialBase {

	public static final int IC_INFO = 1;
	public static final int IC_ARQC = 2;
	public static final int IC_DETAIL = 3;
	public static final int IC_RW = 4;

	static {
		System.loadLibrary("IcCard");
	}

	private int style = 1;

	public IcCard() {

	}

	private String[] getError(int style) {

		StringBuilder builder = new StringBuilder();
		builder.append(sError);
		builder.append(split);
		switch (style) {
		case -1:
		case -203:
			builder.append(RetUtil.Param_Err);
			builder.append(":");
			builder.append(RetUtil.Param_Err_Msg);
			break;
		case -3:
			builder.append(RetUtil.Timeout_Err);
			builder.append(":");
			builder.append(RetUtil.Timeout_Err_Msg);
			break;
		case -101:
			builder.append(RetUtil.ShangDian_ERROR);
			builder.append(":");
			builder.append(RetUtil.ShangDian_ERROR_Msg);
			break;
		case -204:
			builder.append(RetUtil.ARQC_ERROR);
			builder.append(":");
			builder.append(RetUtil.ARQC_ERROR_Msg);
			break;
		default:
			builder.append(RetUtil.Unknown_Err);
			builder.append(":");
			builder.append(RetUtil.Param_Err_Msg);
			break;
		}
		return builder.toString().split(split);
	}

	/**
	 * 读取IC中保存的客户信息
	 * 
	 * @param iIcFlag    : 1：接触式IC卡  2：非接触式IC 3：自动
	 * @param aryTagList : 标签编码数组 “x41,x42”
	 * @param strAIDList
	 * @param strTimeout
	 */
	public String[] getICCardInfo(int iIcFlag, String aryTagList,
			String strAIDList, String strTimeout) {
		int timeOut = Integer.parseInt(strTimeout);
		if (timeOut == -1) {
			return getParamErr();
		}

		start();
		byte[] data = new byte[2048];
		int ret = CT_GetIccInfo(iIcFlag, strAIDList, aryTagList, data, timeOut);

		String[] dataList = null;
		if (ret == 0) {
			String str = new String(data, 0, data.length).trim();
			StringBuilder builder = new StringBuilder();
			builder.append(sRight);
			builder.append(split);
			builder.append("" + iIcFlag);
			builder.append(split);
			builder.append(str);
			dataList = builder.toString().split(split);
		} else {
			dataList = getError(ret);
		}
		quit();
		return dataList;
	}

	public String[] cycleGetICCardInfo(int iIcFlag, String aryTagList,
			String strAIDList, String strTimeout) {
		int timeOut = Integer.parseInt(strTimeout);
		if (timeOut == -1) {
			return getParamErr();
		}

		byte[] data = new byte[2048];
		int ret = CT_GetIccInfo(iIcFlag, strAIDList, aryTagList, data, timeOut);

		String[] dataList = null;
		if (ret == 0) {
			String str = new String(data, 0, data.length).trim();
			StringBuilder builder = new StringBuilder();
			builder.append(sRight);
			builder.append(split);
			builder.append("" + iIcFlag);
			builder.append(split);
			builder.append(str);
			dataList = builder.toString().split(split);
		} else {
			dataList = getError(ret);
		}
		return dataList;
	}

	@Override
	protected int readData(byte[] data, int len, int timeOut) {
		int l = read_buffer(timeOut, len, data);
		if (l > 0) {
			byte[] tempData = new byte[l];
			System.arraycopy(data, 0, tempData, 0, l);
		}

		return l;
	}

	@Override
	protected int writeData(byte[] data, int len) {
		int realLength = getRealReadedLength(data, len);
		byte[] tempData = new byte[realLength];
		System.arraycopy(data, 0, tempData, 0, realLength);
		return write_buffer(data, realLength);
	}

	@Override
	public byte[] getFinancialOpenCommad() {

		return null;
	}

	@Override
	public byte[] getFinancialCloseCommad() {

		if (style == 1) {
			return BusinessInstruct.CloseBusiness((byte) 0x31);
		} else if (style == 2) {
			return BusinessInstruct.CloseBusiness((byte) 0x32);
		}
		return BusinessInstruct.CloseBusiness((byte) 0x31);
	}

	/*!
	 *\brief  写buffer
	 *\data  数据
	 *\len    数据长度
	 *\return 0成功，小于0失败
	 */
	@Override
	protected void sendEnd() {
		super.sendEnd();

		int time = 5;
		byte[] data = new byte[packLength];
		while (true) {

			time--;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (time == 0) {
				break;
			}

			int len = readData(data, packLength, 1);
			if (len == 3 && data[0] == 0x02 && data[1] == (byte) 0x0f
					&& data[2] == 0x03) {
				break;
			} else {
				//sendCommData(data, len);
			}
		}
	}

	private native int write_buffer(byte[] data, int len);

	/*!
	 *\brief  读data
	 *\timeout  时延
	 *\len      接收数据长度
	 *\data     接收buff
	 *\return 大于0成功，小于0失败
	 */
	private native int read_buffer(int timeOut, int len, byte[] data);

	private native int CT_GetIccInfo(int iIcFlag, String jstrAidList,
			String jstrTaglist, byte[] jstrUserInfo, int ntimeout);

	private native int CT_GenerateARQC(int iIcFlag, String pszAidList,
			String arqcData, byte[] pszARQC, int ntimeout);

	private native int CT_ExeICScript(int iIcFlag, String jstrTxData,
			String jstrARPC, int iStatus, byte[] jstrSptResult, byte[] jstrTc,
			int ntimeout);

	private native int CT_GetTxDetail(int iIcFlag, String jstrAidList,
			byte[] jstrTxDetail, int ntimeout);
}
