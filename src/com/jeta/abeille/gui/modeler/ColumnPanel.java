package com.jeta.abeille.gui.modeler;

import java.awt.BorderLayout;

import java.util.Iterator;
import java.util.Collection;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.DataTypeInfoComparator;
import com.jeta.abeille.database.model.DataTypeInfo;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.MetaDataPopupRenderer;
import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.foundation.gui.components.IntegerTextField;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.components.TextFieldwButtonPanel;

import com.jeta.foundation.gui.layouts.TableLayout;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

import com.jeta.forms.components.panel.FormPanel;

/**
 * This is a popup dialog that allows the user to directly edit the attributes
 * of a column
 * 
 * @author Jeff Tassin
 */
public class ColumnPanel extends TSPanel implements JETARule {
	/** set to true if this panel is for a prototype table */
	private boolean m_prototype;

	private TSConnection m_connection;

	/** if we are editing an existing column */
	private ColumnInfo m_current_col;

	/**
	 * the content for this panel
	 */
	private FormPanel m_view;

	private TSComboBox m_datatype;

	/**
	 * ctor
	 */
	public ColumnPanel(TSConnection connection) {
		this(connection, null, true);
	}

	/**
	 * ctor
	 */
	public ColumnPanel(TSConnection connection, ColumnInfo info) {
		this(connection, info, true);
	}

	/**
	 * ctor
	 */
	public ColumnPanel(TSConnection connection, ColumnInfo info, boolean bprototype) {
		m_connection = connection;
		m_prototype = bprototype;
		initialize(connection);
		m_current_col = info;
		setColumnInfo(info);
	}

	/**
	 * JETARule implementation
	 */
	public RuleResult check(Object[] params) {
		if (getColumnName().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		} else {
			return RuleResult.SUCCESS;
		}
	}

	protected void initializeDataTypes(TSConnection connection) {
		m_datatype = (TSComboBox) m_view.getComponentByName(ColumnNames.ID_DATA_TYPE);
		m_datatype.getEditor().addFocusListener(new EditorFocusListener());
		m_datatype.setValidating(false);
		PopupList list = m_datatype.getPopupList();
		list.setRenderer(MetaDataPopupRenderer.createInstance(m_connection));
		SortedListModel listmodel = new SortedListModel();
		listmodel.setComparator(new DataTypeInfoComparator());

		TSDatabase dbimpl = (TSDatabase) connection.getImplementation(TSDatabase.COMPONENT_ID);
		Collection dtypes = dbimpl.getSupportedTypes();

		Iterator iter = dtypes.iterator();
		while (iter.hasNext()) {
			DataTypeInfo di = (DataTypeInfo) iter.next();
			listmodel.add(di);
		}
		list.setModel(listmodel);
		m_datatype.setSelectedItem(DbUtils.getJDBCTypeName((short) java.sql.Types.VARCHAR));

	}

	/**
	 * @return the field info object specified in this dialog
	 */
	public ColumnInfo getColumnInfo() {

		String tname = getTypeName();
		int datatype = 0;
		DataTypeInfo tinfo = getSelectedDataType();
		if (tinfo != null)
			datatype = tinfo.getType();

		ColumnInfo cmd = new ColumnInfo(getColumnName(), datatype, getTypeName(), getColumnSize(), getScale(),
				isNullable(), isPrimaryKey());

		cmd.setAutoIncrement(isAutoIncrement());
		if (!isAutoIncrement()) {
			cmd.setDefaultValue(getDefaultColumnValue());
		}
		return cmd;
	}

	public String getColumnName() {
		return TSUtils.fastTrim(getText(ColumnNames.ID_COLUMN_NAME));
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the size of the column entered by the user
	 */
	public int getColumnSize() {
		return getInteger(ColumnNames.ID_SIZE, 0);
	}

	/**
	 * @return the default column value expression
	 */
	public String getDefaultColumnValue() {
		return getText(ColumnNames.ID_DEFAULT_VALUE);
	}

	/**
	 * @return the value of the precision text field
	 */
	public int getScale() {
		return getInteger(ColumnNames.ID_SCALE, 0);
	}

	/**
	 * @return the item in the data type combo box
	 */
	public String getTypeName() {
		return m_datatype.getText();
	}

	/**
	 * @return the data type value (JDBC type) of the given data type label. If
	 *         the label is not a known type, zero is returned.
	 */
	public DataTypeInfo getSelectedDataType() {
		String typename = getTypeName();
		TSDatabase dbimpl = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
		return dbimpl.getDataTypeInfo(typename);
	}

	/**
	 * @return true if the auto increment box is selected
	 */
	public boolean isAutoIncrement() {
		return isSelected(ColumnNames.ID_AUTO_INCREMENT);
	}

	/**
	 * @return true if we are adding a new column
	 */
	public boolean isNewColumn() {
		return (m_current_col == null);
	}

	/**
	 * @return true if the primary key checkbox is selected
	 */
	public boolean isPrimaryKey() {
		return isSelected(ColumnNames.ID_PRIMARY_KEY);
	}

	/**
	 * @return true if this panel is for a column in a prototype table
	 */
	public boolean isPrototype() {
		return m_prototype;
	}

	/**
	 * @return true if the Nullable check box is selected
	 */
	public boolean isNullable() {
		return isSelected(ColumnNames.ID_ALLOW_NULLS);
	}

	/**
	 * Creates and initializes the components on this view
	 */
	private void initialize(TSConnection connection) {
		setLayout(new BorderLayout());

		m_view = new FormPanel("com/jeta/abeille/gui/modeler/columnView.jfrm");
		add(m_view, BorderLayout.CENTER);

		initializeDataTypes(connection);

		Database db = connection.getDatabase();
		if (!Database.POSTGRESQL.equals(db) && !Database.MYSQL.equals(db)) {
			enableComponent(ColumnNames.ID_DEFAULT_VALUE, false);
		} else {
			enableComponent(ColumnNames.ID_DEFAULT_VALUE, true);
		}

	}

	/**
	 * Sets the field information for this dialog
	 * 
	 * @param fi
	 *            the ColumnInfo object used to initalize the components in the
	 *            dialog
	 */
	public void setColumnInfo(ColumnInfo fi) {
		if (fi != null) {
			setText(ColumnNames.ID_COLUMN_NAME, fi.getColumnName());
			setSelected(ColumnNames.ID_AUTO_INCREMENT, fi.isAutoIncrement());

			String name = fi.getTypeName();
			m_datatype.setSelectedItem(name);

			if (fi.getColumnSize() > 0)
				setText(ColumnNames.ID_SIZE, String.valueOf(fi.getColumnSize()));
			else
				setText(ColumnNames.ID_SIZE, "");

			if (fi.getScale() > 0)
				setText(ColumnNames.ID_SCALE, String.valueOf(fi.getScale()));
			else
				setText(ColumnNames.ID_SCALE, "");

			setSelected(ColumnNames.ID_PRIMARY_KEY, fi.isPrimaryKey());
			setSelected(ColumnNames.ID_ALLOW_NULLS, fi.isNullable());
			setText(ColumnNames.ID_DEFAULT_VALUE, fi.getDefaultValue());
		}
	}

	/**
	 * Listen for focus events to the editor. When we get on, select all
	 */
	public class EditorFocusListener extends java.awt.event.FocusAdapter {
		public void focusGained(java.awt.event.FocusEvent e) {
			m_datatype.getEditor().selectAll();
		}
	}

}
