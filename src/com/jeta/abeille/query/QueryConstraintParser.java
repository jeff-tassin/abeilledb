package com.jeta.abeille.query;

import java.io.StringReader;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.query.constraintparser.ConstraintParser;
import com.jeta.abeille.query.constraintparser.ParseException;

import com.jeta.foundation.utils.TSUtils;

public class QueryConstraintParser {
	private Catalog m_defaultcatalog;
	private Schema m_defaultschema;
	private TSConnection m_connection;

	public QueryConstraintParser(TSConnection connection, Catalog defaultCatalog, Schema defaultSchema) {
		if (defaultCatalog == null)
			defaultCatalog = Catalog.VIRTUAL_CATALOG;

		if (defaultSchema == null)
			defaultSchema = Schema.VIRTUAL_SCHEMA;

		m_defaultcatalog = defaultCatalog;
		m_defaultschema = defaultSchema;
		m_connection = connection;
	}

	/**
	 * Parses a string that contains a query constraint.
	 * 
	 * @param constraintExpression
	 *            the string to parse
	 * @return the resulting constraint node if the parsing was successful
	 */
	public ConstraintNode parseNode(String constraintExpression) {
		try {
			return parseNode2(constraintExpression);
		} catch (Exception e) {
			TSUtils.printException(e);
			return null;
		} catch (Error e) {
			TSUtils.printException(e);
			// @todo fix this guy you don't need to be trapping errors
			// to simulate: type demo.cval = 1 * as a constraint
			return null;
		}
	}

	/**
	 * Forward to our javacc parser
	 */
	private ConstraintNode parseNode2(String constraintExpression) throws ParseException {
		StringReader reader = new StringReader(constraintExpression);
		ConstraintParser parser = new ConstraintParser(reader);
		parser.initialize(m_connection, m_defaultcatalog, m_defaultschema);
		return parser.parse();
	}

}
