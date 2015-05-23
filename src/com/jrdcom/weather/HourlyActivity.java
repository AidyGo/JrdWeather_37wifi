/**************************************************************************************************/
/*                                                                     Date : 10/2012 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2012 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  Feng zhuang                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/HourlyActivity.java   */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.weather;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.jrdcom.bean.HourForShow;
import com.jrdcom.data.MyService;
import com.jrdcom.util.CommonUtils;
import com.jrdcom.util.CustomizeUtils;
import com.jrdcom.weather.R;

public class HourlyActivity extends Activity {

	private MyService myService;
	private static final String TAG = "HourlyActivity";
	private String mLocationKey;
	private String mCityName;
	private ActionBar mActionBar;
	private List<HourForShow> hours = new ArrayList<HourForShow>();
	private boolean isUnitC = true;
	private static final String UNITC = "°C";
	private static final String UNITF = "°F";
	private boolean isWifiConnected;
	private boolean isMobileConnected;

	private WebView mWebView;
	private Button bt_close;
	private ListView mListView;
	private FrameLayout mFrameLayout;

	private WebSettings mWebSettings;
	private MyBroadcasReceiver mBroadcastReceiver;

	private String strAppId;
	private String strPartnerCode;
	private String strIpAddress;
	private String strUserAgent;
	private String strCurrentZipCode;
	private String strWeatherIcon;
	private String strUUID;

	private boolean isOtherConnected = false; // add by jielong.xing at 2014-09-05 begin
	
	private MyListAdapter mListAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!CommonUtils.isSupportHorizontal(this)) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		Intent i = getIntent();
		mLocationKey = i.getExtras().getString("locationKey");
		mCityName = i.getExtras().getString("cityName");

		setContentView(R.layout.hourly_layout);

		mActionBar = this.getActionBar();
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);

		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.titel_night_bg));
		mActionBar.setTitle(mCityName);

		mListView = (ListView) findViewById(R.id.lv_hourly);
		mListView.setDivider(null);

		mWebView = (WebView) findViewById(R.id.webView_ad);
		mWebSettings = mWebView.getSettings();
		mWebSettings.setJavaScriptEnabled(true);

		mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
		mFrameLayout.setVisibility(View.GONE);

		ImageView imgView = (ImageView) findViewById(R.id.img_shape);
		imgView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = null;
				if (mAdUrl != null) {
					content_url = Uri.parse(mAdUrl);
				} else {
					content_url = Uri.parse("http://www.accuweather.com");
				}
				intent.setData(content_url);
				startActivity(intent);
			}
		});

		// CR 575123- Neo Skunkworks - Wells Tang - 001 Begin
		mWebView.setWebViewClient(mWebViewClient);
		// CR 575123- Neo Skunkworks - Wells Tang - 001 End

		bt_close = (Button) findViewById(R.id.bt_ad_close);
		bt_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFrameLayout.setVisibility(View.GONE);
			}
		});
		
		Intent intent = new Intent(HourlyActivity.this, MyService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();		

		SharedPreferences sharedata = getSharedPreferences("weather",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		String unit = CustomizeUtils.getString(HourlyActivity.this, "def_weather_unit_name");
		unit = CustomizeUtils.splitQuotationMarks(unit);
		if ("isUnitF".equals(unit)) {
			isUnitC = sharedata.getBoolean("unit", false);
		} else {
			isUnitC = sharedata.getBoolean("unit", true);
		}

		registerBoradcastReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();		
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

//		Intent intent = new Intent();
//		intent.setClass(HourlyActivity.this, MainActivity.class);
//		intent.putExtra("newCityKey", mLocationKey);
//		startActivity(intent);

		this.finish();
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			myService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			updateConnectedFlags();

			myService = ((MyService.MyBinder) service).getService();
			if (myService != null) {
				hours = myService.getHours(mLocationKey);
				/* FR 485553- Neo Skunkworks - James Jiang - 001 Begin */
				getHourStandard();
				/* FR 485553- Neo Skunkworks - James Jiang - 001 End */
				mListAdapter = new MyListAdapter(HourlyActivity.this, getData());
				mListView.setAdapter(mListAdapter);
				mListView.setDivider(HourlyActivity.this.getResources().getDrawable(R.drawable.line_h));
				mListView.setDividerHeight(1);

				// PR351549-Feng.Zhuang-001 Modify begin
				/*if (isWifiConnected || isMobileConnected || isOtherConnected) {
					// PR684354-Neo Skunkworks-kehao.wei-001 modify begin
					try {
						requestAd();
					} catch (Exception e) {
						Log.e(TAG, "request Ad failed : " + e.toString());
					}
					// PR684354-Neo Skunkworks-kehao.wei-001 modify end
				}*/
			}
		}
	};

	private void requestAd() {
		strAppId = "strAppID=androidapptcl&";
		strPartnerCode = "strPartnerCode=androidapptcl&";
		strIpAddress = "strIpAddress=" + getHostIP().split("%")[0] + "&";

		try {
			strUserAgent = "strUserAgent=" + URLEncoder.encode(mWebSettings.getUserAgentString(), "utf-8") + "&";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		strCurrentZipCode = "strCurrentZipCode=" + mLocationKey.replace(":", "=") + "&";
		strWeatherIcon = "strWeatherIcon=" + myService.getStrWeatherIconFromDB(mLocationKey) + "&";
		strUUID = "strUUID=" + getUUID();

		String ad_url = "http://www.accuweather.com/adrequest/adrequest.asmx/getAdCode?"
				+ strAppId
				+ strPartnerCode
				+ strIpAddress
				+ strUserAgent
				+ strCurrentZipCode + strWeatherIcon + strUUID;

		myService.getAdvertisement(ad_url);
	}

	private boolean is24Hour = false;

	/**
	 * get the time standard of system
	 * 
	 * @return
	 */
	private boolean getHourStandard() {
		ContentResolver cv = getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
		if ("24".equals(strTimeFormat)) {
			is24Hour = true;
		} else if ("12".equals(strTimeFormat)) {
			is24Hour = false;
		}
		return is24Hour;
	}

	/* FR 485553- Neo Skunkworks - James Jiang - 001 End */
	private ArrayList<HashMap<String, Object>> getData() {
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		// PR 466448 - Neo Skunkworks - Tom Yu - 001 begin
		int nHoursSize = hours.size();
		// for (int i = 0; i < 24; i++) {
		for (int i = 0; i < nHoursSize && i < 24; i++) {
			// PR 466448 - Neo Skunkworks - Tom Yu - 001 end
			map = new HashMap<String, Object>();

			String imagepath = "normal_widget_icons_" + (Integer.parseInt(hours.get(i).getIcon()) < 10? "0" : "") + hours.get(i).getIcon();
			int resId = getResources().getIdentifier(imagepath, "drawable", "com.jrdcom.weather");

			map.put("week", getWeekly(hours.get(i).getWeek()));
			String time = getHourOfTime(hours.get(i).getTime());
			map.put("time", time);
			map.put("icon", resId);
			if (isUnitC) {
				map.put("temp", f2c(hours.get(i).getTemp()) + UNITC);
			} else {
				map.put("temp", (int)Double.parseDouble(hours.get(i).getTemp()) + UNITF);
			}
			map.put("text", getWeatherText(hours.get(i).getIcon()));
			int timeType = getTimeType(hours.get(i).getTime(), Integer.parseInt(hours.get(i).getIcon().trim()));
			int background = 0;
			int fontColor = 0;
			switch (timeType) {
			case DAYTIME:
				background = R.drawable.bg_day;
				fontColor = 0xFF9FA0A0;
				break;
			case DAWNTIME:
				background = R.drawable.bg_dawn;
				fontColor = 0xFFFFFFFF;
				break;
			case NIGHTTIME:
				background = R.drawable.bg_night;
				fontColor = 0xFFFFFFFF;
				break;
			}

			map.put("background", background);
			map.put("fontColor", fontColor);
			list.add(map);
		}

		return list;
	}

	/**
	 * base on the time standard of system, exchange to corresponding time.
	 * 
	 * @param time
	 * @return
	 */
	private String getHourOfTime(String time) {
		if (is24Hour) {
			if (time.equals("12 PM")) {
				time = "12:00";
			} else if (time.equals("12 AM")) {
				time = "0:00";
			} else if (time.contains("PM")) {
				int num = Integer.parseInt(time.substring(0, time.indexOf(" PM")));
				if (num > 0 && num < 12) {
					time = (num + 12) + ":00";
				}
			} else if (time.contains("AM")) {
				int num = Integer.parseInt(time.substring(0, time.indexOf(" AM")));
				if (num > 0 && num < 12) {
					time = num + ":00";
				}
			}
		}
		else {
			if (time.contains("PM")) {
				time = time.replace("PM", getString(R.string.date_pm));
			} else if (time.contains("AM")) {
				time = time.replace("AM", getString(R.string.date_am));
			}
		}
		return time;
	}

	private static final int DAYTIME = 0;
	private static final int DAWNTIME = 1;
	private static final int NIGHTTIME = 2;

	private int getTimeType(String time, int icon) {
		int hour = 0;
		if (time.equals("12 PM")) {
			hour = 12;
		} else if (time.equals("12 AM")) {
			hour = 0;
		} else if (time.contains("PM")) {
			int num = Integer.parseInt(time.substring(0, time.indexOf(" PM")));
			if (num > 0 && num < 12) {
				hour = num + 12;
			}
		} else if (time.contains("AM")) {
			int num = Integer.parseInt(time.substring(0, time.indexOf(" AM")));
			if (num > 0 && num < 12) {
				hour = num;
			}
		}

		if ((hour >= 5 && hour < 7) || (hour == 7 && icon >= 33)
				|| (hour == 17 && icon >= 33) || (hour > 17 && hour <= 19)) {
			return DAWNTIME;
		} else if (hour == 7 && icon < 33) {
			return DAYTIME;
		} else if (hour == 17 && icon < 33) {
			return DAYTIME;
		} else if (hour >= 8 && hour < 17) {
			return DAYTIME;
		} else {
			return NIGHTTIME;
		}
	}

	private void registerBoradcastReceiver() {
		mBroadcastReceiver = new MyBroadcasReceiver();
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("android.intent.action.ADVERTISE_BROADCAST");
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private String mAdUrl = null;

	private class MyBroadcasReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();

			String result = b.getString("advertise");
			if (null == result || result.length() <= 0) {
				return;
			}
			int start = result.indexOf("href=\"");
			if (start != -1) {
				start += "href=\"".length();
			}
			int i = 1;
			int end = 0;
			while (result.charAt(start + i) != '\"') {
				i++;
				continue;
			}
			end = start + i;

			if (start != -1) {
				mAdUrl = result.substring(start, end);
			}

			if (result != null && result.length() != 0) {
				mWebView.loadData(result, "text/html", "utf-8");
			}
		}
	}

	WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap bitmap) {
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			mFrameLayout.setVisibility(View.VISIBLE);
		}
	};

	private String f2c(String fahrenheit) {
		int celsius = ((int)Double.parseDouble(fahrenheit) - 32) * 5 / 9;
		return celsius + "";
	}

	private String getUUID() {
		String rad = Math.random() * 100 + "";
		UUID deviceMd5Uuid = UUID.nameUUIDFromBytes(rad.getBytes());

		return getMD5(deviceMd5Uuid.toString());
	}

	public String getMD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}

		byte[] md5Bytes = md5.digest(byteArray);

		StringBuffer hexValue = new StringBuffer();

		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}

		return hexValue.toString();
	}

	// Get the IPAdress of the device,only for user version.
	private String getHostIP() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr.hasMoreElements();) {
					InetAddress inetAddress = ipAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Check the internet connection.
	private void updateConnectedFlags() {
		ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService("connectivity");

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			isMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
			if (!isWifiConnected && !isMobileConnected) {
				isOtherConnected = true;
			}
		} else {
			isWifiConnected = false;
			isMobileConnected = false;
			isOtherConnected = false;
		}
	}

	// PR345764-Feng.Zhuang-001 Add begin
	private String getWeatherText(String weatherID) {
		int iconID = Integer.parseInt(weatherID) - 1;
		String[] weatherDescriptions = this.getResources().getStringArray(R.array.weather_icon_desc);

		return weatherDescriptions[iconID];
	}

	private String getWeekly(String sWeek) {
		String[] enWeekly = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri",
				"Sat", "Sun" };
		if (sWeek != null) {
			String result = null;
			String[] weekly = this.getResources().getStringArray(R.array.weather_weekly);
			for (int i = 0; i < 7; i++) {
				if (sWeek.contains(enWeekly[i])) {
					result = weekly[i];
				}
			}
			return result;
		} else {
			return "";
		}
	}
	// PR345764-Feng.Zhuang-001 Add end

	@Override
	public void onConfigurationChanged(Configuration newConfig) {		
		super.onConfigurationChanged(newConfig);
		mListAdapter = new MyListAdapter(HourlyActivity.this, getData());
		mListView.setAdapter(mListAdapter);
		mListView.setDivider(HourlyActivity.this.getResources().getDrawable(R.drawable.line_h));
		mListView.setDividerHeight(1);
		
	}
	
	
}
