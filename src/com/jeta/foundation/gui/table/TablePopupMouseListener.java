/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import com.jeta.open.gui.framework.UIDirector;

/**
 * Mouse listener to launch popup menu on right click
 * 
 * @author Jeff Tassin
 */
class TablePopupMouseListener extends MouseAdapter {
	/** the table that this listener is added to */
	private JTable m_table;

	/** the table panel */
	private AbstractTablePanel m_tablepanel;

	TablePopupMouseListener(JTable src, AbstractTablePanel tablepanel) {
		m_table = src;
		m_tablepanel = tablepanel;
	}

	void handlePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JTable table = (JTable) e.getSource();
			if (table == m_table) {
				m_tablepanel.setFocusTable(table);

				int col = table.columnAtPoint(e.getPoint());
				int row = table.rowAtPoint(e.getPoint());
				if (col >= 0) {
					TablePopupMenu popup = m_tablepanel.getPopupMenu();
					popup.setTable(table);
					popup.setRow(row);
					popup.setColumn(col);

					UIDirector uidirector = m_tablepanel.getUIDirector();
					if (uidirector != null)
						uidirector.updateComponents(null);

					popup.show(table, e.getX(), e.getY());
				}
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		handlePopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		handlePopup(e);
	}
}
