package com.jeta.abeille.gui.query;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSToolBarTemplate;

import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.gui.editor.BufferEvent;
import com.jeta.foundation.gui.editor.BufferMgr;
import com.jeta.foundation.gui.editor.EditorController;
import com.jeta.foundation.gui.editor.EditorFrameUIDirector;
import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSTextNames;

import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAContainer;

/**
 * A dialog for editing custom SQL in the query builder.
 * 
 * @author Jeff Tassin
 */
public class SQLEditorDialog extends TSDialog {
	private JEditorPane m_editor;

	private BufferMgr m_buffermgr;

	private JTextField m_namefield;

	/**
	 * Constructor
	 */
	public SQLEditorDialog(Frame owner, boolean bModal) {
		super(owner, bModal);
	}

	/**
	 * Creates the menu used for this frame window
	 */
	protected void createMenu() {
		TSEditorUtils.buildMenu(this);
		// MenuTemplate template = getMenuTemplate();

		// JMenuBar menubar = getMenuBar();
		// menubar.remove( template.getMenuAt(
		// TSEditorUtils.MENUBAR_OPTIONS_INDEX ) );

		// JMenu menu = template.getMenuAt(0);
		// menu.remove(0);
	}

	/**
	 * Creates the toolbar used for this frame window
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = getToolBarTemplate();

		template.add(i18n_createToolBarButton(FrameKit.openFileAction, "general/Open16.gif", "Open"));
		template.add(i18n_createToolBarButton(FrameKit.saveFileAction, "general/Save16.gif", "Save"));
		template.addSeparator();
		template.add(i18n_createToolBarButton(TSTextNames.ID_CUT, "general/Cut16.gif", "Cut"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_COPY, "general/Copy16.gif", "Copy"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_PASTE, "general/Paste16.gif", "Paste"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_UNDO, "general/Undo16.gif", "Undo"));
		template.add(i18n_createToolBarButton(TSTextNames.ID_FIND, "general/Find16.gif", "Find"));

		template.add(Box.createHorizontalStrut(20));

		template.add(new JLabel(I18N.getLocalizedDialogLabel("Name")));
		template.add(Box.createHorizontalStrut(5));

		m_namefield = new JTextField(20);
		Dimension d = m_namefield.getPreferredSize();
		m_namefield.setPreferredSize(d);
		m_namefield.setMaximumSize(d);
		m_namefield.setMinimumSize(d);
		template.add(m_namefield);
	}

	/**
	 * @return the editor window for this frame
	 */
	public JEditorPane getCurrentEditor() {
		return m_editor;
	}

	/**
	 * @return the preferred size for the dialog
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(12, 20);
	}

	/**
	 * @return the name for this query
	 */
	public String getName() {
		return m_namefield.getText();
	}

	/**
	 * @return the text in the editor
	 */
	public String getText() {
		return m_editor.getText();
	}

	/**
	 * Initializes the frame window
	 */
	public void initialize(TSConnection conn, String sql, String name) {
		enableToolBar();
		enableMenuBar();

		createMenu();
		createToolBar();

		SQLComponent sqlcomp = SQLUtils.createSQLComponent(conn);
		m_editor = sqlcomp.getEditor();
		JComponent comp = sqlcomp.getExtComponent();

		setPrimaryPanel(comp);

		Buffer buff = new Buffer();
		buff.setContentPanel(comp);
		buff.setEditor(m_editor);
		m_buffermgr = new BufferMgr(buff, true);

		EditorController controller = new SQLEditorController(this, m_buffermgr);
		setUIDirector(new EditorFrameUIDirector(this, m_buffermgr));
		setController(controller);

		setTitle(I18N.getLocalizedMessage("SQL Editor"));
		setText(sql);
		m_namefield.setText(name);
	}

	/**
	 * Sets the text for the sql window. Any existing text is replaced
	 * 
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		if (text == null)
			text = "";

		JEditorPane editor = getCurrentEditor();
		if (editor != null)
			editor.setText(text);

	}

	public class SQLEditorController extends EditorController {
		public SQLEditorController(JETAContainer window, BufferMgr mgr) {
			super(window, mgr);
			assignAction(FrameKit.openFileAction, new OpenFileAction());
		}

		public class OpenFileAction implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				java.io.File f = TSFileChooserFactory.showOpenDialog();
				if (f != null) {
					Buffer buff = m_buffermgr.getCurrentBuffer();
					SQLEditorController.this.loadFile(buff, f);
					buff.getContentPanel().validate();
					buff.getContentPanel().repaint();
					org.netbeans.editor.Utilities.requestFocus(m_editor);
				}
			}
		}
	}
}
