package com.jeta.abeille.gui.sql;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;

import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.Utilities;

import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtUtilities;
import org.netbeans.editor.ext.ExtKit;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;
import com.jeta.abeille.database.model.TSResultSet;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.gui.common.TableSelectorPanel;
import com.jeta.abeille.gui.main.MainFrameNames;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSEvent;
import com.jeta.foundation.componentmgr.TSNotifier;
import com.jeta.foundation.gui.components.JETAFrameListener;
import com.jeta.foundation.gui.components.JETAFrameEvent;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.gui.editor.BufferEvent;
import com.jeta.foundation.gui.editor.BufferMgr;
import com.jeta.foundation.gui.editor.EditorController;
import com.jeta.foundation.gui.editor.EditorDropListener;
import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.gui.editor.KitInfo;
import com.jeta.foundation.gui.editor.TSEditorMgr;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSKit;
import com.jeta.foundation.gui.editor.TSPlainKit;
import com.jeta.foundation.gui.editor.TSTextNames;
import com.jeta.foundation.gui.editor.macros.Macro;
import com.jeta.foundation.gui.editor.macros.MacroMgr;
import com.jeta.foundation.gui.editor.macros.MacroModel;
import com.jeta.foundation.gui.editor.macros.RunMacroAction;

import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.license.LicenseManager;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is the controller for the SQLFrame window
 * 
 * @author Jeff Tassin
 */
public class SQLController extends EditorController implements JETAFrameListener, SQLMediatorListener {
	private TSUserProperties m_settings; // user settings that affect various
											// operations
	private SQLResultsFrame m_resultsframe = null;
	private TSConnection m_connection;
	private SQLFrame m_frame;

	private boolean m_createsqlbuff = false;

	/** the drop listener for drag-drop */
	EditorDropListener m_droplistener = new SQLEditorDropListener(this);

	/**
	 * ctor
	 */
	public SQLController(TSConnection connection, SQLFrame frame, BufferMgr bufferMgr) {
		super(frame, bufferMgr);
		m_frame = frame;
		m_connection = connection;

		ExecuteSQLAction sqlexec = new ExecuteSQLAction(false);
		assignAction(SQLNames.ID_EXECUTE_ALL, sqlexec);
		assignAction(SQLKit.executeSQLAction, sqlexec);
		assignAction(SQLNames.ID_EXECUTE_CURRENT, new ExecuteCurrentAction(true));
		assignAction(SQLNames.ID_EXECUTE_CURRENT_ALL, new ExecuteCurrentAction(false));
		assignAction(SQLNames.ID_STOP, new StopAction());

		assignAction(SQLNames.ID_RUN_INSERT_MACRO, new RunInsertMacro());
		assignAction(SQLNames.ID_RUN_UPDATE_MACRO, new RunUpdateMacro());
		assignAction(SQLNames.ID_RUN_DELETE_MACRO, new RunDeleteMacro());

		assignAction(SQLKit.executeSQLActionNewWindow, new ExecuteSQLAction(true));
		assignAction(SQLKit.sqlHistoryPrevAction, new PrevSQLAction());
		assignAction(SQLKit.sqlHistoryNextAction, new NextSQLAction());
		assignAction(SQLKit.selectIntoSQLBufferAction, new SelectSQLAction());
		assignAction(SQLKit.tableNameCompletionAction, new TableCompletionAction());
		assignAction(TSKit.insertTabAction, new CompletionAction());
		assignAction(SQLKit.forceTabAction, new ForceTabAction());
		assignAction(TSKit.deletePrevCharAction, new DeletePreviousCharAction());
		assignAction(SQLNames.ID_RESET_CONNECTION, new ResetConnectionAction());
		assignAction(SQLNames.ID_CLEAR_BUFFER, new ClearAction());
		// assignAction( SQLNames.ID_SELECT_INTO_SQL, new SelectSQLAction() );
		assignAction(SQLKit.newSQLBuffer, new NewSQLBufferAction());
		assignAction(FrameKit.newBufferAction, new NewBufferAction());

		assignAction(TSTextNames.ID_PREFERENCES, new SQLPreferencesAction2());

		assignAction(SQLKit.openFileIntoSQLBufferAction, new OpenFileIntoSQLAction());
		assignAction(SQLKit.openFileAsNewSQLBufferAction, new OpenFileAsNewSQLAction());

		// override default kill action and ignore if the current buffer is the
		// sql buffer
		assignAction(FrameKit.closeFileAction, new SQLKillBufferAction());

		assignAction(SQLKit.showTableProperties, new NavigateWindowAction(MainFrameNames.ID_TABLE_PROPERTIES));
		assignAction(SQLKit.showModelView, new NavigateWindowAction(MainFrameNames.ID_MODEL_VIEW));
		assignAction(SQLKit.showObjectTree, new NavigateWindowAction(MainFrameNames.ID_OBJECT_TREE));
		assignAction(SQLKit.showSystemInfo, new NavigateWindowAction(MainFrameNames.ID_DRIVER_INFO));
		assignAction(SQLKit.switchConnections, new NavigateWindowAction(MainFrameNames.ID_TOGGLE_CONNECTION));

		SQLFrameUIDirector uidirector = new SQLFrameUIDirector(m_frame);
		m_frame.setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Override the buffer creation process so we can use a SQLBuffer instead.
	 */
	protected Buffer createBuffer(Class kitClass) {
		if (m_createsqlbuff) {
			return new SQLBuffer(m_connection);
		} else
			return super.createBuffer(kitClass);
	}

	/**
	 * Creates a new instances of a SQL buffer
	 */
	SQLBuffer createSQLBuffer() {
		try {
			m_createsqlbuff = true;
			SQLKit kit = new SQLKit(m_connection);
			SQLBuffer buffer = (SQLBuffer) createNew(kit, true);
			updateUI();

			org.netbeans.editor.StatusBar sb = org.netbeans.editor.Utilities.getEditorUI(buffer.getEditor())
					.getStatusBar();
			String[] ws = new String[1];
			ws[0] = "Max Rows: #####";
			sb.addCell(0, "max.rows", ws);

			ws = new String[1];
			ws[0] = "Auto Commit: ####";
			sb.addCell(0, "auto.commit", ws);

			buffer.updateStatus();
			return buffer;
		} finally {
			m_createsqlbuff = false;
		}
	}

	/**
	 * @return the current editor from the buffer manager
	 */
	public JEditorPane getCurrentEditor() {
		Buffer buff = getBufferMgr().getCurrentBuffer();
		if (buff != null)
			return buff.getEditor();
		else
			return null;
	}

	/**
	 * @return the drop listener for drag-drop
	 */
	protected EditorDropListener getDropListener() {
		return m_droplistener;
	}

	/**
	 * @return the underlying history object
	 */
	SQLHistory getHistory() {
		return m_frame.getHistory();
	}

	/**
	 * @return true if a new query results window should be created every time
	 */
	private boolean isNewResultsWindow() {
		javax.swing.JRadioButton btn = (javax.swing.JRadioButton) m_frame
				.getComponentByName(SQLNames.ID_RESULTS_WINDOW_MODE);
		if (btn == null)
			return false;
		else
			return btn.isSelected();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////
	// internal frame listener implementation
	// we implement to support sending results to the same query window, if the
	// user closes the window
	// then we need to know about it

	public void jetaFrameActivated(JETAFrameEvent e) {
	}

	public void jetaFrameClosed(JETAFrameEvent e) {
		if (e.getSource() == m_resultsframe) {
			m_resultsframe = null;
		} else {
			assert (false);
		}
	}

	public void jetaFrameClosing(JETAFrameEvent e) {
		if (e.getSource() == m_resultsframe) {
			m_resultsframe = null;
		}
	}

	public void jetaFrameDeactivated(JETAFrameEvent e) {
	}

	public void jetaFrameDeiconified(JETAFrameEvent e) {
	}

	public void jetaFrameIconified(JETAFrameEvent e) {
	}

	public void jetaFrameOpened(JETAFrameEvent e) {
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * We override from EditorController so we can check of the buffer to kill
	 * is a SQLBuffer. If this is the case, and there is only one SQLBuffer
	 * opened, then we ignore the command. There is always at least one SQL
	 * window opened.
	 * 
	 * @param buff
	 *            the buffer to delete(close)
	 */
	public void killBuffer(Buffer buff) {
		if (buff instanceof SQLBuffer) {
			BufferMgr buffmgr = getBufferMgr();

			int sqlbuffcount = m_frame.getSQLBufferCount();
			if (sqlbuffcount > 1)
				buffmgr.deleteBuffer(buff);
			else {
				buff.getEditor().getToolkit().beep();
			}
		} else
			super.killBuffer(buff);
	}

	/**
	 * @return the kit info for the given extension. We override from
	 *         EditorController to default to the sql editor for most file types
	 */
	protected KitInfo getKitInfo(String ext) {
		KitInfo info = TSEditorMgr.getKitInfo(ext);
		if (info == null || info.getKitClass() == TSPlainKit.class)
			return TSEditorMgr.getKitInfo(SQLKit.class);
		else
			return info;
	}

	/**
	 * This is an internal helper routine that prepares the sql results window
	 * to display a query. This routine checks if we should reuse the results
	 * window and handles this case accordingly.
	 * 
	 * @param stmt
	 *            the statement that created the result set
	 * @param rset
	 *            the result set for the query
	 * @param sql
	 *            the actual unprocessed sql command
	 */
	protected void prepareResultsFrame(SQLMediator mediator) {

		TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();

		if (isNewResultsWindow()) {
			m_resultsframe = (SQLResultsFrame) TSWorkspaceFrame.getInstance().createInternalFrame(
					com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, m_connection.getId());
			m_resultsframe.setSize(m_resultsframe.getPreferredSize());
			wsframe.addWindow(m_resultsframe, false);
		} else {
			if (m_resultsframe == null || !m_resultsframe.isVisible()) {
				m_resultsframe = (SQLResultsFrame) TSWorkspaceFrame.getInstance().createInternalFrame(
						com.jeta.abeille.gui.sql.SQLResultsFrame.class, false, m_connection.getId());
				m_resultsframe.setSize(m_resultsframe.getPreferredSize());
				wsframe.addWindow(m_resultsframe, false);
			}
		}

		Object[] params = new Object[3];
		params[0] = m_connection;
		params[1] = mediator.getResultsManager();
		params[2] = m_frame;
		m_resultsframe.initializeModel(params);

		m_resultsframe.removeFrameListener(this);
		m_resultsframe.addFrameListener(this);
		wsframe.centerWindow(m_resultsframe);
		wsframe.show(m_resultsframe);

		// System.out.println ("SQLController.showFrame:  visible: " +
		// m_resultsframe.getDelegate().isVisible() );
	}

	/**
	 * SQLMediatorListener implementation. Shows the results of the given query
	 * by launching the sql results frame
	 * 
	 * @param mediator
	 *            the sql mediator that ran the command
	 */
	public void notifyEvent(SQLMediatorEvent evt) {
		SQLMediator mediator = evt.getMediator();
		SQLBuffer buffer = (SQLBuffer) mediator.getSQLSource();

		if (evt.getID() == SQLMediatorEvent.ID_TIME_EVENT) {
			long tm = evt.getElapsedTime();
			if (tm > 0) {
				int hours = (int) (tm / 3600000);
				int mins = (int) (tm / 60000 - hours * 60);
				int secs = (int) (tm / 1000 - (hours * 3600 + mins * 60));

				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, hours);
				c.set(Calendar.MINUTE, mins);
				c.set(Calendar.SECOND, secs);

				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				try {
					int line_number = Utilities.getLineOffset((BaseDocument) buffer.getEditor().getDocument(),
							mediator.getStartPos() + 1) + 1;
					String msg = I18N.format("processing_line_2", new Integer(line_number), format.format(c.getTime()));
					org.netbeans.editor.Utilities.setStatusText(buffer.getEditor(), msg);
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
		} else if (evt.getID() == SQLMediatorEvent.ID_SQL_STATEMENT_PROCESSED) {
			JTextComponent textcomp = buffer.getEditor();
			textcomp.setCaretPosition(evt.getEndPos());
			textcomp.moveCaretPosition(evt.getStartPos());
		} else if (evt.getID() == SQLMediatorEvent.ID_COMMAND_FINISHED) {
			try {
				buffer.setEnabled(true);
				buffer.setEditable(true);
				buffer.setBusy(false);
			} catch (Exception e) {
				TSUtils.printException(e);
			}

			JTextComponent textcomp = buffer.getEditor();
			BaseDocument doc = (BaseDocument) textcomp.getDocument();

			/**
			 * select the SQL statement that was just executed if we are
			 * executing a single statement or an error ocurred
			 */
			int status_code = mediator.getResult();
			if (status_code == SQLMediator.SUCCESS || status_code == SQLMediator.ERROR) {
				int endpos = evt.getEndPos();
				if (mediator.isStep() || status_code == SQLMediator.ERROR) {
					textcomp.setCaretPosition(evt.getStartPos());
				}

				try {
					if (org.netbeans.editor.Utilities.isEOL(doc, endpos)) {
						/** set caret to next line */
						if (endpos < doc.getLength()) {
							endpos = endpos + 1;
						}
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}

				if (mediator.isStep() || status_code == SQLMediator.ERROR) {
					textcomp.moveCaretPosition(endpos);
				} else {
					textcomp.setCaretPosition(endpos);
				}
			}

			if (mediator.getResult() == SQLMediator.SUCCESS) {

				if (mediator.hasResults()) {
					prepareResultsFrame(mediator);
					String msg = I18N.getLocalizedMessage("Statement Executed Successfully");
					org.netbeans.editor.Utilities.setStatusText(buffer.getEditor(), msg);
				} else {

					String msg = I18N.getLocalizedMessage("Statement Executed Successfully");
					org.netbeans.editor.Utilities.setStatusText(buffer.getEditor(), msg);
					if (getCurrentEditor() == buffer.getEditor())
						buffer.getEditor().requestFocus();
				}
				getHistory().add(buffer.getText());
				updateComponents(null);
			} else if (mediator.getResult() == SQLMediator.ERROR) {
				// check if we need to auto rollback on exception
				TSDatabase db = (TSDatabase) m_connection.getImplementation(TSDatabase.COMPONENT_ID);
				boolean brollback = db.rollbackOnException();
				if (brollback) {
					buffer.resetConnection();
				}

				SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, m_frame, true);

				int line_number = 0;
				try {
					line_number = Utilities.getLineOffset((BaseDocument) buffer.getEditor().getDocument(),
							evt.getStartPos() + 1) + 1;
				} catch (Exception e) {
					TSUtils.printException(e);
				}

				String error_caption = I18N.format("SQL_Exception_1", new Integer(line_number));
				dlg.initialize(mediator.getException(), mediator.getLastSQL(), brollback);
				dlg.setSize(dlg.getPreferredSize());
				dlg.setErrorCaption(error_caption);

				org.netbeans.editor.Utilities.setStatusText(buffer.getEditor(), error_caption);

				dlg.showCenter();
				updateUI();

				buffer.getEditor().requestFocus();
				getHistory().add(buffer.getText());
				updateComponents(null);

			} else if (mediator.getResult() == SQLMediator.CANCELED) {
				buffer.resetConnection();
				String msg = I18N.getLocalizedMessage("SQL Command Canceled");
				org.netbeans.editor.Utilities.setStatusText(buffer.getEditor(), msg);
				buffer.getEditor().requestFocus();
				updateComponents(null);
			} else if (mediator.getResult() == SQLMediator.COMMAND_NOT_FOUND) {
				buffer.getEditor().requestFocus();

				org.netbeans.editor.Utilities.setStatusText(buffer.getEditor(), "");
				updateComponents(null);

			}
		}
	}

	/**
	 * Starts processing the query in a worker thread
	 */
	public void startQuery(boolean newWindow) {
		try {
			SQLBuffer buffer = (SQLBuffer) getCurrentBuffer();

			assert (!buffer.isBusy());

			buffer.setEnabled(false);
			buffer.setEditable(false);
			buffer.setBusy(true);

			SQLMediator mediator = new SQLMediator(buffer.getConnectionReference(), buffer, this);
			mediator.start();
		} catch (SQLException sqe) {
			SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class,
					(SQLFrame) getFrameWindow(), true);
			dlg.initialize((SQLException) sqe, null);
			dlg.showCenter();
		} catch (Exception e) {
			SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class,
					(SQLFrame) getFrameWindow(), true);
			dlg.initialize(e, null);
			dlg.showCenter();
		}
	}

	/**
	 * Executes the current SQL statement in the buffer
	 */
	public void executeCurrent(boolean single_statement) {
		try {
			SQLBuffer buffer = (SQLBuffer) getCurrentBuffer();
			JTextComponent target = buffer.getEditor();
			Caret caret = target.getCaret();
			int startpos = caret.getDot();

			buffer.setEnabled(false);
			buffer.setEditable(false);
			buffer.setBusy(true);

			SQLMediator mediator = new SQLMediator(buffer.getConnectionReference(), buffer, this);
			mediator.start(startpos, single_statement);
		} catch (Exception e) {
			SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class,
					(SQLFrame) getFrameWindow(), true);
			dlg.initialize(e, null);
			dlg.showCenter();
		}

	}

	/**
	 * Clears the current buffer
	 */
	public class ClearAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buff = getBufferMgr().getCurrentBuffer();
			if (buff != null) {
				buff.clear();
				invokeAction(ExtKit.escapeAction);
			}
		}
	}

	/**
	 * Executes the current SQL command
	 */
	public class ExecuteCurrentAction implements ActionListener {
		private boolean m_single_statement;

		public ExecuteCurrentAction(boolean single) {
			m_single_statement = single;
		}

		public void actionPerformed(ActionEvent evt) {
			LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
			if (jlm.isEvaluation()) {
				jlm.postMessage(I18N.getLocalizedMessage("Feature disabled in evaluation version"));
				return;
			}
			executeCurrent(m_single_statement);
		}
	}

	/**
	 * This action executes the SQL statements found in the query. The work is
	 * done in a background thread
	 */
	public class ExecuteSQLAction implements ActionListener {
		private boolean m_newwindow = false;

		public ExecuteSQLAction(boolean newWindow) {
			m_newwindow = newWindow;
		}

		public void actionPerformed(ActionEvent evt) {
			Buffer buff = getBufferMgr().getCurrentBuffer();
			if (buff instanceof SQLBuffer) {
				JEditorPane editor = buff.getEditor();
				if (editor != null) {
					String sqlcmd = editor.getText().trim();
					if (sqlcmd.length() > 0) {
						startQuery(m_newwindow);
					}
				}
			}
		}
	}

	public class SQLKillBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buff = m_frame.getCurrentBuffer();
			killBuffer(buff);
			m_frame.syncTabs();
		}
	}

	/**
	 * Handle the new command. Creates a new, empty buffer
	 */
	public class NewBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			createNew(SQLKit.class, true);
		}
	}

	/**
	 * Creates a new SQL buffer
	 */
	public class NewSQLBufferAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			createSQLBuffer();
		}
	}

	/**
	 * Places the next sql statement from the history buffer into the sql
	 * window. If there are no next statements, the call does nothing
	 */
	public class NextSQLAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buff = getBufferMgr().getCurrentBuffer();
			if (buff instanceof SQLBuffer) {
				String txt = getHistory().next();
				if (txt != null) {
					JEditorPane editor = buff.getEditor();
					if (editor != null) {
						editor.setText(txt);
						editor.setCaretPosition(0);
					}

					buff.getContentPanel().validate();
					buff.getContentPanel().repaint();
				}
			}
		}
	}

	/**
	 * Opens a file into a new sql buffer
	 */
	public class OpenFileAsNewSQLAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			File f = TSFileChooserFactory.showOpenDialog();
			if (f != null) {
				SQLBuffer buff = createSQLBuffer();
				loadFile(buff, f);
				buff.getContentPanel().validate();
				buff.getContentPanel().repaint();

				org.netbeans.editor.Utilities.requestFocus(buff.getEditor());
			}
		}
	}

	/**
	 * Opens a file into the current sql buffer
	 */
	public class OpenFileIntoSQLAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Buffer buff = m_frame.getCurrentBuffer();
			if (buff instanceof SQLBuffer) {
				File f = TSFileChooserFactory.showOpenDialog();
				if (f != null) {
					openFileIntoBuffer(f, buff);
				}
			}
		}
	}

	/**
    */
	public class RunInsertMacro implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			MacroModel macros = MacroMgr.getMacroModel(SQLKit.class);
			Macro macro = macros.get("sql-insert-template");
			if (macro != null) {
				Buffer buff = m_frame.getCurrentBuffer();
				RunMacroAction action = new RunMacroAction(macro);
				action.actionPerformed(evt, buff.getEditor());
				buff.getEditor().requestFocus();
			}
		}
	}

	/**
    */
	public class RunUpdateMacro implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			MacroModel macros = MacroMgr.getMacroModel(SQLKit.class);
			Macro macro = macros.get("sql-update-template");
			if (macro != null) {
				Buffer buff = m_frame.getCurrentBuffer();
				RunMacroAction action = new RunMacroAction(macro);
				action.actionPerformed(evt, buff.getEditor());
				buff.getEditor().requestFocus();
			}
		}
	}

	/**
    */
	public class RunDeleteMacro implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			MacroModel macros = MacroMgr.getMacroModel(SQLKit.class);
			Macro macro = macros.get("sql-delete-template");
			if (macro != null) {
				Buffer buff = m_frame.getCurrentBuffer();
				RunMacroAction action = new RunMacroAction(macro);
				action.actionPerformed(evt, buff.getEditor());
				buff.getEditor().requestFocus();
			}
		}
	}

	/**
	 * Places the previous sql statement from the history buffer into the sql
	 * window. If there are no previous statements, then the call does nothing
	 */
	public class PrevSQLAction implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			Buffer buff = m_frame.getCurrentBuffer();
			if (buff instanceof SQLBuffer) {
				String txt = getHistory().previous();
				if (txt != null) {
					JEditorPane editor = buff.getEditor();
					if (editor != null) {
						editor.setText(txt);
						editor.setCaretPosition(0);
					}

					buff.getContentPanel().validate();
					buff.getContentPanel().repaint();
				}
			}
		}
	}

	/**
	 * Resets the underlying database connection. This allows the user to
	 * recover somewhat in case the JDBC driver has problems (which can occur
	 * with the current postgresql driver)
	 */
	public class ResetConnectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			BufferMgr buffmgr = getBufferMgr();
			Buffer buff = buffmgr.getCurrentBuffer();
			if (buff instanceof SQLBuffer) {
				SQLBuffer sqlbuff = (SQLBuffer) buff;
				sqlbuff.resetConnection();
			}
		}
	}

	private class SQLPreferencesAction2 implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			SQLPreferences prefs = new SQLPreferences(m_connection);
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			dlg.setTitle(I18N.getLocalizedMessage("Preferences"));
			dlg.setPrimaryPanel(prefs.getView());
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				prefs.apply();
			}
		}
	}

	/**
	 * Copies the selected text into the first available SQL buffer
	 */
	public class SelectSQLAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			BufferMgr buffmgr = getBufferMgr();
			Buffer buff = buffmgr.getCurrentBuffer();
			if (buff instanceof SQLBuffer) {
				buff.getEditor().getToolkit().beep();
			} else {
				SQLBuffer sqlbuff = null;
				// String txt = buff.getEditor().getSelectedText();
				String txt = buff.getEditor().getText();
				if (txt != null && txt.length() > 0) {
					// find the first SQLBuffer that is not busy
					Collection c = buffmgr.getBuffers();
					Iterator iter = c.iterator();
					while (iter.hasNext()) {
						Buffer ibuff = (Buffer) iter.next();
						if (ibuff instanceof SQLBuffer && !((SQLBuffer) ibuff).isBusy()) {
							sqlbuff = (SQLBuffer) ibuff;
							break;
						}
					}
				}

				if (sqlbuff == null)
					buff.getEditor().getToolkit().beep();
				else {
					JEditorPane editor = sqlbuff.getEditor();
					editor.setText(txt);
					buffmgr.selectBuffer(sqlbuff);
					editor.setCaretPosition(0);
				}
			}
		}
	}

	/**
    */
	public class NavigateWindowAction implements ActionListener {
		private String m_frame_cmd;

		public NavigateWindowAction(String frameCmd) {
			m_frame_cmd = frameCmd;
		}

		public void actionPerformed(ActionEvent evt) {
			TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
			JETAController controller = wsframe.getController();
			if (controller != null)
				controller.invokeAction(m_frame_cmd);
		}
	}

	/**
    *
    */
	public static class ForceTabAction implements ActionListener {
		public ForceTabAction() {
			// super( SQLKit.forceTabAction, BaseAction.MAGIC_POSITION_RESET |
			// BaseAction.ABBREV_RESET | BaseAction.WORD_MATCH_RESET);
		}

		public void actionPerformed(ActionEvent evt) {
			System.out.println("Forcetabaction1");
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			System.out.println("Forcetabaction");
		}
	}

	/**
	 * Override the TAB key and provide completion when it is pressed. Tries to
	 * complete the typed text to the left of the caret with a
	 * tablename,schemaname,catalogname, or column name.
	 */
	public static class CompletionAction extends ExtKit.ExtInsertTabAction {
		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			Completion completion = ExtUtilities.getCompletion(target);
			if (completion instanceof SQLCompletion) {
				final SQLCompletion sqlcomp = (SQLCompletion) completion;
				SQLCompletionQuery query = (SQLCompletionQuery) sqlcomp.createQuery();
				query.reset();
				if (sqlcomp.isPaneVisible()) {
					super.actionPerformed(evt, target);
				} else {
					/**
					 * only do completion if the previous character is a valid
					 * character and not a space/newline/tab
					 */
					boolean binserttab = false;
					try {
						Caret caret = target.getCaret();
						int startpos = caret.getDot();
						if (startpos > 0) {
							BaseDocument doc = (BaseDocument) target.getDocument();
							char[] cc = doc.getChars(startpos - 1, 1);
							if (cc != null && cc.length == 1) {
								if (Character.isWhitespace(cc[0])) {
									binserttab = true;
								}
							}
						} else if (startpos == 0) {
							binserttab = true;
						}
					} catch (Exception e) {
						// eat it here
					}

					if (binserttab) {
						super.actionPerformed(evt, target);
					} else {
						completion.popup(false);
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								((SQLCompletionPopup) sqlcomp.getPane()).ensureIndexIsVisible();
							}
						});
					}
				}
			} else {
				assert (false);
			}
		}
	}

	/**
	 * Delete the previous character. We override the default action because we
	 * need to check if a completion popup is visible. If a popup is visible and
	 * is to the right of the caret, we move the popup to the same x-location as
	 * the caret.
	 */
	public static class DeletePreviousCharAction extends TSKit.DeleteCharAction {
		public DeletePreviousCharAction() {
			super(TSKit.deletePrevCharAction, false);
		}

		private void dumpParent(int index, Component parent) {
			if (parent != null)
				System.out.println("dumping parent: " + index + "  parent: " + parent.getClass() + "  bounds: "
						+ parent.getBounds());
			else
				System.out.println("dumping parent: " + index + "  parent: null");
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			try {
				Completion completion = ExtUtilities.getCompletion(target);
				if (completion instanceof SQLCompletion) {
					SQLCompletion sqlcomp = (SQLCompletion) completion;
					com.jeta.foundation.gui.components.PopupList popup = sqlcomp.getPopup();
					if (popup != null && popup.isVisible()) {
						/*
						 * Caret caret = target.getCaret(); int startpos =
						 * caret.getDot(); java.awt.Rectangle r =
						 * target.modelToView( startpos ); System.out.println(
						 * "SQLController.back  invoker: " + popup.getInvoker()
						 * + "   target== invoker: " + (popup.getInvoker() ==
						 * target) ); java.awt.Point pt = new java.awt.Point(
						 * r.x, r.y ); SwingUtilities.convertPointToScreen( pt,
						 * target ); popup.setLocation( pt.x, pt.y );
						 * target.requestFocus(); target.repaint();
						 * 
						 * 
						 * Component parent = popup.getParent(); Component
						 * test_parent = popup;
						 * 
						 * for( int index=1; index < 8; index++ ) { dumpParent(
						 * index, test_parent ); if ( test_parent != null ) {
						 * test_parent = test_parent.getParent(); } }
						 * 
						 * java.awt.Point pt = SwingUtilities.convertPoint(
						 * target, r.x, 0, parent ); if ( parent.getX() > pt.x )
						 * { parent.setLocation( pt.x, parent.getY() );
						 * target.repaint(); }
						 */
					}
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
			super.actionPerformed(evt, target);
		}
	}

	/**
	 * Stops a currently executing SQL script
	 */
	public class StopAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			SQLBuffer buffer = (SQLBuffer) getCurrentBuffer();
			assert (buffer.isBusy());
			buffer.getMediator().cancel();
		}
	}

	/**
	 * Displays a popup of all tables in the database an allows the user to use
	 * completion for a table name
	 */
	public class TableCompletionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Completion completion = ExtUtilities.getCompletion((JTextComponent) evt.getSource());
			if (completion instanceof SQLCompletion) {
				SQLCompletion sqlcomp = (SQLCompletion) completion;
				SQLCompletionQuery query = (SQLCompletionQuery) sqlcomp.createQuery();
				query.setMode(SQLCompletionQuery.SHOW_TABLES);
				completion.popup(false);
			}
		}
	}

}
