package com.jeta.abeille.gui.sql;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;

import java.util.Map;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.Action;

import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.DefaultTableSelectorModel;

import com.jeta.abeille.gui.keyboard.KeyboardManager;
import com.jeta.abeille.gui.keyboard.KeyBindingsNames;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.gui.editor.KitInfo;
import com.jeta.foundation.gui.editor.TSKit;
import com.jeta.foundation.gui.editor.TSEditorMgr;
import com.jeta.foundation.gui.editor.macros.Macro;

import com.jeta.open.gui.utils.JETAToolbox;

/**
 * SQL editor kit with appropriate document
 * 
 * @author Jeff Tassin
 */

public class SQLKit extends TSKit {
	static final long serialVersionUID = -5445829962133684922L;

	/**
	 * The underlying database connection. This is needed for completions (so we
	 * can get the list of tables)
	 */
	private TSConnection m_connection;

	/**
	 * A reference to the completion object. We keep this around because it can
	 * be expensive to create everytime for databases with large numbers of
	 * tables. If the database model changes, call clearCache to reset this
	 * object.
	 */
	private SQLCompletion m_completion;

	static {
		try {
			KitInfo info = new KitInfo("text/x-java", SQLKit.class);
			info.setIcon(TSGuiToolbox.loadImage("sql16.gif"));
			TSEditorMgr.registerEditorKit(info);

			Settings.addInitializer(new SQLSettingsInitializer());
			Settings.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ctor
	 */
	public SQLKit(TSConnection connection) {
		m_connection = connection;
	}

	/**
	 * Clears any cached objects in this kit. This is needed if the database
	 * model changes. We reset any objects that depend on the database model so
	 * they can refresh themselves.
	 */
	public void clearCache() {
		m_completion = null;
	}

	public String getContentType() {
		return "text/x-java";
	}

	/**
	 * Create new instance of syntax coloring scanner
	 * 
	 * @param doc
	 *            document to operate on. It can be null in the cases the syntax
	 *            creation is not related to the particular document
	 */
	public Syntax createSyntax(Document doc) {
		return new SQLSyntax();
	}

	/** Create syntax support */
	public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
		return new SQLSyntaxSupport(doc);
	}

	public Completion createCompletion(ExtEditorUI extEditorUI) {
		// this can be null, when we are doing our own completion
		if (m_connection == null)
			return null;
		else {
			if (m_completion == null) {
				m_completion = new SQLCompletion(extEditorUI, new DefaultTableSelectorModel(m_connection), m_connection);
			}
			return m_completion;
		}
	}

	/** Create the formatter appropriate for this kit */
	public Formatter createFormatter() {
		return new SQLFormatter(this.getClass());
	}

	protected EditorUI createEditorUI() {
		//return new ExtEditorUI();
		return new SQLEditorUI(m_connection.isPROD());
	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		Action[] sqlactions = new Action[] { new FrameKit.FrameAction(SQLKit.sqlHistoryPrevAction),
				new FrameKit.FrameAction(SQLKit.sqlHistoryNextAction),
				new FrameKit.FrameAction(SQLKit.tableNameCompletionAction),
				new FrameKit.FrameAction(SQLKit.executeSQLAction),
				new FrameKit.FrameAction(SQLKit.executeSQLActionNewWindow),
				new FrameKit.FrameAction(SQLKit.openFileIntoSQLBufferAction),
				new FrameKit.FrameAction(SQLKit.openFileAsNewSQLBufferAction),
				new FrameKit.FrameAction(SQLKit.newSQLBuffer), new FrameKit.FrameAction(SQLKit.forceTabAction),
				new FrameKit.FrameAction(SQLKit.showTableProperties), new FrameKit.FrameAction(SQLKit.showObjectTree),
				new FrameKit.FrameAction(SQLKit.showModelView), new FrameKit.FrameAction(SQLKit.showSystemInfo),
				new FrameKit.FrameAction(SQLKit.switchConnections), new SQLController.CompletionAction(),
				new SQLController.DeletePreviousCharAction(), new DefaultKeyTypedAction() };

		return sqlactions;

	}

	/**
	 * @return the list default macros supported by this kit
	 */
	public static Macro[] listDefaultMacros() {
		Macro[] macros = new Macro[] {
				new Macro("sql-select-star", "\"select * from \""),
				new Macro("sql-insert-template",
						"\"INSERT INTO table_name [(column1 [, column2]...)] VALUES (value1, [,value2]...);\""),
				new Macro("sql-update-template",
						"\"UPDATE table_name SET column_name = expression [,column2_name = expression]... [WHERE search_condition];\""),
				new Macro("sql-delete-template", "\"DELETE FROM table_name [WHERE search_condition];\"") };
		return macros;
	}

	/**
	 * Get the default bindings. We are using a different method for
	 * initializing default bindings than the netbeans callback method.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		KeyboardManager kmgr = KeyboardManager.getInstance();

		MultiKeyBinding[] bindings = new MultiKeyBinding[] {
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK),
						SQLKit.sqlHistoryPrevAction),

				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK),
						SQLKit.sqlHistoryNextAction),

				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_MASK),
						SQLKit.tableNameCompletionAction),
				new MultiKeyBinding(
						KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK),
						"sql-select-star"),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK),
						SQLKit.executeSQLAction),

				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK),
						SQLKit.executeSQLActionNewWindow),

				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK),
						SQLKit.forceTabAction),

				new MultiKeyBinding(kmgr.getKeyStroke(KeyBindingsNames.ID_TABLE_PROPERTIES), SQLKit.showTableProperties),

				new MultiKeyBinding(kmgr.getKeyStroke(KeyBindingsNames.ID_OBJECT_TREE), SQLKit.showObjectTree),

				new MultiKeyBinding(kmgr.getKeyStroke(KeyBindingsNames.ID_MODEL_VIEW), SQLKit.showModelView),

				new MultiKeyBinding(kmgr.getKeyStroke(KeyBindingsNames.ID_SYSTEM_INFO), SQLKit.showSystemInfo),

				new MultiKeyBinding(kmgr.getKeyStroke(KeyBindingsNames.ID_SWITCH_CONNECTIONS), SQLKit.switchConnections)

		};
		return bindings;
	}

	/**
	 * Sets the database connection
	 */
	public void setConnection(TSConnection connection) {
		m_connection = connection;
	}

	public static class DefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
		private void _actionPerformed(ActionEvent evt, JTextComponent target) {
			super.actionPerformed(evt, target);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			/**
			 * this is a hack to fix a suble bug when the user presses CTRL+'.'
			 * and types the table name to completion. If we don't do this, the
			 * column popup is not displayed correctly.
			 */
			if (!JETAToolbox.isOSX()) {
				if (".".equals(evt.getActionCommand())) {
					Completion cpl = ExtUtilities.getExtEditorUI(target).getCompletion();
					if (cpl instanceof SQLCompletion) {
						SQLCompletion completion = (SQLCompletion) cpl;
						if (completion.isPaneVisible()) {
							SQLCompletionQuery cq = (SQLCompletionQuery) completion.createQuery();
							if (cq.isShowTables()) {
								completion.setPaneVisible(false);
								completion.cancelRequest();
								final ActionEvent fevt = evt;
								final JTextComponent ftarget = target;
								javax.swing.SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										_actionPerformed(fevt, ftarget);
									}
								});

								return;
							}
						}
					}
				}
			}
			super.actionPerformed(evt, target);
		}
	}

	public static final String sqlHistoryPrevAction = "sql-history-previous"; // inserts
																				// last
																				// sql
																				// command
																				// into
																				// buffer
	public static final String sqlHistoryNextAction = "sql-history-next"; // inserts
																			// next
																			// sql
																			// command
																			// into
																			// buffer
	public static final String columnNameCompletionAction = "column-name-completion";
	public static final String tableNameCompletionAction = "table-name-completion";
	public static final String completionAction = "completion-action";
	public static final String forceQuery = "force-query"; // causes a call to
															// executeQuery
															// regardless of
															// what's in buffer
	/** copies the selected text into the main sql buffer */
	public static final String selectIntoSQLBufferAction = "select-into-sql-buffer";
	public static final String openFileIntoSQLBufferAction = "open-file-into-sql-buffer";
	public static final String openFileAsNewSQLBufferAction = "open-file-as-new-sql-buffer";
	public static final String executeSQLAction = "execute-sql";
	public static final String executeSQLActionNewWindow = "execute-sql-new-results-window";
	public static final String newSQLBuffer = "new-sql-buffer";
	public static final String forceTabAction = "force-tab-action";

	public static final String showTableProperties = "show-table-properties-window";
	public static final String showObjectTree = "show-object-tree";
	public static final String showModelView = "show-model-view";
	public static final String showSystemInfo = "show-system-information";
	public static final String switchConnections = "switch-connections";

}
