package com.jeta.plugins.abeille.mysql;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableMetaData;

/**
 * Utility class for MySQL
 * 
 * @author Jeff Tassin
 */
public class MySQLUtils {

	/**
	 * @return the column attributes for the given column metadata. If the
	 *         attributes are null, then a new Attribute object is created and
	 *         set in the ColumnMetaData object
	 */
	public static MySQLColumnAttributes getAttributes(ColumnMetaData cmd) {
		MySQLColumnAttributes attr = (MySQLColumnAttributes) cmd.getAttributes();
		if (attr == null)
			cmd.setAttributes(attr);

		return attr;
	}

	/**
	 * @returns the table type for the given table
	 */
	public static MySQLTableType getTableType(TableMetaData localtmd) {
		assert (localtmd != null);
		MySQLTableType ttype = MySQLTableType.Unknown;
		MySQLTableAttributes attr = (MySQLTableAttributes) localtmd.getAttributes();
		if (attr != null) {
			ttype = attr.getTableType();
			if (ttype == null) {
				ttype = MySQLTableType.Unknown;
			}
		}
		return ttype;
	}
}
