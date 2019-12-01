package com.jeta.abeille.gui.update;

import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.components.TSComponentNames;

/**
 * Specialization of JTextField that we need for this component
 * 
 * @author Jeff Tassin
 */
public class InstanceTextField extends JTextField implements InstanceTextAdapter {
	private InstanceComponent m_instancecomp;

	public InstanceTextField(InstanceComponent comp) {
		m_instancecomp = comp;
	}

	/**
	 * @return the underlying text component
	 */
	public JTextComponent getTextComponent() {
		return this;
	}

	/**
	 * Intercept commands to set field to null
	 */
	public void processKeyEvent(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_DELETE && evt.isControlDown()) {
			m_instancecomp.setModified(true);
			m_instancecomp.setValue(null);
			/**
			 * you need this to force a repaint of the overlay window in the
			 * instance view. If the editor has a zero-length string and you hit
			 * ctrl-delete, the view will not be repainted
			 */
			m_instancecomp.getComponent().repaint();
		} else
			super.processKeyEvent(evt);
	}

}
