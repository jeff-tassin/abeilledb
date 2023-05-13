package com.jeta.abeille.gui.update;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This is the popup component for the InstanceView. This popup shows a list of
 * Browser targets that the user can choose from. When the user selects an item,
 * the form for that table is displayed. This popup is shown when we have a
 * column in the current table that is linked to multiple tables via foreign key
 * or user defined links. When the user clicks on the column in the form, we
 * show the popup so the user can select the table they are interested in.
 * 
 * @author Jeff Tassin
 */
public class BrowserPopup extends JPopupMenu {
	protected CommandListener m_cmdListener = new CommandListener();

	/** the InstanceView */
	private WeakReference m_controllerref;

	/**
	 * ctor
	 */
	public BrowserPopup() {
	}

	/**
	 * Adds a button to the popup panel
	 * 
	 * @param image
	 *            the image icon to show as the button
	 * @param commandId
	 *            the command id that will be sent to any listeners when the
	 *            button is pressed
	 */
	public void addBrowserTargets(Collection targets) {
		removeAll();
		Iterator iter = targets.iterator();
		while (iter.hasNext()) {
			BrowserLink link = (BrowserLink) iter.next();
			BroswerMenuItem item = new BroswerMenuItem(link);
			add(item);
			item.addActionListener(m_cmdListener);
		}
	}

	public InstanceController getController() {
		return (InstanceController) m_controllerref.get();
	}

	public void setController(InstanceController controller) {
		m_controllerref = new WeakReference(controller);
	}

	class CommandListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			if (obj instanceof BroswerMenuItem) {
				BroswerMenuItem item = (BroswerMenuItem) obj;
				InstanceController controller = getController();
				if (controller != null) {
					controller.launchLinkedInstanceFrame(item.getLink());
				}
			}
		}
	}

	public static class BroswerMenuItem extends JMenuItem {
		private BrowserLink m_target;

		public BroswerMenuItem(BrowserLink target) {
			m_target = target;
			StringBuffer buff = new StringBuffer();
			buff.append(target.getTargetTableId().getTableName());
			buff.append('.');
			buff.append(target.getTargetColumn());
			setText(buff.toString());
		}

		public BrowserLink getLink() {
			return m_target;
		}
	}

}
