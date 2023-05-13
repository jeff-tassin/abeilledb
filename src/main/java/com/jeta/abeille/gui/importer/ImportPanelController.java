package com.jeta.abeille.gui.importer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.GenericFileFilter;
import com.jeta.foundation.gui.components.TSController;

/**
 * This is the controller for the ImportPanel GUI. It handles all button/gui
 * events.
 * 
 * @author Jeff Tassin
 */
public class ImportPanelController extends TSController {
	private ImportPanel m_importpanel; // this is the panel that this controller
										// controls
	private ImportModel m_importmodel;

	private Connection m_connection;

	/** flag indicating if we should write directly to the database */
	private boolean m_directimport = false;

	/**
	 * Constructor
	 */
	public ImportPanelController(ImportPanel panel) {
		super(panel);
		m_importpanel = panel;
		m_importmodel = panel.getModel();
		assignAction(ImportPanel.ID_PREVIEW_IMPORT, new PreviewAction());
		assignAction(ImportPanel.ID_SELECT_FILE, new SelectFileAction());
		assignAction(ImportPanel.ID_START_IMPORT, new StartImportAction());
	}

	private Connection getConnection() throws SQLException {
		if (m_connection == null) {
			TSConnection tsconn = m_importmodel.getConnection();
			m_connection = tsconn.getWriteConnection();
			m_connection.setAutoCommit(true);
		}
		return m_connection;
	}

	/**
	 * Writes out a given number of SQL insert statements to the given writer
	 * object
	 */
	void start(Writer writer, int numInstances) throws IOException, SQLException {
		for (int index = 0; index < numInstances; index++) {
			writer.write("INSERT INTO " + m_importmodel.getTableId().getTableName() + " (");
			boolean bfirst = true;
			for (int row = 0; row < m_importmodel.getRowCount(); row++) {
				ColumnHandler handler = m_importmodel.getColumnHandler(row);
				String output = handler.getOutput(index);
				if (output.length() == 0 || output.equals("null")) {

				} else {
					if (bfirst)
						bfirst = false;
					else
						writer.write(", ");

					writer.write((String) m_importmodel.getValueAt(row, ImportModel.NAME_COLUMN));
				}
			}

			writer.write(")  VALUES( ");

			bfirst = true;
			for (int row = 0; row < m_importmodel.getRowCount(); row++) {
				ColumnHandler handler = m_importmodel.getColumnHandler(row);
				String output = handler.getOutput(index);
				if (output.length() == 0 || output.equals("null")) {

				} else {
					if (bfirst)
						bfirst = false;
					else
						writer.write(", ");

					writer.write(output);
				}
			}
			writer.write(" );\n");
			writeRow(writer, index);
		}
	}

	void writeRow(Writer writer, int index) throws SQLException {
		if (m_directimport) {
			StringWriter sw = (StringWriter) writer;
			String sql = sw.toString();

			System.out.println("writing to database: index = " + index + "  sql = " + sql);
			Connection conn = getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
			stmt.close();
			sw.getBuffer().setLength(0);
		}
	}

	/**
	 * Writes a sample SQL to the console
	 * 
	 */
	public class PreviewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				StringWriter writer = new StringWriter();
				start(writer, 5);
				System.out.println(writer.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Selects a file to write the IMPORT sql to
	 * 
	 */
	public class SelectFileAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JFileChooser fc = new JFileChooser();
			GenericFileFilter filter = new GenericFileFilter();
			filter.addExtension("txt");
			filter.addExtension("sql");
			filter.setDescription("SQL & Text Files");
			fc.setFileFilter(filter);
			// File homedir = new File( "test" );
			// chooser.setCurrentDirectory( homedir );
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				m_importpanel.setFile(f);
			}
		}
	}

	/**
	 * Starts the import
	 * 
	 */
	public class StartImportAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				JCheckBox cbox = (JCheckBox) m_importpanel.getComponentByName(ImportPanel.ID_WRITE_TO_FILE);
				if (cbox.isSelected()) {
					JTextField field = (JTextField) m_importpanel.getComponentByName(ImportPanel.ID_FILE_FIELD);
					String filename = (String) field.getText();
					if (filename.trim().length() == 0) {
						JOptionPane.showMessageDialog(null, "Invalid file name", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						File f = new File(filename);
						// BufferedOutputStream bos = new BufferedOutputStream(
						// new FileOutputStream(f) );
						OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(f));
						BufferedWriter bw = new BufferedWriter(ow);
						start(bw, m_importpanel.getInstances());
						bw.flush();
						bw.close();
					}
				} else {
					m_directimport = true;
					StringWriter writer = new StringWriter();
					start(writer, m_importpanel.getInstances());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
