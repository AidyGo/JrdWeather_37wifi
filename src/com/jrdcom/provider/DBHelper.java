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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/provider/DBHelper.java        */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jrdcom.bean.City;
import com.jrdcom.bean.DayForShow;
import com.jrdcom.bean.HourForShow;
import com.jrdcom.bean.Weather;
import com.jrdcom.bean.WeatherForShow;

public class DBHelper extends SQLiteOpenHelper {

    // private static final String CREATECITY =
    // "CREATE TABLE IF NOT EXISTS city (locationKey VARCHAR PRIMARY KEY NOT NULL , cityName VARCHAR,country VARCHAR,state VARCHAR,updateTime VARCHAR)";//CR
    // 447398 - ting.chen@tct-nj.com - 001 removed
    private static final String CREATECITY = "CREATE TABLE IF NOT EXISTS city (_id INTEGER PRIMARY KEY,locationKey VARCHAR NOT NULL , cityName VARCHAR,country VARCHAR,state VARCHAR,updateTime VARCHAR,isautolocate INTEGER)";
    private static final String CREATECURRENT = "CREATE TABLE IF NOT EXISTS current (locationKey VARCHAR PRIMARY KEY , icon VARCHAR,text VARCHAR,temp VARCHAR,high VARCHAR,low VARCHAR,realfeel VARCHAR,time VARCHAR)";
    private static final String CREATEFORECAST = "CREATE TABLE IF NOT EXISTS forecast (id INTEGER PRIMARY KEY AUTOINCREMENT, locationKey VARCHAR ,dayNum VARCHAR,icon VARCHAR,high VARCHAR,low VARCHAR,week VARCHAR,date VARCHAR,url VARCHAR)";
    private static final String CREATEHOURLY = "CREATE TABLE IF NOT EXISTS hourly (id INTEGER PRIMARY KEY AUTOINCREMENT, locationKey VARCHAR ,week VARCHAR,time VARCHAR,icon VARCHAR,temp VARCHAR,text VARCHAR)";

    private SQLiteDatabase db;
    private static final int VERSION = 2;
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "weather";

    public DBHelper(Context context, String name) {
        super(context, name, null, VERSION);
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATECITY);
        db.execSQL(CREATECURRENT);
        db.execSQL(CREATEFORECAST);
        db.execSQL(CREATEHOURLY);
    }

    public synchronized City getCityByLocationKey(String locationKey) {
        if (!db.isOpen()) {
            return null;
        }
        Cursor c = db.query("city", null, "locationkey = ?",
                new String[] {
                    locationKey
                }, null, null, null, null);// modify by shenxin for PR448288
        if (c == null)
            return null;
        while (c.moveToNext()) {
            String cityName = c.getString(c.getColumnIndex("cityName"));
            String country = c.getString(c.getColumnIndex("country"));
            String state = c.getString(c.getColumnIndex("state"));
            boolean isAutoLocate = c.getInt(c.getColumnIndex("isautolocate")) == 1 ? true : false;
            String updateTime = c.getString(c.getColumnIndex("updateTime"));
            City myCity = new City(locationKey, cityName, country, state, updateTime,
                    isAutoLocate);
            c.close();
            return myCity;
        }
        c.close();
        return null;
    }

    // CR 447398 - ting.chen@tct-nj.com - 001 added end

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized List<City> getCitysFromDatabase() {

        List<City> citys = new ArrayList<City>();
        try {
            Cursor c = db.query("city", new String[] {
                    "locationKey", "cityName",
                    "country", "state", "updateTime", "isautolocate"
            }, null, null,
                    null, null, null, null);
            // CR 447398 - ting.chen@tct-nj.com - 001 added end

            while (c.moveToNext()) {
                String locationKey = c.getString(c.getColumnIndex("locationKey"));
                String cityName = c.getString(c.getColumnIndex("cityName"));
                String country = c.getString(c.getColumnIndex("country"));
                String state = c.getString(c.getColumnIndex("state"));
                String updateTime = c.getString(c.getColumnIndex("updateTime"));
                // CR 447398 - ting.chen@tct-nj.com - 001 added begin
                boolean isAutoLocate = c.getInt(c.getColumnIndex("isautolocate")) == 1 ? true
                        : false;
                citys.add(new City(locationKey, cityName, state, updateTime,
                        country, isAutoLocate));
                // CR 447398 - ting.chen@tct-nj.com - 001 added end
            }

            c.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString()); // PR 466448 - Neo Skunkworks - Tom Yu - 001
        }
        return citys;
    }

    // Get the current weatherinfo
    public WeatherForShow getWeatherForShow(String locationKey) {
        WeatherForShow weather = null;
        if (!db.isOpen()) {
            return null;
        }
        Cursor c = db.rawQuery("SELECT * FROM current WHERE locationKey = ?",
                new String[] {
                    locationKey
                });

        // add by wells,for null pointer protected
        if (c == null) {
            return null;
        }
        // add end;

        while (c.moveToNext()) {
            String icon = c.getString(c.getColumnIndex("icon"));
            String text = c.getString(c.getColumnIndex("text"));
            String temp = c.getString(c.getColumnIndex("temp"));
            String high = c.getString(c.getColumnIndex("high"));
            String low = c.getString(c.getColumnIndex("low"));
            String realfeel = c.getString(c.getColumnIndex("realfeel"));
            String time = c.getString(c.getColumnIndex("time"));

            weather = new WeatherForShow(icon, text, temp, high, low, realfeel,
                    time);
        }
        c.close();

        // PR351637-Feng.Zhuang-001 Add begin
        c = db.rawQuery("SELECT cityName FROM city WHERE locationKey = ?",
                new String[] {
                    locationKey
                });

        // add by wells,for null pointer protected
        if (c == null) {
            return null;
        }
        // add end;

        while (c.moveToNext()) {
            String city = c.getString(c.getColumnIndex("cityName"));

            // PR 466448 - Neo Skunkworks - Tom Yu - 001 begin
            // weather.setCity(city);
            if (null != weather) {
                weather.setCity(city);
            }
            // PR 466448 - Neo Skunkworks - Tom Yu - 001 end
        }
        // PR351637-Feng.Zhuang-001 Add end

        c.close();

        return weather;
    }

    // Get the future weatherinfo
    public List<DayForShow> getDayForShow(String locationKey) {

        List<DayForShow> dayForshow = new ArrayList<DayForShow>();

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return null;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        for (int i = 1; i <= 5; i++) {
            Cursor c = db
                    .rawQuery(
                            "SELECT * FROM forecast WHERE locationKey = ? and dayNum = ?",
                            new String[] {
                                    locationKey, i + ""
                            });

            // add by wells,for null pointer protected
            if (c == null)
            {
                return null;
            }
            // add end

            while (c.moveToNext()) {
                String icon = c.getString(c.getColumnIndex("icon"));
                String high = c.getString(c.getColumnIndex("high"));
                String low = c.getString(c.getColumnIndex("low"));
                String week = c.getString(c.getColumnIndex("week"));
                String date = c.getString(c.getColumnIndex("date"));
                String url = c.getString(c.getColumnIndex("url"));// add by shenxin for PR460544

                dayForshow.add(new DayForShow(icon, high, low, week, date, url));// modify by
                                                                                 // shenxin for
                                                                                 // PR460544
            }
            c.close();
        }

        return dayForshow;
    }

    // Get hourly weather data
    public List<HourForShow> getHourForShow(String locationKey) {
        List<HourForShow> hourForShow = new ArrayList<HourForShow>();
        if (!db.isOpen()) {
            return null;
        }
        Cursor c = db.rawQuery("SELECT * FROM hourly WHERE locationKey = ? ",
                new String[] {
                    locationKey
                });
        if (c == null) {
            return null;
        }
        // add end

        while (c.moveToNext()) {
            String week = c.getString(c.getColumnIndex("week"));
            String time = c.getString(c.getColumnIndex("time"));
            String icon = c.getString(c.getColumnIndex("icon"));
            String temp = c.getString(c.getColumnIndex("temp"));
            String text = c.getString(c.getColumnIndex("text"));

            hourForShow.add(new HourForShow(week, time, icon, temp, text));
        }
        c.close();

        return hourForShow;
    }

    public synchronized void insertCity(City city) {

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return;
        }
        Log.e(TAG, "locationKey = " + city.getLocationKey() + ", cityName = " + city.getCityName());
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        if (!checkDataIfExists("city", city.getLocationKey())) {

            ContentValues values = new ContentValues();

            values.put("locationKey", city.getLocationKey());
            values.put("cityName", city.getCityName());
            values.put("country", city.getCountry());
            values.put("state", city.getState());
            // CR 447398 - ting.chen@tct-nj.com - 001 added begin
            values.put("isautolocate", city.isAutoLocate());
            // CR 447398 - ting.chen@tct-nj.com - 001 added end

            String time = String.valueOf(System.currentTimeMillis());
            values.put("updateTime", time);

            db.insert("city", null, values);
        }
        /* PR 503563 - Neo Skunkworks - Wells Tang - 001 begin */
        // update the time when insert the city again
        else
        {
            ContentValues values = new ContentValues();

            values.put("locationKey", city.getLocationKey());
            values.put("cityName", city.getCityName());
            String time = String.valueOf(System.currentTimeMillis());
            values.put("updateTime", time);

            db.update("city", values, "locationKey=?", new String[] {
                    city.getLocationKey()
            });
        }
        /* PR 503563 - Neo Skunkworks - Wells Tang - 001 end */
    }

    public void updateCityTime() {

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        List<City> citys = getCitysFromDatabase();
        ContentValues values = new ContentValues();
        String time = String.valueOf(System.currentTimeMillis());
        values.put("updateTime", time);

        for (int i = 0; i < citys.size(); i++) {
            db.update("city", values, "locationKey=?", new String[] {
                    citys.get(i).getLocationKey()
            });
        }
    }

    // PR 496643 - Neo Skunkworks - Wells Tang - 001 begin
    // update city exception the autolocate city
    public void updateNotAutoLocateCityTime() {
        if (!db.isOpen()) {
            return;
        }

        List<City> citys = getCitysFromDatabase();
        ContentValues values = new ContentValues();
        String time = String.valueOf(System.currentTimeMillis());
        values.put("updateTime", time);

        for (int i = 0; i < citys.size(); i++) {
            if (!citys.get(i).isAutoLocate()) {
                db.update("city", values, "locationKey=?", new String[] {
                        citys.get(i).getLocationKey()
                });
            }
        }
    }

    // PR 496643 - Neo Skunkworks - Wells Tang - 001 end
    // PR759746 The time sorts wrong after auto location by jielong.xing at 2014-08-08 begin
    public synchronized void updateWeather(Weather weather) {
        if (!db.isOpen()) {
            return;
        }
        ContentValues values;
        values = new ContentValues();
        values.put("locationKey", weather.getLocal().getCityId());
        values.put("icon", weather.getWeatherForShow().getIcon());
        values.put("text", weather.getWeatherForShow().getText());
        values.put("temp", weather.getWeatherForShow().getTemp());
        values.put("high", weather.getWeatherForShow().getTemph());
        values.put("low", weather.getWeatherForShow().getTempl());
        values.put("realfeel", weather.getWeatherForShow().getRealfeel());
        values.put("time", weather.getWeatherForShow().getTime());

        if (!checkDataIfExists("current", weather.getLocal().getCityId())) {
            db.insert("current", null, values);
        } else {
            db.update("current", values, "locationKey = ?",
                    new String[] {
                        weather.getLocal().getCityId()
                    });
        }

        // ---------------------------------------------------------------------------

        for (int i = 0; i < 5; i++) {
            values = new ContentValues();

            String dayNum = (i + 1) + "";
            values.put("locationKey", weather.getLocal().getCityId());
            values.put("icon", weather.getDayForShow().get(i).getIcon());
            values.put("dayNum", dayNum);
            values.put("high", weather.getDayForShow().get(i).getTemph());
            values.put("low", weather.getDayForShow().get(i).getTempl());
            values.put("week", weather.getDayForShow().get(i).getWeek());
            values.put("date", weather.getDayForShow().get(i).getDate());
            values.put("url", weather.getDayForShow().get(i).getUrl());// add by shenxin for
                                                                       // PR460544

            if (!checkDailyIfExists(weather.getLocal().getCityId(), (i + 1)
                    + "")) {
                db.insert("forecast", null, values);
            } else {
                db.update("forecast", values, "locationKey = ?and dayNum=?",
                        new String[] {
                                weather.getLocal().getCityId(), dayNum
                        });
            }
        }

        // ---------------------------------------------------------------------------

        db.delete("hourly", "locationKey=?", new String[] {
                weather.getLocal()
                        .getCityId()
        });
        for (int i = 0; i < 24; i++) {

            values = new ContentValues();
            values.put("locationKey", weather.getLocal().getCityId());
            values.put("week", weather.getHourForShow(i).getWeek());
            values.put("time", weather.getHourForShow(i).getTime());
            values.put("icon", weather.getHourForShow(i).getIcon());
            values.put("temp", weather.getHourForShow(i).getTemp());
            values.put("text", weather.getHourForShow(i).getText());

            db.insert("hourly", null, values);
        }
    }

    public void deleteCity(String locationKey) {

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        db.delete("city", "locationKey = ?", new String[] {
                locationKey
        });
        db.delete("current", "locationKey = ?", new String[] {
                locationKey
        });
        db.delete("forecast", "locationKey = ?", new String[] {
                locationKey
        });
        db.delete("hourly", "locationKey = ?", new String[] {
                locationKey
        });
    }

    // Get strWeatherIcon for ad.
    public String getStrWeatherIcon(String locationKey) {
        String icon = null;

        Cursor c = db.rawQuery(
                "SELECT icon FROM current WHERE locationKey = ?",
                new String[] {
                    locationKey
                });

        // add by wells,for null pointer protected
        if (c == null)
        {
            return null;
        }
        // add end

        while (c.moveToNext()) {
            icon = c.getString(0);
        }
        c.close();

        return icon;
    }

    public boolean checkDataIfExists(String tableName, String locationKey) {

        int count = 0;

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return false;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + tableName
                + " WHERE locationKey = ?", new String[] {
                locationKey
        });

        // add by wells,for null pointer protected
        if (c == null)
        {
            return false;
        }
        // add end

        while (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        return count != 0;
    }

    public boolean checkDailyIfExists(String locationKey, String dayNum) {

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return false;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        int count = 0;
        Cursor c = db
                .rawQuery(
                        "SELECT COUNT(*) FROM forecast WHERE locationKey = ? and dayNum = ?",
                        new String[] {
                                locationKey, dayNum
                        });

        // add by wells,for null pointer protected
        if (c == null)
        {
            return false;
        }
        // add end

        while (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        return count != 0;
    }

    public boolean checkHourlyIfExists(String locationKey, String time) {

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return false;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        int count = 0;
        Cursor c = db
                .rawQuery(
                        "SELECT COUNT(*) FROM hourly WHERE locationKey = ? and time = ?",
                        new String[] {
                                locationKey, time
                        });

        // add by wells,for null pointer protected
        if (c == null)
        {
            return false;
        }
        // add end

        while (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        return count != 0;
    }

    // CR 447398 - ting.chen@tct-nj.com - 001 added begin
    public boolean isFirstCity(String locationKey) {

        // CR 564564- Neo Skunkworks - Wells Tang - 001 Begin
        if (!db.isOpen())
        {
            return false;
        }
        // CR 564564- Neo Skunkworks - Wells Tang - 001 End

        Cursor c = db.query("city", null, "locationkey = ?",
                new String[] {
                    locationKey
                }, null, null, null, null);// modify by shenxin for PR448288
        String id = "";
        if (c == null)
            return false;
        while (c.moveToNext()) {
            id = c.getString(c.getColumnIndex("_id"));
        }
        c.close();
        return Integer.parseInt(id) == 1;
    }
    // CR 447398 - ting.chen@tct-nj.com - 001 added end

}
