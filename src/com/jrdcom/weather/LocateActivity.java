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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
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
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.bean.City;
import com.jrdcom.data.MyService;
import com.jrdcom.util.CommonUtils;
import com.jrdcom.util.SharePreferenceUtils;
import com.jrdcom.widget.UpdateWidgetTimeService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocateActivity extends Activity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "LocateActivity";

    public static final String CITY_BROADCAST = "android.intent.action.CITY_BROADCAST";
    public static final String WEATHER_BROADCAST = "android.intent.action.WEATHER_BROADCAST";

    private Button bt_locate;
    private MyService myService;
    private boolean isWifiConnected;
    private boolean isMobileConnected;
    private CityBroadcasReceiver mBroadcastReceiver;
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
    private LocationListener mGpsListener = null;
    private LocationListener mNetworkListener = null;
    private Location mGpsLocation = null;
    private Location mNetworkLocation = null;
    private boolean mAutoLocateSuccess = false;

    private static final int MSG_TIME_OUT = 0x1001;
    private static final int MSG_REGET_POSITION = 0x1002;
    private static final int MSG_REQUEST_LOCATION_UPDATE = 0x1003;
    private static final int MSG_REMOVE_LOCATEION_UPDATE = 0x1004;

    private boolean isRequestLocationUpdate = false;
    private boolean isNetworkProvideEnable = true;
    private boolean isGPSProvideEnable = true;
    private boolean isNetworkRequestOpen = false;
    private boolean isGPSRequestOpen = false;
    private boolean isOtherConnected;

    private String mSearchCity;

    private boolean isBindService = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("aidy", "onCreate()");
        if (!CommonUtils.isSupportHorizontal(this)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.add_location);
        startService(new Intent(LocateActivity.this, UpdateWidgetTimeService.class));
        initViews();

        pDialog = new ProgressDialog(LocateActivity.this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mGpsListener = new GpsLocationListener();
        mNetworkListener = new NetworkLocationListener();
    }

    private void initViews() {
        this.getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.titel_night_bg));
        bt_locate = (Button) findViewById(R.id.locate_bt_auto);
        bt_connect = (Button) findViewById(R.id.locate_connect);
        mCityList = (ListView) findViewById(R.id.search_citylist);

        bt_locate.setBackgroundResource(R.drawable.button_bg);
        bt_connect.setBackgroundResource(R.drawable.button_bg);
        layout_main = (LinearLayout) findViewById(R.id.locate_layout_main);
        layout_connect = (LinearLayout) findViewById(R.id.locate_layout_connect);
        mCitys = new ArrayList<City>();

        bt_locate.setOnClickListener(this);
        bt_connect.setOnClickListener(this);
        mCityList.setOnItemClickListener(this);
    }

    private void disMissProgressDlgOrFinish() {
        if (pDialog != null) {
            if (!mPausing) {
                pDialog.dismiss();
            }
        }
    }

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
                Log.i("aidy", "onQueryTextSubmit()");
                updateConnectedFlags();

                String cityName = mSearchCity = mSearchView.getQuery().toString().trim();

                mSearchView.clearFocus();
                if (isWifiConnected || isMobileConnected || isOtherConnected) {
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("aidy", "onResume()");
        mPausing = false;
        registerBoradcastReceiver();
        SharePreferenceUtils.inLocalActivity = true;
        layout_main.setVisibility(View.VISIBLE);
        SharedPreferences sharedata = getSharedPreferences("firstuse", MODE_PRIVATE);
        isFirstUse = sharedata.getBoolean("firstUse", true);

        updateConnectedFlags();

        isBindService = (mCityList != null && mCityList.getCount() == 0);
        if (isBindService) {
            Intent intent = new Intent(LocateActivity.this, MyService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
        if (!isWifiConnected && !isMobileConnected && !isOtherConnected) {
            layout_main.setVisibility(View.GONE);
            layout_connect.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.sendEmptyMessage(MSG_REMOVE_LOCATEION_UPDATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("aidy", "onPause()");
        mPausing = true;
        if (isBindService) {
            unbindService(conn);
        }
        SharePreferenceUtils.inLocalActivity = false;
        unregisterReceiver(mBroadcastReceiver);
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("aidy", "onServiceConnected()");
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

    private void firstUseCheck() {
        Log.i("aidy", "firstUseCheck()");
        if (mCitys.size() == 0) {
            updateConnectedFlags();

            if (isWifiConnected || isMobileConnected || isOtherConnected) {
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
        Log.i("aidy", "checkLocationOn() -- isNetworkProvideEnable = " + isNetworkProvideEnable
                + " -- isGPSProvideEnable = " +
                isGPSProvideEnable + " -- isLocationOn = " + isLocationOn);
        return isLocationOn;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i("aidy", "handleMessage() -- msg.what = " + msg.what);
            switch (msg.what) {
                case MSG_REQUEST_LOCATION_UPDATE: {
                    if (isNetworkProvideEnable) {
                        Log.d("aidy", "LocationActivity mNetworkListener");
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                1000L, 5000, mNetworkListener);
                        isNetworkRequestOpen = true;
                    }

                    if (isGPSProvideEnable) {
                        Log.d("aidy", "LocationActivity mGpsListener");
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                1000L, 0, mGpsListener);
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
                case MSG_TIME_OUT: {
                    Log.e(TAG, "AutoLocateTimeout");
                    Intent it = new Intent("android.intent.action.WEATHER_BROADCAST");
                    it.putExtra("connect_timeout", true);
                    sendBroadcast(it);
                    break;
                }
                case MSG_REGET_POSITION: {
                    if (mHandler != null) {
                        mHandler.removeMessages(MSG_REGET_POSITION);
                    }
                    getLocation();
                    break;
                }
            }
        }
    };

    private void getLocation() {
        Log.i("aidy", "getLocation() -- isRequestLocationUpdate = " + isRequestLocationUpdate);
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
                        if (mAutoLocateSuccess) {
                            updateSuccess = true;
                            break;
                        } else {
                            try {
                                Thread.sleep(3000);
                            } catch (Exception e) {
                            }
                        }
                    }
                    mHandler.sendEmptyMessage(MSG_REMOVE_LOCATEION_UPDATE);

                    if (updateSuccess) {
                        if (isBetterLocation(mGpsLocation, mNetworkLocation)) {
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
                                mHandler.sendEmptyMessage(MSG_TIME_OUT);
                                layout_main.setVisibility(View.VISIBLE);
                                layout_connect.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }.start();
        }
    }

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
        mBroadcastReceiver = new CityBroadcasReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocateActivity.CITY_BROADCAST);
        filter.addAction(LocateActivity.WEATHER_BROADCAST);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private class CityBroadcasReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();
            if (LocateActivity.CITY_BROADCAST.equals(intent.getAction())) {
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
            } else if (LocateActivity.WEATHER_BROADCAST.equals(intent.getAction())) {
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
        Log.i("aidy", "updateConnectedFlags()");
        NetworkInfo activeInfo = ((ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

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

    private ArrayList<HashMap<String, String>> getData(List<City> mCitys, String filter) {

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
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

    private boolean isBetterLocation(Location locationA, Location locationB) {
        if (locationA == null) {
            return false;
        }
        if (locationB == null) {
            return true;
        }
        if (locationA.getElapsedRealtimeNanos() > (locationB.getElapsedRealtimeNanos() + 11 * 1000000000)) {
            return true;
        } else if (locationB.getElapsedRealtimeNanos() > (locationA.getElapsedRealtimeNanos() + 11 * 1000000000)) {
            return false;
        }

        if (!locationA.hasAccuracy()) {
            return false;
        }
        if (!locationB.hasAccuracy()) {
            return true;
        }
        return locationA.getAccuracy() < locationB.getAccuracy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        if (mCitys != null && mCitys.size() > 0) {
            City city = mCitys.get(position);

            myService.setUpdateManue();
            myService.insertCity(city, false);
            SharePreferenceUtils.checkCommonCity(LocateActivity.this, city.getLocationKey());

            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.locate_bt_auto:
                updateConnectedFlags();
                if (isWifiConnected || isMobileConnected || isOtherConnected) {
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
                break;
            case R.id.locate_connect:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
        }
    }

    private class GpsLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            Log.e("aidy", "GPS____onLocationChanged Latitude = "
                    + location.getLatitude() + "Longitude = "
                    + location.getLongitude());
            if (location != null) {
                mGpsLocation = location;
                mAutoLocateSuccess = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

    }

    private class NetworkLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            Log.e("aidy", "Network____onLocationChanged Latitude = "
                    + location.getLatitude() + "Longitude = "
                    + location.getLongitude());
            if (location != null) {
                mNetworkLocation = location;
                mAutoLocateSuccess = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

    }

}
