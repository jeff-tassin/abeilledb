package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a view for a stored procedure. It allows the user to
 * see the procedure source, name, owner, parameters and return value.
 * 
 * @author Jeff Tassin
 */
public class SourceView extends TSPanel {
	/** the procedure model */
	private ProcedureModel m_model;

	/** the parameters model */
	private ParametersModel m_parametersmodel;

	/** displays the procedure language */
	private JTextField m_languagefield;

	/** compile the procedure (i.e. drop/recreate) */
	private JButton m_compilebtn;

	/** the editor for this view - displays the source code for the procedure */
	private JEditorPane m_editor;

	/** the 'container' for the SQL editor. */
	private SQLComponent m_sqlcomponent;

	/** the component ids for the view */
	public static final String ID_LANGUAGE_FIELD = "lang.field";
	public static final String ID_SOURCE_COMPONENT = "proc.source";
	public static final String ID_COMPILE_BTN = "compile.btn";

	/**
	 * ctor
	 */
	public SourceView() {
		createComponents();
		setController(new SourceViewController(this));
	}

	/**
	 * ctor
	 */
	public SourceView(ProcedureModel model) {
		this();
		setModel(model);
	}

	/**
	 * Creates and initializes the components on this view
	 */
	void createComponents() {
		removeAll();
		setLayout(new BorderLayout());

		ControlsAlignLayout layout = new ControlsAlignLayout();

		JPanel panel = new JPanel(layout);
		JLabel llabel = new JLabel(I18N.getLocalizedDialogLabel("Language"));

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(5, 5, 10, 5);
		c.anchor = GridBagConstraints.WEST;
		panel.add(llabel, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		m_languagefield = new JTextField();
		m_languagefield.setName(ID_LANGUAGE_FIELD);
		m_languagefield.setEditable(false);
		panel.add(m_languagefield, c);

		c.gridx = 2;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;

		m_compilebtn = i18n_createButton("Compile", ID_COMPILE_BTN, null);
		m_compilebtn.setIcon(TSGuiToolbox.loadImage("incors/16x16/gear.png"));
		panel.add(m_compilebtn, c);

		layout.setMaxTextFieldWidth(m_languagefield, 25);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;

		panel.add(createEditorComponents(), c);
		add(panel, BorderLayout.CENTER);
		revalidate();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Creates the editor components (tab pane containing source editor and
	 * inputs editor )
	 */
	TSPanel createEditorComponents() {
		TSPanel panel = new TSPanel(new BorderLayout());
		panel.add(createSourceEditor(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Create the source editor component of the view
	 */
	TSPanel createSourceEditor() {
		m_sqlcomponent = SQLUtils.createSQLComponent(null);

		m_editor = m_sqlcomponent.getEditor();
		JComponent comp = m_sqlcomponent.getExtComponent();

		m_editor.setName(ID_SOURCE_COMPONENT);
		// editor.setEditable( false );

		TSPanel panel = new TSPanel();
		panel.setLayout(new BorderLayout());
		panel.add(comp, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * @return the underlying data model
	 */
	public ProcedureModel getModel() {
		return m_model;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return new Dimension(600, 400);
	}

	/**
	 * @return the source code for the procedure
	 */
	public String getSource() {
		return m_editor.getText();
	}

	/**
	 * Loads the information from the model into the view components
	 */
	void loadData() {
		if (m_model != null) {
			JTextComponent tc = (JTextComponent) getComponentByName(ID_SOURCE_COMPONENT);
			tc.setText(m_model.getSource());
			ProcedureLanguage lang = m_model.getLanguage();
			if (lang == null) {
				m_languagefield.setText("");
			} else {
				m_languagefield.setText(lang.getLanguage());
			}

		}
	}

	/**
	 * Saves the information in the view into the model
	 */
	void saveToModel() {

	}

	/**
	 * Sets the procedure model and upates the view
	 */
	public void setModel(ProcedureModel model) {
		m_model = model;
		loadData();
		updateComponents();
		m_sqlcomponent.setConnection(model.getConnection());
	}

}
