package com.jeta.abeille.gui.downloader;

import com.jeta.abeille.main.JETAInitializer;

public class DownloadTest {

	public static void main(String[] args) {
		JETAInitializer ji = new JETAInitializer();
		ji.initialize(args);

		DownloaderDialog dlg = new DownloaderDialog((java.awt.Frame) null, false);
		dlg.setSize(dlg.getPreferredSize());

		dlg.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.out.println("got dialog closing event");
				System.exit(0);
			}

			public void windowClosed(java.awt.event.WindowEvent e) {
				System.out.println("got dialog closed event");
				System.exit(0);
			}

		});

		// dlg.start( "http://localhost/jdbcdescriptors/mysql.xml" );
		dlg.showCenter();
	}

}
