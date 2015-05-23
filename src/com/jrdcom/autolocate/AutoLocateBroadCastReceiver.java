/**************************************************************************************************/
/*                                                                     Date : 04/2013 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2012 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  Chen Ting                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/autolocate/AutoLocateBroadCastReceiver.java */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.autolocate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jrdcom.data.MyService;
import com.jrdcom.widget.UpdateWidgetTimeService;

import android.util.Log;

public class AutoLocateBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // context.startService(new Intent(context , AutoLocateService.class));
        // add by wells
        if (intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            Intent myServiceIntent = new Intent(context, MyService.class);
            myServiceIntent.putExtra("boot_complete", true);
            context.startService(myServiceIntent);
        }
        else
        {
            android.util.Log.d("jielong", "AutoLocateBroadCastReceiver");
            context.startService(new Intent(context, MyService.class));
        }

        context.startService(new Intent(context, UpdateWidgetTimeService.class));
        // add end;
    }

}
