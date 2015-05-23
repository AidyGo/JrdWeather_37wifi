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
/*   file    : /packages/apps/JrdWeather/src/com/jrdcom/weather/MyListAdapter.java    */
/*   Labels  :                                                                        */
/*================================================================================================*/
/* Modifications   (month/day/year)                                                   */
/*================================================================================================*/
/*    date     |   author   | feature ID  |modification                               */
/*===============|==============|==================================================================*/
/*===============|==============|===============|==================================================*/

package com.jrdcom.weather;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrdcom.bean.HourlyItem;
import com.jrdcom.weather.R;

public class MyListAdapter extends BaseAdapter {
	private ArrayList<HashMap<String, Object>> data;
	private LayoutInflater layoutInflater;

	public MyListAdapter(Context context,
			ArrayList<HashMap<String, Object>> data) {
		this.data = data;
		this.layoutInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HourlyItem item = null;
		if (convertView == null) {
			item = new HourlyItem();

			convertView = layoutInflater.inflate(R.layout.hourly_listitem, null);
			item.setWeek((TextView) convertView.findViewById(R.id.hour_week));
			item.setTime((TextView) convertView.findViewById(R.id.hour_time));
			item.setIcon((ImageView) convertView.findViewById(R.id.hour_icon));
			item.setTemp((TextView) convertView.findViewById(R.id.hour_temp));
			item.setText((TextView) convertView.findViewById(R.id.hour_text));
			convertView.setTag(item);
		} else {
			item = (HourlyItem) convertView.getTag();
		}

		String week = (String) data.get(position).get("week");
		if (null != week && !"".equals(week)) {
			item.getTime().setGravity(Gravity.CENTER_HORIZONTAL);
			item.getWeek().setVisibility(View.VISIBLE);
			item.getWeek().setText(week);
		} else {
			item.getTime().setGravity(Gravity.CENTER);
			item.getWeek().setVisibility(View.GONE);
		}
		item.getTime().setText((String) data.get(position).get("time"));
		item.getIcon().setBackgroundResource((Integer) data.get(position).get("icon"));
		item.getTemp().setText((String) data.get(position).get("temp"));
		item.getText().setText((String) data.get(position).get("text"));
		int color = (Integer)data.get(position).get("fontColor");
		item.getWeek().setTextColor(color);
		item.getTime().setTextColor(color);
		item.getTemp().setTextColor(color);
		item.getText().setTextColor(color);
		convertView.setBackgroundResource((Integer)data.get(position).get("background"));

		return convertView;
	}
}
