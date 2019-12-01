package com.jeta.abeille.gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a view for a SQL Command.
 * 
 * @author Jeff Tassin
 */
public class SQLCommandView extends TSPanel {
	/** the editor for this view - displays the SQL */
	private JEditorPane m_editor;

	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public SQLCommandView(TSConnection conn, String sql) {
		this(conn, sql, false);
	}

	/**
	 * ctor
	 */
	public SQLCommandView(TSConnection conn, String sql, boolean beditable) {
		m_connection = conn;
		createComponents();
		m_editor.setText(sql);
		m_editor.setEditable(beditable);
	}

	/**
	 * Creates and initializes the components on this view
	 */
	void createComponents() {
		setLayout(new BorderLayout());
		add(createEditorComponents(), BorderLayout.CENTER);
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
		SQLComponent sqlcomp = SQLUtils.createSQLComponent(m_connection);

		m_editor = sqlcomp.getEditor();
		JComponent comp = sqlcomp.getExtComponent();

		TSPanel panel = new TSPanel();
		panel.setLayout(new BorderLayout());
		panel.add(comp, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * @return the underlying editor
	 */
	public JEditorPane getEditor() {
		return m_editor;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(10, 10);
	}

	public String getSQL() {
		return m_editor.getText();
	}
}
