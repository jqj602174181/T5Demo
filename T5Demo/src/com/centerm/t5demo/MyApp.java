package com.centerm.t5demo;

import com.centerm.t5demo.util.PinYin;

import android.app.Application;

public class MyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		//ƴ�� ��ʼ��
		new Thread(){
			public void run() {
				PinYin.getPinYin( "��" );
			};
		}.start();
	}

}
