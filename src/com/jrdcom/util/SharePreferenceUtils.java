package com.jrdcom.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class SharePreferenceUtils {
	public  static  boolean  inLocalActivity = false;
	
	public static void checkCommonCity(Context context, String locationKey){
		SharedPreferences preferences = context.getSharedPreferences("Common_city", Context.MODE_PRIVATE);
		String commonCityLocationKey = preferences.getString("common_city", "");
		if(TextUtils.isEmpty(commonCityLocationKey)){
			Editor editor = preferences.edit();
			/*editor.putString("common_city", commonCityLocationKey);*/ // PR 455558 - ting.chen@tct-nj.com - removed
			// PR 455558 - ting.chen@tct-nj.com - added begin
			editor.putString("common_city", locationKey);
			// PR 455558 - ting.chen@tct-nj.com - added end
			editor.commit();
		}
	}
	
	public static boolean isCommonCity(Context context, String locationKey){
		SharedPreferences preferences = context.getSharedPreferences("Common_city", Context.MODE_PRIVATE);
		String commonCityLocationKey = preferences.getString("common_city", "");
		return locationKey.equals(commonCityLocationKey);
	}
	
	public  static  void   saveLong(Context  context,String  key,long   value)
	{
		SharedPreferences preferences = context.getSharedPreferences("Common_city", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(key,value);
		editor.commit();
	}
	
	public  static  long  getLong(Context  context,String key,long  defaultValue)
	{
		SharedPreferences preferences = context.getSharedPreferences("Common_city", Context.MODE_PRIVATE);
		return preferences.getLong(key,defaultValue);
	}

    /*PR 672508 - Neo Skunkworks - Richard He added - 001 Begin*/
    public static void saveCurrentCityKey(Context context, String cityKey){
        SharedPreferences preferences = context.getSharedPreferences("City_key", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("current_city_key", cityKey);
        editor.commit();
    }

    public static String getCurrentCityKey(Context context){
        SharedPreferences preferences = context.getSharedPreferences("City_key", Context.MODE_PRIVATE);
        return preferences.getString("current_city_key", null);
	}
    /*PR 672508 - Neo Skunkworks - Richard He added - 001 End*/
}
