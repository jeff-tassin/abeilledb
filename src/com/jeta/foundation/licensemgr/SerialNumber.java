/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import java.util.Calendar;
import java.util.StringTokenizer;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.license.LicenseManager;

/**
 * This class is a serial number for JETA Software products. A serial number
 * takes the following form:
 * 
 * [version][product code][license code][month]-[sequence number high
 * digits][day]-[sequence number low digits][year] 1###EMM-SEQ1DD-SEQ2YY
 * 
 * where: version: the version number for this serial number ( currently 1 )
 * product code: 3 digit code - first digit is A for abeille second digit is the
 * major version third digit is the minor version license code: E for
 * evaluation, L for licensed month: two digit month that code was generated (in
 * hex) (1-based) sequence number: the sequence number is a 6 digit hex number
 * that is split into high and low three digits This number identifies the
 * purchaser in our customer database day: two digit day of month that code was
 * generated (in hex) year: two digit year (last two digits) in hex that code
 * was generated
 * 
 * @author Jeff Tassin
 */
public class SerialNumber implements Comparable {
	private static String m_zeros = "000000000000";

	private static final int SEQUENCE_SIZE = 6;
	private static final int MONTH_SIZE = 1;
	private static final int DAY_SIZE = 2;
	private static final int YEAR_SIZE = 2;
	private static final int STAMP_SIZE = 4;
	private static final char SERIAL_DELIMITER = '-';

	public static int VERSION_NUMBER = 1;

	/** product version */
	private int m_majorversion;
	private int m_minorversion;
	private int m_subminorversion;

	/**
	 * product code 'A' is for Abeille Pro 'B' is for Abeille Basic, 'F' is for
	 * Forms Designer
	 */
	private char m_productcode;

	/** the license code 'E' for evaluation 'L' for licensed */
	private char m_licensecode;

	/** the sequence number (unique number that identifies licensee */
	private int m_seqnumber;

	/** the date this serial number was granted */
	private Calendar m_date;

	/**
	 * this is a trailing number that acts as a secondary id in addition to the
	 * sequence number. However, this number is random.
	 */
	private short m_stamp;

	/**
	 * ctor (for parsing)
	 */
	public SerialNumber() {

	}

	/**
	 * ctor
	 */
	public SerialNumber(char productcode, int majVer, int minVer, int subMinVer, char lCode, int seqNumber, short stamp) {
		setProductCode(productcode);
		setMajorVersion(majVer);
		setMinorVersion(minVer);
		setSubMinorVersion(subMinVer);
		setLicenseCode(lCode);
		setSequenceNumber(seqNumber);
		setStamp(stamp);

		m_date = Calendar.getInstance();
	}

	/**
	 * ctor
	 */
	public SerialNumber(char productcode, char lCode, int seqNumber, short stamp) {
		setProductCode(productcode);
		setLicenseCode(lCode);
		setSequenceNumber(seqNumber);
		setStamp(stamp);

		m_date = Calendar.getInstance();
	}

	/**
	 * Compares two integers
	 */
	private static int compareIntegers(int iVal1, int iVal2) {
		if (iVal1 == iVal2)
			return 0;
		else if (iVal1 < iVal2)
			return -1;
		else
			return 1;
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object obj) {
		if (obj instanceof SerialNumber) {
			SerialNumber sn = (SerialNumber) obj;
			if (m_productcode == sn.m_productcode) {
				if (m_licensecode == sn.m_licensecode) {
					if (m_seqnumber == sn.m_seqnumber) {
						// stamps MUST be equal if the sequence numbers are
						// equal
						assert (m_stamp == sn.m_stamp);
						return 0;
					} else {
						return compareIntegers(m_seqnumber, sn.m_seqnumber);
					}
				} else {
					return compareIntegers(m_licensecode, sn.m_licensecode);
				}
			} else {
				return compareIntegers(m_productcode, sn.m_productcode);
			}
		} else {
			return -1;
		}
	}

	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return the license code 'E' for evaluation 'L' for licensed
	 */
	public char getLicenseCode() {
		return m_licensecode;
	}

	/**
	 * @return the date this serial number was generated
	 */
	public Calendar getDate() {
		return m_date;
	}

	/**
	 * @return the major product version (0-9)
	 */
	public int getMajorVersion() {
		return m_majorversion;
	}

	/**
	 * @return the minor product version (0-9)
	 */
	public int getMinorVersion() {
		return m_minorversion;
	}

	/**
	 * @return the product code ('A')
	 */
	public char getProductCode() {
		return m_productcode;
	}

	/**
	 * @return the sequence number
	 */
	public int getSequenceNumber() {
		return m_seqnumber;
	}

	/**
	 * @return the stamp
	 */
	public short getStamp() {
		return m_stamp;
	}

	/**
	 * @return the subminor version (0-9)
	 */
	public int getSubMinorVersion() {
		return m_subminorversion;
	}

	/**
	 * @return true if this serial number is for an evaluation and not an actual
	 *         licensed version
	 */
	public boolean isEvaluation() {
		if (isSpecialEvaluation())
			return true;

		char lcode = getLicenseCode();
		return (lcode == LicenseInfo.EVALUATION_CODE);
	}

	/**
	 * @return true if this is a special evaluation. This allows the user to
	 *         enable all program features but still times out. This is for beta
	 *         versions and people who make explicit requests for a full
	 *         unlimited evaluation.
	 */
	public boolean isSpecialEvaluation() {
		char lcode = getLicenseCode();
		return (lcode == LicenseInfo.SPECIAL_EVALUATION_CODE);
	}

	/**
	 * Print the contents of this object
	 */
	public void print() {
		TSUtils.printMessage("-------------Dumping serial number--------------");
		TSUtils.printMessage("major version: " + m_majorversion);
		TSUtils.printMessage("minor version: " + m_minorversion);
		TSUtils.printMessage("subminor version: " + m_subminorversion);
		TSUtils.printMessage("product code: " + m_productcode);
		TSUtils.printMessage("license code: " + m_licensecode);
		TSUtils.printMessage("sequence number: " + m_seqnumber);

		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MM-dd-yyyy");
		TSUtils.printMessage("calendar: " + format.format(m_date.getTime()));
	}

	/**
	 * Sets the date this serial number was generated
	 */
	public void setDate(Calendar c) {
		m_date = c;
	}

	/**
	 * Sets the license code 'E' for evaluation 'L' for licensed
	 */
	public void setLicenseCode(char lcode) {
		if (lcode != LicenseInfo.EVALUATION_CODE && lcode != LicenseInfo.LICENSED_CODE
				&& lcode != LicenseInfo.SPECIAL_EVALUATION_CODE)
			throw new IllegalArgumentException();

		m_licensecode = lcode;
	}

	/**
	 * Sets the major product version (0-9)
	 */
	public void setMajorVersion(int ver) {
		if (ver < 0 || ver > 9) {
			TSUtils.printMessage("SerialNumber Illegal major version: " + ver);
			throw new IllegalArgumentException();
		}

		m_majorversion = ver;
	}

	/**
	 * Sets the minor product version (0-9)
	 */
	public void setMinorVersion(int ver) {
		if (ver < 0 || ver > 9)
			throw new IllegalArgumentException();

		m_minorversion = ver;
	}

	/**
	 * Sets the product code
	 */
	public void setProductCode(char c) {
		AppLicenseSettings als = (AppLicenseSettings) ComponentMgr.lookup(AppLicenseSettings.COMPONENT_ID);
		assert (als != null);
		char[] prodcodes = als.getProductCodes();
		assert (prodcodes != null);
		if (prodcodes == null) {
			assert (false);
			throw new IllegalArgumentException();
		}

		for (int index = 0; index < prodcodes.length; index++) {
			if (c == prodcodes[index]) {
				m_productcode = c;
				return;
			}
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the sequence number
	 */
	public void setSequenceNumber(int seqno) {
		m_seqnumber = seqno;
	}

	/**
	 * Sets the subminor version (0-9)
	 */
	public void setSubMinorVersion(int ver) {
		if (ver < 0 || ver > 9)
			throw new IllegalArgumentException();

		m_subminorversion = ver;
	}

	/**
	 * Sets the stamp
	 */
	public void setStamp(short stamp) {
		m_stamp = stamp;
	}

	public String toString() {
		return generateSerialString2(this);
	}

	/**
	 * This class generates a serial number for Abeille
	 * 
	 * @param sequencenumber
	 *            a unique sequence number that identifies the license
	 * @param licenseCode
	 *            a code that determines if the license is an evaluation or an
	 *            actual licensed copy. (either E or L)
	 */
	public static String generateSerialString(SerialNumber sn) {
		int sequencenumber = sn.getSequenceNumber();
		char licenseCode = sn.getLicenseCode();
		char productCode = sn.getProductCode();

		String seqhex = padString(Integer.toHexString(sequencenumber), SEQUENCE_SIZE);
		String seqhi = seqhex.substring(0, 3);
		String seqlo = seqhex.substring(3, 6);

		Calendar c = sn.getDate();
		String monhex = Integer.toHexString(c.get(Calendar.MONTH) + 1);
		monhex = padString(monhex, MONTH_SIZE);

		String dayhex = Integer.toHexString(c.get(Calendar.DAY_OF_MONTH));
		dayhex = padString(dayhex, DAY_SIZE);

		String yearhex = Integer.toHexString(c.get(Calendar.YEAR) - 2000);
		yearhex = padString(yearhex, YEAR_SIZE);

		StringBuffer serialbuff = new StringBuffer();
		serialbuff.append(SerialNumber.VERSION_NUMBER);
		serialbuff.append(productCode);
		serialbuff.append(sn.getMajorVersion());
		serialbuff.append(sn.getMinorVersion());
		serialbuff.append(sn.getSubMinorVersion());
		serialbuff.append(licenseCode);
		serialbuff.append(monhex);
		serialbuff.append(SERIAL_DELIMITER);
		serialbuff.append(seqhi);
		serialbuff.append(dayhex);
		serialbuff.append(SERIAL_DELIMITER);
		serialbuff.append(seqlo);
		serialbuff.append(yearhex);

		serialbuff.append(SERIAL_DELIMITER);
		String stampstr = Integer.toHexString(sn.getStamp());
		stampstr = padString(stampstr, STAMP_SIZE);
		serialbuff.append(stampstr);
		return serialbuff.toString();
	}

	/**
	 * This class generates a serial number for Abeille 1.1
	 * 
	 * @param sequencenumber
	 *            a unique sequence number that identifies the license
	 * @param licenseCode
	 *            a code that determines if the license is an evaluation or an
	 *            actual licensed copy. (either E or L)
	 */
	public static String generateSerialString2(SerialNumber sn) {
		int sequencenumber = sn.getSequenceNumber();
		char licenseCode = sn.getLicenseCode();
		char productCode = sn.getProductCode();

		String seqhex = padString(Integer.toHexString(sequencenumber), SEQUENCE_SIZE);
		String seqhi = seqhex.substring(0, 3);
		String seqlo = seqhex.substring(3, 6);

		Calendar c = sn.getDate();
		String monhex = Integer.toHexString(c.get(Calendar.MONTH) + 1);
		monhex = padString(monhex, MONTH_SIZE);

		String dayhex = Integer.toHexString(c.get(Calendar.DAY_OF_MONTH));
		dayhex = padString(dayhex, DAY_SIZE);

		String yearhex = Integer.toHexString(c.get(Calendar.YEAR) - 2000);
		yearhex = padString(yearhex, YEAR_SIZE);

		StringBuffer serialbuff = new StringBuffer();
		serialbuff.append(SerialNumber.VERSION_NUMBER);
		serialbuff.append(productCode);
		serialbuff.append(licenseCode);
		serialbuff.append(monhex);
		serialbuff.append(SERIAL_DELIMITER);
		serialbuff.append(seqhi);
		serialbuff.append(dayhex);
		serialbuff.append(SERIAL_DELIMITER);
		serialbuff.append(seqlo);
		serialbuff.append(yearhex);

		serialbuff.append(SERIAL_DELIMITER);
		String stampstr = Integer.toHexString(sn.getStamp());
		stampstr = padString(stampstr, STAMP_SIZE);
		serialbuff.append(stampstr);
		return serialbuff.toString();
	}

	/**
	 * Preprends zeros to a string if the string length is less than setSize
	 */
	private static String padString(String padee, int setSize) {
		assert (setSize < m_zeros.length());
		assert (padee.length() <= setSize);

		if (padee.length() < setSize) {
			padee = m_zeros.substring(0, setSize - padee.length()) + padee;
		}
		return padee;
	}

}
