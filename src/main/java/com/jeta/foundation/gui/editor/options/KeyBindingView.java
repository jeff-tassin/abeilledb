package com.jeta.foundation.gui.editor.options;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import org.netbeans.editor.MultiKeyBinding;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.table.TableSorter;
import com.jeta.foundation.gui.table.TableUtils;

/**
 * This is the panel for assigning keystroke commands to actions. It shows a
 * table pre-populated with the keys on a keyboard. The KitKeyBindingModel
 * determines which keys will be show on this window. This window allows the
 * user to double-click a mapping and edit.
 * 
 * @author Jeff Tassin
 * 
 */
public class KeyBindingView extends TSPanel {
	/** the data model */
	private KeyBindingModel m_model;

	/** the JTable that displays the keymap */
	private JTable m_keymaptable;

	/** the name for this binding */
	private JTextField m_descriptionfield;

	/**
	 * the main scroll pane. We need this when setting model data model. We
	 * simply create a new table and add it to the scroll pane while removing
	 * the old table
	 */
	private JScrollPane m_scrollpane;

	/** command ids */
	public final static String ID_DESCRIPTION = "KeyBindingView.description";
	public final static String ID_EDIT = "KeyBindingView.edit";
	public final static String ID_REMOVE = "KeyBindingView.remove";
	public final static String ID_NEW = "KeyBindingView.new";
	public final static String ID_CLEAR = "KeyBindingView.clear";
	public final static String ID_RESET_DEFAULTS = "KeyBindingView.reset.defaults";

	/**
	 * ctor
	 */
	public KeyBindingView(KeyBindingModel model) {
		m_model = model;
		initialize();

		KeyBindingUIDirector uidirector = new KeyBindingUIDirector(this);
		setUIDirector(uidirector);
		uidirector.updateComponents(null);
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
			TableSorter sorter = (TableSorter) m_keymaptable.getModel();
			return sorter.getModelRow(index);
		} else
			return index;
	}

	/**
	 * Helper method that creates a toolbar button
	 */
	private JButton createToolbarButton(String commandId, String imageName, String tooltip) {
		JButton btn = i18n_createToolBarButton(imageName, commandId, tooltip);
		btn.setPreferredSize(new Dimension(16, 16));
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setToolTipText(tooltip);
		return btn;
	}

	/**
	 * Creates the macro list panel in the middle of this window
	 * 
	 * @return the panel containing the keymap list
	 */
	private Container createKeyBindingPanel() {

		// description
		JComponent[] labels = new JComponent[1];
		labels[0] = new JLabel(I18N.getLocalizedMessage("Description"));
		JComponent[] comps = new JComponent[1];
		m_descriptionfield = TSEditorUtils.createTextField();
		m_descriptionfield.setName(ID_DESCRIPTION);

		if (!m_model.isNew()) {
			m_descriptionfield.setText(m_model.getBindingName());
			m_descriptionfield.setEnabled(false);
		}

		comps[0] = m_descriptionfield;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_descriptionfield, 25);

		JPanel descpanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);

		m_keymaptable = createTable(m_model);
		m_scrollpane = new JScrollPane(m_keymaptable);

		JPanel bindingpanel = new JPanel(new BorderLayout(8, 8));
		bindingpanel.add(m_scrollpane, BorderLayout.CENTER);

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		JButton newbtn = createToolbarButton(ID_NEW, "incors/16x16/document_add.png",
				I18N.getLocalizedMessage("New Binding"));
		JButton editbtn = createToolbarButton(ID_EDIT, "incors/16x16/document_edit.png",
				I18N.getLocalizedMessage("Edit Selected Binding"));
		JButton clearbtn = createToolbarButton(ID_CLEAR, "incors/16x16/clear.png",
				I18N.getLocalizedMessage("Clear Binding"));
		JButton removebtn = createToolbarButton(ID_REMOVE, "incors/16x16/document_delete.png",
				I18N.getLocalizedMessage("Delete Binding"));
		JButton defaultsbtn = createToolbarButton(ID_RESET_DEFAULTS, "incors/16x16/refresh.png",
				I18N.getLocalizedMessage("Reset To Defaults"));

		toolbar.add(clearbtn);
		toolbar.add(newbtn);
		toolbar.add(editbtn);
		toolbar.add(removebtn);
		toolbar.add(defaultsbtn);
		editbtn.setDefaultCapable(false);
		removebtn.setDefaultCapable(false);
		newbtn.setDefaultCapable(false);
		clearbtn.setDefaultCapable(false);
		defaultsbtn.setDefaultCapable(false);

		bindingpanel.add(toolbar, BorderLayout.NORTH);
		bindingpanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Key Bindings")),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.add(descpanel, BorderLayout.NORTH);
		main.add(bindingpanel, BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return main;
	}

	/**
	 * Creates the main table for the application
	 */
	private JTable createTable(KeyBindingModel model) {
		JTable table = TableUtils.createSortableTable(m_model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		TableColumnModel colmodel = table.getColumnModel();
		TableColumn col = colmodel.getColumn(0);
		col.setCellRenderer(new KeyBindingRenderer());
		return table;
	}

	/**
	 * @return the description for this panel
	 */
	public String getName() {
		return m_descriptionfield.getText();
	}

	/**
	 * @return the table model for the view
	 */
	public KeyBindingModel getModel() {
		return m_model;
	}

	/**
	 * @return the preferred size for this component
	 */
	public Dimension getPreferredSize() {
		return new Dimension(500, 400);
	}

	/**
	 * @return the selected row in the table
	 */
	int getSelectedIndex() {
		return m_keymaptable.getSelectedRow();
	}

	/**
	 * @return the selected row in the table
	 */
	int getSelectedTableRow() {
		return m_keymaptable.getSelectedRow();
	}

	/**
	 * @return the selected binding object. Null is returned if no object is
	 *         selected.
	 */
	public KeyBindingWrapper getSelectedItem() {
		int index = getSelectedIndex();
		if (index >= 0) {
			int modelrow = convertTableToModelIndex(index);
			return m_model.getRow(modelrow);
		} else
			return null;
	}

	/**
	 * @return the table that displays the key bindings
	 */
	public JTable getTable() {
		return m_keymaptable;
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize() {
		setLayout(new BorderLayout());
		add(createKeyBindingPanel(), BorderLayout.CENTER);
	}

	/**
	 * Saves any GUI values to the model
	 */
	public void saveToModel() {
		m_model.setBindingName(getName());
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
		ListSelectionModel model = m_keymaptable.getSelectionModel();
		model.setSelectionInterval(row, row);
	}

	/**
	 * Sets the model for this view
	 */
	void setModel(KeyBindingModel model) {
		m_model = model;

		m_keymaptable = createTable(m_model);
		JPanel panel = (JPanel) m_scrollpane.getParent();
		panel.remove(m_scrollpane);
		m_scrollpane = new JScrollPane(m_keymaptable);
		panel.add(m_scrollpane, BorderLayout.CENTER);
		panel.revalidate();
	}

	/**
	 * Renderer for keybindings
	 */
	class KeyBindingRenderer extends JLabel implements TableCellRenderer {
		public KeyBindingRenderer() {
			// must set or the background color won't show
			setOpaque(true);
			setFont(UIManager.getFont("Table.font"));
		}

		public Component getTableCellRendererComponent(JTable table, Object aValue, boolean bSelected, boolean bFocus,
				int row, int col) {
			// @todo use SwingUtilities to get the proper colors
			if (aValue instanceof KeyBindingWrapper) {
				KeyBindingWrapper wrapper = (KeyBindingWrapper) aValue;
				if (col == 0) {
					setText(wrapper.getBinding().actionName);
					setIcon(wrapper.getIcon());
				}
			} else if (aValue instanceof String)
				setText((String) aValue);

			if (bSelected) {
				setBackground(UIManager.getColor("Table.selectionBackground"));
				setForeground(UIManager.getColor("Table.selectionForeground"));
			} else {
				setBackground(UIManager.getColor("Table.background"));
				setForeground(UIManager.getColor("Table.foreground"));
			}
			return this;
		}
	}

}
