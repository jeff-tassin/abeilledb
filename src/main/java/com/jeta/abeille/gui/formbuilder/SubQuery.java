package com.jeta.abeille.gui.formbuilder;

import java.io.Writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.PreparedStatementWriter;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.abeille.query.ConstraintNode;
import com.jeta.abeille.query.Expression;
import com.jeta.abeille.query.LogicalConnective;
import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.Operator;
import com.jeta.abeille.query.Reportable;
import com.jeta.abeille.query.SQLBuilder;
import com.jeta.abeille.query.SQLBuilderFactory;

/**
 * This class represents a sub query in the form builder
 * 
 * @author Jeff Tassin
 */
public class SubQuery {
	private TreeSet m_tables = new TreeSet();
	private DefaultLinkModel m_links = new DefaultLinkModel();
	private LinkedList m_constrainers = new LinkedList();

	private LinkedList m_reportables = new LinkedList();

	private Statement m_statement;
	private QueryResultSet m_queryset;

	private PreparedStatement m_lastpstmt;

	/**
	 * Adds a column constraint to this sub query. The actual values will be
	 * read later
	 */
	public void addConstraint(ValueProxy proxy) {
		m_constrainers.add(proxy);
	}

	/**
	 * Adds a table to this subquery definition
	 */
	public void addTable(TableId id) {
		m_tables.add(id);
	}

	/**
	 * Adds a link to this query definition
	 */
	public void addLink(Link link) {
		m_links.addLink(link);
	}

	/**
	 * Adds a column to the list of reportables for the query
	 */
	public void addReportable(ColumnMetaData cmd) {
		m_reportables.add(new Reportable(cmd));
	}

	/**
	 * @return true if the given table was previously added to this object
	 */
	public boolean contains(TableId tableid) {
		return m_tables.contains(tableid);
	}

	/**
	 * Executes the underlying query. Note that you must call prepareStatement
	 * first
	 */
	public QueryResultSet executeQuery(ConnectionReference cref, SQLFormatter formatter) throws SQLException {
		TSConnection tsconn = cref.getTSConnection();
		String sql = prepareSQL(tsconn);
		int rtype = tsconn.getResultSetScrollType();
		int concurrency = tsconn.getResultSetConcurrency();

		if (m_lastpstmt != null)
			m_lastpstmt.close();

		PreparedStatement pstmt = cref.getConnection().prepareStatement(sql, rtype, concurrency);
		prepareStatement(pstmt, formatter);
		m_queryset = new QueryResultSet(null, new ResultSetReference(cref, pstmt, pstmt.executeQuery(), null));
		m_queryset.first();
		m_lastpstmt = pstmt;
		return m_queryset;
	}

	/**
	 * @return the constraints for this query (as ValueProxy objects)
	 */
	public Collection getConstraints() {
		return m_constrainers;
	}

	/**
	 * @return the result of the query
	 */
	public QueryResultSet getQueryResults() {
		return m_queryset;
	}

	/**
	 * @return the reportables (as Reportable objects )for this subquery as they
	 *         will be displayed in the query (this will include constraints)
	 */
	public Collection getReportables() {
		LinkedList reportables = new LinkedList(m_reportables);
		Iterator iter = m_constrainers.iterator();
		while (iter.hasNext()) {
			ValueProxy cons = (ValueProxy) iter.next();
			ColumnMetaData cmd = cons.getColumnMetaData();
			Reportable r = new Reportable(cmd);
			if (!reportables.contains(r))
				reportables.add(r);
		}
		return reportables;
	}

	/**
	 * @return the ZERO based index of the given column as found in the query
	 *         result set
	 */
	public int getResultSetIndex(ColumnMetaData cmd) {
		int count = 0;
		Iterator iter = m_reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();
			if (r.getColumn().equals(cmd)) {
				return count;
			}
			count++;
		}

		return -1;
	}

	/**
	 * Builds the query based on the given tables, links, and constraints
	 */
	String prepareSQL(TSConnection conn) throws SQLException {
		Collection reportables = getReportables();
		LinkedList constraints = new LinkedList();

		Iterator iter = m_constrainers.iterator();
		while (iter.hasNext()) {
			ValueProxy cons = (ValueProxy) iter.next();
			ColumnMetaData cmd = cons.getColumnMetaData();
			Operator op = cons.getOperator();
			if (op == null)
				op = Operator.EQUALS;
			Expression expr = new Expression(cmd, op, "?");
			ConstraintNode node = new ConstraintNode(expr);
			if (constraints.size() == 0)
				constraints.add(new QueryConstraint(null, node));
			else
				constraints.add(new QueryConstraint(LogicalConnective.AND, node));
		}

		SQLBuilder builder = SQLBuilderFactory.createBuilder(conn);
		String sql = builder.build(false, m_tables, m_links, constraints, reportables, null, null);
		return sql;
	}

	/**
	 * Builds the query based on the given tables, links, and constraints
	 */
	void prepareStatement(PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		int count = 1;
		Iterator iter = m_constrainers.iterator();
		while (iter.hasNext()) {
			ValueProxy constrainer = (ValueProxy) iter.next();
			constrainer.prepareStatement(count, pstmt, formatter);
			count++;
		}
	}

	/**
	 * Builds the query based on the given tables, links, and constraints
	 */
	public String getPlan(TSConnection conn, SQLFormatter formatter) throws SQLException {
		PreparedStatementWriter pwriter = new PreparedStatementWriter(prepareSQL(conn));
		int count = 1;
		Iterator iter = m_constrainers.iterator();
		while (iter.hasNext()) {
			ValueProxy constrainer = (ValueProxy) iter.next();
			constrainer.prepareStatement(count, pwriter, formatter);
			count++;
		}
		return pwriter.getPreparedSQL();
	}

	/**
	 * Prints this subquery to the console
	 */
	public void print() {
		System.out.println("*******---------- printing sub query ----------******");
		Iterator iter = m_tables.iterator();
		while (iter.hasNext()) {
			TableId tableid = (TableId) iter.next();
			System.out.println("        " + tableid.getFullyQualifiedName());
		}

		m_links.print();
		System.out.println("*******----------------------------------------******");
	}

}
