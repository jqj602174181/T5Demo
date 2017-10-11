package com.centerm.t5demolibrary.device.signature;

import android.os.Environment;

import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.BmpUtil;
import com.centerm.t5demolibrary.utils.DesUtil;
import com.centerm.t5demolibrary.utils.SafetyUnitClass;
import com.centerm.t5demolibrary.utils.StringUtil;

/**
 *  ����ǩ��
 * @author JQJ
 *
 */
public class SignatureFunc{

	private SignatureCmd mSignatureCmd = null;
	private static SignatureFunc instance = null;

	private SignatureFunc()
	{
		this.mSignatureCmd = new SignatureCmd();
		TransControl.getInstance();
	}

	public static SignatureFunc getInstance(){
		if(instance == null){
			synchronized (SignatureFunc.class) {
				instance = new SignatureFunc();
			}
		}
		return instance;
	}

	public String getSwInfo(){
		return mSignatureCmd.getSwInfo();
	}

	/**
	 * ��������ǩ��
	 * @param timeOut ��ʱʱ��
	 * @return �������
	 */
	public int startSignature(int timeOut){
		byte[] timeout = StringUtil.intToBytes(timeOut);
		TransControl.getInstance().setTimeOut(timeOut);
		int nRet = mSignatureCmd.setSignTimeout(timeout);
		nRet = mSignatureCmd.signOn();

		return nRet;
	}

	/**
	 * ��ȡ����ǩ��ͼƬ����
	 * @return ͼƬ·��
	 */
	public int getSignPic(String[] temp){
		String str = "/mnt/internal_sd/hw.png";
		byte[] path = StringUtil.StringToHexAscii(str);
		int nRet = mSignatureCmd.getSignData(path);
		if (nRet >= 0)
		{
			byte[] encryptData = mSignatureCmd.getEncryptData();
			byte[] sourceData = mSignatureCmd.getPicSourceData();
			String filename = Environment.getExternalStorageDirectory().getPath() + "/btnSignPic_Encrypt.png";
			BmpUtil.saveBytesToFile(sourceData, filename);

			byte[] decryptData = SafetyUnitClass.desDecrypt(DesUtil.SIGNDATA_KEY, encryptData);
			String descryptFilename = Environment.getExternalStorageDirectory().getPath()+ "/hw.png";
			BmpUtil.saveBytesToFile(decryptData, descryptFilename);

			temp[0] = descryptFilename;
		}

		return nRet;
	}
	
	/**
	 * ��ȡ����ǩ���켣����
	 * @return �켣����·��
	 */
	public int getSignXml(String[] temp){
		String str = "/mnt/internal_sd/hw.xml";
		byte[] path = StringUtil.StringToHexAscii(str);
		int nRet = mSignatureCmd.getSignData(path);
		if (nRet >= 0)
		{
			byte[] encryptData = mSignatureCmd.getEncryptData();
			byte[] sourceData = mSignatureCmd.getPicSourceData();
			String filename = Environment.getExternalStorageDirectory().getPath() + "/btnSignPic_Encrypt.png";
			BmpUtil.saveBytesToFile(sourceData, filename);

			byte[] decryptData = SafetyUnitClass.desDecrypt(DesUtil.SIGNDATA_KEY, encryptData);
			String descryptFilename = Environment.getExternalStorageDirectory().getPath()+ "/hw.xml";
			BmpUtil.saveBytesToFile(decryptData, descryptFilename);

			temp[0] = descryptFilename;
		}

		return nRet;
	}

	/**
	 * �رյ���ǩ��
	 */
	public int signOff(){
		return mSignatureCmd.signOff();
	}
}