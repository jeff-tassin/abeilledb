package com.jeta.foundation.gui.editor.macros;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.table.TableSorter;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the panel for creating/editing keyboard macros
 * 
 * @author Jeff Tassin
 * 
 */
public class MacroMgrPanel extends TSPanel {
	/** the table that displays the macros */
	private JTable m_macrostable;

	/** the data model */
	private MacroMgrModel m_guimodel;

	/** identifiers */
	public final static String ID_EDIT = "macromgr.editor";
	public final static String ID_REMOVE = "macromgr.remove";
	public final static String ID_NEW = "macromgr.new";

	/**
	 * ctor
	 */
	public MacroMgrPanel(MacroMgrModel model) {
		m_guimodel = model;
		initialize();
	}

	/**
	 * Converts a table row the the corresponding model index. This is needed
	 * when the table is sorted. If the table is not sorted, the table index
	 * will equal the model index
	 * 
	 * @param index
	 *            the table index to convert
	 * @return the corresponding model index
	 */
	int convertTableToModelIndex(int index) {
		if (index >= 0) {
			TableSorter sorter = (TableSorter) m_macrostable.getModel();
			return sorter.getModelRow(index);
		} else
			return index;
	}

	/**
	 * Creates the macro list panel in the middle of this window
	 * 
	 * @return the panel containing the keymap list
	 */
	private Container createMacroMgrPanel() {
		m_macrostable = TableUtils.createSortableTable(m_guimodel);

		TableColumnModel colmodel = m_macrostable.getColumnModel();
		TableColumn col = colmodel.getColumn(0);
		col.setCellRenderer(new MacroRenderer());

		m_macrostable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					// cmd_EditSelectedItem();
				}
			}
		});

		JScrollPane scrollpane = new JScrollPane(m_macrostable);
		m_macrostable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		m_macrostable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JPanel macrospanel = new JPanel(new BorderLayout(8, 0));
		macrospanel.add(scrollpane, BorderLayout.CENTER);

		JPanel btnpanel = new JPanel(new GridLayout(3, 1, 4, 4));
		JButton editbtn = i18n_createButton("Edit", ID_EDIT, "incors/16x16/document_edit.png");
		JButton removebtn = i18n_createButton("Remove", ID_REMOVE, "incors/16x16/document_delete.png");
		JButton newbtn = i18n_createButton("New", ID_NEW, "incors/16x16/document_add.png");
		btnpanel.add(newbtn);
		btnpanel.add(editbtn);
		btnpanel.add(removebtn);
		editbtn.setDefaultCapable(false);
		removebtn.setDefaultCapable(false);
		newbtn.setDefaultCapable(false);

		JPanel buttons = new JPanel(new BorderLayout());
		buttons.add(btnpanel, BorderLayout.NORTH);

		macrospanel.add(buttons, BorderLayout.EAST);

		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.add(macrospanel, BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return main;
	}

	/**
	 * @return the table model for the macro table in this panel
	 */
	public MacroMgrModel getGuiModel() {
		return m_guimodel;
	}

	/**
	 * @return the selected binding object. Null is returned if no object is
	 *         selected.
	 */
	public Macro getSelectedItem() {
		int index = getSelectedTableRow();
		if (index >= 0) {
			int modelrow = convertTableToModelIndex(index);
			return m_guimodel.getRow(modelrow);
		} else
			return null;
	}

	/**
	 * @return the selected row in the table
	 */
	int getSelectedTableRow() {
		return m_macrostable.getSelectedRow();
	}

	/**
	 * @return the table that displays the key bindings
	 */
	public JTable getTable() {
		return m_macrostable;
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize() {
		setLayout(new BorderLayout());
		add(createMacroMgrPanel(), BorderLayout.CENTER);
	}

	/**
	 * Selects a key binding in the table. The row is table coordinates not
	 * model. If the table is sorted, the table row will be different from the
	 * model row
	 * 
	 * @param the
	 *            table row to select
	 */
	public void selectItem(int row) {
		ListSelectionModel model = m_macrostable.getSelectionModel();
		model.setSelectionInterval(row, row);
	}

}
