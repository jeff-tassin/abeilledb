package com.jeta.abeille.licensemgr;

import java.util.Calendar;

/**
 * Converts a serial number in string form to a SerialNumber object.
 * 
 * @author Jeff Tassin
 * 
 */
public class SerialNumberParser {

	/**
	 * Parses a string looking for a serial number (Abeille 1.0)
	 */
	private static SerialNumber parse(String str) throws Exception {
		// first digit is version no
		int versionno = Character.digit(str.charAt(0), 10);
		char prodcode = str.charAt(1);

		int majver = Character.digit(str.charAt(2), 10);
		int minorver = Character.digit(str.charAt(3), 10);
		int subminor = Character.digit(str.charAt(4), 10);
		char lcode = str.charAt(5);

		int month = Integer.parseInt(str.substring(6, 7), 16);
		month--;

		String seq_high = str.substring(8, 11);
		int day = Integer.parseInt(str.substring(11, 13), 16);

		String seq_low = str.substring(14, 17);
		int year = Integer.parseInt(str.substring(17, 19), 16) + 2000;

		int seqno = Integer.parseInt(seq_high + seq_low, 16);

		short stamp = Short.parseShort(str.substring(20, 24), 16);

		SerialNumber sn = new SerialNumber(majver, minorver, subminor, lcode, seqno, stamp);

		sn.setProductCode(prodcode);
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		sn.setDate(c);

		return sn;
	}

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

		SerialNumber sn = new SerialNumber(lcode, seqno, stamp);

		sn.setProductCode(prodcode);
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		sn.setDate(c);
		return sn;
	}

}
