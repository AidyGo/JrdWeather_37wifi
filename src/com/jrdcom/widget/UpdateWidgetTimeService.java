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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/widget/                       */
/*               UpdateWidgetTimeService.java                                         */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.widget;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.jrdcom.bean.City;
import com.jrdcom.data.MyService;
import com.jrdcom.provider.DBHelper;
import com.jrdcom.provider.WeatherInfo.CityInfo;
import com.jrdcom.provider.WeatherInfo.Current;
import com.jrdcom.reflect.SystemProperties;
import com.jrdcom.util.CustomizeUtils;
import com.jrdcom.weather.LocateActivity;
import com.jrdcom.weather.MainActivity;
import com.jrdcom.weather.R;

public class UpdateWidgetTimeService extends Service {
	private Context mContext;
	private final String INVALIDATA_LOCATION = "no_city_key";
	private String locationKey = INVALIDATA_LOCATION;
	private static final int NO_CITY = 0;
	private static final int NORMAL = 1;
	private static final int OFF_LINE = -1;
	private SharedPreferences sp;
	public static int widgetState = NO_CITY;
	private boolean mAutoLocationCity = false;

	private String mTextDes;
	public String currentTemp;
	public String highTemperature;
	public String lowTemperature;
	public String realfeel;
	public String weatherIcon;
	public String cityName;
	public String stateName;
	public String updateTime;
	private int cursorCount = 0;
	private long weatherOfflineTime = 0;
	private Intent mIntent;
	private boolean isUnitC = true;
	private IntentFilter mFilter;
	private static final String TAG = "UpdateWidgetTimeService";
	String[] PROJECTION = new String[] { Current.CURRENT_TEMPERATURE,
			Current.HIGH_TEMPERATURE, Current.LOW_TEMPERATURE,
			Current.REALFEEL, Current.ICON };
	String[] PROJECTION1 = new String[] { CityInfo.CITY_NAME,
			CityInfo.UPDATE_TIME, CityInfo.LOCATION_KEY, CityInfo.STATE_NAME 
	};
	String selection = Current.LOCATION_KEY + " = ?";
	int appWidgetId;
	private DBHelper mDBHelper;

	private static final int HOMESCREEN_LAYOUT = R.layout.weather_clock_widget_new;

	private AppWidgetManager mAppWidgetManager;
	
	// add by jielong.xing for RR947763 at 2015-3-17 begin
	private boolean isUseSystemDateformat = false;
	// add by jielong.xing for RR947763 at 2015-3-17 end

	@Override
	public void onCreate() {
		super.onCreate();
		// make service to be foreground service
		startForeground(0, null);
		mContext = this;
		// register all of the broadcast to update widget
		if (mFilter == null) {
			mFilter = new IntentFilter();
			// receive time update broadcast
			mFilter.addAction(Intent.ACTION_TIME_TICK);
			mFilter.addAction(Intent.ACTION_TIME_CHANGED);
			// receive widget update broadcast
			mFilter.addAction(WeatherClockWidget.UPDATE_VIEW);
			// receive broadcast from weather app
			mFilter.addAction("android.intent.action.WEATHER_BROADCAST");
			// receive broadcast from weather widget(1*4) to exchange cities

			// receive broadcat from weather app to exchange weather unit(F/C)
			mFilter.addAction("android.intent.action.UNIT_BROADCAST");
			mFilter.addAction("android.intent.action.WEATHERICON_NEED_BROADCAST");
			// CR 447398 - ting.chen@tct-nj.com - 001 added begin
			// receive broadcast from autolocate service to update widget.
			mFilter.addAction("com.jrdcom.jrdweather.switchdisplay");
			// CR 447398 - ting.chen@tct-nj.com - 001 added end

			/* CR 520508- Neo Skunkworks - Wells.Tang - 001 Begin */
			mFilter.addAction("android.intent.action.dateformatchange");
			/* CR 520508- Neo Skunkworks - Wells.Tang - 001 end */
		}
		getApplicationContext().registerReceiver(mTimeTickReceiver, mFilter);
		// FR 417282 - ting.chen@tct-nj.com - 001 added begin
		mDBHelper = new DBHelper(mContext);
		// FR 417282 - ting.chen@tct-nj.com - 001 added end

		// PR 455558 - ting.chen@tct-nj.com - 001 added begin
		mAppWidgetManager = AppWidgetManager.getInstance(mContext);
		// PR 455558 - ting.chen@tct-nj.com - 001 added end

		/* PR 477163- Neo Skunkworks - Wells.Tang - 001 Begin */
		getCurrentLocationKey(mContext);

		if (locationKey == null || "".equals(locationKey)
				|| INVALIDATA_LOCATION.equals(locationKey)) {
			queryNextWeatherCity(mContext);
		}

		if (null != updateAllWidgetHandler) {
			updateAllWidgetHandler.removeCallbacks(updateAllWidgetRunnable);
			updateAllWidgetHandler.post(updateAllWidgetRunnable);
		}
		/* PR 477163- Neo Skunkworks - Wells.Tang - 001 End */
		
		// add by jielong.xing for RR947763 at 2015-3-17 begin
		getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.DATE_FORMAT), true,
				mContentObserver);
		// add by jielong.xing for RR947763 at 2015-3-17 end
	}
	
	// add by jielong.xing for RR947763 at 2015-3-17 begin
	private class CustomContentObserver extends ContentObserver {
		public CustomContentObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			updateAllWidgetHandler.removeCallbacks(updateAllWidgetRunnable);
			updateAllWidgetHandler.post(updateAllWidgetRunnable);
		}
	}

	private ContentObserver mContentObserver = new CustomContentObserver();	
	// add by jielong.xing for RR947763 at 2015-3-17 end
	
	private boolean isMTKPlatform() {
        try {
            String platform = SystemProperties.get("ro.mediatek.platform");
            if (platform.startsWith("MT")) {
                return true;
            }
        } catch (Exception e) {
            Log.i(TAG,"It's not MTK platform");
        }
        return false;
    }


	@Override
	public void onDestroy() {
		super.onDestroy();
		getApplicationContext().unregisterReceiver(mTimeTickReceiver);

		/* PR 477163- Neo Skunkworks - Wells.Tang - 001 Begin */
		Intent intent = new Intent(this, UpdateWidgetTimeService.class);
		startService(intent);
		/* PR 477163- Neo Skunkworks - Wells.Tang - 001 End */

		// PR771778 The automatic update time with the weather is not right by
		// jielong.xing at 2014-08-26 begin
		if (isMTKPlatform()) {
			stopForeground(true);
		}		 
		// PR771778 The automatic update time with the weather is not right by
		// jielong.xing at 2014-08-26 end
		 // add by jielong.xing for RR947763 at 2015-3-17 begin
		 getContentResolver().unregisterContentObserver(mContentObserver);
		 // add by jielong.xing for RR947763 at 2015-3-17 end
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;// add by shenxin for restart service

		// PR771778 The automatic update time with the weather is not right by
		// jielong.xing at 2014-08-26 begin
		if (isMTKPlatform()) {
			Log.e(TAG, "MTK Platform");
			Notification notification = new Notification(R.drawable.jrdweather_icon, 
					getResources().getString(R.string.app_name), 
					System.currentTimeMillis());
			Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent notiIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent2, 0);
			notification.setLatestEventInfo(getApplicationContext(), "", "", notiIntent);
			notification.flags |= 0x10000000;
			startForeground(1, notification);
		}		 
		// PR771778 The automatic update time with the weather is not right by
		// jielong.xing at 2014-08-26 end

		/* PR 468561- Neo Skunkworks - Wells.Tang - 001 Begin */
		if (intent != null && "android.action.deletecity".equals(intent.getAction())) {
			if (null != nextCityUpdateHandler) {
				nextCityUpdateHandler.removeCallbacks(updateDeleteCityRunnable);
				nextCityUpdateHandler.post(updateDeleteCityRunnable);
			}
		} else if (intent != null && WeatherClockWidget.UPDATE_VIEW.equals(intent.getAction())) {
			getCurrentLocationKey(mContext);
			if (null != updateAllWidgetHandler) {
				updateAllWidgetHandler.removeCallbacks(updateAllWidgetRunnable);
				updateAllWidgetHandler.post(updateAllWidgetRunnable);
			}
		}
		// add by jielong.xing at 2014-08-14 for PR764489 begin
		else if (null != intent && "android.intent.action.NEXT_CITY_WIDGET_UPDATE".equals(intent.getAction())) {
			if (null != nextCityUpdateHandler) {
				nextCityUpdateHandler.post(nextCityQueryRunalbe);
			}
		}
		// add by jielong.xing at 2014-08-14 for PR764489 end
		else {
			getCurrentLocationKey(mContext);
			updateClockFromTimeBroadcast(mContext);
		}
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mContext = context;
			if (intent.getAction().equals(WeatherClockWidget.UPDATE_VIEW)) {
				// receive broadcast from widget to update view according
				// to locationKey
				getCurrentLocationKey(mContext);
				if (null != updateAllWidgetHandler) {
					updateAllWidgetHandler.removeCallbacks(updateAllWidgetRunnable);
					updateAllWidgetHandler.post(updateAllWidgetRunnable);
				}

			} else {
				if (null != intent.getStringExtra("location_key")) {
					City city = mDBHelper.getCityByLocationKey(intent.getStringExtra("location_key"));
					if ((city != null && !city.isAutoLocate()) || mAutoLocationCity) {
						locationKey = intent.getStringExtra("location_key");
						setCurrentLocationKey(mContext);
					} else {
						boolean manu = intent.getBooleanExtra("manu", false);
						if (city == null || manu || locationKey == null || "".equals(locationKey) || INVALIDATA_LOCATION.equals(locationKey)) {
							locationKey = intent.getStringExtra("location_key");
							setCurrentLocationKey(mContext);
						}
					}
				} else {
					// if receive time update broadcast, it's intent not have
					// location_key info ,so we need read from sharedpreference
					getCurrentLocationKey(mContext);
				}

				/* PR 490499- Neo Skunkworks - Paul Xu modifyed - 001 End */
				// receive time broadcast from system to update widget according
				// to
				// locationKey
				if (intent.getAction().equals(Intent.ACTION_TIME_TICK)
						|| intent.getAction()
								.equals(Intent.ACTION_TIME_CHANGED)) {
					// start service to check and finish update weather every
					// half an hour
					startService(new Intent(UpdateWidgetTimeService.this,
							MyService.class));
					// receive broadcast from weather app to update widget

					// add by jielong.xing for when time changed the icon on
					// widget do not refresh begin
					if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
						if (null != refreshHandler) {
							refreshHandler.removeCallbacks(queryRunable);
							refreshHandler.post(queryRunable);
						}
					}
					// add by jielong.xing for when time changed the icon on
					// widget do not refresh end
				} else if (intent.getAction().equals(
						"android.intent.action.WEATHER_BROADCAST")) {

					Boolean isDataReady = intent.getBooleanExtra("weather",
							false);
					if (isDataReady) {
						if (null != refreshHandler) {
							refreshHandler.removeCallbacks(queryRunable);
							refreshHandler.post(queryRunable);
						}
					}
					if (null != nextCityUpdateHandler
							&& intent.getBooleanExtra("delete", false) == true) {
						nextCityUpdateHandler
								.removeCallbacks(nextCityQueryRunalbe);
						nextCityUpdateHandler.post(nextCityQueryRunalbe);
					}

					// receive broadcast from weather widget(1*4) to exchange to
					// next city
				} else if (intent.getAction().equals(
						"android.intent.action.UNIT_BROADCAST")) {

					if (null != refreshHandler) {
						refreshHandler.removeCallbacks(queryRunable);
						refreshHandler.post(queryRunable);
					}

				}
				// add by shenxin for PR446977 weatherwallpaper
				else if (intent.getAction().equals(
						"android.intent.action.WEATHERICON_NEED_BROADCAST")) {

					try {
						Intent icon = new Intent(
								"android.intent.action.WEATHERICON_GIVE_BROADCAST");
						int mIcon = Integer.parseInt(weatherIcon);

						// CR 652773- Neo Skunkworks -Wells Tang- 001 begin
						SharedPreferences shareUnit = context
								.getSharedPreferences("weather",
										Context.MODE_WORLD_WRITEABLE
												| Context.MODE_WORLD_READABLE);
						String unit = CustomizeUtils.getString(
								UpdateWidgetTimeService.this,
								"def_weather_unit_name");
						/*
						 * PR 695602- Neo Skunkworks - Richard He modified - 001
						 * Begin
						 */
						unit = CustomizeUtils.splitQuotationMarks(unit);
						if ("isUnitF".equals(unit)) {
							/*
							 * PR 695602- Neo Skunkworks - Richard He modified -
							 * 001 End
							 */
							isUnitC = shareUnit.getBoolean("unit", false);
						} else {
							isUnitC = shareUnit.getBoolean("unit", true);
						}

						String strTemp = null;

						if (isUnitC) {
							strTemp = fToc(currentTemp) + "°";
						} else {
							strTemp = currentTemp + "°";
						}

						// /Log.d(TAG,"sendtemp="+strTemp);
						icon.putExtra("temperate", strTemp);
						// CR 652773- Neo Skunkworks -Wells Tang- 001 end

						// add the describe of weather
						// Log.d(TAG,"describe ="+mTextDes);
						mTextDes = getWeatherText(mIcon);
						icon.putExtra("describe", mTextDes);

						icon.putExtra("weatherIcon", mIcon);
						sendBroadcast(icon);
					} catch (Exception e) {
					}

				}
				// CR 447398 - ting.chen@tct-nj.com - 001 added begin
				// receive broadcast from autolocate service to update widget
				else if (intent.getAction().equals(
						"com.jrdcom.jrdweather.switchdisplay")) {
					if (null != refreshHandler) {
						refreshHandler.removeCallbacks(queryRunable);
						refreshHandler.post(queryRunable);
					}
				}
				// CR 447398 - ting.chen@tct-nj.com - 001 added end
			}

		}
	};

	// query current weather info from database
	public void queryWeatherCity(Context context) {
		String selectionParam[] = new String[] { locationKey };
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(Current.CONTENT_URI, PROJECTION,
				selection, selectionParam, null);

		// CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
		// add null pointer protected
		if (cursor != null) {
			cursorCount = cursor.getCount();
		} else {
			cursorCount = 0;
		}
		// CR 564564- Neo Skunkworks - Wells Tang - 001 End

		while (cursor != null && cursor.moveToNext()) {
			currentTemp = cursor.getString(0);
			highTemperature = cursor.getString(1);
			lowTemperature = cursor.getString(2);
			realfeel = cursor.getString(3);
			weatherIcon = cursor.getString(4);
			Log.d(TAG, "weatherDescription= " + currentTemp
					+ " highTemperature= " + highTemperature
					+ " lowTemperature= " + lowTemperature + " realfeel= "
					+ realfeel + " weatherIcon " + weatherIcon);
		}
		String selection1 = CityInfo.LOCATION_KEY + " = ?";
		Cursor cursor2 = contentResolver.query(CityInfo.CONTENT_URI,
				PROJECTION1, selection1, selectionParam, null);
		while (cursor2 != null && cursor2.moveToNext()) {
			cityName = cursor2.getString(0);
			updateTime = cursor2.getString(1);
			locationKey = cursor2.getString(2);
			stateName = cursor2.getString(3);// add by shenxin for PR435934
		}

		// CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
		// add null pointer protected
		if (cursor != null) {
			cursor.close();
		}

		if (cursor2 != null) {
			cursor2.close();
		}
		// CR 564564- Neo Skunkworks - Wells Tang - 001 End

		/* CR 484584- Neo Skunkworks - Paul Xu added - 001 Begin */
		if (mDBHelper.getCityByLocationKey(locationKey) != null
				&& mDBHelper.getCityByLocationKey(locationKey).isAutoLocate()) {
			mAutoLocationCity = true;
		} else {
			mAutoLocationCity = false;
		}
	}

	/**
	 * @author hongjun.tang
	 * @param context
	 */
	public void updateDeleteCity(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(CityInfo.CONTENT_URI,
				PROJECTION1, null, null, null);

		if (cursor != null) {
			cursorCount = cursor.getCount();
			int index = -1;

			if (cursorCount > 0) {
				for (int i = 0; i < cursorCount; i++) {
					cursor.moveToPosition(i);
					if (cursor.getString(2).equals(locationKey)) {
						index = i;
						break;
					}
				}

				if (index == -1) {
					/* PR 468554- Neo Skunkworks - Paul Xu added - 001 Begin */
					cursor.moveToFirst();
					/* PR 468554- Neo Skunkworks - Paul Xu added - 001 End */
					cityName = cursor.getString(0);
					updateTime = cursor.getString(1);
					locationKey = cursor.getString(2);
					stateName = cursor.getString(3);
				}

			} else {
				locationKey = INVALIDATA_LOCATION;
			}

			if (!INVALIDATA_LOCATION.equals(locationKey)) {
				String selectionParam[] = new String[] { locationKey };
				Cursor cursor2 = contentResolver.query(Current.CONTENT_URI,
						PROJECTION, selection, selectionParam, null);
				while (cursor2 != null && cursor2.moveToNext()) {
					currentTemp = cursor2.getString(0);
					highTemperature = cursor2.getString(1);
					lowTemperature = cursor2.getString(2);
					realfeel = cursor2.getString(3);
					weatherIcon = cursor2.getString(4);
				}
				cursor2.close();
				cursor2 = null;
				/* CR 484584- Neo Skunkworks - Paul Xu added - 001 Begin */
				if (mDBHelper.getCityByLocationKey(locationKey) != null
						&& mDBHelper.getCityByLocationKey(locationKey)
								.isAutoLocate()) {
					mAutoLocationCity = true;
				} else {
					mAutoLocationCity = false;
				}
				/* CR 484584- Neo Skunkworks - Paul Xu added - 001 End */
			}

			setCurrentLocationKey(mContext);

			cursor.close();
			cursor = null;

		}
	}

	// add end;

	// query next city weather info when click city area to exchange cities
	public void queryNextWeatherCity(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(CityInfo.CONTENT_URI,
				PROJECTION1, null, null, null);
		cursorCount = cursor.getCount();
		while (cursor != null && cursor.moveToNext()) {
			if (cityName != null) {
				/* PR 513270- Neo Skunkworks - Paul Xu modifyed - 001 Begin */
				if (cityName.equals(cursor.getString(0))
						&& stateName.equals(cursor.getString(3))
						&& locationKey.equals(cursor.getString(2))) {
					/* PR 513270- Neo Skunkworks - Paul Xu modifyed - 001 End */
					if (!cursor.moveToNext()) {
						cursor.moveToPosition(0);
					}
					cityName = cursor.getString(0);
					updateTime = cursor.getString(1);
					locationKey = cursor.getString(2);
					stateName = cursor.getString(3);// add by shenxin for
													// PR435934
					break;
				}
				if (cursor.isLast()) {
					cursor.moveToFirst();
					cityName = cursor.getString(0);
					updateTime = cursor.getString(1);
					locationKey = cursor.getString(2);
					stateName = cursor.getString(3);// add by shenxin for
													// PR435934
					break;
				}
			} else {
				cursor.moveToFirst();
				cityName = cursor.getString(0);
				updateTime = cursor.getString(1);
				locationKey = cursor.getString(2);
				stateName = cursor.getString(3);// add by shenxin for PR435934
				break;
			}
		}
		String selectionParam[] = new String[] { locationKey };
		Cursor cursor2 = contentResolver.query(Current.CONTENT_URI, PROJECTION,
				selection, selectionParam, null);
		while (cursor2 != null && cursor2.moveToNext()) {
			currentTemp = cursor2.getString(0);
			highTemperature = cursor2.getString(1);
			lowTemperature = cursor2.getString(2);
			realfeel = cursor2.getString(3);
			weatherIcon = cursor2.getString(4);
		}
		Log.d(TAG, "queryNextWeatherCity " + "weatherDescription= "
				+ currentTemp + " highTemperature= " + highTemperature
				+ " lowTemperature= " + lowTemperature + " realfeel= "
				+ realfeel + " weatherIcon " + weatherIcon + "locationKey = "
				+ locationKey);
		cursor.close();
		cursor2.close();

		/* CR 484584- Neo Skunkworks - Paul Xu added - 001 Begin */
		if (mDBHelper.getCityByLocationKey(locationKey) != null
				&& mDBHelper.getCityByLocationKey(locationKey).isAutoLocate()) {
			mAutoLocationCity = true;
		} else {
			mAutoLocationCity = false;
		}

	}

	// store current locationkey
	private void setCurrentLocationKey(Context context) {
		if (!locationKey.equals(INVALIDATA_LOCATION)) {
			sp = context.getSharedPreferences("weather",
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = sp.edit();
			// modify by jielong.xing at 2014-09-11 for pr781054 begin
			editor.putString("currentcity", locationKey);
			editor.putString("currentLocationKey", locationKey);
			// modify by jielong.xing at 2014-09-11 for pr781054 end
			editor.commit();
		}
	}

	private void getCurrentLocationKey(Context context) {
		sp = context.getSharedPreferences("weather",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		// modify by jielong.xing at 2014-09-11 for pr781054 begin
		locationKey = sp.getString("currentcity", INVALIDATA_LOCATION);
		// modify by jielong.xing at 2014-09-11 for pr781054 end
	}

	private void viewsUpdate(Context context) {
		// PR 455558 - ting.chen@tct-nj.com - 001 added begin
		int[] appWidgetIds = mAppWidgetManager
				.getAppWidgetIds(new ComponentName(mContext,
						WeatherClockWidget.class));
		for (int appWidgetId : appWidgetIds) {
			viewUpdate(context, appWidgetId);
		}
		// PR 455558 - ting.chen@tct-nj.com - 001 added end
	}

	public void updateClockFromTimeBroadcast(Context mContext) {
		queryWeatherCity(mContext);
		viewsUpdate(mContext);
	}

	// FR 424108 ting.chen@tct-nj.com - 001 added
	private void setViewsState(RemoteViews views, int widgetState) {
		switch (widgetState) {
		case NO_CITY:
			views.setInt(R.id.weather_part, "setVisibility", View.GONE);
			views.setInt(R.id.add_location, "setVisibility", View.VISIBLE);
			views.setInt(R.id.widget_offline_text, "setVisibility", View.GONE);
			break;
		case OFF_LINE:
			views.setInt(R.id.weather_part, "setVisibility", View.GONE);
			views.setInt(R.id.add_location, "setVisibility", View.GONE);
			views.setInt(R.id.widget_offline_text, "setVisibility",
					View.VISIBLE);
			break;
		case NORMAL:
			views.setInt(R.id.weather_part, "setVisibility", View.VISIBLE);
			views.setInt(R.id.add_location, "setVisibility", View.GONE);
			views.setInt(R.id.widget_offline_text, "setVisibility", View.GONE);
			break;
		default:
			break;
		}
	}
	// FR 424108 ting.chen@tct-nj.com - 001 end
	
	private RemoteViews updateCityWeatherView(Context context) {
		RemoteViews views = new RemoteViews(mContext.getPackageName(), HOMESCREEN_LAYOUT);
		setCurrentLocationKey(mContext);

		SharedPreferences shareUnit = context.getSharedPreferences("weather",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);

		String unit = CustomizeUtils.getString(UpdateWidgetTimeService.this, "def_weather_unit_name");
		unit = CustomizeUtils.splitQuotationMarks(unit);
		boolean isFirstCalled = shareUnit.getBoolean("isFirstCalled", true);
		if (isFirstCalled) {
			android.content.SharedPreferences.Editor edit = shareUnit.edit();
			edit.putBoolean("unit", !("isUnitF".equals(unit)));
			edit.putBoolean("isFirstCalled", false);
			edit.commit();
		}
		if ("isUnitF".equals(unit)) {
			isUnitC = shareUnit.getBoolean("unit", false);
		} else {
			isUnitC = shareUnit.getBoolean("unit", true);
		}
		
		// add by jielong.xing for RR947763 at 2015-3-17 begin
		isUseSystemDateformat = CustomizeUtils.getBoolean(UpdateWidgetTimeService.this, "def_widget_use_system_dateformat_on");
		if (isUseSystemDateformat) {
			java.text.SimpleDateFormat format = (java.text.SimpleDateFormat) DateFormat.getDateFormat(this);
			String pattern = format.toPattern();
			if (!pattern.contains("yyyy")) {
				int len = pattern.length();
				StringBuffer sb = new StringBuffer();
				for (int k = 0; k < len; k ++) {
					if (pattern.charAt(k) == 'y') {
						sb.append("y");
					}
				}
				pattern = pattern.replaceAll(sb.toString(), "yyyy");
			}
			Log.e("jielong_widget", "pattern == " + pattern);
			views.setCharSequence(R.id.date, "setFormat12Hour", pattern);
			views.setCharSequence(R.id.date, "setFormat24Hour", pattern);
		}
		// add by jielong.xing for RR947763 at 2015-3-17 end

		if (cursorCount != 0) {
			try {
				weatherOfflineTime = System.currentTimeMillis()
						- Long.parseLong(updateTime);
			} catch (Exception e) {
				weatherOfflineTime = 0;
			}
		}
		// add by jielong.xing for PR760634 at 2014-08-14 begin
		if (mIntent == null) {
			mIntent = new Intent(context, MainActivity.class);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		}
		// add by jielong.xing for PR760634 at 2014-08-14 end
		
		// 861393 Tap weather widget can launcher weather when closed Flip cover by jielong.xing at 2014-12-5 begin
		int flipoverFlag = android.provider.Settings.Global.getInt(mContext.getContentResolver(), "flip_cover_mode", 0);
		// 861393 Tap weather widget can launcher weather when closed Flip cover by jielong.xing at 2014-12-5 end

		if (cursorCount == 0) {
			widgetState = NO_CITY;
			setViewsState(views, widgetState);
			if (flipoverFlag != 2) {
				Intent selectLocationIntent = new Intent(context, LocateActivity.class);
				PendingIntent pSelectLocationIntent = PendingIntent.getActivity(context, 0, selectLocationIntent, 0);
				views.setOnClickPendingIntent(R.id.add_location, pSelectLocationIntent);
			}			

			boolean result = getCurrentTime();
			if (result) {
				views.setInt(R.id.weather_clock_widget_layout, "setBackgroundResource", R.drawable.icons_33);
			} else {
				views.setInt(R.id.weather_clock_widget_layout, "setBackgroundResource", R.drawable.icons_01);
			}

			// if no city,send the broadcast to launcher to update
			Intent intent = new Intent("android.intent.action.WEATHERICON_GIVE_BROADCAST");
			intent.putExtra("weatherIcon", -1);
			intent.putExtra("describe", getString(R.string.app_name));
			sendBroadcast(intent);
		} else if (weatherOfflineTime > 172800000) {
			widgetState = OFF_LINE;
			setViewsState(views, widgetState);
			if (flipoverFlag != 2) {
				Intent selectOfflineIntent = new Intent(context, MainActivity.class);
				PendingIntent pSelectOfflineIntent = PendingIntent.getActivity(context, 0, selectOfflineIntent, 0);

				views.setOnClickPendingIntent(R.id.add_location, pSelectOfflineIntent);
				views.setOnClickPendingIntent(R.id.widget_offline_text, pSelectOfflineIntent);
			}
			
			boolean result = getCurrentTime();
			if (result) {
				views.setInt(R.id.weather_clock_widget_layout, "setBackgroundResource", R.drawable.icons_33);
			} else {
				views.setInt(R.id.weather_clock_widget_layout, "setBackgroundResource", R.drawable.icons_01);
			}
			/*-- PR 489612- Neo Skunkworks - Paul Xu - 001 added End */

			// if the weather is expired,send the broadcast to launcher to  update
			Intent intent = new Intent("android.intent.action.WEATHERICON_GIVE_BROADCAST");
			intent.putExtra("weatherIcon", -1);
			intent.putExtra("describe", getString(R.string.app_name));
			sendBroadcast(intent);
		} else {
			widgetState = NORMAL;

			try {
				Intent icon = new Intent("android.intent.action.WEATHERICON_GIVE_BROADCAST");
				int mIcon = Integer.parseInt(weatherIcon);

				String strTemp = null;
				if (isUnitC) {
					strTemp = fToc(currentTemp) + "°";
				} else {
					strTemp = currentTemp + "°";
				}

				icon.putExtra("temperate", strTemp);

				mTextDes = getWeatherText(mIcon);
				icon.putExtra("describe", mTextDes);

				icon.putExtra("weatherIcon", mIcon);
				sendBroadcast(icon);

				// modify by jielong.xing at 2014-08-29
				int background_weather_icon = getCurrentWeatherIcon(mIcon);
				views.setInt(R.id.weather_clock_widget_layout, "setBackgroundResource", background_weather_icon);
			} catch (Exception e) {
				Log.e(TAG, "weatherIcon is null");
			}

			views.setTextViewText(R.id.city_name, cityName);
			/* CR 484584- Neo Skunkworks - Paul Xu added - 001 Begin */
			views.setTextViewText(R.id.city_name_auto, cityName);
			if (mAutoLocationCity) {
				views.setViewVisibility(R.id.city_name, View.GONE);
				views.setViewVisibility(R.id.city_name_auto, View.VISIBLE);
			} else {
				views.setViewVisibility(R.id.city_name, View.VISIBLE);
				views.setViewVisibility(R.id.city_name_auto, View.GONE);
			}
			/* CR 484584- Neo Skunkworks - Paul Xu added - 001 End */
			if (isUnitC) {
				views.setTextViewText(R.id.real_temperature, fToc(currentTemp)
						+ "°C");
			} else {
				views.setTextViewText(R.id.real_temperature, currentTemp + "°F");
			}

			setViewsState(views, widgetState);
			/* PR 469584- Neo Skunkworks - Paul Xu modifyed - 001 End */
			if (flipoverFlag != 2) {
				Intent cityIntent = new Intent("android.intent.action.NEXT_CITY_WIDGET_UPDATE");
				PendingIntent pCityIntent = PendingIntent.getBroadcast(context, 0, cityIntent, 0);
				views.setOnClickPendingIntent(R.id.city_temperature, pCityIntent);
			}

			mIntent.setAction("com.jrdcom.weather.jump");
			Bundle bundle = new Bundle();
			bundle.putString("newCityKey", locationKey);
			mIntent.putExtras(bundle);
		}
		// add by jielong.xing at 2014-08-14 begin
		if (flipoverFlag != 2) {
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, mIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.rl_weather_clock_widget, pIntent);
			views.setOnClickPendingIntent(R.id.weather_clock_widget_layout, pIntent);
		}
		// add by jielong.xing at 2014-08-14 end

		return views;
	}

	private String getWeatherText(int weatherID) {
		int iconID = weatherID - 1;

		String[] weatherDescriptions = getResources().getStringArray(
				R.array.weather_icon_desc);

		return weatherDescriptions[iconID];
	}

	// add by shenxin for PR456558 start
	private String getWeekly(String sWeek) {
		String[] enWeekly = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri",
				"Sat", "Sun" };
		String result = null;
		String[] weekly = this.getResources().getStringArray(
				R.array.weather_weekly);
		for (int i = 0; i < 7; i++) {
			if (sWeek.contains(enWeekly[i])) {
				result = weekly[i];
			}
		}
		return result;
	}

	// add by shenxin for PR456558 end

	private int getResIdForIcon(String iconId) {
		String imagepath = "small_widget_icons_" + iconId;
		return mContext.getResources().getIdentifier(imagepath, "drawable",
				"com.jrdcom.weather");
	}

	/* PR 522117- Neo Skunkworks - Paul Xu added - 001 Begin */
	private void updateClockViewInKeyguard(Context context,
			final int appWidgetId, RemoteViews views) {
		setCurrentLocationKey(mContext);
		Intent clockIntent = new Intent();
		/* PR 694072- Neo Skunkworks - Richard He modified - 001 Begin */
		clockIntent.setAction(AlarmClock.ACTION_SHOW_ALARMS);// ACTION_SET_ALARM
		/* PR 694072- Neo Skunkworks - Richard He modified - 001 End */
		PendingIntent pClockIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
		views.setOnClickPendingIntent(R.id.clock_widget, pClockIntent);

		AppWidgetManager mAppWidgetManager = AppWidgetManager
				.getInstance(context);

		mAppWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/* PR 522117- Neo Skunkworks - Paul Xu added - 001 End */

	// update time info of the widget
	private void updateClockView(Context context, final int appWidgetId, RemoteViews views) {
		setCurrentLocationKey(mContext);
		// 861393 Tap weather widget can launcher weather when closed Flip cover by jielong.xing at 2014-12-5 begin
		int flipoverFlag = android.provider.Settings.Global.getInt(mContext.getContentResolver(), "flip_cover_mode", 0);
		// 861393 Tap weather widget can launcher weather when closed Flip cover by jielong.xing at 2014-12-5 end
		if (flipoverFlag != 2) {
			Intent clockIntent = new Intent();

			clockIntent.setAction(getAlarmAction());// AlarmClock.ACTION_SHOW_ALARMS);//ACTION_SET_ALARM
			PendingIntent pClockIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
			views.setOnClickPendingIntent(R.id.clock_widget, pClockIntent);
		}
		
		AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(context);

		mAppWidgetManager.updateAppWidget(appWidgetId, views);
	}

	// PR 455558 - ting.chen@tct-nj.com - 001 added begin
	private void viewUpdate(Context context, final int appWidgetId) {
		Bundle myOptions = mAppWidgetManager.getAppWidgetOptions(appWidgetId);
		RemoteViews views = updateCityWeatherView(context);
		updateClockView(context, appWidgetId, views);
	}
	// PR 455558 - ting.chen@tct-nj.com - 001 added end

	private String fToc(String fahrenheit) {
		int celsius = 0;

		try {
			if (fahrenheit != null) {
				celsius = ((int)Double.parseDouble(fahrenheit) - 32) * 5 / 9;
			}
			return celsius + "";
		} catch (Exception e) {
			return "";
		}
	}

	private Handler updateAllWidgetHandler = new Handler() {
	};
	private Runnable updateAllWidgetRunnable = new Runnable() {
		@Override
		public void run() {
			queryWeatherCity(mContext);
			viewsUpdate(mContext);
		}
	};
	
	private Handler refreshHandler = new Handler() {
	};
	private Runnable queryRunable = new Runnable() {

		@Override
		public void run() {
			queryWeatherCity(mContext);
			viewsUpdate(mContext);
		}
	};

	private Handler nextCityUpdateHandler = new Handler() {
	};
	private Runnable nextCityQueryRunalbe = new Runnable() {

		@Override
		public void run() {
			queryNextWeatherCity(mContext);
			viewsUpdate(mContext);

		}
	};

	/* PR 468561- Neo Skunkworks - Wells.Tang - 001 Begin */
	private Runnable updateDeleteCityRunnable = new Runnable() {

		@Override
		public void run() {
			updateDeleteCity(mContext);
			viewsUpdate(mContext);

		}
	};

	/* PR 468561- Neo Skunkworks - Wells.Tang - 001 End */

	/*-- PR 489612- Neo Skunkworks - Paul Xu - 001 added Begin */
	private boolean getCurrentTime() {
		boolean result = false;
		int hour;

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		ContentResolver cv = getContentResolver();
		String timeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		calendar.setTimeInMillis(System.currentTimeMillis());

		// CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
		// if(timeFormat.equals("12")){
		if ("12".equals(timeFormat)) {
			// CR 564564- Neo Skunkworks - Wells Tang - 001 End

			hour = calendar.get(Calendar.HOUR);
			if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
				if (hour < 6) {
					result = true;
				}
			} else {
				if (hour >= 6) {
					result = true;
				}
			}
		} else {
			hour = calendar.get(Calendar.HOUR_OF_DAY);
			if (hour >= 18 || hour < 6) {
				result = true;
			}
		}

		return result;
	}

	private int getCurrentWeatherIcon(int weather_icon_id) {
		if (weather_icon_id > 0) {
			String imagepath = "icons_" + ((weather_icon_id < 10) ? "0" : "") + weather_icon_id;
			int resId = getResources().getIdentifier(imagepath, "drawable", "com.jrdcom.weather");
			return resId;
		} else {
			return R.drawable.icons_01;
		}
	}

	// add by jielong.xing at 2014-08-14 begin
	@SuppressWarnings("rawtypes")
	private String getAlarmAction() {
		try {
			Class alarmClass = Class.forName("android.provider.AlarmClock");
			try {
				Field filed = alarmClass.getDeclaredField("ACTION_SHOW_ALARMS");
				if (filed != null) {
					return "android.intent.action.SHOW_ALARMS";
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return "android.intent.action.SET_TIMER";
	}
	// add by jielong.xing at 2014-08-14 end

}
