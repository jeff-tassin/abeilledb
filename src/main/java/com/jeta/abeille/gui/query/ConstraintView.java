package com.jeta.abeille.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.DefaultTableSelectorModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.components.BasicPopupMenu;
import com.jeta.foundation.gui.components.TSPanelEx;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class displays a list of contraints for a query in the query builder
 * window.
 * 
 * @author Jeff Tassin
 */
public class ConstraintView extends TSPanelEx {
	/** the table that displays the constraints */
	private JTable m_table;
	/** the scroll pane for the table */
	private JScrollPane m_scrollpane;
	/* model for the constraint */
	private ConstraintModel m_model;
	/** the database connection */
	private TSConnection m_connection;

	public static final String ID_SCROLL_PANE = "scroll.pane";

	/**
	 * ctor
	 */
	public ConstraintView(ConstraintModel model, TSConnection connection) {
		setLayout(new BorderLayout());
		m_model = model;
		m_connection = connection;
		add(createTable(), BorderLayout.CENTER);

		setPopupMenu(new BasicPopupMenu(this), m_table);
		m_scrollpane.addMouseListener(getPopupHandler());
	}

	/**
	 * Creates the JTable that displays the constraints
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		m_table = new JTable(m_model);

		m_scrollpane = new JScrollPane(m_table);
		m_scrollpane.getViewport().setBackground(Color.white);
		m_scrollpane.setName(ID_SCROLL_PANE);

		TableUtils.setColumnWidths(m_table);

		// the first column is used to display AND/OR logical connective
		// so, let's set the boolean icon in the header and constrain the
		// column size to 32 pixels
		TableColumnModel cmodel = m_table.getColumnModel();
		cmodel.getColumn(ConstraintModel.LOGIC_COLUMN).setWidth(32);
		cmodel.getColumn(ConstraintModel.LOGIC_COLUMN).setMaxWidth(32);
		cmodel.getColumn(ConstraintModel.LOGIC_COLUMN).setMinWidth(32);
		cmodel.getColumn(ConstraintModel.LOGIC_COLUMN).setPreferredWidth(32);
		cmodel.getColumn(ConstraintModel.LOGIC_COLUMN).setHeaderRenderer(new ColumnHeadingRenderer());

		ConstraintCellEditor constrainteditor = new ConstraintCellEditor(m_connection, new DefaultTableSelectorModel(
				m_connection));
		cmodel.getColumn(ConstraintModel.CONSTRAINT_COLUMN).setCellEditor(constrainteditor);

		ConstraintCellRenderer2 constraintrenderer = new ConstraintCellRenderer2(m_connection,
				new DefaultTableSelectorModel(m_connection));

		cmodel.getColumn(ConstraintModel.CONSTRAINT_COLUMN).setCellRenderer(constraintrenderer);
		return m_scrollpane;
	}

	/**
	 * @return the underlying data model
	 */
	public ConstraintModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected row in the table
	 */
	int getSelectedRow() {
		return m_table.getSelectedRow();
	}

	/**
	 * @return an array of selected rows
	 */
	int[] getSelectedRows() {
		return m_table.getSelectedRows();
	}

	/**
	 * @return the table for this view
	 */
	JTable getTable() {
		return m_table;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// table header renderer that shows an icon in the table header
	//
	static class ColumnHeadingRenderer extends JLabel implements TableCellRenderer {
		private static ImageIcon m_headerimage = TSGuiToolbox.loadImage("dtype_bool16.gif");

		public ColumnHeadingRenderer() {
			this.setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			// Try to set default fore- and background colors
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			// set normal border
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			this.setIcon(m_headerimage);
			return this;
		}
	}

}
