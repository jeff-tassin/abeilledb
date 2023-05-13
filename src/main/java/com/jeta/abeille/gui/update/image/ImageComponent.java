package com.jeta.abeille.gui.update.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.update.ButtonPopup;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceProxy;
import com.jeta.abeille.gui.update.InstanceUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a view of an image This component has an icon and image
 * [icon][icon image]
 * 
 * @author Jeff Tassin
 */
public class ImageComponent extends InstanceComponent {
	private ButtonPopup m_filepopup = new ButtonPopup();
	// for receiving action events from this view
	// see addActionListener
	private LinkedList m_listeners = new LinkedList();
	private InstanceView m_view; // the view that contains this component

	private JLabel m_imageview;

	private int m_width;

	/** commands for popup */
	public static final String OPEN_FILE = "open file";
	public static final String SAVE_FILE = "save file";

	public ImageComponent(String fieldName, int dataType, InstanceView view) {
		super(fieldName, dataType);

		m_view = view;

		m_imageview = new JLabel();
		ImageScroller scroller = new ImageScroller(m_imageview);
		scroller.setPreferredSize(new Dimension(100, 120));
		setComponent(scroller);

		setIcon("openfile16.gif");
		getIconButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton) e.getSource();
				showPopup(btn, btn.getX() - 2, btn.getY());
			}
		});

		setLayout(new MyLayoutManager());

		// TSGuiToolbox.setTextFieldBorder( m_imageview );
	}

	/**
	 * Adds a listener to receive events for the actions that occur from this
	 * component. This allows listeners to get file open/save commands from the
	 * popup source - this object id - not used command - this is set to
	 * LOBComponent.SAVE_FILE or OPEN_FILE
	 * 
	 * @param listener
	 *            the action listener that will receive the events
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * @return the model for the view that owns this component
	 */
	public InstanceModel getModel() {
		return getView().getModel();
	}

	/**
	 * @return the popup menu that allows user to load/save file or launch an
	 *         application
	 */
	public ButtonPopup getPopup() {
		return m_filepopup;
	}

	/**
	 * @return the view object that owns this component
	 */
	public InstanceView getView() {
		return m_view;
	}

	/**
	 * @return the component that actually renders the data value. Some
	 *         components are contained in a scroll pane. If that is the case,
	 *         the properly gets the component contained in the scroll
	 */
	public JComponent getVisualComponent() {
		return m_imageview;
	}

	/**
	 * Sends the action event to all listeners who are interested in receiving
	 * events from this view. Currently, we are only sending out constraint
	 * button events.
	 * 
	 * @param evt
	 *            the action event to send
	 */
	public void notifyListeners(ActionEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		ImageController controller = (ImageController) getController();
		assert (controller != null);
		if (controller != null) {
			controller.prepareStatement(count, pstmt);
		}
	}

	/**
	 * Sets the image for this component
	 */
	public void setImage(byte[] data) {
		m_imageview.setIcon(new ImageIcon(data));
		super.setValue(data);
	}

	/**
	 * Sets the value displayed by this field
	 */
	public void setValue(Object value) {
		InstanceModel model = m_view.getModel();
		InstanceProxy proxy = (InstanceProxy) model.getInstanceProxy();
		if (value != null) {
			try {
				Object lob = InstanceUtils.getBinaryData(value, proxy, getFieldName());
				if (lob instanceof byte[]) {
					super.setValue(lob);
					ImageIcon icon = new ImageIcon((byte[]) lob);
					m_imageview.setIcon(icon);
				} else {
					super.setValue(lob);
				}
			} catch (Exception e) {
				// @todo handle better here
				TSUtils.printException(e);
			}
		} else {
			m_imageview.setIcon(null);
			super.setValue(null);
		}
	}

	/**
	 * Sets the width of this component.
	 */
	public void setWidth(int width) {
		JComponent comp = getComponent();
		Dimension d = comp.getPreferredSize();
		d.width = width - getComponentX();
		m_width = d.width;
		comp.setPreferredSize(d);
		comp.setSize(d);
		comp.repaint();
		comp.validate();
	}

	/**
	 * Shows the constraint popup
	 */
	public void showPopup(JButton sourceBtn, int x, int y) {
		// Dimension d = new Dimension( 18, 16*2 - 2 );
		// m_filepopup.setPreferredSize( d );
		Dimension d = m_filepopup.getPreferredSize();
		m_filepopup.setMaximumSize(d);
		m_filepopup.setSize(d);
		m_filepopup.setSourceObject(sourceBtn);
		m_filepopup.show(this, x, y);
	}

	/**
	 * Gets the value that is displayed in the component and calls setValue on
	 * this object.
	 */
	public void syncValue() {

	}

	/**
	 * @return the sql representation of this value. Binary objects are not
	 *         supported.
	 */
	public String toSQLString(SQLFormatter formatter) {
		return "null";
	}

	class MyLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			JComponent component = getComponent();
			JComponent iconbtn = getIconButton();
			Dimension d = component.getPreferredSize();
			d.width = m_width;
			component.setSize(d);
			component.setLocation(getComponentX(), 0);
			component.doLayout();
			iconbtn.setLocation(0, 0);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(12, 12);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

	/**
	 * Scroll for scrolling images in JLable
	 */
	class ImageScroller extends javax.swing.JScrollPane {
		public ImageScroller(JLabel label) {
			super();
			javax.swing.JPanel p = new javax.swing.JPanel();
			p.setBackground(java.awt.Color.white);
			p.setLayout(new BorderLayout());

			p.add(label, BorderLayout.CENTER);
			getViewport().add(p);
			getHorizontalScrollBar().setUnitIncrement(10);
			getVerticalScrollBar().setUnitIncrement(10);
		}
	}

}
