package com.jeta.abeille.gui.model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSPanelEx;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This is the class that shows the user defined global links in the system
 * 
 * @author Jeff Tassin
 */
public class GlobalLinksView extends TSPanelEx {
	/** the modeler */
	private ModelerModel m_modeler;

	/** the list component that displays the links */
	private JList m_list;

	private LinksEventHandler m_modeler_listener = new LinksEventHandler(this);

	public static final String ID_DELETE_LINK = "modeler.links.delete.link";

	/**
	 * ctor
	 */
	public GlobalLinksView(ModelerModel modeler) {
		m_modeler = modeler;
		initialize();

		setPopupMenu(new JPopupMenu(), m_list);
		getPopupMenu().add(i18n_createMenuItem("Delete Link", ID_DELETE_LINK, null));

		setController(new GlobalLinksController(this));
		setUIDirector(new GlobalLinksUIDirector());
	}

	/**
	 * Adds a link to the view
	 */
	public void addLink(Link link) {
		if (link != null) {
			DefaultListModel lmodel = (DefaultListModel) m_list.getModel();
			lmodel.addElement(link);
			m_list.repaint();
		}
	}

	void dispose() {
		getModeler().removeListener(m_modeler_listener);
	}

	/**
	 * @return the underlying JList component
	 */
	JList getList() {
		return m_list;
	}

	/**
	 * @return the modeler associated with the connection
	 */
	ModelerModel getModeler() {
		return m_modeler;
	}

	/**
	 * @return the selected Link in the view. Null is returned if no link is
	 *         selected
	 */
	public Link getSelectedLink() {
		return (Link) m_list.getSelectedValue();
	}

	/**
	 * Initializes the view
	 */
	protected void initialize() {
		setLayout(new BorderLayout());
		m_list = new JList();
		m_list.setCellRenderer(new LinkRenderer());
		DefaultListModel lmodel = new DefaultListModel();
		m_list.setModel(lmodel);
		add(new JScrollPane(m_list), BorderLayout.CENTER);
		try {
			ModelerModel modeler = getModeler();
			modeler.addListener(m_modeler_listener);
			Collection links = modeler.getUserDefinedLinks();
			Iterator iter = links.iterator();
			while (iter.hasNext()) {
				Link link = (Link) iter.next();
				lmodel.addElement(link);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Removes a link from the view
	 */
	public void removeLink(Link link) {
		if (link != null) {
			DefaultListModel lmodel = (DefaultListModel) m_list.getModel();
			lmodel.removeElement(link);
			m_list.repaint();
		}
	}

	/**
	 * Sets the connection used by this view
	 */
	public void setConnection(TSConnection connection) {
	}

	/**
	 * This renderer is used to render the Link in the JList for this view
	 */
	public static class LinkRenderer extends JLabel implements ListCellRenderer {
		static ImageIcon m_linkicon;

		static {
			m_linkicon = TSGuiToolbox.loadImage("link16.gif");
		}

		/**
		 * ctor
		 */
		public LinkRenderer() {
			setOpaque(true);
			setVerticalAlignment(CENTER);
		}

		/**
		 * ListCellRenderer implementation.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Link link = (Link) value;

			setFont(UIManager.getFont("List.font"));
			if (isSelected) {
				setBackground(UIManager.getColor("List.selectionBackground"));
				setForeground(UIManager.getColor("List.selectionForeground"));
			} else {
				setBackground(UIManager.getColor("List.background"));
				setForeground(UIManager.getColor("List.foreground"));
			}
			setIcon(m_linkicon);

			setText(GlobalLinksView.render(link));
			return this;
		}
	}

	/**
	 * Converts a Link to its String representation
	 */
	static String render(Link link) {
		StringBuffer txt = new StringBuffer();
		txt.append(link.getSourceTableId().toString());
		txt.append(".");
		txt.append(link.getSourceColumn());
		txt.append(" -> ");
		txt.append(link.getDestinationTableId().toString());
		txt.append(".");
		txt.append(link.getDestinationColumn());
		return txt.toString();
	}

	/**
	 * This event handler for the ModelerModel is used with the GlobalLinksView
	 * It gets model changes from the ModelerModel and updates the
	 * GlobalLinksView objects accordingly.
	 * 
	 * @author Jeff Tassin
	 */
	public class LinksEventHandler implements ModelerListener {
		/**
		 * The view we are controlling
		 */
		private GlobalLinksView m_view;

		/**
		 * ctor
		 */
		public LinksEventHandler(GlobalLinksView view) {
			m_view = view;
		}

		/**
		 * ModelerModel Event handler
		 */
		public void eventFired(ModelerEvent evt) {
			if (evt.getID() == ModelerEvent.LINK_CREATED) {
				m_view.addLink((Link) evt.getParameter(0));
			} else if (evt.getID() == ModelerEvent.LINK_DELETED) {
				m_view.removeLink((Link) evt.getParameter(0));
			}
		}
	}

	/**
	 * The controller for this view
	 */
	public class GlobalLinksController extends JETAController {
		/**
		 * The view we are controlling
		 */
		private GlobalLinksView m_view;

		/**
		 * ctor
		 */
		public GlobalLinksController(GlobalLinksView view) {
			super(view);
			m_view = view;
			assignAction(GlobalLinksView.ID_DELETE_LINK, new DeleteLinkAction());
		}

		/**
		 * Action handler for ID_DELETE_LINK
		 */
		public class DeleteLinkAction implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				Link link = m_view.getSelectedLink();
				if (link != null) {
					StringBuffer msg = new StringBuffer();
					msg.append(I18N.getLocalizedMessage("Delete Link"));
					msg.append("\n");
					msg.append(GlobalLinksView.render(link));
					int result = JOptionPane.showConfirmDialog(null, msg.toString(),
							I18N.getLocalizedMessage("Confirm"), JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						try {
							ModelerModel modeler = getModeler();
							modeler.removeUserLink(link);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	public class GlobalLinksUIDirector implements UIDirector {
		/**
		 * UIDirector implementation
		 */
		public void updateComponents(java.util.EventObject evt) {
			enableComponent(ID_DELETE_LINK, (getSelectedLink() != null));
		}
	}
}
