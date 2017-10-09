package com.centerm.t5demo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class TextDialog extends Dialog {

	Context context;
	private String strResult;
	private TextView textView;
	private ImageButton IB_Close;

	public TextDialog(Context context) {
		super(context);
		this.context = context;
	}

	public TextDialog(Context context, int theme){
		super(context, theme);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.text_show_dialog);
		this.setCanceledOnTouchOutside(false);
		textView = (TextView) this.findViewById(R.id.dialog_text);
		if(null != strResult){
			textView.setText(strResult);
		}

		IB_Close = (ImageButton) this.findViewById(R.id.close);
		IB_Close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TextDialog.this.dismiss();
			}
		});
	}

	public void setResult(String str){
		this.strResult = str;
	}
}