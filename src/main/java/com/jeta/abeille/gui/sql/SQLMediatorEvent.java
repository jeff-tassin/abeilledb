package com.jeta.abeille.gui.sql;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents an event sent by the SQLMediator class when processing
 * a series of SQL commands. This event is used with the SQLMediatorListener.
 * 
 * @author Jeff Tassin
 */
public class SQLMediatorEvent {
	/** the event id */
	private int m_id;

	/** the source of the event */
	private SQLMediator m_mediator;

	/**
	 * If the id is ID_SQL_STATEMENT_PROCESSED, then these values are the start
	 * and end position of the SQL statement in the document that contains the
	 * SQL Otherwise, this value is zero
	 */
	private int m_startpos;
	private int m_endpos;

	/**
	 * if this event is an ID_TIME_EVENT event, then this is the elapsed time
	 * Otherwise, this value is zero
	 */
	private long m_elapsed_time;

	public static final int ID_TIME_EVENT = 1;
	public static final int ID_SQL_STATEMENT_PROCESSED = 2;
	public static final int ID_COMMAND_FINISHED = 4;

	/**
	 * standard ctor
	 */
	public SQLMediatorEvent(SQLMediator mediator, int id) {
		m_mediator = mediator;
		m_id = id;
	}

	/**
	 * ctor for a line number event
	 */
	public static SQLMediatorEvent createStatementEvent(SQLMediator mediator, int startpos, int endpos) {
		SQLMediatorEvent evt = new SQLMediatorEvent(mediator, ID_SQL_STATEMENT_PROCESSED);
		evt.m_startpos = startpos;
		evt.m_endpos = endpos;
		return evt;
	}

	/**
	 * ctor for a finished event
	 */
	public static SQLMediatorEvent createCompletedEvent(SQLMediator mediator, int startpos, int endpos) {
		SQLMediatorEvent evt = new SQLMediatorEvent(mediator, ID_COMMAND_FINISHED);
		evt.m_startpos = startpos;
		evt.m_endpos = endpos;
		return evt;
	}

	/**
	 * ctor for a finished event
	 */
	public static SQLMediatorEvent createErrorEvent(SQLMediator mediator, int startpos, int endpos) {
		SQLMediatorEvent evt = new SQLMediatorEvent(mediator, ID_COMMAND_FINISHED);
		evt.m_startpos = startpos;
		evt.m_endpos = endpos;
		return evt;
	}

	/**
	 * ctor for an elapsed time event
	 */
	public static SQLMediatorEvent createTimeEvent(SQLMediator mediator, long elapsedTime) {
		SQLMediatorEvent evt = new SQLMediatorEvent(mediator, ID_TIME_EVENT);
		evt.m_elapsed_time = elapsedTime;
		return evt;
	}

	/**
	 * @return the event id
	 */
	public int getID() {
		return m_id;
	}

	/**
	 * @return the elapsed time only if the event id is ID_TIME_EVENT. If the id
	 *         is some other value, then zero will be returned.
	 */
	public long getElapsedTime() {
		return m_elapsed_time;
	}

	/**
	 * If the id is ID_SQL_STATEMENT_PROCESSED, then this is the start position
	 * of the SQL statement in the document that contains the SQL Otherwise,
	 * this value is zero
	 */
	public int getStartPos() {
		return m_startpos;
	}

	/**
	 * If the id is ID_SQL_STATEMENT_PROCESSED, then this is the end position of
	 * the SQL statement in the document that contains the SQL Otherwise, this
	 * value is zero
	 */
	public int getEndPos() {
		return m_endpos;
	}

	/**
	 * @return the source for this event
	 */
	public SQLMediator getMediator() {
		return m_mediator;
	}

	public void print() {
		if (TSUtils.isDebug()) {
			if (m_id == ID_TIME_EVENT) {
				System.out.println("SQLMediatorEvent  TIME_EVENT");
			} else if (m_id == ID_SQL_STATEMENT_PROCESSED) {
				System.out.println("SQLMediatorEvent  SQL_STATEMENT_PROCESSED   startpos: " + m_startpos + "  endpos: "
						+ m_endpos);
			} else if (m_id == ID_COMMAND_FINISHED) {
				System.out.println("SQLMediatorEvent  COMMAND_FINISHED");
			}
		}
	}
}
