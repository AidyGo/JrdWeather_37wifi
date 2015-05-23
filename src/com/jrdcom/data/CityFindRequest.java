package com.jrdcom.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.bean.City;

public class CityFindRequest {
	private static final String APIKEY = "af7408e9f4d34fa6a411dd92028d4630";
	
	private static final int SINGLE = 0x01;
	private static final int ARRAY = 0x02;
	
	public static List<City> findCityByLocationKey(String locationKey, String lang, boolean withLang) {
		StringBuffer sb = new StringBuffer();
		sb.append("http://api.accuweather.com/locations/v1/");
		sb.append(locationKey);
		sb.append(".json?apikey=");
		sb.append(APIKEY);
		if (withLang) {
			sb.append("&language=").append(lang);
		}
		Log.d("jielong", "findCityByLocationKey url: " + sb.toString());
		return sendFindCityRequest(sb.toString(), SINGLE);	
	}
	
	public static List<City> findCityByGeoLocation(String geolocation, String lang, boolean withLang) {
		StringBuffer sb = new StringBuffer();
		sb.append("http://api.accuweather.com/locations/v1/cities/geoposition/search.json?");
		sb.append("q=").append(geolocation);
		if (withLang) {
			sb.append("&language=").append(lang);
		}		
		sb.append("&apikey=").append(APIKEY);
		Log.d("jielong", "findCityByGeoLocation url: " + sb.toString());
		return sendFindCityRequest(sb.toString(), SINGLE);		
	}
	
	public static List<City> findCityByName(String name, String lang, boolean withLang) {
		StringBuffer sb = new StringBuffer();
		sb.append("http://api.accuweather.com/locations/v1/search?");
		sb.append("q=").append(name);
		if (withLang) {
			sb.append("&language=").append(lang);
		}
		sb.append("&apikey=").append(APIKEY);
		Log.d("jielong", "findCityByName url: " + sb.toString());
		return sendFindCityRequest(sb.toString(), ARRAY);
	}
	
	public static List<City> findCityByPostal(String postal, String lang, boolean withLang) {
		StringBuffer sb = new StringBuffer();		
		sb.append("http://api.accuweather.com/locations/v1/postalcodes/search.json?");
		sb.append("q=").append(postal);
		sb.append("&apikey=").append(APIKEY);
		if (withLang) {
			sb.append("&language=").append(lang);
		}
		
		Log.d("jielong", "findCityByPostal url: " + sb.toString());
		return sendFindCityRequest(sb.toString(), ARRAY);		
	}
	
	private static List<City> sendFindCityRequest(String reqUrl, int type) {
		List<City> cityList = new ArrayList<City>();
		try {
			BasicHttpParams httpParameters = new BasicHttpParams();
	        
	        HttpConnectionParams.setConnectionTimeout(httpParameters,20000);
	        HttpConnectionParams.setSoTimeout(httpParameters, 20000);  
	        HttpClient httpClient = new DefaultHttpClient(httpParameters);
	        
	        HttpGet httpRequest = new HttpGet(reqUrl);   
	        HttpResponse response=httpClient.execute(httpRequest);
	        int ret =response.getStatusLine().getStatusCode();
	        Log.d("jielong", "sendFindCityRequest ret: " + ret);
	        if (ret == 200) {
	        	String strEntity = EntityUtils.toString(response.getEntity());
	        	Log.d("jielong", "sendFindCityRequest strEntity: " + strEntity);
	        	switch (type) {
		        	case SINGLE: {
		        		if (!TextUtils.isEmpty(strEntity)) {
		        			JSONObject obj = new JSONObject(strEntity);
		        			parseJsonObject(obj, cityList);
		        		} else {
		            		return new ArrayList<City>();
		            	}
		            	
		        		break;
		        	}
		        	case ARRAY: {
		        		JSONArray objArray = new JSONArray(strEntity);
		            	if (objArray != null && objArray.length() > 0) {
		            		int len = objArray.length();
		            		for (int i = 0; i < len; i ++) {
		            			JSONObject obj = objArray.getJSONObject(i);
		            			if (obj != null) {
				            		parseJsonObject(obj, cityList);
				            	} else {
				            		return new ArrayList<City>();
				            	}
		            		}
		            	} else {
		            		return new ArrayList<City>();
		            	}
		        		break;
		        	}
	        	}
	        } else {
	        	return new ArrayList<City>();
	        }
		} catch (Exception e) {
			Log.e("jielong","City json parse exception :" + e.getMessage());
			return new ArrayList<City>();
		}
		return cityList;
	}
	
	private static void parseJsonObject(JSONObject obj, List<City> cityList) throws Exception {		
		JSONObject countryObj = obj.getJSONObject("Country");
		JSONObject adminObj = obj.getJSONObject("AdministrativeArea");
		City city = new City();
		city.setLocationKey(obj.getString("Key"));
		city.setCityName(obj.getString("LocalizedName"));
		city.setCountry(countryObj.getString("LocalizedName"));
		city.setState(adminObj.getString("LocalizedName"));
		city.setUpdateTime("");
		cityList.add(city);
	}
}
