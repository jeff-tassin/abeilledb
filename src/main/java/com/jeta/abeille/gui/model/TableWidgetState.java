package com.jeta.abeille.gui.model;

import java.awt.Rectangle;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * We need this object to store TableWdigets because we don't want to store
 * JComponents (which TableWidget extends) in a file due to differences between
 * serialVersionUIDS from VM to VM. For example, IBM's VM has different
 * serialVersionUIDs for JComponent than Sun's VM. So, you can't read in a
 * serialized TableWidget that is stored with Sun VM into Abeille that is
 * running in IBM's VM.
 * 
 * @author Jeff Tassin
 */
public class TableWidgetState implements JETAExternalizable {
	static final long serialVersionUID = 836667340773711706L;

	public static int VERSION = 1;

	private TableWidgetModel m_model;
	private Rectangle m_bounds;

	public TableWidgetState() {

	}

	public TableWidgetState(TableWidget tw) {
		m_model = tw.getModel();
		m_bounds = tw.getBounds();
	}

	public TableWidgetModel getModel() {
		return m_model;
	}

	public Rectangle getBounds() {
		return m_bounds;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_model = (TableWidgetModel) in.readObject();
		m_bounds = (Rectangle) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_model);
		out.writeObject(m_bounds);
	}

}
