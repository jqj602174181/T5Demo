package com.centerm.t5demolibrary.device.idcard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.centerm.t5demolibrary.device.t5.T5Cmd;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;
import com.centerm.t5demolibrary.utils.StringUtil;
import com.centerm.util.financial.IDPhotoService;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 * 身份证
 * @author JQJ
 *
 */
public class IDCardFunc{
	
	private static final String TAG = "IDCardFunc";

	private IDCardCmd mIDCardCmd = null;
	private static IDCardFunc instance = null;
	PersonInfo mPersonInfo;

	private IDCardFunc()
	{
		TransControl.getInstance();
		mIDCardCmd = new IDCardCmd();
	}

	public static IDCardFunc getInstance(){
		if(instance == null){
			synchronized (IDCardFunc.class) {
				instance = new IDCardFunc();
			}
		}
		return instance;
	}

	public String getSwInfo(){
		return mIDCardCmd.getSwInfo();
	}

	/**
	 * 读取二代证信息
	 * 
	 * @param strPersonInfo
	 * @param img
	 * @return
	 */
	public int getIdCardInfo(String[] strPersonInfo)
	{
		if (strPersonInfo.length < 10)
		{
			return ErrorUtil.RET_OUTOF_BUFFER;
		}

		PersonInfo perInfo = new PersonInfo();
		int ret = mIDCardCmd.getPersonMsg(perInfo);
		if (ret != 0)
		{
			Log.e(TAG, "读二代证失败：" + ret);
			return ret;
		}

		if (FormatIDInfo(perInfo, strPersonInfo) == null)
		{
			return ErrorUtil.RET_OUTOF_BUFFER;
		}

		// 设置图片路径
		byte[] byImg = new byte[64 * 1024];
		IDPhotoService.getInstance().ParsePhoto(perInfo.photo, byImg);
		String strImagePath = saveBitmap(byImg);
		if (strImagePath != null)
		{
			strPersonInfo[9] = strImagePath;
		}

		// for (int i = 0; i < strPersonInfo.length; i++)
		// {
		// Log.i(TAG, "i=" + i + "++strPersonInfo[i]:" + strPersonInfo[i]);
		// }

		return ret;
	}

	/**
	 * 设置二代证数组
	 * 
	 * @param pInfo
	 * @param strInfo
	 * @return
	 */
	private String[] FormatIDInfo(PersonInfo pInfo, String[] strInfo)
	{
		if (strInfo.length < 9)
		{
			return null;
		}

		strInfo[0] = StringUtil.ByteToString(pInfo.name);
		strInfo[1] = StringUtil.ByteToString(pInfo.sex);
		strInfo[2] = StringUtil.ByteToString(pInfo.nation);
		strInfo[3] = StringUtil.ByteToString(pInfo.birthday);
		strInfo[4] = StringUtil.ByteToString(pInfo.address);
		strInfo[5] = StringUtil.ByteToString(pInfo.cardId);
		strInfo[6] = StringUtil.ByteToString(pInfo.police);

		String strDate = StringUtil.ByteToString(pInfo.validStart);
		strInfo[7] = StringUtil.FormatDate(strDate);

		strDate = StringUtil.ByteToString(pInfo.validEnd);
		strInfo[8] = StringUtil.FormatDate(strDate);

		if (null != pInfo.fingerdata)
		{
			String strRecv = StringUtil.HexToStringA(pInfo.fingerdata, 1024);
			strInfo[10] = strRecv;
		}

		return strInfo;
	}

	/** 保存方法 */
	public String saveBitmap(byte[] temp)
	{
		String strImagPath = null;

		Bitmap bm = BitmapFactory.decodeByteArray(temp, 0, temp.length);
		File dir = Environment.getExternalStorageDirectory();
		File f = new File(dir.getAbsolutePath(), "photo.bmp");

		try
		{
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			strImagPath = f.getAbsolutePath();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return strImagPath;
	}

	/**
	 * 打开读卡器，读二代证数据
	 * 
	 * @param nTimeOut
	 * @param byCardData
	 * @return
	 * @throws DataException
	 */
	public int getIDCardInfoOnce(String[] personInfo){
		int nRet = ErrorUtil.RET_SUCCESS;
		Log.d(TAG, "启动T5动画");
		T5Cmd.getInstance().start_mv((byte) 0);

		// 寻卡
		nRet = mIDCardCmd.searchCard();
		if (nRet >= 0)
		{
			try
			{
				Thread.sleep(200);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			// 找卡
			nRet = mIDCardCmd.selectCard();
			if (nRet >= 0)
			{
				try
				{
					Thread.sleep(200);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				// 读卡
				nRet = getIdCardInfo(personInfo);
			}
		}

		Log.d(TAG, "关闭卡机动画");
		T5Cmd.getInstance().stop_mv((byte) 0);

		return nRet;
	}

}
