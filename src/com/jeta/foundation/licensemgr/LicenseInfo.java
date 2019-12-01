/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents the data in a JETA license
 * 
 * @author Jeff Tassin
 */
public class LicenseInfo {
	private String m_name;
	private String m_company;
	private String m_email;
	private char m_licensetype;
	private String m_serialno;
	private String m_expires;

	public static final char LICENSE_DELIMITER;
	public static final char EVALUATION_CODE;
	public static final char LICENSED_CODE;
	public static final String DATE_FORMAT = "MM-dd-yyyy";
	public static final char SPECIAL_EVALUATION_CODE;

	static {
		LICENSE_DELIMITER = getDelimiter();
		EVALUATION_CODE = getEvalCode();
		LICENSED_CODE = getLicensedCode();
		SPECIAL_EVALUATION_CODE = getSpecialEvalCode();
	}

	/**
	 * ctor
	 */
	public LicenseInfo(String name, String company, String email, char licensetype, String serialno, String expires) {
		m_name = name;
		m_company = company;
		m_email = email;
		m_licensetype = licensetype;
		m_serialno = serialno;
		m_expires = expires;
	}

	static char getDelimiter() {
		byte b = 24;
		b += 11;
		return (char) b;
	}

	static char getEvalCode() {
		byte b = 59;
		b += 10;
		return (char) b;
	}

	static char getSpecialEvalCode() {
		byte b = 73;
		b += 10;
		return (char) b;
	}

	static char getLicensedCode() {
		byte b = 56;
		b += 20;
		return (char) b;
	}

	/**
	 * Convert the member data into a license string. We don't include the
	 * signature token here. Licenses are of the form:
	 * name|company|email|serialno|licensetype|expdate|signature where expdate
	 * is formatted as: MM-DD-YYYY
	 * 
	 */
	public byte[] getLicenseBytes() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(preProcess(getName()));
		buffer.append(LICENSE_DELIMITER);
		buffer.append(preProcess(getCompany()));
		buffer.append(LICENSE_DELIMITER);
		buffer.append(preProcess(getEmail()));
		buffer.append(LICENSE_DELIMITER);
		buffer.append(getLicenseType());
		buffer.append(LICENSE_DELIMITER);
		buffer.append(getSerialNumber());
		buffer.append(LICENSE_DELIMITER);
		buffer.append(getExpires());
		return buffer.toString().getBytes();
	}

	public String getName() {
		if (m_name == null)
			m_name = "";
		return m_name;
	}

	public String getCompany() {
		if (m_company == null)
			m_company = "";
		return m_company;
	}

	public String getEmail() {
		if (m_email == null)
			m_email = "";
		return m_email;
	}

	public char getLicenseType() {
		if (m_licensetype == '\0')
			m_licensetype = EVALUATION_CODE;
		return m_licensetype;
	}

	public String getSerialNumber() {
		if (m_serialno == null)
			m_serialno = "";
		return m_serialno;
	}

	public String getExpires() {
		if (m_expires == null)
			m_expires = "";
		return m_expires;
	}

	/**
	 * This method takes an input field such as an email address or name and
	 * replaces any characters in that field that match the LICENSE_DELIMITER
	 * character. We replace with the @ character.
	 */
	public String preProcess(String str) {
		if (str == null)
			return "";

		StringBuffer buff = new StringBuffer();
		for (int index = 0; index < str.length(); index++) {
			char c = str.charAt(index);
			if (c == LICENSE_DELIMITER || Character.isISOControl(c))
				buff.append(' ');
			else
				buff.append(c);
		}

		return buff.toString();
	}

}
