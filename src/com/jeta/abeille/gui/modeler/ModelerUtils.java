package com.jeta.abeille.gui.modeler;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.DbUtils;

public class ModelerUtils {
	/**
	 * Iterates over all the columns in the table and makes sure the column
	 * sizes are valid. This is used when modeling a new table from columns in
	 * an existing table. Some JDBC driver (e.g. Postgres) put a column size on
	 * types such as integers and dates. This causes problems when used in a
	 * create table SQL statement, so we check for those sizes here and set to
	 * zero as appropriate
	 */
	public static void validateColumnSizes(TableMetaData tmd) {
		Collection c = tmd.getColumns();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			if (!DbUtils.isAlpha(cmd) || DbUtils.hasPrecision(cmd)) {
				cmd.setColumnSize(0);
				cmd.setScale(0);
			}

			if (cmd.getColumnSize() < 0) {
				cmd.setColumnSize(0);
				cmd.setScale(0);
			}
		}
	}
}
