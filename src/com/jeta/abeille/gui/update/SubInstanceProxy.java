package com.jeta.abeille.gui.update;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.RowInstance;
import com.jeta.abeille.gui.queryresults.QueryResultSet;
import com.jeta.abeille.query.Reportable;

/**
 * This class allows us to display a partial instance in the InstanceView. This
 * is important in some cases where we want to scroll through foreign keys
 * related in a different table.
 * 
 * @author Jeff Tassin
 */
public class SubInstanceProxy extends AbstractInstanceProxy {
	/**
	 * This is a hash of ColumnMetaData objects (keys) to index in the query
	 * result set. This allows us to quickly lookup the index of the column in
	 * the query results to get the value
	 */
	private HashMap m_reportables;

	/**
	 * ctor
	 */
	public SubInstanceProxy(Collection reportables, QueryResultSet queryResults) {
		super(queryResults);
		int index = 0;
		m_reportables = new HashMap();
		Iterator iter = reportables.iterator();
		while (iter.hasNext()) {
			Reportable r = (Reportable) iter.next();
			m_reportables.put(r.getColumn(), new Integer(index));
			index++;
		}

		/*
		 * try { System.out.println( "SubInstanceProxy.ctor   qset.row = " +
		 * m_qset.getRow() ); } catch( Exception e ) { e.printStackTrace(); }
		 */
	}

	/**
	 * @return the index of the column found in the query result set
	 */
	public int getInstanceIndex(ColumnMetaData cmd) {
		Integer i = (Integer) m_reportables.get(cmd);
		if (i == null)
			return -1;
		else
			return i.intValue();
	}

	/**
	 * Sets the value of the given instance component with the value associated
	 * with the given column meta data
	 */
	public void setValue(InstanceComponent comp, ColumnMetaData cmd) throws SQLException {
		// rowinstances are zero based
		QueryResultSet qset = getQueryResults();
		if (qset == null)
			return;

		if (qset.isEmpty())
			return;

		RowInstance instance = qset.getRowInstance(qset.getRow());
		assert (instance != null);
		int iindex = getInstanceIndex(cmd);
		// System.out.println( "subInstanceproxy.setValue: " +
		// cmd.getQualifiedName() + "   qset.row = " + qset.getRow() +
		// "  iindex = " + iindex );
		if (iindex >= 0) {
			comp.setValue(instance.getObject(iindex));
		}
	}
}
