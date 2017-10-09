package com.centerm.t5demolibrary.device.magcard;

import java.io.Serializable;

import com.centerm.t5demolibrary.utils.StringUtil;

public class MagCardInfo implements Serializable
{
	private static final long serialVersionUID = -6867892445849581318L;

	public MagCardInfo()
	{
		// 清空磁道数据
		byte[] data = new byte[1];
		data[0] = 0x00;

		this.track1 = data;
		this.track2 = data;
		this.track3 = data;
	}

	private byte[] track1;
	private byte[] track2;
	private byte[] track3;
	private byte[] status;
	private byte speed;

	public byte[] getTrack1()
	{
		return track1;
	}

	public void setTrack1(byte[] track1, int length)
	{
		this.track1 = new byte[length];
		System.arraycopy(track1, 0, this.track1, 0, length);
	}

	public byte[] getTrack2()
	{
		return track2;
	}

	public void setTrack2(byte[] track2, int length)
	{
		this.track2 = new byte[length];
		System.arraycopy(track2, 0, this.track2, 0, length);
	}

	public byte[] getTrack3()
	{
		return track3;
	}

	public void setTrack3(byte[] track3, int length)
	{
		this.track3 = new byte[length];
		System.arraycopy(track3, 0, this.track3, 0, length);
	}

	public String[] getMagInfo()
	{
		String[] result = new String[3];
		String spilt = MagCardFunc.getInstance().getMagCardCmd().getSpilt();
		result[0] = StringUtil.ByteToString(track1) + spilt;
		result[1] = StringUtil.ByteToString(track2) + spilt;
		result[2] = StringUtil.ByteToString(track3) + spilt;
		return result;
	}

	public byte[] getStatus()
	{
		return status;
	}

	public void setStatus(byte[] status)
	{
		this.status = status;
	}

	public byte getSpeed()
	{
		return speed;
	}

	public void setSpeed(byte speed)
	{
		this.speed = speed;
	}
}
