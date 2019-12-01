package com.jeta.abeille.database.utils;

import com.jeta.abeille.database.model.TSConnection;

/**
 * This is an abstract factory that creates formatter objects for converting
 * object from a SQL result set to strings.
 * 
 * @author Jeff Tassin
 */
public class SQLFormatterFactory {
	/**
	 * Private ctor. Use getInstance
	 */
	private SQLFormatterFactory() {

	}

	/**
	 * @return an instance of a formatter factory for the given database
	 *         connection
	 */
	public static SQLFormatterFactory getInstance(TSConnection connection) {
		// for now just return default
		return new SQLFormatterFactory();
	}

	/**
	 * Creates a formatter object
	 */
	public SQLFormatter createFormatter() {
		return new DefaultSQLFormatter();
	}

}
