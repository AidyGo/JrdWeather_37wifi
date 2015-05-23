package com.jrdcom.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

public class Config {
	private static final String TAG = "WeatherWallpaper";

	public final static int WEATHER_SUNNY = 1;
	public final static int WEATHER_RAIN = 2;
	public final static int WEATHER_SNOW = 3;
	public final static int WEATHER_CLOUDY = 4;
	public final static int WEATHER_FOG = 5;
	public final static int WEATHER_FROST = 6;
	public final static int WEATHER_LIGHTNING = 7;

	public static int[] WEATHER_TYPES = { WEATHER_SUNNY, WEATHER_RAIN,
			WEATHER_SNOW, WEATHER_CLOUDY, WEATHER_FOG, WEATHER_FROST,
			WEATHER_LIGHTNING };

	public static Integer[] WEATHER_SUNNY_ID = { 1, 2, 3, 5, 14, 17, 30, 33,
			34, 35, 36, 38 };
	public static Integer[] WEATHER_CLOUDY_ID = { 4, 6, 7, 13, 16, 20 };
	public static Integer[] WEATHER_RAIN_ID = { 12, 18, 25, 26, 29, 39, 40, 41,
			42, 43 };
	public static Integer[] WEATHER_SNOW_ID = { 19, 21, 22, 23, 44 };
	public static Integer[] WEATHER_FOG_ID = { 8, 11, 32, 37 };
	public static Integer[] WEATHER_FROST_ID = { 24, 31 };
	public static Integer[] WEATHER_LIGHTNING_ID = { 15 };
	
	//icons for sunny night
	public static Integer[] WEATHER_SUNNY_NIGHT = {33,34,35,36,38};
	public static List<Integer> SUNNY_NIGHT_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_SUNNY_NIGHT));

	public static List<Integer> SUNNY_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_SUNNY_ID));
	public static List<Integer> CLOUDY_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_CLOUDY_ID));
	public static List<Integer> RAIN_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_RAIN_ID));
	public static List<Integer> SNOW_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_SNOW_ID));
	public static List<Integer> FOG_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_FOG_ID));
	public static List<Integer> FROST_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_FROST_ID));
	public static List<Integer> LIGHTNING_LIST = new ArrayList<Integer>(
			Arrays.asList(WEATHER_LIGHTNING_ID));

}
