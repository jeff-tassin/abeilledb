/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.split;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSplitPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class implements a custom split pane for the application framework. This
 * split pane offers a smaller divider bar, but adds a thumb that allows you to
 * move the divider. The dividers that come with swing are not very compelling
 * as user interfaces go. This is an attempt to improve the Swing split UI. We
 * accomplish the by using a layered pane (JLayeredPane). The layered pane
 * contains both the split pane instance (in one layer) and the thumb (in
 * another layer). We essentially draw the thumb on top of the split to simulate
 * that it is part of the pane. To use the class, first call createSplitPane to
 * create a CustomSplitPane instance. THEN, call getLayeredPane and add that
 * instance to your container.
 * 
 * @author Jeff Tassin
 */
public class CustomSplitPane extends JSplitPane {
	private boolean m_firsttime = true;
	private double m_divlocation = 0.5f;
	/**
	 * an integer value for the divider location. If not -1, then takes
	 * pecendence of the proportional value
	 */
	private int m_idivlocation = -1;

	private CustomDivider m_divider;

	/**
	 * this is the small button on the divider bar that we use to move the
	 * divider
	 */
	private SplitThumb m_thumb;

	/** split thumb alignment options */
	/**
	 * if vertical split, the thumb is placed in the top pane, lower right
	 * corner.
	 */
	public final static int TOP_LOWER_RIGHT = 1;

	/** the thumb alignment option */
	private int m_thumbalign;

	/**
	 * we use a JLayered pane to handle the thumb. This layered pane is a parent
	 * to this split pane instance
	 */
	private JLayeredPane m_layeredpane;

	static {
	}

	/**
	 * Creates an instance of a CustomSplitPane
	 * 
	 * @param orientation
	 *            the orientation defintion from JSplitPane (either
	 *            JSplitPane.VERTICAL_SPLIT or JSplitPane.HORIZONTAL_SPLIT )
	 *            Note, you don't add the split pane directly to a container.
	 *            After you create the split pane, call getLayeredPane and add
	 *            that object instead. The layered pane is a parent to this
	 *            object.
	 */
	public CustomSplitPane(int newOrientation) {
		super(newOrientation);
		// setUI( new CustomSplitPaneUI() );
		// setDividerSize(3);
		setDividerLocation(0.5f);
		setLayout(new CustomLayoutManager());
		// m_thumb = new SplitThumb();

		// m_layeredpane = new javax.swing.JLayeredPane( );
		// m_layeredpane.setLayout( new SplitLayoutManager(this) );
		// m_layeredpane.add( this, new Integer(0) );
		// m_layeredpane.add( m_thumb, new Integer(1) );
	}

	/**
	 * Override the add for the JSplitPane. Here we test if the added component
	 * is a JScrollPane. If so, we substitute our own layout manager to move the
	 * scroll bars so we can properly show the thumb.
	 */
	public Component add2(Component comp) {
		Component result = super.add(comp);
		if (isHorizontal()) {
			comp = getRightComponent();
			if (comp != null && comp instanceof JScrollPane) {
				JScrollPane scroll = (JScrollPane) comp;
				if (!(scroll.getLayout() instanceof CustomScrollLayout))
					scroll.setLayout(new CustomScrollLayout());
			}
		} else if (isVertical()) {
			comp = getTopComponent();
			if (comp != null && comp instanceof JScrollPane) {
				JScrollPane scroll = (JScrollPane) comp;
				if (!(scroll.getLayout() instanceof CustomScrollLayout))
					scroll.setLayout(new CustomScrollLayout());
			}
		}
		return result;
	}

	/**
	 * @return the layered pane (which is the parent) for this split pane
	 *         instance.
	 */
	private JLayeredPane getLayeredPane() {
		return m_layeredpane;
	}

	/**
	 * @return the split thumb button
	 */
	SplitThumb getThumb() {
		return m_thumb;
	}

	public boolean isHorizontal() {
		return (getOrientation() == JSplitPane.HORIZONTAL_SPLIT);
	}

	public boolean isVertical() {
		return (getOrientation() == JSplitPane.VERTICAL_SPLIT);
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		// if ( m_thumb != null )
		// m_thumb.syncPosition();
	}

	public void setDividerLocation(int divlocation) {
		if (m_firsttime) {
			m_idivlocation = divlocation;
		}
		super.setDividerLocation(divlocation);
	}

	public void setDividerLocation(double propLocation) {
		if (m_firsttime) {
			m_divlocation = propLocation;
			m_idivlocation = -1;
		}
		super.setDividerLocation(propLocation);
	}

	public void paint(Graphics g) {
		if (m_firsttime) {
			if (m_idivlocation == -1)
				setDividerLocation(m_divlocation);
			else
				setDividerLocation(m_idivlocation);
			// m_thumb.syncPosition();
			m_firsttime = false;
		}
		super.paint(g);
	}

	void setThumb(SplitThumb st) {
		m_thumb = st;
	}

	/**
	 * Sets the location of the thumb relative to a split pane
	 */
	public void setThumbAlignment(int align) {
		m_thumbalign = align;
	}

	public class CustomDivider extends BasicSplitPaneDivider {
		public CustomDivider(BasicSplitPaneUI ui) {
			super(ui);
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);

			if (m_thumb != null)
				m_thumb.syncPosition();
		}

		protected void dragDividerTo(int location) {
			repaint();
			super.dragDividerTo(location);

			if (isHorizontal()) {
				m_thumb.dragX(location);
			} else {
				m_thumb.dragY(location);
			}
		}

		public void drag(int location) {
			dragDividerTo(location);
		}

		public void finishDrag(int location) {
			finishDraggingTo(location);
		}
	}

	// layout manager for sql frame
	class CustomLayoutManager implements LayoutManager {
		LayoutManager m_del;

		CustomLayoutManager() {
			m_del = CustomSplitPane.this.getLayout();
		}

		public void addLayoutComponent(String name, Component comp) {
			m_del.addLayoutComponent(name, comp);
		}

		public void layoutContainer(Container parent) {
			if (m_firsttime)
				CustomSplitPane.this.setDividerLocation(m_divlocation);
			m_del.layoutContainer(parent);
			// if ( m_thumb != null )
			// m_thumb.syncPosition();
		}

		public Dimension minimumLayoutSize(Container parent) {
			return m_del.minimumLayoutSize(parent);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return m_del.preferredLayoutSize(parent);
		}

		public void removeLayoutComponent(Component comp) {
			m_del.removeLayoutComponent(comp);
		}
	}

	/**
	 * This class is the split thumb component that the user can drag to move
	 * the splitter in addition to moving the split bar itself
	 */
	public class SplitThumb extends JButton {
		private boolean m_dragging = false;
		private int m_y;

		private int m_deltax = 0;
		private int m_deltay = 0;

		private JScrollBar m_bar = new JScrollBar();

		private ImageIcon m_icon;

		private int THUMB_WIDTH = 16;
		private int THUMB_HEIGHT = 16;

		public SplitThumb() {

			if (CustomSplitPane.this.isHorizontal()) {
				setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				JScrollBar bar = new JScrollBar(JScrollBar.HORIZONTAL);
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
			}
			setSize(THUMB_WIDTH, THUMB_HEIGHT);
		}

		public void processMouseEvent(MouseEvent e) {

			if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				m_dragging = true;
				m_deltax = e.getX();
				m_deltay = e.getY();
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				m_dragging = false;

				if (isHorizontal()) {
					Point pt = SwingUtilities.convertPoint(SplitThumb.this, e.getPoint(), getParent());
					pt.x = pt.x - m_deltax;
					pt.y = SplitThumb.this.getY();
					setLocation(pt);
					JSplitPane sp = (JSplitPane) CustomSplitPane.this;
					CustomSplitPane.this.m_divider.finishDrag(pt.x - THUMB_WIDTH);
				} else {
					Point pt = SwingUtilities.convertPoint(SplitThumb.this, e.getPoint(), getParent());
					pt.y = pt.y - m_deltay;
					pt.x = SplitThumb.this.getX();
					setLocation(pt);
					JSplitPane sp = (JSplitPane) CustomSplitPane.this;
					CustomSplitPane.this.m_divider.finishDrag(pt.y + THUMB_HEIGHT);
				}
			}
		}

		public void processMouseMotionEvent(MouseEvent e) {
			if (m_dragging) {
				if (isHorizontal()) {
					Point pt = SwingUtilities.convertPoint(SplitThumb.this, e.getPoint(), getParent());
					pt.x = pt.x - m_deltax;
					pt.y = SplitThumb.this.getY();
					setLocation(pt);
					CustomSplitPane.this.m_divider.drag(pt.x);
				} else {
					Point pt = SwingUtilities.convertPoint(SplitThumb.this, e.getPoint(), getParent());
					pt.y = pt.y - m_deltay;
					pt.x = SplitThumb.this.getX();
					setLocation(pt);
					CustomSplitPane.this.m_divider.drag(pt.y + THUMB_HEIGHT);
				}

			}
		}

		/**
		 * Synchronizes the thumb with the JSplitPane divider
		 */
		void syncPosition() {
			if (isHorizontal()) {
				Container comp = (Container) CustomSplitPane.this.getRightComponent();
				if (comp != null) {
					int x = comp.getX();
					dragX(x);
				}
			} else {
				Container comp = (Container) CustomSplitPane.this.getTopComponent();
				if (comp != null) {
					int y = comp.getHeight();
					dragY(y);
				}
			}
		}

		/**
		 * Called when the user drags the divider to a new location
		 */
		void dragY(int location) {
			Container comp = (Container) CustomSplitPane.this.getTopComponent();
			Insets insets = comp.getInsets();
			int y = location - THUMB_HEIGHT;
			int x = comp.getWidth() - THUMB_WIDTH;
			m_y = y;
			setLocation(x, y);
		}

		/**
		 * Called when the user drags the divider to a new location
		 */
		void dragX(int location) {
			Container comp = (Container) CustomSplitPane.this.getRightComponent();
			Insets insets = comp.getInsets();
			int x = location;
			int y = comp.getHeight() - THUMB_HEIGHT;
			m_y = y;
			setLocation(x, y);
		}
	}

	public void setUI2(javax.swing.plaf.SplitPaneUI compUI) {
		if (compUI instanceof CustomSplitPaneUI)
			super.setUI(compUI);
		else {
			super.setUI(new CustomSplitPaneUI());
		}
	}

	public class CustomSplitPaneUI extends BasicSplitPaneUI {
		public BasicSplitPaneDivider createDefaultDivider() {
			m_divider = new CustomDivider(this);
			return m_divider;
		}
	}

	/**
	 * Custom layout for scroll pane to move the scroll bar
	 */
	public class CustomScrollLayout extends ScrollPaneLayout.UIResource {
		public CustomScrollLayout() {

		}

		public void layoutContainer(Container parent) {
			super.layoutContainer(parent);

			if (parent instanceof JScrollPane) {
				JScrollPane scroll = (JScrollPane) parent;
				JScrollBar vbar = scroll.getVerticalScrollBar();
				JScrollBar hbar = scroll.getHorizontalScrollBar();

				if (CustomSplitPane.this.isVertical()) {
					if (isVisible(vbar) && !isVisible(hbar)) {
						Dimension d = vbar.getSize();
						d.height -= m_thumb.getHeight();
						vbar.setSize(d);
					}
				} else if (CustomSplitPane.this.isHorizontal()) {
					if (isVisible(hbar)) {
						Dimension d = hbar.getSize();
						d.width -= m_thumb.getWidth();
						hbar.setSize(d);
						Point pt = hbar.getLocation();
						pt.x += m_thumb.getWidth();
						hbar.setLocation(pt);
					}
				}

			}
		}

		private boolean isVisible(Component comp) {
			return (comp != null && comp.isVisible());
		}
	}

}
