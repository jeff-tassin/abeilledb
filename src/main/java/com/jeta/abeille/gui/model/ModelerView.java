package com.jeta.abeille.gui.model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdComparator;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPanelEx;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the class that shows the user defined global links in the system
 * 
 * @author Jeff Tassin
 */
public class ModelerView extends TSPanelEx {
	/** the database connection */
	private ModelerModel m_modeler;

	/** the list component that displays the prototype tables */
	private JList m_list;

	/** the list model that contains the prototype tables */
	private DefaultListModel m_list_model;

	private ModelerViewEventHandler m_modeler_listener = new ModelerViewEventHandler();

	/**
	 * ctor
	 */
	public ModelerView(ModelerModel modeler) {
		m_modeler = modeler;

		initialize();

		JPopupMenu popup = new JPopupMenu();
		setPopupMenu(popup, m_list);
		popup.add(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Table Properties"),
				TableTreeNames.ID_TABLE_PROPERTIES, null));
		popup.add(TSGuiToolbox.createMenuItem(I18N.getLocalizedMessage("Delete Prototype"),
				ModelerNames.ID_DELETE_PROTOTYPE, null));

		getModeler().addListener(m_modeler_listener);
		setController(new ModelerViewController(this));
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

	public TableId getSelectedTable() {
		Object obj = m_list.getSelectedValue();
		if (obj instanceof TableId)
			return (TableId) obj;
		else
			return null;
	}

	public Object[] getSelectedValues() {
		return m_list.getSelectedValues();
	}

	/**
	 * Initializes the view
	 */
	protected void initialize() {
		setLayout(new BorderLayout());
		m_list = new JList();
		m_list.setCellRenderer(new PrototypesRenderer());
		m_list_model = new DefaultListModel();
		m_list.setModel(m_list_model);
		m_list.setDragEnabled(true);
		m_list.setTransferHandler(new ModelerTransferHandler());
		add(new JScrollPane(m_list), BorderLayout.CENTER);
		reload();
	}

	private void reload() {
		try {
			ModelerModel modeler = getModeler();

			TreeSet tables = new TreeSet(new TableIdComparator(modeler.getConnection()));
			m_list_model.removeAllElements();
			Collection protos = modeler.getPrototypes();
			Iterator iter = protos.iterator();
			while (iter.hasNext()) {
				TableId tableid = (TableId) iter.next();
				System.out.println("ModelerView.reload   table: " + tableid + "   hashcode: " + tableid.hashCode());
				tables.add(tableid);
			}

			iter = tables.iterator();
			while (iter.hasNext()) {
				m_list_model.addElement((TableId) iter.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when a table has been created in the underlying data. Tells this
	 * model to update itself
	 */
	public void tableCreated(TSConnection conn, TableId tableid) {
		reload();
	}

	/**
	 * Called when a table has changed in the underlying data. Tells this model
	 * to update itself
	 */
	public void tableChanged(TSConnection conn, TableId tableid) {
		reload();
	}

	/**
	 * Called when a table has been renamed in the underlying data. Tells this
	 * model to update itself
	 */
	public void tableRenamed(TSConnection conn, TableId newId, TableId oldId) {
		reload();
	}

	/**
	 * Called when a table has been deleted in the underlying data. Tells this
	 * model to update itself
	 */
	public void tableDeleted(TSConnection conn, TableId tableId) {
		reload();
	}

	/**
	 * This renderer is used to render the Link in the JList for this view
	 */
	public static class PrototypesRenderer extends JLabel implements ListCellRenderer {
		static ImageIcon m_prototype_icon;

		static {
			m_prototype_icon = TSGuiToolbox.loadImage("incors/16x16/table_sql_create.png");
		}

		/**
		 * ctor
		 */
		public PrototypesRenderer() {
			setOpaque(true);
			setVerticalAlignment(CENTER);
		}

		/**
		 * ListCellRenderer implementation.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			TableId tableid = (TableId) value;

			setFont(UIManager.getFont("List.font"));
			if (isSelected) {
				setBackground(UIManager.getColor("List.selectionBackground"));
				setForeground(UIManager.getColor("List.selectionForeground"));
			} else {
				setBackground(UIManager.getColor("List.background"));
				setForeground(UIManager.getColor("List.foreground"));
			}
			setIcon(m_prototype_icon);

			setText(tableid.getFullyQualifiedName());
			return this;
		}
	}

	/**
	 * This event handler for the ModelerModel is used with the
	 * ModelView/Model/Controllers It gets model changes from the ModelerModel
	 * and updates this view's objects accordingly. (Note that the ModelerModel
	 * typcially gets database events from the DbModel as well as explicit
	 * messages from clients).
	 * 
	 * @author Jeff Tassin
	 */
	public class ModelerViewEventHandler implements ModelerListener {
		/**
		 * ModelerModel Event handler
		 */
		public void eventFired(ModelerEvent evt) {
			TableId tableid = evt.getTableId();
			// any modeler state changes will already be set, so we can make
			// inquiries about
			// the table being passed in the event

			boolean isproto = getModeler().isPrototype(tableid);

			if (evt.getID() == ModelerEvent.TABLE_CHANGED) {
				if (isproto)
					tableChanged(getModeler().getConnection(), tableid);
			} else if (evt.getID() == ModelerEvent.TABLE_RENAMED) {
				TableId newid = (TableId) evt.getParameter(1);
				if (getModeler().isPrototype(newid)) {
					tableRenamed(getModeler().getConnection(), newid, tableid);
				}
			} else if (evt.getID() == ModelerEvent.TABLE_DELETED) {
				if (!isproto)
					tableDeleted(getModeler().getConnection(), tableid);
			} else if (evt.getID() == ModelerEvent.TABLE_CREATED) {
				if (isproto)
					tableCreated(getModeler().getConnection(), tableid);
			}
		}
	}

	/**
	 * Transfer handler for the table widget (allows draggin column meta data
	 * objects)
	 */
	public class ModelerTransferHandler extends TransferHandler {
		/**
		 * No imports
		 */
		public boolean canImport(JComponent comp, DataFlavor[] flavors) {
			return false;
		}

		/**
		 * Creates the transferable for our table widget
		 */
		protected Transferable createTransferable(JComponent comp) {
			if (comp instanceof JList) {
				MultiTransferable mt = new MultiTransferable();
				JList list = (JList) comp;
				Object[] values = list.getSelectedValues();
				for (int index = 0; index < values.length; index++) {
					TableId tableid = (TableId) values[index];
					DbObjectTransfer.addPrototype(mt, m_modeler.getConnection(), m_modeler.getTable(tableid));
				}
				return mt;
			}
			return null;
		}

		/**
		 * Always return copy for TableWidget
		 */
		public int getSourceActions(JComponent comp) {
			return TransferHandler.COPY;
		}

		/**
		 * No import for table widget
		 */
		public boolean importData(JComponent comp, Transferable t) {
			return false;
		}
	}

}
