/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.centerm.t5demo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils
{

	/**
	 * 保存Preference的name
	 */
	public static final String PREFERENCE_NAME = "devInfo";
	public static final String KEY_SERIAL_PORT = "serial_port";
	public static final String KEY_SERIAL_BAUD = "serial_baud";
	public static final String KEY_SERIAL_PID = "pid";
	public static final String KEY_SERIAL_VID = "vid";
	public static final String KEY_HID_BYTES = "hid_bytes";
	public static final String KEY_SELECT_COMM = "comm";
	private static SharedPreferences mSharedPreferences;
	private static PreferenceUtils mPreferenceUtils;
	private static SharedPreferences.Editor editor;

	private String KEY_MAC = "key_mac";

	private PreferenceUtils(Context cxt)
	{
		mSharedPreferences = cxt.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}

	/**
	 * 单例模式，获取instance实例
	 * 
	 * @param cxt
	 * @return
	 */
	public static PreferenceUtils getInstance(Context cxt)
	{
		if (mPreferenceUtils == null)
		{
			mPreferenceUtils = new PreferenceUtils(cxt);
		}

		editor = mSharedPreferences.edit();
		return mPreferenceUtils;
	}

	public void setMac(String mac)
	{
		saveParameter(KEY_MAC, mac);
	}

	public String getMac()
	{
		return getDefaultParameter(KEY_MAC, "94:A1:A2:DD:05:71");
	}

	public String getDefaultParameter(String key, String defaultValue)
	{
		return mSharedPreferences.getString(key, defaultValue);
	}

	public void saveParameter(String key, String value)
	{
		editor.putString(key, value);
		editor.commit();
	}

	public CharSequence getParameter(String key)
	{
		return getDefaultParameter(key, "");
	}
	
}