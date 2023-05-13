package com.jeta.abeille.gui.model;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JViewport;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorDialog;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.i18n.I18N;

/**
 * This is a generic action that adds a table to the model view. It invokes a
 * TableSelectorDialog that allows the user to choose a table. Once the table is
 * chosen, it is added to the view.
 * 
 * @author Jeff Tassin
 */
public abstract class AddTableAction implements ActionListener {
	/**
	 * The parent frame that will own the table selector dialog
	 */
	private Frame m_parentFrame;

	/** the database model */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public AddTableAction(Frame parentFrame, TSConnection conn) {
		m_parentFrame = parentFrame;
		m_connection = conn;
	}

	public void actionPerformed(ActionEvent evt) {
		try {
			TableSelectorDialog tsdlg = new TableSelectorDialog(m_parentFrame, true);
			tsdlg.setModel(m_connection, getTableSelectorModel());
			tsdlg.setSize(tsdlg.getPreferredSize());
			tsdlg.setTitle(I18N.getLocalizedMessage("Add Table"));

			tsdlg.showCenter();
			if (tsdlg.isOk()) {
				TableId id = tsdlg.createTableId(m_connection);
				ModelView view = getView();
				ModelViewModel viewmodel = view.getModel();
				if (id != null && view != null) {
					TableMetaData tmd = m_connection.getTable(id);
					if (tmd != null) {
						addTable(view, id);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addTable(ModelView view, TableId tableid) {
		ModelViewModel viewmodel = view.getModel();
		TableWidget widget = viewmodel.getTableWidget(tableid);
		if (widget == null) {
			int x = 10;
			int y = 10;
			Object parent = view.getParent();
			if (parent instanceof JViewport) {
				JViewport vp = (JViewport) parent;
				Point pt = vp.getViewPosition();
				x += pt.x;
				y += pt.y;
			}
			view.deselectAll();
			widget = viewmodel.addTable(tableid, x, y);
			view.selectComponent(widget);
		} else {
			int x = 10;
			int y = 10;
			Object parent = view.getParent();
			if (parent instanceof JViewport) {
				JViewport vp = (JViewport) parent;
				Point pt = vp.getViewPosition();
				widget.setLocation(pt.x, pt.y);
			}
		}

	}

	protected abstract ModelView getView();

	protected abstract TableSelectorModel getTableSelectorModel();

}
