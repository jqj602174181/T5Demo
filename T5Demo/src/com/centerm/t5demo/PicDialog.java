package com.centerm.t5demo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

public class PicDialog extends Dialog {

	Context context;
	private Bitmap img;
	private ImageView imgView;
	private ImageButton IB_Close;

	public PicDialog(Context context) {
		super(context);
		this.context = context;
	}

	public PicDialog(Context context, int theme, Bitmap bm){
		super(context,theme);
		this.img = bm;
		this.context = context;
	}

	public PicDialog(Context context, int theme){
		super(context, theme);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.pic_show_dialog);
		this.setCanceledOnTouchOutside(false);
		imgView = (ImageView) this.findViewById(R.id.dialog_title_image);
		if(null != img){
			imgView.setImageBitmap(img);
		}

		IB_Close = (ImageButton) this.findViewById(R.id.close);
		IB_Close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PicDialog.this.dismiss();
			}
		});
	}

	@Override
	public void onAttachedToWindow(){
		if(null != img)
			imgView.setImageBitmap(img);
	}

	public void setPicture(Bitmap picture){
		this.img = picture;
	}
}