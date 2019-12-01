package com.jeta.abeille.gui.importer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class GetResultSetDialog extends TSDialog {
	private static ImageIcon m_icon;

	private GetResultSetView m_view;

	static {
		m_icon = TSGuiToolbox.loadImage("query_results16.gif");
	}

	/**
	 * ctor
	 */
	public GetResultSetDialog(java.awt.Frame frame, boolean bmodal) {
		super(frame, bmodal);

	}

	/**
	 * @return the selected result set
	 */
	QueryResultSet getSelectedResultSet() {
		return m_view.getSelectedResultSet();
	}

	/**
	 * Initializes the dialog with a collection of QueryResultSet Objects
	 */
	public void initialize(Collection rsets) {
		m_view = new GetResultSetView(rsets);
		setPrimaryPanel(m_view);
	}

	/**
	 * This panel displays a label and a combo box beneath the label. The combo
	 * box shows all opened SQL results frames in the application
	 */
	private class GetResultSetView extends TSPanel {
		/** the main label above the combo */
		private JLabel m_label;

		/** the combo */
		private JComboBox m_combo;

		/**
		 * Initializes the view with a collection of QueryResultSet Objects
		 */
		GetResultSetView(Collection rsets) {
			initialize();
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			Iterator iter = rsets.iterator();
			while (iter.hasNext()) {
				QueryResultSet rset = (QueryResultSet) iter.next();
				m_combo.addItem(rset);
			}
		}

		/**
		 * @return the preferred size for this panel
		 */
		public Dimension getPreferredSize() {
			Dimension d = m_combo.getPreferredSize();
			Dimension d2 = m_label.getPreferredSize();
			d.width = d2.width * 3;
			d.height *= 4;
			return d;
		}

		/**
		 * @return the selected result set
		 */
		QueryResultSet getSelectedResultSet() {
			return (QueryResultSet) m_combo.getSelectedItem();
		}

		private void initialize() {
			m_label = new JLabel(I18N.getLocalizedMessage("Select Result Set"));
			m_label.setIcon(m_icon);

			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = 0;
			c.insets = new Insets(5, 5, 5, 5);
			c.anchor = GridBagConstraints.NORTHWEST;
			add(m_label, c);

			c.gridy = 1;
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;

			m_combo = new JComboBox();
			m_combo.setRenderer(new QueryResultsCellRenderer());

			add(m_combo, c);

			c.gridy = 2;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.VERTICAL;
			add(new JLabel(""), c);
		}
	}

	/**
	 * Connection box renderer
	 */
	static class QueryResultsCellRenderer extends JLabel implements ListCellRenderer {

		public QueryResultsCellRenderer() {
			// must set or the background color won't show
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			QueryResultSet rset = (QueryResultSet) value;
			String sql = rset.getSQL();
			if (sql.length() > 38) {
				sql = sql.substring(0, 37) + "...";
			}
			setText(sql);
			if (isSelected) {
				setBackground(UIManager.getColor("List.selectionBackground"));
				setForeground(UIManager.getColor("List.selectionForeground"));
			} else {
				setBackground(UIManager.getColor("List.background"));
				setForeground(UIManager.getColor("List.foreground"));
			}
			return this;
		}
	}

}
