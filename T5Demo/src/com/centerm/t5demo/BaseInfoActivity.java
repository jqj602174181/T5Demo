package com.centerm.t5demo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.centerm.t5demo.util.OnMessageListener;
import com.centerm.t5demo.util.PinYin;
import com.centerm.t5demo.util.SureDialog;
import com.centerm.t5demolibrary.device.finger.FingerFunc;
import com.centerm.t5demolibrary.device.iccard.ICCardData;
import com.centerm.t5demolibrary.device.iccard.ICCardFunc;
import com.centerm.t5demolibrary.device.idcard.IDCardFunc;
import com.centerm.t5demolibrary.device.keypad.KeypadFunc;
import com.centerm.t5demolibrary.device.magcard.MagCardFunc;
import com.centerm.t5demolibrary.device.magcard.MagCardInfo;
import com.centerm.t5demolibrary.device.signature.SignatureFunc;
import com.centerm.t5demolibrary.transfer.TransControl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class BaseInfoActivity extends Activity implements View.OnClickListener, OnMessageListener{

	private String TAG = "BaseInfoActivity";
	private static final int MSG_TYPE_EVENT_CLICK = 201;

	private SureDialog sureDialog;

	private Context mContext = null;

	private Button mSignOpenBtn, mSignCloseBtn, mGetSignBtn;
	private Button mReadIdCardBtn;
	private Button mReadIcCardBtn;
	private Button mReadMagCardBtn;
	private Button mKeypadOpenBtn;
	private Button mKeypadCloseBtn;
	private Button mFingerReadBtn;
	private Button mFingerRegisterBtn;

	private UpdateHandler mHandler;
	private ImageView mIv_photo;
	private TextView mTv_person_info;
	private EditText mEt_iccard;
	private EditText mEt_magcard;
	private EditText mEt_password;
	private RadioButton RB_Bankbook, RB_MagneticCard;

	private HashMap<Integer, Button> m_hashBtns;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_baseinfo);

		mContext = this;

		HandlerThread mWayHandlerThread = new HandlerThread(TAG);
		mWayHandlerThread.start();
		mHandler = new UpdateHandler(this, mWayHandlerThread.getLooper());

		initView();
	}

	private void initView(){
		mSignOpenBtn= (Button)findViewById(R.id.BTN_Sign_Open);
		mSignCloseBtn = (Button)findViewById(R.id.BTN_Sign_Close);
		mGetSignBtn = (Button)findViewById(R.id.BTN_Sign_Get);
		mReadIdCardBtn = (Button)findViewById(R.id.BTN_READIDCARD);
		mReadIcCardBtn = (Button)findViewById(R.id.BTN_ReadICCard);
		mReadMagCardBtn = (Button)findViewById(R.id.BTN_ReadMsg);
		mKeypadOpenBtn = (Button)findViewById(R.id.BTN_ReadPassword);
		mKeypadCloseBtn = (Button)findViewById(R.id.BTN_ClosePassword);
		mFingerReadBtn = (Button)findViewById(R.id.BTN_ReadFinger);
		mFingerRegisterBtn = (Button)findViewById(R.id.BTN_RegisterFinger);
		mSignOpenBtn.setOnClickListener(this);
		mSignCloseBtn.setOnClickListener(this);
		mGetSignBtn.setOnClickListener(this);
		mReadIdCardBtn.setOnClickListener(this);
		mReadIcCardBtn.setOnClickListener(this);
		mReadMagCardBtn.setOnClickListener(this);
		mKeypadOpenBtn.setOnClickListener(this);
		mKeypadCloseBtn.setOnClickListener(this);
		mFingerReadBtn.setOnClickListener(this);
		mFingerRegisterBtn.setOnClickListener(this);

		mIv_photo = (ImageView)findViewById(R.id.iv_photo);
		mTv_person_info = (TextView)findViewById(R.id.tv_person_info);
		mEt_iccard = (EditText)findViewById(R.id.EDIT_IC);
		mEt_password = (EditText)findViewById(R.id.EDIT_Password);
		mEt_magcard = (EditText)findViewById(R.id.EDIT_Msg);
		RB_Bankbook = (RadioButton)findViewById(R.id.RB_Bankbook);
		RB_MagneticCard = (RadioButton)findViewById(R.id.RB_MagneticCard);

		m_hashBtns = new HashMap<Integer, Button>();
		m_hashBtns.put(R.id.BTN_Sign_Open, mSignOpenBtn);
		//		m_hashBtns.put(R.id.BTN_Sign_Close, mSignCloseBtn);
		m_hashBtns.put(R.id.BTN_Sign_Get, mGetSignBtn);
		m_hashBtns.put(R.id.BTN_READIDCARD, mReadIdCardBtn);
		m_hashBtns.put(R.id.BTN_ReadICCard, mReadIcCardBtn);
		m_hashBtns.put(R.id.BTN_ReadMsg, mReadMagCardBtn);
		m_hashBtns.put(R.id.BTN_ReadPassword, mKeypadOpenBtn);
		m_hashBtns.put(R.id.BTN_ReadFinger, mFingerReadBtn);
		m_hashBtns.put(R.id.BTN_RegisterFinger, mFingerRegisterBtn);
	}

	private class UpdateHandler extends Handler
	{
		private Context mContext;

		UpdateHandler(Context context, Looper looper_)
		{
			super(looper_);
			mContext = context;
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case MSG_TYPE_EVENT_CLICK:
				int nRet = 0;
				String result = null;
				View v = (View)msg.obj;
				setButtonsEnable(v.getId(), false);
				final StringBuffer buffer = new StringBuffer();
				switch(v.getId()){
				case R.id.BTN_Sign_Open: //启动签名
					SignatureFunc.getInstance().startSignature(60);//调用启动电子签名
					result = SignatureFunc.getInstance().getSwInfo();
					Log.e(TAG, "启动签名结果：" + result);
					break;

				case R.id.BTN_Sign_Get: //获取电子签名
					String[] temp = new String[1];
					nRet = SignatureFunc.getInstance().getSignPic(temp); //调用获取电子签名

					if(temp[0] != null){
						Bitmap bitmap = BitmapFactory.decodeFile(temp[0]);
						if(bitmap == null){
							Log.e(TAG, "signature bitmap parse error.");
							return;
						}
						PicDialog dialog = new PicDialog(BaseInfoActivity.this);
						dialog.setPicture(bitmap);
						dialog.show();
					}
					break;

				case R.id.BTN_READIDCARD: //二代证
					showPersonInfo(null);
					String[] perInfo = new String[11]; 
					nRet = IDCardFunc.getInstance().getIDCardInfoOnce(perInfo); //调用读取ID卡
					if (nRet < 0)
					{
						Log.e(TAG, IDCardFunc.getInstance().getSwInfo() + ", 错误码:" + nRet);
						setButtonsEnable(v.getId(), true);
						return;
					}
					showPersonInfo(perInfo);
					break;

				case R.id.BTN_ReadPassword: //密码键盘
					myRunOnUiThread(new Runnable() {
						@Override
						public void run() {
							mEt_password.setText("");
						}
					});

					nRet = KeypadFunc.getInstance().startKeypad(20); //调用启动密码键盘

					myRunOnUiThread(new Runnable() {
						@Override
						public void run() {
							mEt_password.setText(KeypadFunc.getInstance().getSwInfo());
						}
					});
					break;

				case R.id.BTN_ReadMsg: //磁卡
					myRunOnUiThread(new Runnable() {
						@Override
						public void run() {
							mEt_magcard.setText("");
						}
					});
					MagCardInfo magCardInfo = new MagCardInfo(); 
					nRet = MagCardFunc.getInstance().readTrack(magCardInfo); //调用读取磁卡

					byte[] track1 = magCardInfo.getTrack1();
					byte[] track2 = magCardInfo.getTrack2();
					byte[] track3 = magCardInfo.getTrack3();

					Log.e(TAG, "数据：" + new String(track1) +","+new String(track2) +","+new String(track3));
					if(track1 != null){
						buffer.append(new String(track1).trim());
					}
					if(track2 != null){
						buffer.append(",");
						buffer.append(new String(track2).trim());
					}
					if(track3 != null){
						buffer.append(",");
						buffer.append(new String(track3).trim());
					}
					myRunOnUiThread(new Runnable() {
						@Override
						public void run() {
							mEt_magcard.setText(buffer.toString());
						}
					});
					break;

				case R.id.BTN_ReadICCard: //IC卡
					myRunOnUiThread(new Runnable() {
						@Override
						public void run() {
							mEt_iccard.setText("");
						}
					});

					ICCardData data = new ICCardData();

					if(RB_Bankbook.isChecked()){ //接触式
						data.setCardStyle(1);
						nRet = ICCardFunc.getInstance().getICCardInfo(data); //调用读取IC卡
						result =  ICCardFunc.getInstance().getICData();
					}else if(RB_MagneticCard.isChecked()){
						data.setCardStyle(2);
						nRet = ICCardFunc.getInstance().getICCardInfo(data); //调用读取IC卡
						result =  ICCardFunc.getInstance().getICData();
					}
					buffer.append(result);
					myRunOnUiThread(new Runnable() {
						@Override
						public void run() {
							mEt_iccard.setText(buffer.toString());
						}
					});
					Log.e(TAG, "数据：" + result);
					break;

				case R.id.BTN_ReadFinger: //指纹仪  串口
					nRet = FingerFunc.getInstance().readFingerFeature(20);
					result = FingerFunc.getInstance().getSwInfo();
					Log.e(TAG, "指纹仪特征数据：" + result);
					if(result != null){
						TextDialog dialog = new TextDialog(BaseInfoActivity.this);
						dialog.setResult(result);
						dialog.show();
					}
					break;

				case R.id.BTN_RegisterFinger: //指纹仪登记  串口
					nRet = FingerFunc.getInstance().registerFingerFeature(20);
					result = FingerFunc.getInstance().getSwInfo();
					Log.e(TAG, "指纹仪特征数据：" + result);
					if(result != null){
						TextDialog dialog = new TextDialog(BaseInfoActivity.this);
						dialog.setResult(result);
						dialog.show();
					}
					break;

				default:
					return;
				}

				if(nRet != -6)
					setButtonsEnable(v.getId(), true);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.BTN_Sign_Close){ //关闭签名
			TransControl.getInstance().setUserCancel(true);
			SystemClock.sleep(500);
			SignatureFunc.getInstance().signOff();
			setButtonsEnable(v.getId(), true);
			TransControl.getInstance().setUserCancel(false);
			return;
		}else if(v.getId() == R.id.BTN_ClosePassword){
			TransControl.getInstance().setUserCancel(true);
			SystemClock.sleep(500);
			KeypadFunc.getInstance().closeKeyPad();
			setButtonsEnable(v.getId(), true);
			TransControl.getInstance().setUserCancel(false);
		}
		Message msg = mHandler.obtainMessage(MSG_TYPE_EVENT_CLICK, v);
		mHandler.sendMessage(msg);
	}

	private void showPersonInfo(final String[] szPersonInfo)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if(szPersonInfo == null){
					mTv_person_info.setText(getResources().getString(R.string.unknow_idcardinfo));
					mIv_photo.setImageBitmap(null);
				}else{
					String str = "姓名：%s　　拼音：%s\n性别：%s　　民族：%s　　出生日期：%s\n发证机关：%s　　\n证件到期日：%s\n身份证号：%s\n地址：%s";
					String name = szPersonInfo[0];
					String sex = szPersonInfo[1];
					String nation = szPersonInfo[2];
					String num = szPersonInfo[3];
					String birthday = szPersonInfo[4];
					String address = szPersonInfo[5];
					String police = szPersonInfo[6];
					String validstart = szPersonInfo[7];
					String validend = szPersonInfo[8];

					String pinyin = PinYin.getPinYin(name);
					str = String.format(str, name, pinyin, sex, nation, birthday, police, validstart+"-"+validend, num, address);
					mTv_person_info.setText(str);

					//				if (null != szPersonInfo[10] && mIDCardCmd.hasFingerprint())
					//				{
					//					tvFingerInfo.setText("有数据");
					//				} else
					//				{
					//					tvFingerInfo.setText("空");
					//				}

					String mStrImagPath = szPersonInfo[9];
					Bitmap image = BitmapFactory.decodeFile(mStrImagPath);
					mIv_photo.setImageBitmap(image);
				}
			}
		});
	}

	private void setButtonsEnable(final int id, final boolean enabled)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Iterator<Entry<Integer, Button>> iter = m_hashBtns.entrySet().iterator();
				while (iter.hasNext())
				{
					Map.Entry entry = (Map.Entry) iter.next();
					Integer key = (Integer) entry.getKey();
					Button val = (Button) entry.getValue();
					if (key == id){
						val.setEnabled(true);
					}else{
						val.setEnabled(enabled);
					}
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(sureDialog==null){
			sureDialog = new SureDialog(this, this);
		}
		sureDialog.showDialog();
	}

	@Override
	public void onMessageChange(int msg) {
		switch (msg) {
		case 1:
			finish();//退出 
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void myRunOnUiThread(Runnable runnable){
		this.runOnUiThread(runnable);
	}
}
