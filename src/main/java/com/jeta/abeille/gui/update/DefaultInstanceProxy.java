package com.jeta.abeille.gui.update;

import java.sql.SQLException;

import java.util.HashMap;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.RowInstance;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class defaults to getting instance values from a standard single table
 * query.
 * 
 * @author Jeff Tassin
 */
public class DefaultInstanceProxy extends AbstractInstanceProxy {
	/**
	 * this is the model that contains the metadata as well as the query results
	 * that we are showing
	 */
	private InstanceModel m_instancemodel;

	/**
	 * this is a map of ColumnMetaData objects (key) to indices in the result
	 * set
	 */
	private HashMap m_indices;

	/**
	 * ctor
	 */
	public DefaultInstanceProxy(InstanceModel model, QueryResultSet qset) throws SQLException {
		super(qset);
		m_instancemodel = model;

		m_indices = new HashMap();
		ColumnMetaData[] cols = qset.getColumnMetaData();
		for (int index = 0; index < cols.length; index++) {
			m_indices.put(cols[index], new Integer(index));
		}
	}

	/**
	 * Sets the value of the given instance component with the value associated
	 * with the given column meta data
	 */
	public void setValue(InstanceComponent comp, ColumnMetaData cmd) throws SQLException {
		// rowinstances are zero based
		Object value = null;
		try {
			QueryResultSet qset = getQueryResults();
			if (qset == null)
				return;

			RowInstance instance = qset.getRowInstance(qset.getRow());
			Integer index = (Integer) m_indices.get(cmd);
			assert (instance != null);

			if (index != null) {
				value = instance.getObject(index.intValue());
				comp.setValue(value);
			} else {
				comp.setValue(null);
			}
		} catch (ClassCastException cce) {
			TSUtils.printMessage("ClassCastException for: " + cmd + "    value: " + value.getClass());
			TSUtils.printException(cce);
		}
	}
}
