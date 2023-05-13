package com.jeta.abeille.gui.model;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.io.StringWriter;

import java.util.Iterator;
import java.util.ArrayList;

import com.jeta.foundation.gui.table.BasicSelection;
import com.jeta.foundation.gui.table.export.DecoratorBuilder;
import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.table.export.ExportModel;
import com.jeta.foundation.gui.table.export.StandardExportBuilder;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a heterogeneous set of objects that the user is
 * dragging or copying.
 * 
 * @author Jeff Tassin.
 */
public class MultiTransferable implements Transferable {
	/** this is a list of unique flavors being transfered */
	private ArrayList m_flavors = new ArrayList();

	/**
	 * this is the set of TransferObjects (flavor and object) being transfered
	 */
	private ArrayList m_data = new ArrayList();

	/**
	 * This is a message a component can set for itself when dragging or
	 * copy/paste within the same component
	 */
	private String m_componentmsg;

	private String m_transferuid;

	/**
	 * This is used to calculate a unique id for every transfer object
	 */
	private static int m_count = 0;

	/**
	 * used when the user selects copy special. This affects only String flavors
	 */
	private ExportModel m_exportmodel;

	/**
	 * ctor
	 */
	public MultiTransferable() {
		synchronized (MultiTransferable.class) {
			m_count++;
			addData(DbObjectFlavor.TRANSFER_UID, String.valueOf(m_count));
		}
	}

	/**
	 * Adds the given flavor to the list of flavors being transferred if the
	 * flavor has not already been added. This can be called multiple times with
	 * the same flavor. All calls after the first for the same flavor are
	 * ignored.
	 */
	public void addData(DataFlavor flavor, Object userobj) {
		if (!m_flavors.contains(flavor)) {
			m_flavors.add(flavor);
		}

		if (DbObjectFlavor.TRANSFER_UID.equals(flavor)) {
			m_transferuid = (String) userobj;
			// System.out.println( "got transfer uid: " + userobj );
		} else
			m_data.add(new TransferObject(flavor, userobj));
	}

	/**
	 * @return a message a component set for itself
	 */
	public String getComponentMessage() {
		return m_componentmsg;
	}

	/**
	 * @return the set of object flavors being dragged.
	 */
	public DataFlavor[] getFlavors() {
		return (DataFlavor[]) m_flavors.toArray(new DataFlavor[0]);
	}

	/**
	 * @return the transfer data for the given flavor Note that we can return an
	 *         array as well as a single object
	 */
	public Object getTransferData(DataFlavor flavor) {
		if (flavor.equals(DataFlavor.stringFlavor)) {
			StringBuffer buffer = null;
			BasicSelection basic_selection = null;

			/**
			 * if the export model is not null, then we are using custom
			 * formatting request by the user via the CopySpecial dialog
			 */
			if (m_exportmodel == null)
				buffer = new StringBuffer();
			else
				basic_selection = new BasicSelection();

			// text transfer is a special case for this
			boolean bfirst = true;
			for (int index = 0; index < m_data.size(); index++) {
				TransferObject tobj = (TransferObject) m_data.get(index);
				if (tobj.getDataFlavor().equals(flavor)) {
					if (m_exportmodel == null) {
						if (bfirst)
							bfirst = false;
						else
							buffer.append("\n"); // add a newline as delimiter
													// between text objects

						buffer.append((String) tobj.getObject());
					} else {
						basic_selection.addRow((String) tobj.getObject());
					}
				}
			}

			if (m_exportmodel == null) {
				return buffer.toString();
			} else {
				StringWriter writer = new StringWriter();
				DecoratorBuilder decbuilder = new StandardExportBuilder(-1);
				ExportDecorator decorator = decbuilder.build(m_exportmodel);
				try {
					decorator.write(writer, basic_selection, 0, 0);
					return writer.toString();
				} catch (Exception e) {
					TSUtils.printException(e);
					return "";
				}
			}
		} else if (flavor.equals(DbObjectFlavor.TRANSFER_UID)) {
			return m_transferuid;
		} else {
			ArrayList result = new ArrayList();
			for (int index = 0; index < m_data.size(); index++) {
				TransferObject tobj = (TransferObject) m_data.get(index);
				if (tobj.getDataFlavor().equals(flavor)) {
					result.add(tobj.getObject());
				}
			}
			return result.toArray();
		}
	}

	/**
	 * Transferable implementation.
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return (DataFlavor[]) m_flavors.toArray(new DataFlavor[0]);
	}

	/**
	 * Transferable implemenetation
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return m_flavors.contains(flavor);
	}

	/**
	 * Allows a component to set a message for itself
	 */
	public void setComponentMessage(String msg) {
		m_componentmsg = msg;
	}

	/**
	 * Sets the export model used for formatting the output
	 */
	public void setExportModel(ExportModel exportmodel) {
		m_exportmodel = exportmodel;
	}

	/**
	 * @return the number of items in this transferable
	 */
	public int size() {
		return m_data.size();
	}

	/**
	 * This class is mainly used to associate the flavor with a given user
	 * object that is being transfered
	 */
	class TransferObject {
		private DataFlavor m_flavor;
		private Object m_object;

		public TransferObject(DataFlavor flavor, Object obj) {
			m_flavor = flavor;
			m_object = obj;
		}

		/**
		 * @return the actual object being transfered
		 */
		public Object getObject() {
			return m_object;
		}

		/**
		 * @return the dataflavor of the object being transfered
		 */
		public DataFlavor getDataFlavor() {
			return m_flavor;
		}
	}

}
