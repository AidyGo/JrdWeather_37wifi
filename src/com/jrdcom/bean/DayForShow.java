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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/DayForShow.java          */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

public class DayForShow {
	public DayForShow() {
		super();
	}

	public DayForShow(String icon, String temph, String templ, String week,
			String date, String url) {
		super();
		this.icon = icon;
		this.temph = temph;
		this.templ = templ;
		this.week = week;
		this.date = date;
		this.url = url;//add by shenxin for PR460544
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getTemph() {
		return temph;
	}

	public void setTemph(String temph) {
		this.temph = temph;
	}

	public String getTempl() {
		return templ;
	}

	public void setTempl(String templ) {
		this.templ = templ;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

    //add by shenxin for PR460544 start
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //add by shenxin for PR460544 end

	private String icon;
	private String temph;
	private String templ;
	private String week;
	private String date;
    private String url;//add by shenxin for PR460544
}
