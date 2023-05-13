package com.jeta.abeille.database.utils;

import java.sql.SQLException;
import java.util.Calendar;

/**
 * This class is responsible for formatting values from/to SQL result sets to
 * and from strings
 */
public interface SQLFormatter {
	/**
	 * Formats a value from a result set
	 * 
	 * @param value
	 *            the value to format
	 * @param datatype
	 *            the sql data type ( java.sql.Types )
	 * @return the formatted value
	 */
	public String format(Object value, int datatype) throws SQLException;

	/**
	 * Formats a date to the correct SQL. Note, this should not be used in
	 * prepared statements. Use prepareDate instead.
	 */
	public String formatDate(int year, int month, int day);

	/**
	 * Formats a timestamp to the correct SQL. Note, this should not be used in
	 * prepared statements. Use prepareDate instead.
	 */
	public String formatTimestamp(Calendar c);

	/**
	 * Formats a string value. Inserts the necessary string delimiters
	 * (typically a single quote) where necessary
	 * 
	 * @param value
	 *            the string value to format
	 * @return the formatted value
	 */
	public String formatString(String value);

	/**
	 * Formates a time to the correct SQL. Note, this should not be used in
	 * prepared statements. Use prepareTime instead.
	 */
	public String formatTime(int hours, int minutes, int seconds, int millisecs);

}
