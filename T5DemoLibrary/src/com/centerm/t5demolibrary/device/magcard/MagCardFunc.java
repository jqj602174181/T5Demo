package com.centerm.t5demolibrary.device.magcard;

import com.centerm.t5demolibrary.device.t5.T5OtherCmd;

/**
 *  ´ÅÌõ¿¨
 * @author JQJ
 *
 */
public class MagCardFunc{
	
	private MagCardCmd magCardCmd = null;
	
	protected MagCardCmd getMagCardCmd() {
		return magCardCmd;
	}

	private static MagCardFunc instance = null;

	private MagCardFunc()
	{
		this.magCardCmd = new MagCardCmd();
	}

	public static MagCardFunc getInstance(){
		if(instance == null){
			synchronized (MagCardFunc.class) {
				instance = new MagCardFunc();
			}
		}
		return instance;
	}

	public void cleanSwInfo()
	{
		magCardCmd.cleanSwInfo();
	}

	public void setWitchAndCt(int which, boolean isCT)
	{
		magCardCmd.setWhich(which);
		magCardCmd.setCT(isCT);
	}

	public int readTrack(MagCardInfo magCardInfo){
		T5OtherCmd.getInstance().start_mv((byte) 3);
		int ret = magCardCmd.readTrack(magCardInfo);
		T5OtherCmd.getInstance().stop_mv((byte) 3);
		return ret;
	}

	public String getM_bySwInfo()
	{
		return magCardCmd.getM_bySwInfo();
	}

	public void setCT(boolean isCT)
	{
		magCardCmd.setCT(isCT);
	}

	public int reset()
	{
		return magCardCmd.reset();
	}

	public int readStatus(MagCardInfo info)
	{
		return magCardCmd.readStatus(info);
	}

	public int setSepTilde()
	{
		return magCardCmd.setSepTilde();
	}

	public int setSepOr()
	{
		return magCardCmd.setSepOr();
	}

	public int getSpeed()
	{
		return magCardCmd.getSpeed();
	}

	public int setTimeOut(byte timeout)
	{
		return magCardCmd.setTimeOut(timeout);
	}

}
