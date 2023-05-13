package com.jeta.abeille.gui.model;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;

import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.i18n.I18N;

/**
 * The drop listener interface for the model view
 */
public class TableViewDropListener extends ModelViewDropListener {

	/**
	 * ctor
	 */
	public TableViewDropListener(ModelView view) {
		super(view);
	}

	/**
	 * Utility method to support dropping objects on model view
	 */
	public void drop(Transferable transferable, Point pt) {

		try {
			if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_PROTOTYPE_REFERENCE)) {
				String msg = I18N.getLocalizedMessage("Cannot_drop_prototype_tables_here");
				String title = I18N.getLocalizedMessage("Error");
				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_WIDGET_REFERENCE)) {
				Object obj = transferable.getTransferData(DbObjectFlavor.TABLE_WIDGET_REFERENCE);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					if (!isFromCurrentConnection(results)) {
						String msg = I18N.getLocalizedMessage("Cannot_drop_tables_from_other_connections_here");
						String title = I18N.getLocalizedMessage("Error");
						JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}

			if (transferable.isDataFlavorSupported(DbObjectFlavor.TABLE_REFERENCE)) {
				// this is for table objects that are being dropped from other
				// locations such as the object tree
				Object obj = transferable.getTransferData(DbObjectFlavor.TABLE_REFERENCE);
				if (obj instanceof Object[]) {
					Object[] results = (Object[]) obj;
					if (!isFromCurrentConnection(results)) {
						String msg = I18N.getLocalizedMessage("Cannot_drop_tables_from_other_connections_here");
						String title = I18N.getLocalizedMessage("Error");
						JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			super.drop(transferable, pt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
