package com.jeta.abeille.gui.query;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.model.ModelerGetter;
import com.jeta.abeille.gui.model.ModelerModel;

import com.jeta.foundation.common.JETAExternalizable;

public class QueryBuilderFile implements JETAExternalizable {
	static final long serialVersionUID = 4362001549559040382L;

	public static int VERSION = 1;

	private ModelerModel m_modeler;
	private QueryModel m_query_model;

	public QueryBuilderFile() {

	}

	public QueryBuilderFile(QueryModel queryModel) {
		m_query_model = queryModel;
		m_modeler = queryModel.getModeler();
	}

	public ModelerModel getModeler() {
		return m_modeler;
	}

	public QueryModel getQueryModel() {
		return m_query_model;
	}

	/**
	 * This should be called after de-serialization
	 */
	public void initialize(TSConnection tsconn) {
		m_modeler.setConnection(tsconn);
		m_query_model.initialize(m_modeler);
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		m_modeler = (ModelerModel) in.readObject();
		m_query_model = (QueryModel) in.readObject();
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_modeler);
		out.writeObject(m_query_model);
	}

}
