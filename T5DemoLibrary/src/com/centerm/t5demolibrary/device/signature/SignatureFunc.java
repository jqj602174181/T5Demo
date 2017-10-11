package com.centerm.t5demolibrary.device.signature;

import android.os.Environment;

import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.BmpUtil;
import com.centerm.t5demolibrary.utils.DesUtil;
import com.centerm.t5demolibrary.utils.SafetyUnitClass;
import com.centerm.t5demolibrary.utils.StringUtil;

/**
 *  电子签名
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
	 * 启动电子签名
	 * @param timeOut 超时时间
	 * @return 操作结果
	 */
	public int startSignature(int timeOut){
		byte[] timeout = StringUtil.intToBytes(timeOut);
		TransControl.getInstance().setTimeOut(timeOut);
		int nRet = mSignatureCmd.setSignTimeout(timeout);
		nRet = mSignatureCmd.signOn();

		return nRet;
	}

	/**
	 * 获取电子签名图片数据
	 * @return 图片路径
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
	 * 获取电子签名轨迹数据
	 * @return 轨迹数据路径
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
	 * 关闭电子签名
	 */
	public int signOff(){
		return mSignatureCmd.signOff();
	}
}