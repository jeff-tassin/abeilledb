package com.jeta.foundation.gui.table.export;

import java.io.Writer;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.table.TableModel;

/**
 * This class is simple a composite of decorators
 * 
 * @author Jeff Tassin
 */
public class DecoratorComposite implements ExportDecorator {
	private LinkedList m_decorators = new LinkedList();

	/**
	 * ctor
	 */
	public DecoratorComposite() {

	}

	/**
	 * Adds a decorator to this composite
	 */
	public void add(ExportDecorator decorator) {
		m_decorators.add(decorator);
	}

	/**
	 * @return the set of decorators that make up this composite
	 */
	public Collection getDecorators() {
		return m_decorators;
	}

	/**
	 * ExportDecorator implementation Forward the write call to all decorators
	 * in this composite
	 */
	public void write(Writer writer, TableModel tableModel, int row, int col) throws IOException {
		Iterator iter = m_decorators.iterator();
		while (iter.hasNext()) {
			ExportDecorator decorator = (ExportDecorator) iter.next();
			decorator.write(writer, tableModel, row, col);
		}
	}
}
