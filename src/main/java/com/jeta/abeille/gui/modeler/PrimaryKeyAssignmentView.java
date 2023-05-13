package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.utils.ColumnAssignmentPanel;

import com.jeta.foundation.gui.components.TSPanel;

/**
 * 
 * @author Jeff Tassin
 */
public class PrimaryKeyAssignmentView extends TSPanel {
	private ColumnAssignmentPanel m_assignmentview;

	private TSConnection m_connection;

	/**
	 * The model that contains the columns that are available for the primary
	 * key
	 */
	private ColumnsGuiModel m_availablemodel;

	/**
	 * THe model that contains the columns assigned to the primary key
	 */
	private ColumnsGuiModel m_assignedmodel;

	/**
	 * ctor
	 */
	public PrimaryKeyAssignmentView(TSConnection conn, ColumnsGuiModel assignedModel, ColumnsGuiModel availableModel) {
		m_connection = conn;

		assert (conn != null);
		assert (assignedModel != null);
		assert (availableModel != null);

		m_assignmentview = new ColumnAssignmentPanel(conn);
		setLayout(new BorderLayout());
		add(m_assignmentview, BorderLayout.CENTER);

		m_assignedmodel = assignedModel;
		m_availablemodel = availableModel;

		m_assignmentview.addSourceColumns(availableModel.getData());
		m_assignmentview.addAssignedColumns(assignedModel.getData());
	}

	/**
	 * @return the primary key defined by the view
	 */
	public DbKey createPrimaryKey() {
		DbKey pk = new DbKey();
		Collection cols = m_assignmentview.getAssignedColumns();
		Iterator iter = cols.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			pk.addField(cmd.getColumnName());
		}
		return pk;
	}

	/**
	 * @return a collection of ColumnMetaData objects that are assigned to this
	 *         key
	 */
	public Collection getAssignedColumns() {
		return m_assignmentview.getAssignedColumns();
	}

	/**
	 * @return the preferred size for the view
	 */
	public Dimension getPreferredSize() {
		return m_assignmentview.getPreferredSize();
	}

}
