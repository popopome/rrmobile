package com.jhlee.rr;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RRUtil {
	
	public static String getTodayDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		String dateString = formatter.format(new Date());
		return dateString;
	}
	
	public static String getCurrentTimeString() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:s");
		return formatter.format(new Date());
	}

	public static String formatMoney(long l, long m, boolean useDollarSign) {
		StringBuilder sb = new StringBuilder();
		if(useDollarSign)
			sb.append("$");
		sb.append(l);
		sb.append(".");
		if(m < 10)
			sb.append("0");
		sb.append(m);
		return sb.toString();
	}
}
