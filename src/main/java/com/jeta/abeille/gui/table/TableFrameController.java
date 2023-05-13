package com.jeta.abeille.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import javax.swing.JButton;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.model.QueryTableAction;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.utils.TSUtils;

/**
 * The controller class for the TableFrame window
 * 
 * @author Jeff Tassin
 */
public class TableFrameController extends TSController {
	/** the frame we are controlling */
	private TableFrame m_frame;

	/**
	 * ctor
	 */
	public TableFrameController(TableFrame frame) {
		super(frame);
		m_frame = frame;

		JButton btn = (JButton) m_frame.getComponentByName(TableFrame.ID_RELOAD);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				reloadTable();
			}
		});

		assignAction(TableFrame.ID_SHOW_TABLE_FORM, new ShowInstanceViewAction());
		assignAction(TableFrame.ID_QUERY_TABLE, new TableQueryAction());
		m_frame.setUIDirector(new TableFrameUIDirector(m_frame));
	}

	public TableId getTableId() {
		TableSelectorPanel tspanel = (TableSelectorPanel) m_frame.getComponentByName(TableFrame.ID_TABLE_SELECTOR);
		TSConnection conn = m_frame.getConnection();
		return tspanel.createTableId(m_frame.getConnection());
	}

	/**
	 * Reloads the frame based on the currently selected table
	 */
	public void reloadTable() {
		TSConnection conn = m_frame.getConnection();
		TableId tableid = getTableId();
		try {
			conn.getModel(tableid.getCatalog()).reloadTable(tableid);
			TableMetaData tmd = conn.getModel(tableid.getCatalog()).getTableEx(tableid,
					TableMetaData.LOAD_FOREIGN_KEYS | TableMetaData.LOAD_COLUMNS_EX);
			if (tmd != null) {
				m_frame.loadTable(tableid);
			} else {
				// this is an error, unable to load the table
				m_frame.loadTable(null);
			}
		} catch (Exception e) {
			SQLErrorDialog.showErrorDialog(m_frame, e, null);
			m_frame.loadTable(null);
		}
	}

	/**
	 * Launches the instance view for the selected form
	 */
	public class TableQueryAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TableId tableid = getTableId();
			TSConnection conn = m_frame.getConnection();
			TableMetaData tmd = conn.getTable(tableid);
			if (tmd != null) {
				QueryTableAction.invoke(conn, tableid);
			}
		}
	}

	/**
	 * Launches the instance view for the selected form
	 */
	public class ShowInstanceViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				TableId tableid = getTableId();
				TSConnection conn = m_frame.getConnection();
				TableMetaData tmd = conn.getTable(tableid);
				if (tmd != null) {
					ShowInstanceFrameAction.showFrame(conn, tableid);
				}
			} catch (Exception e) {
				SQLErrorDialog.showErrorDialog(m_frame, e, null);
			}
		}
	}
}
