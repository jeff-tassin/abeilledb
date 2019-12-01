package com.jeta.abeille.gui.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.common.JETAExternalizable;

public class ModelWorkBook implements JETAExternalizable {
	static final long serialVersionUID = -7212796876040392673L;

	public static final int VERSION = 1;

	/**
	 * The modeler that contains prototype tables
	 */
	private ModelerModel m_modeler;

	private ArrayList m_viewmodels = new ArrayList();

	/**
	 * The date which this workbook as created. This is used to time out
	 * evaluation versions.
	 */
	private Calendar m_created_date;

	public ModelWorkBook() {

	}

	public ModelWorkBook(ModelerModel modeler, ModelViewModel model) {
		m_modeler = modeler;
		m_viewmodels.add(model);
	}

	public ModelWorkBook(ModelerModel modeler, Collection viewmodels) {
		m_modeler = modeler;
		m_viewmodels.addAll(viewmodels);
	}

	public Calendar getCreatedDate() {
		if (m_modeler == null)
			return Calendar.getInstance();
		else
			return m_modeler.getCreatedDate();
	}

	public ModelerModel getModeler() {
		return m_modeler;
	}

	public Collection getViewModels() {
		return m_viewmodels;
	}

	/**
	 * You must initialize after de-serialization
	 */
	public void initialize(TSConnection tsconn) {
		m_modeler.setConnection(tsconn);
		Iterator iter = m_viewmodels.iterator();
		while (iter.hasNext()) {
			ModelViewModel mvm = (ModelViewModel) iter.next();
			mvm.initialize(m_modeler);
		}
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		m_modeler = (ModelerModel) in.readObject();
		m_viewmodels = (ArrayList) in.readObject();
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_modeler);
		out.writeObject(m_viewmodels);
	}

}
