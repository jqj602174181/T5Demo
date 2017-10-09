package com.centerm.iccard;

public class BusinessInstruct {
	/*
	 * 该类封装5种金融模块业务的打开和关闭指令和手写签名的打开关闭指令
	 * 金融模块打开指令：Code=<1B>[8OPENCHANNEL<02><P1><03>
	 * 金融模块关闭指令：Code=<1B>[8CLOSECHANNEL<02><P1><03>
	 * <P1>为参数，值为0x30-0x34，其中0x30-0x34分别为：二代证，IC卡，射频卡，磁卡，指纹仪
	 * 
	 * 手写签名打开指令：Code=<1B>[0STARTHW 手写签名关闭指令：Code=<1B>[0CLOSEHW
	 */

	// 指令中可见的部分，指令中<>中的部分为不可见字符
	private static final String OpenChannel = "[8OPENCHANNEL";
	private static final String CloseChannel = "[8CLOSECHANNEL";

	/**
	 * 金融模块打开业务 Code=<1B>[8OPENCHANNEL<02><P1><03>，
	 * 
	 * @param 参数P1的值为0x30
	 *            -0x34
	 * @return 返回封装好的指令
	 */
	public static byte[] OpenBusiness(byte p1) {
		byte[] instruction = new byte[17];
		instruction[0] = (byte) 0x1B;
		System.arraycopy(OpenChannel.getBytes(), 0, instruction, 1,
				OpenChannel.length());
		instruction[OpenChannel.length() + 1] = (byte) 0x02;
		instruction[OpenChannel.length() + 2] = (byte) p1;
		instruction[OpenChannel.length() + 3] = (byte) 0x03;
		return instruction;
	}

	/**
	 * 金融模块关闭业务 Code=<1B>[8CLOSECHANNEL<02><P1><03>
	 * 
	 * @param 参数P1的值为0x30
	 *            -0x34
	 * @return 封装好的指令
	 */
	public static byte[] CloseBusiness(byte p1) {
		byte[] instruction = new byte[18];
		instruction[0] = (byte) 0x1B;
		System.arraycopy(CloseChannel.getBytes(), 0, instruction, 1,
				CloseChannel.length());
		instruction[CloseChannel.length() + 1] = (byte) 0x02;
		instruction[CloseChannel.length() + 2] = (byte) p1;
		instruction[CloseChannel.length() + 3] = (byte) 0x03;
		return instruction;
	}
}
