package com.jeta.abeille.gui.sql;

import com.jeta.foundation.gui.editor.Buffer;
import com.jeta.foundation.gui.editor.EditorFrameUIDirector;
import com.jeta.foundation.gui.editor.FrameKit;
import com.jeta.foundation.gui.editor.TSTextNames;

/**
 * UIDirector for SQLFrame
 * 
 * @author Jeff Tassin
 */
public class SQLFrameUIDirector extends EditorFrameUIDirector {
	/** the frame we are updating */
	private SQLFrame m_frame;

	/**
	 * ctor
	 */
	public SQLFrameUIDirector(SQLFrame frame) {
		super(frame, frame.getBufferMgr());
		m_frame = frame;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		super.updateComponents(evt);
		Buffer buff = m_frame.getCurrentBuffer();
		if (buff instanceof SQLBuffer) {
			if (((SQLBuffer) buff).isBusy()) {
				m_frame.enableComponent(SQLNames.ID_EXECUTE_ALL, false);
				m_frame.enableComponent(SQLNames.ID_EXECUTE_CURRENT, false);
				m_frame.enableComponent(SQLNames.ID_EXECUTE_CURRENT_ALL, false);

				m_frame.enableComponent(SQLNames.ID_STOP, true);

				m_frame.enableComponent(SQLKit.forceQuery, true);
				m_frame.enableComponent(FrameKit.closeFileAction, false);
				m_frame.enableComponent(SQLKit.openFileIntoSQLBufferAction, false);
			} else {
				m_frame.enableComponent(SQLNames.ID_EXECUTE_ALL, true);
				m_frame.enableComponent(SQLNames.ID_EXECUTE_CURRENT, true);
				m_frame.enableComponent(SQLNames.ID_EXECUTE_CURRENT_ALL, true);
				m_frame.enableComponent(SQLNames.ID_STOP, false);

				if (m_frame.getSQLBufferCount() <= 1) {
					m_frame.enableComponent(FrameKit.closeFileAction, false);
				} else {
					m_frame.enableComponent(FrameKit.closeFileAction, true);
				}
				m_frame.enableComponent(SQLKit.openFileIntoSQLBufferAction, true);
			}
			// m_frame.enableComponent( SQLNames.ID_SELECT_INTO_SQL, false );
			m_frame.enableComponent(FrameKit.saveFileAction, false);
		} else {
			m_frame.enableComponent(FrameKit.closeFileAction, true);
			m_frame.enableComponent(SQLNames.ID_EXECUTE_ALL, false);
			m_frame.enableComponent(SQLNames.ID_EXECUTE_CURRENT, false);
			m_frame.enableComponent(SQLNames.ID_EXECUTE_CURRENT_ALL, false);
			m_frame.enableComponent(SQLNames.ID_STOP, false);

			// m_frame.enableComponent( SQLNames.ID_SELECT_INTO_SQL, true );
			m_frame.enableComponent(FrameKit.saveFileAction, true);
			m_frame.enableComponent(SQLKit.openFileIntoSQLBufferAction, false);

		}

		m_frame.enableComponent(SQLKit.openFileAsNewSQLBufferAction, true);

		m_frame.enableComponent(TSTextNames.ID_PREFERENCES, true);
		// m_frame.enableComponent( SQLNames.ID_TABLE_PROPERTIES, true );
		m_frame.enableComponent(SQLNames.ID_CLEAR_BUFFER, true);
		m_frame.enableComponent(SQLKit.newSQLBuffer, true);
		// m_frame.enableComponent( SQLNames.ID_HELP, true );
		m_frame.enableComponent(SQLNames.ID_RESULTS_WINDOW_MODE, true);
	}
}
