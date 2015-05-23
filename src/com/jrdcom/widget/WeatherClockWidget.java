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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/widget/WeatherClockWidget.java*/
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeatherClockWidget extends AppWidgetProvider {

    public static final String UPDATE_VIEW = "com.jrdcom.weather.update";
    // PR 455558 - xin.shen@tct-nj.com - 001 added begin
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().equals("android.intent.action.WEATHERDATA_CLEAN_BROADCAST")){
            context.startService(new Intent(context, UpdateWidgetTimeService.class));
        }
    }
    // PR 455558 - xin.shen@tct-nj.com - 001 added end

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // receive update widget broadcast, send it to UpdateWidgetTimeService
        // to finish it
       
        
        /*PR 468561- Neo Skunkworks - Wells.Tang - 001 start*/
       /* Intent intent = new Intent(UPDATE_VIEW);
        intent.putExtra("appWidgetIds", appWidgetIds);
        context.sendBroadcast(intent);*/
        Intent  intent = new Intent(context,UpdateWidgetTimeService.class);
        intent.setAction(UPDATE_VIEW);
        intent.putExtra("appWidgetIds", appWidgetIds);
        context.startService(intent);
        /*PR 468561- Neo Skunkworks - Wells.Tang - 001 End*/
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, UpdateWidgetTimeService.class));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        context.startService(new Intent(context, UpdateWidgetTimeService.class));
    }

}
