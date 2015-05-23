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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/provider/                     */
/*             WeatherContenProvider.java                                             */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.provider;

import java.util.HashMap;

import com.jrdcom.provider.WeatherInfo.CityInfo;
import com.jrdcom.provider.WeatherInfo.Current;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class WeatherContentProvider extends ContentProvider{
    private DBHelper dbHelper;
    private static final UriMatcher sUriMatcher;
    private static final int Weather = 1;
    private static final int Weather_ID = 2;
    private static final int CITY = 3;
    private static final int CITY_ID = 4;
    
    private static HashMap<String, String> WeatherProjection;
    private static HashMap<String, String> cityProjection;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "current", Weather);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "current/#", Weather_ID);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "city", CITY);
        sUriMatcher.addURI(WeatherInfo.AUTHORITY, "city/#", CITY_ID);
        
        WeatherProjection = new HashMap<String, String>();
        WeatherProjection.put(Current.LOCATION_KEY, Current.LOCATION_KEY);
        WeatherProjection.put(Current.ICON, Current.ICON);
        WeatherProjection.put(Current.CURRENT_TEMPERATURE, Current.CURRENT_TEMPERATURE);
        WeatherProjection.put(Current.LOW_TEMPERATURE, Current.LOW_TEMPERATURE);
        WeatherProjection.put(Current.HIGH_TEMPERATURE, Current.HIGH_TEMPERATURE);
        WeatherProjection.put(Current.WEATHER_DESCRIPTION, Current.WEATHER_DESCRIPTION);
        WeatherProjection.put(Current.REALFEEL, Current.REALFEEL);
        
        cityProjection = new HashMap<String, String>();        
        cityProjection.put(CityInfo.LOCATION_KEY, CityInfo.LOCATION_KEY);
        cityProjection.put(CityInfo.CITY_NAME, CityInfo.CITY_NAME);
        cityProjection.put(CityInfo.UPDATE_TIME, CityInfo.UPDATE_TIME);
        cityProjection.put(CityInfo.STATE_NAME, CityInfo.STATE_NAME);//add by shenxin for PR435934
    }
    

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case Weather:
                return Current.CONTENT_TYPE;
            case Weather_ID:
                return Current.CONTENT_ITEM_TYPE;
            case CITY:
                return CityInfo.CONTENT_TYPE;
            case CITY_ID:
                return CityInfo.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknow URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case Weather:
                qBuilder.setTables("current");
                qBuilder.setProjectionMap(WeatherProjection);
                break;
            case Weather_ID:
                qBuilder.setTables("current");
                qBuilder.setProjectionMap(WeatherProjection);
                qBuilder.appendWhere(Current.LOCATION_KEY + "=" + uri.getPathSegments().get(1));
                break;
            case CITY:
                qBuilder.setTables("city");
                qBuilder.setProjectionMap(cityProjection);
                break;
            case CITY_ID:
                qBuilder.setTables("city");
                qBuilder.setProjectionMap(cityProjection);
                qBuilder.appendWhere(CityInfo.LOCATION_KEY + "=" + uri.getPathSegments().get(1));

            default:
                throw new IllegalArgumentException("Uri 错误！ " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qBuilder.query(db, projection, selection, selectionArgs, null, null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case Weather:
                count = db.update("Weather", values, selection, selectionArgs);
                break;
            case Weather_ID:
                String WeatherId = uri.getPathSegments().get(1);
                count = db.update("Weather", values, Current.LOCATION_KEY + "=" + WeatherId + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("错误的 URI" + uri);
                
        }
        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
    }

}
