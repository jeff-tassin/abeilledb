package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is a panel for adding foreign keys to a table. The main control is a
 * JTable that lists each foreign key in the table [key name] [refrence table]
 * [column mappings] Each foreign key has a child list of columns in the local
 * table that are assigned to the key. We assume you can only assign foreign
 * keys to primary keys of referenced tables. No support for alternate keys yet.
 * 
 * @author Jeff Tassin
 */
public class ForeignKeysView extends TSPanel {

	/** the table that displays the foreign keys */
	private JTable m_table;

	/** data model for the foreign keys */
	private ForeignKeysModel m_model;

	/**
	 * this is the model of columns that are in the table the user is currently
	 * creating
	 */
	private ColumnsGuiModel m_fieldguimodel;

	/** the connection we are currently working against */
	private TSConnection m_connection;

	/** this is the model used for selecting tables in drop down lists */
	private ModelerModel m_tableselector;

	/**
	 * the object that gets the table id that identifies the table we are
	 * working on
	 */
	private TableIdGetter m_idgetter;

	/** command ids */
	public static String ID_ADD_FOREIGN_KEY = "add.foreign.key";
	public static String ID_EDIT_FOREIGN_KEY = "edit.foreign.key";
	public static String ID_DROP_FOREIGN_KEY = "drop.foreign.key";

	public static String ID_FOREIGN_KEYS_TABLE = "foreign.keys.table";

	/**
	 * ctor
	 */
	public ForeignKeysView(ColumnsGuiModel fmodel, TSConnection conn, ModelerModel tableModel, TableIdGetter idgetter,
			boolean bprototype) {
		m_fieldguimodel = fmodel;
		m_connection = conn;
		m_tableselector = tableModel;
		m_idgetter = idgetter;
		setEditable(bprototype);
		initialize(bprototype);
	}

	/**
	 * @return the connection we are working against
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @param colName
	 *            the name of the column whose metadata object we wish to return
	 * @return the corresponding meta data object for the given column name
	 */
	ColumnMetaData getColumnMetaData(String colName) {
		Collection fields = m_fieldguimodel.getData();
		Iterator iter = fields.iterator();
		while (iter.hasNext()) {
			ColumnInfo info = (ColumnInfo) iter.next();
			if (info.getColumnName().equals(colName))
				return info;
		}
		return null;
	}

	/**
	 * @return the 'columns' model associated with this foreign keys panel
	 */
	public ColumnsGuiModel getColumnsGuiModel() {
		return m_fieldguimodel;
	}

	/**
	 * @return the data model for this foreign key gui
	 */
	public ForeignKeysModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected foreign key
	 */
	public ForeignKeyWrapper getSelectedForeignKey() {
		return (ForeignKeyWrapper) m_model.getRow(m_table.getSelectedRow());
	}

	/**
	 * @return the id of the table whose foreign keys we are viewing/editing
	 */
	public TableId getTableId() {
		return m_idgetter.getTableId();
	}

	/**
	 * @return the table selector model
	 */
	public ModelerModel getTableSelector() {
		return m_tableselector;
	}

	/**
	 * @todo we need to clone the table metadata here or before we get here
	 */
	private void initialize(boolean prototype) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		m_model = new ForeignKeysModel(m_connection, isEditable());
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, true);
		m_table = tpanel.getTable();
		m_table.setName(ID_FOREIGN_KEYS_TABLE);
		add(tpanel, BorderLayout.CENTER);

		if (prototype) {
			JToolBar toolbar = new JToolBar();
			toolbar.setFloatable(false);

			JButton addbtn = i18n_createToolBarButton("incors/16x16/document_add.png", ID_ADD_FOREIGN_KEY,
					"Create Foreign Key");
			JButton modifybtn = i18n_createToolBarButton("incors/16x16/document_edit.png", ID_EDIT_FOREIGN_KEY,
					"Modify Foreign Key");
			JButton deletebtn = i18n_createToolBarButton("incors/16x16/document_delete.png", ID_DROP_FOREIGN_KEY,
					"Drop Foreign Key");

			toolbar.add(addbtn);
			toolbar.add(modifybtn);
			toolbar.add(deletebtn);
			add(toolbar, BorderLayout.NORTH);
		}
	}

	/**
	 * Validates input on this panel.
	 * 
	 * @return an error message if the panel has invalid input. Null is returned
	 *         if everything is ok
	 */
	public RuleResult check(Object[] params) {
		// checks
		// 1. make sure each foreign key has 1 or more columns
		// 2. don't allow repeated foriegn keys w/ same columns and ref table
		// assignments
		// 3. make sure referenced tables and columsn exist

		TreeSet keyset = new TreeSet();

		for (int row = 0; row < m_model.getRowCount(); row++) {
			ForeignKeyWrapper wrapper = m_model.getRow(row);
			DbForeignKey fkey = wrapper.getForeignKey();
			assert (fkey != null);

			// case 1
			if (keyset.contains(fkey))
				return new RuleResult(I18N.getLocalizedMessage("Duplicate foreign keys not allowed"));

			keyset.add(fkey);

			DbKey localkey = fkey.getLocalKey();
			Collection cols = localkey.getColumns();

			// case 2
			if (cols.size() == 0) {
				return new RuleResult(I18N.getLocalizedMessage("A foreign key must have at least one column"));
			}

			Iterator citer = cols.iterator();
			while (citer.hasNext()) {
				String colname = (String) citer.next();
				if (!m_fieldguimodel.contains(colname)) {
					// error
					return new RuleResult(I18N.format("foreign_key_column_not_found_1", colname));
				}
			}
		}

		return RuleResult.SUCCESS;
	}

}
