package com.jeta.abeille.gui.update;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.jeta.abeille.database.model.ColumnMetaData;

/**
 * This class is used by the UpdateFrame to define the GUI elements for a single
 * column in the table. Currently, a column element is defined as folows:
 * 
 * [key icon][col name][filter button][jcomponent]
 * 
 * @author Jeff Tassin
 */
public class FieldElement {
	AbstractButton m_icon; // an icon for the primary or foreign keys
	JLabel label; // the column name label
	InstanceComponent fieldcomponent; // the component for entering/displaying
										// the data
	ColumnMetaData cmd; // the column meta data object associated with this
						// element
	AbstractButton filterbtn; // the constraint button
	String m_labeltxt; // the text to show in the label
	private boolean m_bmodified;

	private boolean m_link;

	// at some point we might make this user configurable
	static final int MAX_FIELD_NAME_LENGTH = 30; // the length of the longest
													// field name

	/**
	 * ctor
	 */
	public FieldElement(ColumnMetaData cmd, ColumnSettings settings, boolean link) {
		this.cmd = cmd;
		String labeltxt = cmd.getColumnName();
		if (settings != null) {
			String displayname = settings.getDisplayName();
			if (displayname != null && displayname.trim().length() > 0) {
				labeltxt = displayname;
			}
		}
		m_labeltxt = labeltxt;
		m_link = link;
		label = new JLabel();

		// label.setBorder( javax.swing.BorderFactory.createLineBorder(
		// java.awt.Color.red ) );

		m_icon = new JButton();
		java.awt.Dimension d = new java.awt.Dimension(16, 16);
		m_icon.setSize(d);
		m_icon.setMaximumSize(d);
		m_icon.setPreferredSize(d);
		m_icon.setFocusPainted(false);
		m_icon.setBorderPainted(false);
		m_icon.setVisible(false);
		m_icon.setContentAreaFilled(false);
		m_icon.setFocusable(false);

		updateLabel();
	}

	/**
	 * @return the column metadata associated with this field element
	 */
	public ColumnMetaData getColumnMetaData() {
		return cmd;
	}

	/**
	 * @return the button used for specifying filters for this column
	 */
	public AbstractButton getFilterButton() {
		return filterbtn;
	}

	/**
	 * @return the icon button to the left of the field element
	 */
	public AbstractButton getIconButton() {
		return m_icon;
	}

	/**
	 * @return the component used to display data for this column
	 */
	public InstanceComponent getInstanceComponent() {
		return fieldcomponent;
	}

	/**
	 * @return the label for this component
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * @return the text displayed in the label
	 */
	public String getLabelText() {
		return m_labeltxt;
	}

	public boolean isLink() {
		return m_link;
	}

	/**
	 * Sets the icon button to the left of the field element
	 */
	public void setIcon(Icon icon) {
		m_icon.setIcon(icon);
		if (icon == null) {
			m_icon.setVisible(false);
		} else {
			m_icon.setVisible(true);
		}
	}

	/**
	 * Sets the text to show in the label
	 */
	public void setLabelText(String txt) {
		m_labeltxt = txt;
		updateLabel();
	}

	/**
	 * Updates the text in the label
	 */
	public void updateLabel() {
		StringBuffer sbuff = new StringBuffer();
		if (isLink()) {
			sbuff.append("<html><body><u>");
		}

		if (m_bmodified) {
			sbuff.append(m_labeltxt);
			sbuff.append('*');
		} else {
			sbuff.append(m_labeltxt);
		}

		if (isLink()) {
			sbuff.append("</u></body></html>");
		}

		label.setText(sbuff.toString());
	}

	/**
	 * Sets the component that is responsible for displaying data for a given
	 * column
	 * 
	 * @param icomp
	 *            the component to set
	 */
	public void setColumnComponent(InstanceComponent icomp) {
		fieldcomponent = icomp;
		fieldcomponent.addPropertyChangeListener(InstanceComponent.MODIFIED_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				m_bmodified = ((Boolean) evt.getNewValue()).booleanValue();
				updateLabel();
			}
		});

	}

}
