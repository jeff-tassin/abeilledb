/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.print;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

public class PrintPreview extends TSPanel {
	static final int H_GAP = 16;
	static final int V_GAP = 10;

	public PrintPreview() {
		setPreferredSize(TSGuiToolbox.getWindowDimension(10, 25));
	}

	private int getColumnCount() {
		return 2;
	}

	public Dimension doLayoutInternal() {
		int comp_count = getComponentCount();
		if (comp_count == 0)
			return getPreferredSize();

		Insets ins = getInsets();
		Component comp = getComponent(0);
		Dimension dc = comp.getPreferredSize();
		int comp_width = dc.width;
		int comp_height = dc.height;

		int row = 0;
		int max_x = 0;
		int max_y = 0;

		for (int index = 0; index < comp_count; index++) {
			int col = index % getColumnCount();

			int x = ins.left + H_GAP + col * (comp_width + H_GAP);
			int y = ins.top + V_GAP + row * (comp_height + V_GAP);

			comp = getComponent(index);
			comp.setBounds(x, y, comp_width, comp_height);

			if ((index + 1) % getColumnCount() == 0)
				row++;

			if (max_x < (x + comp_width))
				max_x = x + comp_width;

			if (max_y < (y + comp_height))
				max_y = y + comp_height;
		}

		return new Dimension(max_x + H_GAP, max_y + V_GAP);
	}

	public void doLayout() {
		doLayoutInternal();
	}

}
