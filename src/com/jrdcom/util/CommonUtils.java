package com.jrdcom.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class CommonUtils {
	public static boolean isSupportHorizontal(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenHeight = 0;
		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			screenHeight = dm.heightPixels;
		} else if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			screenHeight = dm.widthPixels;
		}
		
		if (screenHeight <= 480 && getScreenInch(activity) <= 4.5) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isPad(Activity activity) {
		double screenInches = getScreenInch(activity);
		// 大于6尺寸则为Pad
		if (screenInches >= 6.0) {
			return true;
		}
		return false;
	}
	
	public static double getScreenInch(Activity activity) {
//		WindowManager wm = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
//		Display display = wm.getDefaultDisplay();
//		// 屏幕宽度
//		float screenWidth = display.getWidth();
//		// 屏幕高度
//		float screenHeight = display.getHeight();
//		DisplayMetrics dm = new DisplayMetrics();
//		display.getMetrics(dm);
//		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
//		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
//		// 屏幕尺寸
//		double screenInches = Math.sqrt(x + y);
//		return screenInches;
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		int densityDpi = dm.densityDpi;
		double screenInches = diagonalPixels / densityDpi;
		Log.e("jielong", "screenInches == " + screenInches + ", density == " + dm.density + ", densityDpi == " + densityDpi);
		return screenInches;
	}
}
