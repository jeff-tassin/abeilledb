package com.jeta.abeille.gui.model.common;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewModel;
import com.jeta.abeille.gui.model.TableWidget;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.abeille.query.SQLJoiner;
import com.jeta.abeille.query.MySQLSQLBuilder;

/**
 * Copies the joins (as SQL) to the clipboard for the selected tables
 */
public class CopyJoinsAction implements ActionListener {
	private boolean m_qualified = false;
	private ViewGetter m_viewgetter;

	/**
	 * ctor
	 */
	public CopyJoinsAction(ViewGetter viewgetter, boolean qualified) {
		m_viewgetter = viewgetter;
		m_qualified = qualified;
	}

	public void actionPerformed(ActionEvent evt) {
		ModelView view = m_viewgetter.getModelView();
		Collection widgets = view.getSelectedTables();

		if (widgets.size() > 0) {
			LinkedList tables = new LinkedList();
			Iterator iter = widgets.iterator();
			while (iter.hasNext()) {
				TableWidget tw = (TableWidget) iter.next();
				tables.add(tw.getTableId());
			}

			Collection constraints = com.jeta.foundation.utils.EmptyCollection.getInstance();
			/** make the reportables the same as the selected tables */
			Collection reportables = tables;

			ModelViewModel model = view.getModel();
			Collection joins = SQLJoiner.buildJoins(model.getSpecifiedLinkModel(tables), tables, constraints,
					reportables);

			TSConnection tsconn = model.getConnection();

			StringBuffer buff = new StringBuffer();
			iter = joins.iterator();
			while (iter.hasNext()) {
				Link link = (Link) iter.next();
				if (m_qualified) {
					buff.append(MySQLSQLBuilder.buildJoinQualified(link));
				} else {
					buff.append(MySQLSQLBuilder.buildJoin(link));
				}
				if (iter.hasNext())
					buff.append('\n');
			}

			Toolkit kit = Toolkit.getDefaultToolkit();
			Clipboard clipboard = kit.getSystemClipboard();
			StringSelection ss = new StringSelection(buff.toString());
			clipboard.setContents(ss, ss);
		}
	}
}
