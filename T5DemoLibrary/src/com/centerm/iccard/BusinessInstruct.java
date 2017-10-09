package com.centerm.iccard;

public class BusinessInstruct {
	/*
	 * �����װ5�ֽ���ģ��ҵ��Ĵ򿪺͹ر�ָ�����дǩ���Ĵ򿪹ر�ָ��
	 * ����ģ���ָ�Code=<1B>[8OPENCHANNEL<02><P1><03>
	 * ����ģ��ر�ָ�Code=<1B>[8CLOSECHANNEL<02><P1><03>
	 * <P1>Ϊ������ֵΪ0x30-0x34������0x30-0x34�ֱ�Ϊ������֤��IC������Ƶ�����ſ���ָ����
	 * 
	 * ��дǩ����ָ�Code=<1B>[0STARTHW ��дǩ���ر�ָ�Code=<1B>[0CLOSEHW
	 */

	// ָ���пɼ��Ĳ��֣�ָ����<>�еĲ���Ϊ���ɼ��ַ�
	private static final String OpenChannel = "[8OPENCHANNEL";
	private static final String CloseChannel = "[8CLOSECHANNEL";

	/**
	 * ����ģ���ҵ�� Code=<1B>[8OPENCHANNEL<02><P1><03>��
	 * 
	 * @param ����P1��ֵΪ0x30
	 *            -0x34
	 * @return ���ط�װ�õ�ָ��
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
	 * ����ģ��ر�ҵ�� Code=<1B>[8CLOSECHANNEL<02><P1><03>
	 * 
	 * @param ����P1��ֵΪ0x30
	 *            -0x34
	 * @return ��װ�õ�ָ��
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
