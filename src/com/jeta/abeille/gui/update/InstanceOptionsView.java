package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This is a panel for configuring the instance view for a database table.
 * 
 * @author Jeff Tassin
 */
public class InstanceOptionsView extends TSPanel {
	// ////////////////////////////////////////////////////////
	// fields pane
	private InstanceOptionsModel m_optionsmodel; // GUI model for options table
	private JTable m_columnstable;

	public static final String ID_COLUMNS_TABLE = "options.columns.table";
	public static final String ID_TABLE_SCROLL = "options.table.scroll";
	public static final String ID_TOOLBAR = "instanceoptions.toolbar";

	/** command ids */
	public static final String ID_MOVE_UP = "moveup";
	public static final String ID_MOVE_DOWN = "movedown";
	public static final String ID_EDIT_COLUMN = "edit.column";
	public static final String ID_DELETE_SETTING = "delete.setting";
	public static final String ID_RESET_DEFAULTS = "reset.to.defaults";

	public InstanceOptionsView(InstanceMetaData model) {
		initialize(model);
	}

	/**
	 * @return the model coordinates of the table row. Remember that the table
	 *         can be sorted, so the table row can have a different value than
	 *         the model row
	 */
	public int convertTableToModelIndex(int tableRow) {
		// return TableUtils.convertTableToModelIndex( m_columnstable, tableRow
		// );
		return tableRow;
	}

	/**
	 * creates the panel that contains the buttons for this view
	 */
	private JComponent createControlsComponent() {
		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		toolbar.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));
		toolbar.setName(ID_TOOLBAR);

		toolbar.setFloatable(false);
		JButton editbtn = i18n_createToolBarButton("incors/16x16/document_edit.png", ID_EDIT_COLUMN, null);
		editbtn.setToolTipText(I18N.getLocalizedMessage("Edit Column Settings"));
		editbtn.setBorderPainted(false);
		editbtn.setFocusPainted(false);

		JButton moveupbtn = i18n_createToolBarButton("incors/16x16/navigate_up.png", ID_MOVE_UP, null);
		moveupbtn.setToolTipText(I18N.getLocalizedMessage("Move Up"));
		moveupbtn.setBorderPainted(false);
		moveupbtn.setFocusPainted(false);

		JButton movedownbtn = i18n_createToolBarButton("incors/16x16/navigate_down.png", ID_MOVE_DOWN, null);
		movedownbtn.setToolTipText(I18N.getLocalizedMessage("Move Down"));
		movedownbtn.setBorderPainted(false);
		movedownbtn.setFocusPainted(false);

		JButton resetbutton = i18n_createToolBarButton("incors/16x16/refresh.png", ID_RESET_DEFAULTS, null);
		resetbutton.setToolTipText(I18N.getLocalizedMessage("Defaults"));
		resetbutton.setBorderPainted(false);
		resetbutton.setFocusPainted(false);

		toolbar.add(editbtn);
		toolbar.add(moveupbtn);
		toolbar.add(movedownbtn);
		toolbar.add(resetbutton);

		return toolbar;
	}

	/**
	 * @return the underlying GUI model
	 */
	public InstanceOptionsModel getGuiModel() {
		return m_optionsmodel;
	}

	/**
	 * @param index
	 *            the table index to get the object. This index is in table
	 *            coordinates and not model coordinates.
	 * @return the selected option info object at the given table index (not
	 *         model index)
	 */
	public ColumnSettings getItem(int index) {
		if (index < 0)
			return null;
		else
			return m_optionsmodel.getRow(convertTableToModelIndex(index));
	}

	/**
	 * @return the underlying metadata model
	 */
	public InstanceMetaData getMetaData() {
		return m_optionsmodel.getMetaData();
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		// temp
		JButton button = new JButton("test");
		Dimension d = button.getPreferredSize();
		d.width *= 10;
		d.height *= 10;
		return d;
	}

	/**
	 * @return the index of the selected row in the table. The row is in table
	 *         coordinates, not model coordinates
	 */
	public int getSelectedRow() {
		return m_columnstable.getSelectedRow();
	}

	/**
	 * @return the indices of the selected rows in the table. The rows are in
	 *         table coordinates, not model coordinates
	 */
	public int[] getSelectedRows() {
		return m_columnstable.getSelectedRows();
	}

	/**
	 * @return the currently selected item in the table. Null is returned if no
	 *         item is selected
	 */
	public ColumnSettings getSelectedItem() {
		int index = getSelectedRow();
		if (index >= 0) {
			int modelrow = convertTableToModelIndex(index);
			return m_optionsmodel.getRow(modelrow);
		} else
			return null;
	}

	/**
	 * @return the JTable component that displays the options for the given
	 *         database table
	 */
	public JTable getTable() {
		return m_columnstable;
	}

	private void initialize(InstanceMetaData metadata) {
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));

		m_optionsmodel = new InstanceOptionsModel(metadata);

		m_columnstable = new JTable(m_optionsmodel);
		m_columnstable.setName(ID_COLUMNS_TABLE);
		m_columnstable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		m_columnstable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		TableColumnModel cmodel = m_columnstable.getColumnModel();
		TableColumn tc = cmodel.getColumn(InstanceOptionsModel.PRIMARY_KEY_COLUMN);
		tc.setWidth(32);
		tc.setMaxWidth(32);
		tc.setMinWidth(32);
		tc.setPreferredWidth(32);
		tc.setHeaderRenderer(new com.jeta.abeille.gui.modeler.PrimaryKeyRenderer(true));

		JScrollPane scrollpane = new JScrollPane(m_columnstable);
		scrollpane.setName(ID_TABLE_SCROLL);
		TableUtils.setRowHeader(m_columnstable, 3);

		// set upper left corner component to a JPanel so it looks nice for all
		// look and feels.
		// for some L&F's there is a ugly white rectangle in this corner.
		scrollpane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());

		panel.add(scrollpane, BorderLayout.CENTER);

		// JPanel lopanel = new JPanel( new BorderLayout() );
		// lopanel.add( controlspanel, BorderLayout.WEST );
		// panel.add( lopanel, BorderLayout.SOUTH );
		panel.add(createControlsComponent(), BorderLayout.NORTH);

		add(panel, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// set table widths. we only need to set the table width of the name
		// column, all
		// subsequent columns will be sized automatically
		TableModel model = m_columnstable.getModel();
		FontMetrics fm = m_columnstable.getFontMetrics(m_columnstable.getFont());
		String coltitle = model.getColumnName(InstanceOptionsModel.TABLE_NAME_COLUMN);
		int colwidth = 2 * fm.getStringBounds(coltitle, m_columnstable.getGraphics()).getBounds().width / 2;
		cmodel.getColumn(InstanceOptionsModel.TABLE_NAME_COLUMN).setWidth(colwidth);
		cmodel.getColumn(InstanceOptionsModel.TABLE_NAME_COLUMN).setPreferredWidth(colwidth);

		coltitle = model.getColumnName(InstanceOptionsModel.COLUMN_NAME_COLUMN);
		colwidth = 2 * fm.getStringBounds(coltitle, m_columnstable.getGraphics()).getBounds().width;
		cmodel.getColumn(InstanceOptionsModel.COLUMN_NAME_COLUMN).setWidth(colwidth);
		cmodel.getColumn(InstanceOptionsModel.COLUMN_NAME_COLUMN).setPreferredWidth(colwidth);

		coltitle = model.getColumnName(InstanceOptionsModel.DISPLAY_NAME_COLUMN);
		colwidth = 2 * fm.getStringBounds(coltitle, m_columnstable.getGraphics()).getBounds().width;
		cmodel.getColumn(InstanceOptionsModel.DISPLAY_NAME_COLUMN).setWidth(colwidth);
		cmodel.getColumn(InstanceOptionsModel.DISPLAY_NAME_COLUMN).setPreferredWidth(colwidth);

	}

}
