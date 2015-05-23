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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/HalfDay.java             */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

public class HalfDay {

	public String getTxtshort() {
		return txtshort;
	}

	public void setTxtshort(String txtshort) {
		this.txtshort = txtshort;
	}

	public String getTxtlong() {
		return txtlong;
	}

	public void setTxtlong(String txtlong) {
		this.txtlong = txtlong;
	}

	public String getWeathericon() {
		return weathericon;
	}

	public void setWeathericon(String weathericon) {
		this.weathericon = weathericon;
	}

	public String getHightemperature() {
		return hightemperature;
	}

	public void setHightemperature(String hightemperature) {
		this.hightemperature = hightemperature;
	}

	public String getLowtemperature() {
		return lowtemperature;
	}

	public void setLowtemperature(String lowtemperature) {
		this.lowtemperature = lowtemperature;
	}

	public String getRealfeelhigh() {
		return realfeelhigh;
	}

	public void setRealfeelhigh(String realfeelhigh) {
		this.realfeelhigh = realfeelhigh;
	}

	public String getRealfeellow() {
		return realfeellow;
	}

	public void setRealfeellow(String realfeellow) {
		this.realfeellow = realfeellow;
	}

	public String getWindspeed() {
		return windspeed;
	}

	public void setWindspeed(String windspeed) {
		this.windspeed = windspeed;
	}

	public String getWinddirection() {
		return winddirection;
	}

	public void setWinddirection(String winddirection) {
		this.winddirection = winddirection;
	}

	public String getWindgust() {
		return windgust;
	}

	public void setWindgust(String windgust) {
		this.windgust = windgust;
	}

	public String getMaxuv() {
		return maxuv;
	}

	public void setMaxuv(String maxuv) {
		this.maxuv = maxuv;
	}

	public String getRainamount() {
		return rainamount;
	}

	public void setRainamount(String rainamount) {
		this.rainamount = rainamount;
	}

	public String getSnowamount() {
		return snowamount;
	}

	public void setSnowamount(String snowamount) {
		this.snowamount = snowamount;
	}

	public String getIceamount() {
		return iceamount;
	}

	public void setIceamount(String iceamount) {
		this.iceamount = iceamount;
	}

	public String getPrecipamount() {
		return precipamount;
	}

	public void setPrecipamount(String precipamount) {
		this.precipamount = precipamount;
	}

	public String getTstormprob() {
		return tstormprob;
	}

	public void setTstormprob(String tstormprob) {
		this.tstormprob = tstormprob;
	}

	private String txtshort;
	private String txtlong;
	private String weathericon;
	private String hightemperature;
	private String lowtemperature;
	private String realfeelhigh;
	private String realfeellow;
	private String windspeed;
	private String winddirection;
	private String windgust;
	private String maxuv;
	private String rainamount;
	private String snowamount;
	private String iceamount;
	private String precipamount;
	private String tstormprob;

}
