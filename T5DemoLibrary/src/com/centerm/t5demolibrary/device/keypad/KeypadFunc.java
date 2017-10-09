package com.centerm.t5demolibrary.device.keypad;

import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.StringUtil;

/**
 *  √‹¬Îº¸≈Ã
 * @author JQJ
 *
 */
public class KeypadFunc{

	private KeypadCmd mKeypadCmd = null;
	private static KeypadFunc instance = null;

	private KeypadFunc()
	{
		this.mKeypadCmd = new KeypadCmd();
		TransControl.getInstance();
	}

	public static KeypadFunc getInstance(){
		if(instance == null){
			synchronized (KeypadFunc.class) {
				instance = new KeypadFunc();
			}
		}
		return instance;
	}

	public String getSwInfo(){
		return mKeypadCmd.getSwInfo();
	}

	public int startKeypad(int timeOut){
		byte[] timeout = StringUtil.intToBytes(timeOut);
		int nRet = mKeypadCmd.setTimeout(timeout);
		TransControl.getInstance().setTimeOut((timeOut+2) * 1000);
		nRet = mKeypadCmd.enterKey();

		return nRet;
	}
}
