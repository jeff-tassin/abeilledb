package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import javax.swing.table.TableModel;

/**
 * This decorator simply writes out a value
 * 
 * 
 * @author Jeff Tassin
 */
public class LiteralDecorator implements ExportDecorator {
	/** the value to write */
	private String m_literal;

	/**
	 * ctor
	 */
	public LiteralDecorator(String value) {
		m_literal = value;
	}

	/**
	 * ExportDecorator implementation. Simply write out the literal to the
	 * writer
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		if (m_literal != null && m_literal.length() > 0)
			writer.write(m_literal);
	}

}
