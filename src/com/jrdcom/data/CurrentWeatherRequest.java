package com.jrdcom.data;

import java.io.IOException;

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

import com.jrdcom.bean.Currentconditions;

public class CurrentWeatherRequest {
	private static final String APIKEY = "af7408e9f4d34fa6a411dd92028d4630";
	
	public static Currentconditions getCurrentWeather(String locationKey, String lang) throws ClientProtocolException, IOException, JSONException {
		StringBuffer sb = new StringBuffer();
		sb.append("http://api.accuweather.com/currentconditions/v1/");
		sb.append(locationKey).append(".json?");
		sb.append("apikey=").append(APIKEY);
		sb.append("&details=true");
		sb.append("&language=").append(lang);
		
		Log.d("jielong", "getCurrentWeather url: " + sb.toString());
		
		Currentconditions current = new Currentconditions();
		
		BasicHttpParams httpParameters = new BasicHttpParams();
        
        HttpConnectionParams.setConnectionTimeout(httpParameters,20000);
        HttpConnectionParams.setSoTimeout(httpParameters, 20000);  
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        
        HttpGet httpRequest = new HttpGet(sb.toString());   
        HttpResponse response=httpClient.execute(httpRequest);
        int ret =response.getStatusLine().getStatusCode();
        Log.d("jielong", "getCurrentWeather ret: " + ret);
        if (ret == 200) {
        	String strEntity = EntityUtils.toString(response.getEntity());
        	Log.d("jielong", "getCurrentWeather strEntity: " + strEntity);
        	if (!TextUtils.isEmpty(strEntity)) {
        		try {
        			JSONArray resultArray = new JSONArray(strEntity);
        			if (resultArray != null && resultArray.length() > 0) {
            			JSONObject obj = resultArray.getJSONObject(0);
            			if (obj != null) {
            				String icon = obj.optString("WeatherIcon");
            				String phrase = obj.optString("WeatherText");
            				JSONObject tempObj = obj.optJSONObject("Temperature").optJSONObject("Imperial");
            				String temp = tempObj.optString("Value");
            				JSONObject realfellObj = obj.optJSONObject("RealFeelTemperature").optJSONObject("Imperial");
            				String realfeel = realfellObj.optString("Value");
            				
//            				JSONObject rangeObj = obj.optJSONObject("TemperatureSummary").optJSONObject("Past12HourRange");
//            				JSONObject minTempObj = rangeObj.optJSONObject("Minimum").optJSONObject("Imperial");
//            				JSONObject maxTempObj = rangeObj.optJSONObject("Maximum").optJSONObject("Imperial");
//            				String minTemp = minTempObj.optString("Value");							
//    						String maxTemp = maxTempObj.optString("Value");
    						current.setWeathericon(icon);
    						current.setWeathertext(phrase);
    						current.setTemperature(temp);
    						current.setRealfeel(realfeel);
            			}
            		} else {
            			return null;
            		}
        		} catch (JSONException ex) {
        			return null;
        		}       		
        	}
        }
		return current;
	}

}
