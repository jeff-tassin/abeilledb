package com.jeta.abeille.gui.query;

import java.awt.datatransfer.DataFlavor;

public class QueryFlavor {
	/** this is used to indentify a query */
	public static final DataFlavor QUERY = new DataFlavor(com.jeta.abeille.query.TSQuery.class, "query");

	/** sql model */
	public static final DataFlavor SQLMODEL = new DataFlavor(com.jeta.abeille.gui.query.SQLModel.class, "sql");

	/** this is used to indentify a query constraint */
	public static final DataFlavor QUERY_CONSTRAINT = new DataFlavor(com.jeta.abeille.query.QueryConstraint.class,
			"query.constraint");
}
