package com.jeta.abeille.gui.update;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;

import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import com.jeta.foundation.gui.components.*;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is a base component for the UpdateFrame for any component that uses the
 * JTextComponent. This includes integral, numeric, and text types. It has
 * support for displaying modified, null, invalid, and valid values.
 * 
 * @author Jeff Tassin
 */
public abstract class TextComponentBase extends InstanceComponent implements DocumentListener {
	private boolean m_listendocevents = true; // flag that determines if we
												// should listen for document
												// change events

	/**
	 * the number of characters to display for this component. You don't have to
	 * set this unless you want to explicitly size a component. For example,
	 * integer based components have set sizes
	 */
	private int m_size = 0;

	/**
	 * ctor
	 */
	public TextComponentBase(String fieldName, int dataType) {
		super(fieldName, dataType);
	}

	/**
	 * ctor
	 */
	public TextComponentBase(String fieldName, int dataType, int size) {
		super(fieldName, dataType);
		m_size = size;
	}

	public JTextComponent getTextComponent() {
		InstanceTextAdapter adapter = (InstanceTextAdapter) getComponent();
		if (adapter == null)
			return null;
		else
			return adapter.getTextComponent();
	}

	/**
	 * @return the component that actually renders the data value. Some
	 *         components are contained in a scroll pane. If that is the case,
	 *         the properly gets the component contained in the scroll
	 */
	public JComponent getVisualComponent() {
		return getTextComponent();
	}

	/**
	 * DocumentListener event
	 */
	public void changedUpdate(DocumentEvent e) {
		setModified(true);
		InstanceTextAdapter txtadapter = (InstanceTextAdapter) getComponent();
		super.setValue(txtadapter.getTextComponent().getText());
	}

	/**
	 * @return is the preferred with of the text field that displays the numeric
	 *         value. This should is only used if the size value is set
	 */
	public int getPreferredFieldWidth() {
		JTextField txtfield = (JTextField) getComponent();
		int width = TSGuiToolbox.calculateAverageTextWidth(txtfield, m_size);
		return width;
	}

	/**
	 * @return the preferred size for this component
	 */
	public Dimension getPreferredSize() {
		if (m_size > 0) {
			Dimension d = getComponent().getPreferredSize();
			d.width = getPreferredFieldWidth() + getComponentX() + 2;
			return d;
		} else {
			return super.getPreferredSize();
		}
	}

	/**
	 * DocumentListener event
	 */
	public void insertUpdate(DocumentEvent e) {
		setModified(true);
		InstanceTextAdapter txtadapter = (InstanceTextAdapter) getComponent();
		super.setValue(txtadapter.getTextComponent().getText());
	}

	/**
	 * DocumentListener event
	 */
	public void removeUpdate(DocumentEvent e) {
		setModified(true);

		InstanceTextAdapter txtadapter = (InstanceTextAdapter) getComponent();
		super.setValue(txtadapter.getTextComponent().getText());
	}

	/**
	 * Override of InstanceComponent so we can get the text field and add a
	 * document listener
	 */
	public void setComponent(JComponent comp) {
		super.setComponent(comp);
		if (m_listendocevents) {
			JTextComponent txtcomp = getTextComponent();
			txtcomp.getDocument().addDocumentListener(this);
		}
	}

	/**
	 * This allows us to turn off the document listener so we can set the text
	 * in the text field without the component's setValue being called
	 */
	public void setListenDocumentEvents(boolean bListen) {
		m_listendocevents = bListen;
		if (!m_listendocevents) {
			JTextComponent txtcomp = getTextComponent();
			if (txtcomp != null)
				txtcomp.getDocument().removeDocumentListener(this);
		}
	}

	/**
	 * Sets the value displayed by this field
	 */
	public void setValue(Object value) {
		super.setValue(value);
		JTextComponent comp = getTextComponent();
		Document doc = comp.getDocument();
		try {
			// we don't want document changed events here because we may be
			// calling
			// this method from inside the document listener event
			doc.removeDocumentListener(this);
			if (value == null)
				comp.setText("");
			else
				comp.setText(value.toString());
			comp.setCaretPosition(0);
		} finally {
			if (m_listendocevents)
				doc.addDocumentListener(this);
		}
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public void syncValue() {
		String sval = getTextComponent().getText();
		if (isNull() && sval.length() == 0)
			setValue(null);
		else
			setValue(toString());
	}

	/**
	 * @return the contents of the text control
	 */
	public String toString() {
		return getTextComponent().getText();
	}

}
