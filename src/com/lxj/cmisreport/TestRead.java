package com.lxj.cmisreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

/**
 * 
 * 测试类，读取内存中的JSON文件
 *
 */

public class TestRead {
	public String str;
	public Context mContext;

	public TestRead(Context context) {
		/*if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
		String pathString = Environment.getExternalStorageDirectory()
				.toString() + File.separator + filename;*/
		mContext = context;
		toBuffer();
	}

	public void toBuffer(){
		BufferedReader bi = null;
		try {		
			InputStream inputStream = mContext.getAssets().open("reportContent.txt");
			
			bi = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = bi.readLine()) != null) {
				sb.append(line+"\n");	
			}
			str = sb.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bi != null) {
				try {
					bi.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public String getStr() {
		return str;
	}
}
