package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays the properties for a given procedure/function
 * 
 * @author Jeff Tassin
 */
public class ProcedureBrowser extends TSPanel {
	/** the database connection */
	private TSConnection m_connection;

	/** displays the arguments for a set of procedures with the same name */
	private JComboBox m_argscombo = new JComboBox();

	/** the schema and procedure combo boxes */
	private ProcedureSelectorPanel m_procedureselectorpanel;

	/** the view that displays the procedure properties */
	private ProcedureView m_procview;

	/** command ids */
	public static final String ID_RELOAD = "btn.reload";
	public static final String ID_PROCEDURE_SELECTOR = "procedure.selector";

	private static Dimension m_prefsize = new Dimension(700, 500);

	/**
	 * ctor
	 */
	public ProcedureBrowser(TSConnection conn) {
		m_connection = conn;
		createView();
		setController(new ProcedureBrowserController(this));
	}

	/**
	 * ctor
	 */
	public ProcedureBrowser(TSConnection conn, String procName) {
		this(conn);
		if (procName == null || procName.trim().length() == 0)
			return;

		// now try to parse the procedure name and display it
		try {
			Schema schema = null;

			StoredProcedureService sps = (StoredProcedureService) m_connection
					.getImplementation(StoredProcedureService.COMPONENT_ID);
			if (m_connection.supportsSchemas()) {
				int pos = procName.indexOf(".");
				if (pos >= 0) {
					String sname = procName.substring(0, pos);
					procName = procName.substring(pos + 1, procName.length());
					schema = m_connection.getSchema(getCatalog(), sname);
				} else {
					schema = new Schema("pg_catalog");
				}
			} else {
				schema = Schema.VIRTUAL_SCHEMA;
			}

			if (schema != null) {
				Collection procs = sps.getProcedures(schema);
				Iterator iter = procs.iterator();
				while (iter.hasNext()) {
					StoredProcedure proc = (StoredProcedure) iter.next();
					if (I18N.equalsIgnoreCase(procName, proc.getName())) {
						showProcedure(proc);
						break;
					}
				}
			}

		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Creates the buttons
	 */
	public JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		// panel.setBorder( javax.swing.BorderFactory.createLineBorder(
		// java.awt.Color.red ) );
		JButton reloadbtn = createButton(I18N.getLocalizedMessage("Reload"), ID_RELOAD);
		reloadbtn.setIcon(TSGuiToolbox.loadImage("incors/16x16/refresh.png"));
		panel.add(reloadbtn);
		return panel;
	}

	/**
	 * Create the table selector panel used to select the schema/table for this
	 * view
	 */
	public JPanel createProcedureSelectorPanel() {
		m_procedureselectorpanel = new ProcedureSelectorPanel(m_connection);
		m_procedureselectorpanel.setName(ID_PROCEDURE_SELECTOR);

		TSComboBox catalogbox = m_procedureselectorpanel.getCatalogsCombo();
		catalogbox.setEnabled(false);

		TSComboBox schemabox = m_procedureselectorpanel.getSchemasCombo();
		TSComboBox procname = m_procedureselectorpanel.getObjectsCombo();
		m_argscombo = m_procedureselectorpanel.getArgumentsCombo();
		m_argscombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ProcedureWrapper pwrapper = (ProcedureWrapper) m_argscombo.getSelectedItem();
				StoredProcedure proc = null;
				if (pwrapper != null)
					proc = pwrapper.getProcedure();

				setProcedure(proc);
			}
		});

		procname.setValidating(false);
		ControlsAlignLayout layout = m_procedureselectorpanel.getControlsLayout();

		layout.setMaxTextFieldWidth(catalogbox, 35);
		layout.setMaxTextFieldWidth(schemabox, 35);
		layout.setMaxTextFieldWidth(procname, 35);
		layout.setMaxTextFieldWidth(m_argscombo, 35);

		// now add the stick and reload buttons
		JPanel panel = m_procedureselectorpanel.getControlsPanel();

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 2;
		c.gridy = 3;

		JPanel btnspanel = createButtonsPanel();
		panel.add(btnspanel, c);

		return m_procedureselectorpanel;
	}

	/**
	 * creates the main view for this frame
	 */
	public void createView() {
		setLayout(new BorderLayout());
		add(createProcedureSelectorPanel(), BorderLayout.NORTH);
		setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		m_procview = new ProcedureView(m_connection);
		add(m_procview, BorderLayout.CENTER);
	}

	public Catalog getCatalog() {
		return m_connection.getDefaultCatalog();
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return m_prefsize;
	}

	/**
	 * @return the currently selected procedure name from the procedure name
	 *         combo
	 */
	public String getSelectedProcedureName() {
		return m_procedureselectorpanel.getProcedureName();
	}

	/**
	 * @return the currently selected schema from the schema combo
	 */
	public Schema getSelectedSchema() {
		return m_procedureselectorpanel.getSchema();
	}

	/**
	 * Loads the set of procedures in the combo box and displays the first one.
	 * This is used to display different functions/procedures with the same name
	 * 
	 * @param procs
	 *            a collection of StoredProcedure objects to display in the
	 *            frame
	 */
	void reloadProcedures(Collection procs) {
		try {
			m_argscombo.removeAllItems();
			Iterator iter = procs.iterator();
			while (iter.hasNext()) {
				StoredProcedure proc = (StoredProcedure) iter.next();
				proc = ProcedureModel.loadProcedure(m_connection, proc);
				m_argscombo.addItem(new ProcedureWrapper(proc));
			}

			if (m_argscombo.getItemCount() > 0) {
				ProcedureWrapper wrapper = (ProcedureWrapper) m_argscombo.getItemAt(0);
				setProcedure(wrapper.getProcedure());
			} else {
				setProcedure(null);
			}
		} catch (Exception e) {
			SQLErrorDialog.showErrorDialog(this, e, null);
		}
	}

	/**
	 * Sets the procedure for the frame. All views are updated to display the
	 * properties for the procedure
	 */
	private void setProcedure(StoredProcedure proc) {
		m_procview.setProcedure(proc);
	}

	/**
	 * This method displays the given procedure in the frame. It is used when a
	 * caller wants to display a procedure from some other part of the program.
	 * Only the given procedure is shown. Procedures with the same name are not
	 * displayed.
	 */
	public void showProcedure(StoredProcedure proc) {
		try {
			m_argscombo.removeAllItems();
			TSComboBox namecombo = m_procedureselectorpanel.getObjectsCombo();
			if (proc == null) {
				m_procedureselectorpanel.setSchema(null);
				namecombo.setSelectedItem(null);
			} else {
				StoredProcedureService sps = (StoredProcedureService) m_connection
						.getImplementation(StoredProcedureService.COMPONENT_ID);
				proc = sps.load(proc);
				m_argscombo.addItem(new ProcedureWrapper(proc));
				m_procedureselectorpanel.setCatalog(proc.getCatalog());
				m_procedureselectorpanel.setSchema(proc.getSchema());
				namecombo.setSelectedItem(proc);
			}
			m_procview.setProcedure(proc);
		} catch (SQLException se) {
			SQLErrorDialog.showErrorDialog(this, se, null);
		}
	}

}
