package com.jeta.abeille.gui.sequences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.sequences.Sequence;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.DbGuiUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * This view displays the properties for a sequence
 * 
 * @author Jeff Tassin
 */
public class SequenceView extends TSPanel implements JETARule {
	/** the database connection */
	private TSConnection m_connection;

	/** the catalog */
	private Catalog m_catalog;

	/** combo box that displays the available schemas */
	private TSComboBox m_schemascombo;

	/** the name for this sequence */
	private JTextField m_name = new JTextField();

	/** the starting value for this sequence */
	private JTextField m_start = TSGuiToolbox.createNumericTextField();

	/** then increment for this sequence */
	private JTextField m_increment = TSGuiToolbox.createNumericTextField();

	/** min value for this sequence */
	private JTextField m_min = TSGuiToolbox.createNumericTextField();

	/** the max value for this sequence */
	private JTextField m_max = TSGuiToolbox.createNumericTextField();

	/** the cache value for this sequence */
	private JTextField m_cache = TSGuiToolbox.createNumericTextField();

	/** the last value for this sequence */
	private JTextField m_lastvalue = TSGuiToolbox.createNumericTextField();

	/** we use this for resizing */
	private JLabel m_namelabel = new JLabel(I18N.getLocalizedMessage("Name"));

	/**
	 * Indicates if this sequence is temporary
	 */
	private JCheckBox m_temporary;

	/**
	 * This indicates if the sequence should wrap around if the max value is
	 * exceeded.
	 */
	private JCheckBox m_cycle;

	/** the sequence created by this view */
	private Sequence m_newsequence;

	/**
	 * ctor for creating a new sequence
	 * 
	 * @param connection
	 *            the database connection for the sequence
	 * @param seq
	 *            the sequence we are editing. If this is null, then we are
	 *            creating a new sequence
	 * @param selectedSchema
	 *            the schema to select in the schemas combo.
	 */
	public SequenceView(TSConnection connection, Sequence seq, Catalog catalog, Schema selectedSchema) {
		m_connection = connection;
		m_catalog = catalog;
		assert (catalog != null);

		setLayout(new BorderLayout());
		add(createView(), BorderLayout.NORTH);

		if (selectedSchema != null)
			m_schemascombo.setSelectedItem(selectedSchema);

		loadData(seq);
	}

	/**
	 * JETARule implementation
	 */
	public RuleResult check(Object[] params) {
		if (getName().length() == 0)
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		else
			return RuleResult.SUCCESS;
	}

	/**
	 * @return a newly created Sequence object based on the data entered in the
	 *         view
	 */
	public Sequence createSequence() {
		if (m_newsequence == null) {
			m_newsequence = new Sequence(null);
		}

		m_newsequence.setId(new DbObjectId(DbObjectType.SEQUENCE, getCatalog(), getSchema(), getName()));
		m_newsequence.setLastValue(getLastValue());
		m_newsequence.setIncrement(getIncrement());
		m_newsequence.setMax(getMax());
		m_newsequence.setMin(getMin());
		m_newsequence.setCache(getCache());
		m_newsequence.setCycle(isCycle());
		m_newsequence.setTemporary(isTemporary());

		if (m_start.isEnabled())
			m_newsequence.setStart(getStart());
		else
			m_newsequence.setStart(getLastValue());
		return m_newsequence;
	}

	/**
	 * Creates the components for this view
	 */
	public JPanel createView() {
		Component[] left = new Component[9];

		m_schemascombo = DbGuiUtils.createSchemasCombo(m_connection, m_catalog);

		left[0] = new JLabel(I18N.getLocalizedMessage("Schema"));
		left[1] = m_namelabel;
		left[2] = new JLabel("");
		left[3] = new JLabel(I18N.getLocalizedMessage("Start"));
		left[4] = new JLabel(I18N.getLocalizedMessage("Increment"));
		left[5] = new JLabel(I18N.getLocalizedMessage("Min"));
		left[6] = new JLabel(I18N.getLocalizedMessage("Max"));
		left[7] = new JLabel(I18N.getLocalizedMessage("Cache"));
		left[8] = new JLabel(I18N.getLocalizedMessage("Last Value"));

		// checks panel
		JPanel checkspanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		m_temporary = new JCheckBox(I18N.getLocalizedMessage("Temporary"));

		checkspanel.add(m_temporary);
		checkspanel.add(javax.swing.Box.createHorizontalStrut(10));

		m_cycle = new JCheckBox(I18N.getLocalizedMessage("Cycle"));
		checkspanel.add(m_cycle);

		Component[] right = new Component[9];
		right[0] = m_schemascombo;
		right[1] = m_name;
		right[2] = checkspanel;
		right[3] = m_start;
		right[4] = m_increment;
		right[5] = m_min;
		right[6] = m_max;
		right[7] = m_cache;
		right[8] = m_lastvalue;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_schemascombo, 30);
		layout.setMaxTextFieldWidth(m_name, 30);
		layout.setMaxTextFieldWidth(m_start, 20);
		layout.setMaxTextFieldWidth(m_increment, 10);
		layout.setMaxTextFieldWidth(m_min, 20);
		layout.setMaxTextFieldWidth(m_max, 20);
		layout.setMaxTextFieldWidth(m_cache, 10);
		layout.setMaxTextFieldWidth(m_lastvalue, 20);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right, new java.awt.Insets(10, 10, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		m_lastvalue.setEnabled(false);

		return panel;
	}

	/**
	 * @return the cache value entered by the user
	 */
	public Long getCache() {
		return toLong(m_cache.getText());
	}

	/**
	 * @return the catalog
	 */
	public Catalog getCatalog() {
		return m_catalog;
	}

	/**
	 * @return the increment value entered by the user
	 */
	public Long getIncrement() {
		return toLong(m_increment.getText());
	}

	/**
	 * @return the last value entered by the user
	 */
	public Long getLastValue() {
		return toLong(m_lastvalue.getText());
	}

	/**
	 * @return the max value entered by the user
	 */
	public Long getMax() {
		return toLong(m_max.getText());
	}

	/**
	 * @return the minimum value entered by the user
	 */
	public Long getMin() {
		return toLong(m_min.getText());
	}

	/**
	 * @return the name in the text field
	 */
	public String getName() {
		return m_name.getText();
	}

	/**
	 * @return the start value entered by the user
	 */
	public Long getStart() {
		return toLong(m_start.getText());
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		Dimension d = m_name.getPreferredSize();
		Dimension d2 = m_namelabel.getPreferredSize();
		d.height *= 15;
		d.width = d2.width * 10;
		return d;
	}

	/**
	 * @return the selected schema
	 */
	public Schema getSchema() {
		Schema result = DbUtils.getSchema(m_connection, m_catalog, m_schemascombo.getText());
		return result;
	}

	/**
	 * @return the cycle value entered by the user
	 */
	public Boolean isCycle() {
		return Boolean.valueOf(m_cycle.isSelected());
	}

	/**
	 * @return the temporary flag entered by the user
	 */
	public Boolean isTemporary() {
		return Boolean.valueOf(m_temporary.isSelected());
	}

	/**
	 * Loads the sequence information into the view
	 */
	private void loadData(Sequence seq) {
		if (seq != null) {
			m_name.setText(seq.getName());

			m_increment.setText(safeToString(seq.getIncrement()));
			m_min.setText(safeToString(seq.getMin()));
			m_max.setText(safeToString(seq.getMax()));
			m_cache.setText(safeToString(seq.getCache()));
			m_lastvalue.setText(safeToString(seq.getLastValue()));

			if (seq.isCycle() == null) {
				m_cycle.setSelected(false);
			} else {
				m_cycle.setSelected(seq.isCycle().booleanValue());
			}

			m_schemascombo.setEnabled(false);
			m_lastvalue.setEnabled(true);
			m_name.setEnabled(false);
			m_start.setEnabled(false);
			m_increment.setEnabled(false);
			m_min.setEnabled(false);
			m_max.setEnabled(false);
			m_cache.setEnabled(false);
			m_temporary.setEnabled(false);
			;
			m_cycle.setEnabled(false);
			m_start.setEnabled(false);
		}
	}

	/**
	 * Simple helper method that safely gets the toString of an object
	 */
	private String safeToString(Object obj) {
		if (obj == null)
			return "";
		else
			return obj.toString();
	}

	private Long toLong(String txt) {
		if (txt == null)
			return null;
		else {
			txt = txt.trim();
			if (txt.length() == 0)
				return null;
			else
				return Long.valueOf(txt);
		}
	}

}
