package com.jeta.abeille.gui.sql;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.gui.editor.BufferMgr;
import com.jeta.foundation.gui.editor.EditorPreferencesAction;
import com.jeta.foundation.gui.editor.TSTextNames;
import com.jeta.foundation.gui.editor.options.EditorOptionsView;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;
import com.jeta.foundation.utils.TSUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Override EditorPreferences so we can add our own sql settings as well
 */
public class SQLPreferencesAction extends EditorPreferencesAction {
	private JTextField m_delim_field;
	private JTextField m_max_rows_field;

	public SQLPreferencesAction(SQLFrame frame, BufferMgr buffmgr) {
		super(frame, buffmgr);
	}

	/**
	 * Creates the view
	 */
	protected EditorOptionsView createView() {
		EditorOptionsView view = super.createView();

		FormLayout layout = (FormLayout) view.getLayout();
		CellConstraints cc = new CellConstraints();

		layout.appendRow(new RowSpec("4dlu"));
		layout.appendRow(new RowSpec("pref"));

		view.add(new JLabel(I18N.getLocalizedDialogLabel("Max Rows")), cc.xy(1, 5));
		m_max_rows_field = new JTextField(5);
		view.add(m_max_rows_field, cc.xy(3, 5));
		m_max_rows_field.setText(String.valueOf(TSConnection.getMaxQueryRows()));

		layout.appendRow(new RowSpec("4dlu"));
		layout.appendRow(new RowSpec("pref"));

		view.add(new JLabel(I18N.getLocalizedDialogLabel("Delimiter")), cc.xy(1, 7));
		m_delim_field = new JTextField(3);

		JPanel delim_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		delim_panel.add(m_delim_field);
		view.add(delim_panel, cc.xy(3, 7));
		m_delim_field.setText(String.valueOf(TSUserPropertiesUtils.getString(SQLNames.ID_SQL_DELIMITER, ";")));

		return view;
	}

	public void save(EditorOptionsView view) {
		super.save(view);
		TSUserPropertiesUtils.setString(SQLNames.ID_SQL_DELIMITER, TSUtils.fastTrim(m_delim_field.getText()));

		try {
			int max_rows = Integer.parseInt(TSUtils.fastTrim(m_max_rows_field.getText()));
			if (max_rows < 0)
				max_rows = 0;
			TSConnection.setMaxQueryRows(max_rows);
		} catch (Exception e) {

		}

	}
}
