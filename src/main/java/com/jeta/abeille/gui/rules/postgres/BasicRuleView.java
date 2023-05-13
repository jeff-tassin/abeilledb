package com.jeta.abeille.gui.rules.postgres;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSStatusBar;
import com.jeta.foundation.gui.components.TSComponentUtils;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is the panel for editing a Rule.
 * 
 * @author Jeff Tassin
 */
public class BasicRuleView extends TSPanel implements JETARule {

	/** the name for this constraint */
	private JTextField m_namefield;

	/** the label for the expression field */
	private JLabel m_namelabel;

	/** the editor pane for the expression */
	private JEditorPane m_editor;

	private TSConnection m_connection;

	/** component ids */
	public static final String ID_NAME = "rule.name.field";
	public static final String ID_EXPRESSION = "rule.expression.textarea";

	/**
	 * ctor
	 */
	public BasicRuleView(TSConnection conn, Rule rule) {
		m_connection = conn;
		initialize();
		if (rule != null) {
			m_editor.setText(rule.getExpression());
			m_namefield.setText(rule.getName());
		}
	}

	/**
	 * JETARule Implementation
	 */
	public RuleResult check(Object[] params) {
		if (getName().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		}

		if (getExpression().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Expression"));
		}
		return RuleResult.SUCCESS;
	}

	/**
	 * Creates a new Rule object based on the data entered for this view.
	 * 
	 * @return the newly created rule object
	 */
	public Rule createRule() {
		return new Rule(getName(), getExpression());
	}

	/**
	 * Creates and initializes the components for this view
	 */
	private Container createView() {
		// description
		JComponent[] labels = new JComponent[1];
		m_namelabel = new JLabel(I18N.getLocalizedMessage("Name"));
		labels[0] = m_namelabel;
		JComponent[] comps = new JComponent[1];
		m_namefield = new JTextField();
		m_namefield.setEnabled(false);
		m_namefield.setName(ID_NAME);

		comps[0] = m_namefield;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 25);

		JPanel descpanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);

		JPanel commandpanel = new JPanel(new BorderLayout(5, 10));
		commandpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Rule"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		SQLComponent sqlcomp = SQLUtils.createSQLComponent(m_connection);
		m_editor = sqlcomp.getEditor();
		JComponent comp = sqlcomp.getExtComponent();
		commandpanel.add(comp, BorderLayout.CENTER);

		String format_msg = I18N.format("format_1",
				"CREATE RULE name AS ON event TO table [ WHERE condition ] DO [ INSTEAD ] action");
		commandpanel.add(new JLabel(format_msg), BorderLayout.SOUTH);

		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.add(descpanel, BorderLayout.NORTH);
		main.add(commandpanel, BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return main;
	}

	/**
	 * @return the text entered into the expression text area
	 */
	public String getExpression() {
		return m_editor.getText().trim();
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
		return new Dimension(600, 400);
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize() {
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);

		TSStatusBar statusbar = new TSStatusBar();
		TSCell cell = new TSCell("main.cell", "Parse Rule Failed");
		cell.setMain(true);
		cell.setText(I18N.getLocalizedMessage("Parse Rule Failed"));
		cell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		statusbar.addCell(cell);
		add(statusbar, BorderLayout.SOUTH);
	}

	/**
	 * Sets the description (name) for this check
	 * 
	 * @param name
	 *            the text to set for this constraint's name
	 */
	public void setName(String name) {
		m_namefield.setText(name);
	}

}
