package com.jeta.abeille.gui.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JEditorPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.ConstraintNode;

import com.jeta.abeille.gui.sql.SQLKit;
import com.jeta.abeille.gui.sql.SQLCompletion;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;

/**
 * This component allows the user to enter a constraint for a query in the query
 * builder frame.
 * 
 * @author Jeff Tassin
 */
public class ConstraintPanel extends TSPanel {
	/**
	 * The text field used to enter/show the constraint
	 */
	private JEditorPane m_editor;

	/**
	 * This model is used to get the list of tables that we can use for
	 * completion popups in the constraint
	 */
	private TableSelectorModel m_model;

	/**
	 * The database connection
	 */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public ConstraintPanel(TSConnection connection, TableSelectorModel model) {
		m_model = model;
		m_connection = connection;
		initialize();
	}

	/**
	 * @return the constraint represented by this panel
	 */
	public QueryConstraint getConstraint() {
		QueryConstraint qc = new QueryConstraint();
		String constrainttxt = m_editor.getText();

		return new QueryConstraint(constrainttxt, m_connection, m_connection.getCurrentCatalog(),
				m_connection.getCurrentSchema(m_connection.getCurrentCatalog()));
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return new Dimension(500, 100);
	}

	/**
	 * Create the components used by this panel
	 */
	void initialize() {
		m_editor = TSEditorUtils.createEditor(new ConstraintKit(m_connection, m_model));
		JComponent comp = TSEditorUtils.getExtComponent(m_editor);

		TSEditorUtils.showStatusBar(m_editor, false);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.insets = new Insets(2, 5, 2, 5);
		c.anchor = GridBagConstraints.NORTHWEST;
		add(new JLabel(I18N.getLocalizedDialogLabel("Constraint")), c);

		c.gridx = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		comp.setPreferredSize(new Dimension(100, 50));
		add(comp, c);

		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 0.0;
		c.weighty = 1.0;
		add(new JLabel(""), c);
	}

}
