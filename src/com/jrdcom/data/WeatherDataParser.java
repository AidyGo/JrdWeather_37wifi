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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/data/WeatherDataParser.java   */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.jrdcom.bean.Currentconditions;
import com.jrdcom.bean.Day;
import com.jrdcom.bean.Forecast;
import com.jrdcom.bean.HalfDay;
import com.jrdcom.bean.Hour;
import com.jrdcom.bean.Local;
import com.jrdcom.bean.Units;
import com.jrdcom.bean.Weather;

public class WeatherDataParser {

    private static final String ns = null;

    public Weather parse(InputStream in) throws XmlPullParserException,
            IOException {
        try {

            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

            parser.setInput(in, null);

            parser.nextTag();

            return readWeather(parser);
        } finally {
            in.close();
        }
    }

    // Begin to parse the weather information
    private Weather readWeather(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Weather weather = new Weather();

        parser.require(XmlPullParser.START_TAG, ns, "adc_database");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("units")) {
                weather.setUnits(readUnits(parser));
            } else if (name.equals("local")) {
                weather.setLocal(readLocal(parser));
            } else if (name.equals("currentconditions")) {
                weather.setCurrentconditions(readCurrentconditions(parser));
            } else if (name.equals("forecast")) {
                weather.setForecast(readForecast(parser));
            } else {
                skip(parser);
            }
        }
        return weather;
    }

    // parse the units info
    private Units readUnits(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Units units = new Units();

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            if (name.equals("temp")) {
                units.setTemp(readContent(parser, "temp"));
            } else if (name.equals("dist")) {
                units.setDist(readContent(parser, "dist"));
            } else if (name.equals("speed")) {
                units.setSpeed(readContent(parser, "speed"));
            } else if (name.equals("pres")) {
                units.setPres(readContent(parser, "pres"));
            } else if (name.equals("prec")) {
                units.setPrec(readContent(parser, "prec"));
            } else {
                skip(parser);
            }
        }

        return units;
    }

    // read the whole content of each tag
    private String readContent(XmlPullParser parser, String tag)
            throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, tag);

        String content = readText(parser);

        parser.require(XmlPullParser.END_TAG, ns, tag);

        return content;
    }

    // parse the local information of the city you searched
    private Local readLocal(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Local local = new Local();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("city")) {
                local.setCity(readContent(parser, "city"));
            } else if (name.equals("adminArea")) {
                local.setState(readContent(parser, "adminArea"));
            } else if (name.equals("country")) {
                local.setCountry(readContent(parser, "country"));
            } else if (name.equals("cityId")) {
                local.setCityId("cityId:" + readContent(parser, "cityId"));
            } 
            // PR 720494 - Neo Skunkworks - Wells Tang - 001 begin
            else if(null == local.getCityId() && "primaryCityId".equals(name))
            {
                local.setCityId("cityId:" + readContent(parser, "primaryCityId"));
            }
            // PR 720494 - Neo Skunkworks - Wells Tang - 001 begin
            else if (name.equals("lat")) {
                local.setLat(readContent(parser, "lat"));
            } else if (name.equals("lon")) {
                local.setLon(readContent(parser, "lon"));
            } else if (name.equals("time")) {
                local.setTime(readContent(parser, "time"));
            } else if (name.equals("timeZone")) {
                local.setTimezone(readContent(parser, "timeZone"));
            } else if (name.equals("obsDaylight")) {
                local.setObsDaylight(readContent(parser, "obsDaylight"));
            } else if (name.equals("currentGmtOffset")) {
                local.setCurrentGmtOffset(readContent(parser,
                        "currentGmtOffset"));
            } else if (name.equals("timeZoneAbbreviation")) {
                local.setTimeZoneAbbreviation(readContent(parser,
                        "timeZoneAbbreviation"));
            } else {
                skip(parser);
            }
        }
        return local;
    }

    // parse all the detail weather information of this city
    private Currentconditions readCurrentconditions(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Currentconditions currentconditions = new Currentconditions();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("url")) {
                currentconditions.setUrl(readContent(parser, "url"));
            } else if (name.equals("observationtime")) {
                currentconditions.setObservationtime(readContent(parser,
                        "observationtime"));
            } else if (name.equals("pressure")) {
                currentconditions.setPressure(readContent(parser, "pressure"));
            } else if (name.equals("temperature")) {
                currentconditions.setTemperature(readContent(parser,
                        "temperature"));
            } else if (name.equals("realfeel")) {
                currentconditions.setRealfeel(readContent(parser, "realfeel"));
            } else if (name.equals("humidity")) {
                currentconditions.setHumidity(readContent(parser, "humidity"));
            } else if (name.equals("weathertext")) {
                currentconditions.setWeathertext(readContent(parser,
                        "weathertext"));
            } else if (name.equals("weathericon")) {
                currentconditions.setWeathericon(readContent(parser,
                        "weathericon"));
            } else if (name.equals("windgusts")) {
                currentconditions
                        .setWindgusts(readContent(parser, "windgusts"));
            } else if (name.equals("windspeed")) {
                currentconditions
                        .setWindspeed(readContent(parser, "windspeed"));
            } else if (name.equals("winddirection")) {
                currentconditions.setWinddirection(readContent(parser,
                        "winddirection"));
            } else if (name.equals("visibility")) {
                currentconditions.setVisibility(readContent(parser,
                        "visibility"));
            } else if (name.equals("precip")) {
                currentconditions.setPrecip(readContent(parser, "precip"));
            } else if (name.equals("uvindex")) {
                currentconditions.setUvindex(readContent(parser, "uvindex"));
            } else if (name.equals("dewpoint")) {
                currentconditions.setDewpoint(readContent(parser, "dewpoint"));
            } else if (name.equals("cloudcover")) {
                currentconditions.setCloudcover(readContent(parser,
                        "cloudcover"));
            } else if (name.equals("apparenttemp")) {
                currentconditions.setApparenttemp(readContent(parser,
                        "apparenttemp"));
            } else if (name.equals("windchill")) {
                currentconditions
                        .setWindchill(readContent(parser, "windchill"));
            } else {
                skip(parser);
            }
        }

        return currentconditions;
    }

    // parse the weather information of the next 4 days
    private Forecast readForecast(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Forecast forecast = new Forecast();
        List<Day> days = new ArrayList<Day>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("day")) {
                days.add(readDay(parser));
            } else if (name.equals("hourly")) {
                forecast.setHours(readHourly(parser));
            } else {
                skip(parser);
            }
        }
        forecast.setDays(days);
        return forecast;
    }

    // read the forecast of each day
    private Day readDay(XmlPullParser parser) throws XmlPullParserException,
            IOException {

        Day day = new Day();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("url")) {
                day.setUrl(readContent(parser, "url"));
            } else if (name.equals("obsdate")) {
                day.setObsdate(readContent(parser, "obsdate"));
            } else if (name.equals("daycode")) {
                day.setDaycode(readContent(parser, "daycode"));
            } else if (name.equals("sunrise")) {
                day.setSunrise(readContent(parser, "sunrise"));
            } else if (name.equals("sunset")) {
                day.setSunset(readContent(parser, "sunset"));
            } else if (name.equals("daytime")) {
                day.setDaytime(readHalfDay(parser));
            } else if (name.equals("nighttime")) {
                day.setNightday(readHalfDay(parser));
            } else {
                skip(parser);
            }
        }

        return day;

    }

    // read the hourly weather forecast
    private List<Hour> readHourly(XmlPullParser parser) throws XmlPullParserException,
            IOException {

        List<Hour> hours = new ArrayList<Hour>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Hour hour = new Hour();
            if (name.equals("hour")) {
                hour.setTime(parser.getAttributeValue(null, "time"));
                hours.add(readHour(parser, hour));

            } else {
                skip(parser);
            }
        }
        return hours;

    }

    // read the hourly weather forecast
    private Hour readHour(XmlPullParser parser, Hour hour) throws XmlPullParserException,
            IOException {

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("weathericon")) {
                hour.setWeatherIcon(readContent(parser, "weathericon"));
            } else if (name.equals("temperature")) {
                hour.setTemperature(readContent(parser, "temperature"));
            } else if (name.equals("realfeel")) {
                hour.setRealfeel(readContent(parser, "realfeel"));
            } else if (name.equals("dewpoint")) {
                hour.setDewpoint(readContent(parser, "dewpoint"));
            } else if (name.equals("humidity")) {
                hour.setHumidity(readContent(parser, "humidity"));
            } else if (name.equals("precip")) {
                hour.setPrecip(readContent(parser, "precip"));
            } else if (name.equals("rain")) {
                hour.setRain(readContent(parser, "rain"));
            } else if (name.equals("snow")) {
                hour.setSnow(readContent(parser, "snow"));
            } else if (name.equals("ice")) {
                hour.setIce(readContent(parser, "ice"));
            } else if (name.equals("windspeed")) {
                hour.setWindspeed(readContent(parser, "windspeed"));
            } else if (name.equals("winddirection")) {
                hour.setWinddirection(readContent(parser, "winddirection"));
            } else if (name.equals("windgust")) {
                hour.setWindgust(readContent(parser, "windgust"));
            } else if (name.equals("txtshort")) {
                hour.setTxtshort(readContent(parser, "txtshort"));
            } else if (name.equals("mobileLink")) {
                hour.setMobileLink(readContent(parser, "mobileLink"));
            } else {
                skip(parser);
            }
        }
        return hour;

    }

    // read the weather information of the daytime or nighttime
    private HalfDay readHalfDay(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        HalfDay hday = new HalfDay();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("txtshort")) {
                hday.setTxtshort(readContent(parser, "txtshort"));
            } else if (name.equals("txtlong")) {
                hday.setTxtlong(readContent(parser, "txtlong"));
            } else if (name.equals("weathericon")) {
                hday.setWeathericon(readContent(parser, "weathericon"));
            } else if (name.equals("hightemperature")) {
                hday.setHightemperature(readContent(parser, "hightemperature"));
            } else if (name.equals("lowtemperature")) {
                hday.setLowtemperature(readContent(parser, "lowtemperature"));
            } else if (name.equals("realfeelhigh")) {
                hday.setRealfeelhigh(readContent(parser, "realfeelhigh"));
            } else if (name.equals("realfeellow")) {
                hday.setRealfeellow(readContent(parser, "realfeellow"));
            } else if (name.equals("windspeed")) {
                hday.setWindspeed(readContent(parser, "windspeed"));
            } else if (name.equals("winddirection")) {
                hday.setWinddirection(readContent(parser, "winddirection"));
            } else if (name.equals("windgust")) {
                hday.setWindgust(readContent(parser, "windgust"));
            } else if (name.equals("maxuv")) {
                hday.setMaxuv(readContent(parser, "maxuv"));
            } else if (name.equals("rainamount")) {
                hday.setRainamount(readContent(parser, "rainamount"));
            } else if (name.equals("snowamount")) {
                hday.setSnowamount(readContent(parser, "snowamount"));
            } else if (name.equals("iceamount")) {
                hday.setIceamount(readContent(parser, "iceamount"));
            } else if (name.equals("precipamount")) {
                hday.setPrecipamount(readContent(parser, "precipamount"));
            } else if (name.equals("tstormprob")) {
                hday.setTstormprob(readContent(parser, "tstormprob"));
            } else {
                skip(parser);
            }
        }
        return hday;
    }

    // get the text of this tag
    private String readText(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // skip the tag which is not needed
    private void skip(XmlPullParser parser) throws XmlPullParserException,
            IOException {

        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
    
    
    /*PR 456206- Neo Skunkworks - Paul Xu added - 001 Begin*/
	/**
	  * Descript:Weather application gives a Weak signal error
	  * Solution:get cityId by postal code
	  * */
	public String parseCityId(InputStream in) throws XmlPullParserException,IOException {
	String cityId = null;
	try {

	          XmlPullParser parser = Xml.newPullParser();
	          parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	          parser.setInput(in, null);
	          parser.nextTag();
	          cityId = readCityId(parser);
	          } finally {
	          in.close();                    
	          }              
	          return cityId;
         }
	
	  private String readCityId(XmlPullParser parser)  throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "adc_database");

            while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
               }

               String name = parser.getName();
               if (name.equals("local")) {
               return readLocalCityId(parser);
                } else {
                skip(parser);
                  }
              }
                return null;
            }
            
         private String readLocalCityId(XmlPullParser parser) throws XmlPullParserException, IOException {

              while (parser.next() != XmlPullParser.END_TAG) {
                 if (parser.getEventType() != XmlPullParser.START_TAG) {
                      continue;
                     }
                  
                    String name = parser.getName();
 
                 if (name.equals("primaryCityId")) {
                  return readContent(parser, "primaryCityId");
                   } else {
                    skip(parser);
                   }
                }
               return null;
             }
          /*PR 456206- Neo Skunkworks - Paul Xu added - 001 End*/
}
