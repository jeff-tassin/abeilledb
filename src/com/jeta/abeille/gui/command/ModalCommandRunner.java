package com.jeta.abeille.gui.command;

import java.awt.ActiveEvent;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * Invokes a database command in a background thread. Some databases might not
 * throw exceptions in the cases of deadlock, so we need to allow the user to
 * cancel a command that might be locked by closing the connection. If the
 * command takes longer than a defined amount of time (currently 3 seconds),
 * then this object will show a dialog on the screen that allows the user to
 * cancel the command and close the connection.
 * 
 * @author Jeff Tassin
 */
public class ModalCommandRunner {
	/** flag that indicates if the command has successfully completed */
	private boolean m_completed = false;

	/**
	 * this is the component that initiated the command. we use this component
	 * to determine the owner of the cancel or error dialogs
	 */
	private Component m_commander;

	/** dialog that lets the user cancel the command */
	private TSDialog m_canceldialog;

	/** the object that performs the command against the database */
	private Command m_command;

	/**
	 * flag that indicates the state of the command (i.e. waiting, completed,
	 * canceled, error )
	 */
	private int m_state;

	/** holds the exception if an error occured while running the command */
	private Exception m_error;

	/** the timer to pulse the event queue and check for time outs */
	private Timer m_timer;

	/** flag that indicates that we should run in a modal loop */
	private boolean m_modalloop = true;

	/** the database we are running commands against */
	private TSConnection m_connection;

	/**
	 * the amount of time in milliseconds to wait for the command to complete
	 * before invoking the cancel dialog
	 */
	private static final int TIMEOUT = 3000;

	public static final int WAITING = 1;
	public static final int COMPLETED = 2;
	public static final int CANCELED = 3;
	private static final int ERROR = 4;

	/**
	 * ctor
	 * 
	 * @param commander
	 *            the component whose Frame or Dialog parent is the owner for
	 *            the dialog this runner may invoke
	 * @param cmd
	 *            the command object to run
	 */
	public ModalCommandRunner(TSConnection connection, Component commander, Command command) {
		m_connection = connection;
		m_commander = commander;
		m_command = command;
	}

	/**
	 * ctor
	 * 
	 * @param commander
	 *            the component whose Frame or Dialog parent is the owner for
	 *            the dialog this runner may invoke
	 * @param cmd
	 *            the command object to run
	 */
	public ModalCommandRunner(TSConnection connection, TSInternalFrame commander, Command command) {
		m_connection = connection;
		if (commander != null)
			m_commander = commander.getDelegate();
		m_command = command;
	}

	/**
	 * ctor
	 * 
	 * @param cmd
	 *            the command object to run
	 */
	public ModalCommandRunner(TSConnection connection, Command command) {
		this(connection, (Component) null, command);
	}

	/**
	 * Sets the completed flag to true and notifies the GUI thread
	 */
	private synchronized void commandCompleted() {
		if (m_state == CANCELED)
			return;

		m_state = COMPLETED;

		// if dialog is visible on screen, let's close it
		Runnable gui_update = new Runnable() {
			public void run() {
				if (m_canceldialog != null) {
					m_canceldialog.cmdOk();
				}
			}
		};
		SwingUtilities.invokeLater(gui_update);
	}

	/**
	 * Called if an error occurs while processing the command
	 */
	private synchronized void commandError(SQLException e) {
		// the act of canceling may have caused an exception to be thrown, so
		// let's ignore in that case
		if (m_state == CANCELED)
			return;

		m_state = ERROR;
		m_error = e;
		// if dialog is visible on screen, let's close it
		Runnable gui_update = new Runnable() {
			public void run() {
				if (m_canceldialog != null) {
					m_canceldialog.cmdOk();
				}
			}
		};

		SwingUtilities.invokeLater(gui_update);
	}

	/**
	 * Enters a modal loop that dispatches GUI events and does not return for a
	 * given number of milliseconds
	 */
	private void enterModalLoop() {

		m_modalloop = true;

		final long starttm = System.currentTimeMillis();
		ActionListener force_events = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				long tm = System.currentTimeMillis();
				if (m_state == WAITING) {
					if ((tm - starttm) >= TIMEOUT) {
						m_modalloop = false;
					}
				} else {
					m_timer.stop();
				}
			}
		};

		m_timer = new Timer(300, force_events);
		m_timer.start();

		Component parent = TSWorkspaceFrame.getInstance();
		Cursor current_cursor = parent.getCursor();
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (SwingUtilities.isEventDispatchThread()) {
			try {
				EventQueue theQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
				while (m_state == WAITING && m_modalloop) {
					AWTEvent event = theQueue.getNextEvent();
					Object source = event.getSource();
					if (event instanceof ActiveEvent) {
						((ActiveEvent) event).dispatch();
					} else if (source instanceof Component) {
						((Component) source).dispatchEvent(event);
					} else if (source instanceof MenuComponent) {
						((MenuComponent) source).dispatchEvent(event);
					} else {
						System.out.println("Unable to dispatch: " + event);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			assert (false);
		}

		parent.setEnabled(true);
		parent.setCursor(current_cursor);

		if (m_state == WAITING)
			handleTimeOut();
	}

	/**
	 * @return the exception if an error occured while running the command
	 */
	public Exception getError() {
		return m_error;
	}

	/**
	 * If the command takes to long, we invoke dialog after a small amount of
	 * time (currently 3 seconds) that let's the user cancel the command
	 */
	private void handleTimeOut() {
		// ok, the command is still blocking so let's inform the user
		m_canceldialog = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_commander, true);
		m_canceldialog.setTitle(I18N.getLocalizedMessage("Working"));
		CommandPanel panel = new CommandPanel(m_command.getTimeoutMessage());
		m_canceldialog.setPrimaryPanel(panel);
		m_canceldialog.setSize(m_canceldialog.getPreferredSize());
		m_canceldialog.setCancelEnabled(false);
		JButton btn = m_canceldialog.getOkButton();
		btn.setEnabled(false);
		btn.setVisible(false);
		m_canceldialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);

		// add a listener on the panel checkbox. the box must be selected in
		// order to close the dialog
		final JCheckBox cbox = (JCheckBox) panel.getComponentByName(CommandPanel.ID_CANCEL_BOX);
		if (!m_connection.supportsCancelStatement()) {
			cbox.setEnabled(false);
		}
		cbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (cbox.isSelected()) {
					JButton btn = m_canceldialog.getOkButton();
					btn.setEnabled(true);
				} else {
					JButton btn = m_canceldialog.getOkButton();
					btn.setEnabled(false);
				}
			}
		});

		m_canceldialog.showCenter();
		if (m_canceldialog.isOk()) {
			// the command may have completed while the dialog was on the screen
			if (m_state == WAITING) {
				try {
					m_state = CANCELED;
					m_command.cancel();
				} catch (SQLException se) {
					m_state = ERROR;
					m_error = se;
				}
			}

		} else {
			assert (false);
		}

		// an error occurred
		if (m_state == ERROR) {
			// SQLErrorDialog sdlg = (SQLErrorDialog)TSGuiToolbox.createDialog(
			// SQLErrorDialog.class, m_commander, true );
			// sdlg.initialize( m_error, m_command.getSQL() );
			// sdlg.setSize( sdlg.getPreferredSize() );
			// sdlg.showCenter();
		}
		m_canceldialog = null;
	}

	/**
	 * Invokes the command with a worker thread. If the command takes too long a
	 * dialog is invoked that allows the user to cancel the operation. The
	 * result is returned.
	 * 
	 * @return the result of the command (completed, canceled, error)
	 */
	public int invoke() throws SQLException {
		Worker worker = new Worker();
		Thread t = new Thread(worker);

		m_state = WAITING;
		t.start();

		// enters the loop for a given number of seconds
		enterModalLoop();

		if (m_state == WAITING)
			handleTimeOut();

		assert (m_state != WAITING);

		if (m_state == COMPLETED)
			return COMPLETED;
		else if (m_state == CANCELED)
			return CANCELED;
		else {
			if (m_error instanceof SQLException)
				throw (SQLException) m_error;
			else
				throw new SQLException(m_error.getMessage());
		}
	}

	/**
	 * Worker thread that actually invokes the command
	 */
	public class Worker implements Runnable {
		/**
		 * Runnable implementation
		 */
		public void run() {
			try {
				m_command.invoke();
				commandCompleted();
			} catch (SQLException e) {
				commandError(e);
			}
		}
	}

}
