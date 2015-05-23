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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/bean/Weather.java             */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.bean;

import java.util.ArrayList;
import java.util.List;

public class Weather {

    private Units units;
    private Local local;
    private Currentconditions currentconditions;
    private Forecast forecast;
    private static final String MON = "Monday";
    private static final String TUE = "Tuesday";
    private static final String WED = "Wednesday";
    private static final String THU = "Thursday";
    private static final String FRI = "Friday";
    private static final String SAT = "Saturday";
    private static final String SUN = "Sunday";

    public Units getUnits() {
        return units;
    }

    public void setUnits(Units units) {
        this.units = units;
    }

    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Currentconditions getCurrentconditions() {
        return currentconditions;
    }

    public void setCurrentconditions(Currentconditions currentconditions) {
        this.currentconditions = currentconditions;
    }

    // get the information what needed for display
    public WeatherForShow getWeatherForShow() {
        WeatherForShow w = new WeatherForShow();

        w.setIcon(getCurrentconditions().getWeathericon());
        w.setText(getCurrentconditions().getWeathertext());
        w.setTemp(getCurrentconditions().getTemperature());
        w.setTemph(getForecast().getDays().get(0).getDaytime()
                .getHightemperature());
        w.setTempl(getForecast().getDays().get(0).getNightday()
                .getLowtemperature());
        w.setRealfeel(getCurrentconditions().getRealfeel());
        w.setTime(getLocal().getTime());

        return w;
    }

    public List<DayForShow> getDayForShow() {
        List<DayForShow> days = new ArrayList<DayForShow>();

        days.add(getForecast().getDays().get(0).getDayForShow());
        days.add(getForecast().getDays().get(1).getDayForShow());
        days.add(getForecast().getDays().get(2).getDayForShow());
        days.add(getForecast().getDays().get(3).getDayForShow());
        days.add(getForecast().getDays().get(4).getDayForShow());

        return days;
    }

    public HourForShow getHourForShow(int i) {
        HourForShow hour = getForecast().getHours().get(i).getHourForShow();

        if (i == 0) {
            hour.setWeek(getForecast().getDays().get(0).getDaycode());
        }
        if (hour.getTime().equals("12 AM")) {
            hour.setWeek(getNextDayCode(getForecast().getDays().get(0).getDaycode()));
        }

        return hour;
    }

    private String getNextDayCode(String week) {
        if (week.equals(MON)) {
            return TUE;
        } else if (week.equals(TUE)) {
            return WED;
        } else if (week.equals(WED)) {
            return THU;
        } else if (week.equals(THU)) {
            return FRI;
        } else if (week.equals(FRI)) {
            return SAT;
        } else if (week.equals(SAT)) {
            return SUN;
        } else if (week.equals(SUN)) {
            return MON;
        }

        else {
            return "";
        }

    }
}
