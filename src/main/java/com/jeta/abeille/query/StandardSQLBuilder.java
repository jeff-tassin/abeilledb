package com.jeta.abeille.query;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.TableId;

/**
 * This class provides the logic to build SQL from a query definition.
 * 
 * @author Jeff Tassin
 */
class StandardSQLBuilder {
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
			Collection reportables) throws SQLException {
		StringBuffer sql = new StringBuffer("SELECT ");
		if (distinct)
			sql.append("DISTINCT ");

		sql.append(buildReportables(reportables));
		sql.append("\nFROM ");

		String fromstr = buildFrom(reportables);
		if (fromstr != null)
			sql.append(fromstr);

		String wherestr = buildWhere(qtables, qlinkmodel, constraints, reportables);
		if (wherestr != null)
			sql.append(wherestr);

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
	public String buildWhere(Collection qtables, LinkModel qlinkmodel, Collection constraints, Collection reportables) {
		StringBuffer wherebuff = new StringBuffer();

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
				wherebuff.append(buildJoin(link));
				if (jiter.hasNext())
					wherebuff.append("\nAND ");
			}
		}

		if (band && constraints.size() > 0) {
			wherebuff.append("\nAND ");
		}

		if (constraints.size() > 0) {
			wherebuff.append(buildConstraints(constraints));
		}

		if (wherebuff.length() > 0) {
			wherebuff.insert(0, "\nWHERE ");
		}
		return wherebuff.toString();
	}

	/**
	 * Builds the constraint portion of a SQL command from a collection of
	 * QueryConstraint objects.
	 */
	public String buildConstraints(Collection constraints) {
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
				// sql.append( cnode.toSQL() );
				assert (false);
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
	 * Builds a SQL component for a join for the given link
	 */
	private static String buildJoin(Link link) {
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
	 * Builds the from portion of a sql string form a list of reportables
	 */
	public String buildFrom(Collection reportables) {
		TreeSet tables = new TreeSet();
		boolean bfirst = true;
		StringBuffer from = new StringBuffer();
		Iterator iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();
			TableId id = r.getTableId();
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
	public String buildReportables(Collection reportables) {
		StringBuffer reports = new StringBuffer();
		Iterator iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();
			String tablename = r.getTableName();
			if (tablename != null && tablename.length() > 0)
				tablename = tablename.trim();

			if (tablename != null && tablename.length() > 0) {
				reports.append(r.getTableName());
				reports.append(".");
			}

			reports.append(r.getColumnName());

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
