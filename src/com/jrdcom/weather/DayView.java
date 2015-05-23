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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/DayView.java          */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.weather;

import java.util.Calendar;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrdcom.bean.DayForShow;

public class DayView extends LinearLayout {
	private Context mContext = null;
	private ImageView iv_icon;
	private TextView tv_temph, tv_templ, tv_week, tv_date;

	private static final String UNITC = "°C";
	private static final String UNITF = "°F";

	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.day_weather_layout, this);

		iv_icon = (ImageView) findViewById(R.id.day_iv_icon);
		tv_temph = (TextView) findViewById(R.id.day_tv_temph);
		tv_templ = (TextView) findViewById(R.id.day_tv_templ);
		tv_week = (TextView) findViewById(R.id.day_tv_week);
		tv_date = (TextView) findViewById(R.id.day_tv_date);
	}

	public void setDay(DayForShow day, boolean isUnitC) {
		String imagepath = "normal_widget_icons_" + (Integer.parseInt(day.getIcon()) < 10? "0" : "") + day.getIcon();
		int resId = getResources().getIdentifier(imagepath, "drawable", "com.jrdcom.weather");
		iv_icon.setBackgroundResource(resId);

		String[] dates = day.getDate().split("-");
		String month = dates[1];
		String daily = dates[2];
		if (month.length() == 1) {
			month = "0".concat(month);
		}

		if (daily.length() == 1) {
			daily = "0".concat(daily);
		}
		java.text.SimpleDateFormat format = (java.text.SimpleDateFormat)DateFormat.getDateFormat(mContext);
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
			pattern = pattern + ".";
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(daily));
		String date = DateFormat.format(pattern, calendar).toString();
		
		String highTag = "H:";
		String lowTag = "L:";
		if ("RU".equals(language)) {
			highTag = "Д:";
			lowTag = "H:";
		}
		
		if (isUnitC) {
			tv_temph.setText(highTag + f2c(day.getTemph()) + UNITC);
			tv_templ.setText(lowTag + f2c(day.getTempl()) + UNITC);
		} else {
			tv_temph.setText(highTag + (int)Double.parseDouble(day.getTemph()) + UNITF);
			tv_templ.setText(lowTag + (int)Double.parseDouble(day.getTempl()) + UNITF);
		}

		// PR345764-Feng.Zhuang-001 Modify begin
		tv_week.setText(getWeekly(day.getWeek()));
		// PR345764-Feng.Zhuang-001 Modify end
		tv_date.setText(date);
	}

	private String f2c(String fahrenheit) {
		try {
			int celsius = ((int)Double.parseDouble(fahrenheit) - 32) * 5 / 9;
			return celsius + "";
		} catch (Exception e) {
			return "";
		}
	}

	// PR345764-Feng.Zhuang-001 Add begin
	private String getWeekly(String sWeek) {
		String[] enWeekly = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri",
				"Sat", "Sun" };
		String result = null;
		String[] weekly = this.getResources().getStringArray(R.array.weather_weekly);
		for (int i = 0; i < 7; i++) {
			if (sWeek.contains(enWeekly[i])) {
				result = weekly[i];
			}
		}
		return result;
	}
	// PR345764-Feng.Zhuang-001 Add end
}