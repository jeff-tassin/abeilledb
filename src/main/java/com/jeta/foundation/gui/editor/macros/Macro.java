package com.jeta.foundation.gui.editor.macros;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Icon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jeta.foundation.xml.XMLUtils;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a macro to be executed
 * 
 * @todo validate name of macros. Don't allow macro names with invalid
 *       characters like quotes. Also, macro names must be unique.
 * 
 * @author Jeff Tassin
 */
public class Macro implements Cloneable {
	/** the name of this macro */
	private String m_name;

	/** the command to execute ( action names delimited by spaces ) */
	private String m_command;

	/** the parsed command */
	private ArrayList m_commands = new ArrayList();

	/** icon used to display the macro - can be null */
	private Icon m_icon;

	/** the kit class that this macro belongs to */
	private Class m_kitclass;

	/** identifiers */
	public final static String NAME = "name";
	public final static String COMMAND = "command";

	/*
	 * ctor
	 */
	public Macro() {

	}

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name of the macro
	 * @param command
	 *            the command to execute
	 */
	public Macro(String name, String command) {
		m_name = name;
		setCommand(command);
	}

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name of the macro
	 * @param command
	 *            the command to execute
	 * @param icon
	 *            the icon used in the macro mgr view
	 */
	public Macro(String name, String command, Icon icon) {
		this(name, command);
		m_icon = icon;
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		return new Macro(m_name, m_command, m_icon);
	}

	/**
	 * Generates the command string from the parsed list of commands
	 */
	private void generateCommand() {
		StringBuffer cmdseq = new StringBuffer();
		Iterator iter = m_commands.iterator();
		while (iter.hasNext()) {
			String cmd = (String) iter.next();
			cmdseq.append(cmd);
			if (iter.hasNext())
				cmdseq.append(' ');
		}

		m_command = cmdseq.toString();
	}

	/**
	 * @return the collection of named actions invoked by this macro. For
	 *         example, if you macro performed: caret-backward "select"
	 *         caret-forward-word, the two actions, caret-backward and
	 *         caret-forward-word would be returned. The insert string
	 *         ("select") would not be returned.
	 */
	public Collection getActionNames() {
		LinkedList results = new LinkedList();
		Iterator iter = m_commands.iterator();
		while (iter.hasNext()) {
			String command = (String) iter.next();
			int pos = command.indexOf('"');
			if (pos < 0) // we don't have a quote char, so assume that the
							// command name is a valid action name
			{
				results.add(command);
			}
		}
		return results;
	}

	/**
	 * @return the command string that defines this macro A command string is a
	 *         sequence of action or macro names to execute separated by spaces.
	 */
	public String getCommand() {
		return m_command;
	}

	/**
	 * @return the icon for this macro (the icon corresponds to the editor kit
	 *         class )
	 */
	public Icon getIcon() {
		return m_icon;
	}

	/**
	 * @return the kit class for this macro
	 */
	public Class getKitClass() {
		return m_kitclass;
	}

	/**
	 * @return the name of this macro
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Parses a command into its individual action calls and loads the commands
	 * array.
	 */
	private void parseCommand(String commandString) {
		m_commands.clear();

		StringBuffer actionname = new StringBuffer();
		StringBuffer literal = new StringBuffer();

		char[] command = commandString.toCharArray();
		int len = command.length;

		for (int i = 0; i < len; i++) {
			if (Character.isWhitespace(command[i]))
				continue;

			if (command[i] == '"') {
				literal.setLength(0);
				literal.append('"');
				while (++i < len && command[i] != '"') {
					char ch = command[i];
					literal.append(ch);
					if (ch == '\\') {
						if (++i >= len) { // '\' at the end
							throw new IllegalArgumentException();
						}
						ch = command[i];
						if (ch != '"' && ch != '\\') { // neither \\ nor \"
							throw new IllegalArgumentException();
						} // else fall through

						literal.append(ch);
					}
				}

				// if we are here, then the literal was successfully parsed
				m_commands.add(literal.toString());
			} else {
				// parse the action name
				actionname.setLength(0);
				while (i < len && !Character.isWhitespace(command[i])) {
					char ch = command[i++];
					if (ch == '\\') {
						if (i >= len) {
							// macro ending with single '\'
							throw new IllegalArgumentException();
						}
						;

						ch = command[i++];

						if (ch != '\\' && !Character.isWhitespace(ch)) {
							throw new IllegalArgumentException();
						} // else fall through
					}
					actionname.append(ch);
				}

				m_commands.add(actionname.toString());
			}
		}
	}

	/**
	 * Initializes this macro object from xml
	 */
	public void readXML(Element element) {
		assert (element.getNodeName().equalsIgnoreCase("Macro"));
		m_name = XMLUtils.getChildText(element, NAME);
		m_command = XMLUtils.getChildText(element, COMMAND);
	}

	/**
	 * Sets the command to execute for this macro
	 * 
	 * @param command
	 *            the command
	 */
	public void setCommand(String command) {
		m_command = command;
		parseCommand(command);
	}

	/**
	 * Sets the icon for this macro (the icon corresponds to the editor kit
	 * class )
	 */
	public void setIcon(Icon icon) {
		m_icon = icon;
	}

	/**
	 * Sets the kit class for this macro
	 */
	public void setKitClass(Class kitclass) {
		m_kitclass = kitclass;
	}

	/**
	 * Sets the name of this macro
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * If the macro (oldName) is found in the command list, it is updated to
	 * newNew in the list
	 */
	public void updateMacroName(String newName, String oldName) {
		for (int index = 0; index < m_commands.size(); index++) {
			String command = (String) m_commands.get(index);
			if (I18N.equals(oldName, command)) {
				m_commands.set(index, newName);
			}
		}
		generateCommand();
	}

	/**
	 * Writes this macro out to XML
	 */
	public Element writeXML(Document doc) {
		Element element = doc.createElement("Macro");

		Element name = doc.createElement(NAME);
		element.appendChild(doc.createTextNode("\n"));
		name.appendChild(doc.createTextNode(getName()));

		Element command = doc.createElement(COMMAND);
		command.appendChild(doc.createTextNode(getCommand()));

		element.appendChild(name);
		command.appendChild(doc.createTextNode("\n"));
		element.appendChild(command);
		command.appendChild(doc.createTextNode("\n"));
		return element;
	}
}
