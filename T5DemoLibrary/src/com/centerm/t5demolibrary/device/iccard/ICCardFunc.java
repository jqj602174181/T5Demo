package com.centerm.t5demolibrary.device.iccard;

import com.centerm.t5demolibrary.transfer.TransControl;

/**
 *  IC¿¨
 * @author JQJ
 *
 */
public class ICCardFunc{

	private static final String TAG = "ICCardFunc";

	private ICCardCmd mICCardCmd = null;
	private static ICCardFunc instance = null;

	private ICCardFunc()
	{
		this.mICCardCmd = new ICCardCmd();
		TransControl.getInstance();
	}

	public static ICCardFunc getInstance(){
		if(instance == null){
			synchronized (ICCardFunc.class) {
				instance = new ICCardFunc();
			}
		}
		return instance;
	}

	public int getICCardInfo(ICCardData data){
		return mICCardCmd.getICCardNum(data);
	}
	
	public String getICData(){
		return mICCardCmd.getICData();
	}
}
