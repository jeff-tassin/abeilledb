package com.jeta.abeille.gui.sql;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.ResultsManager;
import com.jeta.abeille.gui.sql.input.SQLInput;
import com.jeta.abeille.gui.sql.input.SQLInputDialog;
import com.jeta.abeille.gui.sql.input.SQLInputModel;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;
import org.netbeans.editor.BaseDocument;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class runs a sql command in a background thread. It interacts with the
 * buffer as it runs providing feedback. It is assumed that the mediator will
 * close any statement and result set
 * 
 * @author Jeff Tassin
 */
public class SQLThread {
	/** the database connection */
	private ConnectionReference m_connectionref;

	/** the sql statement (we reuse this object each time in the query loop) */
	private Statement m_statement;

	/** the result sets if the query returned anything */
	private ResultsManager m_resultsmgr;

	/** the document that contains the sql we are executing */
	private BaseDocument m_document;

	/**
	 * we allow passing a string instead of a BaseDocument. In this case, set
	 * the m_document to null and set this string to the SQL you wish to
	 * parse/execute
	 */
	private String m_sqlbuff;

	private char m_delimiter = SQLPreferences.getDelimiter();

	/**
	 * provides a layer between the mediator and this thread. Basically, moves
	 * some of the GUI complexity out of this class
	 */
	private SQLMediator m_mediator;

	/**
	 * this flag controls whether this thread should wait for user input before
	 * executing the next SQL statement. It is used primarily for debugging a
	 * SQL script
	 */
	private boolean m_step = false;

	/**
	 * If we are in step mode, this represents the location of the next SQL
	 * statement to execute
	 */
	private int m_startpos;

	/**
	 * Constructor
	 */
	public SQLThread(ConnectionReference cref, Statement stmt, BaseDocument doc, SQLMediator mediator, int startpos) {
		m_connectionref = cref;
		m_resultsmgr = new ResultsManager(cref.getTSConnection());
		m_statement = stmt;
		m_document = doc;
		m_mediator = mediator;
		m_step = false;
		m_startpos = startpos;
	}

	/**
	 * Runs the sql in the given string.
	 */
	public SQLThread(ConnectionReference cref, Statement stmt, String sqlbuff, SQLMediator mediator) {
		m_connectionref = cref;
		m_resultsmgr = new ResultsManager(cref.getTSConnection());
		m_statement = stmt;
		m_sqlbuff = sqlbuff;
		m_mediator = mediator;
		m_startpos = -1;
	}
	/**
	 * Runs the sql in the given string.
	 */
	public SQLThread(ConnectionReference cref, Statement stmt, String sqlbuff, SQLMediator mediator, char delimiter) {
		this(cref, stmt, sqlbuff, mediator);
		m_delimiter = delimiter;
	}

	/**
	 * Method to clean up any resources after the thread has completed or was
	 * canceled (The statement is closed by the caller)
	 */
	private void cleanup() {
		m_mediator = null;
		m_document = null;
	}

	/**
	 * @return the underlying document that contains the sql we are executing
	 */
	private BaseDocument getDocument() {
		return m_document;
	}

	private SQLDocumentParser createParser() throws Exception {
		if (m_document == null)
			return new SQLDocumentParser(m_connectionref.getTSConnection(), m_sqlbuff,  m_delimiter );
		else
			return new SQLDocumentParser(m_connectionref.getTSConnection(), m_startpos, m_document, m_delimiter );
	}

	/**
	 * @return a statement object used for executing sql
	 */
	private Statement getStatement() throws SQLException {
		return m_statement;
	}

	/**
	 * @return true if the user canceled this operation
	 */
	public boolean isCanceled() {
		return m_mediator.isCanceled();
	}

	/**
	 * @return tne flag controls whether this thread should wait for user input
	 *         before executing the next SQL statement. It is used primarily for
	 *         debugging a SQL script
	 */
	public boolean isStep() {
		return m_step;
	}

	/**
	 * Invokes the SQLInputView in a dialog. This allows the user to enter
	 * values for contraints found in the sql where clause. The SQLInput objects
	 * are associated with the question tokens found in the sql so that if the
	 * SQLInputModel is changed, the TokenInfo objects will be updated.
	 * 
	 * @param inputs
	 *            a collection of SQLInput objects
	 */
	private void requestInputsFromUser(Collection c) {
		final Collection inputs = c;
		Runnable gui_run = new Runnable() {
			public void run() {

				SQLInputModel inputmodel = new SQLInputModel();
				Iterator iter = inputs.iterator();
				while (iter.hasNext()) {
					SQLInput input = (SQLInput) iter.next();
					inputmodel.addInput(input);
				}

				SQLInputDialog dlg = (SQLInputDialog) TSGuiToolbox.createDialog(SQLInputDialog.class,
						TSWorkspaceFrame.getInstance(), true);
				dlg.initialize(inputmodel);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					// save the view inputs
					inputmodel.save();
				} else {
					// the user hit cancel on the dialog, so we cancel the query
					m_mediator.cancel();
				}
			}
		};

		try {
			/** we need to wait for the gui_run to finish before returning */
			SwingUtilities.invokeAndWait(gui_run);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Runnable implementation
	 */
	private void run() {
		String sql = null;

		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);
		SQLDocumentParser parser = null;
		try {
			/**
			 * flag that indicates if we actually find a SQL to run. This
			 * handles the case where the user presses the execute SQL button
			 * and there is nothing in the editor to execute
			 */
			boolean got_some_sql = false;

			Statement stmt = null;
			parser = createParser();

			String lastsql = null;
			String lastrawsql = null;
			String rawsql = null;
			while (parser.hasMoreTokens()) {
				if (isCanceled()) {
					break;
				}

				rawsql = parser.getSQL();

				// now, check if the user has typed an input form (e.g. literal
				// = ? ) in the
				// where clause. If so, then we need to popup the SQLInputView
				// dialog and
				// get the user to input the constraints
				if (parser.hasInputs()) {
					// invoke the SQLInputView. This method will set the
					// constraints in the
					// parser input objects so that when we call
					// parser.getSQL(), the inputs will
					// be correctly filled out. If the user presses cancel,
					// m_mediator.cancel will be
					// called from the following method
					requestInputsFromUser(parser.getInputs());
					if (m_mediator.isCanceled()) {
						// break out of loop
						break;
					}
				}

				sql = parser.getSQL();
				sql = sql.trim();
				if (sql.length() == 0)
					sql = null;

				if (sql != null) {

					if (m_mediator.isCanceled()) {
						// break out of the loop

						break;
					} else {
						// ok, we now have a sql command, so let's run it
						stmt = getStatement();

						logger.fine(sql);
						lastsql = sql;
						lastrawsql = rawsql;

						m_mediator.beginExecuteStatement(parser.getStartPos(), parser.getEndPos());

						if (TSUtils.isDebug()) {
							// System.out.println("SQLThread.executing sql: " + sql);
							/** for testing only */
							// sleep( 1000 );
						}

						got_some_sql = true;

						/**
						 * database does not support multipe opened result sets,
						 * so we need to fully cache any results
						 */
						if (!supportsMultipleOpenResults()) {
							m_resultsmgr.cacheCurrentResults();
						}

						boolean returnedrset = stmt.execute(sql);
						while (true) {
							if (!returnedrset) {
								int updateCount = stmt.getUpdateCount();
								if (updateCount == -1) {
									break;
								}
							} else {

								ResultSet rs = stmt.getResultSet();
								if (rs != null) {
									ResultSetReference rref = new ResultSetReference(m_connectionref, stmt, rs, sql);
									rref.setUnprocessedSQL(rawsql);
									m_resultsmgr.addResults(rref);
								} else {
									System.out.println("SQLThread. failed to get resultset ");
								}
							}

							if (m_resultsmgr.size() == 0) {
								if (!stmt.getMoreResults())
									break;
							} else {
								/**
								 * if we are here, then there is no good way to
								 * determine if there are more result sets
								 * without closing the current set, so we just
								 * don't support returning multiple result sets
								 * from a SQL statement for databases that don't
								 * support Statement.KEEP_CURRENT_RESULT
								 */
								break;
							}
						}
					}
				}

				// we do this in case there is some whitespace or comments or
				// other non-executable sql
				// following the sql statement. We want to ignore that stuff
				sql = lastsql;
				rawsql = lastrawsql;

				/**
				 * if we are stepping through the SQL, then break since we
				 * execute only one statement at a time
				 */
				if (isStep()) {
					break;
				}
			} // while( parser.hasMoreTokens() )

			m_resultsmgr.lock();

			if (m_mediator.isCanceled()) {
				m_mediator.commandCanceled();
			} else if (got_some_sql) {
				System.out.println("SQLThread done " + m_sqlbuff);
				if ( m_sqlbuff != null ) {
					// using string buffer. no start/end pos
					m_mediator.commandCompleted(-1,0, m_resultsmgr);
				} else {
					m_mediator.commandCompleted(parser.getStartPos(), parser.getEndPos(), m_resultsmgr);
				}
			} else {
				m_mediator.commandNotFound();
			}
		} catch (Exception e) {
			TSUtils.printException(e);
			int startpos = 0;
			int endpos = 0;
			if (parser != null) {
				startpos = parser.getStartPos();
				endpos = parser.getEndPos();
			}

			logger.log(Level.WARNING, e.getMessage());
			m_mediator.commandCompleted(startpos, endpos, e);
		} finally {
			cleanup();
		}

	}

	/**
	 * For testing only
	 */
	private void sleep(int msecs) {
		try {
			Thread.currentThread().sleep(msecs);
		} catch (Exception e) {

		}
	}

	/**
	 * Starts the thread
	 */
	public void start() {
		start(false);
	}

	/**
	 * Starts the thread
	 */
	public void start(boolean bstep) {
		m_step = bstep;
		Thread t = new Thread(new Runnable() {
			public void run() {
				SQLThread.this.run();
			}
		});
		t.start();
	}

	/**
	 * @return true if the database supports multiple result sets. PointBase
	 *         does not.
	 */
	private boolean supportsMultipleOpenResults() {
		return m_connectionref.getTSConnection().supportsMultipleOpenResults();
	}

}
