/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.tcl.com
 * PR768316 remove the dependence for JrdMusic by fengke at 2014.08.18
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrdcom.reflect;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class SystemProperties {
    private static final String TAG = "JrdMusic/SystemProperties";
    private static final String className = "android.os.SystemProperties";
    private static Class<?> reflectClassInfo = null;
    public static Object obj=null;//fengke
    //private Context mContext;
    
    
    public SystemProperties() {
        try {
            reflectClassInfo = Class.forName(className);
            Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
        } catch (Exception e) {
            Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
        }
    }
    
    public static String get(String key) {
        String returnValue = null;
        if (reflectClassInfo == null) {
            try {
                reflectClassInfo = Class.forName(className);
                Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
            } catch (Exception e) {
                Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
            }
        }
        if (reflectClassInfo != null) {
            try {
                Method method=reflectClassInfo.getDeclaredMethod("get",String.class);
                returnValue = (String) method.invoke(obj,key);
            } catch (Exception e) {
                Log.v(TAG, "SystemProperties: get(String key) e = "+ e);
            }
        }
        Log.d(TAG, "SystemProperties get(String key) returnValue:"+returnValue);
        return returnValue;
    }


    public static String get(String key, String def) {
        String returnValue = null;
        if (reflectClassInfo == null) {
            try {
                reflectClassInfo = Class.forName(className);
                Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
            } catch (Exception e) {
                Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
            }
        }
        if (reflectClassInfo != null) {
            try {
                Method method=reflectClassInfo.getDeclaredMethod("get",String.class,String.class);
                returnValue = (String) method.invoke(obj,key,def);
            } catch (Exception e) {
                Log.v(TAG, "SystemProperties: get(String key, String def) e = "+ e);
            }
        }
        Log.d(TAG, "SystemProperties get(String key, String def) returnValue:"+returnValue);
        return returnValue;
    }


    public static int getInt(String key, int def) {
        int returnValue = -1;
        if (reflectClassInfo == null) {
            try {
                reflectClassInfo = Class.forName(className);
                Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
            } catch (Exception e) {
                Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
            }
        }
        if (reflectClassInfo != null) {
            try {
                Method method=reflectClassInfo.getDeclaredMethod("getInt",String.class,int.class);
                returnValue = (Integer) method.invoke(obj,key,def);
            } catch (Exception e) {
                Log.v(TAG, "SystemProperties: getInt(String key, int def) e = "+ e);
            }
        }
        Log.d(TAG, "SystemProperties getInt(String key, int def) returnValue:"+returnValue);
        return returnValue;
    }

    public static long getLong(String key, long def) {
        long returnValue = -1;
        if (reflectClassInfo == null) {
            try {
                reflectClassInfo = Class.forName(className);
                Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
            } catch (Exception e) {
                Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
            }
        }
        if (reflectClassInfo != null) {
            try {
                Method method=reflectClassInfo.getDeclaredMethod("getLong",String.class,long.class);
                returnValue = (Long) method.invoke(obj,key,def);
            } catch (Exception e) {
                Log.v(TAG, "SystemProperties: getLong(String key, long def) e = "+ e);
            }
        }
        Log.d(TAG, "SystemProperties getLong(String key, long def) returnValue:"+returnValue);
        return returnValue;
    }

    public static boolean getBoolean(String key, boolean def) {
        Boolean returnValue = false;
        if (reflectClassInfo == null) {
            try {
                reflectClassInfo = Class.forName(className);
                Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
            } catch (Exception e) {
                Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
            }
        }
        if (reflectClassInfo != null) {
            try {
                Method method=reflectClassInfo.getDeclaredMethod("getBoolean",String.class,boolean.class);
                returnValue = (Boolean) method.invoke(obj,key,def);
            } catch (Exception e) {
                Log.v(TAG, "SystemProperties: getBoolean(String key, boolean def) e = "+ e);
            }
        }
        Log.d(TAG, "SystemProperties getBoolean(String key, boolean def) returnValue:"+returnValue);
        return returnValue;
    }

    public static void set(String key, String val) {

        if (reflectClassInfo == null) {
            try {
                reflectClassInfo = Class.forName(className);
                Log.d(TAG, "SystemProperties reflectClassInfo:"+reflectClassInfo);
            } catch (Exception e) {
                Log.d(TAG, "SystemProperties reflectClassInfo: e = "+e);
            }
        }
        if (reflectClassInfo != null) {
            try {
                Method method=reflectClassInfo.getDeclaredMethod("set",String.class,String.class);
                method.invoke(obj,key,val);
            } catch (Exception e) {
                Log.v(TAG, "SystemProperties: set(String key, String val) e = "+ e);
            }
        }
        Log.d(TAG, "SystemProperties set(String key, String val)");

    }



}
