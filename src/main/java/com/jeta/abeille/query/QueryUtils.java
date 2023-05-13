package com.jeta.abeille.query;

import java.util.*;
import com.jeta.abeille.database.model.*;
import com.jeta.foundation.utils.*;

public class QueryUtils {
	public static String generateCSVString(Iterator ids) {
		StringBuffer fromsql = new StringBuffer();

		while (ids.hasNext()) {
			Object obj = ids.next();
			fromsql.append(obj.toString());
			if (ids.hasNext())
				fromsql.append(", ");
		}
		return fromsql.toString();
	}

	public static String generatePATHSQLComponent(Iterator links) {
		StringBuffer sql = new StringBuffer();
		while (links.hasNext()) {
			Link link = (Link) links.next();
			// DbKey sourcekey = link.getCandidateKey();
			// DbKey destkey = link.getForeignKey().getLocalKey();
			// sql.append( generateLINKSQLComponent( link.getSourceTableId(),
			// sourcekey, link.getDestinationTableId(), destkey ) );
			// if ( links.hasNext() )
			// sql.append( " AND " );
		}

		return sql.toString();
	}

	public static String generateLINKSQLComponent(TableId sourceTable, DbKey sourceKey, TableId destTable, DbKey destKey) {
		StringBuffer sql = new StringBuffer();
		assert (sourceKey.getColumnCount() == destKey.getColumnCount());
		for (int index = 0; index < sourceKey.getColumnCount(); index++) {
			sql.append(sourceTable.toString());
			sql.append(".");
			sql.append(sourceKey.getColumn(index));
			sql.append(" = ");
			sql.append(destTable.toString());
			sql.append(".");
			sql.append(destKey.getColumn(index));
		}
		return sql.toString();
	}

}
