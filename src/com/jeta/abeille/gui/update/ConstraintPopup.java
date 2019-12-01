package com.jeta.abeille.gui.update;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import com.jeta.foundation.gui.utils.*;

/**
 * This is the popup component for the UpdateFrame. It shows a set of constraint
 * buttons that the user can select.
 * 
 * @author Jeff Tassin
 */
public class ConstraintPopup extends ButtonPopup {
	public ConstraintPopup() {
		addButton(InstanceView.getConstraintIcon(InstanceView.EQUAL), InstanceView.EQUAL);
		addButton(InstanceView.getConstraintIcon(InstanceView.NOTEQUAL), InstanceView.NOTEQUAL);
		addButton(InstanceView.getConstraintIcon(InstanceView.LESSTHAN), InstanceView.LESSTHAN);
		addButton(InstanceView.getConstraintIcon(InstanceView.LESSTHANEQUAL), InstanceView.LESSTHANEQUAL);
		addButton(InstanceView.getConstraintIcon(InstanceView.GREATERTHAN), InstanceView.GREATERTHAN);
		addButton(InstanceView.getConstraintIcon(InstanceView.GREATERTHANEQUAL), InstanceView.GREATERTHANEQUAL);
		addButton(InstanceView.getConstraintIcon(InstanceView.LIKE), InstanceView.LIKE);
		addButton(InstanceView.getConstraintIcon(InstanceView.UNSELECTED), InstanceView.UNSELECTED);
	}
}
