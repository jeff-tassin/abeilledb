package com.jeta.abeille.gui.query;

import java.awt.Color;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.ModelerEvent;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.ModelViewModel;
import com.jeta.abeille.gui.model.ModelViewModelEvent;
import com.jeta.abeille.gui.model.TableWidget;

import com.jeta.abeille.query.ConstraintNode;
import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.Reportable;
import com.jeta.abeille.query.TSQuery;
import com.jeta.abeille.query.SQLBuilder;
import com.jeta.abeille.query.SQLBuilderFactory;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.EmptyCollection;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the model for the query builder
 * 
 * @author Jeff Tassin
 */
public class QueryModel extends ModelViewModel implements JETAExternalizable {
	static final long serialVersionUID = 3891047627552157151L;

	public static final int VERSION = 1;

	private Catalog m_catalog;

	private Schema m_schema;

	/** The list of reportables for this query (Reportable objects) */
	private LinkedList m_reportables;

	/** The list of constraints for this query (QueryConstraint objects) */
	private LinkedList m_constraints;

	/** Flag that indicates if the query should return distinct results */
	private Boolean m_distinct;

	/**
	 * Flag that indicates if all table and column names should be fully
	 * qualfied with catalog.schema in the generated SQL
	 */
	private Boolean m_qualified = Boolean.TRUE;

	/** the sql that the paths, constraints, and tables in this model represent */
	private transient String m_sql;

	private static final String QUERY_MODEL = "querybuilder.model";

	/** event for this model when the name of the model changes */
	public static final int MODEL_NAME_CHANGED = 100;

	/**
	 * default ctor for serialization
	 */
	public QueryModel() {

	}

	/**
	 * Standard constructor when creating queries
	 * 
	 * @param connection
	 *            this is the database connection. We need this to load the
	 *            metadata and links for tables we are working with
	 * @param name
	 *            this is the name for this query.
	 * @param tag
	 *            this is a unique identifier for this query
	 */
	public QueryModel(TSConnection tsconn) {
		super("query.builder", new ModelerModel(tsconn), tsconn);
		m_reportables = new LinkedList();
		m_constraints = new LinkedList();
		m_catalog = tsconn.getDefaultCatalog();
		m_schema = tsconn.getCurrentSchema(m_catalog);
	}

	/**
	 * Adds a constraint to this reportable
	 */
	public void addConstraint(QueryConstraint c) {
		m_constraints.add(c);
		m_sql = null;

		if (TSUtils.isDebug()) {
			Collection ctables = c.getTables();
			Iterator citer = ctables.iterator();
			while (citer.hasNext()) {
				TableId id = (TableId) citer.next();
				assert (id != null);
			}
		}
	}

	/**
	 * Adds a reportable to this query
	 */
	public void addReportable(Reportable reportable) {
		m_reportables.add(reportable);
		m_sql = null;
	}

	/**
	 * Excludes the widget (table) from the query generation
	 */
	void excludeWidget(TableWidget tw) {
		tw.getModel().setBackground(Color.gray);
		tw.getModel().setForeground(Color.white);
		tw.getModel().setProperty(QueryNames.ID_EXCLUDE_TABLE, Boolean.TRUE);
		tw.repaint();
	}

	/**
	 * Includes the widget (table) in the query generation
	 */
	void includeWidget(TableWidget tw) {
		tw.getModel().setBackground(null);
		tw.getModel().setForeground(null);
		tw.getModel().setProperty(QueryNames.ID_EXCLUDE_TABLE, null);
		tw.repaint();
	}

	public Catalog getCatalog() {
		return m_catalog;
	}

	public Schema getSchema() {
		return m_schema;
	}

	/**
	 * @return the constraints in the query
	 */
	public Collection getConstraints() {
		return m_constraints;
	}

	/**
	 * @return the reportables in the query
	 */
	public Collection getReportables() {
		return m_reportables;
	}

	/**
	 * @return the sql for this query
	 */
	public String getSQL() {
		if (m_sql == null) {
			// for debugging purposes
			validateModel();

			Collection tables = getSpecifiedTables();
			LinkModel linkmodel = getSpecifiedLinkModel(tables);
			Collection constraints = getConstraints();
			Collection reportables = getReportables();

			if (TSUtils.isDebug()) {
				System.out.println("---------------- Query Definition ----------- ");
				Iterator iter = tables.iterator();
				while (iter.hasNext()) {
					TableId tableid = (TableId) iter.next();
					System.out.println(tableid.getFullyQualifiedName());
				}

				if (linkmodel instanceof DefaultLinkModel) {
					DefaultLinkModel dlm = (DefaultLinkModel) linkmodel;
					dlm.print();
				}
			}

			try {
				SQLBuilder builder = SQLBuilderFactory.createBuilder(getConnection());
				if (isQualified()) {
					m_sql = builder.build(isDistinct(), tables, linkmodel, constraints, reportables, null, null);
				} else {
					m_sql = builder.build(isDistinct(), tables, linkmodel, constraints, reportables, getCatalog(),
							getSchema());
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}

		return m_sql;
	}

	/**
	 * @return a collection of TableId objects that are explicitly specified for
	 *         the query. Some tables in the model might be marked as 'excluded'
	 *         so we don't include those tables.
	 */
	public Collection getSpecifiedTables() {
		LinkedList results = new LinkedList();
		Collection widgets = getTableWidgets();
		Iterator iter = widgets.iterator();
		while (iter.hasNext()) {
			boolean include = true;
			TableWidget w = (TableWidget) iter.next();
			Object prop = w.getModel().getProperty(QueryNames.ID_EXCLUDE_TABLE);
			if (prop instanceof Boolean) {
				Boolean bval = (Boolean) prop;
				if (bval.booleanValue())
					include = false;
			}

			if (include)
				results.add(w.getTableId());

		}
		return results;
	}

	public boolean isDistinct() {
		if (m_distinct == null)
			return false;
		else
			return m_distinct.booleanValue();
	}

	/**
	 * Flag that indicates if all table and column names should be fully
	 * qualfied with catalog.schema in the generated SQL
	 */
	public boolean isQualified() {
		if (m_qualified == null)
			return false;
		else
			return m_qualified.booleanValue();
	}

	/**
	 * Removes all constraint objects from this query
	 */
	public void removeConstraints() {
		m_constraints.clear();
		m_sql = null;
	}

	/**
	 * Removes all reportable objects from this query
	 */
	public void removeReportables() {
		m_reportables.clear();
		m_sql = null;
	}

	/**
	 * Sets the catalog and schema for this proxy
	 */
	public void set(Catalog catalog, Schema schema) {
		m_catalog = catalog;
		m_schema = schema;
	}

	public void setDistinct(boolean distinct) {
		m_distinct = Boolean.valueOf(distinct);
	}

	/**
	 * Flag that indicates if all table and column names should be fully
	 * qualfied with catalog.schema in the generated SQL
	 */
	public void setQualified(boolean qualified) {
		m_qualified = Boolean.valueOf(qualified);
	}

	/**
	 * Removes the given table widget from the canvas. This also removes all
	 * links connected to that table
	 */
	public void removeWidget(TableWidget w) {
		Collection inlinks = getLinkModel().getLinks(w.getTableId());
		Iterator liter = inlinks.iterator();
		while (liter.hasNext()) {
			Link link = (Link) liter.next();
			if (link.isUserDefined())
				getModeler().removeUserLink(link);
		}
		super.removeWidget(w);
	}

	/**
	 * Diagnostic routine that prints the joins to the console
	 */
	public void printJoins() {
		Collection tables = getTables();
		LinkModel linkmodel = getLinkModel();
		Collection constraints = getConstraints();
		Collection reportables = getReportables();

		TreeSet constraintables = new TreeSet();
		Iterator iter = constraints.iterator();
		while (iter.hasNext()) {
			QueryConstraint qc = (QueryConstraint) iter.next();
			ConstraintNode node = qc.getConstraintNode();
			if (node != null)
				constraintables.addAll(node.getTables());
		}

		TreeSet reportabletables = new TreeSet();
		iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();
			reportabletables.add(r.getTableId());
		}

		com.jeta.abeille.query.SQLJoiner.buildJoins(linkmodel, tables, constraintables, reportabletables);
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire this class
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		int version = in.readInt();
		m_catalog = (Catalog) in.readObject();
		m_schema = (Schema) in.readObject();
		m_constraints = (LinkedList) in.readObject();
		m_reportables = (LinkedList) in.readObject();
		m_distinct = (Boolean) in.readObject();
		try {
			m_qualified = (Boolean) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * we need to iterate over all widgets to test if they are excluded. If
		 * so, they background color needs to be set here
		 */
		Collection widgets = getTableWidgets();
		Iterator iter = widgets.iterator();
		while (iter.hasNext()) {
			TableWidget tw = (TableWidget) iter.next();
			if (Boolean.TRUE.equals(tw.getModel().getProperty(QueryNames.ID_EXCLUDE_TABLE))) {
				excludeWidget(tw);
			}
		}
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_catalog);
		out.writeObject(m_schema);
		out.writeObject(m_constraints);
		out.writeObject(m_reportables);
		out.writeObject(Boolean.valueOf(isDistinct()));
		out.writeObject(Boolean.valueOf(isQualified()));
	}

}
