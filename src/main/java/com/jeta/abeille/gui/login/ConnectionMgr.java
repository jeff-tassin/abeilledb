package com.jeta.abeille.gui.login;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.Database;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.resources.ResourceLoader;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.xml.XMLUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This model stores the defined database connections that the user can select
 * from.
 * 
 * @author Jeff Tassin
 */
public class ConnectionMgr {

	/** the name of the configuration file */
	private static String m_configfile = "config" + File.separatorChar + "connections.xml";

	/** the list of defined connections */
	private LinkedList m_connections = null;

	/**
	 * Adds a connection to the defined list of connections
	 */
	public void addConnection(ConnectionInfo model) {
		m_connections.add(model);
	}

	/**
	 * Creates an XML element for the given connection <Connection>
	 * <Database>PostgreSQL</Database> <Name>Test</Name>
	 * <Url>jdbc:postgresql://magnolia:5432/test</Url>
	 * <Driver>org.postgresql.Driver</Driver> <UID>1334343.1123.ARQGLJQJ.DD<UID>
	 * (just an example) <Connection>
	 * 
	 * @return the created element. Null is returned if an error occurs (e.g. no
	 *         UID )
	 */
	public Element createConnectionElement(Document doc, ConnectionInfo cinfo) {
		Element element = null;
		String uid = cinfo.getUID();
		if (uid != null && uid.length() > 0) {
			element = doc.createElement("Connection");
			element.appendChild(doc.createTextNode("\n"));
			element.appendChild(ConnectionMgr.createElement(doc, "Database", cinfo.getDatabase().toString()));
			element.appendChild(doc.createTextNode("\n"));
			element.appendChild(ConnectionMgr.createElement(doc, "Name", cinfo.getName()));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Server", cinfo.getServer()));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Description", cinfo.getDescription()));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Port", String.valueOf(cinfo.getPort())));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "UID", cinfo.getUID()));
			element.appendChild(doc.createTextNode("\n"));

			Collection jars = cinfo.getJars();
			Iterator iter = jars.iterator();
			while (iter.hasNext()) {
				String jar = TSUtils.fastTrim((String) iter.next());
				if (jar != null || jar.length() > 0) {
					element.appendChild(ConnectionMgr.createElement(doc, "JDBC", jar));
					element.appendChild(doc.createTextNode("\n"));
				}
			}

			element.appendChild(ConnectionMgr.createElement(doc, "URL", cinfo.getUrl()));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Advanced", String.valueOf(cinfo.isAdvanced())));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Embedded", String.valueOf(cinfo.isEmbedded())));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Parameter1", cinfo.getParameter1()));
			element.appendChild(doc.createTextNode("\n"));

			element.appendChild(ConnectionMgr.createElement(doc, "Driver", cinfo.getDriver()));
			element.appendChild(doc.createTextNode("\n"));

		} else {
			assert (false);
		}
		return element;
	}

	/**
	 * Helper method to create an element with the given tag and value
	 */
	private static Element createElement(Document doc, String tagName, String value) {
		if (value == null) {
			value = "";
		}
		assert (tagName != null);
		Element result = doc.createElement(tagName);
		// assert( value != null && value.length() > 0 );

		Text txtnode = doc.createTextNode(value);
		result.appendChild(txtnode);
		return result;
	}

	/**
	 * Deletes the given connection info object
	 */
	public void deleteConnection(ConnectionInfo info) {
		m_connections.remove(info);
	}

	/**
	 * @return the connection with the given uid. Null is returned if the
	 *         connection cannot be found
	 */
	public ConnectionInfo getConnection(String uid) {
		if (uid == null)
			return null;

		Iterator iter = m_connections.iterator();
		while (iter.hasNext()) {
			ConnectionInfo info = (ConnectionInfo) iter.next();
			if (uid.equals(info.getUID()))
				return info;
		}

		return null;
	}

	/**
	 * @return the relative path to the directory where we store our objects
	 */
	private String getConnectionsResource() {
		return m_configfile;
	}

	/**
	 * @return the collection of ConnectionInfo objects that were previously
	 *         defined by the user. Each ConnectionInfo object represents a
	 *         connection and application state data for a given database/user
	 */
	public Collection getDefinedConnections() {
		if (m_connections == null)
			loadConnections();

		return m_connections;
	}

	/**
	 * Loads the connection definitions from the connections.xml file
	 */
	private void loadConnections() {
		m_connections = new LinkedList();
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		if (loader != null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(loader.getInputStream(getConnectionsResource()));

				Element root = document.getDocumentElement();
				root.normalize();
				// we now have the root element, so parse to get the connections

				NodeList nlist = root.getChildNodes();
				for (int index = 0; index < nlist.getLength(); index++) {
					Node item = nlist.item(index);
					if (item instanceof Element) {
						Element element = (Element) item;
						String elname = element.getNodeName();
						if (elname.equalsIgnoreCase("Connection")) {
							ConnectionInfo info = parseConnection(element);
							if (info != null)
								m_connections.add(info);
						}
					}
				}
			} catch (java.io.FileNotFoundException fnfe) {
				// just eat this one

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Modifies an existing connection
	 */
	public void modifyConnection(ConnectionInfo newModel, ConnectionInfo oldModel) {
		assert (newModel.getUID().equals(oldModel.getUID()));
		ListIterator iter = m_connections.listIterator();
		while (iter.hasNext()) {
			ConnectionInfo info = (ConnectionInfo) iter.next();
			if (info.getUID().equals(oldModel.getUID())) {
				iter.set(newModel);
				break;
			}
		}
	}

	/**
	 * This method parses a Connection element from a connections.xml file.
	 * <Connection> <Database>PostgreSQL</Database> <Name>Test</Name>
	 * <Url>jdbc:postgresql://magnolia:5432/test</Url>
	 * <Driver>org.postgresql.Driver</Driver> <UID>1334343.1123.ARQGLJQJ.DD<UID>
	 * <Connection>
	 * 
	 * @return a ConnectionInfo object populated with the information found in
	 *         the file
	 */
	private ConnectionInfo parseConnection(Element element) {
		ConnectionInfo info = null;
		Database db = null;
		String server = null;
		String name = null;
		String portstr = null;
		String driver = null;
		String driverstr = null;
		String uid = null;
		String description = null;
		String url = null;
		String advanced = null;
		String embedded = null;
		String param1 = null;
		LinkedList jars = new LinkedList();

		NodeList nlist = element.getChildNodes();
		for (int index = 0; index < nlist.getLength(); index++) {
			Node item = nlist.item(index);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) item;
				String elname = e.getNodeName();
				if (elname != null) {
					elname = elname.trim();
					if (elname.equalsIgnoreCase("Database"))
						db = Database.fromString(XMLUtils.getNodeValue(e));
					else if (elname.equalsIgnoreCase("Name"))
						name = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Server"))
						server = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Description"))
						description = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Port"))
						portstr = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("UID"))
						uid = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("JDBC")) {
						String jar = TSUtils.fastTrim(XMLUtils.getNodeValue(e));
						if (jar.length() > 0) {
							jars.add(jar);
						}
					} else if (elname.equalsIgnoreCase("URL"))
						url = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Advanced"))
						advanced = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Embedded"))
						embedded = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Parameter1"))
						param1 = XMLUtils.getNodeValue(e);
					else if (elname.equalsIgnoreCase("Driver"))
						driverstr = XMLUtils.getNodeValue(e);
				}
			}
		}

		int port = 0;
		try {
			if (driverstr != null)
				driver = driverstr;

			if (portstr != null) {
				port = Integer.parseInt(portstr);
			}
		} catch (Exception e) {
			TSUtils.printException(e);

		}

		if ( db == Database.GENERIC && driver.contains("sqlserver")) {
			db = Database.SQLSERVER;
		}


		/** at a minimum, the Database and UID must be defined */
		if (db != null && uid != null) {
			info = new ConnectionInfo(db, uid, driver, name, server, port);
			try {
				info.setDescription(description);
			} catch (Exception e) {
				TSUtils.printException(e);
			}

			info.setJars(jars);
			info.setUrl(url);

			try {
				info.setAdvanced(Boolean.valueOf(advanced).booleanValue());
			} catch (Exception e) {
				TSUtils.printException(e);
			}

			try {
				if (embedded == null)
					info.setEmbedded(false);
				else
					info.setEmbedded(Boolean.valueOf(embedded).booleanValue());
			} catch (Exception e) {
				TSUtils.printException(e);
			}

			info.setParameter1(param1);

		} else {
			assert (false);
		}
		return info;
	}

	public void print() {
		System.out.println("ConnectionMgr.print  " + hashCode());
		Iterator iter = m_connections.iterator();
		while (iter.hasNext()) {
			ConnectionInfo cinfo = (ConnectionInfo) iter.next();
			cinfo.print();
		}
	}

	/**
	 * Saves the model to the connections.xml file
	 */
	public void save() throws IOException {
		LinkedList results = new LinkedList();
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		if (loader != null) {

			// System.out.println( "ConnectionMgr.save  " + hashCode() +
			// "  m_connections.size = " + m_connections.size() );
			String configfile = getConnectionsResource();
			loader.createResource(configfile);
			OutputStream os = loader.getOutputStream(configfile);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.newDocument();
				Element root = document.createElement("ConnectionList");
				root.appendChild(document.createTextNode("\n"));

				document.appendChild(root);

				Iterator iter = m_connections.iterator();
				while (iter.hasNext()) {
					ConnectionInfo cinfo = (ConnectionInfo) iter.next();
					Element element = createConnectionElement(document, cinfo);
					if (element != null) {
						root.appendChild(element);
						root.appendChild(document.createTextNode("\n"));
					}
				}

				TransformerFactory tfactory = TransformerFactory.newInstance();
				Transformer serializer = tfactory.newTransformer();
				DOMSource ds = new DOMSource(document);
				serializer.transform(ds, new StreamResult(os));
				os.flush();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
