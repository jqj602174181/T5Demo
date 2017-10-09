package com.centerm.t5demolibrary.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class BmpUtil
{
	public static final String TAG = "BmpUtil";

	/**
	 * ��Bitmap��Ϊ .bmp��ʽͼƬ
	 * 
	 * @param bitmap
	 */
	public static void saveBmp(Bitmap bitmap)
	{

		if (bitmap == null)
			return;

		// λͼ��С
		int nBmpWidth = bitmap.getWidth();
		int nBmpHeight = bitmap.getHeight();

		// ͼ�����ݴ�С
		int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);

		try
		{
			// �洢�ļ���
			String filename = "/mnt/usb_storage/test.bmp";
			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
			}
			FileOutputStream fileos = new FileOutputStream(filename);

			// bmp�ļ�ͷ
			int bfType = 0x4d42;
			long bfSize = 14 + 40 + bufferSize;
			int bfReserved1 = 0;
			int bfReserved2 = 0;
			long bfOffBits = 14 + 40;

			// ����bmp�ļ�ͷ
			writeWord(fileos, bfType);
			writeDword(fileos, bfSize);
			writeWord(fileos, bfReserved1);
			writeWord(fileos, bfReserved2);
			writeDword(fileos, bfOffBits);

			// bmp��Ϣͷ
			long biSize = 40L;
			long biWidth = nBmpWidth;
			long biHeight = nBmpHeight;
			int biPlanes = 1;
			int biBitCount = 24;
			long biCompression = 0L;
			long biSizeImage = 0L;
			long biXpelsPerMeter = 0L;
			long biYPelsPerMeter = 0L;
			long biClrUsed = 0L;
			long biClrImportant = 0L;

			// ����bmp��Ϣͷ
			writeDword(fileos, biSize);
			writeLong(fileos, biWidth);
			writeLong(fileos, biHeight);
			writeWord(fileos, biPlanes);
			writeWord(fileos, biBitCount);
			writeDword(fileos, biCompression);
			writeDword(fileos, biSizeImage);
			writeLong(fileos, biXpelsPerMeter);
			writeLong(fileos, biYPelsPerMeter);
			writeDword(fileos, biClrUsed);
			writeDword(fileos, biClrImportant);

			// ����ɨ��
			byte bmpData[] = new byte[bufferSize];
			int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
			for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
				for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3)
				{
					int clr = bitmap.getPixel(wRow, nCol);
					bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
					bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
					bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
				}

			fileos.write(bmpData);
			fileos.flush();
			fileos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected static void writeWord(FileOutputStream stream, int value) throws IOException
	{
		byte[] b = new byte[2];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) (value >> 8 & 0xff);
		stream.write(b);
	}

	protected static void writeDword(FileOutputStream stream, long value) throws IOException
	{
		byte[] b = new byte[4];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) (value >> 8 & 0xff);
		b[2] = (byte) (value >> 16 & 0xff);
		b[3] = (byte) (value >> 24 & 0xff);
		stream.write(b);
	}

	protected static void writeLong(FileOutputStream stream, long value) throws IOException
	{
		byte[] b = new byte[4];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) (value >> 8 & 0xff);
		b[2] = (byte) (value >> 16 & 0xff);
		b[3] = (byte) (value >> 24 & 0xff);
		stream.write(b);
	}

	public static void saveBytesToFile(byte[] bmpData, String filename)
	{
		try
		{
			File file = new File(filename);
			if (!file.exists())
			{
				Log.d(TAG, "�ļ������ڣ�������ͼƬ");
				file.createNewFile();
			}
			else
			{
				Log.d(TAG, "ɾ����ͼƬ��������ͼƬ");
				file.delete();
				file.createNewFile();
			}
			FileOutputStream fileos = new FileOutputStream(filename);
			fileos.write(bmpData);
			fileos.flush();
			fileos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
