package com.jeta.abeille.query;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class provides the logic to build SQL from a query definition for MySQL.
 * MySQL is different than the standard SQL builder because MySQL requires the
 * FROM tables to include not only those tables specified in the containts and
 * reportables but also those tables specified in the JOINS.
 * 
 * @author Jeff Tassin
 */
public class MySQLSQLBuilder implements SQLBuilder {
	/**
	 * This is a list of Expression objects that match (and are ordered) the
	 * constraints for the built sql
	 */
	private LinkedList m_constraintExpressions = new LinkedList();

	/**
	 * Builds the sql command from the given query builder objects
	 * 
	 * @param tables
	 *            the collection of tables that are used for making the joins
	 * @param linkmodel
	 *            the collection of links that join the set of given tables
	 * @param constraints
	 *            a collection of QueryConstraint objects
	 * @param reportbles
	 *            the columns that we wish to report
	 */
	public String build(boolean distinct, Collection qtables, LinkModel qlinkmodel, Collection constraints,
			Collection reportables, Catalog currentCatalog, Schema currentSchema) throws SQLException {
		TSUtils.printMessage("SQLBuilder  catalog: " + currentCatalog + "  schema: " + currentSchema);

		StringBuffer sql = new StringBuffer("SELECT ");
		if (distinct)
			sql.append("DISTINCT ");

		sql.append(buildReportables(reportables, currentCatalog, currentSchema));
		sql.append("\nFROM ");

		StringBuffer wherebuff = new StringBuffer();
		Collection reportabletables = buildWhere(wherebuff, qtables, qlinkmodel, constraints, reportables,
				currentCatalog, currentSchema);
		String fromstr = buildFrom(reportabletables);
		if (fromstr != null)
			sql.append(fromstr);

		sql.append(wherebuff.toString());
		return sql.toString();

	}

	/**
	 * Builds the where portion of a sql command. Automatically creates the
	 * joins and constraints from the given parameters
	 * 
	 * @param tables
	 *            the collection of tables that are used for making the joins
	 * @param linkmodel
	 *            the collection of links that join the set of given tables
	 * @param constraints
	 *            a collection of QueryConstraint objects
	 * @param reportbles
	 *            the columns that we wish to report
	 */
	public String buildWhere(Collection qtables, LinkModel qlinkmodel, Collection constraints, Collection reportables,
			Catalog currentCatalog, Schema currentSchema) {
		StringBuffer wherebuff = new StringBuffer();
		buildWhere(wherebuff, qtables, qlinkmodel, constraints, reportables, currentCatalog, currentSchema);
		return wherebuff.toString();
	}

	/**
	 * Builds the where portion of a sql command. Automatically creates the
	 * joins and constraints from the given parameters
	 * 
	 * @param tables
	 *            the collection of tables that are used for making the joins
	 * @param linkmodel
	 *            the collection of links that join the set of given tables
	 * @param constraints
	 *            a collection of QueryConstraint objects
	 * @param reportbles
	 *            the columns that we wish to report
	 */
	public Collection buildWhere(StringBuffer wherebuff, Collection qtables, LinkModel qlinkmodel,
			Collection constraints, Collection reportables, Catalog currentCatalog, Schema currentSchema) {
		// convert the constraints and reportables to TableId objects for the
		// SQLJoiner
		TreeSet constraintables = new TreeSet();
		Iterator iter = constraints.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			ConstraintNode node = qc.getConstraintNode();
			// node.print();
			if (node != null)
				constraintables.addAll(node.getTables());
		}

		TreeSet reportabletables = new TreeSet();
		iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();
			reportabletables.add(r.getTableId());
		}

		boolean band = false;

		Collection joins = SQLJoiner.buildJoins(qlinkmodel, qtables, constraintables, reportabletables);
		if (joins.size() > 0) {
			band = true;
			Iterator jiter = joins.iterator();
			while (jiter.hasNext()) {
				Link link = (Link) jiter.next();
				wherebuff.append(buildJoin(link, currentCatalog, currentSchema));
				if (jiter.hasNext())
					wherebuff.append("\nAND ");

				reportabletables.add(link.getSourceTableId());
				reportabletables.add(link.getDestinationTableId());
			}
		}
		reportabletables.addAll(constraintables);

		if (band && constraints.size() > 0) {
			wherebuff.append("\nAND ");
		}

		if (constraints.size() > 0) {
			wherebuff.append(buildConstraints(constraints, currentCatalog, currentSchema));
		}

		if (wherebuff.length() > 0) {
			wherebuff.insert(0, "\nWHERE ");
		}

		return reportabletables;
	}

	/**
	 * Builds the constraint portion of a SQL command from a collection of
	 * QueryConstraint objects.
	 */
	public String buildConstraints(Collection constraints, Catalog currentCatalog, Schema currentSchema) {
		// printConstraints( constraints );
		StringBuffer sql = new StringBuffer();
		boolean bfirst = true;
		Iterator iter = constraints.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			if (!bfirst) {
				sql.append(" ");
				sql.append(qc.getLogicalConnective().toString());
				sql.append(" ");
			}

			// qc.print();
			if (qc.isValid()) {
				ConstraintNode cnode = qc.getConstraintNode();
				sql.append(cnode.toSQL(currentCatalog, currentSchema));
				m_constraintExpressions.addAll(cnode.getExpressions());
			} else {
				// just append what the user typed, that's all we can do
				sql.append(qc.toString());
			}
			bfirst = false;
		}
		return sql.toString();
	}

	/**
	 * Given a start table, this method traverses the links until it finds a
	 * leaf table
	 * 
	 * @param already_joined
	 *            a set of tables that have already been joined. We need this to
	 *            avoid circular joins
	 */
	private Collection buildJoins(TreeSet already_joined, TableId tableId, LinkModel model) {
		// System.out.println( "buildJoins : " + tableId );

		LinkedList joins = new LinkedList();
		Collection outlinks = model.getOutLinks(tableId);
		Iterator iter = outlinks.iterator();
		while (iter.hasNext()) {
			Link link = (Link) iter.next();
			TableId desttable = link.getDestinationTableId();
			// now we have a join
			joins.add(link);
			if (already_joined.contains(desttable)) {
				// System.out.println( "StandardSQLBuilder found cicular join: "
				// + tableId );
			} else {
				already_joined.add(desttable);
				Collection subjoins = buildJoins(already_joined, desttable, model);
				joins.addAll(subjoins);
			}
		}
		return joins;
	}

	/**
	 * Builds a SQL component for a join for the given link. Does not qualify
	 * the table names
	 */
	public static String buildJoin(Link link) {
		// System.out.println( "buildJoin : " + link );
		StringBuffer join = new StringBuffer();
		join.append(link.getSourceTableName());
		join.append(".");
		join.append(link.getSourceColumn());
		join.append("=");
		join.append(link.getDestinationTableName());
		join.append(".");
		join.append(link.getDestinationColumn());
		return join.toString();
	}

	/**
	 * Builds a SQL component for a join for the given link. Qualifies the table
	 * names with their catalog/schema.
	 */
	public static String buildJoinQualified(Link link) {
		// System.out.println( "buildJoin : " + link );
		StringBuffer join = new StringBuffer();
		join.append(link.getSourceTableId().getFullyQualifiedName());
		join.append(".");
		join.append(link.getSourceColumn());
		join.append("=");
		join.append(link.getDestinationTableId().getFullyQualifiedName());
		join.append(".");
		join.append(link.getDestinationColumn());
		return join.toString();
	}

	/**
	 * Builds a SQL component for a join for the given link. Qualifies the names
	 * if the table catalog/schema does not equal the currentCatalog and/or
	 * currentSchema and both the currentCatalog and currentSchema are not null.
	 */
	public static String buildJoin(Link link, Catalog currentCatalog, Schema currentSchema) {
		// System.out.println( "buildJoin : " + link );
		StringBuffer join = new StringBuffer();

		join.append(DbUtils.getQualifiedName(currentCatalog, currentSchema, link.getSourceTableId()));
		join.append(".");
		join.append(link.getSourceColumn());

		join.append("=");
		join.append(DbUtils.getQualifiedName(currentCatalog, currentSchema, link.getDestinationTableId()));
		join.append(".");
		join.append(link.getDestinationColumn());

		return join.toString();
	}

	/**
	 * Builds the from portion of a sql string form a list of reportables
	 */
	public String buildFrom(Collection tableSet) {
		TreeSet tables = new TreeSet();
		boolean bfirst = true;
		StringBuffer from = new StringBuffer();
		Iterator iter = tableSet.iterator();
		while (iter.hasNext()) {
			TableId id = (TableId) iter.next();
			if (!tables.contains(id)) {
				tables.add(id);
				if (bfirst)
					bfirst = false;
				else
					from.append(", ");

				from.append(id.getFullyQualifiedName());
			}
		}
		return from.toString();
	}

	/**
	 * Builds a portion of sql string from a list of reportables
	 */
	public String buildReportables(Collection reportables, Catalog currentCatalog, Schema currentSchema) {
		StringBuffer reports = new StringBuffer();
		Iterator iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();

			reports.append(DbUtils.getQualifiedName(currentCatalog, currentSchema, r.getColumn()));

			String outputname = r.getOutputName();
			if (outputname != null) {
				outputname = outputname.trim();
				if (outputname.length() > 0) {
					reports.append(" AS ");
					reports.append("\"");
					reports.append(outputname);
					reports.append("\"");
				}
			}
			if (iter.hasNext())
				reports.append(", ");
		}
		return reports.toString();
	}

	/**
	 * @return a collection of Expression objects that correspond to the
	 *         constraints in the build SQL. The order of the expressions is the
	 *         same as the order of the SQL so you can use this are part of a
	 *         prepared statement sequence.
	 */
	public Collection getConstraintExpressions() {
		return m_constraintExpressions;
	}

	/**
	 * Debugging
	 */
	private void printConstraints(Collection constraints) {
		// System.out.println(
		// "-----------StandardSQLBuilder.printConstraints----------" );
		Iterator iter = constraints.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			qc.print();
		}
	}

	/**
	 * Dump the link model
	 */
	public void printLinkModel(LinkModel linkmodel) {
		Collection ltables = linkmodel.getTables();
		Iterator iter = ltables.iterator();
		while (iter.hasNext()) {
			TableId id = (TableId) iter.next();
			// System.out.println( "link table: " + id );
			Collection inlinks = linkmodel.getInLinks(id);
			// System.out.println( "dumping in links for: " + id );
			Iterator liter = inlinks.iterator();
			while (liter.hasNext()) {
				Link l = (Link) liter.next();
				l.print();
			}

			System.out.println("dumping out links for: " + id);
			Collection outlinks = linkmodel.getOutLinks(id);
			liter = outlinks.iterator();
			while (liter.hasNext()) {
				Link l = (Link) liter.next();
				l.print();
			}

		}
	}

}
