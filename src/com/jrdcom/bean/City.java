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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/City.java                */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class City {

    private String locationKey;
    private String cityName;
    private String state;
    private String updateTime;
    private String country;
    
    //CR 447398 - ting.chen@tct-nj.com - 001 added begin
    private boolean isAutoLocate;
    
    public boolean isAutoLocate() {
		return isAutoLocate;
	}

	public void setAutoLocate(boolean isAutoLocate) {
		this.isAutoLocate = isAutoLocate;
	}
    //CR 447398 - ting.chen@tct-nj.com - 001 added end

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public City() {
        super();
    }

    public City(String locationKey, String cityName, String state,
            String updateTime) {
        super();
        this.locationKey = locationKey;
        this.cityName = cityName;
        this.state = state;
        this.updateTime = updateTime;
    }

    public City(String locationKey, String cityName, String state,
            String updateTime, String country) {
        super();
        this.locationKey = locationKey;
        this.cityName = cityName;
        this.state = state;
        this.updateTime = updateTime;
        this.country = country;
    }
    
    //CR 447398 - ting.chen@tct-nj.com - 001 added begin
    public City(String locationKey, String cityName, String state,
            String updateTime, String country, boolean isAutoLocate){
    	this(locationKey,cityName,state,updateTime,country);
    	this.isAutoLocate = isAutoLocate;
    }
    //CR 447398 - ting.chen@tct-nj.com - 001 added end

    public String getCityInfoForList() {
        return cityName + "," + country + "(" + state + ")";
    }

    public String getUpdateTimeFormated() {
        // PR351637-Feng.Zhuang-001 Modify begin
        SimpleDateFormat format = new SimpleDateFormat("MM/dd K:mm ");
        // PR351637-Feng.Zhuang-001 Modify begin

        long l = Long.parseLong(this.updateTime);
        Date date = new Date(l);

        return format.format(date);
    }

    public boolean getAMPM()
    {
        SimpleDateFormat format = new SimpleDateFormat("k");

        long l = Long.parseLong(this.updateTime);
        Date date = new Date(l);

        String ampm = format.format(date);

        return Integer.parseInt(ampm) < 12;
    }

    public String getCityIdNum() {
        return locationKey.substring(7);
    }
}
