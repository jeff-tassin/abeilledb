package com.jeta.abeille.gui.table.mysql;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.layouts.TableLayout;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.plugins.abeille.mysql.MySQLDatabaseImplementation;
import com.jeta.plugins.abeille.mysql.MySQLTableAttributes;
import com.jeta.plugins.abeille.mysql.MySQLTableType;

/**
 * @author Jeff Tassin
 */
public class MySQLTableAttributesView extends TSPanel {
	/** the table id we are showing options for */
	private TableId m_tableid;

	/** the database connection */
	private TSConnection m_connection;

	/**
	 * The combo box that displays the available table types for this MySQL
	 * instance.
	 */
	private JComboBox m_typescombo;

	/** label used to display a description for the table type */
	private JLabel m_description;

	/** the button used for setting the default table type */
	private JButton m_defaultbtn;

	/**
	 * flag that indicates if the given table is a prototype or exists in the
	 * database
	 */
	private boolean m_prototype = true;

	/** a collection of supported table types (MySQLTableType objects ) */
	private Collection m_supportedtypes;

	/**
	 * ctor for viewing an existing table
	 */
	public MySQLTableAttributesView(TSConnection connection) {
		this(connection, false, null);
	}

	/**
	 * ctor for editing a new table
	 */
	public MySQLTableAttributesView(TSConnection connection, boolean prototype, MySQLTableAttributes attr) {
		m_connection = connection;
		m_prototype = prototype;
		setLayout(new BorderLayout());
		add(createView(), BorderLayout.NORTH);
		setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));

		if (isPrototype())
			setTableType(getDefaultTableType());

		if (attr != null)
			setAttributes(attr);
	}

	/**
	 * Creates the view components
	 */
	private JComponent createView() {
		m_description = new JLabel();
		m_description.setText("Description ");

		m_defaultbtn = new JButton(I18N.getLocalizedMessage("Set Default"));
		m_typescombo = new JComboBox();

		if (!isPrototype()) {
			m_typescombo.setEnabled(false);
			m_defaultbtn.setEnabled(false);
		}

		MySQLDatabaseImplementation dbimpl = (MySQLDatabaseImplementation) m_connection
				.getImplementation(MySQLDatabaseImplementation.COMPONENT_ID);
		m_supportedtypes = dbimpl.getSupportedTableTypes();
		Iterator iter = m_supportedtypes.iterator();
		while (iter.hasNext()) {
			MySQLTableType ttype = (MySQLTableType) iter.next();
			if (!(isPrototype() && ttype == MySQLTableType.MERGE)) {
				m_typescombo.addItem(ttype);
			}
		}

		/** 3 rows x 4 columns */
		double size[][] = {
				{ TableLayout.PREFERRED, 10, TSGuiToolbox.calculateAverageTextWidth(m_typescombo, 25), 40,
						TableLayout.PREFERRED }, { TableLayout.PREFERRED, 10, TableLayout.PREFERRED } };

		JPanel panel = new JPanel(new TableLayout(size));

		panel.add(new JLabel(I18N.getLocalizedMessage("Table Type")), "0,0");
		panel.add(m_typescombo, "2,0");

		panel.add(m_defaultbtn, "4,0");
		// panel.add( m_description, "2,2,4,2" );

		m_defaultbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MySQLTableType defaulttype = getDefaultTableType();
				MySQLTableType selectedtype = (MySQLTableType) JOptionPane.showInputDialog(null,
						I18N.getLocalizedMessage("Table Type"), I18N.getLocalizedMessage("Set Default Table Type"),
						JOptionPane.QUESTION_MESSAGE, null, m_supportedtypes.toArray(), defaulttype);

				if (selectedtype != null && !selectedtype.equals(defaulttype)) {
					setDefaultTableType(selectedtype);
				}
			}
		});

		return panel;
	}

	/**
	 * @return the default MySQL table type when creating a new table in the
	 *         modeler
	 */
	public static MySQLTableType getDefaultTableType() {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		String tabletype = userprops.getProperty(MySQLTableAttributes.DEFAULT_TABLE_TYPE, "");
		MySQLTableType ttype = MySQLTableType.fromString(tabletype);
		if (ttype == MySQLTableType.Unknown)
			ttype = MySQLTableType.MyISAM;

		return ttype;
	}

	/**
	 * @return the flag that indicates if we are viewing/editing table
	 *         attributes for a prototype table
	 */
	private boolean isPrototype() {
		return m_prototype;
	}

	/**
	 * Loads the data into the view
	 */
	public void loadData(TableId tableId) {
		TableMetaData tmd = m_connection.getTable(tableId);
		if (tmd == null) {
			m_typescombo.setEnabled(false);
			setTableType(MySQLTableType.Unknown);
		} else {
			MySQLTableAttributes attr = (MySQLTableAttributes) tmd.getAttributes();
			setAttributes(attr);
		}
	}

	/**
	 * Sets the table attributes for this view
	 */
	public void setAttributes(MySQLTableAttributes attr) {
		if (attr == null) {
			setTableType(MySQLTableType.Unknown);
		} else {
			MySQLTableType ttype = attr.getTableType();
			if (ttype == null) {
				ttype = MySQLTableType.Unknown;
			}
			setTableType(ttype);
		}
	}

	/**
	 * Sets the table id for this view
	 */
	public void setTableId(TableId tableId) {
		m_tableid = tableId;
		loadData(tableId);
	}

	/**
	 * Sets the default MySQL table type when creating a new table in the
	 * modeler
	 */
	public static void setDefaultTableType(MySQLTableType ttype) {
		TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
		userprops.setProperty(MySQLTableAttributes.DEFAULT_TABLE_TYPE, ttype.toString());
	}

	/**
	 * Sets the selected table type in the combo. If the type is not found in
	 * the combo, it is added and then selected
	 */
	private void setTableType(MySQLTableType ttype) {
		if (typesComboContains(ttype)) {
			m_typescombo.setSelectedItem(ttype);
		} else {
			m_typescombo.addItem(ttype);
			m_typescombo.setSelectedItem(ttype);
		}
	}

	/**
	 * Sets the table attributes
	 */
	public void toTable(TableMetaData tmd) {
		MySQLTableAttributes attr = (MySQLTableAttributes) tmd.getAttributes();
		if (attr == null) {
			attr = new MySQLTableAttributes();
			tmd.setAttributes(attr);
		}
		attr.setTableType((MySQLTableType) m_typescombo.getSelectedItem());
	}

	/**
	 * @return true if the table types combo contains the given table type
	 */
	private boolean typesComboContains(MySQLTableType ttype) {
		boolean result = false;
		javax.swing.ListModel lmodel = m_typescombo.getModel();
		for (int index = 0; index < lmodel.getSize(); index++) {
			MySQLTableType regtype = (MySQLTableType) lmodel.getElementAt(index);
			if (regtype.equals(ttype)) {
				result = true;
				break;
			}
		}
		return result;
	}

}
