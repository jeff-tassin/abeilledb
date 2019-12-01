package com.jeta.abeille.gui.store;

import java.io.IOException;
import javax.swing.KeyStroke;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * A serializable KeyStroke
 */
public class KeyStrokeInfo implements JETAExternalizable {
	static final long serialVersionUID = 4028067227410688865L;

	public static int VERSION = 1;

	private int m_key_code;
	private int m_modifiers;

	public KeyStrokeInfo() {

	}

	public KeyStrokeInfo(KeyStroke keystroke) {
		setKeyStroke(keystroke);
	}

	public void setKeyStroke(KeyStroke keystroke) {
		m_key_code = keystroke.getKeyCode();
		m_modifiers = keystroke.getModifiers();
	}

	public KeyStroke getKeyStroke() {
		return KeyStroke.getKeyStroke(m_key_code, m_modifiers);
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_key_code = in.readInt();
		m_modifiers = in.readInt();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeInt(m_key_code);
		out.writeInt(m_modifiers);
	}

}
