/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.centerm.t5demolibrary.bluetooth;

import com.centerm.t5demolibrary.utils.ErrorUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * BluetoothEnabler is a helper to manage the Bluetooth on/off checkbox
 * preference. It turns on/off Bluetooth and ensures the summary of the
 * preference reflects the current state.
 */
public final class BluetoothEnabler {

	private static final String TAG = "BluetoothEnabler";
	private final Context mContext;
	private final LocalBluetoothAdapter mLocalAdapter;
	private final IntentFilter mIntentFilter;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Broadcast receiver is always running on the UI thread here,
			// so we don't need consider thread synchronization.
			//			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			//			handleStateChanged(state);
		}
	};

	public BluetoothEnabler(Context context)
	{
		mContext = context;
		mLocalAdapter = LocalBluetoothAdapter.getInstance();
		mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	}

	public void resume()
	{

		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG, "resume+++++");

		// Bluetooth state is not sticky, so set it manually
		//		handleStateChanged(mLocalAdapter.getBluetoothState());

		mContext.registerReceiver(mReceiver, mIntentFilter);
	}

	public void pause()
	{
		if (mLocalAdapter == null)
		{
			return;
		}

		mContext.unregisterReceiver(mReceiver);
	}

	//	void handleStateChanged(int state)
	//	{
	//		if (ErrorUtil.LOG_DEBUG)
	//			Log.i(TAG, "handleStateChanged++state:" + state);
	//
	//		switch (state)
	//		{
	//		case BluetoothAdapter.STATE_TURNING_ON:
	//			break;
	//		case BluetoothAdapter.STATE_ON:
	//			break;
	//		case BluetoothAdapter.STATE_TURNING_OFF:
	//			break;
	//		case BluetoothAdapter.STATE_OFF:
	//			break;
	//		default:
	//		}
	//	}

	public LocalBluetoothAdapter getBluetoothAdapter()
	{
		return mLocalAdapter;
	}

	public void setBluetoothStatus(boolean isEnable){
		if (mLocalAdapter != null)
		{
			mLocalAdapter.setBluetoothEnabled(isEnable);
		}
	}

	public int pairing(String mac)
	{
		if (!mLocalAdapter.isEnabled())
		{
			Log.e(TAG, "请先打开蓝牙!");
			return -1;
		}

		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG, "Pairing++get text mac:" + mac);

		if (!BluetoothAdapter.checkBluetoothAddress(mac) || mLocalAdapter == null)
		{
			Log.e(TAG, "蓝牙MAC错误");
			return -2;
		}

		BluetoothDevice mDevice = mLocalAdapter.getRemoteDevice(mac);
		Log.e(TAG, "mDevice.getBondState():" + mDevice.getBondState());
		if (mDevice.getBondState() == BluetoothDevice.BOND_NONE)
		{
			if (ErrorUtil.LOG_DEBUG)
				Log.i(TAG, "Pairing++无配对，需要配对...");

			// 取消扫描，否则匹配会不稳定
			mLocalAdapter.stopScanning();
			if (!CachedBluetoothDevice.createBond(mDevice))
			{
				Log.e(TAG, "createBond失败!");
				return -3;
			}
		}else if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
			if (ErrorUtil.LOG_DEBUG)
				Log.i(TAG, "Pairing++正在配对...");
			return 1;
		}else if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
			return 2;
		}
		return 0;
	}
}
