package com.jeta.abeille.gui.triggers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.triggers.Trigger;

import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.components.TextFieldwComponentPanel;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * The view for creating/editing a trigger
 * 
 * @author Jeff Tassin
 */
public class TriggerView extends TSPanel {
	/** the name for this trigger */
	private JTextField m_namefield = new JTextField();

	/** the argumements for the trigger procedure */
	private JTextField m_argsfield = new JTextField();

	/** used for determining the preferred size for the view */
	private JLabel m_argslabel;

	/** displays the schema.name of the function assigned to this trigger */
	private JTextField m_procedurefield;

	/** the database connection */
	private TSConnection m_connection;

	/** the id of the table this trigger is assigned to */
	private TableId m_tableid;

	/** when radio buttons */
	private JRadioButton m_beforeradio;
	private JRadioButton m_afterradio;

	/** event check boxes */
	private JCheckBox m_insertcheck;
	private JCheckBox m_updatecheck;
	private JCheckBox m_deletecheck;

	/** command ids */
	public static final String ID_SHOW_PROCEDURE_BROWSER = "show.procedure.browser";

	/** component names */
	public static final String ID_FUNCTION_FIELD = "function.text.field";

	/**
	 * ctor
	 * 
	 * @param conn
	 *            the database connection
	 * @param tableId
	 *            the table this trigger is/will be assigned to
	 */
	public TriggerView(TSConnection conn, TableId tableId) {
		m_tableid = tableId;
		m_connection = conn;
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.NORTH);
		setController(new TriggerViewController(this));
	}

	/**
	 * ctor
	 * 
	 * @param conn
	 *            the database connection
	 * @param tableId
	 *            the table this trigger is/will be assigned to
	 */
	public TriggerView(TSConnection conn, TriggerWrapper wrapper) {
		this(conn, wrapper.getTrigger().getTableId());
		loadData(wrapper);
	}

	/**
	 * Creates a trigger object based on the data in this view
	 */
	public Trigger createTrigger() {
		Trigger trigger = new Trigger(null, m_tableid);
		trigger.setName(getTriggerName());
		trigger.setFunctionName(getFunctionName());
		trigger.setDelete(m_deletecheck.isSelected());
		trigger.setInsert(m_insertcheck.isSelected());
		trigger.setUpdate(m_updatecheck.isSelected());
		trigger.setBefore(m_beforeradio.isSelected());
		trigger.setFunctionArgs(m_argsfield.getText().trim());
		return trigger;
	}

	/**
	 * Creates the components for this view
	 */
	public JPanel createView() {
		Component[] left = new Component[5];
		left[0] = new JLabel(I18N.getLocalizedDialogLabel("Name"));
		left[1] = new JLabel(I18N.getLocalizedDialogLabel("When"));
		left[2] = new JLabel(I18N.getLocalizedDialogLabel("Event"));
		left[3] = new JLabel(I18N.getLocalizedDialogLabel("Function"));

		m_argslabel = new JLabel(I18N.getLocalizedDialogLabel("Arguments"));
		left[4] = m_argslabel;

		TextFieldwButtonPanel procedurepanel = new TextFieldwButtonPanel(TSGuiToolbox.loadImage("ellipsis16.gif"));
		m_procedurefield = procedurepanel.getTextField();
		m_procedurefield.setName(ID_FUNCTION_FIELD);
		JButton fbtn = procedurepanel.getButton();
		// setCommandHandler( fbtn, ID_SHOW_PROCEDURE_BROWSER );

		// when panel
		JPanel whenpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_beforeradio = new JRadioButton(I18N.getLocalizedMessage("Before"));
		m_afterradio = new JRadioButton(I18N.getLocalizedMessage("After"));

		m_beforeradio.setSelected(true);
		ButtonGroup whengroup = new ButtonGroup();
		whengroup.add(m_beforeradio);
		whengroup.add(m_afterradio);
		whenpanel.add(m_beforeradio);
		whenpanel.add(m_afterradio);

		// event panel
		JPanel eventpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_insertcheck = new JCheckBox(I18N.getLocalizedMessage("Insert"));
		m_updatecheck = new JCheckBox(I18N.getLocalizedMessage("Update"));
		m_deletecheck = new JCheckBox(I18N.getLocalizedMessage("Delete"));
		eventpanel.add(m_insertcheck);
		eventpanel.add(m_updatecheck);
		eventpanel.add(m_deletecheck);

		JLabel argsmsg = new JLabel(I18N.getLocalizedMessage("(comma-separated list)"));
		TextFieldwComponentPanel argspanel = new TextFieldwComponentPanel(m_argsfield, argsmsg);

		Component[] right = new Component[5];
		right[0] = m_namefield;
		right[1] = whenpanel;
		right[2] = eventpanel;
		right[3] = procedurepanel;
		right[4] = argspanel;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 30);
		layout.setMaxTextFieldWidth(procedurepanel, 30);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the name of the trigger function entered by the user
	 */
	public String getFunctionName() {
		return m_procedurefield.getText().trim();
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(8, 10);
	}

	/**
	 * @return the name of the trigger entered by the user
	 */
	public String getTriggerName() {
		return m_namefield.getText().trim();
	}

	/**
	 * @return true if at least one of the trigger events are checked. This is
	 *         mainly used to validate the inputs.
	 */
	boolean isEventSelected() {
		return (m_insertcheck.isSelected() || m_updatecheck.isSelected() || m_deletecheck.isSelected());
	}

	/**
	 * Loads the GUI controls with the information found in the trigger
	 */
	private void loadData(TriggerWrapper wrapper) {
		if (wrapper != null) {
			m_namefield.setText(wrapper.getName());
			m_argsfield.setText(wrapper.getParametersString());
			m_procedurefield.setText(wrapper.getProcedureName());

			Trigger trigger = wrapper.getTrigger();
			if (trigger.isBefore())
				m_beforeradio.setSelected(true);
			else
				m_afterradio.setSelected(true);

			if (trigger.isInsertEvent())
				m_insertcheck.setSelected(true);

			if (trigger.isDeleteEvent())
				m_deletecheck.setSelected(true);

			if (trigger.isUpdateEvent())
				m_updatecheck.setSelected(true);
		}
	}
}
