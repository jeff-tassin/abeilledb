package com.jeta.abeille.gui.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Used to store the current frame state
 * 
 */
public class ModelViewFrameState implements JETAExternalizable {
	static final long serialVersionUID = 6695764349777917084L;

	public static int VERSION = 1;

	public static final String COMPONENT_ID = "ModelViewFrameState";

	private boolean m_showmodeler;
	private int m_dividerloc;

	/**
	 * ctor only for serialization
	 */
	public ModelViewFrameState() {

	}

	/**
	 * ctor
	 */
	public ModelViewFrameState(boolean bshow, int divloc) {
		m_showmodeler = bshow;
		m_dividerloc = divloc;
	}

	public boolean isModelerVisible() {
		return m_showmodeler;
	}

	public int getDividerLocation() {
		return m_dividerloc;
	}

	public void setDividerLocation(int divloc) {
		m_dividerloc = divloc;
	}

	public void setModelerVisible(boolean bshow) {
		m_showmodeler = bshow;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_showmodeler = in.readBoolean();
		m_dividerloc = in.readInt();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeBoolean(m_showmodeler);
		out.writeInt(m_dividerloc);
	}

}
