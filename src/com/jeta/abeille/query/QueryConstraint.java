package com.jeta.abeille.query;

import java.util.Collection;
import java.util.Iterator;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.query.ConstraintNode;
import com.jeta.abeille.query.LogicalConnective;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.utils.EmptyCollection;

/**
 * This class represents a single constraint as viewed in the constraint view
 * list. constraint1 (e.g. age > 30 and age < 35 ) AND constraint2 (e.g.
 * firstname = 'jeff' ) OR constraint3 (e.g. state = 'tx' ) etc.
 * 
 * @author Jeff Tassin
 */
public class QueryConstraint implements JETAExternalizable {
	static final long serialVersionUID = 6519455898433789854L;

	public static int VERSION = 1;

	/** this is the parsed node */
	private ConstraintNode m_node;

	/** this is text that the user typed */
	private String m_text;

	/**
	 * This is the logical (AND/OR) that connects this constraint with the
	 * PREVIOUS constraint in the list. If this constraint is the first one,
	 * then this guy is undefined (and will be null)
	 */
	private LogicalConnective m_lc;

	/**
	 * The default catalog where this constraint applies
	 */
	private Catalog m_catalog = Catalog.VIRTUAL_CATALOG;

	/**
	 * The default schema where the constraint applies
	 */
	private Schema m_schema = Schema.VIRTUAL_SCHEMA;

	/**
	 * ctor
	 */
	public QueryConstraint() {
		m_lc = LogicalConnective.AND;
	}

	/**
	 * ctor
	 */
	public QueryConstraint(String txt, TSConnection connection, Catalog defaultCatalog, Schema defaultSchema) {
		this();
		if (txt == null)
			txt = "";

		m_text = txt;
		m_catalog = defaultCatalog;
		m_schema = defaultSchema;
		QueryConstraintParser parser = new QueryConstraintParser(connection, defaultCatalog, defaultSchema);
		m_node = parser.parseNode(txt);
	}

	/**
	 * ctor Simple query constraint with one expression
	 */
	public QueryConstraint(TableId tableId, String colName, Operator op, String value, Catalog defaultCatalog,
			Schema defaultSchema) {
		this();
		m_catalog = defaultCatalog;
		m_schema = defaultSchema;
		m_node = new ConstraintNode(tableId, colName, op, value);
	}

	/**
	 * ctor for InstanceView and FormBuilder only
	 */
	public QueryConstraint(LogicalConnective lc, ConstraintNode node) {
		m_lc = lc;
		m_node = node;
	}

	/**
	 * @return true if this constraint node has a column in the given table
	 */
	public boolean contains(TableId tableId) {
		if (isValid())
			return m_node.contains(tableId);
		else
			return false;
	}

	/**
	 * @return the constraint node for this constraint
	 */
	public ConstraintNode getConstraintNode() {
		return m_node;
	}

	/**
	 * @return the logical connective for this constraint
	 */
	public LogicalConnective getLogicalConnective() {
		return m_lc;
	}

	/**
	 * @return all tables (TableId) objects that make up this constraint
	 */
	public Collection getTables() {
		if (isValid()) {
			return m_node.getTables();
		} else {
			return EmptyCollection.getInstance();
		}
	}

	/**
	 * @return true if the constraint has a valid, parsed ConstraintNode
	 */
	public boolean isValid() {
		return (m_node != null);
	}

	/**
	 * Prints this constraint to the console
	 */
	public void print() {
		if (m_node == null) {
			System.out.println("node is null.... " + m_text);
		} else {
			m_node.print();
		}
	}

	/**
	 * This is a helper method that iterates over all expressions in the
	 * constraint and sets the value to ? so the constraint can be used in a
	 * prepared statement.
	 */
	public static void prepareConstraint(QueryConstraint qc) {
		ConstraintNode node = qc.m_node;
		node.prepareConstraint();
	}

	/**
	 * Sets the constraint
	 */
	public void setConstraintNode(ConstraintNode node) {
		m_node = node;
	}

	/**
	 * Sets the logical connective for this constraint
	 */
	public void setLogicalConnective(LogicalConnective lc) {
		m_lc = lc;
	}

	public void set(TSConnection tsconn, Catalog catalog, Schema schema) {
		m_catalog = catalog;
		m_schema = schema;

		if (m_text != null) {
			QueryConstraintParser parser = new QueryConstraintParser(tsconn, catalog, schema);
			m_node = parser.parseNode(m_text);
		}
	}

	/**
	 * @return the string representation of this node
	 */
	public String toString() {
		if (m_text == null) {
			if (m_node != null)
				return m_node.toSQL(m_catalog, m_schema);
			else
				return "";
		} else {
			return m_text;
		}
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_node = (ConstraintNode) in.readObject();
		m_text = (String) in.readObject();
		;
		m_lc = (LogicalConnective) in.readObject();
		m_catalog = (Catalog) in.readObject();
		m_schema = (Schema) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_node);
		out.writeObject(m_text);
		out.writeObject(m_lc);
		out.writeObject(m_catalog);
		out.writeObject(m_schema);
	}

}
