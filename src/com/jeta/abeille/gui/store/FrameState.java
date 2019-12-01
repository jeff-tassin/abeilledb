package com.jeta.abeille.gui.store;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.awt.Rectangle;

import javax.swing.JFrame;

import com.jeta.foundation.gui.components.TSInternalFrame;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Class that represents the frame state (child window positions/sizes) for the
 * main frame window.
 */
public class FrameState implements JETAExternalizable {
	static final long serialVersionUID = 566155687616257380L;

	public static int VERSION = 1;

	private Rectangle m_bounds;
	private boolean m_visible;
	private boolean m_internal;

	public FrameState() {

	}

	/**
	 * ctor
	 */
	public FrameState(TSInternalFrame iframe) {
		m_visible = iframe.isVisible();
		m_internal = true;
		if (iframe.getDelegate() instanceof JFrame) {
			m_internal = false;
		}
		m_bounds = new Rectangle(iframe.getBounds());
	}

	/**
	 * ctor
	 */
	public FrameState(JFrame frame) {
		m_bounds = new Rectangle(frame.getBounds());
	}

	/**
	 * @return the frame bounds for the given frame
	 */
	public Rectangle getBounds() {
		return m_bounds;
	}

	public boolean isInternal() {
		return m_internal;
	}

	public boolean isVisible() {
		return m_visible;
	}

	public void setInternal(boolean internal) {
		m_internal = internal;
	}

	public void setVisible(boolean bvis) {
		m_visible = bvis;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_bounds = (Rectangle) in.readObject();
		m_visible = in.readBoolean();
		m_internal = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_bounds);
		out.writeBoolean(m_visible);
		out.writeBoolean(m_internal);
	}

}
