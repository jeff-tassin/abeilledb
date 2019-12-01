package com.jeta.abeille.gui.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.layouts.ColumnLayout;

import com.jeta.foundation.gui.table.ClipboardUtils;
import com.jeta.foundation.gui.table.ClipboardNames;
import com.jeta.foundation.gui.table.export.ExportModel;

import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.TSTablePanel;

/**
 * This is the GUI used to export data from a query to a file. The user can
 * specify the file as well as options such as delimiters and including column
 * names.
 * 
 * @author Jeff Tassin
 */
public class ExportPanel extends TSPanel {
	/**
	 * The model that describes the data we wish to export
	 */
	private SQLExportModel m_model;

	/**
	 * The text field that displays or allows the user to input the filename
	 */
	private JTextField m_filenamefield;

	/**
	 * The column options table
	 */
	private JTable m_table;

	/**
	 * The table panel that contains the column options table
	 */
	private TSTablePanel m_tablepanel;

	/**
	 * The label that displays the status of the export. (i.e. the number of
	 * rows saved so far)
	 */
	private JLabel m_statuslabel;

	public static final char SINGLE_QUOTES = '\'';
	public static final char DOUBLE_QUOTES = '\"';

	/**
	 * ctor
	 * 
	 * @param model
	 *            the model that describes the data we wish to export
	 */
	public ExportPanel(SQLExportModel model) {
		m_model = model;
		createComponents();
		setController(new ExportPanelController(this));
	}

	void createComponents() {
		setLayout(new BorderLayout(10, 10));
		add(createTopPanel(), BorderLayout.NORTH);

		JTabbedPane tab_pane = new JTabbedPane();
		tab_pane.addTab(I18N.getLocalizedMessage("General Options"),
				TSGuiToolbox.loadImage("incors/16x16/document_preferences.png"), createGeneralOptionsPanel());

		tab_pane.addTab(I18N.getLocalizedMessage("Columns"), TSGuiToolbox.loadImage("incors/16x16/column.png"),
				createColumnsPanel());

		add(tab_pane, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	JPanel createTopPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new ColumnLayout());
		panel.add(createToolBar());
		panel.add(Box.createVerticalStrut(5));
		panel.add(createTargetComponents());
		panel.add(Box.createVerticalStrut(5));
		return panel;
	}

	private JButton createToolBarBtn(String iconName, String txt, String cmd) {
		JButton btn = new JButton(txt);
		btn.setIcon(TSGuiToolbox.loadImage(iconName));
		btn.setName(cmd);
		btn.setVerticalAlignment(JButton.BOTTOM);
		return btn;
	}

	private JPanel createToolBar() {
		JPanel panel = new JPanel(new java.awt.GridLayout(1, 5));
		panel.add(createToolBarBtn("incors/16x16/clear.png", "Clear", ExportNames.ID_RESET));
		panel.add(createToolBarBtn("incors/16x16/element_run.png", "Start", ExportNames.ID_START_EXPORT));
		panel.add(createToolBarBtn("incors/16x16/stop.png", "Stop", ExportNames.ID_STOP_EXPORT));
		panel.add(createToolBarBtn("incors/16x16/document_view.png", "Preview", ExportNames.ID_SAMPLE_OUTPUT));

		/*
		javax.swing.JButton hbtn = createToolBarBtn("incors/16x16/help2.png", "Help", ExportNames.ID_HELP);
		com.jeta.foundation.help.HelpUtils.enableHelpOnButton(hbtn, ExportNames.ID_HELP);
		panel.add(hbtn);
		 */

		return panel;
	}

	/**
	 * Creates and initializes the components on this panel
	 */
	JPanel createGeneralOptionsPanel() {
		Component[] labels = new Component[5];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Column Headers"));
		labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Value Delimiter"));
		labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Text Delimiter"));
		labels[3] = new JLabel(I18N.getLocalizedDialogLabel("Line Decorator"));
		labels[4] = new JLabel("");

		JTextField txtfield = TSEditorUtils.createTextField();
		txtfield.setName(ExportNames.ID_LINE_DECORATOR);
		txtfield.setText(ExportNames.LINE_EXPRESSION);

		Component[] comps = new Component[5];

		comps[0] = ClipboardUtils.createShowHeadersComponents(m_model.isShowColumnNames());
		comps[1] = ClipboardUtils
				.createValueComponent(ClipboardNames.ID_VALUE_DELIMITER, ClipboardNames.ID_NULLS_FIELD);
		comps[2] = createTextDelimComponents();
		comps[3] = txtfield;
		comps[4] = javax.swing.Box.createVerticalStrut(30);

		JPanel cpanel = TSGuiToolbox.alignLabelTextRows(labels, comps, new Insets(5, 0, 4, 10));
		cpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(cpanel, BorderLayout.NORTH);
		return panel;
	}

	/**
	 * This panel contains the table that allows the user to specify options on
	 * how the data is stored for individual columns
	 * 
	 * @return the container that contains the component
	 */
	Container createColumnsPanel() {
		// TSTablePanel tablepanel = new TSTablePanel(
		// m_model.getColumnOptionsModel() );
		m_tablepanel = TableUtils.createSimpleTable(m_model.getColumnOptionsModel(), false);
		m_table = m_tablepanel.getTable();
		TableUtils.setColumnWidths(m_table);

		m_table.setName(ExportNames.ID_STANDARD_COLUMNS_TABLE);
		TableColumnModel cmodel = m_table.getColumnModel();
		TableColumn col = cmodel.getColumn(ColumnOptionsModel.COLUMNNAME_COLUMN);
		col.setPreferredWidth(col.getWidth() * 2);

		col = cmodel.getColumn(ColumnOptionsModel.OUTPUT_COLUMN);
		col.setPreferredWidth(col.getWidth() * 4);

		JPanel innerpanel = new JPanel(new BorderLayout());
		innerpanel.add(m_tablepanel, BorderLayout.CENTER);

		JPanel btnpanel = new JPanel();
		btnpanel.setLayout(new BoxLayout(btnpanel, BoxLayout.X_AXIS));

		JButton btn = createButton(TSGuiToolbox.loadImage("navigation/Up16.gif"), ExportNames.ID_MOVE_UP);
		Dimension d = btn.getPreferredSize();
		d.width = 24;
		btn.setPreferredSize(d);
		btn.setMaximumSize(d);
		btnpanel.add(btn);

		btn = createButton(TSGuiToolbox.loadImage("navigation/Down16.gif"), ExportNames.ID_MOVE_DOWN);
		d = btn.getPreferredSize();
		d.width = 24;
		btn.setPreferredSize(d);
		btn.setMaximumSize(d);
		btnpanel.add(btn);

		innerpanel.add(btnpanel, BorderLayout.SOUTH);

		innerpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel outerpanel = new JPanel(new BorderLayout());
		outerpanel.add(innerpanel, BorderLayout.CENTER);
		innerpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		return outerpanel;
	}

	/**
	 * Creates a set of radio buttons that the user can select the target of the
	 * export: clipboard or file
	 * 
	 * @return the resulting container that has the radio buttons
	 */
	Container createTargetComponents() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		ButtonGroup group = new ButtonGroup();

		JRadioButton rbtn = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("Clipboard"));
		rbtn.setName(ExportNames.ID_TARGET_CLIPBOARD);
		rbtn.setSelected(true);
		panel.add(rbtn);
		group.add(rbtn);
		panel.add(Box.createHorizontalStrut(20));

		rbtn = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("File"));
		rbtn.setName(ExportNames.ID_TARGET_FILE);
		panel.add(rbtn);
		group.add(rbtn);

		// JPanel o_panel = new JPanel( new BorderLayout() );

		m_filenamefield = TSEditorUtils.createTextField();
		m_filenamefield.setName(ExportNames.ID_FILENAME_FIELD);
		m_filenamefield.setEnabled(false);

		JButton filebtn = createButton(TSGuiToolbox.loadImage("openfile16.gif"), ExportNames.ID_SELECT_FILE);
		TextFieldwButtonPanel fnamepanel = new TextFieldwButtonPanel(m_filenamefield, filebtn);
		filebtn.setEnabled(false);

		panel.add(Box.createHorizontalStrut(5));
		panel.add(fnamepanel);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Target")),
				BorderFactory.createEmptyBorder(5, 10, 5, 10)));

		// rbtn = TSGuiToolbox.createRadioButton(
		// I18N.getLocalizedMessage("Database Table") );
		// rbtn.setName( ExportNames.ID_TARGET_DATABASE_TABLE );
		// panel.add( rbtn );
		// group.add( rbtn );

		return panel;
	}

	/**
	 * Creates a set of radio buttons that the user can select how text values
	 * in a store result set are delimited. Currently, we only support single
	 * quotes and double quotes
	 * 
	 * @return the resulting container that has the radio buttons
	 */
	Container createTextDelimComponents() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		ButtonGroup group = new ButtonGroup();

		JRadioButton rbtn = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("Single Quote"));
		rbtn.setName(ExportNames.ID_SINGLE_QUOTE_RADIO);
		rbtn.setSelected(true);
		panel.add(rbtn);
		group.add(rbtn);
		panel.add(Box.createHorizontalStrut(10));

		rbtn = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("Double Quotes"));
		rbtn.setName(ExportNames.ID_DOUBLE_QUOTES_RADIO);
		panel.add(rbtn);
		group.add(rbtn);
		panel.add(Box.createHorizontalStrut(10));

		rbtn = TSGuiToolbox.createRadioButton(I18N.getLocalizedMessage("None"));
		rbtn.setName(ExportNames.ID_NO_QUOTES_RADIO);
		panel.add(rbtn);
		group.add(rbtn);

		panel.add(Box.createHorizontalStrut(10));
		JCheckBox cbox = TSGuiToolbox.createCheckBox("SQL");
		cbox.setName(ExportNames.ID_SQL_FORMAT);
		cbox.setSelected(true);
		panel.add(cbox);

		// create a horizontal space, 16 pixels wide
		/*
		 * panel.add( Box.createHorizontalStrut( 16 ) ); JCheckBox sqlformat =
		 * TSGuiToolbox.createCheckBox( I18N.getLocalizedMessage("SQL Format")
		 * ); sqlformat.setName( ExportNames.ID_SQL_FORMAT ); if (
		 * m_model.isSQLFormat() ) sqlformat.setSelected( true ); panel.add(
		 * sqlformat );
		 */

		return panel;
	}

	/**
	 * @return the text entered (path and filename) in the file name text field
	 */
	public String getPath() {
		return m_filenamefield.getText();
	}

	/**
	 * @return the underlying data model for this view
	 */
	public SQLExportModel getModel() {
		return m_model;
	}

	/**
	 * @return the currently selected row in the column options table. -1 is
	 *         returned if no row is selected
	 */
	public int getSelectedRow() {
		return m_table.getSelectedRow();
	}

	/**
	 * @return true if the export to clipboard radio button is selected
	 */
	public boolean isExportToClipboard() {
		boolean result = false;
		JRadioButton rbtn = (JRadioButton) getComponentByName(ExportNames.ID_TARGET_CLIPBOARD);
		if (rbtn != null)
			result = rbtn.isSelected();
		return result;
	}

	/**
	 * @return true if the export to file radio button is selected
	 */
	public boolean isExportToFile() {
		boolean result = false;
		JRadioButton rbtn = (JRadioButton) getComponentByName(ExportNames.ID_TARGET_FILE);
		if (rbtn != null)
			result = rbtn.isSelected();
		return result;
	}

	/**
	 * Reads the data model and updates the GUI components
	 */
	public void readModel() {
		// save filename
		JTextField txtfield = (JTextField) getComponentByName(ExportNames.ID_FILENAME_FIELD);
		txtfield.setText(m_model.getFileName());

		// line decorator
		txtfield = (JTextField) getComponentByName(ExportNames.ID_LINE_DECORATOR);
		txtfield.setText(m_model.getLineDecorator());

		txtfield = (JTextField) getComponentByName(ClipboardNames.ID_NULLS_FIELD);
		txtfield.setText(m_model.getNullsValue());
	}

	/**
	 * Removes all components from this panel and rebuilds the view. This is
	 * used when one component makes other components visible (e.g. when the
	 * user clicks the show column headers check box).
	 */
	void rebuildComponents() {
		repaint();
		removeAll();
		createComponents();
		revalidate();
	}

	/**
	 * Resets the model to default settings
	 */
	public void reset() {
		m_model.reset();
		readModel();
	}

	/**
	 * Reads the input from the components and stores the data in the
	 * ExportModel
	 */
	public void saveToModel() {
		// save filename
		JTextField txtfield = (JTextField) getComponentByName(ExportNames.ID_FILENAME_FIELD);
		assert (txtfield != null);
		m_model.setFileName(txtfield.getText());

		// column names
		JCheckBox cbox = (JCheckBox) getComponentByName(ClipboardNames.ID_SHOWHEADERS_CHECKBOX);
		assert (cbox != null);
		m_model.setShowColumnNames(cbox.isSelected());

		// column header delimiter
		TSComboBox combo = (TSComboBox) getComponentByName(ClipboardNames.ID_COLUMN_HEADER_DELIMITER);
		assert (combo != null);
		m_model.setColumnNameDelimiter(combo.getText());

		// value delimiter
		combo = (TSComboBox) getComponentByName(ClipboardNames.ID_VALUE_DELIMITER);
		assert (combo != null);
		m_model.setValueDelimiter(combo.getText());

		// nulls
		txtfield = (JTextField) getComponentByName(ClipboardNames.ID_NULLS_FIELD);
		assert (txtfield != null);
		m_model.setNullsValue(txtfield.getText());

		// text delimiter
		JRadioButton rbtn = (JRadioButton) getComponentByName(ExportNames.ID_SINGLE_QUOTE_RADIO);
		assert (rbtn != null);
		if (rbtn.isSelected())
			m_model.setTextDelimiter(SINGLE_QUOTES);
		else {
			rbtn = (JRadioButton) getComponentByName(ExportNames.ID_DOUBLE_QUOTES_RADIO);
			if (rbtn.isSelected())
				m_model.setTextDelimiter(DOUBLE_QUOTES);
			else
				m_model.setTextDelimiter('\0');
		}

		cbox = (JCheckBox) getComponentByName(ExportNames.ID_SQL_FORMAT);
		m_model.setSQLTextDelimit(cbox.isSelected());

		// line decorator
		txtfield = (JTextField) getComponentByName(ExportNames.ID_LINE_DECORATOR);
		assert (txtfield != null);
		m_model.setLineDecorator(txtfield.getText());

		// cbox = (JCheckBox)getComponentByName( ExportNames.ID_SQL_FORMAT );
		// assert( cbox != null );
		// m_model.setSQLFormat( cbox.isSelected() );

	}

	/**
	 * Selects the given row in the table
	 */
	public void selectRow(int row) {
		m_tablepanel.deselect();
		m_table.setRowSelectionInterval(row, row);
		m_table.setColumnSelectionInterval(0, m_model.getColumnOptionsModel().getColumnCount() - 1);
		m_table.repaint();
	}

	/**
	 * Sets the target file for the export
	 */
	public void setOutputFile(String path) {
		m_filenamefield.setText(path);
	}

	/**
	 * Changes all the settings in the model to export the results as SQL
	 * (insert statements)
	 */
	public void setSQLFormat() {
		m_model.setSQLFormat();
		readModel();
	}

}
