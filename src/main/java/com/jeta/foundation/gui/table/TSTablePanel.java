package com.jeta.foundation.gui.table;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.*;

import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.split.CustomSplitPane;
import com.jeta.foundation.gui.split.SplitLayoutManager;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This panel is used to display the results of a SQL query in a JTable
 * 
 * @author Jeff Tassin
 */
public class TSTablePanel extends AbstractTablePanel {

	private List<MouseListener> m_listeners = new LinkedList<MouseListener>();

	/** the custom splitter window */
	private CustomSplitPane m_split;

	/**
	 * this object is responsible for updating (enabling) the toolbar and menus
	 * based on the state of the table
	 */
	private UIDirector m_uidirector;

	/**
	 * if no split, this is the main table if horizontal split, this is the top
	 * table if vertical split, this is the left table
	 */
	private JTable m_table1;
	private TableRowHeader m_rowheader1;

	/**
	 * if no split, this is null if horizontal split, this is the bottom table
	 * if vertical split, this is the right table
	 */
	private JTable m_table2;
	private TableRowHeader m_rowheader2;

	/** the table with the most recent focus */
	private JTable m_focustable;

	/** the table model */
	private TableModel m_model;

	/** Flag that indicates whether the columns of the table can be sorted */
	private boolean m_sortable;

	/** different view modes */
	static final int NORMAL = 1;
	static final int SPLIT_HORIZONTAL = 2;
	static final int SPLIT_VERTICAL = 3;
	static final int TRANSPOSED = 4;

	/** the current view mode */
	private int m_viewmode;

	/**
	 * ctor
	 */
	public TSTablePanel(TableModel model) {
		this(model, true);
	}

	/**
	 * ctor
	 */
	public TSTablePanel(TableModel model, boolean sortable) {
		m_sortable = sortable;
		try {
			m_model = model;
			initialize();
			// set the table properties
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param table
	 *            the table that owns the column we wish to sort
	 * @param viewcolumn
	 *            the column to sort in table coordintates
	 * @return true if the given column in the given table can be sorted.
	 */
	public boolean canSortColumn(JTable table, int viewcolumn) {
		return true;
	}

	/**
	 * Helper method that creates a clone of a table column
	 */
	static TableColumn cloneColumn(TableColumn col) {
		TableColumn newcol = new TableColumn(col.getModelIndex());
		newcol.setCellRenderer(col.getCellRenderer());
		newcol.setPreferredWidth(col.getPreferredWidth());
		newcol.setHeaderRenderer(col.getHeaderRenderer());
		newcol.setIdentifier(col.getIdentifier());
		return newcol;
	}

	/**
	 * Helper method that creates a clone of a table column model
	 */
	static DefaultTableColumnModel cloneColumnModel(TableColumnModel colmodel) {
		DefaultTableColumnModel clone = new DefaultTableColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn col = colmodel.getColumn(index);
			clone.addColumn(TSTablePanel.cloneColumn(col));
		}
		return clone;
	}

	/**
	 * Creates a column model based on the user settings. This can also be
	 * retrieved from a previous query
	 */
	public TableColumnModel createColumnModel() {
		DefaultTableColumnModel model = new DefaultTableColumnModel();

		LinkedList list = new LinkedList();
		Collection tablecols = getTableColumns();
		Iterator tciter = tablecols.iterator();
		while (tciter.hasNext()) {
			TableColumnInfo info = (TableColumnInfo) tciter.next();
			if (info.isVisible())
				list.add(info);
		}

		// now sort the list according to the natural ordering of the
		// TableColumnInfo
		// (which is the index)
		Collections.sort(list);
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			TableColumnInfo info = (TableColumnInfo) iter.next();
			TableColumn column = new TableColumn(info.getModelIndex(), info.getWidth());
			column.setCellRenderer(info.getColumn().getCellRenderer());
			column.setPreferredWidth(info.getWidth());
			column.setIdentifier(info.getColumn().getIdentifier());
			model.addColumn(column);
		}
		return model;
	}

	public void addMouseListener(MouseListener listener) {
		m_listeners.add( listener );
		if ( m_table1 != null ) {
			m_table1.addMouseListener(listener);
		}
		if ( m_table2 != null ) {
			m_table2.addMouseListener(listener);
		}
	}

	public void removeMouseListener(MouseListener listener) {
		m_listeners.remove( listener );
		if ( m_table1 != null ) {
			m_table1.removeMouseListener(listener);
		}
		if ( m_table2 != null ) {
			m_table2.removeMouseListener(listener);
		}
	}

	/**
	 * Create the popup menu for customizing this table
	 */
	void createPopupMenu() {
		TablePopupMenu tablepopup = getPopupMenu();
		tablepopup.addSeparator();
		tablepopup.add(createMenuItem(I18N.getLocalizedMessage("Split Column"), TSTableNames.ID_SPLIT_COLUMN, null));
		tablepopup.add(createMenuItem(I18N.getLocalizedMessage("Hide Column"), TSTableNames.ID_HIDE_COLUMN, null));
		tablepopup.add(createMenuItem(I18N.getLocalizedMessage("Table Options"), TSTableNames.ID_TABLE_OPTIONS, null));
	}

	/**
	 * Helper method that instantiates a table
	 */
	private JTable createTable(TableModel model, TableColumnModel colmodel) {
		JTable result = null;
		if (isSortable()) {
			TableSorter sorter = new TableSorter(model);
			result = new JTable(sorter, colmodel);
			TableUtils.initializeTableSorter(sorter, result, this);
		} else {
			result = new JTable(model, colmodel);
		}
		return result;
	}

	/**
	 * Deselects any selected cells in the active table
	 */
	public void deselect() {
		JTable table = getFocusTable();
		if (table != null) {
			table.clearSelection();
			TableRowHeader header = getRowHeader(table);
			if (header != null)
				header.clearSelection();
		}
	}

	/**
	 * Provide our own dispose so we can get more effective garbage collection
	 */
	public void dipose() {
		removeAll();
		m_model = null;
		if (m_table1 != null) {
			TableSorter sorter = (TableSorter) m_table1.getModel();
			sorter.setModel(null);
		}
		if (m_table2 != null) {
			TableSorter sorter = (TableSorter) m_table1.getModel();
			sorter.setModel(null);
		}
	}

	/**
	 * @return the table that has the current or most recent focus
	 */
	public JTable getFocusTable() {
		return m_focustable;
	}

	/**
	 * Gets the 'other' table for a given table. If the passed in table is
	 * m_table1, then we return m_table2. Likewise, if the passed table is
	 * m_table2, then we return m_table1. Null is returned if the given table is
	 * not found.
	 * 
	 * @param table
	 *            the table that we use to determine the other table to get.
	 * @return the other table for a given table.
	 * 
	 */
	public JTable getOtherTable(JTable table) {
		if (table == m_table1)
			return m_table2;
		else if (table == m_table2)
			return m_table1;
		else {
			assert (false);
			return null;
		}

	}

	/**
	 * @return the underlying data model. This is the original model, not the
	 *         TableSorter (if there is one )
	 */
	public TableModel getModel() {
		return m_model;
	}

	/**
	 * @return the row header associated with the table
	 */
	TableRowHeader getRowHeader(JTable table) {
		if (table == m_table1)
			return m_rowheader1;
		else if (table == m_table2)
			return m_rowheader2;
		else
			return null;
	}

	/**
	 * Get the scroll pane that contains the given table
	 */
	public JScrollPane getScrollPane(JTable table) {
		if (table == null)
			return null;

		Container parent = table.getParent();
		while (parent != null) {
			if (parent instanceof JScrollPane)
				return (JScrollPane) parent;

			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * @return the a set of RowSelection objects that represents the selected
	 *         cells in the table. The selection is returned in model
	 *         coordinates.
	 */
	public TableSelection getSelection() {
		return SelectionBuilder.build(this);
	}

	/**
	 * @return the selected row header indices for all tables. The indices are
	 *         in model coordinates
	 */
	public int[] getSelectedRowHeaders() {
		int[] indices1 = new int[0];
		int[] indices2 = new int[0];

		if (m_rowheader1 != null) {
			indices1 = m_rowheader1.getSelectedIndices();
			for (int index = 0; index < indices1.length; index++)
				indices1[index] = convertTableToModelIndex(m_table1, indices1[index]);
		}

		if (m_rowheader2 != null) {
			indices2 = m_rowheader2.getSelectedIndices();
			for (int index = 0; index < indices2.length; index++)
				indices2[index] = convertTableToModelIndex(m_table2, indices2[index]);
		}

		int[] result = new int[indices1.length + indices2.length];

		System.arraycopy(indices1, 0, result, 0, indices1.length);
		System.arraycopy(indices2, 0, result, indices1.length, indices2.length);

		return result;
	}

	/**
	 * @return the default table
	 */
	public JTable getTable() {
		return m_table1;
	}

	/**
	 * @return table 1. For normal view, this is the only table. For split
	 *         vertical, this is the left table. For split horizontal, this is
	 *         the top table
	 */
	JTable getTable1() {
		return m_table1;
	}

	/**
	 * @return table 2. For normal view, this is null. For split vertical, this
	 *         is the right table. For split horizontal, this is the bottom
	 *         table
	 */
	JTable getTable2() {
		return m_table2;
	}

	/**
	 * @return the UIDirector for the panel
	 */
	public UIDirector getUIDirector() {
		return m_uidirector;
	}

	/**
	 * @return the current view mode
	 */
	int getViewMode() {
		return m_viewmode;
	}

	/**
	 * Hides the column in the given table. If the table has only one column,
	 * this method will ignore the request and not remove the column.
	 * 
	 * @param col
	 *            the column index (in table coordinates) to remove
	 * @param table
	 *            the table whose column we wish to remove
	 */
	public void hideColumn(int col, JTable table) {
		if (table != null) {
			TableColumnModel colmodel = table.getColumnModel();
			int colcount = colmodel.getColumnCount();
			int viewmode = getViewMode();
			if (viewmode == NORMAL || viewmode == SPLIT_HORIZONTAL) {
				// for these modes we don't allow the last column to be removed
				if (col >= 0 && col < colcount && colcount > 1) {
					TableColumn tc = colmodel.getColumn(col);
					colmodel.removeColumn(tc);
					table.repaint();

					if (viewmode == SPLIT_HORIZONTAL) {
						JTable other = getOtherTable(table);
						if (other != null) {
							colmodel = other.getColumnModel();
							tc = colmodel.getColumn(col);
							colmodel.removeColumn(tc);
							other.repaint();
						}
					}
				}
			} else if (viewmode == SPLIT_VERTICAL) {
				if (colcount == 1) {
					// if we are split vertical and this is the last column,
					// then
					// just show normal with the other table
					JTable other = getOtherTable(table);
					if (other != null) {
						TableColumn tc = colmodel.getColumn(col);
						colmodel.removeColumn(tc);
						showNormal(other.getColumnModel());
					}
				} else if (colcount > 1) {
					TableColumn tc = colmodel.getColumn(col);
					colmodel.removeColumn(tc);
					table.repaint();
				}
			}
		}

	}

	/**
	 * Initializes the view
	 */
	void initialize() {
		setLayout(new BorderLayout());
		createPopupMenu();
		setController(new TSTablePanelController(this));

		showNormal(null);

		m_model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (m_rowheader1 != null) {
					TableRowHeaderModel headermodel = (TableRowHeaderModel) m_rowheader1.getModel();
					headermodel.tableChanged();
				}
				if (m_rowheader2 != null) {
					TableRowHeaderModel headermodel = (TableRowHeaderModel) m_rowheader2.getModel();
					headermodel.tableChanged();
				}
				repaint();
			}
		});
	}

	/**
	 * Initializes common settings for a table in this view
	 * 
	 * @param scroll
	 *            the scroll pane that contains the table.
	 */
	private void initializeCommon(JScrollPane scroll, JTable table) {
		// table.setRowSelectionAllowed( true );
		// table.setColumnSelectionAllowed( true );
		table.setCellSelectionEnabled(true);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.addMouseListener(new TablePopupMouseListener(table, this));

		final JTable tablesrc = table;
		JTableHeader th = tablesrc.getTableHeader();
		th.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (tablesrc == m_table1) {
					setFocusTable(m_table1);
					m_rowheader1.clearSelection();
					if (m_table2 != null)
						TableUtils.clearColumnHeaders(m_table2);
				}

				if (tablesrc == m_table2 && m_rowheader2 != null) {
					setFocusTable(m_table2);
					m_rowheader2.clearSelection();
				}

				if (tablesrc == m_table2) {
					setFocusTable(m_table2);
					TableUtils.clearColumnHeaders(m_table1);
				}
			}
		});

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable table = (JTable) e.getSource();
				setFocusTable(table);
				TableRowHeader rowheader = getRowHeader(table);
				if (rowheader != null)
					rowheader.clearSelection();
			}
		});

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false),
				TSComponentNames.ID_COPY);
		TSController controller = (TSController) getController();
		table.getActionMap().put(TSComponentNames.ID_COPY, controller.new DelegateAction(TSComponentNames.ID_COPY));

		setFocusTable(m_table1);

		if (scroll != null) {
			JButton select_all_btn = new JButton();
			select_all_btn.setFocusPainted(false);
			select_all_btn.setFocusable(false);
			select_all_btn.addActionListener(new SelectAllListener(table));
			scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, select_all_btn);
		}

		m_listeners.forEach(table::addMouseListener);
	}

	/**
	 * @return true if the columns can be sorted
	 */
	public boolean isSortable() {
		return m_sortable;
	}

	/**
	 * @return true if the view is currently split horizontally
	 */
	public boolean isSplitHorizontal() {
		return getViewMode() == TSTablePanel.SPLIT_HORIZONTAL;
	}

	/**
	 * @return true if the view is currently split veritcally
	 */
	public boolean isSplitVertical() {
		return getViewMode() == TSTablePanel.SPLIT_VERTICAL;
	}

	/**
	 * This resets any sorting in case the model has changed
	 */
	public void resetSort() {
		TableSorter sorter = (TableSorter) m_table1.getModel();
		sorter.reallocateIndexes();
		if (m_table2 != null) {
			sorter = (TableSorter) m_table1.getModel();
			sorter.reallocateIndexes();
		}
	}

	/**
	 * This reads the column widths, settings, and column orders for table 1 and
	 * stores them in the table columns array
	 */
	public void saveTableSettings() {
		// System.out.println( "saving table settings " );
		// if the mode is vertical, let's first save the table settings from the
		// right table. We then save the settings from the left table. If there
		// are common columns, the the left table will take precedence
		if (getViewMode() == SPLIT_VERTICAL) {
			TableColumnModel colmodel = m_table2.getColumnModel();
			for (int index = 0; index < colmodel.getColumnCount(); index++) {
				TableColumn column = colmodel.getColumn(index);
				// TableColumnInfo info = (TableColumnInfo)m_tablecolumns.get(
				// column.getModelIndex() );
				TableColumnInfo info = getColumnInfo(column.getModelIndex());
				assert (info != null);
				info.setWidth(column.getPreferredWidth());
			}
		}

		TableColumnModel colmodel = m_table1.getColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn column = colmodel.getColumn(index);

			// TableColumnInfo info = (TableColumnInfo)m_tablecolumns.get(
			// column.getModelIndex() );
			TableColumnInfo info = getColumnInfo(column.getModelIndex());
			assert (info != null);
			info.setWidth(column.getPreferredWidth());
		}
	}

	/**
	 * Selects an entire column. If the view mode is normal or vertical, then
	 * the selection only affects the active table. However, if the viewmode is
	 * horizontal, then the selection will affect both tables
	 * 
	 * @param column
	 *            the column in the given table(s) to select
	 */
	public void selectColumn(int column) {
		if (isSplitHorizontal()) {
			selectColumn(m_table1, column);
			selectColumn(m_table2, column);
		} else {
			selectColumn(getFocusTable(), column);
		}
	}

	/**
	 * Sets the row header for the given table. Also adds a listener to the
	 * header so we can get selection events. We need to get selection events
	 * here because in the split vertical case, we need to select the row from
	 * two tables.
	 */
	void setRowHeader(JTable table) {
		final TableRowHeader header = TableUtils.setRowHeader(table);
		if (table == m_table1)
			m_rowheader1 = header;
		else if (table == m_table2)
			m_rowheader2 = header;
		else {
			assert (false);
		}

		// add a listener to the row header so that we can select the entire row
		// in the table when the user clicks the item in the row header
		header.addListSelectionListener(new RowHeaderListener());
	}

	/**
	 * Shows the query results window with no split
	 */
	public void showNormal() {
		if (m_viewmode == NORMAL)
			return;

		if (m_viewmode == SPLIT_HORIZONTAL) {
			saveTableSettings();
			showNormal(m_table1.getColumnModel());
		} else {
			showNormal(createColumnModel());
		}
	}

	/**
	 * Shows the query results window with no split
	 */
	void showNormal(TableColumnModel colmodel) {
		if (m_viewmode == NORMAL)
			return;
		else if (m_viewmode == SPLIT_HORIZONTAL)
			saveTableSettings();

		removeAll();

		m_viewmode = NORMAL;

		if ( m_table2 != null ) {
			m_listeners.forEach( (listener) -> m_table2.removeMouseListener(listener) );
		}
		if ( m_table1 != null ) {
			m_listeners.forEach( (listener) -> m_table1.removeMouseListener(listener) );
		}
		m_table2 = null;
		m_rowheader2 = null;

		if (m_table1 == null) {
			m_table1 = createTable(m_model, colmodel);
			syncTableColumnCache();
		} else {
			m_table1 = createTable(m_model, colmodel);
		}

		JScrollPane scroll = new JScrollPane(m_table1);
		initializeCommon(scroll, m_table1);

		add(scroll, BorderLayout.CENTER);
		setRowHeader(m_table1);

		revalidate();
		m_uidirector = new NormalTableEnabler(this);
	}

	/**
	 * Resets the column sizes based on the TableColumnInfo settings
	 */
	public void resizeColumns() {
		TableColumnModel colmodel = m_table1.getColumnModel();
		for (int index = 0; index < colmodel.getColumnCount(); index++) {
			TableColumn column = colmodel.getColumn(index);
			TableColumnInfo info = getColumnInfo(column.getModelIndex());
			if (info != null)
				column.setPreferredWidth(info.getWidth());
		}

		if (m_table2 != null) {
			colmodel = m_table2.getColumnModel();
			for (int index = 0; index < colmodel.getColumnCount(); index++) {
				TableColumn column = colmodel.getColumn(index);
				TableColumnInfo info = getColumnInfo(column.getModelIndex());
				if (info != null)
					column.setPreferredWidth(info.getWidth());
			}
		}

	}

	/**
	 * Sets the focus table
	 */
	void setFocusTable(JTable focusTable) {
		m_focustable = focusTable;
	}

	/**
	 * This moves the columns from one table to another in a vertical split
	 * view. If the current mode is normal, then we create a vertical split. If
	 * the selected columns span the entire table, then the result is a normal
	 * view
	 * 
	 * @param cols
	 *            the columns to split (in view coordinates )
	 * @param table
	 *            the table that contains the given columns
	 */
	void splitColumns(int[] cols, JTable table) {
		// here we create two column models
		// left and right
		if (cols == null || cols.length == 0)
			return;

		if (m_viewmode == SPLIT_HORIZONTAL)
			return;

		JTable srctable = table;
		JTable desttable = null;
		if (srctable == m_table1)
			desttable = m_table2;
		else if (srctable == m_table2)
			desttable = m_table1;
		else {
			assert (false);
			return;
		}

		// first create the right table column model
		TableColumnModel srccolmodel = srctable.getColumnModel();
		TableColumn[] movecols = new TableColumn[cols.length];
		// now remove the selected columns from the column model
		for (int index = 0; index < cols.length; index++) {
			movecols[index] = srccolmodel.getColumn(cols[index]);
		}

		for (int index = 0; index < movecols.length; index++) {
			srccolmodel.removeColumn(movecols[index]);
		}

		// okay, now the srccolmodel has the columsn removed
		// let's now add the columns to the destcolmodel
		TableColumnModel destcolmodel = null;
		if (m_viewmode == SPLIT_VERTICAL)
			destcolmodel = desttable.getColumnModel();
		else if (m_viewmode == NORMAL) {
			destcolmodel = new DefaultTableColumnModel();
		}

		HashMap destcols = new HashMap();
		for (int index = 0; index < destcolmodel.getColumnCount(); index++) {
			TableColumn col = destcolmodel.getColumn(index);
			destcols.put(col.getModelIndex(), col);
		}

		// add the move column to the left model if the left model does not
		// already contain the column
		for (int index = 0; index < movecols.length; index++) {
			TableColumn col = movecols[index];
			if (!destcols.containsKey(col.getModelIndex()))
				destcolmodel.addColumn(col);
		}

		if (srccolmodel == null || srccolmodel.getColumnCount() == 0) {
			showNormal(destcolmodel);
		} else {
			// this is a split vertical case
			if (m_viewmode == NORMAL)
				splitVertical(destcolmodel, srccolmodel);
			else if (srctable == m_table1)
				splitVertical(srccolmodel, destcolmodel);
			else
				splitVertical(destcolmodel, srccolmodel);
		}

	}

	/**
	 * Splits the view horizontally. This is opposite from how it is defined in
	 * the JSplitPane (which makes no sense). The definition should be based on
	 * the orientation of the divider (as it is here )
	 */
	public void splitHorizontal() {
		if (m_viewmode == SPLIT_HORIZONTAL)
			return;
		else if (m_viewmode == NORMAL)
			splitHorizontal(m_table1.getColumnModel());
		else if (m_viewmode == SPLIT_VERTICAL) {
			TableColumnModel colmodel = createColumnModel();
			splitHorizontal(colmodel);
		}
	}

	/**
	 * Splits the view horizontally. This is opposite from how it is defined in
	 * the JSplitPane (which makes no sense). The definition should be based on
	 * the orientation of the divider (as it is here )
	 */
	void splitHorizontal(TableColumnModel colmodel) {
		if (m_viewmode == SPLIT_HORIZONTAL)
			return;

		// else if ( m_viewmode == NORMAL )
		// saveTableSettings();

		m_viewmode = SPLIT_HORIZONTAL;

		removeAll();

		m_split = new CustomSplitPane(JSplitPane.VERTICAL_SPLIT);
		m_split.setDividerLocation(0.5f);

		// recreate table 1
		TableModel sortermodel = m_model;
		if (isSortable())
			sortermodel = new TableSorter(m_model);

		m_table1 = new JTable(sortermodel, colmodel);
		FixedTableHeader header = new FixedTableHeader(colmodel);
		m_table1.setTableHeader(header);
		if (isSortable())
			TableUtils.initializeTableSorter((TableSorter) sortermodel, m_table1, this);

		final JScrollPane topscroll = new JScrollPane(m_table1);
		initializeCommon(topscroll, m_table1);

		setRowHeader(m_table1);
		topscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		m_split.add(topscroll);
		// m_split.setThumbAlignment( CustomSplitPane.TOP_LOWER_RIGHT );

		// now create a copy of the column model for the lower table
		DefaultTableColumnModel bottomcm = TSTablePanel.cloneColumnModel(colmodel);

		colmodel = bottomcm;
		m_table2 = new JTable(sortermodel, colmodel);

		header = new FixedTableHeader(colmodel);
		m_table2.setTableHeader(header);

		if (isSortable())
			TableUtils.initializeTableSorter((TableSorter) sortermodel, m_table2, this);

		final JScrollPane bottomscroll = new JScrollPane(m_table2);
		initializeCommon(bottomscroll, m_table2);

		setRowHeader(m_table2);
		m_split.add(bottomscroll);

		JScrollBar scrollbar = bottomscroll.getHorizontalScrollBar();
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				JViewport bvp = bottomscroll.getViewport();
				JViewport tvp = topscroll.getViewport();
				Point pt = tvp.getViewPosition();
				pt.x = bvp.getViewPosition().x;
				tvp.setViewPosition(pt);
			}
		});

		m_split.setContinuousLayout(false);

		// JLayeredPane lp = new JLayeredPane( );
		// lp.setLayout( new SplitLayoutManager(m_split) );
		m_split.setSize(600, 500);
		// lp.add( m_split, new Integer(0) );
		// lp.add( m_split.getThumb(), new Integer(1) );

		// add( lp, BorderLayout.CENTER );
		add(m_split, BorderLayout.CENTER);

		// syncTableColumns();

		revalidate();
		// lp.repaint();
		m_split.repaint();

		repaint();
		m_uidirector = new HorizontalTableEnabler(this);

	}

	/**
	 * Splits the view vertically. This is opposite from the definition in
	 * JSplitPane (which makes no sense). The definition should be based on the
	 * orientation of the divider (as it is here )
	 */
	public void splitVertical() {
		if (m_viewmode == SPLIT_VERTICAL)
			return;

		if (m_viewmode == NORMAL || m_viewmode == SPLIT_HORIZONTAL) {
			saveTableSettings();
			TableColumnModel leftmodel = m_table1.getColumnModel();
			TableColumnModel rightmodel = TSTablePanel.cloneColumnModel(leftmodel);
			splitVertical(leftmodel, rightmodel);
		}

	}

	/**
	 * Splits the view vertically. This is opposite from the definition in
	 * JSplitPane (which makes no sense). The definition should be based on the
	 * orientation of the divider (as it is here )
	 */
	void splitVertical(TableColumnModel leftcolmodel, TableColumnModel rightcolmodel) {
		removeAll();

		float fdiv_location = 0.5f;
		if (m_viewmode == SPLIT_VERTICAL) {
			int divloc = m_split.getDividerLocation();
			fdiv_location = (float) divloc / (float) m_split.getWidth();
			m_split.remove(2);
			m_split.remove(1);
		} else {
			m_split = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		}

		m_split.setDividerLocation(fdiv_location);
		m_viewmode = SPLIT_VERTICAL;

		// recreate table 1
		TableModel sorter1 = m_model;
		if (isSortable())
			sorter1 = new DualTableSorter(this, m_model, null);

		m_table1 = new JTable(sorter1, leftcolmodel);
		if (isSortable())
			TableUtils.initializeTableSorter((TableSorter) sorter1, m_table1, this);

		final JScrollPane lscroll = new JScrollPane(m_table1);
		lscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		initializeCommon(lscroll, m_table1);

		setRowHeader(m_table1);
		// m_split.setLeftComponent( lscroll );
		m_split.add(lscroll);

		TableModel sorter2 = m_model;
		if (isSortable())
			sorter2 = new DualTableSorter(this, m_model, (TableSorter) sorter1);

		m_table2 = new JTable(sorter2, rightcolmodel);

		if (isSortable()) {
			TableUtils.initializeTableSorter((TableSorter) sorter2, m_table2, this);
			((DualTableSorter) sorter1).setSyncTableSorter((TableSorter) sorter2);
		}

		final JScrollPane rscroll = new JScrollPane(m_table2);
		// m_split.setRightComponent( rscroll );
		m_split.add(rscroll);
		initializeCommon(null, m_table2);

		JScrollBar scrollbar = rscroll.getVerticalScrollBar();
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				JViewport rvp = rscroll.getViewport();
				JViewport lvp = lscroll.getViewport();
				Point pt = lvp.getViewPosition();
				pt.y = rvp.getViewPosition().y;
				lvp.setViewPosition(pt);
			}
		});

		m_split.setContinuousLayout(false);

		// JLayeredPane lp = new JLayeredPane( );
		// lp.setLayout( new SplitLayoutManager(m_split) );
		// m_split.setSize( 600, 500 );
		// lp.add( m_split, new Integer(0) );
		// lp.add( m_split.getThumb(), new Integer(1) );

		// add( lp, BorderLayout.CENTER );
		add(m_split, BorderLayout.CENTER);

		// syncTableColumns();

		revalidate();
		// lp.repaint();
		m_split.repaint();

		m_uidirector = new VerticalTableEnabler(this);

		repaint();
	}

	/**
	 * Sets all the column widths of table 2 equal to those of table 1
	 */
	private void syncTableColumns() {
		// now set the column widths of table 2 to match those of table 1
		TableColumnModel colmodel1 = m_table1.getColumnModel();
		TableColumnModel colmodel2 = m_table2.getColumnModel();
		for (int col = 0; col < colmodel1.getColumnCount(); col++) {
			TableColumn tcol1 = colmodel1.getColumn(col);
			TableColumn tcol2 = colmodel2.getColumn(col);
			tcol2.setPreferredWidth(tcol1.getPreferredWidth());
		}
	}

	/**
	 * Override so we can update our table headers
	 */
	public void updateUI() {
		super.updateUI();

		if (m_table1 != null) {
			m_table1.updateUI();
			m_table1.getTableHeader().updateUI();

			Container p = m_table1.getParent();
			if (p instanceof JViewport) {
				Container gp = p.getParent();
				if (gp instanceof JScrollPane) {
					JScrollPane scrollPane = (JScrollPane) gp;
					scrollPane.updateUI();
				}
			}

		}

		if (m_rowheader1 != null)
			m_rowheader1.updateUI();

		if (m_table2 != null) {
			m_table2.updateUI();
			if (m_table2.getTableHeader() != null) {
				m_table2.getTableHeader().updateUI();
			}
		}

		if (m_rowheader2 != null)
			m_rowheader2.updateUI();
	}

	/**
	 * We provide this header so that when we have a horizonal split, we resize
	 * the same column in the other table.
	 */
	class FixedTableHeader extends JTableHeader {
		private boolean m_colmoved = false;
		private TableColumn m_draggedcol = null;

		FixedTableHeader(TableColumnModel cm) {
			super(cm);
		}

		public void columnMoved(TableColumnModelEvent e) {
			super.columnMoved(e);
			if (e.getFromIndex() != e.getToIndex()) {
				m_colmoved = true;
				m_draggedcol = getDraggedColumn();
			}
		}

		/**
		 * Listen for mouse up events so we can detect if a column has been
		 * moved. If so, then we need to update the corresponding column if the
		 * current view mode is horizontal split
		 * 
		 */
		public void processMouseEvent(MouseEvent evt) {
			super.processMouseEvent(evt);
			if (evt.getID() == MouseEvent.MOUSE_RELEASED && m_colmoved && m_draggedcol != null) {
				JTable srctable = getTable();
				int srcmodelindex = m_draggedcol.getModelIndex();
				int srcviewindex = table.convertColumnIndexToView(srcmodelindex);

				JTable desttable = m_table1;
				if (srctable == m_table1)
					desttable = m_table2;

				int destviewindex = desttable.convertColumnIndexToView(srcmodelindex);
				desttable.moveColumn(destviewindex, srcviewindex);
				m_colmoved = false;
			}
		}

		/**
		 * Listen for column size events so we can update the corresponding
		 * column
		 */
		public void processMouseMotionEvent(MouseEvent evt) {
			super.processMouseMotionEvent(evt);
			TableColumn col = getResizingColumn();
			JTable dragtable = this.getTable();
			JTable synctable = m_table1;
			if (dragtable == m_table1)
				synctable = m_table2;

			if (col != null) {
				int prefwidth = col.getPreferredWidth();
				int viewindex = dragtable.convertColumnIndexToView(col.getModelIndex());
				TableColumn col2 = synctable.getColumnModel().getColumn(viewindex);
				col2.setPreferredWidth(prefwidth);
			}
		}
	}

	/**
	 * Listener that selects all items in a table
	 */
	public class SelectAllListener implements ActionListener {
		private JTable m_table;

		SelectAllListener(JTable table) {
			m_table = table;
		}

		public void actionPerformed(ActionEvent evt) {
			m_table.selectAll();
			if (getViewMode() == SPLIT_VERTICAL) {
				JTable other = getOtherTable(m_table);
				if (other != null)
					other.selectAll();

			}
		}
	}

	/**
	 * A listener to the row header so that we can select the entire row in the
	 * table when the user clicks the item in the row header
	 */
	public class RowHeaderListener implements ListSelectionListener {
		public RowHeaderListener() {
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				TableRowHeader header = (TableRowHeader) e.getSource();
				JTable tablesrc = header.getTable();
				int[] indices = header.getSelectedIndices();

				if (tablesrc == m_table1) {
					m_table1.clearSelection();
					if (indices.length > 0)
						m_table1.setColumnSelectionInterval(0, m_table1.getColumnCount() - 1);

					if (isSplitVertical()) {
						m_table2.clearSelection();
						if (indices.length > 0)
							m_table2.setColumnSelectionInterval(0, m_table2.getColumnCount() - 1);

					}

					for (int index = 0; index < indices.length; index++) {
						m_table1.addRowSelectionInterval(indices[index], indices[index]);
						if (isSplitVertical())
							m_table2.addRowSelectionInterval(indices[index], indices[index]);
					}

				} else if (tablesrc == m_table2) {
					// if we are here, then by definition the table is split
					// horizontally
					m_table2.clearSelection();
					if (indices.length > 0)
						m_table2.setColumnSelectionInterval(0, m_table2.getColumnCount() - 1);

					for (int index = 0; index < indices.length; index++) {
						m_table2.addRowSelectionInterval(indices[index], indices[index]);
					}
				}
				setFocusTable(tablesrc);
			}
		}
	}

}
