package com.centerm.t5demo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class CrashUtil {

	private static String TAG = "CrashUtil";
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	public static void saveCrashInfo2File(Throwable ex)
	{
		StringBuffer sb = new StringBuffer();

		//将 StringBuffer sb 中的字符串写出到文件中
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer); 
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {  
			cause.printStackTrace(printWriter);  
			cause = cause.getCause();  
		}
		printWriter.close();
		sb.append(writer.toString());

		try {
			long timestamp = System.currentTimeMillis();
			String time = format.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".txt";
			//文件输出路径 
			String path = Environment.getExternalStorageDirectory().getPath() + "/t5demoinfo/";
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(path + fileName);
			fos.write(sb.toString().getBytes());
			fos.close();
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e); 
		}
	}
}
