package com.jeta.abeille.gui.importer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.io.File;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TableMetaData;

//import com.jeta.abeille.gui.common.TableSelectorPanel;
//import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.abeille.gui.modeler.DataTypeRenderer;
import com.jeta.abeille.gui.modeler.PrimaryKeyRenderer;

import com.jeta.abeille.gui.store.ColumnInfo;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.table.TSTablePanel;

import com.jeta.foundation.i18n.I18N;

/**
 * This is a panel for import test data into a RDBMS table.
 * 
 * @author Jeff Tassin
 */
public class ImportPanel extends TSPanel {
	// ////////////////////////////////////////////////////////
	// fields pane

	private ImportModel m_importmodel; // GUI model for field table
	private JTable m_columnstable;

	private JTextField m_filefield;
	private JButton m_previewbtn;
	private JButton m_startbtn;
	private JButton m_stopbtn;
	private JTextField m_instancesfield;

	private static ImageIcon m_pkimage = TSGuiToolbox.loadImage("primarykey16.gif");
	private static ImageIcon m_pkheaderimage = TSGuiToolbox.loadImage("primarykeyheader16.gif");
	private static ImageIcon m_fkicon = TSGuiToolbox.loadImage("foreignkey16.gif");

	// component ids
	public static final String ID_COLUMNS_TABLE = "columnstable";
	public static final String ID_FILE_FIELD = "file.field";

	public static final String ID_SELECT_FILE = "select.file";
	public static final String ID_PREVIEW_IMPORT = "preview.import";
	public static final String ID_START_IMPORT = "start.import";
	public static final String ID_STOP_IMPORT = "stop.import";
	public static final String ID_WRITE_TO_FILE = "write.to.file";

	/**
	 * ctor
	 */
	public ImportPanel(ImportModel model) {
		m_importmodel = model;
		initialize();
	}

	/**
	 * Creates the controls for this panel
	 */
	private JPanel createControlsPanel() {
		JPanel btnpanel = new JPanel();

		m_previewbtn = i18n_createButton("Preview", ID_PREVIEW_IMPORT);
		btnpanel.add(m_previewbtn);
		m_startbtn = i18n_createButton("Start", ID_START_IMPORT);
		btnpanel.add(m_startbtn);
		m_stopbtn = i18n_createButton("Stop", ID_STOP_IMPORT);
		btnpanel.add(m_stopbtn);
		m_stopbtn.setEnabled(false);

		JPanel spanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 2, 2);

		spanel.add(new JLabel("Instances: "), c);

		c.gridx = 1;
		c.gridy = 0;
		m_instancesfield = new JTextField(5);
		m_instancesfield.setText("100");
		spanel.add(m_instancesfield, c);

		c.gridx = 1;
		c.gridy = 1;
		JCheckBox cbox = new JCheckBox("Write to file");
		cbox.setName(ID_WRITE_TO_FILE);
		spanel.add(cbox, c);

		c.gridx = 0;
		c.gridy = 2;
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.weightx = 1.0;
		spanel.add(new JLabel("Filename: "), c);

		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		m_filefield = new JTextField();
		m_filefield.setName(ID_FILE_FIELD);
		spanel.add(m_filefield, c);

		c.gridx = 3;
		c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;

		JButton changebtn = createButton("Change", ID_SELECT_FILE);
		spanel.add(changebtn, c);

		c.gridx = 0;
		c.gridy = 4;
		spanel.add(Box.createVerticalStrut(10), c);

		JPanel result = new JPanel(new BorderLayout());
		result.add(btnpanel, BorderLayout.NORTH);
		result.add(spanel, BorderLayout.SOUTH);
		return result;
	}

	/**
	 * @return a collection of ColumnMetaData objects specified by the user
	 */
	public Collection getColumns() {
		return m_importmodel.getColumns();
	}

	/**
	 * @return the number of instances
	 */
	public int getInstances() {
		return Integer.parseInt(m_instancesfield.getText());
	}

	/**
	 * @return the field table in this panel
	 */
	public JTable getTable() {
		return m_columnstable;
	}

	/**
	 * @return the model for this panel
	 */
	public ImportModel getModel() {
		return m_importmodel;
	}

	/**
	 * @return the preferred size of this panel
	 */
	public Dimension getPreferredSize() {
		int width = TSGuiToolbox.calculateAverageTextWidth(m_columnstable, 10) * 10;
		java.awt.FontMetrics metrics = m_columnstable.getFontMetrics(m_columnstable.getFont());
		int height = metrics.getHeight() * 15;
		Dimension d = new Dimension(width, height);
		// make sure d can't exceed screen size
		TSGuiToolbox.calculateReasonableComponentSize(d);
		return d;
	}

	/**
	 * @return the currently selected item in the table. Null is returned if no
	 *         item is selected
	 */
	ColumnInfo getSelectedItem() {
		int index = m_columnstable.getSelectedRow();
		if (index >= 0)
			return (ColumnInfo) m_importmodel.getRow(index);
		else
			return null;
	}

	/**
	 * Creates and initializes the controls for this panel
	 */
	private void initialize() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		// m_columnstable = new JTable( m_importmodel );
		TSTablePanel tablepanel = new TSTablePanel(m_importmodel);
		m_columnstable = tablepanel.getTable();

		m_columnstable.setName(ID_COLUMNS_TABLE);

		// m_columnstable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		TableColumnModel cmodel = m_columnstable.getColumnModel();
		cmodel.getColumn(ImportModel.PRIMARYKEY_COLUMN).setWidth(32);
		cmodel.getColumn(ImportModel.PRIMARYKEY_COLUMN).setMaxWidth(32);
		cmodel.getColumn(ImportModel.PRIMARYKEY_COLUMN).setMinWidth(32);
		cmodel.getColumn(ImportModel.PRIMARYKEY_COLUMN).setPreferredWidth(32);
		cmodel.getColumn(ImportModel.PRIMARYKEY_COLUMN).setHeaderRenderer(new PrimaryKeyRenderer(true));
		cmodel.getColumn(ImportModel.DATATYPE_COLUMN).setCellRenderer(new DataTypeRenderer());

		panel.add(createControlsPanel(), BorderLayout.NORTH);
		panel.add(tablepanel, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);

		setController(new ImportPanelController(this));
	}

	/**
	 * @return the selected column in the columns table (in model coordinates)
	 */
	public int getSelectedColumn() {
		return m_columnstable.getSelectedColumn();
	}

	/**
	 * Sets the file that we will use to output the insert SQL
	 */
	void setFile(File f) {
		m_filefield.setText(f.getAbsolutePath());
	}
}
