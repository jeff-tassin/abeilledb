package com.jeta.abeille.gui.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

/**
 * Shows the sql results preferences
 * 
 * @author Jeff Tassin
 */
public class SQLResultsPreferencesView extends TSPanel {
	public static final String ID_SQL_RESULTS_CONFIRM_COMMIT = "sql.results.confirm.commit";

	/** the data store for our properties */
	private TSUserProperties m_userprops;

	/** show commit dialog check box */
	private JCheckBox m_commitbox;

	/** component ids */
	public static final String ID_COMMIT_BOX = "commit.checkbox";

	/**
	 * ctor
	 */
	public SQLResultsPreferencesView(TSUserProperties userprops) {
		m_userprops = userprops;
		initialize();
		loadData();
		setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Font f = m_commitbox.getFont();
		FontMetrics metrics = m_commitbox.getFontMetrics(f);
		int width = metrics.stringWidth(m_commitbox.getText()) * 3 + 10;
		int height = metrics.getHeight() * 3 + 10;
		return new Dimension(width, height);
	}

	/**
	 * Creates and initializes the components in the view.
	 */
	private void initialize() {
		setLayout(new BorderLayout());

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel("");
		m_commitbox = new JCheckBox(I18N.getLocalizedMessage("Show Commit Dialog"));

		JComponent[] controls = new JComponent[1];
		controls[0] = m_commitbox;

		JPanel panel = TSGuiToolbox.alignLabelTextRows(labels, controls);
		add(panel, BorderLayout.NORTH);
	}

	/**
	 * Loads the data from the user properties into the model
	 */
	private void loadData() {
		boolean show = Boolean.valueOf(
				m_userprops.getProperty(SQLResultsPreferencesView.ID_SQL_RESULTS_CONFIRM_COMMIT, "true"))
				.booleanValue();
		m_commitbox.setSelected(show);
	}

	/**
	 * Saves the preferences
	 */
	void save() {
		m_userprops.setProperty(SQLResultsPreferencesView.ID_SQL_RESULTS_CONFIRM_COMMIT,
				String.valueOf(m_commitbox.isSelected()));
	}
}
