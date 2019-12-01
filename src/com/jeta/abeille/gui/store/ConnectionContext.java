package com.jeta.abeille.gui.store;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSInternalFrame;

/*
 * Class that has all internal frame windows for a given connection.
 * Used by the main frame window
 * @author Jeff Tassin
 */
public class ConnectionContext {
	static final long serialVersionUID = -8458958688785501919L;

	private transient TSConnection m_connection;
	private transient LinkedList m_frames = new LinkedList();

	private transient HashMap m_frame_classes = new HashMap();
	private transient TSInternalFrame m_last_frame;

	public static final String COMPONENT_ID = "connection.ConnectionContext";

	/**
	 * ctor
	 */
	public ConnectionContext(TSConnection conn) {
		m_connection = conn;
	}

	public void addFrame(TSInternalFrame iframe) {
		m_frames.add(iframe);
	}

	public void clearFrames() {
		m_frames.clear();
	}

	/**
	 * @return the database connection for this context
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the singleton frame for a given frame class
	 */
	public Collection getFrames() {
		return m_frames;
	}

	public TSInternalFrame getFrame(Class c) {
		return (TSInternalFrame) m_frame_classes.get(c);
	}

	/**
	 * Sets the last frame used by this connection
	 */
	public TSInternalFrame getLastFrame() {
		return m_last_frame;
	}

	public void setFrame(Class frameClass, TSInternalFrame frame) {
		m_frame_classes.put(frameClass, frame);
	}

	/**
	 * Sets the last frame used by this connection
	 */
	public void setLastFrame(TSInternalFrame iframe) {
		m_last_frame = iframe;
	}

	/**
	 * Sets the connection for this context
	 */
	public void setConnection(TSConnection connection) {
		m_connection = connection;
	}

}
