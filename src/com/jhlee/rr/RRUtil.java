package com.jhlee.rr;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.widget.Toast;

public class RRUtil {
	public static String getTodayDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(new Date());
		return dateString;
	}
	
	public static String getCurrentTimeString() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:s");
		return formatter.format(new Date());
	}

	
}
