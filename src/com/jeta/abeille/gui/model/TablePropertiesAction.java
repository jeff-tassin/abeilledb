package com.jeta.abeille.gui.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.table.TableFrame;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * Action handler for table properties command. Invokes the TableFrame for the
 * selected table
 */
public class TablePropertiesAction implements ActionListener {
	private TSConnection m_connection;

	private TableIdGetter m_idgetter;

	public TablePropertiesAction(TSConnection connection, TableIdGetter idgetter) {
		m_connection = connection;
		m_idgetter = idgetter;
	}

	/**
	 * ActionListener implementation
	 */
	public void actionPerformed(ActionEvent evt) {
		TablePropertiesAction.showPropertiesDialog(m_connection, m_idgetter);
	}

	/**
	 * @return the last table displayed in the dialog
	 * 
	 */
	public static TableId getLastTable(TSConnection connection) {
		TableId lastid = null;
		ObjectStore os = (ObjectStore) connection.getObjectStore();
		try {
			lastid = (TableId) os.load(com.jeta.abeille.gui.modeler.ModelerNames.ID_LAST_TABLE_ID);
		} catch (Exception e) {
			// noop
		}
		return lastid;
	}

	/**
	 * Invokes the table property dialog using the last table
	 */
	public static void showPropertiesDialog(TSConnection connection) {
		showPropertiesDialog(connection, (TableId) null);
	}

	/**
	 * Invokes the table property dialog using the last table
	 */
	public static void showPropertiesDialog(TSConnection connection, TableIdGetter idgetter) {
		TableId tableid = null;
		if (idgetter == null)
			tableid = TablePropertiesAction.getLastTable(connection);
		else
			tableid = idgetter.getTableId();

		showPropertiesDialog(connection, tableid);
	}

	/**
	 * Invokes the table property dialog using the last table
	 */
	public static void showPropertiesDialog(TSConnection tsconn, TableId tableid) {
		if (tableid == null)
			tableid = TablePropertiesAction.getLastTable(tsconn);

		if (tableid != null) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			TableFrame tableframe = (TableFrame) wsframe.show(TableFrame.class, tsconn);
			if (tableframe != null) {
				tableframe.setTableId(tableid);
				wsframe.show(tableframe);
			}
		}
	}

}
