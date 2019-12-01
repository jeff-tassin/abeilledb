package com.jeta.abeille.gui.update;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.jeta.foundation.i18n.I18N;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.gui.update.java.JavaObjectHandler;
import com.jeta.abeille.gui.update.image.ImageObjectHandler;
import com.jeta.abeille.gui.update.text.TextLOBObjectHandler;

/**
 * Factory class that creates ColumnHandler objects
 * 
 * @author Jeff Tassin
 */
public class ColumnHandlerFactory {
	private static LinkedHashMap m_handlers = new LinkedHashMap();

	static {
		registerFactory(new StandardFactory(com.jeta.abeille.gui.update.DefaultColumnHandler.class,
				DefaultColumnHandler.HANDLER_NAME));

		registerFactory(new StandardFactory(com.jeta.abeille.gui.update.EditorHandler.class, EditorHandler.HANDLER_NAME));

		registerFactory(new JavaFactory(true));

		registerFactory(new FileFactory(false));

		registerFactory(new StandardFactory(com.jeta.abeille.gui.update.image.ImageObjectHandler.class,
				ImageObjectHandler.HANDLER_NAME));

		registerFactory(new StandardFactory(com.jeta.abeille.gui.update.text.TextLOBObjectHandler.class,
				TextLOBObjectHandler.HANDLER_NAME));

		registerFactory(new ClobFactory(TextLOBObjectHandler.CLOB_HANDLER_NAME));
		registerFactory(new FileFactory(true));

		registerFactory(new JavaFactory(false));

		registerFactory(new BasicFactory(java.sql.Types.INTEGER, DefaultColumnHandler.INTEGER_HANDLER));

		registerFactory(new BasicFactory(java.sql.Types.NUMERIC, DefaultColumnHandler.NUMERIC_HANDLER));

		registerFactory(new BasicFactory(java.sql.Types.VARCHAR, DefaultColumnHandler.VARCHAR_HANDLER));

	}

	public static void registerFactory(HandlerFactory factory) {
		m_handlers.put(factory.getName(), factory);
	}

	/**
	 * Creates a handler for the given handler name
	 * 
	 * @param handlerName
	 *            the name of the handler (as returned from getHandlerNames)
	 * @return the ColumnHandler object that corresponds to the name.
	 */
	static ColumnHandler createHandler(String handlerName) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		HandlerFactory factory = (HandlerFactory) m_handlers.get(handlerName);
		return factory.createHandler();
	}

	/**
	 * @return a collection (String objects) of handler names
	 */
	static Collection getHandlerNames() {
		return m_handlers.keySet();
	}

	interface HandlerFactory {
		ColumnHandler createHandler() throws ClassNotFoundException, InstantiationException, IllegalAccessException;

		public String getName();
	}

	static class StandardFactory implements HandlerFactory {
		private Class m_handlerclass;
		private String m_name;

		StandardFactory(Class c, String name) {
			m_handlerclass = c;
			m_name = name;
		}

		public ColumnHandler createHandler() throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			return (ColumnHandler) m_handlerclass.newInstance();
		}

		public String getName() {
			return m_name;
		}
	}

	/**
	 * Factory for handling clob types
	 */
	static class ClobFactory implements HandlerFactory {
		private String m_name;

		public ClobFactory(String name) {
			m_name = name;
		}

		public String getName() {
			return m_name;
		}

		public ColumnHandler createHandler() throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			return new com.jeta.abeille.gui.update.text.TextLOBObjectHandler(true);
		}
	}

	/**
	 * Factory for handling java types
	 */
	static class JavaFactory implements HandlerFactory {
		/**
		 * set this to true if the object is stored in the database as a blob.
		 * set to false if the object is stored and returned directly (i.e.
		 * resultset.getObject(..) returns the object itself
		 */
		private boolean m_blob = true;

		public JavaFactory(boolean blob) {
			m_blob = blob;
		}

		public String getName() {
			if (m_blob)
				return com.jeta.abeille.gui.update.java.JavaObjectHandler.BLOB_HANDLER_NAME;
			else
				return com.jeta.abeille.gui.update.java.JavaObjectHandler.HANDLER_NAME;
		}

		public ColumnHandler createHandler() throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			return new com.jeta.abeille.gui.update.java.JavaObjectHandler(m_blob);
		}
	}

	/**
	 * Factory for handling file types
	 */
	static class FileFactory implements HandlerFactory {
		/**
		 * set this to true if the object should be handled as a clob.
		 * Basically, this means that when we load from disk, we use a Reader
		 * object instead of an binary stream.
		 */
		private boolean m_clob = true;

		public FileFactory(boolean clob) {
			m_clob = clob;
		}

		public String getName() {
			if (m_clob)
				return FileObjectHandler.CLOB_HANDLER_NAME;
			else
				return FileObjectHandler.HANDLER_NAME;
		}

		public ColumnHandler createHandler() throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			return new FileObjectHandler(m_clob);
		}
	}

	static class BasicFactory implements HandlerFactory {
		private int m_type;
		private String m_name;

		BasicFactory(int dataType, String handlerName) {
			m_type = dataType;
			m_name = handlerName;
		}

		public ColumnHandler createHandler() {
			return new DefaultColumnHandler(m_type, m_name);
		}

		public String getName() {
			return m_name;
		}

	}

}
