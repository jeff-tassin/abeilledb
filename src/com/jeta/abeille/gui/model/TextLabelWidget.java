package com.jeta.abeille.gui.model;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.border.*;

public class TextLabelWidget extends JComponent {
	private String m_text = "";

	public TextLabelWidget() {
		m_text = "This is a test";
		setBackground(new Color(255, 255, 204));
		setFont(new Font("Arial", Font.BOLD, 14));
		setUI(new TextLabelWidgetUI());
	}

	public String getText() {
		return m_text;
	}

	public void setText(String sVal) {
		m_text = sVal;
		repaint();
	}

}