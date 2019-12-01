package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import java.awt.datatransfer.Transferable;

import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DebugGraphics;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.ColumnMetaDataRenderer;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a table in the model view. Currently, it is a JList
 * that can be resized and moved. The columns of the table are shown in the
 * list. Note that eventhough we don't store TableWidgets to disk, this class
 * must still be serializable for drag-n-drop/copy-paste.
 * 
 * @author Jeff Tassin
 */
public class TableWidget extends JComponent implements JETAExternalizable {
	/** serialization */
	static final long serialVersionUID = 6402156732647769536L;

	public static int VERSION = 1;

	/** the data model for this widget */
	private TableWidgetModel m_model;

	/**
	 * the main gui component that makes up this widget. Displays the columns in
	 * the given database table
	 */
	private transient JList m_list = new JList();
	private transient JScrollPane m_listscroll;

	/**
	 * we keep this border around because when we change Look&Feel, the scroll
	 * pane in the table widget seems to re-create its own border
	 */
	private static Border m_empty_border = BorderFactory.createEmptyBorder(0, 2, 0, 0);

	/**
	 * An array of LinkWidgets attached to this table. We reference the links
	 * here because we need to update their position when the user scrolls the
	 * scrollbar in the JList
	 */
	private transient LinkedList m_links = new LinkedList();

	/** the metadata renderer for each column */
	private transient ColumnMetaDataRenderer m_column_renderer;

	public static final Color INVALID_TABLE_COLOR = Color.red;

	public static final String ID_SHOW_DATA_TYPE = "table.widget.show.data.type";
	public static final String ID_SHOW_ICON = "table.widget.show.icon";

	/**
	 * Default ctor (needed for serialization)
	 */
	public TableWidget() {
		setUI(new TableWidgetUI());
	}

	/**
	 * ctor
	 */
	public TableWidget(TableWidgetModel model) {
		this();
		m_model = model;
		reset();
	}

	/**
	 * ctor
	 */
	public TableWidget(TableWidgetState tw_state) {
		this();
		m_model = tw_state.getModel();
		setBounds(tw_state.getBounds());
	}

	/**
	 * Adds a link widget to this view
	 */
	void addLink(LinkWidget link) {
		m_links.add(link);
	}

	/**
	 * Deselects any selected columns in the list
	 */
	void deselect() {
		m_list.clearSelection();
	}

	/**
	 * @return the catalog that this table belongs to
	 */
	public Catalog getCatalog() {
		return m_model.getCatalog();
	}

	/**
	 * @return the number of columns in the table represented by this widget
	 */
	public int getColumnCount() {
		return m_model.getColumnCount();
	}

	/**
	 * @return the set of columns (ColumnMetaData objects) in the table that
	 *         this widget represents
	 */
	public Collection getColumns() {
		return m_model.getColumns();
	}

	public TSConnection getConnection() {
		return m_model.getConnection();
	}

	/**
	 * @return the flag indicating whether columns from this table can be
	 *         dragged
	 */
	public boolean getDragEnabled() {
		return m_list.getDragEnabled();
	}

	/**
	 * @return the JList component that displays the columns in this table
	 */
	public JList getJList() {
		return m_list;
	}

	/**
	 * @return the link widgets attached to this table
	 */
	public Collection getLinkWidgets() {
		return m_links;
	}

	/**
	 * @return the underlying data model
	 */
	public TableWidgetModel getModel() {
		return m_model;
	}

	/**
	 * @return the scroll pane that owns the list
	 */
	public JComponent getListContainer() {
		return m_listscroll;
	}

	public JScrollPane getScrollPane() {
		return m_listscroll;
	}

	/**
	 * @return the underlying table metadata for this widget
	 */
	public TableMetaData getTableMetaData() {
		return m_model.getMetaData();
	}

	/**
	 * @return the table id for this table
	 */
	public TableId getTableId() {
		return m_model.getTableId();
	}

	/**
	 * @return the name of the table that is represented by this widget
	 */
	public String getTableName() {
		return m_model.getTableName();
	}

	/**
	 * @return the y offset in pixels (in the JList) relative to this widget's
	 *         origin of the given column
	 */
	public int getFieldY(String fieldName) {
		TableWidgetUI twui = (TableWidgetUI) ui;
		assert (twui != null);
		return twui.getFieldY(this, fieldName);
	}

	/**
	 * Resets this widget based on the items in the given table
	 */
	void reset() {
		// remove any components that might be added
		removeAll();

		m_column_renderer = ColumnMetaDataRenderer.createInstance(m_model.getConnection(), m_model.getMetaData());
		m_list.setBackground(Color.white);
		m_list.setCellRenderer(m_column_renderer);
		boolean showicon = TSUserPropertiesUtils.getBoolean(ID_SHOW_ICON, true);
		m_column_renderer.setShowIcon(showicon);

		int fontsize = m_model.getFontSize();
		Font f = javax.swing.UIManager.getFont("List.font");
		f = f.deriveFont((float) fontsize);
		m_list.setFont(f);

		f = f.deriveFont(Font.BOLD);
		setFont(f);

		// the default state is to allow the user to drag/copy columns from the
		// list
		m_list.setDragEnabled(true);
		m_list.setTransferHandler(new TableWidgetTransferHandler());
		m_list.setEnabled(true);
		m_list.setAutoscrolls(false);

		m_listscroll = new JScrollPane(m_list);
		m_listscroll.setBorder(m_empty_border);
		m_listscroll.setBackground(Color.white);
		add(m_listscroll);
		m_listscroll.setLocation(1, 28);
		m_listscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		DefaultListModel model = new DefaultListModel();
		m_list.setModel(model);

		Collection columns = m_model.getColumns();
		Iterator iter = columns.iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			model.addElement(cmd);
		}

		m_listscroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				Iterator iter = m_links.iterator();
				while (iter.hasNext()) {
					LinkWidget link = (LinkWidget) iter.next();
					link.recalc();
				}
			}
		});

		int width = getWidth();
		int height = getHeight();
		if (width == 0 || height == 0) {
			TableWidgetUI twui = (TableWidgetUI) ui;
			setSize(twui.getPreferredSize(this));
		}
		m_list.revalidate();
		m_list.doLayout();
		updateSettings();
	}

	/**
	 * @return true if the widget is not a prototype and the underlying database
	 *         model has not been/finished loaded
	 */
	public boolean isLoaded() {
		return m_model.isLoaded();
	}

	/**
	 * @return the value of the database saved flag. This indicates whether we
	 *         have saved this table to the database or not
	 */
	public boolean isSaved() {
		return m_model.isSaved();
	}

	/**
	 * @return true if the widget is not a prototype and cannot be found in the
	 *         database
	 */
	public boolean isValid() {
		return m_model == null ? true : m_model.isValid();
	}

	/**
	 * @remove the given link from this widget's list of links
	 */
	public void removeLink(Link link) {
		if (link == null)
			return;

		Iterator iter = m_links.iterator();
		while (iter.hasNext()) {
			LinkWidget w = (LinkWidget) iter.next();
			if (w.getLink().equals(link))
				iter.remove();
		}
	}

	/**
	 * Gets called when TableWidget is moved or resized. We need to redraw the
	 * widget in addition to telling its associated links to redraw themselves
	 */
	public void setBounds(int x, int y, int width, int height) {
		Iterator iter = m_links.iterator();

		while (iter.hasNext()) {
			LinkWidget link = (LinkWidget) iter.next();
			link.repaint();
		}

		super.setBounds(x, y, width, height);
		if (ui instanceof TableWidgetUI) {
			TableWidgetUI twui = (TableWidgetUI) ui;
			twui.recalcLayout(this);
		}

		iter = m_links.iterator();
		while (iter.hasNext()) {
			LinkWidget link = (LinkWidget) iter.next();
			link.recalc();
		}
	}

	/**
	 * Sets the flag that determines if the columns in this table can be dragged
	 * with the mouse
	 */
	public void setDragEnabled(boolean bDrag) {
		m_list.setEnabled(bDrag);
		m_list.setDragEnabled(bDrag);
	}

	/**
	 * Sets the data model for this widget
	 */
	public void setModel(TableWidgetModel model) {
		m_model = model;
		reset();
	}

	/**
	 * Updates the renderer settings for this widget
	 */
	public void updateSettings() {
		boolean showtype = TSUserPropertiesUtils.getBoolean(ID_SHOW_DATA_TYPE, true);
		boolean showicon = TSUserPropertiesUtils.getBoolean(ID_SHOW_ICON, true);
		m_column_renderer.setShowType(showtype);
		m_column_renderer.setShowIcon(showicon);
		m_column_renderer.setPrimaryKeyColor(TSUserPropertiesUtils.getColor(
				ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND,
				ModelViewSettings.getDefaultColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND)));

		m_column_renderer.setForeignKeyColor(TSUserPropertiesUtils.getColor(
				ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND,
				ModelViewSettings.getDefaultColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND)));
		getModel().updateSettings();
		m_list.revalidate();
		m_list.doLayout();
		revalidate();
		repaint();
	}

	public void updateUI() {
		Font f = getFont();
		super.updateUI();
		// we do this because the Metal Look and Feel changeds the Font in the
		// column renderer to bold
		setFont(f);

		if (m_listscroll != null) {
			m_listscroll.setBorder(m_empty_border);
		}

		if (ui instanceof TableWidgetUI) {
			TableWidgetUI twui = (TableWidgetUI) ui;
			twui.recalcLayout(this);
		}

	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		TableWidgetState tw_state = (TableWidgetState) in.readObject();

		m_model = tw_state.getModel();
		Rectangle rect = tw_state.getBounds();

		super.setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Provide our own serialization handling because we don't need/want to
	 * store the entire JComponent hierarchy for this class
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(new TableWidgetState(this));
	}

}
