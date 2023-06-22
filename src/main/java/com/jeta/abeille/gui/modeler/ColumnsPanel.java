package com.jeta.abeille.gui.modeler;

import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.store.ColumnInfo;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This is a panel for editing columns in a RDBMS table.
 * 
 * @author Jeff Tassin
 */
public class ColumnsPanel extends TSPanel {
	/** the data model */
	private ColumnsGuiModel m_model;

	/** the table panel that contains the main table */
	private AbstractTablePanel m_columns_table_panel;

	/** the main table */
	private JTable m_columnstable;

	/** the toolbar on the panel */
	private JToolBar m_toolbar;

	private static ImageIcon m_pkimage = TSGuiToolbox.loadImage("primarykey16.gif");
	private static ImageIcon m_pkheaderimage = TSGuiToolbox.loadImage("primarykeyheader16.gif");
	private static ImageIcon m_fkicon = TSGuiToolbox.loadImage("foreignkey16.gif");

	/** component ids */
	public static final String ID_COLUMNS_TABLE = "columnstable";
	public static final String ID_TABLE_NAME = TableSelectorPanel.ID_TABLES_COMBO;
	public static final String ID_CONTROLS_PANEL = "controls.panel";
	public static final String ID_MOVE_UP = "move.up";
	public static final String ID_MOVE_DOWN = "move.down";
	public final static String ID_ADD_COLUMN = "addcolumn";
	public final static String ID_EDIT_COLUMN = "editcolumn";
	public final static String ID_REMOVE_COLUMN = "removecolumn";
	public final static String ID_IMPORT_TABLE = "import";


	public static String ID_GENERATE_INSERT_SQL = "generate.insert.sql";


	public static final String ID_ALTER_TABLE_COLUMNS = "checked.feature.alter.table.columns";

	/**
	 * ctor
	 */
	public ColumnsPanel(ColumnsGuiModel model) {
		this(model, true);
	}

	/**
	 * ctor
	 */
	public ColumnsPanel(ColumnsGuiModel model, boolean bprototype) {
		m_model = model;
		initialize(bprototype);

		// java.util.TreeSet keyset = new java.util.TreeSet();
		// keyset.add( javax.swing.KeyStroke.getKeyStroke(
		// java.awt.event.KeyEvent.VK_TAB, java.awt.event.InputEvent.CTRL_MASK,
		// false ) );
		// m_columnstable.setFocusTraversalKeys(
		// java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keyset );

		if (bprototype) {
			setFocusCycleRoot(true);
			setFocusTraversalPolicy(new ColumnsPanelFocusPolicy(getFocusTraversalPolicy()));
		}
	}

	protected JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(i18n_createToolBarButton("incors/16x16/row_add.png", ID_GENERATE_INSERT_SQL,  "Generate Insert SQL"));
		return toolbar;
	}


	public String getCatalog() {
		return null;
	}

	/**
	 * @return a collection of ColumnMetaData objects specified by the user
	 */
	public Collection getColumns() {
		return m_model.getData();
	}

	/**
	 * @return the field table in this panel
	 */
	public JTable getTable() {
		return m_columnstable;
	}

	/**
	 * @return the model for this panel
	 */
	public ColumnsGuiModel getModel() {
		return m_model;
	}

	/**
	 * @return the preferred size of this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(10, 15);
	}

	/**
	 * @return the currently selected item in the table. Null is returned if no
	 *         item is selected
	 */
	ColumnInfo getSelectedItem() {
		int index = m_columnstable.getSelectedRow();
		if (index >= 0)
			return (ColumnInfo) m_model.getRow(index);
		else
			return null;
	}

	public ColumnInfo[] getSelectedItems() {
		int[] rows = m_columnstable.getSelectedRows();
		ArrayList<ColumnInfo> results = new ArrayList<ColumnInfo>();
		for (int row : rows) {
			results.add((ColumnInfo) m_model.getRow(TableUtils.convertTableToModelIndex(m_columnstable, row)));
		}
		return results.toArray(new ColumnInfo[0]);
	}

	/**
	 * @return the primary key defined in this panel. Null is returned if no
	 *         primary key is defined.
	 */
	public DbKey getPrimaryKey() {
		DbKey pk = new DbKey();
		Collection cols = m_model.getData();
		if (cols != null) {
			Iterator iter = cols.iterator();
			while (iter.hasNext()) {
				ColumnInfo fi = (ColumnInfo) iter.next();
				// fi.print();
				if (fi.isPrimaryKey()) {
					pk.addField(fi.getColumnName());
				}
			}
		}

		if (pk.getColumnCount() == 0)
			return null;
		else
			return pk;
	}

	/**
	 * Creates and initializes the controls for this panel
	 */
	private void initialize(boolean bprototype) {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		setLayout(new BorderLayout());

		m_columns_table_panel = TableUtils.createSimpleTable(m_model, true);
		m_columnstable = m_columns_table_panel.getTable();

		/**
		 * Override the Copy Special action for the Columns table. We do this so
		 * we can show the Copy Special dialog with the option to copy the
		 * tablename prepended to the column name *if* a columnmetadata object
		 * is part of the selection.
		 */
		m_columns_table_panel.getController().assignAction(TSComponentNames.ID_COPY_SPECIAL,
				new ColumnsCopySpecialAction());
		m_columnstable.setName(ID_COLUMNS_TABLE);

		TableColumnModel cmodel = m_columnstable.getColumnModel();
		cmodel.getColumn(ColumnsGuiModel.PRIMARYKEY_COLUMN).setWidth(32);
		cmodel.getColumn(ColumnsGuiModel.PRIMARYKEY_COLUMN).setMaxWidth(32);
		cmodel.getColumn(ColumnsGuiModel.PRIMARYKEY_COLUMN).setMinWidth(32);
		cmodel.getColumn(ColumnsGuiModel.PRIMARYKEY_COLUMN).setPreferredWidth(32);
		cmodel.getColumn(ColumnsGuiModel.PRIMARYKEY_COLUMN).setHeaderRenderer(new PrimaryKeyRenderer(true));
		cmodel.getColumn(ColumnsGuiModel.PRIMARYKEY_COLUMN).setCellRenderer(new PrimaryKeyRenderer(false));
		cmodel.getColumn(ColumnsGuiModel.DATATYPE_COLUMN).setCellRenderer(new DataTypeRenderer());

		DataTypeCellEditor celleditor = new DataTypeCellEditor(m_model.getConnection());
		cmodel.getColumn(ColumnsGuiModel.DATATYPE_COLUMN).setCellEditor(celleditor);

		add(createToolBar(), BorderLayout.NORTH );
		add(m_columns_table_panel, BorderLayout.CENTER);

		createToolBar(bprototype);
	}

	/**
	 * Creates the toolbar for this view
	 */
	private void createToolBar(boolean bprototype) {
		TSConnection tsconn = m_model.getConnection();
		TSDatabase db = (TSDatabase) tsconn.getImplementation(TSDatabase.COMPONENT_ID);
		if (bprototype || db.supportsFeature(ID_ALTER_TABLE_COLUMNS)) {
			m_toolbar = new JToolBar();
			m_toolbar.setFloatable(false);
			m_toolbar.setName(ID_CONTROLS_PANEL);

			if (bprototype) {
				JButton addbtn = i18n_createToolBarButton("incors/16x16/document_add.png", ID_ADD_COLUMN, "Add Column");
				JButton editbtn = i18n_createToolBarButton("incors/16x16/document_edit.png", ID_EDIT_COLUMN,
						"Modify Column");
				JButton deletebtn = i18n_createToolBarButton("incors/16x16/document_delete.png", ID_REMOVE_COLUMN,
						"Drop Column");
				JButton moveupbtn = i18n_createToolBarButton("incors/16x16/navigate_up.png", ID_MOVE_UP, null);
				JButton movedownbtn = i18n_createToolBarButton("incors/16x16/navigate_down.png", ID_MOVE_DOWN, null);

				m_toolbar.add(addbtn);
				m_toolbar.add(editbtn);
				m_toolbar.add(deletebtn);
				m_toolbar.add(moveupbtn);
				m_toolbar.add(movedownbtn);
				add(m_toolbar, BorderLayout.NORTH);
			}
		}
	}

	/**
	 * @return the selected column in the columns table (in model coordinates)
	 */
	public int getSelectedColumn() {
		return m_columnstable.getSelectedColumn();
	}

	/**
	 * Updates the user interface when the model changes
	 */
	public void notifyModelChanged() {
		m_model.fireTableDataChanged();
		ListSelectionModel selectionModel = m_columnstable.getSelectionModel();
		selectionModel.setSelectionInterval(m_model.getRowCount() - 1, m_model.getRowCount() - 1);
		m_columnstable.repaint();
	}

	/**
	 * Sets the flag that indicates if we can edit this panel or not
	 */
	public void setEditable(boolean bEditable) {
		super.setEditable(bEditable);
		if (!bEditable) {
			repaint();
			JComponent comp = (JComponent) getComponentByName(ID_CONTROLS_PANEL);
			if (comp != null) {
				Container parent = comp.getParent();
				if (parent != null) {
					parent.remove(comp);
					parent.invalidate();
					parent.validate();
				}
			}
			revalidate();
		}
	}

	/**
	 * Hides/shows the toolbar at the top of the panel (The toolbar is currently
	 * displayed by default)
	 */
	public void setToolbarVisible(boolean vis) {
		// show is not yet implemented
		assert (vis == false);
		if (!vis) {
			remove(m_toolbar);
			revalidate();
			repaint();
		}
	}

	/**
	 * Stops any editing at the current editing cell
	 */
	public void stopCellEditing() {
		JTable table = getTable();
		if (table.isEditing()) {
			int row = table.getEditingRow();
			int col = table.getEditingColumn();
			if (row >= 0 || col >= 0) {
				TableCellEditor celleditor = table.getCellEditor(row, col);
				celleditor.stopCellEditing();
			}
		}
	}

	public class ColumnsPanelFocusPolicy extends FocusTraversalPolicy {
		private FocusTraversalPolicy m_delegate;

		public ColumnsPanelFocusPolicy(FocusTraversalPolicy delegate) {
			m_delegate = delegate;
		}

		public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
			/**
			 * this is required otherwise the focus will go to the toolbar or
			 * some other component other than the columns table after setting
			 * the data type
			 */
			if (aComponent instanceof TSComboBox.ComboButton || aComponent instanceof TSComboBox.ComboTextField) {
				return m_columnstable;
			}

			if (aComponent instanceof JTable) {
				Window win = javax.swing.SwingUtilities.getWindowAncestor(aComponent);
				if (win instanceof TSDialog) {
					TSDialog dlg = (TSDialog) win;
					return dlg.getOkButton();
				} else {
					return m_delegate.getComponentAfter(focusCycleRoot, aComponent);
				}
			} else {
				return m_delegate.getComponentAfter(focusCycleRoot, aComponent);
			}
		}

		public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
			return m_delegate.getComponentBefore(focusCycleRoot, aComponent);
		}

		public Component getDefaultComponent(Container focusCycleRoot) {
			return m_delegate.getDefaultComponent(focusCycleRoot);
		}

		public Component getFirstComponent(Container focusCycleRoot) {
			return m_delegate.getFirstComponent(focusCycleRoot);
		}

		public Component getInitialComponent(Window window) {
			return m_delegate.getInitialComponent(window);
		}

		public Component getLastComponent(Container focusCycleRoot) {
			return m_delegate.getLastComponent(focusCycleRoot);
		}
	}

	/**
	 * Override the Copy Special action for the Columns table. We do this so we
	 * can show the Copy Special dialog with the option to copy the tablename
	 * prepended to the column name *if* a columnmetadata object is part of the
	 * selection.
	 */
	public class ColumnsCopySpecialAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

			/**
			 * provide a specialized version of TableModel so we can prepend the
			 * table name if needed.
			 */
			class MyTableModel extends javax.swing.table.AbstractTableModel {
				/** flag that indicates if we should include the table name */
				private boolean m_inc_table;

				MyTableModel(boolean inc_table) {
					m_inc_table = inc_table;
				}

				public Object getValueAt(int rowIndex, int columnIndex) {
					if (m_inc_table && (columnIndex == ColumnsGuiModel.NAME_COLUMN)) {
						ColumnInfo ci = (ColumnInfo) m_model.getRow(rowIndex);
						if (ci != null) {
							return ci.getTableQualifiedName();
						}
					}
					return m_model.getValueAt(rowIndex, columnIndex);
				}

				public int getColumnCount() {
					return m_model.getColumnCount();
				}

				public int getRowCount() {
					return m_model.getRowCount();
				}

				public String getColumnName(int column) {
					return m_model.getColumnName(column);
				}

				public Class getColumnClass(int column) {
					return m_model.getColumnClass(column);
				}
			}
			;

			// the copy options dialog
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, ColumnsPanel.this, true);
			com.jeta.abeille.gui.model.common.CopyColumnsPanel panel = com.jeta.abeille.gui.model.common.CopyColumnsPanel
					.createPanel();
			dlg.setTitle(I18N.getLocalizedMessage("Copy Special"));
			dlg.setPrimaryPanel(panel);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				TableSelection selection = m_columns_table_panel.getSelection();
				selection.setDelegate(new MyTableModel(panel.isIncludeTable()));
				com.jeta.foundation.gui.table.export.ExportModel export_model = panel.getModel(selection);
				TableUtils.copyToClipboard(export_model, selection);
			}
		}
	}
}
