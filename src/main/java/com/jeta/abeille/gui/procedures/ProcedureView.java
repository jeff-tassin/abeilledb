package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays the properties for a given procedure/function
 * 
 * @author Jeff Tassin
 */
public class ProcedureView extends TSPanel {
	/** the database connection */
	private TSConnection m_connection;

	/** the model for the procedures */
	private ProcedureModel m_proceduremodel;

	/** the parameters view */
	private ParametersView m_paramsview;

	/** the src view */
	private SourceView m_srcview;

	private JTextArea m_description;

	/**
	 * ctor
	 */
	public ProcedureView(TSConnection connection) {
		m_connection = connection;
		createView();
	}

	/**
	 * creates the view
	 */
	void createView() {
		setLayout(new BorderLayout());
		JTabbedPane tab = new JTabbedPane();
		add(tab, BorderLayout.CENTER);

		m_proceduremodel = new ProcedureModel(m_connection);
		m_srcview = new SourceView(m_proceduremodel);
		tab.addTab(I18N.getLocalizedMessage("Source"), m_srcview);

		m_paramsview = new ParametersView();
		tab.addTab(I18N.getLocalizedMessage("Arguments"), m_paramsview);

		JPanel dpanel = new JPanel(new BorderLayout());
		m_description = new JTextArea();
		m_description.setWrapStyleWord(true);
		m_description.setEditable(false);

		// JScrollPane scroll = new JScrollPane( m_description );
		// dpanel.add( scroll, BorderLayout.CENTER );
		// dpanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 )
		// );
		// tab.addTab( I18N.getLocalizedMessage( "Description" ), dpanel );
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Sets the procedure for the frame. All views are updated to display the
	 * properties for the procedure
	 */
	public void setProcedure(StoredProcedure proc) {
		m_proceduremodel.setProcedure(proc);
		m_srcview.setModel(m_proceduremodel);
		m_paramsview.setModel(m_proceduremodel);
		if (proc == null) {
			m_description.setText("");
		} else {
			if (proc.getDescription() == null) {
				m_description.setText("");
			} else {
				m_description.setText(proc.getDescription());
			}
		}
	}
}
