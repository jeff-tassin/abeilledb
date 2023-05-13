package com.jeta.abeille.gui.help;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.PopupList;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.forms.components.panel.FormPanel;

/**
 */
public class SQLReferenceFrame extends TSInternalFrame {
	/** the frame icon for this frame */
	private static ImageIcon m_frameicon;

	/**
	 * The database connectionx
	 */
	private TSConnection m_connection;

	private FormPanel m_view;

	private TreeMap m_sql_entries = new TreeMap();

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/data_scroll.png");
	}

	/**
	 * ctor
	 */
	public SQLReferenceFrame() {
		super(I18N.getLocalizedMessage("SQL Reference"));
		setFrameIcon(m_frameicon);
		m_view = new FormPanel("com/jeta/abeille/gui/help/sqlReference.jfrm");
		setController(new SQLReferenceController(m_view, this));
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Creates and initializes the components on this frame
	 * 
	 * @param params
	 *            the list of parameters need to initialize this frame
	 *            Currently, we expect an array (size == 2) where the first
	 *            parameter must be the database connection (TSConnection) and
	 *            the second parameter must be a StoredProcedure object ( can be
	 *            null)
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];
		setTitle(I18N.getLocalizedMessage("SQL Reference"));
		setShortTitle(I18N.getLocalizedMessage("SQL Reference"));

		getContentPane().add(m_view, BorderLayout.CENTER);

		SQLReferenceService factory = (SQLReferenceService) m_connection
				.getImplementation(SQLReferenceService.COMPONENT_ID);
		if (factory != null) {
			SQLReference ref = factory.getReference();
			Collection entries = ref.getHelpEntries();
			TSComboBox cbox = (TSComboBox) m_view.getComponentByName(SQLReferenceNames.ID_TOPIC_COMBO);

			PopupList list = cbox.getPopupList();
			list.getModel().setComparator(new SQLHelpEntryComparator());

			Iterator iter = entries.iterator();
			while (iter.hasNext()) {
				SQLHelpEntry entry = (SQLHelpEntry) iter.next();
				cbox.addItem(entry);
				m_sql_entries.put(entry.getDisplayLabel(), entry);
			}
		}
	}

	public URL lookupSQLReference(String displayLabel) {
		SQLHelpEntry entry = (SQLHelpEntry) m_sql_entries.get(displayLabel);
		SQLReferenceService factory = (SQLReferenceService) m_connection
				.getImplementation(SQLReferenceService.COMPONENT_ID);
		try {
			if (entry != null && factory != null) {
				SQLReference ref = factory.getReference();
				return ref.getContent(entry.getUrl());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
    */
	public void showReference(URL url) {
		try {
			if (url != null) {
				JEditorPane editor = (JEditorPane) m_view.getTextComponent("help.editor");
				if (editor != null) {
					editor.setPage(url);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
