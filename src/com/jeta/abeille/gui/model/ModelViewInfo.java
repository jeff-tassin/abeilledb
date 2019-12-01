package com.jeta.abeille.gui.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class is used to store the list of ModelViews that are present in the
 * ModelViewFrame. The frame loads the list at startup. As each view is
 * accessed, the model is is retrieved from this class. If the view has not been
 * loaded, the model is loaded at the time of access. This improves startup
 * performance because loading all views at once can take time.
 * 
 * @author Jeff Tassin
 */
public class ModelViewInfo implements JETAExternalizable {
	static final long serialVersionUID = 3769398427995792166L;

	public static int VERSION = 1;

	/** the id of the model */
	private String m_model_uid;

	/** the name of the view */
	private String m_view_name;

	/**
	 * ctor only for serialization
	 */
	public ModelViewInfo() {

	}

	public ModelViewInfo(String uid, String viewName) {
		assert (uid != null);
		m_model_uid = uid;
		m_view_name = viewName;
	}

	public String getModelId() {
		return m_model_uid;
	}

	public String getViewName() {
		return m_view_name;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_model_uid = (String) in.readObject();
		m_view_name = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_model_uid);
		out.writeObject(m_view_name);
	}

}
