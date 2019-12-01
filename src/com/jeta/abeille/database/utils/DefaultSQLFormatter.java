package com.jeta.abeille.database.utils;

import java.sql.SQLException;
import java.sql.Date;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * This class is responsible for formatting values from/to SQL result sets to
 * and from strings
 */
public class DefaultSQLFormatter implements SQLFormatter {
	private static DecimalFormat[] m_integerformat;
	private static String[] m_months = { "January", "February", "March", "April", "May", "June", "July", "August",
			"September", "October", "November", "December" };

	static {
		m_integerformat = new DecimalFormat[10];
		m_integerformat[1] = new DecimalFormat("0");
		m_integerformat[2] = new DecimalFormat("00");
		m_integerformat[3] = new DecimalFormat("000");
		m_integerformat[4] = new DecimalFormat("0000");
		m_integerformat[5] = new DecimalFormat("00000");
		m_integerformat[6] = new DecimalFormat("000000");
		m_integerformat[7] = new DecimalFormat("0000000");
		m_integerformat[8] = new DecimalFormat("00000000");
		m_integerformat[9] = new DecimalFormat("000000000");

	}

	/**
	 * Converts a date to a SQL string without any quote characters
	 */
	private String convertDate(Calendar c) {
		// @todo MySQL: Dates must be given in year-month-day order (for
		// example, '98-09-04'), rather than in the month-day-year or
		// day-month-year orders commonly used elsewhere (for example,
		// '09-04-98', '04-09-98').

		// ISO format
		StringBuffer buff = new StringBuffer();
		buff.append(m_months[c.get(Calendar.MONTH)]);
		buff.append(" ");
		buff.append(c.get(Calendar.DAY_OF_MONTH));
		buff.append(" ");
		buff.append(formatInteger(c.get(Calendar.YEAR), 4));
		return buff.toString();
	}

	/**
	 * Converts a time to a SQL string without any quote characters
	 */
	private String convertTime(Calendar c) {
		// ISO format
		StringBuffer buff = new StringBuffer();
		buff.append(formatInteger(c.get(Calendar.HOUR_OF_DAY), 2));
		buff.append(":");
		buff.append(formatInteger(c.get(Calendar.MINUTE), 2));
		buff.append(":");
		buff.append(formatInteger(c.get(Calendar.SECOND), 2));
		return buff.toString();
	}

	/**
	 * Formats a value from a result set
	 * 
	 * @param value
	 *            the value to format
	 * @param datatype
	 *            the sql data type ( java.sql.Types )
	 * @return the formatted value
	 */
	public String format(Object value, int datatype) throws SQLException {
		if (value != null) {
			if (DbUtils.isAlpha(datatype))
				return formatString(value.toString());
			else if (datatype == java.sql.Types.DATE) {
				java.sql.Date date = (java.sql.Date) value;
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				return convertDate(c);
			} else if (datatype == java.sql.Types.TIME) {
				java.sql.Time tm = (java.sql.Time) value;
				Calendar c = Calendar.getInstance();
				c.setTime(tm);
				return convertTime(c);

			} else if (datatype == java.sql.Types.TIMESTAMP) {
				// @todo need to work on this
				java.sql.Timestamp ts = (java.sql.Timestamp) value;
				java.sql.Time tm = new java.sql.Time(ts.getTime());
				Calendar c = Calendar.getInstance();
				c.setTime(tm);
				return formatTimestamp(c);
			} else
				return value.toString();
		} else
			return "";
	}

	/**
	 * Formats an integer with the given number of digits
	 */
	private String formatInteger(int value, int digits) {
		assert (digits < 10);
		DecimalFormat format = m_integerformat[digits];
		return format.format(value);
	}

	/**
	 * Formats a string value. Inserts the necessary string delimiters
	 * (typically a single quote) where necessary
	 * 
	 * @param value
	 *            the string value to format
	 * @return the formatted value
	 */
	public String formatString(String value) {
		if (value != null)
			return DbUtils.toSQL(value, '\'');
		else
			return "";
	}

	/**
	 * Formats a date to the correct SQL
	 */
	public String formatDate(int month, int day, int year) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Formates a time to the correct SQL. Note, this should not be used in
	 * prepared statements. Use prepareTime instead.
	 */
	public String formatTime(int hours, int minutes, int seconds, int millisecs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Formats a timestamp to the correct SQL. Note, this should not be used in
	 * prepared statements. Use prepareDate instead.
	 */
	public String formatTimestamp(Calendar c) {
		StringBuffer buff = new StringBuffer();
		buff.append("'");
		buff.append(convertDate(c));
		buff.append(" ");
		buff.append(convertTime(c));
		buff.append("'");
		return buff.toString();
	}

}
