package com.jeta.abeille.gui.update;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import com.jeta.foundation.gui.utils.*;

/**
 * This is the popup component for the InstanceView.
 * 
 * @author Jeff Tassin
 */
public class ButtonPopup extends JPopupMenu {
	protected LinkedList m_listeners = new LinkedList();
	protected CommandListener m_cmdListener = new CommandListener();
	protected Object m_sourceObject; // allows the caller to assign an object
										// that triggered popup
	protected int NUM_BUTTONS;
	protected ArrayList m_buttons = new ArrayList();
	private HashMap m_icons = new HashMap();

	public ButtonPopup() {
		// setLayout( new PopupLayoutManager() );
	}

	/**
	 * When a button is pressed, this method gets called by CommandListener
	 * 
	 * @param commandId
	 *            the command id
	 * @param e
	 *            the action event
	 */
	void _actionPerformed(ActionEvent e) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(e);
		}
	}

	/**
	 * Allows the client to register listeners for button events
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
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
	public void addButton(ImageIcon image, String commandId) {
		// AbstractButton button = new MyButton();
		// button.setIcon( image );
		// button.setFocusPainted(false);
		// button.setBorderPainted(false);
		// button.addActionListener( m_cmdListener );
		// button.setActionCommand( commandId );
		// button.setSize( 16, 16 );
		// button.setFocusable( false );
		// button.setRequestFocusEnabled( false );
		// button.setVisible( true );
		// add( button );
		// m_buttons.add( button );

		m_icons.put(commandId, image);

		JMenuItem item = new JMenuItem(image);
		item.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		item.setMargin(new java.awt.Insets(0, 0, 0, 5));

		add(item);
		item.setActionCommand(commandId);
		item.addActionListener(m_cmdListener);

		m_buttons.add(item);

		NUM_BUTTONS = m_buttons.size();
		// setLayout( new GridLayout( NUM_BUTTONS, 1, 0, 2 ) );
	}

	public Icon getIcon(String commandId) {
		return (Icon) m_icons.get(commandId);
	}

	/**
	 * @return the preferred size for this component
	 */
	public Dimension getPreferredSize() {
		return super.getPreferredSize();
		// w = 47
		// Dimension d = new Dimension( 37, (16+5)*8 );
		// return d;
	}

	/**
	 * Allows the caller to get a previously assigned source object that
	 * triggered this popup
	 * 
	 * @return the source object
	 */
	public Object getSourceObject() {
		return m_sourceObject;
	}

	/**
	 * Allows the caller to assign a source object that triggered this popup
	 * 
	 * @param sourceObject
	 *            the source object
	 */
	public void setSourceObject(Object sourceObject) {
		m_sourceObject = sourceObject;
	}

	class CommandListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ButtonPopup.this._actionPerformed(e);
		}
	}

}
