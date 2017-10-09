package com.centerm.t5demo.util;

import com.centerm.t5demo.R;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SureDialog extends PopupWindow implements OnClickListener{


	private View mainView;
	private Button okButton;
	private Button cancelButton;
	private TextView titleTextView;
	private Context context;
	private OnMessageListener messageListener;

	public SureDialog(Activity activity,OnMessageListener listener) {
		super(activity);
		mainView = activity.getLayoutInflater().inflate(R.layout.exit_dialog, null);
		this.setContentView(mainView);
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		this.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.t5_on_select));
		getId();
		messageListener = listener;
	}

	private void getId()
	{
		okButton = (Button)mainView.findViewById(R.id.exit_ok);
		cancelButton = (Button)mainView.findViewById(R.id.exit_cancel);
		titleTextView = (TextView)mainView.findViewById(R.id.exit_exitTextView);
		cancelButton.setOnClickListener(this);
		okButton.setOnClickListener(this);
	}

	public void setTitle(String title)
	{
		titleTextView.setText(title);
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.exit_cancel:
			break;
		case R.id.exit_ok:
			messageListener.onMessageChange(1);
			break;
		default:
			break;
		}
		dismiss();
	}

	public void showDialog()
	{
		if(!isShowing()){
			showAtLocation(mainView, Gravity.CENTER,0,0);
		}
	}
	
	@Override
	public void dismiss() {
		super.dismiss();

	}
}
