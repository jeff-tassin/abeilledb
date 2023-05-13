package com.jeta.abeille.gui.importer;

import java.awt.BorderLayout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableColumnModel;

import com.jeta.abeille.gui.common.MetaDataTableRenderer;
import com.jeta.abeille.gui.model.MultiTransferable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of columns used in the ImportBuilder
 * 
 * @author Jeff Tassin
 */
public class SourceColumnsView extends TSPanel {
	private JTable m_table;

	private SourceColumnsModel m_model;

	/*
	 * ctor
	 */
	public SourceColumnsView(SourceColumnsModel model) {
		setLayout(new BorderLayout());
		setModel(model);
	}

	public void setModel(SourceColumnsModel model) {
		m_model = model;
		removeAll();
		TSTablePanel tpanel = TableUtils.createSimpleTable(model, false);
		add(tpanel, BorderLayout.CENTER);

		m_table = tpanel.getTable();
		TableColumnModel cmodel = m_table.getColumnModel();
		// cmodel.getColumn( SourceColumnsModel.COLUMN_NAME_COLUMN
		// ).setCellRenderer( new MetaDataTableRenderer() );

		m_table.setDragEnabled(true);
		m_table.setTransferHandler(new SourceTransferHandler());

	}

	public class SourceTransferHandler extends TransferHandler {

		/**
		 * No imports
		 */
		public boolean canImport(JComponent comp, DataFlavor[] flavors) {
			return false;
		}

		/**
		 * Creates the transferable for our table widget
		 */
		protected Transferable createTransferable(JComponent comp) {
			MultiTransferable mt = null;
			if (comp instanceof JTable) {
				int row = m_table.getSelectedRow();
				if (row >= 0) {
					mt = new MultiTransferable();
					SourceColumn sc = m_model.getRow(row);
					mt.addData(ImportObjectFlavor.SOURCE_COLUMN, sc);
				}
			}
			return mt;
		}

		/**
		 * Always return copy for this handler
		 */
		public int getSourceActions(JComponent comp) {
			return TransferHandler.COPY;
		}

		/**
		 * No import here
		 */
		public boolean importData(JComponent comp, Transferable t) {
			return false;
		}
	}

}
