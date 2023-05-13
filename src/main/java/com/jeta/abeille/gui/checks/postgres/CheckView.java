package com.jeta.abeille.gui.checks.postgres;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JEditorPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This is the panel for editing a CheckConstraint.
 * 
 * @author Jeff Tassin
 */
public class CheckView extends TSPanel implements JETARule {

	/** the name for this constraint */
	private JTextField m_namefield;

	/** the label for the expression field */
	private JLabel m_namelabel;

	/** the editor pane for the expression */
	private JEditorPane m_editor;

	/** the database connection */
	private TSConnection m_connection;

	/** component ids */
	public static final String ID_NAME = "constraint.name.field";
	public static final String ID_EXPRESSION = "constraint.expression.textarea";

	/**
	 * ctor
	 */
	public CheckView(TSConnection conn, CheckConstraint check) {
		m_connection = conn;
		initialize(check);
	}

	/**
	 * JETARule implementation
	 */
	public RuleResult check(Object[] params) {
		if (getName().length() == 0)
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		else if (getExpression().length() == 0)
			return new RuleResult(I18N.getLocalizedMessage("Invalid Expression"));
		else
			return RuleResult.SUCCESS;
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
		m_namefield.setName(ID_NAME);

		comps[0] = m_namefield;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 25);

		JPanel descpanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);

		JPanel commandpanel = new JPanel(new BorderLayout(5, 10));
		commandpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Expression"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		SQLComponent sqlcomp = SQLUtils.createSQLComponent(m_connection);
		m_editor = sqlcomp.getEditor();
		JComponent comp = sqlcomp.getExtComponent();
		commandpanel.add(comp, BorderLayout.CENTER);
		String format_msg = I18N.getLocalizedMessage("check_constraint_example");
		commandpanel.add(new JLabel(format_msg), BorderLayout.SOUTH);

		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.add(descpanel, BorderLayout.NORTH);
		main.add(commandpanel, BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return main;
	}

	/**
	 * @return the expression for the check
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
		Dimension d = m_namelabel.getPreferredSize();
		d.width *= 15;
		d.height *= 12;
		return d;
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize(CheckConstraint cc) {
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.CENTER);

		if (cc != null) {
			setName(cc.getName());
			setExpression(cc.getExpression());
		}

		if (cc != null)
			m_namefield.setEnabled(false);

	}

	/**
	 * Sets the expression for this check
	 * 
	 * @param expression
	 *            the text to set for this constraint's expression
	 */
	public void setExpression(String expr) {
		m_editor.setText(expr);
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
