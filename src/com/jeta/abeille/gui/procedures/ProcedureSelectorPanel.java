package com.jeta.abeille.gui.procedures;

import java.awt.Dimension;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.common.MetaDataPopupRenderer;
import com.jeta.abeille.gui.common.SchemaSelectorPanel;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.SortedListModel;
import com.jeta.foundation.gui.components.PopupList;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is a panel that contains a schema and procedure combo box to allow the
 * user to select a procedure/function from the database.
 * 
 * @author Jeff Tassin
 */
public class ProcedureSelectorPanel extends SchemaSelectorPanel {
	private HashMap m_schemas = new HashMap();

	/** an empty list model we reuse when an invalid schema is selected */
	private SortedListModel m_empty = new SortedListModel();

	private JComboBox m_argscombo;

	/**
	 * Support adding other components that have labels aligned with schema and
	 * table selector labels.
	 */
	public ProcedureSelectorPanel(TSConnection connection) {
		super(connection);

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Arguments"));

		JComponent[] comps = new JComponent[1];
		m_argscombo = new JComboBox();

		comps[0] = m_argscombo;

		initialize(labels, comps, false);

		DbModel model = getConnection().getModel(getCatalog());
		Collection schemas = model.getSchemas();
		setSchemas(getConnection().getCurrentSchema(), schemas);

		PopupList list = getObjectsCombo().getPopupList();
		list.setRenderer(MetaDataPopupRenderer.createInstance(getConnection()));
		list.setComparator(new ProcedureNameComparator());

		setCatalogs(getConnection().getDefaultCatalog(), getConnection().getCatalogs());
		reloadProcedures(getConnection().getCurrentSchema());

		getSchemasCombo().addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (isSchemaReload()) {
					reloadProcedures(getSchema());
				}
			}
		});
	}

	public JComboBox getArgumentsCombo() {
		return m_argscombo;
	}

	public Catalog getCatalog() {
		return getConnection().getDefaultCatalog();
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height += 20;
		return d;
	}

	/**
	 * @return the set of procedures that have the given name and schema. This
	 *         can return more than one result because some databases allow
	 *         functions/procedures with the same name (method overloading).
	 */
	public Collection getProcedures() {
		try {
			Schema schema = getSchema();
			String procname = getProcedureName();
			StoredProcedureService sps = (StoredProcedureService) getConnection().getImplementation(
					StoredProcedureService.COMPONENT_ID);
			return sps.getProcedures(schema, procname);
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
			return new LinkedList();
		}
	}

	/**
	 * @return the selected procedure name
	 */
	public String getProcedureName() {
		return getObjectsCombo().getText().trim();
	}

	/**
	 * Create the controls for this panel and loads them with the necessary data
	 * 
	 * @param labels
	 *            an array of JLabels to add to this panel
	 * @param comps
	 *            an array of JComponents to add to this panel (aligned
	 *            horizonally with the corresponding JLabel from the labels
	 *            array.
	 * @param bfirst
	 *            set to true if you want the labels and components to appear
	 *            before the schema and reftable components, false otherwise
	 */
	public void initialize(JLabel[] labels, JComponent[] comps, boolean bfirst) {
		super.initialize(labels, comps, bfirst);
		getObjectsLabel().setText(I18N.getLocalizedDialogLabel("Function"));
	}

	/**
	 * Reloads the procedures for the selected schema
	 */
	private void reloadProcedures(Schema schema) {
		SortedListModel listmodel = (SortedListModel) m_schemas.get(schema);
		if (listmodel == null) {
			if (schema == null) {
				listmodel = m_empty;
			} else {
				try {
					listmodel = new SortedListModel();
					listmodel.setComparator(new ProcedureNameComparator());
					StoredProcedureService sps = (StoredProcedureService) getConnection().getImplementation(
							StoredProcedureService.COMPONENT_ID);
					Collection c = sps.getProcedures(schema);
					Iterator iter = c.iterator();
					while (iter.hasNext()) {
						listmodel.add(iter.next());
					}
					m_schemas.put(schema, listmodel);
				} catch (Exception e) {
					listmodel = m_empty;
					m_schemas.put(schema, listmodel);
					TSUtils.printStackTrace(e);
				}

				try {
					getConnection().getMetaDataConnection().commit();
				} catch (SQLException e) {
					TSUtils.printException(e);
				}

			}
		}
		PopupList list = getObjectsCombo().getPopupList();
		list.setModel(listmodel);
	}

	/**
	 * This class compares only the table names of two table ids. It does not
	 * take into account the schema
	 */
	public static class ProcedureNameComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			if (o1 != null && o2 != null) {
				String tname1 = null;
				String tname2 = null;

				if (o1 instanceof String) {
					tname1 = (String) o1;
				} else if (o1 instanceof StoredProcedure) {
					StoredProcedure id1 = (StoredProcedure) o1;
					tname1 = id1.toString();
				}

				if (o2 instanceof String) {
					tname2 = (String) o2;
				} else if (o2 instanceof StoredProcedure) {
					StoredProcedure id2 = (StoredProcedure) o2;
					tname2 = id2.toString();
				}

				if (tname1 != null) {
					return tname1.compareToIgnoreCase(tname2);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		}
	}

}
