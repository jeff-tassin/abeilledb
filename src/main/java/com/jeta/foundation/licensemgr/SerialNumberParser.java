/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import java.util.Calendar;

/**
 * Converts a serial number in string form to a SerialNumber object.
 * 
 * @author Jeff Tassin
 * 
 */
public class SerialNumberParser {

	/**
	 * Parses a string looking for a serial number for Abeille 1.1
	 */
	public static SerialNumber parse2(String str) throws Exception {
		// first digit is version no
		int versionno = Character.digit(str.charAt(0), 10);
		char prodcode = str.charAt(1);

		// int majver = Character.digit( str.charAt(2), 10 );
		// int minorver = Character.digit( str.charAt(3), 10 );
		// int subminor = Character.digit( str.charAt(4), 10 );
		char lcode = str.charAt(2);

		int month = Integer.parseInt(str.substring(3, 4), 16);
		month--;

		String seq_high = str.substring(5, 8);
		int day = Integer.parseInt(str.substring(8, 10), 16);

		String seq_low = str.substring(11, 14);
		int year = Integer.parseInt(str.substring(14, 16), 16) + 2000;

		int seqno = Integer.parseInt(seq_high + seq_low, 16);

		short stamp = Short.parseShort(str.substring(17, 21), 16);

		SerialNumber sn = new SerialNumber(prodcode, lcode, seqno, stamp);

		sn.setProductCode(prodcode);
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		sn.setDate(c);
		return sn;
	}

}
