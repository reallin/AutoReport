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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class SetTimeService extends Service {
	public static String name ;
	public static String pass ;
	private long current = 0;
	private static final String PREFS_NAME = "MyUserInfo";
	protected final static String ENCODE = "UTF-8";
	protected final static String HTTP_CHARSET = "UTF-8";
	protected static String host = "http://nadr.hust.edu.cn/cmis";
	protected static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36";
	public AlarmManager alarm;
	public PendingIntent pi; 
	protected static DefaultHttpClient _httpClient = null;
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x123:
				Toast.makeText(SetTimeService.this, "服务器还没开", Toast.LENGTH_SHORT)
						.show();
				alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
				int time = 30*1000;
				long trigger = SystemClock.elapsedRealtime()+time;
				Intent intent = new Intent(SetTimeService.this,BroadCastBack.class);
				intent.putExtra("name", name);
				intent.putExtra("pass", pass);
				pi = PendingIntent.getBroadcast(SetTimeService.this, 0, intent, 0);
				alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, pi);
				break;
			case 0x124:	
				rand(this);
				break;

			case 0x125:
				//Login("LEBRONS", "yyt243783340123",this);
				
				Login(name, pass,this);
				
				break;
			case 0x126:
				preReport(this);
		break;
			case 0x127:
				Report(this);break;
			case 0x128:
				Toast.makeText(SetTimeService.this, "提交成功", Toast.LENGTH_SHORT)
				.show();
				SetTimeService.this.stopSelf();
				break;
				
			case 0x129:
				Toast.makeText(SetTimeService.this, "用户名或密码错误", Toast.LENGTH_SHORT)
				.show();
				SetTimeService.this.stopSelf();break;
			}
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		name = intent.getStringExtra("name");
		pass = intent.getStringExtra("pass");
		new Thread(new Runnable(){
			
			@Override
			public void run() {
				Log.e("enter", "service打开了");
				Log.e("enter", name);
				Log.e("enter", pass);
				toIndex(handler);
			}
			
		}).start();
		
		
		
		
		return super.onStartCommand(intent, flags, startId);
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
	

}
