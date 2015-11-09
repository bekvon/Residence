package com.bekvon.bukkit.residence.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.bekvon.bukkit.residence.Residence;

public class GetTime {
    public static String getTime(Long time) {
	Date dNow = new Date(time);
	SimpleDateFormat ft = new SimpleDateFormat(Residence.getConfigManager().getDateFormat());
	ft.setTimeZone(TimeZone.getTimeZone(Residence.getConfigManager().getTimeZone()));
	return ft.format(dNow);
    }
}
