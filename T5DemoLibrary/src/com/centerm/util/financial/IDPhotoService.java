package com.centerm.util.financial;

public class IDPhotoService
{
	static IDPhotoService instance;

	private IDPhotoService()
	{
	}

	/*
	 * ! \brief ��ȡ����֤serviceʵ�� \return serviceʵ��
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
		// ���ض�̬��
		System.loadLibrary("idcaread");
		System.loadLibrary("idphoto");
	}

	/********************* ��̬���з������� *************************/
	/*
	 * ! \brief ��������֤ͼƬ \param [in] byIn - ����ͼƬ���� \param [out] byOut - ����ͼƬ����
	 * \return ��Ƭ���ݳ���
	 */
	public native int ParsePhoto(byte[] byIn, byte[] byOut);
}
