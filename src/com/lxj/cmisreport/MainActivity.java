package com.lxj.cmisreport;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static DefaultHttpClient _httpClient = null;
	private Button open;
	private EditText nameText;
	private EditText passText;
	private CheckBox cbxPwd;
	private long current = 0;
	private static final String PREFS_NAME = "MyUserInfo";
	protected final static String ENCODE = "UTF-8";
	protected final static String HTTP_CHARSET = "UTF-8";
	protected static String host = "http://nadr.hust.edu.cn/cmis";
	protected static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36";
	Button setTime,close;
	PendingIntent pi ;
	AlarmManager aManager;
	Calendar currentTime = Calendar.getInstance();
	
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x123:
				Toast.makeText(MainActivity.this, "服务器没开", Toast.LENGTH_SHORT)
						.show();
				break;
			case 0x124:
				rand(this);
				break;

			case 0x125:
				//Login("LEBRONS", "yyt243783340123",this);
				Log.e("name and pass", nameText.getText().toString()+passText.getText().toString());
				Login(nameText.getText().toString(), passText.getText().toString(),this);
				
				break;
			case 0x126:
				preReport(this);
		break;
			case 0x127:
				Report(this);break;
			case 0x128:
				Toast.makeText(MainActivity.this, "提交成功", Toast.LENGTH_SHORT)
				.show();break;
			case 0x129:
				Toast.makeText(MainActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT)
				.show();break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTime = (Button) findViewById(R.id.setTime);
		close = (Button) findViewById(R.id.close);
		// 获取AlarmManager对象
				aManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		open = (Button) super.findViewById(R.id.open);
		nameText = (EditText)super.findViewById(R.id.edit_admin);
		passText = (EditText)super.findViewById(R.id.edit_pass);
		cbxPwd = (CheckBox)super.findViewById(R.id.rememberPwd);
		 LoadUserDate();
		
		open.setOnClickListener(new openOnclickListener());
		close.setOnClickListener(new closeOnClickListener());
		cbxPwd.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				SaveUserDate();
			}
			
		});
		setTimeToReport();
	}
	public void setTimeToReport(){
		
		
		// 为“设置闹铃”按钮绑定监听器。
		setTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View source) {
				Calendar currentTime = Calendar.getInstance();
				currentTime.setTimeInMillis(System.currentTimeMillis());
				// 创建一个TimePickerDialog实例，并把它显示出来。
				new TimePickerDialog(MainActivity.this, 0, // 绑定监听器
						new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker tp, int hourOfDay,
									int minute) {
								// 指定启动AlarmActivity组件
								Intent intent = new Intent(MainActivity.this,
										SetTimeService.class);
								intent.putExtra("name", nameText.getText().toString());
								intent.putExtra("pass", passText.getText().toString());
								// 创建PendingIntent对象
								 pi = PendingIntent.getService(
										MainActivity.this, 0, intent, 0);
								Calendar c = Calendar.getInstance();
								//c.setTimeInMillis(System.currentTimeMillis());
								// 根据用户选择时间来设置Calendar对象
								c.set(Calendar.HOUR_OF_DAY, hourOfDay);
								c.set(Calendar.MINUTE, minute);
								if (System.currentTimeMillis()>c.getTimeInMillis()) {
									c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)+1);
								}
								// 设置AlarmManager将在Calendar对应的时间启动指定组件
								aManager.setExact(AlarmManager.RTC_WAKEUP,
										c.getTimeInMillis(), pi);

								// 显示闹铃设置成功的提示信息
								Toast.makeText(MainActivity.this, "闹铃设置成功啦",
										Toast.LENGTH_SHORT).show();
							}
						}, currentTime.get(Calendar.HOUR_OF_DAY), currentTime
								.get(Calendar.MINUTE), true).show();
			}
		});
	}

	class openOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			 
			toIndex(handler);
		}

	}
	
	class closeOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			  Intent intent = new Intent(MainActivity.this,SetTimeService.class);
	            PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
	            //获取闹钟管理器
	            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	            alarmManager.cancel(pendingIntent);
		}
		
	}
	public static void rand(final Handler handle) {
		final HttpGet randRequest = new HttpGet(host + "/rand.action");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// randRequest =
					HttpResponse httpResponse = _httpClient
							.execute(randRequest);
					int statusCode = httpResponse.getStatusLine()
							.getStatusCode();
					if (statusCode != 200) {
						randRequest.abort();
						throw new RuntimeException(
								"HttpClient,error status code :" + statusCode);
					} else {
						Message msg = new Message();
						msg.what = 0x125;
						handle.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (randRequest != null)
						randRequest.abort();
				}
			}
		}).start();
	}
	public static void preReport(final Handler handler) {
		final HttpGet httpGet = new HttpGet("http://nadr.hust.edu.cn/cmis/report/toAddReport.action?type=1");;
		new Thread(new Runnable() {

			@Override
			public void run() {
		 try {
			 httpGet.addHeader("Referer", "http://nadr.hust.edu.cn/cmis/user/dologin.action");
			 HttpResponse httpResponse = _httpClient.execute(httpGet);
			  HttpEntity entity = httpResponse.getEntity();
			  String result = null;
			  if (entity != null) {
				  result = EntityUtils.toString(entity, "utf-8");
			  }
			  if(httpResponse.getStatusLine().getStatusCode()==200){
				  Message msg = new Message();
				  msg.what = 0x127;
				  handler.sendMessage(msg);
			  }
			 // System.out.println("toAddReport.action--->\n"+result);
			  } catch (Exception e) {
				  e.printStackTrace();
			  }
		 finally{
			 if(httpGet!=null) {
				 httpGet.abort();
			 }
		 }
			}
		}).start();
	}

	public static void Login(final String name, final String passWord,final Handler handler) {
		final HttpPost doLoginPost = new HttpPost(host + "/user/login.action");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("j_username", name));
					params.add(new BasicNameValuePair("j_password", passWord));
					params.add(new BasicNameValuePair("rand", "1991"));
					doLoginPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP_CHARSET));
					doLoginPost.addHeader("Referer",
							"http://nadr.hust.edu.cn/cmis/toIndex.action");
					HttpResponse httpResponse = _httpClient
							.execute(doLoginPost);
					int statusCode = httpResponse.getStatusLine()
							.getStatusCode();
					 HttpEntity entity = httpResponse.getEntity();
					String context = null;
					 if (entity != null) {
					 context = EntityUtils.toString(entity, "utf-8");
					 }
					 Log.e("loginerror", context);
					 if(context.contains("错误")){
						 Message msg = new Message();
						 msg.what = 0x129;
						 handler.sendMessage(msg);
					 }else{
						 Message msg = new Message();
						 msg.what = 0x126;
						 handler.sendMessage(msg);
					 }
					// System.out.println(context);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					if (doLoginPost != null)
						doLoginPost.abort();
				}
			}
		}).start();
	}

	public void Report(final Handler handler) {
		final HttpPost reportPost = new HttpPost(host+"/report/addReport.action");
		 String contentString = new TestRead(this).getStr();
		final String reportContent;
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(new Date());
		 if(calendar.get(Calendar.DAY_OF_WEEK)-1==1){
			 reportContent = contentString.split("Week")[0].trim();
			} else if(calendar.get(Calendar.DAY_OF_WEEK)-1==5) {
				reportContent = contentString.split("Week")[2].trim();
			} else {
				reportContent = contentString.split("Week")[1].trim();
			}

		new Thread(new Runnable() {

			@Override
			public void run() {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String time = format.format(new Date());
			List <NameValuePair> params = new ArrayList<NameValuePair>();  
	        params.add(new BasicNameValuePair("type", "1")); 
	        params.add(new BasicNameValuePair("report.reportdate", time)); 
	        params.add(new BasicNameValuePair("report.content",reportContent)); 
	        reportPost.setEntity(new UrlEncodedFormEntity(params,HTTP_CHARSET));
	        reportPost.addHeader("Referer", "http://nadr.hust.edu.cn/cmis/report/toAddReport.action?type=1");
			HttpResponse httpResponse = _httpClient.execute(reportPost);
			 int statusCode = httpResponse.getStatusLine().getStatusCode();
			 if (statusCode == 200) {
				 Message msg = new Message();
				 msg.what = 0x128;
				 handler.sendMessage(msg);
			 }
//				 doLoginPost.abort();
//			     throw new RuntimeException("HttpClient,error status code :"
//			    		 + statusCode);
//			  }
			HttpEntity entity = httpResponse.getEntity();
			String context = null;
			if (entity != null) {
				 context = EntityUtils.toString(entity, "utf-8");
			 }
			//System.out.println("toAddReport.action--->\n"+context);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if(reportPost!=null) 
				reportPost.abort();
		}
			}
		}).start();
	}
	

	public static void toIndex(final Handler handler) {

		final HttpGet toIndexRequest = new HttpGet(host + "/toIndex.action");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					_httpClient = new DefaultHttpClient();
					// toIndexRequest = new HttpGet(host+"/toIndex.action");
					HttpResponse httpResponse = _httpClient
							.execute(toIndexRequest);
					HttpEntity entity = httpResponse.getEntity();
					String responseString = entity.toString();
					int statusCode = httpResponse.getStatusLine()
							.getStatusCode();
					Log.e("error", statusCode + "");
					if (statusCode != 200) {
						toIndexRequest.abort();
						Message msg = new Message();
						msg.what = 0x123;
						handler.sendMessage(msg);
						throw new RuntimeException(
								"HttpClient,error status code :" + statusCode);

					} else {
						Message msg = new Message();
						msg.what = 0x124;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (toIndexRequest != null)
						// toIndexRequest.releaseConnection();
						toIndexRequest.abort();
				}
			}
		}).start();

	}
	// 璁颁綇鐢ㄦ埛鍚嶅拰瀵嗙爜
		public void SaveUserDate() {
			SharedPreferences preferences = getSharedPreferences(
					PREFS_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			if (cbxPwd.isChecked()) {

				editor.putBoolean("isSaved", true);
				editor.putString("name", nameText.getText().toString());
				editor.putString("password", passText.getText().toString());
			} else {
				editor.putBoolean("isSaved", false);
				editor.putString("name", nameText.getText().toString());
				editor.putString("password", "");
			}
			editor.commit();
		}

		// 鏄剧ず鐢ㄦ埛鍚嶅拰瀵嗙爜
		public void LoadUserDate() {
			SharedPreferences mPreferences = getSharedPreferences(
					PREFS_NAME, 0);
			if (mPreferences.getBoolean("isSaved", false)) {
				String nameString = mPreferences.getString("name", "");
				String passString = mPreferences.getString("password", "");
				if (!"".equals(passString)) {
					nameText.setText(nameString);
					passText.setText(passString);
					cbxPwd.setChecked(true);
				} else {
					cbxPwd.setChecked(false);
				}
			} else {
				String nameString = mPreferences.getString("name", "");
				String passString = "";
				nameText.setText(nameString);
				passText.setText(passString);
			}
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK&&event.getAction() == KeyEvent.ACTION_DOWN){
				if(System.currentTimeMillis() - current >= 2000){
					   Toast. makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT ).show();
					   current = System. currentTimeMillis();
				}else{
					this.finish();
				}
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

}
