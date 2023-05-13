package com.jeta.abeille.gui.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

public class NodeSerializer implements JETAExternalizable {
	static final long serialVersionUID = 7772325501147282445L;

	public static int VERSION = 1;

	private Object m_userobj;

	private String m_uid;

	private String m_parentuid;

	/** the associated node in the JTree */
	private transient ObjectTreeNode m_treenode;

	public NodeSerializer() {
	}

	/**
	 * ctor
	 * 
	 * @param userObject
	 *            the user object that we are managing in the tree
	 * @param uid
	 *            a unique indentifier for the object
	 */
	public NodeSerializer(Object userObject, String uid) {
		m_userobj = userObject;
		m_uid = uid;
	}

	/**
	 * @return the parent uid for this object
	 */
	public String getParentUID() {
		return m_parentuid;
	}

	public ObjectTreeNode getTreeNode() {
		return m_treenode;
	}

	public String getUID() {
		return m_uid;
	}

	public Object getUserObject() {
		return m_userobj;
	}

	/**
	 * Sets the parent uid for this object
	 */
	public void setParentUID(String parentUID) {
		m_parentuid = parentUID;
	}

	public void setTreeNode(ObjectTreeNode treeNode) {
		m_treenode = treeNode;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_userobj = in.readObject();
		m_uid = (String) in.readObject();
		m_parentuid = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_userobj);
		out.writeObject(m_uid);
		out.writeObject(m_parentuid);
	}

}
