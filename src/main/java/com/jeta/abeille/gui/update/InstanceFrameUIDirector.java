package com.jeta.abeille.gui.update;

import com.jeta.abeille.gui.sql.SQLResultsFrame;

import com.jeta.foundation.gui.components.EnableEnum;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.UIDirector;

import java.util.EventObject;

/**
 * This class is used for updating the instance frame toolbar/menus based on the
 * state of the model
 * 
 * @author Jeff Tassin
 */
public class InstanceFrameUIDirector implements UIDirector {
	/** the frame we are updating */
	private InstanceFrame m_frame;

	/** the view that the frame is based on */
	private InstanceView m_instanceview;

	/** the model that the frame is based on */
	private InstanceModel m_instancemodel;

	/**
	 * ctor
	 */
	public InstanceFrameUIDirector(InstanceFrame frame, InstanceView view) {
		m_frame = frame;
		m_instanceview = view;
		m_instancemodel = m_instanceview.getModel();
	}

	/**
	 * @return the frame this UIDirector is responsible for updating
	 */
	public InstanceFrame getFrame() {
		return m_frame;
	}

	/**
	 * @return the view this UIDirector is responsible for updating
	 */
	public InstanceView getView() {
		return m_instanceview;
	}

	/**
	 * Updates the UI
	 */
	public void updateComponents(EventObject evt) {
		try {
			if (m_instancemodel.getState() == InstanceModel.BUSY) {
				m_frame.enableComponent(InstanceFrame.ID_CLEAR_FORM, false);
				m_frame.enableComponent(InstanceFrame.ID_ADD_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_DELETE_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_MODIFY_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_RUN_QUERY, false);
				m_frame.enableComponent(InstanceFrame.ID_SHOW_TABULAR_RESULTS, false);
				m_frame.enableComponent(InstanceFrame.ID_FIRST_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_NEXT_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_PREV_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_LAST_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_CONFIGURE, false);
				m_frame.enableComponent(InstanceFrame.ID_COMMIT, false);
				m_frame.enableComponent(InstanceFrame.ID_ROLLBACK, false);
				m_frame.enableComponent(InstanceFrame.ID_PASTE_INSTANCE, false);
				// m_frame.enableComponent( InstanceFrame.ID_STOP, true );
			} else {
				// m_frame.enableComponent( InstanceFrame.ID_STOP, false );
				m_frame.enableComponent(InstanceFrame.ID_CLEAR_FORM, true);
				m_frame.enableComponent(InstanceFrame.ID_RUN_QUERY, true);
				m_frame.enableComponent(InstanceFrame.ID_SHOW_TABULAR_RESULTS, true);
				m_frame.enableComponent(InstanceFrame.ID_CONFIGURE, true);
				m_frame.enableComponent(InstanceFrame.ID_PREFERENCES, true);

				if (m_instancemodel.allowsUpdates()) {
					m_frame.enableComponent(InstanceFrame.ID_ADD_ROW, true);
					m_frame.enableComponent(InstanceFrame.ID_DELETE_ROW, m_instanceview.isViewConstrained());
					m_frame.enableComponent(InstanceFrame.ID_MODIFY_ROW, m_instanceview.isViewConstrained());

					boolean btrans = m_instancemodel.supportsTransactions();
					if (btrans && m_instancemodel.shouldCommit()) {
						m_frame.enableComponent(InstanceFrame.ID_COMMIT, true);
						m_frame.enableComponent(InstanceFrame.ID_ROLLBACK, true);
						m_frame.setStatus(InstanceFrame.COMMIT_STATUS, I18N.getLocalizedMessage("Commit required"));
					} else {
						m_frame.enableComponent(InstanceFrame.ID_COMMIT, false);
						m_frame.enableComponent(InstanceFrame.ID_ROLLBACK, false);
						m_frame.setStatus(InstanceFrame.COMMIT_STATUS, "");
					}
				} else {
					m_frame.enableComponent(InstanceFrame.ID_ADD_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_DELETE_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_MODIFY_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_COMMIT, false);
					m_frame.enableComponent(InstanceFrame.ID_ROLLBACK, false);
				}

				updateNavigationUI();

				boolean enable_paste_instance = false;

				InstanceFrameLauncher launcher = m_frame.getLauncher();
				if (launcher != null) {
					Object src = launcher.getSource();
					if (src instanceof InstanceFrame) {
						InstanceFrame iframe = (InstanceFrame) src;
						enable_paste_instance = iframe.isVisible();
					}
				}
				m_frame.enableComponent(InstanceFrame.ID_PASTE_INSTANCE, enable_paste_instance);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the buttons/menus responsible for navigation
	 */
	private void updateNavigationUI() {
		try {
			InstanceProxy iproxy = m_instancemodel.getInstanceProxy();
			if (iproxy == null) {
				m_frame.enableComponent(InstanceFrame.ID_FIRST_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_NEXT_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_PREV_ROW, false);
				m_frame.enableComponent(InstanceFrame.ID_LAST_ROW, false);
			} else {
				if (iproxy.isEmpty()) {
					m_frame.enableComponent(InstanceFrame.ID_FIRST_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_PREV_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_NEXT_ROW, false);
					m_frame.enableComponent(InstanceFrame.ID_LAST_ROW, false);
				} else {
					m_frame.enableComponent(InstanceFrame.ID_FIRST_ROW, !iproxy.isFirst());
					m_frame.enableComponent(InstanceFrame.ID_PREV_ROW, !iproxy.isFirst());
					m_frame.enableComponent(InstanceFrame.ID_NEXT_ROW, !iproxy.isLast());

					if (iproxy.isScrollable()) {
						m_frame.enableComponent(InstanceFrame.ID_LAST_ROW, !iproxy.isLast());
					} else {
						if (iproxy.getRowCount() >= 0)
							m_frame.enableComponent(InstanceFrame.ID_LAST_ROW, !iproxy.isLast());
						else
							m_frame.enableComponent(InstanceFrame.ID_LAST_ROW, false);
					}
				}
			}

			boolean show_tabular = true;
			InstanceFrameLauncher launcher = m_frame.getLauncher();
			if (launcher != null) {
				Class lclass = launcher.getSourceClass();
				if (SQLResultsFrame.class.isAssignableFrom(lclass)) {
					SQLResultsFrame sqlframe = (SQLResultsFrame) launcher.getSource();
					if (sqlframe == null || !sqlframe.isVisible())
						show_tabular = false;

				}
			}

			if (iproxy == null || iproxy.isEmpty())
				show_tabular = false;

			m_frame.enableComponent(InstanceFrame.ID_SHOW_TABULAR_RESULTS, show_tabular);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
