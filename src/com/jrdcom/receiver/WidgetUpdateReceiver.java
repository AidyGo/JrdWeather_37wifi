/**************************************************************************************************/
/*                                                                     Date : 08/2014 */
/*                            PRESENTATION                                            */
/*              Copyright (c) 2014 JRD Communications, Inc.                           */
/**************************************************************************************************/
/*                                                                                                */
/*    This material is company confidential, cannot be reproduced in any              */
/*    form without the written permission of JRD Communications, Inc.                 */
/*                                                                                                */
/*================================================================================================*/
/*   Author :  jielong.xing                                                            */
/*   Role :   JrdWeather                                                              */
/*================================================================================================*/
/* Comments :                                                                         */
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/receiver/WidgetUpdateReceiver.java     */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.receiver;

import com.jrdcom.widget.UpdateWidgetTimeService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WidgetUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if ("android.intent.action.NEXT_CITY_WIDGET_UPDATE".equals(action)) {
			Intent serviceIntent = new Intent(context, UpdateWidgetTimeService.class);
			serviceIntent.setAction("android.intent.action.NEXT_CITY_WIDGET_UPDATE");
			context.startService(serviceIntent);
		}
	}

}
