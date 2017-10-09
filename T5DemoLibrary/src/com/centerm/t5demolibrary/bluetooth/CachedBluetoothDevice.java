package com.centerm.t5demolibrary.bluetooth;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;

public class CachedBluetoothDevice
{

	// ≈‰∂‘∞Û∂®
	public static boolean createBond(BluetoothDevice dev)
	{
		try
		{
			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
			Boolean ret = (Boolean) createBondMethod.invoke(dev);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/* …Ë÷√∆•≈‰√‹¬Î */
	public static boolean setPasskey(BluetoothDevice dev, int passkey)
	{
		try
		{
			Method setKeyMethod = BluetoothDevice.class.getMethod("setPasskey", int.class);
			Boolean ret = (Boolean) setKeyMethod.invoke(dev, passkey);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean setPin(BluetoothDevice dev, byte[] pin)
	{
		try
		{
			Method setPinMethod = BluetoothDevice.class.getMethod("setPin", byte[].class);
			Boolean ret = (Boolean) setPinMethod.invoke(dev, pin);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean cancelPairingUserInput(BluetoothDevice dev)
	{
		try
		{
			Method cancelInputMethod = BluetoothDevice.class.getMethod("cancelPairingUserInput");
			Boolean ret = (Boolean) cancelInputMethod.invoke(dev);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean setPairingConfirmation(BluetoothDevice dev, boolean confirm)
	{
		try
		{
			Method confirmMethod = BluetoothDevice.class.getMethod("setPairingConfirmation", boolean.class);
			Boolean ret = (Boolean) confirmMethod.invoke(dev, confirm);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean removeBond(BluetoothDevice dev)
	{
		try
		{
			Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
			Boolean ret = (Boolean) removeBondMethod.invoke(dev);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean cancelBondProcess(BluetoothDevice dev)
	{
		try
		{
			Method cancelBondMethod = BluetoothDevice.class.getMethod("cancelBondProcess");
			Boolean ret = (Boolean) cancelBondMethod.invoke(dev);
			return ret.booleanValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
}
