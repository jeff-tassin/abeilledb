package com.jeta.abeille.gui.export;

import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.sql.ResultSetMetaData;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import com.jeta.abeille.gui.queryresults.QueryResultsModel;
import com.jeta.abeille.gui.sql.SQLComponent;
import com.jeta.abeille.gui.sql.SQLUtils;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.table.export.DecoratorBuilder;
import com.jeta.foundation.gui.table.export.ExportDecorator;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the ExportFrame
 * 
 * @author Jeff Tassin
 */
public class ExportFrameController extends TSController {
	/**
	 * The frame window we are controlling.
	 */
	private ExportFrame m_frame;

	/**
	 * The object that we use to start, stop, and get status information for an
	 * active export.
	 */
	private ExportMediator m_mediator;

	/**
	 * ctor
	 */
	public ExportFrameController(ExportFrame frame) {
		super(frame.getExportPanel());
		m_frame = frame;
		assignAction(ExportNames.ID_RESET, new ResetAction());
		assignAction(ExportNames.ID_SQL_FORMAT, new SQLFormatAction());
		assignAction(ExportNames.ID_START_EXPORT, new StartExportAction());
		assignAction(ExportNames.ID_STOP_EXPORT, new StopExportAction());
		assignAction(ExportNames.ID_SAMPLE_OUTPUT, new ShowOutputAction());
		assignAction(ExportNames.ID_CLOSE, new CloseFrameAction());

		ExportFrameUIDirector uidirector = new ExportFrameUIDirector(m_frame.getExportPanel());
		m_frame.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Called by the mediator when the export operation has been completed
	 */
	void exportCompleted() {
		Runnable gui_update = new Runnable() {
			public void run() {
				String msg = I18N.getLocalizedMessage("Export Completed");
				m_frame.setStatusMessage(msg);
			}
		};
		SwingUtilities.invokeLater(gui_update);

	}

	/**
	 * Called by the mediator if an exception is throw during the export.
	 */
	void exportError(Exception e) {
		e.printStackTrace();
	}

	/**
	 * Called by the mediator when a row is written during the export. Here, we
	 * update the status bar in the frame.
	 */
	void exportStatus(int row) {
		final int lrow = row;
		Runnable gui_update = new Runnable() {
			public void run() {
				String msg = I18N.format("exported_row_1", new Integer(lrow));
				m_frame.setStatusMessage(msg);
			}
		};
		SwingUtilities.invokeLater(gui_update);
	}

	/**
	 * Invoked if the export was explicitly stopped by the user
	 */
	void exportStopped() {
		Runnable gui_update = new Runnable() {
			public void run() {
				String msg = I18N.getLocalizedMessage("Export Canceled");
				m_frame.setStatusMessage(msg);
			}
		};
		SwingUtilities.invokeLater(gui_update);
	}

	/**
	 * closes the frame
	 */
	public class CloseFrameAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSWorkspaceFrame.getInstance().disposeFrame(m_frame);
		}
	}

	/**
	 * Sets the export settings to default
	 */
	public class ResetAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			ExportPanel panel = m_frame.getExportPanel();
			panel.reset();
		}
	}

	/**
	 * Sets the export settings to export the data as SQL
	 */
	public class SQLFormatAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			ExportPanel panel = m_frame.getExportPanel();
			panel.setSQLFormat();
		}
	}

	/**
	 * This action starts the export of the query results to a file
	 */
	public class StartExportAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			ExportPanel view = m_frame.getExportPanel();
			view.saveToModel();
			if (validateInput()) {
				try {
					SQLExportModel exportmodel = view.getModel();
					if (view.isExportToClipboard()) {
						StringWriter writer = new StringWriter();
						DecoratorBuilder decbuilder = new QueryExportBuilder(-1);
						ExportDecorator decorator = decbuilder.build(exportmodel);
						TableModel tablemodel = exportmodel.getSelection();
						decorator.write(writer, tablemodel, 0, 0);

						Toolkit kit = Toolkit.getDefaultToolkit();
						Clipboard clipboard = kit.getSystemClipboard();
						StringSelection ss = new StringSelection(writer.toString());
						clipboard.setContents(ss, ss);
					} else if (view.isExportToFile()) {
						String filename = exportmodel.getFileName();
						if (filename != null && filename.length() > 0) {
							File f = new File(filename);
							Writer writer = new BufferedWriter(new FileWriter(f));
							m_mediator = new ExportMediator(ExportFrameController.this, writer, exportmodel);
						} else {
							String msg = I18N.getLocalizedMessage("Invalid File Name");
							String title = I18N.getLocalizedMessage("Error");
							JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
						}
					}
				} catch (Exception e) {
					TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, m_frame, true);
					dlg.initialize(I18N.getLocalizedMessage("Export Failed"), e);
					dlg.setSize(dlg.getPreferredSize());
					dlg.showCenter();
				}
			}
		}
	}

	/**
	 * Stops a currently running export operation
	 */
	public class StopExportAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			if (m_mediator != null)
				m_mediator.stopExport();
		}
	}

	/**
	 * This action iterates over 5 rows in the result set and creates a sample
	 * output dialog so the user can examine the effects of formatting.
	 */
	public class ShowOutputAction extends AbstractAction {
		public void actionPerformed(ActionEvent evt) {
			ExportPanel panel = m_frame.getExportPanel();
			if (validateInput()) {
				StringWriter writer = new StringWriter();
				panel.saveToModel();
				SQLExportModel exportmodel = panel.getModel();

				DecoratorBuilder decbuilder = new QueryExportBuilder(5);
				ExportDecorator decorator = decbuilder.build(exportmodel);
				try {

					TableModel tablemodel = exportmodel.getSelection();

					decorator.write(writer, tablemodel, 0, 0);

					SQLComponent sqlcomp = SQLUtils.createSQLComponent(null);

					// JEditorPane editor = TSEditorUtils.createEditor(
					// com.jeta.abeille.gui.sql.SQLKit.class, null );
					// JComponent comp = TSEditorUtils.getExtComponent( editor
					// );
					JEditorPane editor = sqlcomp.getEditor();
					JComponent comp = sqlcomp.getExtComponent();
					editor.setText(writer.toString());

					JPanel editorpanel = new JPanel(new BorderLayout());
					editorpanel.add(comp, BorderLayout.CENTER);
					TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
					dlg.setTitle(I18N.getLocalizedMessage("Export Preview"));
					dlg.showCloseLink();
					dlg.setPrimaryPanel(editorpanel);
					dlg.setSize(600, 400);
					dlg.showCenter();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Checks the data entered by the user to make sure it is valid.
	 * 
	 * @return true if the entered data is valid, false otherwise
	 */
	boolean validateInput() {
		return true;
	}
}
