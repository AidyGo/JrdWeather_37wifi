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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/provider/WeatherInfo.java     */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class WeatherInfo {
    
    public static final String AUTHORITY = "com.jrdcom.provider.weatherinfo";
    
    public static final class Current implements BaseColumns {
        
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/current");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jrd.provider.weather";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jrd.provider.weather";
        
        public static final String LOCATION_KEY = "locationKey";
        public static final String ICON = "icon";
        public static final String WEATHER_DESCRIPTION = "text";
        public static final String CURRENT_TEMPERATURE = "temp";
        public static final String REALFEEL = "realfeel";
        public static final String LOW_TEMPERATURE = "low";
        public static final String HIGH_TEMPERATURE="high";
    }
    
    public static final class CityInfo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/city");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jrd.provider.city";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jrd.provider.city";
        
        public static final String LOCATION_KEY = "locationKey";
        public static final String CITY_NAME = "cityName";
        public static final String UPDATE_TIME = "updateTime";
        public static final String STATE_NAME = "state";//add by shenxin for PR435934
        
    }
       
}
