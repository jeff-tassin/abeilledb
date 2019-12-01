package com.jeta.abeille.gui.sql.input;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents the constraints/parameters that will be used to build
 * the SQLInputView
 * 
 * @author Jeff Tassin
 */
public class SQLInputModel {
	/** a collection of InputField objects */
	private ArrayList m_data;

	/**
	 * ctor
	 */
	public SQLInputModel() {

	}

	/**
	 * Adds an input with the given label
	 */
	public void addInput(SQLInput input) {
		if (m_data == null)
			m_data = new ArrayList();

		m_data.add(new InputField(input));
	}

	/**
	 * @return the InputField at the given (zero-based) index
	 */
	public InputField getInput(int index) {
		if (m_data == null)
			return null;
		else {
			return (InputField) m_data.get(index);
		}
	}

	/**
	 * @return the number if input fields in this model
	 */
	public int getInputCount() {
		if (m_data == null)
			return 0;
		else
			return m_data.size();
	}

	/**
	 * Saves the fields to to input objects
	 */
	public void save() {
		Iterator iter = m_data.iterator();
		while (iter.hasNext()) {
			InputField field = (InputField) iter.next();
			field.save();
		}
	}
}
