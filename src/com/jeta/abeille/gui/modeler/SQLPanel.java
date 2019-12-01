package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;

/**
 * This panel shows the SQL that is generated from the table definition in the
 * table editor
 * 
 * @author Jeff Tassin
 */
public class SQLPanel extends TSPanel {
	/** the sql text editor */
	private JTextComponent m_sqleditor;

	/**
	 * ctor
	 */
	public SQLPanel(TSConnection conn) {
		setLayout(new BorderLayout());

		SQLComponent sqlcomp = SQLUtils.createSQLComponent(conn);
		m_sqleditor = sqlcomp.getEditor();
		m_sqleditor.setEditable(false);
		JComponent comp = sqlcomp.getExtComponent();

		add(comp, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel btnpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel label = new JLabel(I18N.getLocalizedMessage("<html><body><u>Save to file</u></body></html>"));
		label.setIcon(TSGuiToolbox.loadImage("incors/16x16/disk_blue.png"));
		label.setName(ModelerNames.ID_SQL_SAVE_TO_FILE);
		label.setHorizontalTextPosition(javax.swing.JLabel.LEFT);
		label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		btnpanel.add(label);

		add(btnpanel, BorderLayout.SOUTH);

		setController(new SQLPanelController());
	}

	/**
	 * Sets the text of the editor
	 */
	public void setText(String txt) {
		m_sqleditor.setText(txt);
		m_sqleditor.setCaretPosition(0);
	}

	/**
	 * Controller for this panel
	 */
	class SQLPanelController extends TSController {

		SQLPanelController() {
			super(SQLPanel.this);
			getLabel(ModelerNames.ID_SQL_SAVE_TO_FILE).addMouseListener(new SaveSQLAction());
		}

		/**
		 * Action that invokes a file save dialog. This allows the user to save
		 * the sql to a file
		 */
		public class SaveSQLAction extends MouseAdapter {
			public void mouseClicked(MouseEvent evt) {
				File f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory.showSaveDialog();
				if (f != null) {
					try {
						com.jeta.foundation.utils.TSUtils.safeSaveFile(f, m_sqleditor);
					} catch (Exception e) {
						TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, m_sqleditor,
								true);
						dlg.initialize(I18N.getLocalizedMessage("Error"), e);
						dlg.showCenter();
					}
				}
			}
		}
	}
}
