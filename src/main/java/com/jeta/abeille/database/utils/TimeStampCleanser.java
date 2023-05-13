package com.jeta.abeille.database.utils;

import java.util.*;

public class TimeStampCleanser implements Cleanser {
	public TimeStampCleanser() {

	}

	// for Hypersonic SQL Timestamp format must be 'yyyy-mm-dd
	// hh:mm:ss.fffffffff'

	String dateCleanse(String dateValue) {
		// 1/27/1966
		try {
			StringTokenizer tz = new StringTokenizer(dateValue, "/");
			String month = tz.nextToken();

			String day = tz.nextToken();

			String year = tz.nextToken();
			dateValue = year + "-" + month + "-" + day;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateValue;
	}

	String timeCleanse(String timeValue) {
		// 0:00:00
		return timeValue;
	}

	public String cleanse(String value) {
		value = value.trim();
		int spacepos = value.indexOf(' ');
		String datevalue = value.substring(0, spacepos);
		String timevalue = value.substring(spacepos + 1, value.length());

		datevalue = dateCleanse(datevalue);
		timevalue = timeCleanse(timevalue);
		value = '\'' + datevalue + " " + timevalue + '\'';
		return value;
	}
}
