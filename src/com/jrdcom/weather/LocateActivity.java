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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/LocateActivity.java   */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jrdcom.bean.City;
import com.jrdcom.data.MyService;
import com.jrdcom.util.CommonUtils;
import com.jrdcom.util.SharePreferenceUtils;
import com.jrdcom.widget.UpdateWidgetTimeService;
import com.jrdcom.weather.R;

public class LocateActivity extends Activity {
    private static final String TAG = "LocateActivity";
    private Button bt_locate;
    private MyService myService;
    private boolean isWifiConnected;
    private boolean isMobileConnected;
    private MyBroadcasReceiver mBroadcastReceiver;
    private LocationManager mLocationManager;
    private ProgressDialog pDialog;
    private List<City> mCitys;
    private Button bt_connect;
    private SearchView mSearchView;
    private boolean isFirstUse = true;
    private LinearLayout layout_main, layout_connect;
    private ListView mCityList;
    private Location mLocation;
    private boolean mPausing = false;
    // zhaoyun.wu
    private LocationListener mGpsListener = null;
    private LocationListener mNetworkListener = null;
    private Location mGpsLocation = null;
    private Location mNetworkLocation = null;
    // zhaoyun.wu end
    private boolean mAutoLocateSuccess = false;

    private static final int MSGTIMEOUT = 0x10001; // retry 10 times all
                                                   // failed,then send timeout
                                                   // message
    private static final int MSGREGETPOSITION = 0x10002; // retry message

    private boolean isOtherConnected; // add by junye.li for PR762484

    private String mSearchCity; // add by junye.li for PR787604

    private boolean isBindService = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!CommonUtils.isSupportHorizontal(this)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.add_location);

        // PR 628746 - Neo Skunkworks - Wells Tang - 001 begin
        // when enter weather,start the service to avoid service died
        // startService(new Intent(LocateActivity.this, MyService.class));
        startService(new Intent(LocateActivity.this, UpdateWidgetTimeService.class));
        // PR 628746 - Neo Skunkworks - Wells Tang - 001 end

        this.getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.titel_night_bg));

        bt_locate = (Button) findViewById(R.id.locate_bt_auto);
        bt_connect = (Button) findViewById(R.id.locate_connect);

        bt_locate.setBackgroundResource(R.drawable.button_bg);
        bt_connect.setBackgroundResource(R.drawable.button_bg);

        layout_main = (LinearLayout) findViewById(R.id.locate_layout_main);
        layout_connect = (LinearLayout) findViewById(R.id.locate_layout_connect);

        bt_locate.setOnClickListener(locateListener);

        // pr 459968,462102 by yamin.cao@tct-nj.com end
        bt_connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });

        pDialog = new ProgressDialog(LocateActivity.this);

        mCitys = new ArrayList<City>();

        mCityList = (ListView) findViewById(R.id.search_citylist);

        mCityList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (mCitys != null && mCitys.size() > 0) {
                    City city = mCitys.get(arg2);

                    myService.setUpdateManue();
                    myService.insertCity(city, false);
                    SharePreferenceUtils.checkCommonCity(LocateActivity.this, city.getLocationKey());

                    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setMessage(getResources().getString(R.string.loading));
                    pDialog.show();
                }
            }
        });

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // zhaoyun.wu begin
        mGpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(TAG, "GPS____onLocationChanged Latitude = "
                        + location.getLatitude() + "Longitude = "
                        + location.getLongitude());
                if (location != null) {
                    mGpsLocation = location;
                    mAutoLocateSuccess = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
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
                    mAutoLocateSuccess = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        // zhaoyun.wu end
    }

    // dismiss the progress dlg if activity not pausing,else
    // finish the activity,locate activity do not need to keep activity on
    // background
    private void disMissProgressDlgOrFinish() {
        if (pDialog != null) {
            if (!mPausing) {
                pDialog.dismiss();
            }
            // PR854784 [Monitor][Weather]DUT automatic return to the home screen after press the
            // power button twice in city searching interface by jielong.xing at 2014-12-6 begin
            // else {
            // finish();
            // }
            // PR854784 [Monitor][Weather]DUT automatic return to the home screen after press the
            // power button twice in city searching interface by jielong.xing at 2014-12-6 end
        }
    }

    OnClickListener locateListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            updateConnectedFlags();

            // modify by junye.li for PR762484 begin
            if (isWifiConnected || isMobileConnected || isOtherConnected) {
                // modify by junye.li for PR762484 end
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setMessage(getResources().getString(R.string.locating));
                pDialog.show();

                getLocation();
            } else {
                Toast.makeText(LocateActivity.this,
                        getResources().getString(R.string.locate_connect_error),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_actionbar, menu);

        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        mSearchView.setIconified(false);
        mSearchView.setVisibility(View.GONE);
        mSearchView.setQueryHint(getResources().getString(R.string.insert_location));

        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                updateConnectedFlags();

                // modify by junye.li for PR787604 begin
                String cityName = mSearchCity = mSearchView.getQuery().toString().trim();
                // modify by junye.li for PR787604 end

                mSearchView.clearFocus();
                // modify by junye.li for PR762484 begin
                if (isWifiConnected || isMobileConnected || isOtherConnected) {
                    // modify by junye.li for PR762484 end
                    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setMessage(getResources().getString(R.string.searching));
                    pDialog.show();

                    getCityListFromService(cityName);
                } else {
                    Toast.makeText(LocateActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() != 0) {
                    bt_locate.setVisibility(View.GONE);
                } else {
                    mCityList.setVisibility(View.GONE);
                    bt_locate.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void getCityListFromService(String cityName) {
        if (cityName != null && cityName.length() != 0) {
            myService.sendCityFindRequest(cityName);
        } else {
            Toast.makeText(LocateActivity.this,
                    getResources().getString(R.string.insert_location),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * private WifiManager mWifiManager;
     *//**
     * scan the wifi mac for the provider to autolocate
     * 
     * @return
     */
    /*
     * private boolean startScanWifi() { boolean ret = false; try { int wifiScanAways =
     * Settings.Global.getInt(getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0);
     * if (wifiScanAways == 1) { if (mWifiManager == null) { mWifiManager = (WifiManager)
     * getSystemService(Context.WIFI_SERVICE); } if (mWifiManager != null) {
     * mWifiManager.startScan(); ret = true; } } } catch (SettingNotFoundException e) { Log.d(TAG,
     * "settings not found"); e.printStackTrace(); } return ret; }
     */

    @Override
    protected void onResume() {
        super.onResume();

        mPausing = false;

        /* PR 637010- Neo Skunkworks - Wells Tang added - 001 Begin */
        registerBoradcastReceiver();
        /* PR 637010- Neo Skunkworks - Wells Tang added - 001 End */

        SharePreferenceUtils.inLocalActivity = true;

        layout_main.setVisibility(View.VISIBLE);

        SharedPreferences sharedata = getSharedPreferences("firstuse", MODE_PRIVATE);
        isFirstUse = sharedata.getBoolean("firstUse", true);

        isBindService = (mCityList != null && mCityList.getCount() == 0);
        if (isBindService) {
            Intent intent = new Intent(LocateActivity.this, MyService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }

        updateConnectedFlags();

        // modify by junye.li for PR762484 begin
        if (!isWifiConnected && !isMobileConnected && !isOtherConnected) {
            layout_main.setVisibility(View.GONE);
            layout_connect.setVisibility(View.VISIBLE);
        }
    }

    // the broadcast need to listen when onCreate and unregister in onDestroy,
    // to avoid when activity is pausing,then click add location,the activity
    // not
    // refreshed
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.sendEmptyMessage(MSGREMOVELOCATEIONUPDATE);

    }

    @Override
    protected void onPause() {
        super.onPause();

        mPausing = true;

        if (isBindService) {
            unbindService(conn);
        }

        // PR 470670 - Neo Skunkworks - Tom Yu 001 begin
        SharePreferenceUtils.inLocalActivity = false;
        // PR 470670 - Neo Skunkworks - Tom Yu 001 End

        unregisterReceiver(mBroadcastReceiver);
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
                mCitys = myService.checkDataBase();
                if (isFirstUse) {
                    firstUseCheck();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (mCitys.size() > 0) {
            startActivity(new Intent(LocateActivity.this, MainActivity.class));
        }

        finish();
    }

    // When user click the app's icon the first time,turn to auto locate.
    private void firstUseCheck() {
        if (mCitys.size() == 0) {
            updateConnectedFlags();

            // modify by junye.li for PR762484 begin
            if (isWifiConnected || isMobileConnected || isOtherConnected) {
                // modify by junye.li for PR762484 end
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setMessage(getResources().getString(R.string.locating));
                pDialog.show();

                getLocation();
            } else {
                layout_main.setVisibility(View.GONE);
                layout_connect.setVisibility(View.VISIBLE);
            }
        }
    }

    // zhaoyun.wu begin
    private boolean checkLocationOn() {
        boolean isLocationOn = (Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF);
        PackageManager pm = getApplicationContext().getPackageManager();
        isNetworkProvideEnable = (mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK));
        isGPSProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS));
        return isLocationOn;
    }

    // zhaoyun.wu end

    private static final int MSG_REQUEST_LOCATION_UPDATE = 0x1001;
    private static final int MSG_REMOVE_LOCATEION_UPDATE = 0x1002;
    private boolean isRequestLocationUpdate = false;
    private boolean isNetworkProvideEnable = true;
    private boolean isGPSProvideEnable = true;

    private boolean isNetworkRequestOpen = false;
    private boolean isGPSRequestOpen = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REQUEST_LOCATION_UPDATE: {
                    if (isNetworkProvideEnable) {
                        Log.d("jielong", "LocationActivity mNetworkListener");
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                10000, 1, mNetworkListener);
                        isNetworkRequestOpen = true;
                    }

                    if (isGPSProvideEnable) {
                        Log.d("jielong", "LocationActivity mGpsListener");
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                10000, 1, mGpsListener);
                        isGPSRequestOpen = true;
                    }
                    isRequestLocationUpdate = true;
                    break;
                }
                case MSG_REMOVE_LOCATEION_UPDATE: {
                    if (isNetworkRequestOpen) {
                        if (mNetworkListener != null) {
                            Log.d("jielong", "LocationActivity removeUpdates NetworkListener");
                            mLocationManager.removeUpdates(mNetworkListener);
                        }
                        isNetworkRequestOpen = false;
                    }
                    if (isGPSRequestOpen) {
                        if (mGpsListener != null) {
                            Log.d("jielong", "LocationActivity removeUpdates mGpsListener");
                            mLocationManager.removeUpdates(mGpsListener);
                        }
                        isGPSRequestOpen = false;
                    }
                    isRequestLocationUpdate = false;
                    break;
                }
            }
        }
    };

    private void getLocation() {
        if (!checkLocationOn()) {
            disMissProgressDlgOrFinish();

            if (!mPausing) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.locate_location_service)
                        .setPositiveButton(getResources().getString(R.string.common_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        startActivity(new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.common_cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {

                                    }
                                }).show();
            }
        } else {
            if (isRequestLocationUpdate) {
                return;
            }
            new Thread() {
                public void run() {
                    mHandler.sendEmptyMessage(MSG_REQUEST_LOCATION_UPDATE);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }

                    boolean updateSuccess = false;
                    for (int i = 0; i < 60; i++) {
                        if (!mAutoLocateSuccess) {
                            try {
                                Thread.sleep(3000);
                            } catch (Exception e) {
                            }
                        } else {
                            updateSuccess = true;
                            break;
                        }
                    }

                    mHandler.sendEmptyMessage(MSG_REMOVE_LOCATEION_UPDATE);

                    if (updateSuccess) {
                        if (isBetterLocation(mGpsLocation, mNetworkLocation)) {
                            Log.e(TAG, "zhaoyun.wu____isBetterLocation=true");
                            mLocation = mGpsLocation;
                        } else {
                            mLocation = mNetworkLocation;
                        }

                        if (mLocation != null) {
                            updateLocation(mLocation);
                        }
                    } else {
                        LocateActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                handler.sendEmptyMessage(MSGTIMEOUT);
                                layout_main.setVisibility(View.VISIBLE);
                                layout_connect.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }.start();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSGTIMEOUT: {
                    Log.e(TAG, "AutoLocateTimeout");
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
                    break;
                }
                case MSGREGETPOSITION: {
                    if (handler != null) {
                        handler.removeMessages(MSGREGETPOSITION);
                    }
                    getLocation();
                    break;
                }
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void updateLocation(Location mLocation) {
        isFirstUse = false;
        Editor sharedata = getSharedPreferences("firstuse", MODE_PRIVATE).edit();
        sharedata.putBoolean("firstUse", isFirstUse);
        sharedata.commit();
        getLocationData(mLocation);
    }

    private void getLocationData(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            myService.setUpdateManue();
            myService.autoLocate(latitude, longitude);
        }
    }

    private void registerBoradcastReceiver() {
        mBroadcastReceiver = new MyBroadcasReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("android.intent.action.CITY_BROADCAST");
        myIntentFilter.addAction("android.intent.action.WEATHER_BROADCAST");
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private class MyBroadcasReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();
            if (intent.getAction().equals("android.intent.action.CITY_BROADCAST")) {
                boolean isCityGot = b.getBoolean("city");
                if (isCityGot) {
                    myService.setUpdateManue();
                    mCitys = myService.getCitys();

                    bt_locate.setVisibility(View.GONE);
                    mCityList.setVisibility(View.VISIBLE);
                    mCityList.setAdapter(new SimpleAdapter(context, getData(
                            mCitys, mSearchCity), R.layout.citylist_layout,
                            new String[] {
                                    "cityName"
                            },
                            new int[] {
                                    R.id.cityListitem
                            }));
                } else {
                    Toast.makeText(LocateActivity.this,
                            getResources().getString(R.string.connot_find_location),
                            Toast.LENGTH_LONG).show();
                }
                disMissProgressDlgOrFinish();
            } else if ("android.intent.action.WEATHER_BROADCAST".equals(intent.getAction())) {
                String newLocationKey = b.getString("location_key");

                boolean manu = b.getBoolean("manu", false);

                if (manu && newLocationKey != null) {
                    if (pDialog.isShowing()) {
                        disMissProgressDlgOrFinish();
                    }

                    // when the locateactivity in background,do not jump to mainactivity
                    if (!mPausing) {
                        Intent i = new Intent();
                        i.putExtra("newCityKey", newLocationKey);
                        i.setClass(LocateActivity.this, MainActivity.class);
                        startActivity(i);
                    }

                    LocateActivity.this.finish();
                }

                boolean connect_faild = b.getBoolean("connect_faild");
                if (connect_faild) {
                    disMissProgressDlgOrFinish();

                    Toast.makeText(LocateActivity.this,
                            getResources().getString(R.string.locate_connect_error),
                            Toast.LENGTH_LONG).show();
                }

                boolean connect_timeout = b.getBoolean("connect_timeout");
                if (connect_timeout) {
                    disMissProgressDlgOrFinish();

                    Toast.makeText(LocateActivity.this,
                            getResources().getString(R.string.connect_timeout),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

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

    // modify by junye.li for PR787604 begin
    private ArrayList<HashMap<String, String>> getData(List<City> mCitys, String filter) {

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        // modify by jielong.xing for PR787604 begin
        int citySize = 0;
        if (mCitys != null && mCitys.size() > 0) {
            citySize = mCitys.size();
        }

        for (int i = 0; i < citySize;) {
            City city = mCitys.get(i);
            if (filter != null) {
                String cityName = city.getCityInfoForList();
                String locationKey = city.getLocationKey();
                if (null != locationKey && locationKey.startsWith("postalCode")) {
                    if (locationKey.contains(filter)) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("cityName", city.getCityInfoForList());
                        list.add(map);
                        i++;
                        continue;
                    }
                }
                if (!cityName.replaceAll(" ", "").toLowerCase()
                        .contains(filter.replaceAll(" ", "").toLowerCase())) {
                    mCitys.remove(city);
                    citySize--;
                    continue;
                }
            }
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("cityName", city.getCityInfoForList());
            list.add(map);

            i++;
        }

        return list;
    }

    // modify by junye.li for PR787604 end

    // zhaoyun.wu begin
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
    // zhaoyun.wu end
}
