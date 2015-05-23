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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/WeatherForShow.java      */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

public class WeatherForShow {

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public WeatherForShow() {
        super();
    }

    public WeatherForShow(String icon, String text, String temp, String temph,
            String templ, String realfeel, String time) {
        super();
        this.icon = icon;
        this.text = text;
        this.temp = temp;
        this.temph = temph;
        this.templ = templ;
        this.realfeel = realfeel;
        this.time = time;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
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

    public String getRealfeel() {
        return realfeel;
    }

    public void setRealfeel(String realfeel) {
        this.realfeel = realfeel;
    }

    private String icon;
    private String text;
    private String temp;
    private String temph;
    private String templ;
    private String realfeel;
    private String time;

    // PR351637-Feng.Zhuang-001 Add begin
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    // PR351637-Feng.Zhuang-001 Add begin

}
