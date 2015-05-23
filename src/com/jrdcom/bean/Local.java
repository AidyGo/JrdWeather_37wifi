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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Local.java               */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

public class Local {

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getObsDaylight() {
        return obsDaylight;
    }

    public void setObsDaylight(String obsDaylight) {
        this.obsDaylight = obsDaylight;
    }

    public String getCurrentGmtOffset() {
        return currentGmtOffset;
    }

    public void setCurrentGmtOffset(String currentGmtOffset) {
        this.currentGmtOffset = currentGmtOffset;
    }

    public String getTimeZoneAbbreviation() {
        return timeZoneAbbreviation;
    }

    public void setTimeZoneAbbreviation(String timeZoneAbbreviation) {
        this.timeZoneAbbreviation = timeZoneAbbreviation;
    }

    private String city;
    private String state;
    private String country;
    private String cityId;
    private String lat;
    private String lon;
    private String time;
    private String timezone;
    private String obsDaylight;
    private String currentGmtOffset;
    private String timeZoneAbbreviation;

}
