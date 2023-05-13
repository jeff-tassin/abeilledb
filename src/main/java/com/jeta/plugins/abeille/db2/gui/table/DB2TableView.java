package com.jeta.plugins.abeille.db2.gui.table;

import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.table.generic.GenericTableView;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.plugins.abeille.db2.gui.checks.DB2ChecksModel;
import com.jeta.plugins.abeille.db2.gui.checks.DB2ChecksView;

import com.jeta.plugins.abeille.db2.gui.triggers.DB2TriggersModel;
import com.jeta.plugins.abeille.db2.gui.triggers.DB2TriggersView;

/**
 * This view displays all the properties for a DB2 table in a JTabbedPane
 * 
 * @author Jeff Tassin
 */
public class DB2TableView extends GenericTableView {
	/** model for triggers for the selected table */
	private DB2TriggersModel m_triggersmodel;

	/** model for check constraints for the selected table */
	private DB2ChecksModel m_checksmodel;

	/**
	 * ctor
	 */
	public DB2TableView(TSConnection conn) {
		super(conn);
		insertView(I18N.getLocalizedMessage("Checks"), null, createChecksView(), 5);
		insertView(I18N.getLocalizedMessage("Triggers"), null, createTriggersView(), 6);
	}

	/**
	 * Creates the view that displays the constraints for this table
	 */
	private TSPanel createChecksView() {
		m_checksmodel = new DB2ChecksModel(getConnection(), null);
		DB2ChecksView view = new DB2ChecksView(m_checksmodel);
		return view;
	}

	/**
	 * Creates the view that displays the triggers for this table
	 */
	private TSPanel createTriggersView() {
		m_triggersmodel = new DB2TriggersModel(getConnection(), null);
		DB2TriggersView view = new DB2TriggersView(m_triggersmodel);
		return view;
	}

	/**
	 * Sets the current table id for the view. All tabs are updated to display
	 * the properties for the given table
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		try {
			m_triggersmodel.setTableId(tableId);
			m_checksmodel.setTableId(tableId);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

}
