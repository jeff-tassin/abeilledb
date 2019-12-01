package com.jeta.abeille.gui.main.options;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.SchemaSelectorPanel;
import com.jeta.abeille.gui.common.TableSelectorModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class displays a schema selector panel. It allows the user to select a
 * database/schema used in SQL completion and the default values for table
 * properties dialogs.
 * 
 * @author Jeff Tassin
 */
public class SetPathView extends TSPanel {
	private TSConnection m_connection;
	private TableSelectorModel m_tableselector;
	private SchemaSelectorPanel m_schemaselector;

	public SetPathView(TSConnection tsconn) {
		m_connection = tsconn;
		m_tableselector = new DefaultTableSelectorModel(m_connection);

		// Create a reusable CellConstraints instance.
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("pref:grow", "fill:50dlu, 3px, fill:pref, 20px");

		setLayout(layout);

		add(buildTextPanel(), cc.xy(1, 1));
		add(TSGuiToolbox.createSeparator(null), cc.xy(1, 2));
		add(createBottomPanel(), cc.xy(1, 3));
	}

	private JComponent buildTextPanel() {
		StyleContext sc = new StyleContext();
		DefaultStyledDocument doc = new DefaultStyledDocument(sc);
		SearchPathStyles sp = new SearchPathStyles(doc, sc);
		sp.loadDocument();
		JTextPane p = new MyTextPane(doc);
		p.setEditable(false);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(p, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createBottomPanel() {
		JPanel panel = new JPanel();
		// Create a reusable CellConstraints instance.
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("10px, pref, 12px, left:pref:grow, 12dlu", "top:10dlu, pref");

		panel.setLayout(layout);

		JLabel label = new JLabel();
		label.setIcon(javax.swing.UIManager.getIcon("OptionPane.questionIcon"));
		panel.add(label, cc.xy(2, 2, "l,t"));

		m_schemaselector = SchemaSelectorPanel.createInstance(m_connection, m_tableselector);
		panel.add(m_schemaselector, cc.xy(4, 2));
		return panel;
	}

	public Catalog getCatalog() {
		return m_schemaselector.getCatalog();
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	public Schema getSchema() {
		return m_schemaselector.getSchema();
	}

	/**
	 * Override JTextPane so we can prevent sizing too small so that the text
	 * word-wraps.
	 */
	public static class MyTextPane extends JTextPane {
		public MyTextPane(DefaultStyledDocument doc) {
			super(doc);
		}

		public void setBounds(int x, int y, int width, int height) {
			Dimension size = this.getPreferredSize();
			super.setBounds(x, y, Math.max(size.width, width), height);
		}
	}
}
