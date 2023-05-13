package com.jeta.abeille.query;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TSConnection;

/**
 * Factory class for creating SQLBuilders
 * 
 * @author Jeff Tassin
 */
public class SQLBuilderFactory {

	public static SQLBuilder createBuilder(TSConnection conn) {
		return new MySQLSQLBuilder();
	}
}
