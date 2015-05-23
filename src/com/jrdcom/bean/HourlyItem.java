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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/HourlyItem.java          */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

import android.widget.ImageView;
import android.widget.TextView;

public class HourlyItem {
	public TextView getWeek() {
		return week;
	}

	public void setWeek(TextView week) {
		this.week = week;
	}

	public TextView getTime() {
		return time;
	}

	public void setTime(TextView time) {
		this.time = time;
	}

	public ImageView getIcon() {
		return icon;
	}

	public void setIcon(ImageView icon) {
		this.icon = icon;
	}

	public TextView getTemp() {
		return temp;
	}

	public void setTemp(TextView temp) {
		this.temp = temp;
	}

	public TextView getText() {
		return text;
	}

	public void setText(TextView text) {
		this.text = text;
	}

	private TextView week;
	private TextView time;
	private ImageView icon;
	private TextView temp;
	private TextView text;
}
