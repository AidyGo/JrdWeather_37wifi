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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/data/CityFindParser.java      */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/


package com.jrdcom.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.jrdcom.bean.City;

public class CityFindParser {

    public static List<City> parse(InputStream in) throws Exception {

        List<City> list = null;
        XmlPullParser parser = Xml.newPullParser();

        parser.setInput(in, "utf-8");

        int eventCode = parser.getEventType();

        City city = null;

        while (eventCode != XmlPullParser.END_DOCUMENT) {
            switch (eventCode) {
                case XmlPullParser.START_DOCUMENT:
                    list = new ArrayList<City>();
                    break;
                case XmlPullParser.START_TAG:
                    if ("location".equals(parser.getName())) {
                        city = new City();

                        city.setCityName(parser.getAttributeValue(null, "city"));
                        city.setCountry(parser.getAttributeValue(null, "country"));
                        city.setState(parser.getAttributeValue(null, "adminArea"));
                        city.setLocationKey(parser.getAttributeValue(null,
                                "location"));
                        city.setUpdateTime("");
                        /*PR 551477- Neo Skunkworks - James Jiang - 001 Begin*/
                        //parser.nextTag();
                    }
                   break;
                        /*PR 551477- Neo Skunkworks - James Jiang - 001 End*/
                case XmlPullParser.END_TAG:
                    if (("location".equals(parser.getName()) && city != null)) {
                        list.add(city);
                        city = null;
                    }
                        /*PR 551477- Neo Skunkworks - James Jiang - 001 Begin*/
                   break;
                        /*PR 551477- Neo Skunkworks - James Jiang - 001 End*/
                default:
                    break;
            }
            eventCode = parser.next();
        }
        return list;
    }

}
