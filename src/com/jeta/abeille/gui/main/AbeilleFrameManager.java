package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.ConnectionId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;

import com.jeta.abeille.gui.sql.SQLFrame;

import com.jeta.abeille.gui.help.SQLReferenceFrame;
import com.jeta.abeille.gui.formbuilder.FormBuilderFrame;
import com.jeta.abeille.gui.login.ConnectionMgrFrame;
import com.jeta.abeille.gui.model.ModelViewFrame;
import com.jeta.abeille.gui.model.ObjectTreeFrame;
import com.jeta.abeille.gui.sql.SQLFrame;
import com.jeta.abeille.gui.sql.SQLResultsFrame;
import com.jeta.abeille.gui.query.QueryBuilderFrame;
import com.jeta.abeille.gui.procedures.ProcedureFrame;
import com.jeta.abeille.gui.security.SecurityMgrFrame;
import com.jeta.abeille.gui.table.TableFrame;
import com.jeta.abeille.gui.logger.LoggerFrame;

import com.jeta.foundation.gui.components.*;

/**
 * New frame manager for abeille
 * 
 * @author Jeff Tassin
 */
public class AbeilleFrameManager extends JFrameManager {
	private ArrayList m_frames = new ArrayList();

	private MainFrame m_main_frame;

	private boolean m_otree_docked = true;

	/** the list of active windows */
	private LinkedList m_window_list = new LinkedList();

	public AbeilleFrameManager(MainFrame mainframe) {
		m_main_frame = mainframe;
	}

	/**
	 * Called when the frame is activated.
	 */
	public void activateFrame(TSInternalFrame iframe) {
		final TSInternalFrame sframe = iframe;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sframe.requestFocus();
			}
		});

		if (iframe.getDelegate() instanceof JPanelFrame) {
			m_window_list.remove(iframe);
			m_window_list.addFirst(iframe);
		}
	}

	/**
	 * Adds the window to the desktop
	 */
	public void addWindow(TSInternalFrame frame) {
		if (!m_frames.contains(frame)) {
			m_frames.add(frame);
		}

		if (!(frame instanceof ObjectTreeFrame) && (frame.getDelegate() instanceof JPanelFrame)) {
			setConnectionContext(frame);

			JTabbedPane tabpane = m_main_frame.getTabbedPane();
			for (int index = 0; index < tabpane.getTabCount(); index++) {
				Component comp = tabpane.getComponentAt(index);
				if (comp == frame.getDelegate()) {
					tabpane.setSelectedIndex(index);
					return;
				}
			}

			if (frame instanceof ConnectionMgrFrame) {
				tabpane.insertTab(frame.getShortTitle(), frame.getFrameIcon(), (JPanelFrame) frame.getDelegate(), null,
						0);
				tabpane.setSelectedIndex(0);
			} else if (frame instanceof LoggerFrame) {
				if (tabpane.getTabCount() > 0) {
					WindowDelegate wd = (WindowDelegate) tabpane.getComponentAt(0);
					if (wd.getTSInternalFrame() instanceof ConnectionMgrFrame) {
						tabpane.insertTab(frame.getShortTitle(), frame.getFrameIcon(),
								(JPanelFrame) frame.getDelegate(), null, 1);
						tabpane.setSelectedIndex(1);
					} else {
						tabpane.insertTab(frame.getShortTitle(), frame.getFrameIcon(),
								(JPanelFrame) frame.getDelegate(), null, 0);
						tabpane.setSelectedIndex(0);
					}
				}
			} else {
				tabpane.addTab(frame.getShortTitle(), frame.getFrameIcon(), (JPanelFrame) frame.getDelegate());
				tabpane.setSelectedIndex(tabpane.getTabCount() - 1);
			}
		}
	}

	public WindowDelegate createWindow(TSInternalFrame frame, String caption) {
		if (frame instanceof ObjectTreeFrame) {
			if (m_otree_docked) {
				JPanelFrame result = new JPanelFrame(frame, caption);
				return result;
			} else {
				JControlledFrame result = new JControlledFrame(frame, caption);
				return result;
			}
		} else if (frame instanceof ModelViewFrame) {
			JPanelFrame panelframe = new JPanelFrame(frame, caption);
			return panelframe;
		} else if (frame instanceof SQLFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof TableFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof SystemInfoFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof LoggerFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof ConnectionMgrFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof SecurityMgrFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof QueryBuilderFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof FormBuilderFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof ProcedureFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof SQLReferenceFrame) {
			return new JPanelFrame(frame, caption);
		} else if (frame instanceof SQLResultsFrame) {
			return new JPanelFrame(frame, caption);
		} else
			return new JFrameEx(frame, caption);
	}

	public TSInternalFrame[] getAllFrames() {
		return (TSInternalFrame[]) m_frames.toArray(new TSInternalFrame[0]);
	}

	/**
	 * @return the frame at the given tab index. Null is returned if the index
	 *         is invalid
	 */
	public TSInternalFrame getFrameAt(int index) {
		return null;
	}

	public Dimension getWorkspaceSize() {
		return java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	}

	/**
	 * Removes the window from this workspace
	 */
	public void removeWindow(TSInternalFrame tsiframe) {
		m_frames.remove(tsiframe);

		if (tsiframe.getDelegate() instanceof JFrameEx) {
			JFrameEx jframe = (JFrameEx) tsiframe.getDelegate();
			jframe.hide();
		} else if (tsiframe.getDelegate() instanceof JPanelFrame) {
			JETAFrameEvent evt = new JETAFrameEvent(tsiframe);
			Collection listeners = tsiframe.getFrameListeners();
			if (listeners.size() > 0)
				listeners = new java.util.LinkedList(listeners);

			Iterator iter = listeners.iterator();
			while (iter.hasNext()) {
				JETAFrameListener listener = (JETAFrameListener) iter.next();
				listener.jetaFrameClosing(evt);
			}

			JTabbedPane tabpane = m_main_frame.getTabbedPane();
			JPanelFrame jframe = (JPanelFrame) tsiframe.getDelegate();

			TSInternalFrame nextframe = null;

			/** now try to select the last active window */
			m_window_list.remove(tsiframe);
			iter = m_window_list.iterator();
			while (iter.hasNext()) {
				TSInternalFrame iframe = (TSInternalFrame) iter.next();
				for (int index = 0; index < tabpane.getTabCount(); index++) {
					Object tabobj = tabpane.getComponentAt(index);
					if (iframe.getDelegate() == tabobj) {
						nextframe = iframe;
						break;
					}
				}
				if (nextframe != null)
					break;

				iter.remove();
			}
			tabpane.remove(jframe);
			if (nextframe != null)
				m_main_frame.show(nextframe);

		}
	}

	/**
	 * Moves the frame to the top of the Z-order and sets it selected. Deselects
	 * all other frames
	 */
	public void selectFrame(TSInternalFrame frame) {
		if (frame == null) {
			assert (false);
			return;
		}

		if (frame.getDelegate() instanceof JFrameEx) {
			JFrameEx jframe = (JFrameEx) frame.getDelegate();
			jframe.show();
		} else {
			if (!(frame instanceof ObjectTreeFrame)) {
				setConnectionContext(frame);

				JTabbedPane tpane = m_main_frame.getTabbedPane();
				for (int index = 0; index < tpane.getTabCount(); index++) {
					Component comp = tpane.getComponentAt(index);
					if (frame.getDelegate() == comp) {
						tpane.setSelectedIndex(index);
						break;
					}
				}
			}
		}

		if (frame instanceof SQLFrame)
			frame.requestFocus();
	}

	void setObjectTreeFrameDock(boolean otree_docked) {
		m_otree_docked = otree_docked;
	}

	/**
	 * Sets the connection context of the main frame to that of the given
	 * internal frame.
	 */
	private void setConnectionContext(TSInternalFrame iframe) {
		if (m_main_frame.isConnectionOriented(iframe) && iframe.getDelegate() instanceof JPanelFrame) {
			TSConnection tsconn = m_main_frame.getConnection();
			if (tsconn != null) {
				ConnectionId id = tsconn.getId();
				Object winid = iframe.getWindowId();
				if (winid instanceof ConnectionId) {
					if (!id.equals(winid)) {
						try {
							tsconn = TSConnectionMgr.getConnection((ConnectionId) winid);
							if (tsconn != null) {
								m_main_frame.setCurrentConnection(tsconn);
							} else {
								assert (false);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * Updates the workspace if a title has changed in a frame. This is mainly
	 * for tabbed panes that need to update their tab titles.
	 */
	public void updateTitle(TSInternalFrame iframe) {
		JTabbedPane tabpane = m_main_frame.getTabbedPane();
		for (int index = 0; index < tabpane.getTabCount(); index++) {
			Component comp = tabpane.getComponentAt(index);
			if (comp == iframe.getDelegate()) {
				tabpane.setTitleAt(index, iframe.getTitle());
				return;
			}
		}
	}

	public void updateUI() {

	}
}
