package com.centerm.util.financial;

public class IDPhotoService
{
	static IDPhotoService instance;

	private IDPhotoService()
	{
	}

	/*
	 * ! \brief 获取二代证service实例 \return service实例
	 */
	public static IDPhotoService getInstance()
	{
		if (instance == null)
		{
			instance = new IDPhotoService();
		}
		return instance;
	}

	static
	{
		// 加载动态库
		System.loadLibrary("idcaread");
		System.loadLibrary("idphoto");
	}

	/********************* 动态库中方法声明 *************************/
	/*
	 * ! \brief 解析二代证图片 \param [in] byIn - 加密图片数据 \param [out] byOut - 解密图片数据
	 * \return 照片数据长度
	 */
	public native int ParsePhoto(byte[] byIn, byte[] byOut);
}
