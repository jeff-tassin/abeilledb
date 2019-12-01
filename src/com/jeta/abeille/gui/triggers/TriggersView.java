package com.jeta.abeille.gui.triggers;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

/**
 * This class displays a list of triggers assigned to a given table
 * 
 * @author Jeff Tassin
 */
public class TriggersView extends TSPanel {
	/** the table that displays the triggers */
	private JTable m_table;
	/** the scroll pane for the table */
	private JScrollPane m_scrollpane;
	/* model for the triggers */
	private TriggersModel m_model;

	/** command ids */
	public static final String ID_CREATE_TRIGGER = "create.trigger";
	public static final String ID_EDIT_TRIGGER = "edit.trigger";
	public static final String ID_DELETE_TRIGGER = "delete.trigger";

	/**
	 * ctor
	 */
	public TriggersView(TriggersModel model) {
		setLayout(new BorderLayout());
		m_model = model;
		// add( createButtonPanel(), BorderLayout.NORTH );
		add(createTable(), BorderLayout.CENTER);
		// setController( new TriggersViewController( this ) );
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Helper method to create a button
	 */
	private JButton _createButton(String id, String iconName) {
		JButton btn = i18n_createButton(null, id, iconName);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		return btn;
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		JButton btn = _createButton(ID_CREATE_TRIGGER, "general/New16.gif");
		toolbar.add(btn);

		btn = _createButton(ID_EDIT_TRIGGER, "general/Edit16.gif");
		toolbar.add(btn);

		btn = _createButton(ID_DELETE_TRIGGER, "general/Delete16.gif");
		toolbar.add(btn);

		return toolbar;
	}

	/**
	 * Creates the JTable that displays the table triggers
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, false);
		m_table = tpanel.getTable();
		return tpanel;
	}

	/**
	 * @return the underlying data model
	 */
	public TriggersModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected trigger. Null is returned if no trigger is selected.
	 */
	public TriggerWrapper getSelectedTrigger() {
		return m_model.getRow(m_table.getSelectedRow());
	}
}
