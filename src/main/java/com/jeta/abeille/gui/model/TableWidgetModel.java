package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

/**
 * This is the data model for the table widget
 * 
 * @author Jeff Tassin
 */
public class TableWidgetModel implements JETAExternalizable {
	static final long serialVersionUID = 1410292937642519733L;

	public static int VERSION = 1;

	/**
	 * This underlying table id. We get the metadata from the modeler. The
	 * modeler also keeps track if the table is saved or not
	 */
	private TableId m_tableid;

	/**
	 * This is the main model instance that stores any metadata that we are
	 * modeling
	 */
	private transient ModelerModel m_modeler;

	/**
	 * Flag that indicates if this widget is invalid.
	 */
	private transient boolean m_invalid = false;

	/**
	 * Properties a caller can specify that are specific to that caller's
	 * requirements.
	 */
	private HashMap m_properties = new HashMap();

	/** allows a caller to override the foreground and background colors */
	private transient Color m_foreground;
	private transient Color m_background;

	private transient Color m_gradient_color;
	private transient Paint m_gradient;

	private static Color m_table_background = ModelViewSettings.getDefaultColor(ModelViewSettings.ID_TABLE_BACKGROUND);
	private static Color m_table_foreground = ModelViewSettings.getDefaultColor(ModelViewSettings.ID_TABLE_FOREGROUND);
	private static Color m_prototype_background = ModelViewSettings
			.getDefaultColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND);
	private static Color m_prototype_foreground = ModelViewSettings
			.getDefaultColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND);
	private static boolean m_paint_gradient = true;

	/**
	 * For serialization only
	 */
	public TableWidgetModel() {

	}

	/**
	 * ctor
	 */
	public TableWidgetModel(ModelerModel modeler, TableId id) {
		m_tableid = id;
		m_modeler = modeler;
		assert (modeler != null);
	}

	/**
	 * @return the color of the caption bar and bottom bar
	 */
	public Color getBackground() {
		// draw caption rect
		// the caption color changes depending on whether the table exists
		Color captioncolor = getTableBackground();
		if (isLoaded()) {
			if (m_background != null) {
				captioncolor = m_background;
			} else {
				if (!isValid()) {
					captioncolor = TableWidget.INVALID_TABLE_COLOR;
				} else if (!isSaved()) {
					captioncolor = getPrototypeBackground();
				}
			}
		} else {
			captioncolor = Color.gray;
		}
		return captioncolor;
	}

	/**
	 * @return the catalog of the table that is represented by this widget
	 */
	public Catalog getCatalog() {
		return getTableId().getCatalog();
	}

	/**
	 * @return the number of columns in the table represented by this widget
	 */
	public int getColumnCount() {
		TableMetaData tmd = getMetaData();
		if (tmd != null) {
			return tmd.getColumnCount();
		} else
			return 0;
	}

	/**
	 * @return the set of columns (ColumnMetaData objects) in the table that
	 *         this widget represents
	 */
	public Collection getColumns() {
		TableMetaData tmd = getMetaData();
		if (tmd != null) {
			return tmd.getColumns();
		} else {
			if (TSUtils.isDebug()) {
				TSUtils.printMessage("TableWidgetModel.getColumns encounted null metadata for : "
						+ m_tableid.getFullyQualifiedName());
			}
			return com.jeta.foundation.utils.EmptyCollection.getInstance();
		}
	}

	/**
	 * @return the underlying database connection that this widget belongs to
	 */
	public TSConnection getConnection() {
		return getModeler().getConnection();
	}

	/**
	 * @param height
	 *            the height of the caption bar
	 * @return the gradient paint used for painting the caption bar
	 */
	public Paint getGradient(int height) {
		Color color = getBackground();
		if (m_gradient == null || m_gradient_color == null || !m_gradient_color.equals(color)) {
			m_gradient_color = color;
			m_gradient = new GradientPaint(0, 0, color.brighter(), 0, height, color);
		}
		return m_gradient;
	}

	public static int getFontSize() {
		int fontsize = 10;
		try {
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			String fsize = userprops.getProperty(ModelViewNames.ID_TABLE_WIDGET_FONT_SIZE, "10");
			fontsize = Integer.parseInt(fsize);
		} catch (Exception e) {
			TSUtils.printException(e);
		}
		return fontsize;
	}

	/**
	 * @return the text color
	 */
	public Color getForeground() {
		Color textcolor = Color.black;

		if (isLoaded()) {
			if (m_foreground == null) {
				if (isSaved())
					textcolor = getTableForeground();
				else
					textcolor = getPrototypeForeground();
			} else {
				textcolor = m_foreground;
			}
		} else {
			textcolor = Color.darkGray;
		}
		return textcolor;
	}

	public static Color getPrototypeBackground() {
		return m_prototype_background;
	}

	public static Color getPrototypeForeground() {
		return m_prototype_foreground;
	}

	public static Color getTableBackground() {
		return m_table_background;
	}

	public static Color getTableForeground() {
		return m_table_foreground;
	}

	/**
	 * @return the data model used by the modeling system
	 */
	public ModelerModel getModeler() {
		return m_modeler;
	}

	/**
	 * @return the underlying table metadata for this widget
	 */
	public TableMetaData getMetaData() {
		if (m_invalid) {
			// if we are here, then we attempted to load the table meta data in
			// a previous
			// call and were unable to find the table in the DbModel. So, we
			// assume the
			// table does not exist. The user must reload the tables/model in
			// order
			// to clear this flag
			return null;
		} else {
			/**
			 * when a table is first loaded by the system as startup, only the
			 * table columns are loaded and not the foreign keys. This is done
			 * to speed up loading. So, we need to make sure the foreign keys
			 * are loaded by calling getTableEx
			 */
			ModelerModel modeler = getModeler();
			if (modeler.isPrototype(m_tableid)) {
				TableMetaData tmd = modeler.getPrototype(m_tableid);
				if (tmd == null) {
					m_invalid = true;
					assert (false);
				}
				return tmd;
			} else {
				DbModel dbmodel = modeler.getDbModel(m_tableid.getCatalog());
				TableMetaData tmd = modeler.getTableEx(m_tableid, TableMetaData.LOAD_FOREIGN_KEYS
						| TableMetaData.LOAD_COLUMNS_EX);
				if (tmd == null) {
					m_invalid = true;
				}
				return tmd;
			}
		}

	}

	/**
	 * @returns a property
	 */
	public Object getProperty(String propName) {
		return m_properties.get(propName);
	}

	/**
	 * @return the table id for this table
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * @return the name of the table that is represented by this widget
	 */
	public String getTableName() {
		return getTableId().getTableName();
	}

	/**
	 * @return the value of the database saved flag. This indicates whether we
	 *         have saved this table to the database or not
	 */
	public boolean isSaved() {
		return !getModeler().isPrototype(m_tableid);
	}

	/**
	 * @return true if the widget is not a prototype and cannot be found in the
	 *         database
	 */
	public boolean isValid() {
		return !m_invalid;
	}

	/**
	 * @return true if the widget is not a prototype and the underlying database
	 *         model has not been/finished loaded
	 */
	public boolean isLoaded() {
		return true;
	}

	/**
	 * Flag that indicates if the caption color should be painted as a gradient
	 */
	public boolean isPaintGradient() {
		return m_paint_gradient;
	}

	/**
	 * Override serialize so we can initialize properly
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		m_invalid = false;
	}

	public void setBackground(Color color) {
		m_background = color;
	}

	public void setForeground(Color color) {
		m_foreground = color;
	}

	public void setModeler(ModelerModel modeler) {
		assert (modeler != null);
		m_modeler = modeler;
	}

	/**
	 * Sets a property
	 */
	public void setProperty(String propName, java.io.Serializable obj) {
		m_properties.put(propName, obj);
	}

	public static void setPrototypeBackground(Color color) {
		if (color != null)
			m_prototype_background = color;
	}

	public static void setPrototypeForeground(Color color) {
		if (color != null)
			m_prototype_foreground = color;
	}

	public static void setTableBackground(Color color) {
		if (color != null)
			m_table_background = color;
	}

	public static void setTableForeground(Color color) {
		if (color != null)
			m_table_foreground = color;
	}

	public void updateSettings() {

		setPrototypeBackground(TSUserPropertiesUtils.getColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND,
				ModelViewSettings.getDefaultColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND)));

		setPrototypeForeground(TSUserPropertiesUtils.getColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND,
				ModelViewSettings.getDefaultColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND)));

		setTableBackground(TSUserPropertiesUtils.getColor(ModelViewSettings.ID_TABLE_BACKGROUND,
				ModelViewSettings.getDefaultColor(ModelViewSettings.ID_TABLE_BACKGROUND)));

		setTableForeground(TSUserPropertiesUtils.getColor(ModelViewSettings.ID_TABLE_FOREGROUND,
				ModelViewSettings.getDefaultColor(ModelViewSettings.ID_TABLE_FOREGROUND)));

		m_paint_gradient = TSUserPropertiesUtils.getBoolean(ModelViewSettings.ID_TABLE_PAINT_GRADIENT, true);
		m_gradient = null;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_tableid = (TableId) in.readObject();
		m_properties = (HashMap) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);

		out.writeObject(m_tableid);
		out.writeObject(m_properties);
	}

}
