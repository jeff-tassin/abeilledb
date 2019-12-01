package com.jeta.abeille.gui.table.postgres;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.i18n.I18N;

import com.jeta.abeille.database.postgres.PostgresObjectStore;

/**
 * This panel shows the PostreSQL specific options for a given table
 * 
 * @author Jeff Tassin
 */
public class PostgresView extends TSPanel {
	/** the table id we are showing options for */
	private TableId m_tableid;

	/** the database connection */
	private TSConnection m_connection;

	/**
	 * check box that indicates if the oid should be displayed for the given
	 * table
	 */
	private JCheckBox m_oidbox;

	/** command ids */
	public static final String ID_APPLY_BTN = "postgresview.apply.btn";
	public static final String ID_OID_CHECKBOX = "postgresview.oid.checkbox";

	/**
	 * ctor
	 */
	public PostgresView(TSConnection connection, TableId tableid) {
		m_connection = connection;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		m_oidbox = new JCheckBox(I18N.getLocalizedMessage("Show oid"));
		m_oidbox.setName(ID_OID_CHECKBOX);
		add(m_oidbox);
		add(Box.createHorizontalStrut(10));
		JButton btn = i18n_createButton("Apply", ID_APPLY_BTN);
		add(btn);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setController(new PostgresViewController());
		setTableId(tableid);
	}

	/**
	 * Override setEditable from TSPanel so we can enable/disable apply button
	 */
	public void setEditable(boolean bedit) {
		super.setEditable(bedit);
		JButton btn = (JButton) getComponentByName(ID_APPLY_BTN);
		btn.setEnabled(!bedit);
		JCheckBox cbox = (JCheckBox) getComponentByName(ID_OID_CHECKBOX);
		cbox.setEnabled(!bedit);
	}

	/**
	 * Sets the table id for this view
	 */
	public void setTableId(TableId tableId) {
		if (tableId == null) {
			setEditable(true);
			m_oidbox.setSelected(false);
			return;
		} else {
			setEditable(false);
		}
		m_oidbox.setSelected(false);

		m_tableid = tableId;
		PostgresObjectStore postgresos = PostgresObjectStore.getInstance(m_connection);
		if (postgresos != null) {
			if (postgresos.isShowOID(m_tableid))
				m_oidbox.setSelected(true);
		}
	}

	/**
	 * Controller for this view
	 */
	public class PostgresViewController extends TSController {
		public PostgresViewController() {
			super(PostgresView.this);
			assignAction(PostgresView.ID_APPLY_BTN, new ApplyAction());
		}

		/**
		 * Handler for apply button. Sets the oid flag for the given table and
		 * tells the DbModel to reload the table metadata (the DbModel will fire
		 * an event that other application clients receive and thus will update
		 * their table views).
		 */
		public class ApplyAction implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				PostgresObjectStore postgresos = PostgresObjectStore.getInstance(m_connection);
				if (postgresos != null) {
					JCheckBox cbox = (JCheckBox) PostgresView.this.getComponentByName(PostgresView.ID_OID_CHECKBOX);
					postgresos.setShowOID(m_tableid, cbox.isSelected());
					m_connection.getModel(m_tableid.getCatalog()).reloadTable(m_tableid);
				}
			}
		}
	}

}
