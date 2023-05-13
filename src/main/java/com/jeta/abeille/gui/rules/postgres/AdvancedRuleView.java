package com.jeta.abeille.gui.rules.postgres;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * @author Jeff Tassin
 */
public class AdvancedRuleView extends TSPanel implements JETARule {
	/** the table id */
	private TableId m_tableid;

	/** the database connection. needed to check for schema support */
	private TSConnection m_connection;

	/** the name for this constraint */
	private JTextField m_namefield;

	/** the editor pane for the expression */
	private JEditorPane m_whereeditor;

	/** the editor pane for the expression */
	private JEditorPane m_doeditor;

	private JCheckBox m_insteadbox;
	private JRadioButton m_selectradio;
	private JRadioButton m_insertradio;
	private JRadioButton m_updateradio;
	private JRadioButton m_deleteradio;

	/** component ids */
	public static final String ID_NAME = "name.field";
	public static final String ID_WHERE_EDITOR = "where.editor";

	/**
	 * ctor for creating a new view
	 */
	public AdvancedRuleView(TSConnection conn, TableId tableid) {
		m_connection = conn;
		m_tableid = tableid;
		initialize();
	}

	/**
	 * JETARule Implementation
	 */
	public RuleResult check(Object[] params) {
		if (getEvent().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("An event must be selected"));
		}

		if (getName().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		}

		return RuleResult.SUCCESS;
	}

	/**
	 * Creates the rule based on the information in the view
	 */
	public Rule createRule() {
		return new Rule(getName(), getExpression());
	}

	/**
	 * Creates and initializes the components for this view
	 */
	private Container createView() {
		// description
		JComponent[] labels = null;
		JComponent[] comps = null;

		JLabel namelabel = new JLabel(I18N.getLocalizedMessage("Name"));
		m_namefield = new JTextField();
		m_namefield.setName(ID_NAME);

		JPanel eventpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_selectradio = new JRadioButton(I18N.getLocalizedMessage("Select"));
		m_insertradio = new JRadioButton(I18N.getLocalizedMessage("Insert"));
		m_updateradio = new JRadioButton(I18N.getLocalizedMessage("Update"));
		m_deleteradio = new JRadioButton(I18N.getLocalizedMessage("Delete"));

		ButtonGroup grp = new ButtonGroup();
		grp.add(m_selectradio);
		grp.add(m_insertradio);
		grp.add(m_updateradio);
		grp.add(m_deleteradio);

		eventpanel.add(m_selectradio);
		eventpanel.add(m_insertradio);
		eventpanel.add(m_updateradio);
		eventpanel.add(m_deleteradio);

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 25);

		labels = new JComponent[2];
		comps = new JComponent[2];
		labels[0] = namelabel;
		labels[1] = new JLabel(I18N.getLocalizedMessage("Event"));

		comps[0] = m_namefield;
		comps[1] = eventpanel;

		JPanel propspanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);

		JPanel wherepanel = new JPanel(new BorderLayout());
		wherepanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Where"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		SQLComponent sqlcomp = SQLUtils.createSQLComponent(m_connection);
		sqlcomp.setLineNumberEnabled(false);
		sqlcomp.showStatusBar(false);

		m_whereeditor = sqlcomp.getEditor();
		// JComponent comp = sqlcomp.getExtComponent();
		// wherepanel.add( comp, BorderLayout.CENTER );
		JScrollPane scroll = new JScrollPane(m_whereeditor);
		wherepanel.add(scroll, BorderLayout.CENTER);

		JPanel dopanel = new JPanel(new BorderLayout(10, 10));
		dopanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Do"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		sqlcomp = SQLUtils.createSQLComponent(m_connection);
		sqlcomp.setLineNumberEnabled(false);
		sqlcomp.showStatusBar(false);

		m_doeditor = sqlcomp.getEditor();

		m_insteadbox = new JCheckBox(I18N.getLocalizedMessage("Instead"));

		dopanel.add(m_insteadbox, BorderLayout.NORTH);
		scroll = new JScrollPane(m_doeditor);
		dopanel.add(scroll, BorderLayout.CENTER);

		JPanel editorspanel = new JPanel();
		editorspanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0f;
		c.weighty = 0.3f;
		editorspanel.add(wherepanel, c);

		c.gridy = 1;
		c.weighty = 0.7f;
		editorspanel.add(dopanel, c);

		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.add(propspanel, BorderLayout.NORTH);

		main.add(editorspanel, BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return main;
	}

	/**
	 * @return the do action text
	 */
	public String getDoAction() {
		return m_doeditor.getText().trim();
	}

	/**
	 * @return the selected event
	 */
	public String getEvent() {
		if (m_selectradio.isSelected())
			return "SELECT";
		else if (m_insertradio.isSelected())
			return "INSERT";
		else if (m_updateradio.isSelected())
			return "UPDATE";
		else if (m_deleteradio.isSelected())
			return "DELETE";
		else {
			return "";
		}
	}

	/**
	 * Creates the rule expression
	 */
	public String getExpression() {
		/**
		 * CREATE [ OR REPLACE ] RULE name AS ON event TO table [ WHERE
		 * condition ] DO [ INSTEAD ] action
		 */

		StringBuffer buff = new StringBuffer();
		if (m_connection.supportsSchemas())
			buff.append("CREATE OR REPLACE RULE ");
		else
			buff.append("CREATE RULE ");

		buff.append(getName());
		buff.append(" AS ON ");
		buff.append(getEvent());
		buff.append(" TO ");
		buff.append(m_tableid.getFullyQualifiedName());

		String where = getWhereCondition();
		if (where.length() > 0) {
			buff.append(" WHERE ");
			buff.append(where);
		}

		String doaction = getDoAction();
		buff.append(" DO ");
		if (m_insteadbox.isSelected())
			buff.append("INSTEAD ");

		if (doaction.length() == 0)
			buff.append("NOTHING");
		else
			buff.append(doaction);

		return buff.toString();
	}

	/**
	 * @return the description for this panel
	 */
	public String getName() {
		return m_namefield.getText().trim();
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 16);
	}

	/**
	 * @return the where condition text
	 */
	public String getWhereCondition() {
		return m_whereeditor.getText().trim();
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize() {
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);
	}

	/**
	 * Sets the event radio
	 */
	public void setEvent(String event) {
		assert (event != null);
		event = event.trim();
		if (event.equalsIgnoreCase("SELECT"))
			m_selectradio.setSelected(true);
		if (event.equalsIgnoreCase("INSERT"))
			m_insertradio.setSelected(true);
		if (event.equalsIgnoreCase("UPDATE"))
			m_updateradio.setSelected(true);
		if (event.equalsIgnoreCase("DELETE"))
			m_deleteradio.setSelected(true);
	}

	public void setName(String name) {
		m_namefield.setText(name);
	}

	public void setWhere(String where) {
		if (where != null)
			m_whereeditor.setText(where);
	}

	public void setInstead(boolean instead) {
		m_insteadbox.setSelected(instead);
	}

	public void setDoAction(String doaction) {
		if (doaction != null)
			m_doeditor.setText(doaction);
	}

}
