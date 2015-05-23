package com.jrdcom.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.*;

public class CustomizeUtils {
    private static final String TAG = "CustomizeUtils";
    // path of file isdm_JrdCustTest_defaults.xml
    private static final String PATH = "/custpack/plf/JrdWeather/";
    private static final String FILE = "isdm_JrdWeather_defaults.xml";

    /**
     * get isdm value which is bool
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static boolean getBoolean(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "bool", mContext.getPackageName());
        // get the native isdmID value
        boolean result = mContext.getResources().getBoolean(id);
        try {
            String bool_frameworks = getISDMString(new File(PATH + FILE), def_name, "bool");
            if (null != bool_frameworks) {
                result = Boolean.parseBoolean(bool_frameworks);
            }
        } catch (XmlPullParserException e) {           
            e.printStackTrace();
        } catch (IOException e) {            
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get isdm value which is string
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static String getString(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "string", mContext.getPackageName());
        // get the native isdmID value
        String result = mContext.getResources().getString(id);
        try {
            String string_frameworks = getISDMString(new File(PATH + FILE), def_name, "string");
            if (null != string_frameworks) {
                result = string_frameworks;
            }
        } catch (XmlPullParserException e) {            
            e.printStackTrace();
        } catch (IOException e) {            
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * get isdm value which is integer
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static int getInteger(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "integer", mContext.getPackageName());
        // get the native isdmID value
        int result = (int)mContext.getResources().getInteger(id);
        try {
            String string_frameworks = getISDMString(new File(PATH + FILE), def_name, "integer");
            if (null != string_frameworks) {
                result = Integer.getInteger(string_frameworks);
            }
        } catch (XmlPullParserException e) {            
            e.printStackTrace();
        } catch (IOException e) {            
            e.printStackTrace();
        }
        return result;
    }

    /**
     * parser the XML file to get the isdmID value
     * 
     * @param file : xml file
     * @param name : isdmID
     * @param type : isdmID type like bool and string
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getISDMString(File file, String name, String type)
            throws XmlPullParserException,
            IOException {
        if (!file.exists() || null == file) {
            Log.e(TAG, "file not exist : " + file);
            return null;
        }
        String result = null;
        InputStream inputStream = new FileInputStream(file);
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(inputStream, "utf-8");

        int evtType = xmlParser.getEventType();
        boolean query_end = false;
        while (evtType != XmlPullParser.END_DOCUMENT && !query_end) {

            switch (evtType) {
                case XmlPullParser.START_TAG:

                    String start_tag = xmlParser.getAttributeValue(null, "name");
                    String start_type = xmlParser.getName();
                    if (null != start_tag && type.equals(start_type) && start_tag.equals(name)) {
                        result = xmlParser.nextText();                       
                        query_end = true;
                    }
                    break;

                case XmlPullParser.END_TAG:

                    break;

                default:
                    break;
            }
            // move to next node if not tail
            evtType = xmlParser.next();
        }
        inputStream.close();
        return result;
    }

    /* PR 695602- Neo Skunkworks - Richard He add - 001 Begin */
    /**
     * 
     * @param str like "aaa"
     * @return
     */
    public static String splitQuotationMarks(String str){
        if(null != str && str.length() > 2 && str.startsWith("\"") && str.endsWith("\"")){
			str = str.substring(1, str.length()-1);
        }
        Log.d(TAG, "!--->After splitQuotationMarks: "+str);
        return str;
    }
    /* PR 695602- Neo Skunkworks - Richard He add - 001 End */
}