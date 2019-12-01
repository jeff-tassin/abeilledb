package com.jeta.abeille.gui.common;

import java.awt.BorderLayout;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.StyledBannerView;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a TableSelectorPanel as well as a text area that provides
 * a help message for this adding tables to a view. If you want to just use a
 * TableSelector without the help message, use the TableSelectorPanel
 * 
 * @author Jeff Tassin
 */
public class TableSelectorView extends TSPanel {
	private TableSelectorPanel m_tableselectorpanel;

	/**
	 * ctor
	 */
	public TableSelectorView(TSConnection tsconn) {
		m_tableselectorpanel = new TableSelectorPanel(tsconn);
		createView();
	}

	private void createView() {
		setLayout(new BorderLayout());

		java.util.LinkedList paras = new java.util.LinkedList();
		paras.add(new StyledBannerView.Paragraph("title", new StyledBannerView.Run("none", I18N
				.getLocalizedDialogLabel("Add Table"))));
		paras.addAll(StyledBannerView.createParagraphs("normal", I18N.getLocalizedMessage("Add_Table_msg")));

		add(new StyledBannerView(paras, m_tableselectorpanel), BorderLayout.CENTER);
	}

	public TableSelectorPanel getTableSelectorPanel() {
		return m_tableselectorpanel;
	}
}
