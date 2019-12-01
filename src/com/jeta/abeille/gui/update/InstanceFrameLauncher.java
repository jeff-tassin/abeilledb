package com.jeta.abeille.gui.update;

import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.database.model.ColumnMetaData;

/**
 * This class is used to define an object that launched an InstanceFrame. We use
 * this primarily to support pasting data from one frame to another. This class
 * defines how columns from the launcher frame map to the launched frame. For
 * single tables, the mapping is simply the foreign key to primary key
 * relationship. However, for the form builder, the mapping is whatever
 * 
 * @author Jeff Tassin
 */
public class InstanceFrameLauncher {
	/**
	 * the object that launched this frame. If the launcher is another
	 * InstanceFrame, then well allow the user to paste data from one frame to
	 * another
	 */
	private WeakReference m_source = null;

	/**
	 * The class of object that launched this frame
	 */
	private Class m_sourceclass;

	/**
	 * a map of source columns (ColumnMetaData objects - key) to launched view
	 * columns (ColumnMetaData objects - values)
	 */
	private HashMap m_colmap;

	/**
	 * ctor
	 */
	public InstanceFrameLauncher(Object source) {
		m_source = new WeakReference(source);
		if (source != null)
			m_sourceclass = source.getClass();
	}

	/**
	 * ctor
	 * 
	 * @param source
	 *            the object that is launching the instance frame
	 * @param srccols
	 *            an array of columns in the source object (if the source has an
	 *            InstanceView) that are mapped to columns in the launched view
	 * @param destcols
	 *            an array of columns in the launched view that are mapped back
	 *            to the source view Note that destcols.length must equal
	 *            srccols.length because this is a one-to-one mapping
	 */
	public InstanceFrameLauncher(Object source, ColumnMetaData[] srccols, ColumnMetaData[] destcols) {
		this(source);
		m_colmap = new HashMap();

		if (srccols != null && destcols != null && srccols.length == destcols.length) {
			for (int index = 0; index < srccols.length; index++) {
				m_colmap.put(srccols[index], destcols[index]);
			}
		}
	}

	/**
	 * ctor
	 * 
	 * @param source
	 *            the object that is launching the instance frame
	 * @param srccols
	 *            a collection of columns (ColumnMetaData objects) in the source
	 *            object (if the source has an InstanceView) that are mapped to
	 *            columns in the launched view
	 * @param destcols
	 *            a collection of columns (ColumnMetaData objects) in the
	 *            launched view that are mapped back to the source view Note
	 *            that destcols.size must equal srccols.size because this is a
	 *            one-to-one mapping
	 */
	public InstanceFrameLauncher(Object source, Collection srccols, Collection destcols) {
		this(source);
		m_colmap = new HashMap();

		if (srccols != null && destcols != null && srccols.size() == destcols.size()) {
			Iterator siter = srccols.iterator();
			Iterator diter = destcols.iterator();
			while (siter.hasNext()) {
				ColumnMetaData srccol = (ColumnMetaData) siter.next();
				ColumnMetaData destcol = (ColumnMetaData) diter.next();
				m_colmap.put(srccol, destcol);
			}
		}
	}

	/**
	 * @return the column in the launched view that is mapped to the source view
	 *         column Null is returned if the mapping is not found.
	 */
	public ColumnMetaData getMappedColumn(ColumnMetaData sourceCol) {
		return (ColumnMetaData) m_colmap.get(sourceCol);
	}

	/**
	 * @return the source object that is responsible for launching the frame
	 */
	public Object getSource() {
		if (m_source == null)
			return null;
		else
			return m_source.get();
	}

	/**
	 * @return the class of the object that launched this frame
	 */
	public Class getSourceClass() {
		return m_sourceclass;
	}

	/**
	 * @return the list of linked columns from the source instance frame
	 */
	public Collection getSourceColumns() {
		return m_colmap.keySet();
	}

}
