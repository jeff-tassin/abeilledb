package com.jeta.abeille.gui.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.jdbc.ResultSetInfoPanel;
import com.jeta.abeille.gui.jdbc.ResultSetParametersView;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.i18n.I18N;

/**
 * This dialog shows ResultSetMetaData and the SQL for a given query.
 * 
 * @author Jeff Tassin
 */
public class QueryInfoDialog extends TSDialog {
	private ResultSetInfoPanel m_controlspanel; // the panel that contains the
												// controls for this dialog

	/**
	 * QueryInfoDialog constructor
	 */
	public QueryInfoDialog(Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * QueryInfoDialog constructor
	 */
	public QueryInfoDialog(Dialog owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * @return the preferred size for this dialog so that the caller can size
	 *         the window properly
	 */
	public Dimension getPreferredSize() {
		return new Dimension(600, 400);
	}

	/**
	 * Creates and initializes the controls for this dialog.
	 * 
	 * @param rset
	 *            the ResultSet whose metadata we will show
	 * @param sql
	 *            the SQL string to show
	 * @throws SQLException
	 */
	public void initialize(TSConnection conn, ResultSetReference rsetref, String sql) throws SQLException {
		Container container = this.getDialogContentPanel();
		container.setLayout(new BorderLayout());

		JTabbedPane tabpane = new JTabbedPane();
		ResultSetInfoPanel rsetpanel = new ResultSetInfoPanel(conn);
		ResultSetParametersView paramsview = new ResultSetParametersView(conn);
		tabpane.addTab("ResultSetMetaData", rsetpanel); // NOI18N
		tabpane.addTab(I18N.getLocalizedMessage("Parameters"), paramsview);

		SQLComponent sqlcomp = SQLUtils.createSQLComponent(conn);
		JEditorPane editor = sqlcomp.getEditor();
		JScrollPane scroll = new JScrollPane(editor);
		tabpane.addTab("SQL", scroll);
		editor.setText(sql);
		editor.setEditable(false);

		rsetpanel.refresh(rsetref.getMetaData());
		paramsview.refresh(rsetref.getResultSet());

		container.add(tabpane, BorderLayout.CENTER);
	}

}
