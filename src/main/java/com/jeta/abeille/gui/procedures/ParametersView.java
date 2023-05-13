package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ProcedureParameter;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This component displays the procedure parameters and allows the user to
 * input/edit parameters for those databases that don't support automatic
 * procedure metadata interrogation
 * 
 * @author Jeff Tassin
 */
public class ParametersView extends TSPanel {
	/** the table that displays the parameters */
	private JTable m_table;

	/** the scroll pane for the table */
	private JScrollPane m_scrollpane;

	/* model for the parameters */
	private ParametersModel m_model;

	/** command ids for the toolbar/view */
	public static final String ID_ADD_PARAMETER = "add.parameter";
	public static final String ID_EDIT_PARAMETER = "edit.parameter";
	public static final String ID_DELETE_PARAMETER = "delete.parameter";
	public static final String ID_MOVE_UP = "move.up";
	public static final String ID_MOVE_DOWN = "move.down";
	public static final String ID_RETURN_COMPONENT = "return.component";
	public static final String ID_REFRESH = "refresh";

	/**
	 * ctor
	 */
	public ParametersView() {
		setLayout(new BorderLayout());
		String title = I18N.getLocalizedMessage("Parameters");
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		m_model = new ParametersModel();
		createComponents();
	}

	/**
	 * Creates and initializes the components on this view
	 */
	void createComponents() {
		removeAll();
		setLayout(new BorderLayout());

		ControlsAlignLayout layout = new ControlsAlignLayout();

		JPanel panel = new JPanel(layout);
		JLabel returnlabel = new JLabel(I18N.getLocalizedDialogLabel("Return"));

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(5, 5, 10, 5);
		c.anchor = GridBagConstraints.WEST;
		panel.add(returnlabel, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		JTextField returnfield = new JTextField();
		returnfield.setName(ID_RETURN_COMPONENT);
		// for postgres - fix later for MySQL and others
		returnfield.setEditable(false);
		panel.add(returnfield, c);

		layout.setMaxTextFieldWidth(returnfield, 25);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;

		panel.add(createTableComponents(), c);

		add(panel, BorderLayout.CENTER);
		revalidate();
	}

	/**
	 * Creates the JTable that displays the JDBC driver properties
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		AbstractTablePanel tpanel = TableUtils.createBasicTablePanel(m_model, false);
		m_table = tpanel.getTable();
		return tpanel;
	}

	/**
	 * Create the components at the bottom of the panel (toolbar and param
	 * table)
	 */
	JPanel createTableComponents() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(createTable(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * creates the panel that contains the buttons for this view
	 */
	private JComponent createToolbar() {
		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		toolbar.setFloatable(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		JButton editbtn = i18n_createButton(null, ID_EDIT_PARAMETER, "general/Edit16.gif");
		editbtn.setBorderPainted(false);
		editbtn.setFocusPainted(false);

		JButton moveupbtn = i18n_createButton(null, ID_MOVE_UP, "navigation/Up16.gif");
		moveupbtn.setBorderPainted(false);
		moveupbtn.setFocusPainted(false);

		JButton movedownbtn = i18n_createButton(null, ID_MOVE_DOWN, "navigation/Down16.gif");
		movedownbtn.setBorderPainted(false);
		movedownbtn.setFocusPainted(false);

		JButton refreshbtn = i18n_createButton(null, ID_REFRESH, "general/Refresh16.gif");
		refreshbtn.setBorderPainted(false);
		refreshbtn.setFocusPainted(false);

		toolbar.add(editbtn);
		toolbar.add(moveupbtn);
		toolbar.add(movedownbtn);
		toolbar.add(refreshbtn);

		return toolbar;
	}

	/**
	 * @return deletes the selected procedure parameter from the model.
	 */
	public void deleteSelectedParameter() {
		int row = getSelectedRow();
		if (row >= 0)
			m_model.deleteRow(row);
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		Font f = m_table.getFont();
		FontMetrics metrics = m_table.getFontMetrics(f);
		int height = metrics.getHeight();
		int avgwidth = TSGuiToolbox.calculateAverageTextWidth(m_table, 70);
		return new Dimension(avgwidth, height * 20);
	}

	/**
	 * @return the table model
	 */
	public ParametersModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected procedure parameter in the table. Null is returned
	 *         if nothing is selected.
	 */
	public ProcedureParameter getSelectedParameter() {
		int row = getSelectedRow();
		if (row >= 0)
			return m_model.getRow(row);
		else
			return null;
	}

	/**
	 * @return the row in the table. -1 is returned if no row is selected
	 */
	public int getSelectedRow() {
		return m_table.getSelectedRow();
	}

	/**
	 * @return the table for this view
	 */
	JTable getTable() {
		return m_table;
	}

	/**
	 * Initializes the view with data from the model
	 */
	private void loadData() {
		if (m_model != null) {
			JTextField returnfield = (JTextField) getComponentByName(ID_RETURN_COMPONENT);
			returnfield.setText(m_model.getReturnType());
		}
	}

	/**
	 * Saves the data in the view to the model
	 */
	public void saveToModel() {
		if (m_model != null) {
			JTextField returnfield = (JTextField) getComponentByName(ID_RETURN_COMPONENT);
			m_model.setReturnType(returnfield.getText());
		}
	}

	/**
	 * Sets the data model
	 */
	public void setModel(ProcedureModel model) {
		m_model.setReturnType(model.getReturnType());
		m_model.setParameters(model.getParameters());
		loadData();
	}

}
