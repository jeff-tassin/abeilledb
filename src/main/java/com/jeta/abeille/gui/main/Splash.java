package com.jeta.abeille.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

/**
 * Splash screen for the application
 * 
 * @author Jeff Tassin
 */
public class Splash extends JWindow {
	private static long WAIT_TIME = 2000;
	private static long m_start_time = 0;

	public Splash() {
		try {
			m_start_time = System.currentTimeMillis();

			ImageIcon icon = loadImage("resources/images/jeta_about.png");
			if (icon == null) {
				return;
			}

			JLabel label = new JLabel(icon);
			label.setBorder(BorderFactory.createLineBorder(java.awt.Color.black));
			getContentPane().add(label, BorderLayout.CENTER);
			pack();

			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					m_start_time = 0;
					setVisible(false);
					dispose();
				}
			});

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension labelSize = label.getPreferredSize();
			setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));
			setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		if (m_start_time > 0) {
			try {
				long current = System.currentTimeMillis();
				if ((current - m_start_time) < WAIT_TIME) {
					Thread.currentThread().sleep(WAIT_TIME - (current - m_start_time));
				}
			} catch (Exception e) {

			}
		}
		super.dispose();
	}

	/**
	 * Loads the main splash screen image
	 */
	private ImageIcon loadImage(String imageResource) {
		try {
			ClassLoader loader = Splash.class.getClassLoader();
			InputStream istream = loader.getResourceAsStream(imageResource);
			BufferedInputStream bis = new BufferedInputStream(istream);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int numread = bis.read(buff);
			while (numread > 0) {
				bos.write(buff, 0, numread);
				numread = bis.read(buff);
			}
			bis.close();

			return new ImageIcon(bos.toByteArray());
		} catch (Exception e) {

		}
		return null;
	}

}
