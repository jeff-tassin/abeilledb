package com.jeta.abeille.gui.views;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.View;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.DbGuiUtils;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the panel for editing/creatings a View.
 * 
 * @author Jeff Tassin
 */
public class ViewView extends TSPanel {
	/** combo box that displays the available schemas */
	private TSComboBox m_schemascombo;

	/** the name for this constraint */
	private JTextField m_namefield;

	/** the label for the expression field */
	private JLabel m_namelabel;

	/** the editor pane for the expression */
	private JEditorPane m_editor;

	/** the database connection */
	private TSConnection m_connection;

	/** the view we are editing if not a new view */
	private View m_view;

	/** component ids */
	public static final String ID_NAME = "view.name.field";
	public static final String ID_VIEW_SOURCE = "view.source";
	public static final String ID_MODIFY_VIEW = "modify.view";

	/**
	 * ctor
	 */
	public ViewView(TSConnection connection, boolean showToolbar) {
		m_connection = connection;
		initialize(false, showToolbar, false);
		if (showToolbar) {
			// setController( new AlterViewController(this) );
			m_editor.setEditable(false);
		}
	}

	/**
	 * ctor for modifying a view
	 */
	public ViewView(TSConnection connection, View view) {
		m_connection = connection;
		initialize(false, false, true);
		setView(view);
	}

	/**
	 * ctor for creating a new view
	 */
	public ViewView(TSConnection connection, Catalog catalog, Schema selectedSchema) {
		m_connection = connection;
		initialize(true, false, true);

		if (selectedSchema != null)
			m_schemascombo.setSelectedItem(selectedSchema);
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		JButton btn = i18n_createToolBarButton("general/Edit16.gif", ID_MODIFY_VIEW,
				I18N.getLocalizedMessage("Modify View"));
		toolbar.add(btn);

		return toolbar;
	}

	/**
	 * @return the View represented by the parameters in the GUI
	 */
	public View createView() {
		TableId tableid = new TableId(getCatalog(), getSchema(), getName());
		View view = new View(tableid);
		view.setDefinition(getDefinition());
		return view;
	}

	/**
	 * Creates and initializes the components for this view
	 */
	private Container createView(boolean showschema, boolean showName) {
		JPanel main = new JPanel(new BorderLayout(8, 8));

		if (showName) {
			// description
			JComponent[] labels = null;
			JComponent[] comps = null;

			m_namelabel = new JLabel(I18N.getLocalizedMessage("Name"));
			m_namefield = new JTextField();
			m_namefield.setName(ID_NAME);

			ControlsAlignLayout layout = new ControlsAlignLayout();

			if (showschema) {
				m_schemascombo = DbGuiUtils.createSchemasCombo(m_connection, getCatalog());

				labels = new JComponent[2];
				comps = new JComponent[2];

				labels[0] = new JLabel(I18N.getLocalizedMessage("Schema"));
				labels[1] = m_namelabel;

				comps[0] = m_schemascombo;
				comps[1] = m_namefield;

				layout.setMaxTextFieldWidth(m_schemascombo, 25);
			} else {
				labels = new JComponent[1];
				comps = new JComponent[1];
				labels[0] = m_namelabel;
				comps[0] = m_namefield;
			}

			layout.setMaxTextFieldWidth(m_namefield, 25);

			JPanel descpanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);
			main.add(descpanel, BorderLayout.NORTH);
		}

		JPanel commandpanel = new JPanel(new BorderLayout());
		commandpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Definition"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		SQLComponent sqlcomp = SQLUtils.createSQLComponent(m_connection);
		m_editor = sqlcomp.getEditor();
		JComponent comp = sqlcomp.getExtComponent();
		commandpanel.add(comp, BorderLayout.CENTER);

		main.add(commandpanel, BorderLayout.CENTER);
		return main;
	}

	public Catalog getCatalog() {
		return m_connection.getDefaultCatalog();
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * This returns the view that was passed in setView. It does not create a
	 * View object based on the data in the GUI. Use createView instead. This
	 * method will return null if setView is never called.
	 */
	public View getView() {
		return m_view;
	}

	/**
	 * @return the description for this panel
	 */
	public String getName() {
		if (m_namefield == null)
			return "";
		else
			return m_namefield.getText().trim();
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(10, 15);
	}

	/**
	 * @return the source SQL for the view
	 */
	public String getDefinition() {
		return m_editor.getText().trim();
	}

	/**
	 * @return the selected schema
	 */
	public Schema getSchema() {
		if (m_connection.supportsSchemas()) {
			if (m_view == null) {
				Schema result = DbUtils.getSchema(m_connection, getCatalog(), m_schemascombo.getText());
				return result;
			} else {
				return m_view.getSchema();
			}
		} else {
			return Schema.VIRTUAL_SCHEMA;
		}
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize(boolean showschema, boolean showToolbar, boolean showName) {
		if (showToolbar)
			setLayout(new BorderLayout(5, 5));
		else
			setLayout(new BorderLayout());

		add(createView(showschema, showName), BorderLayout.CENTER);
		if (showToolbar) {
			// add( createButtonPanel(), BorderLayout.NORTH );
		}
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Sets the query for this view
	 * 
	 * @param src
	 *            the text to set for this view's source
	 */
	public void setDefinition(String src) {
		m_editor.setText(src);
	}

	/**
	 * Sets the description (name) for this check
	 * 
	 * @param name
	 *            the text to set for this constraint's name
	 */
	public void setName(String name) {
		if (m_namefield != null) {
			m_namefield.setText(name);
		}
	}

	/**
	 * Sets the view to display
	 */
	public void setView(View view) {
		setDefinition(view.getDefinition());
		setName(view.getName());
		if (m_namefield != null) {
			m_namefield.setEnabled(false);
		}
		m_view = view;
	}
}
