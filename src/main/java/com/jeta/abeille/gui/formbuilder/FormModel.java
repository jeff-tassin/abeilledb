package com.jeta.abeille.gui.formbuilder;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.model.ModelViewModel;
import com.jeta.abeille.gui.model.ModelViewModelEvent;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.ModelerGetter;
import com.jeta.abeille.gui.update.ColumnSettings;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * This is the model for the form builder
 * 
 * @author Jeff Tassin
 */
public class FormModel extends ModelViewModel implements JETAExternalizable {
	static final long serialVersionUID = 7361539102679877589L;

	public static int VERSION = 1;

	/** The object id (not necessarily unique) for this form */
	private DbObjectId m_id;

	/** The unique identifier for this form */
	private String m_uid;

	/** the is the table this is the basis for the form */
	private TableId m_anchortable;

	/** the columns model */
	private FormInstanceMetaData m_metadata = new FormInstanceMetaData();

	private static final String FORM_MODEL = "jeta.abeille.gui.formbuilder.formmodel";

	/** event for this model when the name of the model changes */
	public static final int MODEL_NAME_CHANGED = 100;

	/**
	 * default ctor for serialization
	 */
	public FormModel() {

	}

	/**
	 * Standard constructor
	 * 
	 * @param connection
	 *            this is the database connection. We need this to load the
	 *            metadata and links for tables we are working with
	 * @param name
	 *            this is the name for this form.
	 * @param uid
	 *            this is a unique identifier for this form
	 */
	private FormModel(ModelerGetter modeler_getter, TSConnection tsconn) {
		super("form.builder", new ModelerModel(tsconn), tsconn);
	}

	/**
	 * @return the table that is the basis for the form
	 */
	public TableId getAnchorTable() {
		return m_anchortable;
	}

	/**
	 * The catalog that owns this form
	 */
	public Catalog getCatalog() {
		if (m_id == null) {
			assert (false);
			return null;
		} else {
			return m_id.getCatalog();
		}
	}

	/**
	 * @return the number of columns defined for the form
	 */
	public int getColumnCount() {
		return m_metadata.getColumnCount();
	}

	/**
	 * @return the metadata that makes up the columns of the form
	 */
	public FormInstanceMetaData getInstanceMetaData() {
		return m_metadata;
	}

	/**
	 * @return the user defined name for this form
	 */
	public String getName() {
		return m_id.getObjectName();
	}

	/**
	 * The schema that owns this form
	 */
	public Schema getSchema() {
		if (m_id == null) {
			assert (false);
			return null;
		} else {
			return m_id.getSchema();
		}
	}

	/**
	 * @return the key used to identify this model in the object store
	 */
	public String getStoreKey() {
		return getStoreKey(m_uid);
	}

	static String getStoreKey(String uid) {
		return FORM_MODEL + "." + uid;
	}

	/**
	 * @return the uid for this form
	 */
	public String getUID() {
		return m_uid;
	}

	/**
	 * Creates an instance of a form model. If the model is found in the data
	 * store it is loaded. If the model is not found, a new instance is created.
	 * The tag is used to uniquely indentify the model
	 * 
	 * @param connection
	 *            the database connection
	 * @param tag
	 *            the form identifier
	 */
	public static FormModel loadInstance(TSConnection connection, String uid, DbObjectId objid) {
		/*
		 * assert( uid != null ); assert( connection != null ); assert( objid !=
		 * null );
		 * 
		 * ObjectStore os = connection.getObjectStore(); String storename =
		 * getStoreKey( uid ); FormModel model = null;
		 * 
		 * try { model = (FormModel)os.load( storename ); } catch(
		 * java.io.IOException ioe ) { ioe.printStackTrace(); }
		 * 
		 * if ( model == null ) { model = new FormModel( connection, uid ); }
		 * else { assert( uid.equals(model.m_uid) );
		 * 
		 * if ( model.getConnection() == null ) model.setConnection( connection
		 * ); } model.setId( objid ); model.initialize(); return model;
		 */
		return null;
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire this class
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		int version = in.readInt();
		m_id = (DbObjectId) in.readObject();
		m_uid = (String) in.readObject();
		ColumnSettings[] settings = (ColumnSettings[]) in.readObject();
		m_anchortable = (TableId) in.readObject();

		m_metadata = new FormInstanceMetaData(settings, m_anchortable);
	}

	/**
	 * Saves this form
	 */
	public void save() {
		ObjectStore os = getConnection().getObjectStore();
		String storename = getStoreKey();
		try {
			os.store(storename, this);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Set the table that is the basis for the form
	 */
	public void setAnchorTable(TableId tableid) {
		m_anchortable = tableid;
		m_metadata.setAnchorTable(tableid);

	}

	/**
	 * Sets this form's id
	 * 
	 * @param objid
	 *            the id (schema.name) of the form to set
	 */
	public void setId(DbObjectId objid) {
		m_id = objid;
	}

	/**
	 * Sets the catalog and schema for this proxy
	 */
	public void set(Catalog catalog, Schema schema) {
		m_id = m_id.change(catalog, schema);
		fireEvent(new ModelViewModelEvent(FormModel.MODEL_NAME_CHANGED, getName()));
	}

	/**
	 * Sets this form's name
	 * 
	 * @param name
	 *            the name of the form to set
	 */
	public void setName(String name) {
		m_id = m_id.changeName(name);
		fireEvent(new ModelViewModelEvent(FormModel.MODEL_NAME_CHANGED, name));
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_id);
		out.writeObject(m_uid);
		out.writeObject(m_metadata.getColumnSettings());
		out.writeObject(m_anchortable);
	}
}
