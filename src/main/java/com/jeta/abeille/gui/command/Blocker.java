package com.jeta.abeille.gui.command;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Blocker extends EventQueue {
	private Component[] restrictedComponents;
	private Vector helperVector;
	private boolean inBlockedState = false;
	private EventQueue sysQ = Toolkit.getDefaultToolkit().getSystemEventQueue();
	private boolean alreadyBlockedOnce = false;
	private static Blocker instance = null;

	public static synchronized Blocker Instance() {
		if (instance == null) {
			instance = new Blocker();
		}
		return instance;
	}

	private Blocker() {
		restrictedComponents = null;
	}

	private void reset() {
		if (inBlockedState) {
			setBlockingEnabled(false);
		}
		restrictedComponents = null;
	}

	public void setRestrictedComponents(Component[] restrictedComponents) {
		reset(); // puts the Blocker into an unblocked state, and clears the
					// restrictedComponents array (see private method below)
		helperVector = new Vector();
		// global Vector variable
		if (restrictedComponents != null) {
			extractAllComponents(restrictedComponents);
		}

		// builds the blockedComponent array
		if (helperVector.size() >= 1) {
			this.restrictedComponents = new Component[helperVector.size()];
			for (int k = 0; k < helperVector.size(); k++) {
				this.restrictedComponents[k] = (Component) helperVector.elementAt(k);
			}
		} else {
			this.restrictedComponents = null;
		}
	}

	private void extractAllComponents(Component[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				helperVector.addElement(array[i]);
				if (((Container) array[i]).getComponentCount() != 0) {
					extractAllComponents(((Container) array[i]).getComponents());
				}
			}
		}
	}

	private void adjustFocusCapabilities(boolean blocked) {
		if (blocked) {
			for (int i = 0; i < restrictedComponents.length; i++) {
				if (restrictedComponents[i] instanceof JComponent) {
					((JComponent) restrictedComponents[i]).setRequestFocusEnabled(false);
				}

				// removes the focus indicator from all components that are
				// capable
				// of painting their focus
				if (restrictedComponents[i] instanceof AbstractButton) {
					((AbstractButton) restrictedComponents[i]).setFocusPainted(false);
				}
			}
		} else {
			for (int k = 0; k < restrictedComponents.length; k++) {
				if (restrictedComponents[k] instanceof JComponent) {
					((JComponent) restrictedComponents[k]).setRequestFocusEnabled(true);
				}
				if (restrictedComponents[k] instanceof AbstractButton) {
					((AbstractButton) restrictedComponents[k]).setFocusPainted(true);
				}
			}
		}
	}

	private Component getSource(AWTEvent event) {
		Component source = null;
		// each of these five MouseEvents will still be valid (regardless
		// of their source), so we still want to process them.
		if ((event instanceof MouseEvent) && (event.getID() != MouseEvent.MOUSE_DRAGGED)
				&& (event.getID() != MouseEvent.MOUSE_ENTERED) && (event.getID() != MouseEvent.MOUSE_EXITED)
				&& (event.getID() != MouseEvent.MOUSE_MOVED) && (event.getID() != MouseEvent.MOUSE_RELEASED)) {
			MouseEvent mouseEvent = (MouseEvent) event;
			source = SwingUtilities.getDeepestComponentAt(mouseEvent.getComponent(), mouseEvent.getX(),

			mouseEvent.getY());
		} else if (event instanceof KeyEvent && event.getSource() instanceof Component) {
			source = SwingUtilities.findFocusOwner((Component) (event.getSource()));
		}
		return source;
	}

	private boolean isSourceBlocked(Component source) {
		boolean blocked = false;
		if ((restrictedComponents != null) && (source != null)) {
			int i = 0;
			while (i < restrictedComponents.length && (restrictedComponents[i].equals(source) == false))
				i++;

			blocked = i < restrictedComponents.length;
		}
		return blocked;
	}

	protected void dispatchEvent(AWTEvent event) {
		boolean blocked = false;

		if (inBlockedState) {
			// getSource is a private helper method
			blocked = isSourceBlocked(getSource(event));
		}
		if (blocked && (event.getID() == MouseEvent.MOUSE_CLICKED || event.getID() == MouseEvent.MOUSE_PRESSED)) {
			Toolkit.getDefaultToolkit().beep();
		}

		else if (blocked && event instanceof KeyEvent && event.getSource() instanceof Component) {
			DefaultFocusManager dfm = new DefaultFocusManager();
			dfm.getCurrentManager();
			Component currentFocusOwner = getSource(event);

			boolean focusNotFound = true;
			do {
				dfm.focusNextComponent(currentFocusOwner);
				currentFocusOwner = SwingUtilities.findFocusOwner((Component) event.getSource());
				if (currentFocusOwner instanceof JComponent) {
					focusNotFound = (((JComponent) currentFocusOwner).isRequestFocusEnabled() == false);
				}
			} while (focusNotFound);
		} else {
			super.dispatchEvent(event);
		}
	}

	public void setBlockingEnabled(boolean block) {
		// this methods must be called from the AWT thread to avoid
		// toggling between states while events are being processed
		if (block && !inBlockedState && restrictedComponents != null) {

			adjustFocusCapabilities(true);
			// "adjustFocusCapabilities" is a private helper function that
			// sets the focusEnabled & focusPainted flags for the
			// appropriate components. Its boolean parameter signifies
			// whether we are going into a blocked or unblocked state
			// (true = blocked, false = unblocked)

			if (!alreadyBlockedOnce) {

				// here is where we replace the SystemQueue
				sysQ.push(this);
				alreadyBlockedOnce = true;
			}
			inBlockedState = true;
		} else if (!block && inBlockedState) {
			adjustFocusCapabilities(false);
			inBlockedState = false;
		}
	}
}
