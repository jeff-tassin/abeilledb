package com.jeta.abeille.gui.queryresults;

import javax.swing.*;
import java.awt.*;

public class RendererHelper {
	private Color m_deletecolor;
	private Color m_selected;
	private Color m_selectedback;

	private Color m_standard;
	private Color m_standardback;
	private Color m_evalcolor;

	private static Boolean m_iseval;

	private static Color m_null_color = new Color(224, 224, 224);

	public RendererHelper() {
		m_deletecolor = new Color(200, 200, 200);
		m_selected = UIManager.getColor("Table.selectionForeground");
		m_selectedback = UIManager.getColor("Table.selectionBackground");
		m_standard = UIManager.getColor("Table.foreground");
		m_standardback = UIManager.getColor("Table.background");
		m_evalcolor = new Color(235, 235, 235);
	}

	private static boolean isEvaluation() {
		/**
		 * if ( m_iseval == null ) { LicenseManager jlm =
		 * (LicenseManager)ComponentMgr.lookup( LicenseManager.COMPONENT_ID );
		 * m_iseval = Boolean.valueOf( jlm.isEvaluation() ); } return
		 * m_iseval.booleanValue();
		 */
		return false;
	}

	/**
	 * Helper method to render comopnent colors in the QueryResultsView
	 */
	void renderComponentColors(Component comp, QueryResultsView view, JTable table, Object aValue, boolean bSelected,
			int row, int col) {
		if (isEvaluation() && row > 24) {
			comp.setForeground(m_evalcolor);
			comp.setBackground(m_evalcolor);
		} else {
			if (view == null) {
				if (bSelected) {
					comp.setForeground(m_selected);
					comp.setBackground(m_selectedback);
				} else {
					comp.setForeground(m_standard);
					comp.setBackground(m_standardback);
				}
			} else {
				Color forecolor = m_standard;

				if (bSelected) {
					forecolor = m_selected;
				} else {
					forecolor = m_standard;
				}

				comp.setForeground(forecolor);
				if (bSelected) {
					comp.setBackground(m_selectedback);
				} else {
					if (aValue == null) {
						comp.setBackground(m_null_color);
					} else {
						comp.setBackground(m_standardback);
					}
				}
			}
		}
	}

}
