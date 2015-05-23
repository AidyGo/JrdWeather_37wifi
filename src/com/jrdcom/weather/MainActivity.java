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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/MainActivity.java     */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.bean.City;
import com.jrdcom.bean.DayForShow;
import com.jrdcom.bean.WeatherForShow;
import com.jrdcom.bean.WeatherInfo;
import com.jrdcom.data.MyService;
import com.jrdcom.util.CommonUtils;
import com.jrdcom.util.CustomizeUtils;
import com.jrdcom.util.SharePreferenceUtils;
import com.jrdcom.widget.Config;
import com.jrdcom.widget.UpdateWidgetTimeService;
import com.jrdcom.widget.WeatherClockWidget;
import com.jrdcom.weather.R;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "3_7_wifi";

    private static final int INITVIEWPAPER = 0x10001;

    private List<City> mCitys;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private List<View> mViews;

    private WindowManager wm;
    private int mScreenHeight;

    private DayView days[];

    private MyService myService;
    private int mPosition;
    private ProgressDialog pDialog;
    private MyBroadcasReceiver mBroadcastReceiver;
    private ImageView iv_web;
    private List<ImageView> mImageViews;
    private ImageView iv_point;
    private boolean isUnitC = true;
    private boolean isWifiConnected;
    private boolean isMobileConnected;
    private PagerAdapter mAdapter;
    private String mTempKey;

    private MenuItem mRefreshItem;
    private MenuItem mShowMenuItem;

    private ViewGroup mViewPoints;
    private boolean isUpdating = false;

    private boolean isOtherConnected; // add by junye.li for PR762484

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "MainActivity -- onCreate()");
        if (!CommonUtils.isSupportHorizontal(this)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mTempKey = intent.getExtras().getString("newCityKey");
        }

        setContentView(R.layout.activity_main);

        wm = (WindowManager) getApplicationContext().getSystemService("window");
        mScreenHeight = wm.getDefaultDisplay().getHeight();

        days = new DayView[4];

        mActionBar = this.getActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        mActionBar.setDisplayHomeAsUpEnabled(false);

        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.titel_sunny_bg));

        mCitys = new ArrayList<City>();

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setMessage(getResources().getString(R.string.loading));

        iv_web = (ImageView) findViewById(R.id.iv_web);
        iv_web.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.accuweather.com");
                intent.setData(content_url);
                startActivity(intent);
            }
        });

        // add by jielong.xing at 2014-08-14 for PR764489 begin
        Intent widgetServiceIntent = new Intent(this, UpdateWidgetTimeService.class);
        startService(widgetServiceIntent);
        // add by jielong.xing at 2014-08-14 for PR764489 end

        // add by jielong.xing at 2014-09-23 for PR792049 begin
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.DATE_FORMAT), true,
                mContentObserver);
        // add by jielong.xing at 2014-09-23 for PR792049 end
        isPad = CommonUtils.isPad(this);
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MyService.MyBinder) service).getService();
            if (myService != null) {
                if (checkDataBase()) {
                    initViewPager();
                    ;
                }
            }
        }
    };

    // add by jielong.xing for PR928058 at 2015-2-14 begin
    @Override
    protected void onStart() {
        super.onStart();
        Intent bindServiceIntent = new Intent(MainActivity.this, MyService.class);
        bindService(bindServiceIntent, conn, Context.BIND_AUTO_CREATE);

        registerBoradcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);

        unregisterReceiver(mBroadcastReceiver);
    }

    // add by jielong.xing for PR928058 at 2015-2-14 end

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTempKey = savedInstanceState.getString("tempkey");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tempkey", mTempKey);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String action = intent.getAction();
        if (action != null && action.equals("com.jrdcom.weather.jump")) {
            mTempKey = intent.getStringExtra("newCityKey");

            // solve the problem when the mainactivity is on the background,it will not jump to the
            // right page while click on the widget
            initViewPager();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences shareUnit = getSharedPreferences("weather",
                Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
        String unit = CustomizeUtils.getString(MainActivity.this, "def_weather_unit_name");
        unit = CustomizeUtils.splitQuotationMarks(unit);
        if ("isUnitF".equals(unit)) {
            isUnitC = shareUnit.getBoolean("unit", false);
        } else {
            isUnitC = shareUnit.getBoolean("unit", true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (previewbubble != null) {
            previewbubble.dismiss();
            previewbubble = null;
        }

        // add by wells,do not change the index of page when restart
        if (mViewPager != null && myService != null) {
            int item = mViewPager.getCurrentItem();
            if (item >= 0) {
                mTempKey = myService.getTempkeyByItem(item);
                /* PR 672508 - Neo Skunkworks - Richard He added - 001 Begin */
                SharePreferenceUtils.saveCurrentCityKey(getApplicationContext(), mTempKey);
                /* PR 672508 - Neo Skunkworks - Richard He added - 001 End */
            }
        }
        // add end;

        if (mCitys.size() != 0) {
            String locationKey = mCitys.get(mPosition).getLocationKey();
            Editor sharedata = getSharedPreferences("weather",
                    Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE).edit();
            sharedata.putString("currentcity", locationKey);
            sharedata.putString("currentLocationKey", locationKey);
            sharedata.commit();

            // add by jielong.xing at 2014-09-11 for pr781054 begin
            Intent intent = new Intent();
            intent.setAction(WeatherClockWidget.UPDATE_VIEW);
            sendBroadcast(intent);
            // add by jielong.xing at 2014-09-11 for pr781054 end
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // add by jielong.xing at 2014-09-23 for PR792049 begin
        getContentResolver().unregisterContentObserver(mContentObserver);
        // add by jielong.xing at 2014-09-23 for PR792049 end
    }

    // Get citys from database,if there are no city,go to locate page.
    private boolean checkDataBase() {
        mCitys = myService.checkDataBase();

        if (mCitys.size() == 0) {
            startActivity(new Intent(MainActivity.this, LocateActivity.class));
            MainActivity.this.finish();
            return false;
        } else {
            return true;
        }
    }

    // add by jielong.xing at 2014-09-23 for PR792049 begin
    private class CustomContentObserver extends ContentObserver {
        public CustomContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            initViewPager();
        }
    }

    private ContentObserver mContentObserver = new CustomContentObserver();

    // add by jielong.xing at 2014-09-23 for PR792049 end

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new MyBroadcasReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("android.intent.action.WEATHER_BROADCAST");
        // CR 447398 - ting.chen@tct-nj.com - 001 added begin
        myIntentFilter.addAction("com.jrdcom.jrdweather.switchdisplay");
        // CR 447398 - ting.chen@tct-nj.com - 001 added end
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private class MyBroadcasReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.WEATHER_BROADCAST")) {
                Bundle b = intent.getExtras();

                boolean isDataGot = b.getBoolean("weather");
                if (isDataGot) {
                    if (mViewPager != null && myService != null) {
                        int item = mViewPager.getCurrentItem();
                        if (item >= 0) {
                            mTempKey = myService.getTempkeyByItem(item);
                        }
                    }
                    // modify end
                    isUpdating = false;
                    initViewPager();

                    // CR 552491 - Neo Skunkworks - Wells Tang - 001 begin
                    boolean locationError = b.getBoolean("locationerror", false);
                    if (locationError) {
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.msg_unable_obtain_location),
                                Toast.LENGTH_LONG).show();
                    }
                    // CR 552491 - Neo Skunkworks - Wells Tang - 001 end
                }
                boolean connect_faild = b.getBoolean("connect_faild");
                if (connect_faild) {
                    pDialog.dismiss();
                    isUpdating = false;

                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_LONG).show();
                }
                boolean connect_timeout = b.getBoolean("connect_timeout");
                if (connect_timeout) {
                    pDialog.dismiss();
                    isUpdating = false;

                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.connect_timeout),
                            Toast.LENGTH_LONG).show();
                }
                // CR 447398 - ting.chen@tct-nj.com - 001 added begin
            } else if (action.equals("com.jrdcom.jrdweather.switchdisplay")) {
                // add by wells,remove unknow broadcast caused initViewPager twice
                boolean fromAutoLote = intent.getBooleanExtra("fromautolocate", false);
                if (!fromAutoLote) {
                    return;
                }
                // add end;

                // modified by wells,do not change the index of paper when refresh
                if (mViewPager != null && myService != null) {
                    int item = mViewPager.getCurrentItem();
                    if (item >= 0) {
                        mTempKey = myService.getTempkeyByItem(item);
                    }
                }
                initViewPager();
            }
        }
    }

    // PR 470670 - Neo Skunkworks - Tom Yu 001 begin
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case INITVIEWPAPER: {
                    if (mHandler != null) {
                        mHandler.removeMessages(INITVIEWPAPER);
                    }

                    if (!SharePreferenceUtils.inLocalActivity) {
                        initViewPager();
                    } else {
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(INITVIEWPAPER, 20000);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };
    // PR 470670 - Neo Skunkworks - Tom Yu 001 End

    private PopupWindow previewbubble;

    // Init the main page of weather information.
    private void initViewPager() {
        if (null == myService) {
            return;
        }
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mViews = new ArrayList<View>();

        mActionBar.setTitle(R.string.app_name);

        mImageViews = new ArrayList<ImageView>();
        mViewPoints = (ViewGroup) findViewById(R.id.pointGroup);

        mViews.clear();
        mImageViews.clear();
        mViewPoints.removeAllViews();
        mViewPager.removeAllViews();
        mPosition = 0;

        mCitys = myService.checkDataBase();

        // PR 524949 - Neo Skunkworks - Wells Tang 001 begin
        if (mCitys == null || mCitys.size() == 0) {
            startActivity(new Intent(MainActivity.this, LocateActivity.class));
            finish();
            return;
        }
        // PR 524949 - Neo Skunkworks - Wells Tang 001 begin
        // add by jielong.xing at 2014-10-17 begin
        mTitleBackground = new int[mCitys.size()];
        // add by jielong.xing at 2014-10-17 end
        for (int i = 0; i < mCitys.size(); i++) {
            // PR351637-Feng.Zhuang-001 Modify begin
            WeatherInfo weatherInfo = myService.getWeatherFromDB(mCitys.get(i).getLocationKey());

            // modified by wells
            WeatherForShow weatherForShow = weatherInfo.getWeatherForShow();
            List<DayForShow> dayForShow = weatherInfo.getDayForShow();

            if (weatherForShow == null || dayForShow == null) {
                myService.deleteCity(mCitys.get(i).getLocationKey());
                mCitys.clear();
                initViewPager();
                return;
            }
            // modify end;

            // add by jielong.xing at 2014-10-17 begin
            mTitleBackground[i] = getTitleBackground(weatherForShow.getIcon());
            // add by jielong.xing at 2014-10-17 end

            iv_point = new ImageView(MainActivity.this);
            iv_point.setPadding(4, 0, 4, 0);
            iv_point.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            if (i == 0) {
                iv_point.setBackgroundResource(R.drawable.point_light);
            } else {
                iv_point.setBackgroundResource(R.drawable.point_dark);
            }
            mImageViews.add(iv_point);

            mViewPoints.addView(mImageViews.get(i));

            View view = inflater.inflate(R.layout.view_pager_layout, null);
            mViews.add(view);

            /* CR 484584- Neo Skunkworks - Paul Xu modifyed - 001 Begin */
            WeatherView wView = new WeatherView(this, null);
            wView = (WeatherView) view.findViewById(R.id.weatherview);
            /* CR 484584- Neo Skunkworks - Paul Xu modifyed - 001 End */

            /*
             * PR729705 The hourlyactivity is not hide when the value of
             * def_weather_hide_hourlyactivity is 1 by jielong.xing at 2014-07-15 begin
             */
            if (!isHourlyActivityHide()) {
                wView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // CR 568320- Neo Skunkworks - Wells Tang - 001 Begin
                        if (mCitys == null || mCitys.size() == 0) {
                            return;
                        }
                        // CR 568320- Neo Skunkworks - Wells Tang - 001 End

                        Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
                        intent.putExtra("locationKey", mCitys.get(mPosition).getLocationKey());
                        intent.putExtra("cityName", mCitys.get(mPosition).getCityName());

                        startActivity(intent);
                        // finish();

                    }
                });
            }
            /*
             * PR729705 The hourlyactivity is not hide when the value of
             * def_weather_hide_hourlyactivity is 1 by jielong.xing at 2014-07-15 end
             */
            days[0] = (DayView) view.findViewById(R.id.dv_day1);
            days[1] = (DayView) view.findViewById(R.id.dv_day2);
            days[2] = (DayView) view.findViewById(R.id.dv_day3);
            days[3] = (DayView) view.findViewById(R.id.dv_day4);

            // PR 466448 - Neo Skunkworks - Tom Yu - 001 begin
            int nWeatherInfoSize = dayForShow.size();
            // PR 466448 - Neo Skunkworks - Tom Yu - 001 end

            mCitys = myService.checkDataBase();

            String newUpdateTime = null;
            long time = Long.parseLong(mCitys.get(i).getUpdateTime());
            SimpleDateFormat format12 = new SimpleDateFormat("hh:mmaa");
            SimpleDateFormat format24 = new SimpleDateFormat("HH:mm");

            // PR792049 Wrong date format used in weather app by jielong.xing at 2014-09-19 begin
            java.text.SimpleDateFormat format = (java.text.SimpleDateFormat) DateFormat
                    .getDateFormat(this);
            String pattern = format.toPattern();
            int monthIndex = pattern.indexOf("M");
            int dayIndex = pattern.indexOf("d");
            if (monthIndex > dayIndex) {
                int lastMonthIndex = pattern.lastIndexOf("M");
                pattern = pattern.substring(dayIndex, lastMonthIndex + 1);
            } else if (dayIndex > monthIndex) {
                int lastDayIndex = pattern.lastIndexOf("d");
                pattern = pattern.substring(monthIndex, lastDayIndex + 1);
            }
            String language = getResources().getConfiguration().locale.getCountry();
            if ("de".equals(language.toLowerCase())) {
                if (!pattern.toUpperCase().contains("MMM")) {
                    pattern = pattern + ".";
                }
            }
            java.util.Calendar currentTime = java.util.Calendar.getInstance();
            currentTime.setTimeInMillis(time);
            if (DateFormat.is24HourFormat(this)) {
                newUpdateTime = DateFormat.format(pattern, currentTime) + " "
                        + format24.format(time);
            } else {
                newUpdateTime = DateFormat.format(pattern, currentTime) + " "
                        + format12.format(time);
            }
            // PR792049 Wrong date format used in weather app by jielong.xing at 2014-09-19 end

            wView.setWeatherData(weatherForShow, isUnitC, newUpdateTime, mCitys.get(i)
                    .isAutoLocate());

            // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
            for (int j = 0; j < 4; j++) {
                if (nWeatherInfoSize > j + 1) {
                    days[j].setDay(dayForShow.get(j + 1), isUnitC);
                    final String linkUrl = dayForShow.get(j + 1).getUrl();

                    days[j].setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showForecastWebView(linkUrl);
                        }
                    });
                }
            }
        }

        mAdapter = new PagerAdapter() {

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                mPosition = position;
                super.setPrimaryItem(container, position, object);
            }

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                if (position < mViews.size()) {
                    ((ViewPager) container).removeView(mViews.get(position));
                }
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ((ViewPager) container).addView(mViews.get(position));
                return mViews.get(position);
            }

            @Override
            public void finishUpdate(ViewGroup container) {
                for (int i = 0; i < mCitys.size(); i++) {
                    if (i == mPosition) {
                        mImageViews.get(i).setBackgroundResource(R.drawable.point_light);
                    } else {
                        mImageViews.get(i).setBackgroundResource(R.drawable.point_dark);
                    }
                }
            }

            // PR781233、PR778618[Monitor][Force Close][Weather]The weather happened force close when
            // change the city by jielong.xing at 2014-09-05 begin
            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
            // PR781233、PR778618[Monitor][Force Close][Weather]The weather happened force close when
            // change the city by jielong.xing at 2014-09-05 end
        };

        mViewPager.setAdapter(mAdapter);
        // add by jielong.xing at 2014-10-17 begin
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
            }

        });
        // add by jielong.xing at 2014-10-17 end
        String cityKey = SharePreferenceUtils.getCurrentCityKey(getApplicationContext());
        if (null == mTempKey || mTempKey.equals("")) {
            if (null != cityKey && !"".equals(cityKey)) {
                mTempKey = cityKey;
                mViewPager.setCurrentItem(myService.getCurrentPosition(cityKey));
            } else {
                mViewPager.setCurrentItem(0);
            }
        } else {
            mViewPager.setCurrentItem(myService.getCurrentPosition(mTempKey));
        }

        if (mViewPager != null) {
            int position = mViewPager.getCurrentItem();
            if (position >= 0 && mTitleBackground != null && mTitleBackground.length > 0) {
                mActionBar.setBackgroundDrawable(getResources().getDrawable(
                        mTitleBackground[position]));
            }
        }
    }

    // add by jielong.xing for reskin at 2014-10-17 begin
    private int[] mTitleBackground = null;

    /**
     * get title bar background
     * 
     * @param weatherID
     * @return
     */
    private int getTitleBackground(String weatherID) {
        int weather_icon_id = 0;
        try {
            weather_icon_id = Integer.parseInt(weatherID);
        } catch (Exception e) {
            android.util.Log.e(TAG, "getTitleBackground weatherID = " + weatherID);
            weather_icon_id = 0;
        }
        if (weather_icon_id > 0) {
            if (Config.SUNNY_LIST.contains(weather_icon_id)) {
                if (Config.SUNNY_NIGHT_LIST.contains(weather_icon_id)) {
                    return R.drawable.titel_night_bg;
                }

                return R.drawable.titel_sunny_bg;
            } else if (Config.CLOUDY_LIST.contains(weather_icon_id)) {
                return R.drawable.titel_cloudy_bg;
            } else if (Config.RAIN_LIST.contains(weather_icon_id)) {
                return R.drawable.titel_rain_bg;
            } else if (Config.SNOW_LIST.contains(weather_icon_id)) {
                return R.drawable.titel_snow_bg;
            } else if (Config.FOG_LIST.contains(weather_icon_id)) {
                return R.drawable.titel_snow_bg;
            } else if (Config.FROST_LIST.contains(weather_icon_id)) {
                return R.drawable.titel_cloudy_bg;
            } else if (Config.LIGHTNING_LIST.contains(weather_icon_id)) {
                return R.drawable.titel_rain_bg;
            }
        }
        return R.drawable.titel_sunny_bg;
    }

    // add by jielong.xing for reskin at 2014-10-17 end

    /*
     * PR729705 The hourlyactivity is not hide when the value of def_weather_hide_hourlyactivity is
     * 1 by jielong.xing at 2014-07-15 begin
     */
    private boolean isHourlyActivityHide() {
        return CustomizeUtils.getBoolean(this,
                "def_weather_hide_hourlyactivity");
    }

    /*
     * PR729705 The hourlyactivity is not hide when the value of def_weather_hide_hourlyactivity is
     * 1 by jielong.xing at 2014-07-15 end
     */

    private void showForecastWebView(String uri) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(uri);
        intent.setData(content_url);

        try {
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    // Check the internet connection.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService("connectivity");

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            // add by junye.li for PR762484 begin
            if (!isWifiConnected && !isMobileConnected) {
                isOtherConnected = true;
            }
            // add by junye.li for PR762484 end
        } else {
            isWifiConnected = false;
            isMobileConnected = false;
            isOtherConnected = false; // add by junye.li for PR762484
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar, menu);

        mRefreshItem = menu.findItem(R.id.menu_refresh);
        mRefreshItem.setActionView(R.layout.action_view);
        View refreshView = mRefreshItem.getActionView();
        ImageView refreshIv = (ImageView) refreshView.findViewById(R.id.menu_img);
        refreshIv.setImageResource(R.drawable.refresh);

        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                updateConnectedFlags();
                // modify by junye.li for PR762484 begin
                if (isWifiConnected || isMobileConnected || isOtherConnected) {
                    // modify by junye.li for PR762484 end
                    if (!isUpdating) {
                        isUpdating = true;
                        pDialog.show();

                        /* PR 515884- Neo Skunkworks - Wells Tang - 001 Begin */
                        myService.setUpdateManue();

                        // Modified by Wells Tang 2014-0925,move requestlocation
                        // update to autolocate thread
                        // for we need to scan wifi before request
                        myService.updateWeather();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mShowMenuItem = menu.findItem(R.id.menu_show_menu);
        mShowMenuItem.setActionView(R.layout.action_view);

        View showMenuView = mShowMenuItem.getActionView();
        ImageView showMenuIv = (ImageView) showMenuView.findViewById(R.id.menu_img);
        showMenuIv.setImageResource(R.drawable.menu);
        showMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showPopupMenuLayout();
            }
        });

        return true;
    }

    private View popupMenuLayout = null;

    private void showPopupMenuLayout() {
        if (null == popupMenuLayout) {
            popupMenuLayout = findViewById(R.id.popup_menu_layout);
        }

        View addLocation = popupMenuLayout.findViewById(R.id.menu_addlocation);
        View delLocation = popupMenuLayout.findViewById(R.id.menu_deletelocation);
        View changeUnit = popupMenuLayout.findViewById(R.id.menu_changeunit);
        addLocation.setOnClickListener(this);
        delLocation.setOnClickListener(this);
        changeUnit.setOnClickListener(this);

        TextView tv_menu_unit = (TextView) popupMenuLayout.findViewById(R.id.tv_menu_unit);
        if (isUnitC) {
            tv_menu_unit.setText(getResources().getString(R.string.change_to_F));
        } else {
            tv_menu_unit.setText(getResources().getString(R.string.change_to_C));
        }

        int visible = popupMenuLayout.getVisibility();
        if (visible == View.VISIBLE) {
            popupMenuLayout.setVisibility(View.GONE);
        } else if (visible == View.GONE) {
            popupMenuLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();

            if (null != popupMenuLayout && popupMenuLayout.getVisibility() == View.VISIBLE) {
                Rect hitRect = new Rect();
                popupMenuLayout.getGlobalVisibleRect(hitRect);
                if (!hitRect.contains(x, y)) {
                    popupMenuLayout.setVisibility(View.GONE);
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (popupMenuLayout != null && popupMenuLayout.getVisibility() == View.VISIBLE) {
            popupMenuLayout.setVisibility(View.GONE);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.menu_addlocation:
                if (mCitys.size() >= 11) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.citys_full),
                            Toast.LENGTH_LONG).show();
                } else {
                    startActivity(new Intent(MainActivity.this, LocateActivity.class));
                    MainActivity.this.finish();
                }

                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.menu_deletelocation:
                myService.deleteCity(mCitys.get(mPosition).getLocationKey());

                if (checkDataBase()) {
                    if (mPosition >= mCitys.size()) {
                        if (mCitys.size() != 0) {
                            mTempKey = mCitys.get(0).getLocationKey();
                        }
                    } else {
                        mTempKey = mCitys.get(mPosition).getLocationKey();
                    }
                    initViewPager();
                }

                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.menu_changeunit:
                android.util.Log.e("xjl", "changeunit");
                if (mCitys == null || mCitys.size() == 0) {
                    return;
                }
                isUnitC = !isUnitC;

                Editor sharedata = getSharedPreferences("weather",
                        Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE).edit();
                sharedata.putBoolean("unit", isUnitC);
                sharedata.commit();

                Intent it = new Intent("android.intent.action.UNIT_BROADCAST");
                it.putExtra("isUnitC", isUnitC);
                sendBroadcast(it);

                mTempKey = mCitys.get(mPosition).getLocationKey();
                initViewPager();
                if (popupMenuLayout != null) {
                    popupMenuLayout.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    private boolean isPad = false;

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (!isPad) {
            showPopupMenuLayout();
        }
        return false;
    }

    /**
     * show the main menu when menu key be pressed
     * 
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!event.isCanceled()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    if (isPad) {
                        showPopupMenuLayout();
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
