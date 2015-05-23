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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/WeatherView.java      */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.weather;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrdcom.bean.WeatherForShow;
import com.jrdcom.widget.Config;
import com.jrdcom.weather.R;

public class WeatherView extends RelativeLayout {
	private ImageView iv_icon;
	private TextView mWeatherInfor, tv_temp, tv_temph, tv_realfeel,
			tv_temp_unit, tv_city;
	private TextView tv_time;
	private WeatherForShow weatherForShow;

	private static final String UNITC = "°C";
	private static final String UNITF = "°F";

	public WeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.weather, this);

		iv_icon = (ImageView) findViewById(R.id.ac_iv_icon);

		tv_time = (TextView) findViewById(R.id.ac_tv_time);
		tv_city = (TextView) findViewById(R.id.ac_tv_city);

		mWeatherInfor = (TextView) findViewById(R.id.ac_tv_text);
		tv_temp = (TextView) findViewById(R.id.ac_tv_temp);
		tv_temp_unit = (TextView) findViewById(R.id.ac_tv_temp_unit);
		tv_temph = (TextView) findViewById(R.id.ac_tv_high);
		tv_realfeel = (TextView) findViewById(R.id.ac_tv_realfeel_temp);
	}

	public void setWeatherData(WeatherForShow weather, boolean isUnitC,
			String updateTime, boolean isAutoLocation) {
		this.weatherForShow = weather;

		this.setBackgroundResource(getWeatherBackground(weatherForShow.getIcon()));

		String imagepath = "icons_" + (Integer.parseInt(weather.getIcon()) < 10? "0" : "") + weather.getIcon();
		int resId = getResources().getIdentifier(imagepath, "drawable", "com.jrdcom.weather");
		iv_icon.setBackgroundResource(resId);
		tv_time.setText(updateTime);

		// PR351637-Feng.Zhuang-001 Add begin
		tv_city.setText(weather.getCity());
		// PR351637-Feng.Zhuang-001 Add end

		tv_city.setSelected(true); 

		if (isAutoLocation) {
			tv_city.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.app_auto_location_city), null);
		} else {
			tv_city.setPadding(0, 0, 20, 0);
			tv_city.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
		
		String language = getContext().getResources().getConfiguration().locale.getCountry();
		String highTag = "H:";
		String lowTag = "L:";
		if ("RU".equals(language)) {
			highTag = "Д:";
			lowTag = "H:";
		}

		if (isUnitC) {
			tv_temph.setText(highTag + f2c(weather.getTemph()) + UNITC + "\n"
					+ lowTag + f2c(weather.getTempl()) + UNITC);

			tv_temp.setText(f2c(weather.getTemp()));
			tv_temp_unit.setText(UNITC);
			tv_realfeel.setText(getResources().getString(R.string.real_feel)
					+ "® \n" + f2c(weather.getRealfeel()) + UNITC);

		} else {
			tv_temph.setText(highTag + (int)Double.parseDouble(weather.getTemph()) + UNITF + "\n"
					+ lowTag + (int)Double.parseDouble(weather.getTempl()) + UNITF);
			tv_temp.setText((int)Double.parseDouble(weather.getTemp()) + "");
			tv_temp_unit.setText(UNITF);
			tv_realfeel.setText(getResources().getString(R.string.real_feel)
					+ "® \n" + (int)Double.parseDouble(weather.getRealfeel()) + UNITF);
		}
		// PR 466448 - Neo Skunkworks - Wells Tang - 001 End

		// PR345764-Feng.Zhuang-001 Modify begin
		mWeatherInfor.setText(getWeatherText(weather.getIcon()));
		// PR345764-Feng.Zhuang-001 Modify end
	}

	public void changeUnit(boolean isUnitC) {
		if (isUnitC) {
			tv_temph.setText("H:" + f2c(weatherForShow.getTemph()) + UNITC
					+ "\nL:" + f2c(weatherForShow.getTempl()) + UNITC);
			tv_temp.setText(f2c(weatherForShow.getTemp()) + UNITC);
			tv_realfeel.setText(getResources().getString(R.string.real_feel)
					+ " \n" + f2c(weatherForShow.getRealfeel()) + UNITC);
		} else {
			tv_temph.setText("H:" + (int)Double.parseDouble(weatherForShow.getTemph()) + UNITF + "\nL:"
					+ (int)Double.parseDouble(weatherForShow.getTempl()) + UNITF);
			tv_temp.setText((int)Double.parseDouble(weatherForShow.getTemp()) + UNITF);
			tv_realfeel.setText(getResources().getString(R.string.real_feel)
					+ " \n" + (int)Double.parseDouble(weatherForShow.getRealfeel()) + UNITF);
		}
	}

	private String f2c(String fahrenheit) {
		int celsius = ((int)Double.parseDouble(fahrenheit) - 32) * 5 / 9;
		return celsius + "";
	}

	// PR345764-Feng.Zhuang-001 Add begin
	private String getWeatherText(String weatherID) {
		int iconID = Integer.parseInt(weatherID) - 1;
		String[] weatherDescriptions = this.getResources().getStringArray(R.array.weather_icon_desc);
		return weatherDescriptions[iconID];
	}
	// PR345764-Feng.Zhuang-001 Add end

	private int getWeatherBackground(String weatherID) {
		int weather_icon_id = 0;
		try {
			weather_icon_id = Integer.parseInt(weatherID);
		} catch (Exception ex) {
			Log.e("WeatherView", "getWeatherBackground NumberFormatException, weatherId = " + weatherID);
			weather_icon_id = 0;
		}
		if (weather_icon_id > 0) {
			if (Config.SUNNY_LIST.contains(weather_icon_id)) {
				if (Config.SUNNY_NIGHT_LIST.contains(weather_icon_id)) {
					return R.drawable.bg_night;
				}

				return R.drawable.bg_sunny;
			} else if (Config.CLOUDY_LIST.contains(weather_icon_id)) {
				return R.drawable.bg_cloudy;
			} else if (Config.RAIN_LIST.contains(weather_icon_id)) {
				return R.drawable.bg_rain;
			} else if (Config.SNOW_LIST.contains(weather_icon_id)) {
				return R.drawable.bg_snow;
			} else if (Config.FOG_LIST.contains(weather_icon_id)) {
				return R.drawable.bg_snow;
			} else if (Config.FROST_LIST.contains(weather_icon_id)) {
				return R.drawable.bg_cloudy;
			} else if (Config.LIGHTNING_LIST.contains(weather_icon_id)) {
				return R.drawable.bg_rain;
			}
		}
		return R.drawable.bg_sunny;
	}
}
