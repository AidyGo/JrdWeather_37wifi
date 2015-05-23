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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/data/MyService.java           */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.bean.City;
import com.jrdcom.bean.Currentconditions;
import com.jrdcom.bean.Day;
import com.jrdcom.bean.Forecast;
import com.jrdcom.bean.Hour;
import com.jrdcom.bean.HourForShow;
import com.jrdcom.bean.Local;
import com.jrdcom.bean.Weather;
import com.jrdcom.bean.WeatherInfo;
import com.jrdcom.provider.DBHelper;
import com.jrdcom.util.CustomizeUtils;
import com.jrdcom.util.SharePreferenceUtils;
import com.jrdcom.weather.R;
import com.jrdcom.widget.UpdateWidgetTimeService;


public class MyService extends Service {
    private static final String TAG = "MyService";

    //private static final String URL_CITY_FIND = "http://tclandroidicsapp.accu-weather.com/widget/tclandroidicsapp/city-find.asp?";
    //private static final String URL_WEATHER_DATA = "http://tclandroidicsapp.accu-weather.com/widget/tclandroidicsapp/weather-data.asp?location=";
    // public static final String WIDGET_UPDATE_WEATHER_ACTION =
    // "com.jrdcom.action.WIDGET_UPDATE_WEATHER";
    private  static  final  String  TCLREGISTERKEY = "C704c66781c37b94ca24a7fcefb44303";
    private  static  final  String  URL_BAIDU_GEOCODER ="http://api.map.baidu.com/geocoder?";
    private  static  final  String  KEY_LAST_REFRESHTIME ="key_last_record_time";
    private  static  final  String  KEY_LAST_AUTOLOCATETIME ="key_last_autolocate_time";
    private static final String CITY_TOKYO_LOCATIONKEY = "226396";// add by jielong.xing at 2014-09-30
    
    private  static  final  long  HALFHOUR    =  1800000;   //update auto locate
    private  static  final  long  TWOHOUR    =   7200000;   //update all weather
    private  static  final  long  ONEHOUR    =   3600000;   //update autolocate weather
    private  static  final  long  LOCATIONCHANGETIME = 3000;   //wait the location change time

    private DBHelper helper;
    private List<City> mCityList;

    private String mSearchURL;
    private String mSearchURLNoId;//modify by shenxin for get different language for city name PR458505
    private String mWeatherURL;
    private String mAdvertisementURL;
    public boolean isDataUpdated = true;
    private UpdateThread mUpdateThread;
    private WeatherDataThread mWeatherDataThread;
    private MyBinder mMyBinder = new MyBinder();
    private LocationManager mLocationManager;

    private boolean isWifiConnected;

    private boolean isMobileConnected;

    private boolean  isGoogleServiceOpened = false;
    
    private boolean  isScreenOn = false;
    
//    private WifiManager   mWifiManager;

    /*PR 456206- Neo Skunkworks - Paul Xu added - 001 Begin*/
    public static final String POSTALCODE = "postalCode";
    public static final String CITYID = "cityId:";
    private City tempCity = null;
    /*PR 456206- Neo Skunkworks - Paul Xu added - 001 End*/
    
    // PR 466448 - Neo Skunkworks - Tom Yu - 001 begin
    private static final String LOCATION_KEY = "location_key";
    private static final String PREFERENCES_NAME = "autolocate_city";
    private SharedPreferences mPreferences;
    // PR 466448 - Neo Skunkworks - Tom Yu - 001 end
    
    private  boolean  mAutoUpdate = false;
    private  static   final   int   MSGUPDATEWIFISTATE = 0x10002;
    private  static   final   int   MSGREQUESTLOCATIONUPDATE = 0x1003;
    private static final int MSGREMOVELOCATIONUPDATE = 0x1004;
    
    private  boolean  mRequestUpdateSuccess = false;
    private  boolean  mLocationUpdated = false;
    /*PR 540454- Neo Skunkworks - Paul Xu added - 001 Begin*/
    private static final int LANGUAGE_ID_ZH_TW = 14;
    /*PR 540454- Neo Skunkworks - Paul Xu added - 001 End*/
    
    private boolean isOtherConnected = false; // add by jielong.xing at 2014-09-05
    
	//zhaoyun.wu begin
	private LocationListener mGpsListener = null;
	private LocationListener mNetworkListener = null;
	private Location mGpsLocation = null;
	private Location mNetworkLocation = null;
	
	//zhaoyun.wu end
	
//	private double mPositionData[][] = {{33.7988684,-79.3151525},{43.7697581,-79.3716889},{23.0376432, 114.3430633}, {32.6555757, -64.6091765}, {48.6461768, -79.6417216}}; 
	
	private final static int RETRYTIME = 60; // add by jielong.xing at 2014-09-25
	
	private boolean isFirstUse = true; // add by jielong.xing at 2014-09-30
	
	private boolean isNetworkProvideEnable = true;
	private boolean isGPSProvideEnable = true;
	
	private boolean isNetworkRequestOpen = false;
	private boolean isGPSRequestOpen = false;
    
    Handler  mHandler =  new  Handler()
    {
        public void handleMessage(Message msg) {
            switch (msg.what) 
            {            
            case MSGUPDATEWIFISTATE:
            {
                mHandler.removeMessages(MSGUPDATEWIFISTATE);
                updateConnectedFlags();
                Log.d(TAG, "MSGUPDATEWIFISTATE:isWifiConnected = " + isWifiConnected);
                if(isWifiConnected )
                 {
                	Log.d(TAG, "MSGUPDATEWIFISTATE:start updateWeather()");
                     mAutoUpdate = true;
                     updateWeather();
                 }
                break;
            }
            // CR 486491 - Neo Skunkworks - Wells Tang - 001 end
            //Add by Wells.Tang 2014-9-25,for requestLocationUpdate need to run on ui thread and must
            //wait the wifi scan so move if to handler
            case  MSGREQUESTLOCATIONUPDATE:
            {
            	mHandler.removeMessages(MSGREQUESTLOCATIONUPDATE);
            	requestLocationUpdate();
                break;
            }
            case MSGREMOVELOCATIONUPDATE:
            {
            	mHandler.removeMessages(MSGREMOVELOCATIONUPDATE);
            	removeLocationUpdate();
            	break;
            }
            //add end
            default:
                break;
            }
        }
    };
    
    /**
     * @author hongjun.tang
     * @return
     */
    public   void setUpdateManue()
    {
        /*if(mHandler!=null)
        {
            mHandler.removeMessages(MSGUPDATEAUTOLOCATE);
        }*/
        mAutoUpdate = false;
    }

    
    
    /**
     * only update the autolocate city
     * @author hongjun.tang
     * @return
     */
    public  boolean  updateAutoLocateCity(boolean  forceRefresh)
    {
        //Log.d(TAG,"updateAutoLocateCity");
        //zhaoyun.wu begin
		Location location = null;
//		mNetworkLocation= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		mGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (isBetterLocation(mGpsLocation, mNetworkLocation)) {
			location = mGpsLocation;
		} else {
			location = mNetworkLocation;
		}
		//zhaoyun.wu end
        
        boolean  ret = false;
        if(location != null)
        {
            try
            {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                
                City city = findCity(latitude,longitude);                
                City   oldCity = null;                
                if(city!=null )
                {
                     List<City> cityList = helper.getCitysFromDatabase(); 
                     
                     long   lastAutoLocateTime = -1;
                     int citySize = 0;
                     if (cityList != null) {
                    	 citySize = cityList.size();
                     }
                     
                     for (int i = 0; i < citySize; i ++) {
                    	 City var = cityList.get(i);
                    	 if (var.isAutoLocate()) {
                    		 oldCity = var;
                    		 lastAutoLocateTime = Long.parseLong(var.getUpdateTime());
                    		 break;
                    	 }
                     }
                     
                     long  currentTime  = System.currentTimeMillis(); 
                     
                     //if(oriName == null || !oriName.equals(city.getCityName()))
                     if(needReplaceCity(city,oldCity))
                     {
                     
                         if(oldCity!=null)
                         {
                              deleteCity(oldCity.getLocationKey());
                         }
                          
                          city.setAutoLocate(true);
                          helper.insertCity(city);
                         
                          ret =  insertCityBlock(city.getLocationKey());

                         // PR 517748 - Neo Skunkworks - Wells Tang - 001 begin
                         if(!ret)
                         {
                             deleteCity(city.getLocationKey());
                         }
                         // PR 517748 - Neo Skunkworks - Wells Tang - 001 end
                     }
                     else
                     {
                         if((lastAutoLocateTime!=-1 && currentTime - lastAutoLocateTime >= ONEHOUR)|| forceRefresh)
                         {
                             city.setAutoLocate(true);
                             if(oldCity!=null)
                              {
                                  city.setLocationKey(oldCity.getLocationKey());
                              }
                              helper.insertCity(city);
                              ret =  insertCityBlock(city.getLocationKey());
                         }
                         else
                         {
                             ret = true;
                         }
                     }
                     
                     if(ret)
                     {
                         SharePreferenceUtils.saveLong(MyService.this, KEY_LAST_AUTOLOCATETIME, System.currentTimeMillis());
                     }
                     
                    
                }
            }
            catch(Exception  e)
            {
                Log.e(TAG,"get the city info error");
                ret = false;
            }
        }
        else
        {
            Log.e(TAG,"updateAutoLocateCity location = null");
        }
        return  ret;
    }
    

    @Override
    public IBinder onBind(Intent arg0) {

        return mMyBinder;
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new DBHelper(this);
        mCityList = new ArrayList<City>();
        
        // PR 466448 - Neo Skunkworks - Tom Yu - 001 begin
        mPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        // PR 466448 - Neo Skunkworks - Tom Yu - 001 end
        
        // CR 486491 - Neo Skunkworks - Wells Tang - 001 begin
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        myIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiver, myIntentFilter);
        // CR 486491 - Neo Skunkworks - Wells Tang - 001 end
        
        // add by jielong.xing for pr796640 at 2014-09-30 begin
        SharedPreferences pref = getApplicationContext().getSharedPreferences("isFirstUse", Context.MODE_PRIVATE);
		isFirstUse = pref.getBoolean("isFirstUse", true);
		boolean isForceSetTokyo = isForceSetTokyo();
		if (isFirstUse && isForceSetTokyo) {
	        myIntentFilter = new IntentFilter();
	        myIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
	        registerReceiver(mNetworkBroadcastReceiver, myIntentFilter);
		} else {
			isFirstUse = false;
		}
        // add by jielong.xing for pr796640 at 2014-09-30 end

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //zhaoyun.wu begin
		mGpsListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {

				Log.e(TAG, "GPS____onLocationChanged Latitude = "
								+ location.getLatitude() + "Longitude = "
								+ location.getLongitude());
				if (location != null) {
					mGpsLocation = location;
					mLocationUpdated = true;
				}

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		};

		mNetworkListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {

				Log.e(TAG, "Network____onLocationChanged Latitude = "
								+ location.getLatitude() + "Longitude = "
								+ location.getLongitude());
				if (location != null) {
					mNetworkLocation = location;
					mLocationUpdated = true;
				}

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
				// isGpsOpen = false;
			}
		};
		//zhaoyun.wu end       
    }
    
    // CR 486491 - Neo Skunkworks - Wells Tang - 001 begin
    private   BroadcastReceiver   mBroadcastReceiver =  new  BroadcastReceiver()
    {
         @Override
         public void onReceive(Context context, Intent intent) 
         {
             String action = intent.getAction();
             if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action))
             {
                 mHandler.sendEmptyMessageDelayed(MSGUPDATEWIFISTATE,3000);
             }
            // PR 512016 - Neo Skunkworks - Wells Tang - 001 begin
             else if(Intent.ACTION_SCREEN_OFF.equals(action))
             {
                 Log.d("jielong","screen off");
                 mHandler.sendEmptyMessage(MSGREMOVELOCATIONUPDATE);
                 
             }
             // PR 512016 - Neo Skunkworks - Wells Tang - 001 end
         }
    };

    @Override
    public void onDestroy() {
        // CR 486491 - Neo Skunkworks - Wells Tang - 001 begin
        unregisterReceiver(mBroadcastReceiver);
        // CR 486491 - Neo Skunkworks - Wells Tang - 001 end
        
        if (isFirstUse) {
        	unregisterReceiver(mNetworkBroadcastReceiver);
        }
        
        helper.close();

        // PR 508150 - Neo Skunkworks - Wells Tang - 001 begin
        /*try
        {
            if(mLocationManager!=null)
            {
              if(mRequestUpdateSuccess)
              {
                  mLocationManager.removeUpdates(this);
              }
              mLocationManager = null;
            }
        }
        catch(Exception e)
        {
        }*/
        mHandler.sendEmptyMessage(MSGREMOVELOCATIONUPDATE);
        // PR 508150 - Neo Skunkworks - Wells Tang - 001 end
      
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);

    }

     /*PR 515884- Neo Skunkworks - Wells Tang  - 001 Begin*/
    public   boolean   requestLocationUpdate()
    {
        boolean  noError = true;
        updateConnectedFlags();
        Log.d(TAG, "requestLocationUpdate: mRequestUpdateSuccess=" + mRequestUpdateSuccess + ", isScreenOn = " + isScreenOn + ", mLocationManager!=null?" + (mLocationManager!=null) + ", isGoogleServiceOpened = " + isGoogleServiceOpened);
        if(!mRequestUpdateSuccess && isScreenOn &&  mLocationManager!=null && isGoogleServiceOpened)
        {
            Log.e(TAG,"jielong_requestLocationUpdate");
            try
            {
                //zhaoyun.wu begin
            	if (isNetworkProvideEnable) {
            		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, mNetworkListener);
            		isNetworkRequestOpen = true;
            	}
            	if (isGPSProvideEnable) {
            		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, mGpsListener);
            		isGPSRequestOpen = true;
            	}
        		
			  	//zhaoyun.wu end
                mRequestUpdateSuccess = true;
                mLocationUpdated = false;
                //Log.d(TAG,"requestLocationUpdates success");
                Log.d("jielong","requestLocationUpdate:isNetworkRequestOpen = " + isNetworkRequestOpen + ", isGPSRequestOpen = " + isGPSRequestOpen);
            }
            catch(Exception e)
            { 
                noError = false;
                Log.e(TAG,"requestLocationUpdates failed");
            }
        }
        return noError;
    }
    
    public  void  removeLocationUpdate()
    {
        if(mRequestUpdateSuccess)
         {
            Log.e(TAG,"jielong_removeLocationUpdate");
            Log.d("jielong","removeLocationUpdate start:isNetworkRequestOpen = " + isNetworkRequestOpen + ", isGPSRequestOpen = " + isGPSRequestOpen);
             try
            {
                //zhaoyun.wu begin
            	// mLocationManager.removeUpdates(MyService.this);
            	 if (isNetworkRequestOpen) {
 					if (mNetworkListener != null) {
 						Log.e("jielong","remove mNetworkListener");
 						mLocationManager.removeUpdates(mNetworkListener);
 					}
 					isNetworkRequestOpen = false;
 				}
 				if (isGPSRequestOpen) {
 					if (mGpsListener != null) {
 						Log.e("jielong","remove mGpsListener");
 						mLocationManager.removeUpdates(mGpsListener);
 					}
 					isGPSRequestOpen = false;
 				}
				//zhaoyun.wu end
                mRequestUpdateSuccess = false;
                mLocationUpdated = false;
                Log.d("jielong","removeLocationUpdate end:isNetworkRequestOpen = " + isNetworkRequestOpen + ", isGPSRequestOpen = " + isGPSRequestOpen);
            }
            catch(Exception e)
            {
                Log.e(TAG,"removeLocationUpdate failed");
            }
         }
    }
     /*PR 515884- Neo Skunkworks - Wells Tang  - 001 End*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        boolean   boot_complete = false;
//        if(intent!=null)
//        {
//            boot_complete = intent.getBooleanExtra("boot_complete",false);
//        }
//        
//        if(!boot_complete)
//        {
            checkUpdate();
//        }
        /*else
        {
            updateConnectedFlags();
            if ((isWifiConnected || isMobileConnected || isOtherConnected))
            {
                mAutoUpdate = true;
                updateWeather();                
            }
        }*/
        //modified by wells,restart the service when it was killed
        super.onStartCommand(intent, flags, startId);
        return  START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
        //modify end;
    }

    // feng.zhuang add for check network.
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

        //zhaoyun.wu begin
		/*isGoogleServiceOpened =  Settings.Secure.isLocationProviderEnabled(getContentResolver(),
				LocationManager.NETWORK_PROVIDER);*/
		isGoogleServiceOpened = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
				Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF);
		
		PackageManager pm = getApplicationContext().getPackageManager();
		isNetworkProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) 
				&& pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK));
		isGPSProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) 
				&& pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS));
		//zhaoyun.wu end
        
        // PR 512016 - Neo Skunkworks - Wells Tang - 001 begin
        PowerManager  pw = (PowerManager)getSystemService(Context.POWER_SERVICE);
        isScreenOn =  pw.isScreenOn();
        // PR 512016 - Neo Skunkworks - Wells Tang - 001 end
        
        /*
        Log.d(TAG,"updateConnectedFlags "+"\n google provider is "+isGoogleServiceOpened+
            "\n screen on ="+isScreenOn+"\n wifi connect ="+isWifiConnected+
            "\n mobile connect ="+isMobileConnected);
        */
    }

    // private static final int REFRESH_KEY = 0;
    // private static final int CONNECT_FAILD = 3;

    // Send the request of city-find.
    public void sendCityFindRequest(String cityName) {
/*
        int languageId = getLanguageId();

        try {
            mSearchURL = new StringBuilder(URL_CITY_FIND)
                    .append("location=" + URLEncoder.encode(cityName, "utf-8")
                            + "&langid=" + languageId).toString().trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        CityFindThread thread = new CityFindThread();
        thread.cityName = cityName;
        thread.start();
    }

    // Parse the city information from AccuWeather.
    private class CityFindThread extends Thread {
    	public String cityName = null;
        @Override
        public void run() {
            try {
            	List<City> tempCityList = CityFindRequest.findCityByName(URLEncoder.encode(cityName, "utf-8"), getLanguage(), true);
                /*InputStream mInputStream = downloadUrl(mSearchURL).getInputStream();
                List<City> tempCityList = CityFindParser.parse(mInputStream);
                mInputStream.close();*/

                if (tempCityList.size() == 0) {
                    Intent i = new Intent("android.intent.action.CITY_BROADCAST");
                    i.putExtra("city", false);
                    sendBroadcast(i);
                } else {
                    mCityList = tempCityList;

                    Intent i = new Intent("android.intent.action.CITY_BROADCAST");
                    i.putExtra("city", true);
                    sendBroadcast(i);
                }
            } catch (Exception e) {
                Log.e(TAG, "CityFindThread:" + e.getMessage());
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
        }
    }

    // Auto locate,get the city by location,insert the city to database,and show
    // the weather information directly
    public void autoLocate(double latitude, double longitude) {
       /* String location = "latitude=" + latitude + "&longitude=" + longitude;
        int languageId = getLanguageId();
        mSearchURL = (URL_CITY_FIND + location + "&langid=" + languageId).trim();//modify by shenxin for get different language for city name PR458505
        mSearchURLNoId = (URL_CITY_FIND + location).trim();*/
        CityFindGPSThread thread = new CityFindGPSThread();
        
        thread.mLatitude = latitude;
        thread.mLongitude = longitude;
        thread.start(); 
    }

    /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 begin*/
    private  boolean  isAutoLocateInChinaArea()
    {
        //return   getResources().getBoolean(R.bool.def_weather_use_baidumap);
        return   CustomizeUtils.getBoolean(MyService.this,"def_weather_use_baidumap");
    }
    /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 end*/
    
    /* PR 529191  - Neo Skunkworks -  Wells Tang - 001 begin*/
    private  boolean  isUseProvinceNameAsLocationName()
    {
        //return   getResources().getBoolean(R.bool.def_weather_use_province_as_locationname);
        return  CustomizeUtils.getBoolean(MyService.this,"def_weather_use_province_as_locationname");
    }
    /* PR 529191  - Neo Skunkworks -  Wells Tang - 001 end*/

    /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 begin*/
    private   String  getDistributeName(double  latitude,double longitude)
    {
         
         String distribute = null;
         String baidulocation = "location="+latitude+","+longitude;
         String outputformat ="output=json";
         String tclkey ="key="+TCLREGISTERKEY;
         String baiduUrl = URL_BAIDU_GEOCODER+baidulocation+"&"+outputformat+"&"+tclkey;
         //Log.d(TAG,"request url="+baiduUrl);
         
         try
         {
             BasicHttpParams httpParameters = new BasicHttpParams();
             
             HttpConnectionParams.setConnectionTimeout(httpParameters,20000);
             HttpConnectionParams.setSoTimeout(httpParameters, 20000);
             HttpClient client = new DefaultHttpClient(httpParameters);    
             HttpClient httpClient = new DefaultHttpClient(httpParameters);
             
             HttpGet httpRequest = new HttpGet(baiduUrl);   
             HttpResponse response=httpClient.execute(httpRequest);
             int ret =response.getStatusLine().getStatusCode();
             if (ret == 200) {
                 String  strEntity = EntityUtils.toString(response.getEntity());
                // Log.d(TAG,"strEntity="+strEntity);
                 JSONObject  json = new  JSONObject(strEntity);
                 if(json!=null)
                 {
                     JSONObject  resultJson = json.getJSONObject("result");
                     
                     if(resultJson == null)
                     {
                         Log.e(TAG,"result json=null");
                         return null;
                     }
                     JSONObject  conponentJson = resultJson.getJSONObject("addressComponent");
                     if(conponentJson!=null)
                     {
                         distribute = conponentJson.getString("district");
                         //Log.d(TAG,"distribute="+distribute);
                     }
                     else
                     {
                         Log.e(TAG,"conponentJson==null");
                         return null;
                     }
                 }
              }else{
                //  Log.d(TAG,"getDistributeName http get return "+ret);
                  return null;
              }
         }
         catch(Exception e)
         {
             Log.e(TAG,e.toString());
         }

         //Log.d(TAG,"getDistributeName latitude="+latitude+" longitude="+longitude+" distribute="+distribute);
         
         return distribute;
        
    }
    /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 end*/
    
    /**
     * @author hongjun.tang
     * @param latitude
     * @param longitude
     * @return
     */
    private  City  findCity(double  latitude,double longitude)
    {     
         /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 begin*/
         String   distributeName = null;
         /*PR 540454- Neo Skunkworks - Paul Xu modified - 001 Begin*/
         /*
         if(isAutoLocateInChinaArea())
         {
         */
         String lang = getLanguage();
         if(isAutoLocateInChinaArea() || "zh-tw".equals(lang)){
         /*PR 540454- Neo Skunkworks - Paul Xu modified - 001 End*/
             distributeName = getDistributeName(latitude,longitude);
             
             if(distributeName == null)
              {
                  return null;
              }
         }
         /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 end*/
         
                  
        /* String location = "latitude=" + latitude + "&longitude=" + longitude;
         int languageId = getLanguageId();
         mSearchURL = (URL_CITY_FIND + location + "&langid=" + languageId).trim();//modify by shenxin for get different language for city name PR458505
         mSearchURLNoId = (URL_CITY_FIND + location).trim();*/

        try {
            /*InputStream mInputStream = downloadUrl(mSearchURL).getInputStream();
            List<City> citys = CityFindParser.parse(mInputStream);
            mInputStream.close();*/
            String location = latitude + "," + longitude;
   		 	List<City> citys = CityFindRequest.findCityByGeoLocation(location, lang, true);

            if (citys.size() != 0) {
                City city = citys.get(0);
                if (TextUtils.isEmpty(city.getCityName())) {
                    /*InputStream mNoIdInputStream = downloadUrl(mSearchURLNoId).getInputStream();
                    List<City> mCitys = CityFindParser.parse(mNoIdInputStream);
                    
                    mNoIdInputStream.close();*/
                	List<City> mCitys = CityFindRequest.findCityByGeoLocation(location, lang, false);
                    if (mCitys.size() != 0) {
                        city = mCitys.get(0);
                    }
                }

                /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 begin*/
                if(city!=null && distributeName!=null)
                {
                    //PR 577072 - Neo Skunkworks -  Wells Tang - 001 begin
                    if(!"".equals(distributeName.trim()))
                    {
                        city.setCityName(distributeName);
                    }
                    //PR 577072 - Neo Skunkworks -  Wells Tang - 001 end
                }
                /* PR 503563 - Neo Skunkworks -  Wells Tang - 001 end*/
                
                /* PR 529191  - Neo Skunkworks -  Wells Tang - 001 begin*/
                if(city!=null && isUseProvinceNameAsLocationName())
                {
                    city.setCityName(city.getState());
                }
                /* PR 529191  - Neo Skunkworks -  Wells Tang - 001 end*/
                return city;
            }

        } catch (Exception e) {
            Log.e(TAG, "CityFindGPSThread" + e.getMessage());
        }
        return null;
    }
    

    class CityFindGPSThread extends Thread {
        public double  mLatitude = 0;
        public double mLongitude = 0;
        
        public void run() {
            try {
            	String lang = getLanguage();
                String  distributeName = null;
                if(isAutoLocateInChinaArea() || "zh-tw".equals(lang)){
                    distributeName  =  getDistributeName(mLatitude,mLongitude);
                    if(distributeName == null)
                    {
                         Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                         it.putExtra("connect_timeout", true);
                         sendBroadcast(it);
                         return;
                    }
                }
                
                String location = mLatitude + "," + mLongitude;
    			List<City> citys = CityFindRequest.findCityByGeoLocation(location, lang, true);
                /*InputStream mInputStream = downloadUrl(mSearchURL).getInputStream();
                List<City> citys = CityFindParser.parse(mInputStream);
                mInputStream.close();*/

                // Always just one city can be return
                if (citys.size() != 0) {
                    City city = citys.get(0);
                    if (TextUtils.isEmpty(city.getCityName())) {
                        /*InputStream mNoIdInputStream = downloadUrl(mSearchURLNoId).getInputStream();
                        List<City> mCitys = CityFindParser.parse(mNoIdInputStream);
                        mNoIdInputStream.close();*/
                    	List<City> mCitys = CityFindRequest.findCityByGeoLocation(location, lang, false);
                        if (mCitys.size() != 0) {
                            city = mCitys.get(0);
                        }
                    }
                    
                    if(distributeName!=null)
                    {
                        if(!"".equals(distributeName.trim()))
                        {
                            city.setCityName(distributeName);
                        }
                    }
                    
                    if(isUseProvinceNameAsLocationName())
                    {
                        city.setCityName(city.getState());
                    }
                    List<City> cityList = helper.getCitysFromDatabase(); 
                    
                    String  oriName = null;
                    String  oriLocationKey = null;
                    int citySize = 0;
                    if (cityList != null) {
                    	citySize = cityList.size();
                    }
                    for (int i = 0; i < citySize; i ++) {
                    	City var = cityList.get(i);
                    	if (var.isAutoLocate()) {
                    		oriName = var.getCityName();
                    		oriLocationKey = var.getLocationKey();
                    		break;
                    	}
                    }
                    
                    if(oriName != null && oriName.equals(city.getCityName()))
                    {
                        city.setLocationKey(oriLocationKey);
                    }
                    else if(oriName!=null)
                    {
                        if(oriLocationKey!=null)
                        {
                            deleteCity(oriLocationKey);
                        }
                    }
                    
                    SharePreferenceUtils.saveLong(MyService.this, KEY_LAST_AUTOLOCATETIME, System.currentTimeMillis());
                    
                    insertCity(city,true);
                    Editor editor = mPreferences.edit();
                        editor.putString(LOCATION_KEY, city.getLocationKey());
                        editor.commit();
                    
                    SharePreferenceUtils.checkCommonCity(MyService.this, city.getLocationKey());
                    //CR 447398 - ting.chen@tct-nj.com - 001 added end
                }

            } catch (Exception e) {
                Log.e(TAG, "CityFindGPSThread" + e.getMessage());
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
                e.printStackTrace();
            }

        }        
    }

    // Send the weather data request
    public void sendWeatherDataRequest(String locationKey) {
//        mWeatherURL = new StringBuilder(URL_WEATHER_DATA).append(locationKey).toString().trim();

        mWeatherDataThread = new WeatherDataThread();
        
        // PR 456296 - Neo Skunkworks - Wells Tang - 001 begin
        if(locationKey.startsWith(POSTALCODE))
        {
            mWeatherDataThread.isCityId = false;
        }
        else
        {
            mWeatherDataThread.isCityId = true;
        }
        // PR 456296 - Neo Skunkworks - Wells Tang - 001 begin
        mWeatherDataThread.locationKey = locationKey;
        mWeatherDataThread.start();
    }
    
     // PR 456296 - Neo Skunkworks - Wells Tang - 001 begin
    public  boolean  insertCityBlock(String locationkey)
    {
        return insertCityBlock(locationkey,false);
    }
    // PR 456296 - Neo Skunkworks - Wells Tang - 001 end
    
    /**
     * @author hongjun.tang
     * @param locationkey
     */
    public  boolean  insertCityBlock(String locationkey,boolean  manu)
    {
         boolean  result = false;
//         mWeatherURL = new StringBuilder(URL_WEATHER_DATA).append(locationkey).toString().trim();
          try {
        	  String lang = getLanguage();
          	  Weather weather = getWeather(locationkey, lang);
          	  if (weather == null) {
          		  return false;
          	  }
              /*InputStream mInputStream = downloadUrl(mWeatherURL).getInputStream();
              Weather weather = new WeatherDataParser().parse(mInputStream);
              mInputStream.close();*/

              insertWeatherIntoDB(weather,manu);
              result = true;

          } catch (SocketException e) {
              Log.e(TAG, "WeatherDataThread SocketException");
              if(!mAutoUpdate)
              {
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
              }
          } catch (Exception e) {
              Log.e(TAG, "WeatherDataThread Exception");
              if(!mAutoUpdate)
              {
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
              }
          }
          
          return result;
    }
    

    // Parse the xml returned from weather-data, and insert it into database.
    private class WeatherDataThread extends Thread {
        public boolean isCityId = true;
        public String locationKey = "";
        
        @Override
        public void run() {
            try {

                /*InputStream mInputStream = downloadUrl(mWeatherURL).getInputStream();
                
                //reduce one time http post
                Weather weather = new WeatherDataParser().parse(mInputStream);
                mInputStream.close();*/
                String lang = getLanguage();
            	Weather weather = getWeather(locationKey, lang);
            	
            	if (weather == null) {
            		deleteCity(locationKey);
            		Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
            		return;
            	}
                if(!isCityId)
                {
                	//parse the location key from weather
                    tempCity.setLocationKey(weather.getLocal().getCityId());
                    helper.insertCity(tempCity);
                    tempCity = null;
                }
                insertWeatherIntoDB(weather,true);             

            } catch (SocketException e) {
                Log.e(TAG, "WeatherDataThread SocketException");
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            } catch (Exception e) {
                Log.e(TAG, "WeatherDataThread Exception");
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
        }
    }

    public void autoUpdate() {
        mUpdateThread = new UpdateThread();
        mUpdateThread.start();
    }

    public boolean isAutoUpdating() {
        boolean flagUpdate = false;
        boolean flagWeatherUpdate = false;
        if (mUpdateThread != null)
        {
            flagUpdate = !mUpdateThread.isAlive();
        }
        if (mWeatherDataThread != null)
        {
            flagWeatherUpdate = !mWeatherDataThread.isAlive();
        }
        return flagUpdate || flagWeatherUpdate;
    }

    // Check the citys if need be updated,per 1min.
    private class UpdateThread extends Thread {
        @Override
        public void run() {
            try {
                checkUpdate();
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Log.e(TAG, "UpdateThread InterruptedException");
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            } catch (Exception e) {
                Log.e(TAG, "UpdateThread Exception");
                Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                it.putExtra("connect_timeout", true);
                sendBroadcast(it);
            }
        }
    }
/* PR 470013 - Neo Skunkworks - James Jiang - 001 begin*/
    private int mLastHour = 0;
/* PR 470013 - Neo Skunkworks - James Jiang - 001 end*/
    public void checkUpdate() {
        boolean flag = false;
        updateConnectedFlags();
        Log.e(TAG, "checkUpdate::::isWifiConnected = " + isWifiConnected + ", isMobileConnected = " + isMobileConnected + ", isOtherConnected = " + isOtherConnected + ", isScreenOn = " + isScreenOn + ", isGoogleServiceOpened = " + isGoogleServiceOpened);
        if ((isWifiConnected || isMobileConnected || isOtherConnected) && isScreenOn)
        {
            long time = System.currentTimeMillis();
            
            List<City> citys = this.helper.getCitysFromDatabase();
            if (citys != null && citys.size() != 0) {
                long   lastRefreshTime = SharePreferenceUtils.getLong(MyService.this, KEY_LAST_REFRESHTIME, -1);
                if(lastRefreshTime == -1 || time - lastRefreshTime >TWOHOUR)
                {
                	// if location update is ongoing, don't duplicate do the update action. 
                	if (mRequestUpdateSuccess) {
                		return;
                	}
                    flag = true;    
                    mAutoUpdate =true;
                    updateWeather();
                }
                else if(isGoogleServiceOpened)
                {
                    long  autoLocateTime  = -1;
                    autoLocateTime = SharePreferenceUtils.getLong(MyService.this, KEY_LAST_AUTOLOCATETIME, -1);

                    if(autoLocateTime == -1 || time-autoLocateTime>HALFHOUR)
                    {                        
                    	Log.e(TAG, "checkUpdate::::time more than halfhour, mRequestUpdateSuccess == " + mRequestUpdateSuccess);
                    	// if location update is ongoing, don't duplicate do the update action. 
                    	if (mRequestUpdateSuccess) {
                    		return;
                    	}
                            new  Thread()
                            {
                                public void run()
                                {
                                	/*boolean  scanWifi = startScanWifi();
                                    //sleep 5 seconds to wait for the wifi scan result
                    				if(scanWifi)
                    				{
                    					try
                    					{
                    						Thread.sleep(5000);
                    					}
                    					catch(Exception e)
                    					{
                    					}
                    				}*/
                    				mHandler.sendEmptyMessage(MSGREQUESTLOCATIONUPDATE);
                                    //wait for requestlocationupdate run finished
                    				try
                					{
                						Thread.sleep(100);
                					}
                    				catch(Exception e)
                    				{
                    				}
                    				
                    				if(!mRequestUpdateSuccess)
                    				{
                    					return;
                    				}
                                    //modify end 
                                    // PR 512016 - Neo Skunkworks - Wells Tang - 001 begin
                                    boolean   updateSuccess = false;
                                    for(int i =0;i<RETRYTIME;i++)
                                    {
                                        if(!mLocationUpdated)
                                        {
                                            try
                                            {
                                                sleep(LOCATIONCHANGETIME);
                                            }
                                            catch(Exception e)
                                            {
                                            }
                                        }
                                        else
                                        {
                                            updateSuccess = true;
                                            break;
                                        }
                                    }
                                    mHandler.sendEmptyMessage(MSGREMOVELOCATIONUPDATE);

                                    if(!updateSuccess)
                                    {
                                        Log.e(TAG,"checkUpdate::::wait 180 seconds,but the location has not updated");
                                        
                                        return;
                                    }
                                    Log.e(TAG,"checkUpdate::::location has updated!");
                                    // PR 512016 - Neo Skunkworks - Wells Tang - 001 end
                                    mAutoUpdate = true;
                                    updateAutoLocateCity(false);
                                }
                            }.start();
                        }
                         /*PR 515884- Neo Skunkworks - Wells Tang  - 001 End*/
                        
                       
                }
            }
            else
            {
            	if (mRequestUpdateSuccess) {
            		return;
            	}
                 mAutoUpdate = true;
                 updateWeather();          
                 flag = true;                   
            }
        }
       /* PR 470013 - Neo Skunkworks - James Jiang - 001 begin*/
       if(!flag){
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                if(mLastHour != hour){
                    //update app  but do not update data
                      updateView();
                      mLastHour = hour;
                   }
        }
       /* PR 470013 - Neo Skunkworks - James Jiang - 001 end*/
    }

    // Get the InputStream from internet.
    private HttpURLConnection downloadUrl(String urlString) throws IOException {

        URL url = new URL(urlString);
       // Log.d(TAG, "url = " + url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException");
            if(!mAutoUpdate)
            {
                  Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                  it.putExtra("connect_timeout", true);
                  sendBroadcast(it);
            }

            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.e(TAG, "UnknownHostException");
            if(!mAutoUpdate)
            {
                  Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                  it.putExtra("connect_timeout", true);
                  sendBroadcast(it);
            }

            e.printStackTrace();
        } catch (SocketException e) {
            Log.e(TAG, "SocketException");
            if(!mAutoUpdate)
            {
                  Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                  it.putExtra("connect_timeout", true);
                  sendBroadcast(it);
            }

            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "downloadUrl Exception");
            if(!mAutoUpdate)
            {
                  Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                  it.putExtra("connect_timeout", true);
                  sendBroadcast(it);
            }
        }
        return conn;
    }
    /* PR 470013 - Neo Skunkworks - James Jiang - 001 begin*/
    /**
     * update ViewPager when last Hour do not equal with current Hour
     */
    private void updateView(){
        UpdateViewThread thread = new UpdateViewThread();
        thread.start();
     }
    
    private class UpdateViewThread extends Thread{
        @Override
        public void run() {
         mCityList = helper.getCitysFromDatabase();
         if (mCityList.size() > 0 ) {  
          Intent it = new Intent(
                        "android.intent.action.WEATHER_BROADCAST");
          it.putExtra("weather", true);
          sendBroadcast(it);
           }
         }
     } 
    /* PR 470013 - Neo Skunkworks - James Jiang - 001 begin*/
    public void updateWeather() {
        mCityList = helper.getCitysFromDatabase();

        WeatherUpdateThread thread = new WeatherUpdateThread();
        thread.start();
    }
    
    /**
     * scan the wifi mac for autolocate provider
     * @return
     */
    /*private  boolean   startScanWifi()
    {
    	//Log.d(TAG,"startScanWifi");
    	boolean  ret = false;
    	try {
			int   wifiScanAways  =Settings.Global.getInt(getContentResolver(), "wifi_scan_always_enabled", 0); 
			
			if( wifiScanAways ==1)
			{
				if(mWifiManager == null)
				{
					mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				}
				
				if(mWifiManager!=null)
				{
					mWifiManager.startScan();
					ret = true;
				}
			}
			
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"settings not found");
			e.printStackTrace();
		}
    	
    	return ret;
    }*/

    private class WeatherUpdateThread extends Thread {
        @Override
        public void run() {
            try {
                
                // PR 466448 - Neo Skunkworks - Tom Yu - 001 begin
                List<City> cityList = helper.getCitysFromDatabase(); 
                // PR 466448 - Neo Skunkworks - Tom Yu - 001 end
                boolean  autolocateSuccess = false;
                
                double latitude = -1;
                double longitude = -1;

                updateConnectedFlags();
                boolean   updateSuccess = false;
                //Modified by Wells 2014-9-25,scan the wifi mac before requestlocation update
                Log.d(TAG, "WeatherUpdateThread:::isWifiConnected = " + isWifiConnected + ", isMobileConnected = " + isMobileConnected + ", isOtherConnected = " + isOtherConnected + ", isScreenOn = " + isScreenOn + ", isGoogleServiceOpened = " + isGoogleServiceOpened);
            	if(isGoogleServiceOpened )
            	{
            		/*boolean  scanWifi = startScanWifi();
    				if(scanWifi)
    				{
    					try
    					{
    						Thread.sleep(5000);
    					}
    					catch(Exception e)
    					{
    					}
    				}*/
    				mHandler.sendEmptyMessage(MSGREQUESTLOCATIONUPDATE);
    				try
					{
						Thread.sleep(100);
					}
    				catch(Exception e)
    				{
    					
    				}
    				
    				for(int i =0;i<RETRYTIME;i++)
                    {
                        if(!mLocationUpdated)
                        {
                            try
                            {
                                sleep(LOCATIONCHANGETIME);
                            }
                            catch(Exception e)
                            {
                            }
                        }
                        else
                        {
                            updateSuccess = true;
                            break;
                        }
                    }
                    

                    //remove the location update immediately
    				mHandler.sendEmptyMessage(MSGREMOVELOCATIONUPDATE);                
                    Log.e(TAG,"WeatherUpdateThread:::updateSuccess = " + updateSuccess);
                    if(!updateSuccess)
                    {
                        Log.e(TAG,"WeatherUpdateThread:::wait 180 seconds,but the location has not updated");
                        if(!mAutoUpdate)
                        {
                              Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                              it.putExtra("connect_timeout", true);
                              sendBroadcast(it);
                        }
                    }
                    else
                    {
                    	Log.e(TAG, "WeatherUpdateThread::location has updated");
                        //zhaoyun.wu begin
                    	Location location = getBetterLocation();
                    	//zhaoyun.wu end
                        
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.e(TAG, "WeatherUpdateThread:::latitude = " + latitude + ", longitude = " + longitude);
                    }               
            	}
                //modify end
                
                boolean   updateWeatherOnly = !isGoogleServiceOpened;
                Log.e(TAG, "WeatherUpdateThread:::mAutoUpdate = " + mAutoUpdate + ", updateWeatherOnly = " + updateWeatherOnly);
                if(!mAutoUpdate && !updateSuccess)
                {
                    updateWeatherOnly = true;
                }
                
                City autolocateCity = null;
                int citySize = 0;
                if (cityList != null) {
                	citySize = cityList.size();
                }
                
                for (int i = 0; i < citySize; i ++) {
                	City var = cityList.get(i);
                	boolean isAutoLocate = var.isAutoLocate();
                	String locationKey = var.getLocationKey();
                	if (updateWeatherOnly || !isAutoLocate) {
                		/*mWeatherURL = new StringBuilder(URL_WEATHER_DATA).append(locationKey).toString().trim();
                		InputStream mInputStream = downloadUrl(mWeatherURL).getInputStream();
                        Weather weather = new WeatherDataParser().parse(mInputStream);
                        mInputStream.close();*/
                		String lang = getLanguage();
                    	Weather weather = getWeather(locationKey, lang);
                    	if (weather == null) {
                    		continue;
                    	}
                        helper.updateWeather(weather);
                	} else if (isAutoLocate) {
                		autolocateCity = var;
                	}
                }
                
                // CR 720447 - Neo Skunkworks - Wells Tang - 001 begin
                //if  get latitude and longitude success,update autolocate city
                if(updateSuccess)
                {
                	/*if (true) {
                		int len = mPositionData.length;
                		java.util.Random rand = new java.util.Random();
                		int r = rand.nextInt(len);
                		double position[] = mPositionData[r];
                		latitude = position[0];
                		longitude = position[1];
                		Log.e("xjl", "latitude = " + latitude + ", longitude = " + longitude);
                	}*/
                    //Log.d(TAG,"update autolocate city");
                    City city = findCity(latitude,longitude);
                    
                    if(city!=null)
                    {
                        city.setAutoLocate(true);
                        
                        if(autolocateCity!=null)
                        {
                            //new autolocate city replace the old autolocte city
                            if(needReplaceCity(city,autolocateCity))
                            {
                                //Log.d(TAG,"replace city");
                                deleteCity(autolocateCity.getLocationKey());
                                city.setAutoLocate(true);
                                helper.insertCity(city);
                                
                                boolean ret =  insertCityBlock(city.getLocationKey());
                                if(ret)
                                {
                                     autolocateSuccess = true;
                                }
                                else
                                {
                                    deleteCity(city.getLocationKey());
                                }
                            }
                            //new autolocate city and old autolocate city is in the same region,use the same locationkey
                            else
                            {
                                //Log.d(TAG,"update autolocate  aaa");
                                city.setLocationKey(autolocateCity.getLocationKey());
                                helper.insertCity(city);
                                boolean ret = insertCityBlock(city.getLocationKey());
                                if(ret)
                                {
                                    autolocateSuccess = true;
                                }
                            }
                        }
                        //there is no autolocation city before
                        else
                        {
                            //Log.d(TAG,"insert new city");
                            helper.insertCity(city);
                            
                            boolean  ret = insertCityBlock(city.getLocationKey());
                            if(ret)
                            {
                                autolocateSuccess = true;
                                if(cityList!=null)
                                  {
                                   cityList.add(city);
                                  }
                            }
                            else
                            {
                                deleteCity(city.getLocationKey());
                            }
                        }
                    }
                }
                // CR 720447 - Neo Skunkworks - Wells Tang - 001 End
                Log.e(TAG, "WeatherUpdateThread:::autolocateSuccess = " + autolocateSuccess);
                if(autolocateSuccess)
                {
                     SharePreferenceUtils.saveLong(MyService.this, KEY_LAST_AUTOLOCATETIME, System.currentTimeMillis());
                }
               

                if (cityList!=null && cityList.size() > 0 ) {  // PR 466448 - Neo Skunkworks - Tom Yu - 001
                    
                     // PR 496643 - Neo Skunkworks - Wells Tang - 001 begin
                    if(autolocateSuccess || updateWeatherOnly)
                    {
                         helper.updateCityTime();
                    }
                    else
                    {
                        helper.updateNotAutoLocateCityTime();
                    }
                     // PR 496643 - Neo Skunkworks - Wells Tang - 001 end
                   
                    Intent it = new Intent(
                            "android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("weather", true);
                    // CR 552491 - Neo Skunkworks - Wells Tang - 001 begin
                    if(isGoogleServiceOpened && updateWeatherOnly)
                    {
                        it.putExtra("locationerror",true);
                    }
                    // CR 552491 - Neo Skunkworks - Wells Tang - 001 end
                    sendBroadcast(it);
                }
                
                SharePreferenceUtils.saveLong(MyService.this, KEY_LAST_REFRESHTIME, System.currentTimeMillis());

                
                /*if(mAutoUpdate && !updateWeatherOnly && (isWifiConnected|| isMobileConnected || isOtherConnected))
                {
                      if(!autolocateSuccess)
                      {
                          mCurrentTime = 0;
                            mHandler.sendEmptyMessageDelayed(MSGUPDATEAUTOLOCATE,RETRYINTERVAL);
                      }
                }*/
               
            } catch (SocketException e) {
                Log.e(TAG, "WeatherUpdateThread SocketException");
                
                if(!mAutoUpdate)
                {
                      Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                      it.putExtra("connect_timeout", true);
                      sendBroadcast(it);
                }
            } catch (Exception e) {
                Log.e(TAG, "WeatherUpdateThread Exception");
                
                if(!mAutoUpdate)
                {
                      Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                      it.putExtra("connect_timeout", true);
                      sendBroadcast(it);
                }
            }
        }
    }
    
  //if the autolocate city needs to update,return true,else
    //return false
    private  boolean  needReplaceCity(City  newCity,City  oldCity)
    {
        if(newCity== null || newCity.getCityName() == null)
        {
            return false;
        }
        
        if(oldCity == null)
        {
            return true;
        }
        
        String  newCityName = newCity.getCityName();
        String  newStateName = newCity.getState();        
        
        boolean  ret = false;
        
        if(newCityName.equals(oldCity.getCityName()))
        {
            if(newStateName != null && !newStateName.equals(oldCity.getState()))
            {
                ret = true;
            }
        }
        else
        {
            ret = true;;
        }
        
        return  ret;
    }

    // Insert weather data into database.
    private void insertWeatherIntoDB(Weather weather,boolean  manu) {
        helper.updateWeather(weather);
        
        mCityList = checkDataBase();

        Intent i = new Intent("android.intent.action.WEATHER_BROADCAST");
        i.putExtra("weather", true);
        /*PR 470670- Neo Skunkworks - Wells Tang  - 001 Begin*/
        i.putExtra("manu",manu);
        /*PR 470670- Neo Skunkworks - Wells Tang  - 001 End*/
        i.putExtra("city_count", mCityList.size());
        i.putExtra("location_key", weather.getLocal().getCityId());
        sendBroadcast(i);
        
        long   lastRefreshTime = SharePreferenceUtils.getLong(MyService.this, KEY_LAST_REFRESHTIME, -1);
        if(lastRefreshTime == -1)
        {
            SharePreferenceUtils.saveLong(MyService.this, KEY_LAST_REFRESHTIME, System.currentTimeMillis());
        }

    }

    public List<City> getCitys() {
        return this.mCityList;
    }

    // Get the current position by locationKey
    public int getCurrentPosition(String tempKey) {
        int j = 0;
        mCityList = checkDataBase();
        for (int i = 0; i < mCityList.size(); i++) {
            if (mCityList.get(i).getLocationKey().equals(tempKey)) {
                j = i;
            }
        }
        return j;
    }
    
    /**
     * @author hongjun.tang
     * @param item
     * @return location key of the index
     */
    public   String   getTempkeyByItem(int  item)
    {
        mCityList = checkDataBase();
        if(mCityList == null || mCityList.size()<=item)
        {
            return null;
        }
        else
        {
            return  mCityList.get(item).getLocationKey();
        }
    }
    //add end;

    public void getAdvertisement(String url) {
        this.mAdvertisementURL = url;

        AdvertisementThread thread = new AdvertisementThread();
        thread.start();
    }

    private class AdvertisementThread extends Thread {
        @Override
        public void run() {
            try {
                InputStream mInputStream = downloadUrl(mAdvertisementURL).getInputStream();
                String result = new AdvertisementParser().parse(mInputStream);
                mInputStream.close();

                Intent i = new Intent("android.intent.action.ADVERTISE_BROADCAST");
                i.putExtra("advertise", result);
                sendBroadcast(i);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //CR 447398 - ting.chen@tct-nj.com - 001 modified begin
    public void insertCity(City city, boolean isAutoLocate) {//add a boolean parameter for autolocate
        //CR 447398 - ting.chen@tct-nj.com - 001 modified end
    	Log.e(TAG, "insertCity() call insert function!");
        this.isDataUpdated = false;
        city.setAutoLocate(isAutoLocate);//CR 447398 - ting.chen@tct-nj.com - 001 added
        helper.insertCity(city);
        
        // PR 456296 - Neo Skunkworks - Wells Tang - 001 begin
        if (!city.getLocationKey().startsWith(POSTALCODE))
        {
            helper.insertCity(city);
        }
        else 
        {
            tempCity = city;
        }
        // PR 456296 - Neo Skunkworks - Wells Tang - 001 end

        sendWeatherDataRequest(city.getLocationKey());
    }    

    // Check all citys if one city has no weather data,just delete it.
    public void checkAllTable() {
        // PR:372080,zhuang feng modify start.
        mCityList = checkDataBase();
        if (mCityList.size() != 0) {
            for (City c : mCityList) {
                boolean ifCurrentNew = !helper.checkDataIfExists("current",
                        c.getLocationKey());
                if (ifCurrentNew) {
                    deleteCity(c.getLocationKey());
                    mCityList = checkDataBase();
                }
            }
        }
        // PR:372080,zhuang feng modify end.
    }

    public WeatherInfo getWeatherFromDB(String locationKey) {
        WeatherInfo weather = new WeatherInfo();

        weather.setWeatherForShow(helper.getWeatherForShow(locationKey));
        weather.setDayForShow(helper.getDayForShow(locationKey));

        return weather;
    }

    public String getStrWeatherIconFromDB(String locationKey) {
        return helper.getStrWeatherIcon(locationKey);
    }

    public List<HourForShow> getHours(String locationKey) {
        return helper.getHourForShow(locationKey);
    }

    public void deleteCity(String locationKey) {
        helper.deleteCity(locationKey);
        
        Intent  i = new Intent(this,UpdateWidgetTimeService.class);
        i.setAction("android.action.deletecity");
        startService(i);        
    }

    public List<City> checkDataBase() {

        return helper.getCitysFromDatabase();
    }

    // get the current languageID
    /*private int getLanguageId() {
        int languageId = 0;
        String langcode = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
        String[] langids = this.getResources().getStringArray(R.array.language);

        PR 560936- Neo Skunkworks - Wells Tang  - 001 Begin
        for (int i = 0; i < langids.length; i++) {
            if (langcode.contains(langids[i])) {
                languageId = i;
            }
        }
        languageId ++;
        PR 560936- Neo Skunkworks - Wells Tang  - 001 End
        
        return languageId;
    }*/
    
    private String getLanguage() {
		String langcode = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry().toLowerCase();
		String[] langids = this.getResources().getStringArray(R.array.language);
		for (int i = 0; i < langids.length; i ++) {
			if (langcode.contains(langids[i])) {
				return langids[i];
			}
		}
		return "en-us";
	}

    //CR 447398 - ting.chen@tct-nj.com - 001 added begin
    public boolean isFirstCity(String locationKey){
        return helper.isFirstCity(locationKey);
    }
    //CR 447398 - ting.chen@tct-nj.com - 001 added end

	//zhaoyun.wu begin
	private boolean isBetterLocation(Location locationA, Location locationB) {
		if (locationA == null) {
			return false;
		}
		if (locationB == null) {
			return true;
		}
		// A provider is better if the reading is sufficiently newer. Heading
		// underground can cause GPS to stop reporting fixes. In this case it's
		// appropriate to revert to cell, even when its accuracy is less.
		if (locationA.getElapsedRealtimeNanos() > (locationB.getElapsedRealtimeNanos() + 11 * 1000000000)) {
			return true;
		} else if (locationB.getElapsedRealtimeNanos() > (locationA.getElapsedRealtimeNanos() + 11 * 1000000000)) {
			return false;
		}

		// A provider is better if it has better accuracy. Assuming both
		// readings
		// are fresh (and by that accurate), choose the one with the smaller
		// accuracy circle.
		if (!locationA.hasAccuracy()) {
			return false;
		}
		if (!locationB.hasAccuracy()) {
			return true;
		}
		return locationA.getAccuracy() < locationB.getAccuracy();
	}

	private Location getBetterLocation() {
		Location location = null;
//		mNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		mGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (isBetterLocation(mGpsLocation, mNetworkLocation)) {
			Log.e(TAG, "zhaoyun.wu___isAutoLocate && !updateWeatherOnly___mGpsLocation");
			location = mGpsLocation;
		} else {
			location = mNetworkLocation;
		}
		return location;
	}
	//zhaoyun.wu end
	
	// add by jielong.xing for pr796640 at 2014-09-30 begin
	private synchronized void sendTokyoFindRequest(String cityName) {
		/*int languageId = getLanguageId();
		try {
			mSearchURL = new StringBuilder(URL_CITY_FIND)
				.append("location=" + URLEncoder.encode(cityName, "utf-8")
					+ "&langid=" + languageId).toString().trim();
			mWeatherURL = new StringBuilder(URL_WEATHER_DATA).append(CITY_TOKYO_LOCATIONKEY).toString().trim();
		} catch (Exception e) {
			Log.e(TAG, "findTokyoData exception :: " + e.getMessage());
		}*/
		
		TokyoCityAndWeatherFindThread thread = new TokyoCityAndWeatherFindThread();
		thread.cityName = cityName;
		
		thread.start();
	}
	
	private class TokyoCityAndWeatherFindThread extends Thread {
		public String cityName = "";
		public void run() {
			try {
				/*InputStream mInputStream = downloadUrl(mSearchURL).getInputStream();
	            List<City> tempCityList = CityFindParser.parse(mInputStream);
	            mInputStream.close();*/
				List<City> tempCityList = CityFindRequest.findCityByName(URLEncoder.encode(cityName, "utf-8"), getLanguage(), true);
	            
	            City tokyoCity = null;
	            if (null != tempCityList && tempCityList.size() > 0) {
	            	for (City city : tempCityList) {
	            		String locationKey = city.getLocationKey();
	            		if (CITY_TOKYO_LOCATIONKEY.equals(locationKey)) {
	            			tokyoCity = city;
	            			break;
	            		}
	            	}
	            }
	            
	            if (tokyoCity != null) {
	            	setUpdateManue();
	            	insertCity(tokyoCity, false);
	            	
	            	SharePreferenceUtils.checkCommonCity(getApplicationContext(), tokyoCity.getLocationKey());
	            	
	            	/*mInputStream = downloadUrl(mWeatherURL).getInputStream();
	                Weather weather = new WeatherDataParser().parse(mInputStream);
	                mInputStream.close();*/
	            	/*Weather weather = getWeather(CITY_TOKYO_LOCATIONKEY, getLanguage());	                
	                insertWeatherIntoDB(weather, true);*/
	                
	                Editor editor = getApplicationContext().getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).edit();
	                editor.putBoolean("isFirstUse", false);
	                editor.commit();
	                
	                isFirstUse = false;
	                
	                unregisterReceiver(mNetworkBroadcastReceiver);
	            }
			} catch (Exception e) {
				Log.e(TAG, "TokyoCityAndWeatherFindThread run exception :: " + e.getMessage());
			}
		}
	}
	
	private boolean isForceSetTokyo() {
		return CustomizeUtils.getBoolean(MyService.this, "def_weather_forceSetTokyoAsDefaultCity_on");
	}
	
	private BroadcastReceiver mNetworkBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			
			if (networkInfo != null && networkInfo.isConnected()) {
				sendTokyoFindRequest("tokyo");
			}
		}
		
	};
	// add by jielong.xing for pr796640 at 2014-09-30 end
	
	private Weather getWeather(String locationKey, String lang) throws ClientProtocolException, IOException, JSONException, ParseException {
		Weather weather = new Weather();
    	Currentconditions current = CurrentWeatherRequest.getCurrentWeather(locationKey, lang);
    	if (current == null) {
    		return null;
    	}
    	weather.setCurrentconditions(current);
    	Forecast forecast = new Forecast();
    	List<Day> dayList = ForecastWeatherRequest.getDailyForecastWeather(locationKey, lang);
    	List<Hour> hourList = ForecastWeatherRequest.get24HourForecastWeather(locationKey, lang);
    	forecast.setDays(dayList);
    	forecast.setHours(hourList);
    	weather.setForecast(forecast);
    	Local local = new Local();
    	local.setCityId(locationKey);
    	local.setTime(System.currentTimeMillis() + "");
    	weather.setLocal(local);
    	return weather;
	}
}
