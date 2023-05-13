package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbTableModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

import com.jeta.foundation.utils.TSUtils;

/**
 * This dialog is used for editing/creating database tables.
 * 
 * @author Jeff Tassin
 */
public class TableEditorPanel extends TSPanel implements JETARule {
	/** the main tab pane for this view */
	private JTabbedPane m_tabpane;

	/** the table meta data that we are editing */
	private TableMetaData m_tmd;

	/** the database connection */
	private TSConnection m_connection;

	/** the model used for selecting tables */
	private ModelerModel m_tableselector;

	/** object that gets the table id that we are editing */
	private TableIdGetter m_idgetter;

	// //////////////////////////////////////////
	// columns panel
	private ColumnsPanel m_columnsview; // this is the panel that shows the
										// column definitions for the table

	// ////////////////////////////////////////////////////////
	// primary key pane
	private PrimaryKeyView m_pkview;

	// ///////////////////////////////////////////////////////
	// foreign keys pane
	private ForeignKeysView m_fkeyview;

	// //////////////////////////////////////////////////////
	// sql pane
	private SQLPanel m_sqlview;

	/**
	 * ctor
	 */
	public TableEditorPanel(TableMetaData td, TSConnection conn, ModelerModel tablemodel, TableIdGetter idgetter) {
		m_tmd = td;
		m_connection = conn;
		m_tableselector = tablemodel;
		m_idgetter = idgetter;
		if (m_tmd == null) {
			TableId tableid = new TableId(conn.getDefaultCatalog(), conn.getCurrentSchema(), "");
			m_tmd = new TableMetaData(tableid);
		}
		initialize();
		setEditable(true);
	}

	/**
	 * Creates the primary key panel. Called by initialize.
	 */
	private PrimaryKeyView createPrimaryKeyPanel() {
		String keyname = "";
		if (m_tmd != null) {
			DbKey pk = m_tmd.getPrimaryKey();
			if (pk != null) {
				keyname = pk.getKeyName();
			}
		}
		PrimaryKeyView pkview = new PrimaryKeyView(m_connection, m_columnsview.getModel(), keyname, true);
		return pkview;
	}

	public ColumnsPanel getColumnsView() {
		return m_columnsview;
	}

	/**
	 * Creates the SQL Panel. Called by initialize. This panel allows the user
	 * to view/tweak the generated SQL
	 */
	private SQLPanel createSQLPanel() {
		m_sqlview = new SQLPanel(m_connection);
		return m_sqlview;
	}

	/**
	 * @return the active view.
	 */
	public JPanel getActiveView() {
		return (JPanel) m_tabpane.getSelectedComponent();
	}

	public String getCatalog() {
		return null;
	}

	/**
	 * @return a collection of ColumnMetaData objects specified by the user
	 */
	public Collection getColumns() {
		return m_columnsview.getColumns();
	}

	/**
	 * @return the underlying database connection for this dialog
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the preferred size for this controller
	 */
	public Dimension getPreferredSize() {
		return m_columnsview.getPreferredSize();
	}

	/**
	 * @return the primary key of the table defined by this dialog. Null is
	 *         returned if no primary key is defined.
	 */
	public DbKey getPrimaryKey() {
		DbKey pk = m_columnsview.getPrimaryKey();
		if (pk != null) {
			pk.setKeyName(m_pkview.getPrimaryKeyName());
		}
		return pk;
	}

	/**
	 * @return the sql that describes the data entered in to the panel
	 */
	public String getSQL() throws SQLException {
		TableMetaData tmd = createTableMetaData(m_idgetter.getTableId());
		return DbUtils.createTableSQL(m_connection, tmd);
	}

	/**
	 * @return a collection of foreign keys (DbForeignKey objects)
	 */
	private Collection getForeignKeys() {
		return m_fkeyview.getModel().getForeignKeys();
	}

	/**
	 * @return the main tab pane for this view
	 */
	public JTabbedPane getTabbedPane() {
		return m_tabpane;
	}

	/**
	 * Creates a new TableMetaData object for the given definition.
	 * 
	 * @param tableId
	 *            the id to set
	 * @return the TableMetaData object that is described by this dialog.
	 */
	public TableMetaData createTableMetaData(TableId tableId) {
		// m_tmd should never be null here, but it might be un-initialized
		// (empty)
		//
		m_tmd = new TableMetaData(tableId);

		DbTableModel model = m_tableselector;
		DbKey key = getPrimaryKey();

		m_tmd.setPrimaryKey(getPrimaryKey());
		Collection fkeys = getForeignKeys();
		Iterator iter = fkeys.iterator();
		while (iter.hasNext()) {
			DbForeignKey fk = (DbForeignKey) iter.next();
			fk.setLocalTableId(tableId);

			TableMetaData reftable = model.getTable(fk.getReferenceTableId());
			if (reftable != null) {
				DbKey pkey = reftable.getPrimaryKey();
				if (pkey != null) {
					fk.setReferenceKeyName(pkey.getKeyName());
					m_tmd.addForeignKey(fk);
				}
			}
		}
		iter = getColumns().iterator();
		while (iter.hasNext()) {
			m_tmd.addColumn((ColumnMetaData) iter.next());
		}
		return m_tmd;
	}

	/**
	 * Creates and intializes the panels on this dialog
	 */
	protected void initialize() {
		Container container = this;
		container.setLayout(new BorderLayout());

		m_tabpane = new JTabbedPane();
		ColumnsGuiModel colmodel = new ColumnsGuiModel(m_connection, false, m_tmd, true);
		m_columnsview = new ColumnsPanel(colmodel);
		m_columnsview.setController(new ColumnsPanelController(m_columnsview));
		m_columnsview.setName(ModelerNames.ID_COLUMNS_PANEL);

		m_tabpane.addTab(I18N.getResource("Columns"), m_columnsview);

		m_pkview = createPrimaryKeyPanel();
		m_pkview.setController(new PrimaryKeyViewController(m_pkview));
		m_tabpane.addTab(I18N.getResource("Primary Key"), m_pkview);

		m_fkeyview = new ForeignKeysView(m_columnsview.getModel(), m_connection, m_tableselector, m_idgetter,
				isEditable());
		m_fkeyview.setName(ModelerNames.ID_FOREIGN_KEYS_PANEL);
		m_fkeyview.setController(new ForeignKeysController(m_fkeyview));
		m_fkeyview.getModel().setTable(m_tmd);

		m_tabpane.addTab(I18N.getLocalizedMessage("Foreign Keys"), m_fkeyview);
		m_sqlview = createSQLPanel();
		m_tabpane.addTab(I18N.getLocalizedMessage("SQL"), m_sqlview);

		container.add(m_tabpane, BorderLayout.CENTER);

		m_tabpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Component comp = m_tabpane.getSelectedComponent();
				if (comp == m_pkview) {
					refreshPrimaryKeyView();
				} else if (comp == m_sqlview) {
					refreshSQLView();
				}
			}
		});
	}

	/**
	 * Reloads the primary key information in the primary key tab view
	 */
	void refreshPrimaryKeyView() {
		m_pkview.loadData(m_columnsview.getModel());
	}

	/**
	 * Generates the SQL for the given table definition and places it in the SQL
	 * text area
	 */
	void refreshSQLView() {
		try {
			m_sqlview.setText(getSQL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the editable flag for this panel. Controls whether the user can edit
	 * the entries in the panel or not.
	 */
	public void setEditable(boolean bEditable) {
		super.setEditable(bEditable);
		m_columnsview.setEditable(bEditable);
		m_fkeyview.setEditable(bEditable);
	}

	public void stopEditing() {
		m_columnsview.stopCellEditing();
	}

	/**
	 * Checks all components for valid input.
	 * 
	 * @return an error message if a component fails validation. Otherwise, null
	 *         is returned if everything is ok
	 */
	public RuleResult check(Object[] params) {
		RuleResult result = RuleResult.SUCCESS;

		// result = m_columnsview.check( params );

		if (result.equals(RuleResult.SUCCESS))
			result = m_fkeyview.check(params);

		return result;
	}

}
