package com.jeta.abeille.gui.modeler;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.modeler.mysql.MySQLColumnPanel;
import com.jeta.abeille.gui.modeler.mysql.MySQLColumnPanelController;
import com.jeta.abeille.gui.modeler.mysql.MySQLForeignKeyView;
import com.jeta.abeille.gui.modeler.mysql.MySQLTableValidationRule;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.open.rules.JETARule;

public class ModelerFactory {

	private TSConnection m_connection;

	private ModelerFactory(TSConnection conn) {
		m_connection = conn;
	}

	public ColumnPanel createColumnPanel(TSConnection conn, TableId parentTableId, ColumnInfo info, boolean bproto) {
		ColumnPanel panel = null;
		if (conn.getDatabase() == Database.MYSQL) {
			panel = new MySQLColumnPanel(conn, parentTableId, info, bproto);
			panel.setController(new MySQLColumnPanelController((MySQLColumnPanel) panel));
		} else {
			panel = new ColumnPanel(conn, info, bproto);
			panel.setController(new ColumnPanelController(panel));
		}
		return panel;
	}

	public ForeignKeyView createForeignKeyView(ForeignKeysView view, DbForeignKey fKey) {
		Database db = getDatabase();
		ModelerModel tablemodel = view.getTableSelector();
		TableId tableid = view.getTableId();

		if (fKey == null) {
			if (db.equals(Database.MYSQL))
				return new MySQLForeignKeyView(getConnection(), tableid, tablemodel, view.getColumnsGuiModel()
						.getData());
			else
				return new ForeignKeyView(getConnection(), tableid, tablemodel, view.getColumnsGuiModel().getData());
		} else {
			if (db.equals(Database.MYSQL))
				return new MySQLForeignKeyView(getConnection(), tableid, fKey, tablemodel, view.getColumnsGuiModel()
						.getData());
			else
				return new ForeignKeyView(getConnection(), tableid, fKey, tablemodel, view.getColumnsGuiModel()
						.getData());
		}
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public Database getDatabase() {
		return m_connection.getDatabase();
	}

	public static ModelerFactory getFactory(TSConnection conn) {
		// for now, just return this object
		return new ModelerFactory(conn);
	}

	/**
	 * @return a rule for validating a table metadata object
	 */
	public JETARule getTableValidationRule() {
		Database db = getDatabase();
		if (db.equals(Database.MYSQL)) {
			return new MySQLTableValidationRule();
		} else {
			return com.jeta.open.rules.EmptyRule.getInstance();
		}
	}
}
