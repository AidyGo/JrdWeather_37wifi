package com.jrdcom.data;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.bean.Day;
import com.jrdcom.bean.HalfDay;
import com.jrdcom.bean.Hour;

public class ForecastWeatherRequest {
	private static final String APIKEY = "af7408e9f4d34fa6a411dd92028d4630";
	
	public static List<Hour> get24HourForecastWeather(String locationKey, String lang) throws ClientProtocolException, IOException, JSONException, ParseException {
		StringBuffer sb = new StringBuffer();
		sb.append("http://api.accuweather.com/forecasts/v1/hourly/24hour/");
		sb.append(locationKey).append(".json?");
		sb.append("apikey=").append(APIKEY);
		sb.append("&language=").append(lang);
		
		Log.d("jielong", "get24HourForecastWeather url: " + sb.toString());
		
		List<Hour> hourList = new ArrayList<Hour>();
		
		BasicHttpParams httpParameters = new BasicHttpParams();
        
        HttpConnectionParams.setConnectionTimeout(httpParameters,20000);
        HttpConnectionParams.setSoTimeout(httpParameters, 20000);  
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        
        HttpGet httpRequest = new HttpGet(sb.toString());   
        HttpResponse response=httpClient.execute(httpRequest);
        int ret =response.getStatusLine().getStatusCode();
        Log.d("jielong", "get24HourForecastWeather ret: " + ret);
        if (ret == 200) {
        	String strEntity = EntityUtils.toString(response.getEntity());
        	Log.d("jielong", "get24HourForecastWeather strEntity: " + strEntity);
        	if (!TextUtils.isEmpty(strEntity)) {
        		JSONArray resultArray = new JSONArray(strEntity);
        		if (resultArray != null && resultArray.length() > 0) {
        			int len = resultArray.length();
        			for (int i = 0; i < len; i ++) {
        				JSONObject hourlyObj = resultArray.getJSONObject(i);
        				if (hourlyObj != null) {
	        				Hour hour = new Hour();
        					String dateStr = hourlyObj.optString("DateTime");
        					dateStr = dateStr.replaceAll(":", "");
        					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HHmmssz");
        					Date date = format.parse(dateStr);
        					SimpleDateFormat timeFormat = new SimpleDateFormat("hh aa", Locale.ENGLISH);
        					String time = timeFormat.format(date);
        					String icon = hourlyObj.optString("WeatherIcon");
        					String phrase = hourlyObj.optString("IconPhrase");
        					JSONObject tempObj = hourlyObj.optJSONObject("Temperature");
        					hour.setWeatherIcon(icon);
        					hour.setTemperature(tempObj.optString("Value"));
        					hour.setTxtshort(phrase);
        					hour.setTime(time);
        					hourList.add(hour);
        				}else {
        					return new ArrayList<Hour>();
        				}
        			}
        		} else {
        			return new ArrayList<Hour>();
        		}
        	}
        }
		return hourList;
	}
	
	public static List<Day> getDailyForecastWeather(String locationKey, String lang) throws ClientProtocolException, IOException, JSONException {
		StringBuffer sb = new StringBuffer();
		sb.append("http://api.accuweather.com/forecasts/v1/daily/5day/");
		sb.append(locationKey).append(".json?");
		sb.append("apikey=").append(APIKEY);
		sb.append("&language=").append(lang);
		
		Log.d("jielong", "getDailyForecastWeather url: " + sb.toString());
		
		List<Day> dayList = new ArrayList<Day>();
		
		BasicHttpParams httpParameters = new BasicHttpParams();
        
        HttpConnectionParams.setConnectionTimeout(httpParameters,20000);
        HttpConnectionParams.setSoTimeout(httpParameters, 20000);  
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        
        HttpGet httpRequest = new HttpGet(sb.toString());   
        HttpResponse response=httpClient.execute(httpRequest);
        int ret =response.getStatusLine().getStatusCode();
        Log.d("jielong", "getDailyForecastWeather ret: " + ret);
        if (ret == 200) {
        	String strEntity = EntityUtils.toString(response.getEntity());
        	Log.d("jielong", "getDailyForecastWeather strEntity: " + strEntity);
        	if (!TextUtils.isEmpty(strEntity)) {
        		JSONObject resultObj = new JSONObject(strEntity);
        		if (resultObj != null) {
        			JSONArray array = resultObj.getJSONArray("DailyForecasts");
        			if (null != array && array.length() > 0) {
        				int len = array.length();
        				for (int i = 0; i < len; i ++) {
        					JSONObject obj = array.getJSONObject(i);
        					if (obj != null) {
        							        						
        						String dateStr = obj.optString("Date");
        						if (dateStr != null) {
        							dateStr = dateStr.substring(0, 10);	        							
        						}
        						JSONObject dayObj = obj.optJSONObject("Day");	        						
        						JSONObject minTempObj = obj.optJSONObject("Temperature").optJSONObject("Minimum");
        						JSONObject maxTempObj = obj.optJSONObject("Temperature").optJSONObject("Maximum");
        						Day day = new Day();
        						day.setDaycode(getWeek(dateStr));
        						day.setUrl(obj.optString("MobileLink"));
        						HalfDay dayTime = new HalfDay();
        						dayTime.setHightemperature(maxTempObj.optString("Value"));
        						dayTime.setWeathericon(dayObj.optString("Icon"));
        						dayTime.setTxtshort(dayObj.optString("IconPhrase"));
        						HalfDay nightTime =new HalfDay();
        						nightTime.setLowtemperature(minTempObj.optString("Value"));
        						day.setDaytime(dayTime);
        						day.setNightday(nightTime);
        						day.setObsdate(dateStr);
        						dayList.add(day);
        					}
        				}
        			}
        		}
        	}	        	
        } 
		return dayList;
	}
	
	private static String getWeek(String date) {
		Calendar calendar = Calendar.getInstance();
		String[] str = date.split("-");
        
        int year = Integer.parseInt(str[0]);
        int month = Integer.parseInt(str[1]);
        int day = Integer.parseInt(str[2]);
        calendar.set(year,month-1,day);
        int number = calendar.get(Calendar.DAY_OF_WEEK)-1;
        String[] weekStr = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday",};
        return weekStr[number];
	}

}
