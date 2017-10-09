package com.centerm.t5demo;

import com.centerm.t5demo.util.CrashUtil;
import com.centerm.t5demo.util.PreferenceUtils;
import com.centerm.t5demolibrary.interfaces.OnMesBackListener;
import com.centerm.t5demolibrary.transfer.TransControl;
import com.centerm.t5demolibrary.utils.ErrorUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements View.OnClickListener, 
RadioGroup.OnCheckedChangeListener, OnMesBackListener{

	public static final String TAG = "LoginActivity";

	private TextView connectTxt;
	private Button loginBtn;
	private RadioGroup rgConn;
	private ImageView connFlag;
	private RadioButton rbUsbHid, rbBlueTooth;
	private ImageButton IB_Settings;

	private EditText accountEdit;
	private EditText passwordEdit;

	private static final String ACCOUNT = "admin";
	private static final String PASSWORD = "123";

	private TextView tvState;
	private boolean isConnect = false;//用于判断是否连接上
	private String currentState = "未 连 接";
	private String remoteMac;

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mContext = this;

		TransControl.getInstance().init(mContext, this);
		TransControl.getInstance().setShortConnect(false);

		initView();
		setDevType();

		//异常信息捕获
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() 
		{
			public void uncaughtException(Thread thread, Throwable ex) 
			{
				//任意一个线程异常后统一的处理
				ex.printStackTrace();
				CrashUtil.saveCrashInfo2File(ex);
				Log.e( TAG, "T5Demo error..................................");
			}
		});
	}

	private void initView() {
		connectTxt = (TextView)findViewById(R.id.bt_conn_open);
		loginBtn = (Button)findViewById(R.id.login_Btn);
		rgConn = (RadioGroup)findViewById(R.id.rg_conn);
		rbUsbHid = (RadioButton)findViewById(R.id.hid_conn);
		rbBlueTooth = (RadioButton)findViewById(R.id.bt_conn);
		accountEdit = (EditText)findViewById(R.id.account);
		passwordEdit = (EditText)findViewById(R.id.password);
		tvState = (TextView)findViewById(R.id.tvBTState);
		//		tvState.setText(currentState);
		connFlag = (ImageView)findViewById(R.id.connection_flag);
		IB_Settings = (ImageButton)findViewById(R.id.IB_settings);

		connectTxt.setOnClickListener(this);
		loginBtn.setOnClickListener(this);
		IB_Settings.setOnClickListener(this);
		rgConn.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.bt_conn_open:
			if(rbBlueTooth.isChecked()){ //蓝牙连接
				SharedPreferences sp = mContext.getSharedPreferences("Settings", Activity.MODE_PRIVATE);
				remoteMac = sp.getString("remoteMac", "E0:76:D0:DA:0F:87");
				TransControl.getInstance().connectByBluetooth(remoteMac);
			}else if(rbUsbHid.isChecked()){
				TransControl.getInstance().connectByHid();
			}
			break;
		case R.id.IB_settings:
			SettingsDialog dialog = new SettingsDialog(mContext, R.style.MyDialog);
			dialog.show();
			break;
		case R.id.login_Btn:
			if(!isConnect){
				Toast.makeText(LoginActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
				return;
			}
			//			String account = accountEdit.getEditableText().toString();
			//			String password = passwordEdit.getEditableText().toString();
			//			if(TextUtils.isEmpty(account)){
			//				Toast.makeText(LoginActivity.this, "帐号不能为空！", Toast.LENGTH_SHORT).show();
			//				return;
			//			}
			//			if(TextUtils.isEmpty(password)){
			//				Toast.makeText(LoginActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
			//				return;
			//			}
			//			if(!account.equals(ACCOUNT)){
			//				Toast.makeText(LoginActivity.this, "帐号不对！", Toast.LENGTH_SHORT).show();
			//				return;
			//			}
			//			if(!password.equals(PASSWORD)){
			//				Toast.makeText(LoginActivity.this, "密码不对！", Toast.LENGTH_SHORT).show();
			//				return;
			//			}

			Intent mIntent = new Intent();
			mIntent.setClass(LoginActivity.this, BaseInfoActivity.class);
			LoginActivity.this.startActivity(mIntent);
			Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
			finish();
			break;
		}
	}

	@Override
	public void onDestroy()
	{
		if (ErrorUtil.LOG_DEBUG)
			Log.i(TAG, "onDestroy");
		TransControl.getInstance().onDestroy();
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		setDevType();
	}

	private void setDevType()
	{
		int devType = TransControl.DEV_USBHID;
		if (rbUsbHid.isChecked())
		{
			devType = TransControl.DEV_USBHID;
		} else if (rbBlueTooth.isChecked()) {
			devType = TransControl.DEV_BLUETOOTH;
		}
		TransControl.getInstance().setDevType(devType);
	}

	@Override
	public void setBluetoothStatus(final String strBluetoothState) {
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				tvState.setText(strBluetoothState);
				if(strBluetoothState.equals("配对成功")){
					isConnect = true;
					connectTxt.setEnabled(false);
					PreferenceUtils.getInstance(mContext).setMac(remoteMac);
				}
			}
		});
	}

	@Override
	public void setUsbStatus(final String strUsbState) {
		runOnUiThread(new Runnable()
		{
			@Override
			public void run() {
				if(strUsbState != null){
					tvState.setText(strUsbState);
					if(strUsbState.equals("USB授权成功")){
						isConnect = true;
					}
				}
			}
		});
	}
}