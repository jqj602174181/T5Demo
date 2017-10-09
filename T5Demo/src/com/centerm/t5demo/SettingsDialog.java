package com.centerm.t5demo;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @author JQJ
 * Ҫ���������豸mac��ַ����
 */
public class SettingsDialog extends Dialog {

	Context context;
	private EditText ET_Mac;
	private Button IB_SettingsOK, IB_SettingsCancel;
	private String remoteMac;
	private SharedPreferences sp;

	public SettingsDialog(Context context) {
		super(context);
		this.context = context;
	}

	public SettingsDialog(Context context,int theme){
		super(context,theme);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.settings_show_dialog);
		this.setCanceledOnTouchOutside(false);

		sp = context.getSharedPreferences("Settings", Activity.MODE_PRIVATE);
		remoteMac = sp.getString("remoteMac", "E0:76:D0:DA:0F:87");

		ET_Mac = (EditText) this.findViewById(R.id.et_mac);
		ET_Mac.setText(remoteMac);

		IB_SettingsOK = (Button) this.findViewById(R.id.settings_ok);
		IB_SettingsOK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String mac = ET_Mac.getText().toString();
				if(mac.equals("")){
					Toast.makeText(context, "����mac��ַ������Ϊ��!", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!isMac(mac)){
					Toast.makeText(context, "��������ȷ������mac��ַ!", Toast.LENGTH_SHORT).show();
					return;
				}
				if(sp != null){
					sp.edit().putString("remoteMac", mac).commit();
				}
				SettingsDialog.this.dismiss();
			}
		});

		IB_SettingsCancel = (Button) this.findViewById(R.id.settings_cancel);
		IB_SettingsCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingsDialog.this.dismiss();
			}
		});
	}

	/**
	 * У��ip��ַ�Ƿ���ȷ
	 */
	private final static String PATTERN = 
			"[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}";
	public static boolean isMac( String mac){
		Pattern patten = Pattern.compile(PATTERN);
		return patten.matcher( mac ).matches();		
	}

}