package com.centerm.t5demolibrary.utils;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * @class: SafetyUnit
 * @function: 加密算法模块
 * @author: zxx
 * @data: 2013-02-19
 */
public final class SafetyUnitClass
{
	final static String DES = "DES/ECB/NoPadding";
	final static String TriDes = "DESede/ECB/NoPadding";

	final static String AES = "AES/ECB/NoPadding";
	final static String AESKEY = "AES";

	/**
	 * @name: desEncrypt
	 * @function: DES加密
	 * 
	 * @param byte[]
	 *            key ---密钥
	 * @param byte[]
	 *            data ---加密的数据
	 * @return byte[] 成功返回加密后的数据，失败返回null
	 */
	public static byte[] desEncrypt(byte[] key, byte[] data)
	{
		if (key == null || data == null)
		{
			Log.e("SafetyUnitClass", "desEncrypt param is null........");
			return null;
		}
		int len = ((data.length + 7) / 8) * 8;
		if (len <= 0)
		{
			Log.e("SafetyUnitClass", "desEncrypt data.length=0........");
			return null;
		}
		byte[] needData = new byte[len];
		Arrays.fill(needData, (byte) 0x00);
		System.arraycopy(data, 0, needData, 0, data.length);

		try
		{
			KeySpec ks = new DESKeySpec(key);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			SecretKey ky = kf.generateSecret(ks);

			Cipher c = Cipher.getInstance(DES);
			c.init(Cipher.ENCRYPT_MODE, ky);
			return c.doFinal(needData);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @name: desDecrypt
	 * @function: DES解密
	 * 
	 * @param byte[]
	 *            key ---密钥
	 * @param byte[]
	 *            data ---解密的数据
	 * @return byte[] 成功返回解密后的数据，失败返回null
	 */
	public static byte[] desDecrypt(byte[] key, byte[] data)
	{
		if (key == null || data == null)
		{
			return null;
		}

		try
		{
			KeySpec ks = new DESKeySpec(key);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			SecretKey ky = kf.generateSecret(ks);

			Cipher c = Cipher.getInstance(DES);
			c.init(Cipher.DECRYPT_MODE, ky);
			return c.doFinal(data);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @name: triDesEncrypt
	 * @function: 3DES加密
	 * 
	 * @param byte[]
	 *            key ---密钥
	 * @param byte[]
	 *            data ---加密的数据
	 * @return 成功返回加密后的数据，失败返回null
	 */
	public static byte[] triDesEncrypt(byte[] key, byte[] data)
	{
		if (key == null || data == null)
		{
			return null;
		}

		int len = (data.length + 7) / 8 * 8;
		if (len <= 0)
		{
			return null;
		}
		byte[] needData = new byte[len];
		System.arraycopy(data, 0, needData, 0, data.length);

		byte[] k = new byte[24];

		// 调整密钥长度24
		if (key.length == 16)
		{
			System.arraycopy(key, 0, k, 0, key.length);
			System.arraycopy(key, 0, k, 16, 8);
		}
		else if (key.length == 24)
		{
			System.arraycopy(key, 0, k, 0, 24);
		}
		else
		{
			return null;
		}

		try
		{
			KeySpec ks = new DESedeKeySpec(k);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DESede");
			SecretKey ky = kf.generateSecret(ks);

			Cipher c = Cipher.getInstance(TriDes);
			c.init(Cipher.ENCRYPT_MODE, ky);
			return c.doFinal(needData);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * @name: triDesDecrypt
	 * @function: 3DES解密
	 * 
	 * @param byte[]
	 *            key ---密钥
	 * @param byte[]
	 *            data ---解密的数据
	 * @return byte[] 成功返回解密后的数据，失败返回null
	 */
	public static byte[] triDesDecrypt(byte[] key, byte[] data)
	{
		if (key == null || data == null)
		{
			return null;
		}

		int len = data.length;
		if (data.length % 8 != 0)
		{
			len = (data.length + 7) / 8 * 8;
		}

		if (len <= 0)
		{
			return null;
		}

		byte[] needData = new byte[len];
		System.arraycopy(data, 0, needData, 0, data.length);

		byte[] k = new byte[24];
		if (key.length == 16)
		{
			System.arraycopy(key, 0, k, 0, key.length);
			System.arraycopy(key, 0, k, 16, 8);
		}
		else if (key.length == 24)
		{
			System.arraycopy(key, 0, k, 0, 24);
		}
		else
		{
			return null;
		}

		try
		{
			KeySpec ks = new DESedeKeySpec(k);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DESede");
			SecretKey ky = kf.generateSecret(ks);

			Cipher c = Cipher.getInstance(TriDes);
			c.init(Cipher.DECRYPT_MODE, ky);
			return c.doFinal(needData);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @name: desSelectEncrypt
	 * @function: DES与3DES选择性加密
	 * 
	 * @param byte[]
	 *            key ---密钥 byte[] data ---加密的数据
	 * @return 成功返回加密后的数据，失败返回null
	 */
	public static byte[] desSelectEncrypt(byte[] key, byte[] data)
	{
		if (key.length == 16 || key.length == 24)
		{
			return triDesEncrypt(key, data);
		}
		else if (key.length == 8)
		{
			return desEncrypt(key, data);
		}

		return null;
	}

	/**
	 * @name: desSelectDecrypt
	 * @function: DES与3DES选择性解密
	 * 
	 * @param byte[]
	 *            key ---密钥
	 * @param byte[]
	 *            data ---加密的数据
	 * @return 成功返回加密后的数据，失败返回null
	 */
	public static byte[] desSelectDecrypt(byte[] key, byte[] data)
	{
		if (key.length == 16 || key.length == 24)
		{
			return triDesDecrypt(key, data);
		}
		else if (key.length == 8)
		{
			return desDecrypt(key, data);
		}

		return null;
	}

	/**
	 * @name: getPin
	 * @function: 获取PIN
	 * 
	 * @param String
	 *            pin ---PIN码
	 * 
	 * @return byte[] 返回转换后的PIN码
	 */
	public static byte[] getPin(byte[] arrPin)
	{
		if (arrPin == null)
		{
			return null;
		}
		int iLen = arrPin.length;
		if (iLen < 0 || iLen > 14)
		{
			return null;
		}

		byte[] bTmp = new byte[16];
		byte[] encode = null;

		Arrays.fill(bTmp, (byte) 0x46);
		bTmp[0] = 0x30;
		if (iLen >= 10 && iLen <= 14)
		{
			bTmp[1] = (byte) ((iLen & 0xFF) - 10 + 0x41);
		}
		else
		{
			bTmp[1] = (byte) ((iLen & 0xFF) + 0x30);
		}
		for (int i = 0; i < arrPin.length; ++i)
		{
			bTmp[i + 2] = arrPin[i];
		}
		encode = ChangeDataClass.hexStringToBytes(bTmp);
		return encode;
	}

	/**
	 * @name: getAccNo
	 * @function: 获账号
	 * 
	 * @param String
	 *            accno ---账号
	 * 
	 * @return byte[] 返回转换后的账号
	 */
	public static byte[] getAccNo(byte[] accno)
	{
		if (accno == null)
		{
			return null;
		}

		byte encode[] = null;
		if (accno.length == 12)
		{
			byte arrAccno[] = new byte[16];
			Arrays.fill(arrAccno, (byte) 0x30);
			for (int i = 0; i < 12; i++)
			{
				arrAccno[i + 4] = accno[i];
			}
			encode = ChangeDataClass.hexStringToBytes(arrAccno);
		}
		else
		{
			int len = accno.length;
			byte temp[] = new byte[12];
			Arrays.fill(temp, (byte) 0x30);
			if (len < 12)
			{
				System.arraycopy(accno, 0, temp, 12 - len, len);
			}
			else if (len > 12)
			{
				System.arraycopy(accno, (len - 1) - 12, temp, 0, 12);
			}

			byte arrAccno[] = new byte[16];
			Arrays.fill(arrAccno, (byte) 0x30);
			for (int i = 0; i < 12; i++)
			{
				arrAccno[i + 4] = temp[i];
			}
			encode = ChangeDataClass.hexStringToBytes(arrAccno);
		}
		return encode;
	}

	/**
	 * @name: getPinBlock
	 * @function: 获PinBlock
	 * 
	 * @param String
	 *            pin ---PIN码
	 * @param String
	 *            accno ---账号
	 * @return byte[] 返回PinBlock
	 */
	public static byte[] getPinBlock(byte[] pin, byte[] accno)
	{
		byte arrPin[] = getPin(pin);
		byte arrAccno[] = null;
		if (accno.length == 8)
		{
			arrAccno = new byte[8];
			System.arraycopy(accno, 0, arrAccno, 0, accno.length);
		}
		else
		{
			arrAccno = getAccNo(accno);
		}
		if (arrPin == null || arrAccno == null)
		{
			return null;
		}

		byte arrRet[] = new byte[8];
		// PIN BLOCK 格式等于 PIN 按位异或 主帐号;
		for (int i = 0; i < 8; i++)
		{
			arrRet[i] = (byte) (arrPin[i] ^ arrAccno[i]);
		}
		return arrRet;
	}

	/**
	 * @name: encryptAnsi9_8
	 * @function: Ansi9.8标准，并用DES选择性进行DES或3DES进行加密
	 * 
	 * @param String
	 *            pin ---PIN码
	 * @param String
	 *            accno ---账号
	 * @return byte[] 返回PinBlock
	 */
	public static byte[] encryptAnsi9_8(byte[] key, byte[] pin, byte[] accno)
	{
		return desSelectEncrypt(key, getPinBlock(pin, accno));
	}

	/**
	 * AES加密
	 * 
	 * @param data
	 *            需要加密的内容
	 * @param key
	 *            加密密码
	 * @return
	 */
	public static byte[] encryptAES(byte[] key, byte[] data)
	{

		if (key == null || data == null)
		{
			return null;
		}
		if (key.length != 16 && key.length != 24 && key.length != 32)
		{
			return null;
		}

		try
		{
			int nLen = (data.length + 15) / 16 * 16;
			byte[] bNeedData = new byte[nLen];
			System.arraycopy(data, 0, bNeedData, 0, data.length);
			SecretKeySpec genkey = new SecretKeySpec(key, AESKEY);
			Cipher cipher = Cipher.getInstance(AES);// 创建密码器
			cipher.init(Cipher.ENCRYPT_MODE, genkey);// 初始化
			byte[] result = cipher.doFinal(bNeedData);
			return result; // 加密
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		}
		catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AES解密
	 * 
	 * @param data
	 *            待解密内容
	 * @param key
	 *            解密密钥
	 * @return
	 */
	public static byte[] decryptAES(byte[] key, byte[] data)
	{
		if (data == null || key == null)
		{
			return null;
		}
		if (key.length != 16 && key.length != 24 && key.length != 32)
		{
			return null;
		}

		try
		{
			SecretKeySpec genkey = new SecretKeySpec(key, AESKEY);
			Cipher cipher = Cipher.getInstance(AES);// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, genkey);// 初始化
			byte[] result = cipher.doFinal(data);
			return result; // 加密
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		}
		catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] SHA1(byte[] data)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(data);
			byte messageDigest[] = digest.digest();

			return messageDigest;
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	//
	// //生成密钥对
	// public static KeyPair getKeyPair(int keybitlen) throws Exception
	// {
	// KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
	// keyPairGen.initialize(keybitlen);
	// KeyPair keyPair = keyPairGen.generateKeyPair();
	// PublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
	// PrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
	//
	// byte[] encdata = encRSA(publicKey, "1111111111111111111".getBytes() );
	// byte[] divencdata = ChangeDataClass.bytesToHexString(encdata);
	// Log.i("test", "divencdata = " + new String(divencdata));
	//
	// byte[] data = decRSA( privateKey, encdata );
	// byte[] divdata = ChangeDataClass.bytesToHexString(data);
	// Log.i("test", "divdata = " + new String(divdata));
	//
	// return keyPair;
	// }

	// 获取组合后的公钥数据
	public static PublicKey getPublicKey(String modulus, String publicExponent) throws Exception
	{
		// 16进制数据转大数
		BigInteger m = new BigInteger(modulus, 16);
		BigInteger e = new BigInteger(publicExponent, 16);

		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		return publicKey;
	}

	// 获取组合后的私钥数据
	public static PrivateKey getPrivateKey(String modulus, String privateExponent) throws Exception
	{
		// 16进制数据转大数
		BigInteger m = new BigInteger(modulus, 16);
		BigInteger e = new BigInteger(privateExponent, 16);

		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		return privateKey;
	}

	// 公钥加密
	public static byte[] encRSA(PublicKey publicKey, byte[] text)
	{
		// 加解密类
		Cipher cipher;
		try
		{
			// "RSA/ECB/PKCS1Padding" 就是：“算法/工作模式/填充模式”
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

			// 加密
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(text);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		}
		catch (BadPaddingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	// 私钥解密
	public static byte[] decRSA(PrivateKey privateKey, byte[] enctext)
	{
		// 加解密类
		Cipher cipher;
		try
		{
			// "RSA/ECB/PKCS1Padding" 就是：“算法/工作模式/填充模式”
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

			// 加密
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(enctext);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		}
		catch (BadPaddingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] pinBlockSM4(byte[] pin, byte[] accno)
	{
		if (pin == null || accno == null)
		{
			return null;
		}

		byte temp[] = new byte[32];
		Arrays.fill(temp, (byte) 0x30);
		if (accno.length != 12)
		{
			int accnolen = accno.length;
			if (accnolen < 12)
			{
				System.arraycopy(accno, 0, temp, 32 - (accnolen - 1), accno.length - 1);
			}
			else if (accno.length > 12)
			{
				System.arraycopy(accno, (accno.length - 1) - 12, temp, 20, 12);
			}
		}
		else
		{
			System.arraycopy(accno, 0, temp, 20, 12);
		}

		byte[] bTmp = new byte[32];
		int iLen = pin.length;

		Arrays.fill(bTmp, (byte) 0x46);
		bTmp[0] = 0x30;
		if (iLen >= 10 && iLen <= 14)
		{
			bTmp[1] = (byte) ((iLen & 0xFF) - 10 + 0x41);
		}
		else
		{
			bTmp[1] = (byte) ((iLen & 0xFF) + 0x30);
		}

		for (int i = 0; i < pin.length; ++i)
		{
			bTmp[i + 2] = pin[i];
		}

		byte[] combaccno = ChangeDataClass.hexStringToBytes(temp);
		byte[] combpin = ChangeDataClass.hexStringToBytes(bTmp);

		byte[] pinblock = new byte[16];
		for (int j = 0; j < 16; j++)
		{
			pinblock[j] = (byte) (combaccno[j] ^ combpin[j]);
		}

		return pinblock;
	}

}
