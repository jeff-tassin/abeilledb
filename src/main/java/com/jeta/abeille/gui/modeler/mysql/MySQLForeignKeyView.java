package com.jeta.abeille.gui.modeler.mysql;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.ModelerModel;

import com.jeta.abeille.gui.modeler.ForeignKeyView;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.open.rules.AbstractRule;

/**
 * This class displays the view for a single foreign key
 * 
 * @author Jeff Tassin
 */
public class MySQLForeignKeyView extends ForeignKeyView {

	/**
	 * ctor for creating a new foreign key
	 * 
	 * @param tableselector
	 *            this is the model that contains the tables we can reference
	 *            for our foreign key. It includes both prototype and existing
	 *            tables.
	 * @param cols
	 *            a collection of ColumnMetaData objects that define the columns
	 *            in the currently modeled table.
	 */
	public MySQLForeignKeyView(TSConnection connection, TableId localTable, ModelerModel tableselector, Collection cols) {
		super(connection, localTable, tableselector, cols);
	}

	/**
	 * ctor for editing an existing foreign key
	 * 
	 * @param tableselector
	 *            this is the model that contains the tables we can reference
	 *            for our foreign key. It includes both prototype and existing
	 *            tables.
	 * @param cols
	 *            a collection of ColumnMetaData objects that define the columns
	 *            in the currently modeled table.
	 */
	public MySQLForeignKeyView(TSConnection connection, TableId localid, DbForeignKey fKey, ModelerModel tableselector,
			Collection cols) {
		super(connection, localid, fKey, tableselector, cols);
		// javax.swing.JComponent btn = getComponentByName(
		// ForeignKeyView.ID_EDIT_COLUMN_ASSIGNMENTS );
		// btn.setEnabled( false );
	}

	/**
	 * Create the panel that contains the transactions and action panels.
	 */
	protected JPanel createPropertiesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));
		panel.add(createActionsPanel(), BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		return panel;
	}

	/**
	 * @return the preferred size for the view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 20);
	}

	/**
	 * @return a validator for this view
	 */
	public AbstractRule getValidatorRule() {
		return new MySQLForeignKeyValidatorRule();
	}

	/**
	 * Creates and initializes the components on the view
	 */
	protected void createView() {
		super.createView();
		JTextField namefield = (JTextField) getComponentByName(ForeignKeyView.ID_NAME_FIELD);
		namefield.setEnabled(false);
	}

}
