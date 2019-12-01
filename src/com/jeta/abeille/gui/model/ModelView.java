package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JList;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DefaultLinkModel;
import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.LinkModel;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.gui.canvas.ModelCanvas;
import com.jeta.foundation.gui.components.BasicPopupMenu;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.table.CopyListOptionsPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.CompositeComponentFinder;
import com.jeta.open.support.DefaultComponentFinder;

/**
 * This is the 'canvas' that graphically shows table properties and their
 * relationships to other tables.
 * 
 * @author Jeff Tassin
 */
public class ModelView extends ModelCanvas implements Printable {
	/** for debugging only */
	private java.awt.geom.Rectangle2D.Float m_testrect;

	/** the data model for this view */
	private ModelViewModel m_model;

	/**
	 * The link we are dragging
	 */
	private DragLink m_draglink;

	/**
	 * The ui responsible for allowing users to drag links from one table to
	 * another
	 */
	private LinkUI m_linkui;

	/**
	 * The mouse listener for table widgets. Basically, we listen for popup
	 * trigger events and pass them on to the view
	 */
	private MouseAdapter m_mousehandler;

	/**
	 * this is the popup context menu that shows copy/cut/paste and others for
	 * model view
	 */
	private BasicPopupMenu m_popup;

	/** the mouse handler for the popup context menu */
	private PopupHandler m_popuphandler = new PopupHandler();

	/** this is the popup context menu that shows copy for column objects */
	private JPopupMenu m_column_popup;

	/** the mouse handler for the popup context menu */
	private ColumnMetaDataPopupHandler m_column_popup_handler = new ColumnMetaDataPopupHandler();

	/** the model event handler */
	private ModelViewModelEventHandler m_mvmlistener = new ModelViewModelEventHandler();

	/** we keep this around so we can easily calculate the link terminal sizes */
	private FontMetrics m_fontmetrics;

	/**
	 * ctor Initializes the view with the default link UI
	 */
	public ModelView(ModelViewModel model) {
		initialize(model);
		LinkUI linkui = new LinkUI(this);
		setLinkUI(linkui);
		// initial state
		linkui.setEnabled(false);
	}

	/**
	 * ctor Initializes the view with the given link UI
	 */
	public ModelView(ModelViewModel model, LinkUI linkui) {
		initialize(model);
		setLinkUI(linkui);
	}

	/**
	 * Creates a component finder that is used to locate child components in
	 * this panel by name.
	 */
	protected ComponentFinder createComponentFinder() {
		CompositeComponentFinder finder = new CompositeComponentFinder(new DefaultComponentFinder(this));
		/** must add menus here because they are not owned by this container */
		finder.add(new DefaultComponentFinder(m_popup));
		return finder;
	}

	/**
	 * Called when a catalog has been changed or reloaded. Basically, we need to
	 * reload every widget in the view that belongs to the catalog.
	 */
	public void catalogChanged(Catalog cat) {
		if (cat == null)
			return;

		LinkedList tables = new LinkedList();
		tables.addAll(getTableWidgets());
		Iterator iter = tables.iterator();
		while (iter.hasNext()) {
			TableWidget tw = (TableWidget) iter.next();
			if (cat.equals(tw.getCatalog())) {
				tableChanged(tw.getTableId());
			}
		}
	}

	/**
	 * @return true if this view contains the given table widget
	 */
	public boolean contains(TableWidget widget) {
		int count = getComponentCount();
		for (int index = 0; index < count; index++) {
			JComponent comp = (JComponent) getComponent(index);
			if (comp == widget)
				return true;
		}
		return false;
	}

	/**
	 * Creates the link widget in the model and adds to this canvas
	 */
	public LinkWidget addLinkWidget(Link link) {
		FontMetrics fm = getFontMetrics();
		LinkWidget result = m_model.createLinkWidget(link);
		if (result != null) {
			result.setContainer(this);
			result.setTerminalHeight(fm.getHeight());
			result.recalc();
		}
		touchModifiedTime();
		return result;
	}

	/**
	 * Adds the given widget to this canvas and sets up the UI for the widget
	 */
	private void addWidgetToCanvas(TableWidget w) {
		// add the widget to the datamodel
		Collection linkwidgets = w.getLinkWidgets();
		Iterator iter = linkwidgets.iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			lw.setContainer(this);
			lw.setTerminalHeight(getFontMetrics().getHeight());
		}

		JList list = w.getJList();
		// support copying selected columns from the list to the clipboard
		list.addMouseListener(m_column_popup_handler);

		// add mouse listeners to the list component so we can support dragging
		// links
		if (m_linkui != null) {
			list.addMouseMotionListener(m_linkui);
			list.addMouseListener(m_linkui);
			w.setDragEnabled(!m_linkui.isEnabled());
		}

		w.setVisible(true);
		w.addMouseListener(m_mousehandler);

		add(w, 0);
		repaint();
	}

	/**
	 * For debugging purposes
	 */
	private void debugDraw(Graphics2D g2) {
		if (m_testrect != null) {
			g2.setPaint(java.awt.Color.blue);
			g2.drawRect((int) m_testrect.getX(), (int) m_testrect.getY(), (int) m_testrect.getWidth(),
					(int) m_testrect.getHeight());
		}
	}

	/**
	 * Override from ModelCanvas so we can tell each table widget to deselect
	 * any selected columns in this list
	 */
	public void deselectAll() {
		super.deselectAll();
		for (int index = 0; index < getComponentCount(); index++) {
			java.awt.Component comp = getComponent(index);
			if (comp instanceof TableWidget) {
				TableWidget tw = (TableWidget) comp;
				tw.deselect();
			}
		}
	}

	/**
	 * Override from ModelCanvas so we can tell each table widget to deselect
	 * any selected columns in this list
	 */
	public void deselectComponent(JComponent c) {
		super.deselectComponent(c);
		for (int index = 0; index < getComponentCount(); index++) {
			java.awt.Component comp = getComponent(index);
			if (comp instanceof TableWidget) {
				TableWidget tw = (TableWidget) comp;
				tw.deselect();
			}
		}
	}

	/**
	 * Allows the user to create links from one table to another
	 */
	public void enableLinkTool(boolean bEnable) {
		Collection c = getTableWidgets();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidget w = (TableWidget) iter.next();
			// if the link tool is enabled, the drag is disabled
			w.setDragEnabled(!bEnable);
		}

		getLinkUI().enableLinkCreation(bEnable);
		getLinkUI().setEnabled(true);
	}

	/**
	 * @return the minimum rectangle the encloses all the table widgets and link
	 *         widgets in the view.
	 */
	public Rectangle getComponentBounds() {
		int min_x = getWidth();
		int min_y = getHeight();

		int max_x = 0;
		int max_y = 0;

		// first iterate over all table widgets to get the min/max bounds
		Collection widgets = getTableWidgets();
		Iterator iter = widgets.iterator();
		while (iter.hasNext()) {
			TableWidget w = (TableWidget) iter.next();
			if (min_x > w.getX())
				min_x = w.getX();

			int right = w.getX() + w.getWidth();
			if (max_x < right)
				max_x = right;

			if (min_y > w.getY())
				min_y = w.getY();

			int bottom = w.getY() + w.getHeight();
			if (max_y < bottom)
				max_y = bottom;
		}

		// now iterate over all link widgets to get the min/max bounds
		iter = getLinkWidgets().iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			Rectangle[] lr = lw.getBoundingRects();
			for (int index = 0; index < lr.length; index++) {
				Rectangle rect = lr[index];
				if (min_x > rect.x)
					min_x = rect.x;

				if (min_y > rect.y)
					min_x = rect.y;

				if (max_x < (rect.x + rect.width))
					max_x = rect.x + rect.width;

				if (max_y < (rect.y + rect.height))
					max_y = rect.y + rect.height;
			}
		}

		return new Rectangle(min_x, min_y, max_x - min_x, max_y - min_y);
	}

	/**
	 * @return the connection associated with this view
	 */
	public TSConnection getConnection() {
		return m_model.getConnection();
	}

	/**
	 * @return the link we are currently dragging
	 */
	public DragLink getDragLink() {
		return m_draglink;
	}

	/**
	 * @return the font metrics that form the basis for this view. In this case,
	 *         this is the font found in the JList the table widgets set to the
	 *         current font size for the view.
	 */
	public FontMetrics getFontMetrics() {
		if (m_fontmetrics == null) {
			Font f = javax.swing.UIManager.getFont("List.font");
			f = f.deriveFont((float) TableWidgetModel.getFontSize());
			m_fontmetrics = getFontMetrics(f);
		}
		return m_fontmetrics;
	}

	/**
	 * @return the controller instance that is responsible for dragging links on
	 *         the canvas
	 */
	LinkUI getLinkUI() {
		return m_linkui;
	}

	/**
	 * @return the collection of link widgets in this view
	 */
	public LinkedList getLinkWidgets() {
		return m_model.getLinkWidgets();
	}

	/**
	 * @return the underlying data model for this viewn
	 */
	public ModelViewModel getModel() {
		return m_model;
	}

	/**
	 * @return the context popup menu for this view
	 */
	public BasicPopupMenu getPopup() {
		return m_popup;
	}

	/**
	 * @return the currently selected table in the current view. If more than
	 *         one table is selected, the table with the current focus is
	 *         selected from the view. If no tables are selected, null is
	 *         returned.
	 */
	public TableWidget getSelectedTableWidget() {
		Iterator iter = getSelectedItems().iterator();
		if (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof TableWidget) {
				return (TableWidget) obj;
			}
		}
		return null;
	}

	/**
	 * @return the currently selected table in the current view. If more than
	 *         one table is selected, the table with the current focus is
	 *         selected from the view. If no tables are selected, null is
	 *         returned.
	 */
	public TableMetaData getSelectedTable() {
		Iterator iter = getSelectedItems().iterator();
		if (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof TableWidget) {
				TableMetaData tmd = ((TableWidget) obj).getTableMetaData();
				return tmd;
			}
		}
		return null;
	}

	/**
	 * @return the currently selected tables (TableWidget objects) in the
	 *         current view
	 */
	public Collection getSelectedTables() {
		LinkedList results = null;
		Iterator iter = getSelectedItems().iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof TableWidget) {
				if (results == null) {
					results = new LinkedList();
				}
				results.add(obj);
			}
		}
		return (results == null ? com.jeta.foundation.utils.EmptyCollection.getInstance() : results);
	}

	/**
	 * @return the set of table ids that is currently on this view
	 */
	public Collection getTables() {
		return m_model.getTables();
	}

	/**
	 * @return the set of table widgets that is currently on this view
	 */
	public Collection getTableWidgets() {
		return m_model.getTableWidgets();
	}

	/**
	 * @return the table widget for the givent table id
	 */
	TableWidget getTableWidget(TableId id) {
		return m_model.getTableWidget(id);
	}

	/**
	 * @return the name of this view
	 */
	public String getViewName() {
		return m_model.getViewName();
	}

	/**
	 * Common initialization of the view with the given model
	 */
	private void initialize(ModelViewModel model) {
		m_model = model;
		m_model.addListener(m_mvmlistener);

		/**
		 * Create the mouse handler to send popup trigger events to the view for
		 * each table widget. This allows the user to right click on a table
		 * widget and still invoke the context menu
		 */
		m_mousehandler = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					MouseListener[] mls = (MouseListener[]) (ModelView.this.getListeners(MouseListener.class));
					for (int index = 0; index < mls.length; index++) {
						mls[index].mousePressed(e);
					}
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					MouseListener[] mls = (MouseListener[]) (ModelView.this.getListeners(MouseListener.class));
					for (int index = 0; index < mls.length; index++) {
						mls[index].mouseReleased(e);
					}
				}
			}
		};

		m_popup = new BasicPopupMenu(this);
		m_popup.addSeparator();
		m_popup.add(i18n_createMenuItem("Table Properties", ModelViewNames.ID_TABLE_PROPERTIES, null));
		m_popup.add(i18n_createMenuItem("Copy As New", ModelViewNames.ID_COPY_AS_NEW, null));
		addMouseListener(m_popuphandler);

		m_column_popup = new JPopupMenu();
		JMenuItem item = new JMenuItem(I18N.getLocalizedMessage("Copy"));
		item.setName(TSComponentNames.ID_COPY);
		item.addActionListener(new ColumnMetaDataPopupListener());
		m_column_popup.add(item);

		item = new JMenuItem(I18N.getLocalizedMessage("Copy Special"));
		item.setName(TSComponentNames.ID_COPY_SPECIAL);
		item.addActionListener(new ColumnMetaDataPopupListener(true));
		m_column_popup.add(item);

		Collection c = getTableWidgets();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidget w = (TableWidget) iter.next();
			addWidgetToCanvas(w);
		}

		c = getLinkWidgets();
		iter = c.iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			lw.setContainer(this);
		}
		resetLinkTerminals();
	}

	/**
	 * @return true if the the user is running in evaluation mode
	 */
	public boolean isEvaluation() {
		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		return jlm.isEvaluation();
	}

	/**
	 * Override paint so we can draw the dragging link if it is being dragged
	 */
	public void paint(Graphics g) {
		super.paint(g);

		// call this last so that the drag link is drawn over everything else
		if (m_draglink != null && m_draglink.isDragging())
			m_draglink.paintComponent(g, this);
	}

	/**
	 * Renders the link widgets on the canvas.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Iterator iter = getLinkWidgets().iterator();
		while (iter.hasNext()) {
			LinkWidget lw = (LinkWidget) iter.next();
			lw.paintComponent(g, this);
		}
	}

	/**
	 * Prints the message "Evaluation" on the printer graphics
	 */
	public void paintEvaluationMessage(Graphics pg, int org_x, int org_y, int wPage, int hPage) {
		Font f = javax.swing.UIManager.getFont("Table.font");
		FontMetrics fm = getFontMetrics(f);

		int width = fm.stringWidth(I18N.getLocalizedMessage("Evaluation"));
		int new_point_size = (f.getSize() * wPage - 50) / width - 2;
		f = f.deriveFont((float) new_point_size);
		fm = getFontMetrics(f);
		width = fm.stringWidth(I18N.getLocalizedMessage("Evaluation"));

		Font old_font = pg.getFont();
		pg.setFont(f);
		Color old_color = pg.getColor();
		pg.setColor(Color.lightGray);
		pg.drawString(I18N.getLocalizedMessage("Evaluation"), org_x + (wPage - width) / 2, org_y + hPage / 2);
		pg.setFont(old_font);
		pg.setColor(old_color);
	}

	/**
	 * Printable implementation
	 */
	public int print(Graphics pg, PageFormat pageFormat, int pageIndex) throws PrinterException {
		Color c = getBackground();
		try {

			Collection widgets = getTableWidgets();
			if (widgets.size() == 0)
				return Printable.NO_SUCH_PAGE;

			int max_x = 0;
			int max_y = 0;
			Iterator iter = widgets.iterator();
			while (iter.hasNext()) {
				TableWidget w = (TableWidget) iter.next();
				int right = w.getX() + w.getWidth();
				if (max_x < right)
					max_x = right;

				int bottom = w.getY() + w.getHeight();
				if (max_y < bottom)
					max_y = bottom;
			}

			int wPage = (int) pageFormat.getImageableWidth();
			int hPage = (int) pageFormat.getImageableHeight();

			int cols = max_x / wPage + 1;
			int rows = max_y / hPage + 1;

			int total_pages = cols * rows;
			int row = pageIndex / cols;
			int col = pageIndex % cols;
			if (cols == 1) {
				col = 0;
			}

			if (pg == null) {
				return (pageIndex < total_pages) ? Printable.PAGE_EXISTS : Printable.NO_SUCH_PAGE;
			} else {
				setBackground(Color.white);
				pg.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
				pg.setClip(0, 0, wPage, hPage);
				if (pageIndex == 0) {
					paint(pg);
					if (isEvaluation()) {
						paintEvaluationMessage(pg, 0, 0, (int) pageFormat.getImageableWidth(),
								(int) pageFormat.getImageableHeight());
					}
					return Printable.PAGE_EXISTS;
				} else if (pageIndex < total_pages) {
					int x_org = col * wPage;
					int y_org = row * hPage;
					int clip_width = wPage;
					int clip_height = hPage;

					pg.translate(-x_org, -y_org);

					if ((x_org + clip_width) > getWidth()) {
						clip_width = getWidth() - x_org;
						if (clip_width < 0)
							clip_width = 0;
					}

					if ((y_org + clip_height) > getHeight()) {
						clip_height = getHeight() - y_org;
						if (clip_height < 0)
							clip_height = 0;
					}

					pg.setClip(x_org, y_org, clip_width, clip_height);
					paint(pg);
					if (isEvaluation()) {
						paintEvaluationMessage(pg, x_org, y_org, (int) pageFormat.getImageableWidth(),
								(int) pageFormat.getImageableHeight());
					}
					return Printable.PAGE_EXISTS;
				} else {
					return Printable.NO_SUCH_PAGE;
				}
			}
		} finally {
			setBackground(c);
		}
	}

	/**
	 * Removes the given link widget from the table
	 */
	public LinkWidget removeLinkWidget(LinkWidget link) {
		m_model.removeLinkWidget(link);
		touchModifiedTime();
		return null;
	}

	/**
	 * Removes the given link widget from the table
	 */
	public LinkWidget removeLinkWidget(Link link) {
		m_model.removeLinkWidget(link);
		touchModifiedTime();
		return null;
	}

	/**
	 * Removes the given table widget from the canvas. This also removes all
	 * links connected to that table
	 */
	private void removeWidget(TableWidget w) {
		if (w == null)
			return;

		boolean bfound = false;
		for (int index = 0; index < getComponentCount(); index++) {
			java.awt.Component comp = getComponent(index);
			if (comp == w) {
				bfound = true;
			}
		}

		// if ( bfound )
		// System.out.println(
		// "ModelView.removeWidget  found component to remove: " +
		// w.getTableId() );
		// else
		// System.out.println(
		// "ModelView.removeWidget  did NOT find component to remove: " +
		// w.getTableId() );

		remove(w); // remove Component from container
		repaint();
	}

	/**
	 * Updates the terminal sizes for each link. This is needed during startup
	 * or when we change the font size for each table widget. The link terminal
	 * size is proportinal to the font size
	 */
	void resetLinkTerminals() {
		FontMetrics fm = getFontMetrics();
		if (fm != null) {
			Iterator iter = getLinkWidgets().iterator();
			while (iter.hasNext()) {
				LinkWidget lw = (LinkWidget) iter.next();
				lw.getSourceTerminal().setMaximumHeight(fm.getHeight());
				lw.getDestinationTerminal().setMaximumHeight(fm.getHeight());
				lw.recalc();
			}
		}
		repaint();
	}

	/**
	 * Sets the widget used to represent a dragged link on the canvas.
	 */
	public void setDragLink(DragLink draglink) {
		m_draglink = draglink;
		if (m_draglink != null) {
			FontMetrics fm = getFontMetrics();
			m_draglink.setTerminalHeight(fm.getHeight());
		}
	}

	/**
	 * Sets the font size for all components in the view
	 */
	public void setFontSize(int fontsize) {
		FontMetrics fm = null;
		float pct_width = 0.0f;
		float pct_height = 0.0f;
		Font old_font = null;
		Font new_font = null;
		Font new_list_font = null;

		Collection c = getTableWidgets();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidget widget = (TableWidget) iter.next();
			JList list = widget.getJList();

			if (fm == null) {
				old_font = widget.getFont();
				fm = widget.getFontMetrics(old_font);

				float old_width = (float) TSGuiToolbox.calculateAverageTextWidth(widget, 25);
				float old_height = (float) fm.getHeight();

				new_font = old_font.deriveFont((float) fontsize);
				widget.setFont(new_font);

				Font f = list.getFont();
				new_list_font = f.deriveFont((float) fontsize);
				list.setFont(new_list_font);

				float new_width = (float) TSGuiToolbox.calculateAverageTextWidth(widget, 25);
				float new_height = (float) widget.getFontMetrics(new_font).getHeight();

				/** update the font metrics to the latest */
				m_fontmetrics = list.getFontMetrics(f);

				try {
					pct_width = (new_width - old_width) / 100.0f;
					pct_height = (new_height - old_height) / 100.0f;
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}

			widget.setFont(new_font);
			list.setFont(new_list_font);

			widget.setBounds(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
			widget.revalidate();
			widget.doLayout();
			widget.repaint();
		}

		touchModifiedTime();
		resetLinkTerminals();
	}

	/**
	 * Sets the link ui for this view.
	 */
	private void setLinkUI(LinkUI linkui) {
		assert (m_linkui == null);
		m_linkui = linkui;
		// also have the linkui respond to mouse events over the canvas so it
		// can
		// handle selection and moving/deleting/dragging of links
		addMouseMotionListener(m_linkui);
		addMouseListener(m_linkui);

		// now set the linkui on all the tables once it has been created
		// this allows us to create/drag links from one table to another
		Collection c = getTableWidgets();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidget w = (TableWidget) iter.next();
			try {
				JList list = w.getJList();
				list.addMouseMotionListener(m_linkui);
				list.addMouseListener(m_linkui);

				// let's foce the widget to do a layout
				// this will cause the TableWidgetUI to properly set the sizes
				// of
				// the JList and ScrollPane in the widget
				// we do this here because we know it is called from the
				// constructor and is
				// the last thing done in the ctor
				w.setBounds(w.getBounds());
			} catch (Exception e) {
				// this could fail if the table was deleted from the database,
				// but not from our canvas
				// for some reason
				e.printStackTrace();
				remove(w);
				iter.remove();
			}
		}
	}

	/**
	 * Sets the name of this view. This is currently used in the view tab in the
	 * ModelViewFrame.
	 */
	public void setViewName(String viewName) {
		m_model.setViewName(viewName);
	}

	/**
	 * Called when a table's metadata has changed. Reset the given widget and
	 * all widgets that are related to this table
	 */
	public void tableChanged(TableId tableId) {
		if (TSUtils.isDebug()) {
			// System.out.println( "ModelView.tableChanged: " + tableId );
		}

		TableWidget widget = getTableWidget(tableId);
		if (widget != null) {
			// just remove the table and re-add it
			Rectangle rect = widget.getBounds();
			// System.out.println( "ModelView.tableChanged: " + tableId );
			m_model.removeWidget(widget);
			// System.out.println(
			// "ModelView.tableChanged . removed table, now re-adding" );
			TableWidget tw = m_model.addTable(tableId, rect);
			// TableMetaData tmd = tw.getTableMetaData();
			// tmd.print();
		}
		touchModifiedTime();
	}

	/**
	 * Renames the table in the view and updates any linked tables
	 */
	public void tableRenamed(TableId newId, TableId oldId) {
		// System.out.println( "ModelView.tableRenamed  newId = " +
		// newId.getFullyQualifiedName() );
		// System.out.println( "ModelView.tableRenamed  oldId = " +
		// oldId.getFullyQualifiedName() );
		TableWidget updatewidget = getTableWidget(oldId);
		if (updatewidget != null) {
			// we need this to prevent concurrent modification of widgets in
			// model while iterating
			LinkedList updates = new LinkedList();

			Collection widgets = m_model.getTableWidgets();
			Iterator iter = widgets.iterator();
			while (iter.hasNext()) {
				TableWidget widget = (TableWidget) iter.next();
				Collection links = widget.getLinkWidgets();
				Iterator liter = links.iterator();
				while (liter.hasNext()) {
					LinkWidget linkwidget = (LinkWidget) liter.next();
					Link link = linkwidget.getLink();
					if (oldId.equals(link.getDestinationTableId()) && widget != updatewidget) {
						updates.add(widget);
						break; // out of inner loop
					}
				}
			}

			// you need to remove the rename table first, then iterate over all
			// tables that
			// reference the rename table and re-add those tables
			m_model.removeWidget(updatewidget);

			// now remove and re-add the updated tables so that the links can be
			// regenerated
			iter = updates.iterator();
			while (iter.hasNext()) {
				TableWidget widget = (TableWidget) iter.next();
				m_model.removeWidget(widget);
				m_model.addTable(widget.getTableId(), widget.getBounds());
			}

			m_model.addTable(newId, updatewidget.getBounds());
		}
		touchModifiedTime();

	}

	/**
	 * Called when the user has changed some preferences and we need to update
	 * the views to refresh based on the the new preferences.
	 */
	public void updateSettings() {
		Collection c = getTableWidgets();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			TableWidget w = (TableWidget) iter.next();
			w.updateSettings();
		}
	}

	public void updateUI() {
		super.updateUI();
		Component[] comps = getComponents();
		for (int index = 0; index < comps.length; index++) {
			Component comp = comps[index];
			if (comp instanceof JComponent) {
				((JComponent) comp).updateUI();
			}
		}
	}

	/**
	 * This is the event handler for handling events from the model
	 */
	public class ModelViewModelEventHandler implements ModelViewModelListener {
		/**
		 * This will only be fired by the model when a new widget has been added
		 */
		public void eventFired(ModelViewModelEvent evt) {
			TableId id = evt.getTableId();
			if (evt.getID() == ModelViewModelEvent.TABLE_ADDED) {
				TableWidget widget = getTableWidget(id);
				addWidgetToCanvas(widget);

				if (TSUtils.isTest()) {
					com.jeta.abeille.test.JETATestFactory.runTest("test.jeta.abeille.gui.model.ModelViewValidator",
							ModelView.this);
					try {
						// ModelerModel modeler = ModelerModel.createInstance(
						// getConnection() );
						// com.jeta.abeille.test.JETATestFactory.runTest(
						// "test.jeta.abeille.gui.model.ModelerValidator",
						// modeler );
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} else if (evt.getID() == ModelViewModelEvent.TABLE_REMOVED) {
				TableWidget widget = (TableWidget) evt.getParameter(1);
				removeWidget(widget);

				if (TSUtils.isTest()) {
					com.jeta.abeille.test.JETATestFactory.runTest("test.jeta.abeille.gui.model.ModelViewValidator",
							ModelView.this);
					try {
						// ModelerModel modeler = ModelerModel.createInstance(
						// getConnection() );
						// com.jeta.abeille.test.JETATestFactory.runTest(
						// "test.jeta.abeille.gui.model.ModelerValidator",
						// modeler );
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Listener on the popup menu trigger
	 */
	public class PopupHandler extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				m_popup.show((javax.swing.JComponent) e.getSource(), e.getX(), e.getY());
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				m_popup.show((javax.swing.JComponent) e.getSource(), e.getX(), e.getY());
			}
		}
	}

	/**
	 * Listener on the popup menu trigger
	 */
	public class ColumnMetaDataPopupHandler extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				m_column_popup.show((javax.swing.JComponent) e.getSource(), e.getX(), e.getY());
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				m_column_popup.show((javax.swing.JComponent) e.getSource(), e.getX(), e.getY());
			}
		}
	}

	/**
	 * Popup handler when user right clicks on a column in a TableWidget list.
	 * Allows us to copy all selected column object in all table widgets.
	 */
	public class ColumnMetaDataPopupListener implements ActionListener {
		/** flag that indicates if we should show the copy special dialog */
		private boolean m_copy_special;

		/**
		 * ctor
		 */
		public ColumnMetaDataPopupListener() {
			m_copy_special = false;
		}

		/**
		 * ctor
		 */
		public ColumnMetaDataPopupListener(boolean special) {
			m_copy_special = special;
		}

		public void actionPerformed(ActionEvent evt) {
			MultiTransferable mt = new MultiTransferable();
			boolean qualified = false;

			if (m_copy_special) {
				// the copy options dialog
				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, ModelView.this, true);
				com.jeta.abeille.gui.model.common.CopyColumnsPanel panel = com.jeta.abeille.gui.model.common.CopyColumnsPanel
						.createPanel();
				dlg.setTitle(I18N.getLocalizedMessage("Copy Special"));
				dlg.setPrimaryPanel(panel);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
				if (dlg.isOk()) {
					mt.setExportModel(panel.getModel());
					qualified = panel.isIncludeTable();
				}
			}

			Collection widgets = getTableWidgets();
			Iterator iter = widgets.iterator();
			while (iter.hasNext()) {
				TableWidget tw = (TableWidget) iter.next();
				JList list = tw.getJList();
				Object[] values = list.getSelectedValues();
				for (int index = 0; index < values.length; index++) {
					ColumnMetaData cmd = (ColumnMetaData) values[index];
					DbObjectTransfer.addTransferable(mt, cmd, qualified);
				}
			}
			Toolkit kit = Toolkit.getDefaultToolkit();
			Clipboard clipboard = kit.getSystemClipboard();
			clipboard.setContents(mt, null);
		}
	}

}
