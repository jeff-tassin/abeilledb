/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.foundation.gui.table.TSTablePanel;

/**
 * Printable class for printing a JTable
 */
public class TablePrintable implements Printable {
	/** the table we are printing */
	private JTable m_table;

	/** the space between columns */
	private static final int COLUMN_GAP = 10;

	public TablePrintable(TSTablePanel tpanel) {
		m_table = tpanel.getTable();
	}

	public TablePrintable(JTable table) {
		m_table = table;
	}

	/**
	 * Printable implementation
	 */
	public int print(Graphics pg, PageFormat pageFormat, int pageIndex) throws PrinterException {

		TableModel tmodel = m_table.getModel();
		int wPage = (int) pageFormat.getImageableWidth();
		int hPage = (int) pageFormat.getImageableHeight();

		Font f = m_table.getFont();
		FontMetrics fm = m_table.getFontMetrics(f);
		if (pg != null) {
			pg.setFont(m_table.getFont());
		}

		if (pageIndex == 0) {
			int total_width;
			int x = 0;
			int y = 50;
			if (pg != null) {
				pg.setColor(Color.black);
				pg.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
				pg.setClip(0, 0, wPage, hPage);
			}
			TableColumnModel colmodel = m_table.getColumnModel();
			for (int index = 0; index < colmodel.getColumnCount(); index++) {
				TableColumn column = colmodel.getColumn(index);
				if (pg != null) {
					String colname = tmodel.getColumnName(column.getModelIndex());
					pg.drawString(colname, x, y);
				}
				x = x + column.getWidth() + COLUMN_GAP;
				if (x >= wPage)
					break;
			}

			y += fm.getHeight();

			if (pg != null) {
				pg.translate(0, y);
			}

			int x_total = 0;
			for (int row = 0; row < m_table.getRowCount(); row++) {
				x_total = 0;
				x = 0;
				for (int col = 0; col < colmodel.getColumnCount(); col++) {
					TableColumn column = colmodel.getColumn(col);
					if (pg != null) {
						TableCellRenderer cellrenderer = m_table.getCellRenderer(row, col);
						if (cellrenderer != null) {
							Component comp = cellrenderer.getTableCellRendererComponent(m_table,
									m_table.getValueAt(row, col), false, false, row, col);
							pg.translate(x, 0);
							x_total += x;
							if (comp instanceof JComponent) {
								JComponent jcomp = (JComponent) comp;
								jcomp.setSize(column.getWidth(), fm.getHeight());
								int jx = jcomp.getX();
								int jy = jcomp.getY();
								jcomp.setLocation(0, 0);
								jcomp.print(pg);
								jcomp.setLocation(jx, jy);
								jcomp.setSize(0, 0);
							}
						} else {
							assert (false);
						}
					}
					x = column.getWidth() + COLUMN_GAP;
				}
				y += fm.getHeight();

				if (pg != null) {
					pg.translate(-x_total, fm.getHeight());
				}

				if (y >= hPage)
					break;
			}
			return Printable.PAGE_EXISTS;
		} else {
			return Printable.NO_SUCH_PAGE;
		}

	}

}
