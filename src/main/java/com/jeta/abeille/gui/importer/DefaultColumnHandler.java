package com.jeta.abeille.gui.importer;

public class DefaultColumnHandler implements ColumnHandler {
	private String m_value;

	public DefaultColumnHandler(String value) {
		m_value = value;
	}

	public String getOutput(int row) {
		return m_value;
	}
}
