package com.jeta.abeille.gui.update;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a binary component in the UpdateFrame This component
 * has an icon and a read only text field showing the size in bytes of the
 * object [icon][JTextField]
 * 
 * @author Jeff Tassin
 */
public class LOBComponent extends BasicTextComponent {
	private String m_displaymsg; // this is the message shown in the text field
									// for all binary objects (locale specific)
	private ButtonPopup m_filepopup = new ButtonPopup();
	// for receiving action events from this view
	// see addActionListener
	private LinkedList m_listeners = new LinkedList();
	private InstanceView m_view; // the view that contains this component

	public LOBComponent(String fieldName, int dataType, InstanceView view, String displayMsg) {
		super(fieldName, dataType, false);

		m_displaymsg = displayMsg;
		m_view = view;

		setListenDocumentEvents(false);

		getTextField().setEditable(false);

		setIcon("openfile16.gif");
		getIconButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton) e.getSource();
				showPopup(btn, btn.getX() - 2, btn.getY());
			}
		});
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
	 * Clears the current control
	 */
	public void clear() {
		setValue(null);
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
		LOBController controller = (LOBController) getController();
		assert (controller != null);
		if (controller != null) {
			controller.prepareStatement(count, pstmt);
		}
	}

	/**
	 * Sets the value displayed by this field
	 */
	public void setValue(Object value) {
		InstanceModel model = m_view.getModel();
		InstanceProxy proxy = (InstanceProxy) model.getInstanceProxy();
		if (value == null) {
			super.setValue(null);
		} else {
			try {
				if (value instanceof java.sql.Blob || value instanceof java.sql.Clob) {
					super.setValue(value);
				} else if (value instanceof java.io.File) {
					super.setValue(value);
				} else {
					Object data = InstanceUtils.getBinaryData(value, proxy, getFieldName());
					super.setValue(data);
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
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

	// /////////////////////////////////////////////////////////////////////////////////
	// ///// commands for popup
	public static final String OPEN_FILE = "open file";
	public static final String SAVE_FILE = "save file";

}
