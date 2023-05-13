/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.java;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.List;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionView;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ListCompletionView;

/**
 * Java completion query specifications
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaCompletion extends Completion {

	public static final int PUBLIC_LEVEL = 3;
	public static final int PROTECTED_LEVEL = 2;
	public static final int PACKAGE_LEVEL = 1;
	public static final int PRIVATE_LEVEL = 0;

	public static final SimpleClass BOOLEAN_CLASS = new SimpleClass("boolean", ""); // NOI18N
	public static final SimpleClass BYTE_CLASS = new SimpleClass("byte", ""); // NOI18N
	public static final SimpleClass CHAR_CLASS = new SimpleClass("char", ""); // NOI18N
	public static final SimpleClass DOUBLE_CLASS = new SimpleClass("double", ""); // NOI18N
	public static final SimpleClass FLOAT_CLASS = new SimpleClass("float", ""); // NOI18N
	public static final SimpleClass INT_CLASS = new SimpleClass("int", ""); // NOI18N
	public static final SimpleClass LONG_CLASS = new SimpleClass("long", ""); // NOI18N
	public static final SimpleClass SHORT_CLASS = new SimpleClass("short", ""); // NOI18N
	public static final SimpleClass VOID_CLASS = new SimpleClass("void", ""); // NOI18N

	public static final BaseType BOOLEAN_TYPE = new BaseType(BOOLEAN_CLASS, 0);
	public static final BaseType BYTE_TYPE = new BaseType(BYTE_CLASS, 0);
	public static final BaseType CHAR_TYPE = new BaseType(CHAR_CLASS, 0);
	public static final BaseType DOUBLE_TYPE = new BaseType(DOUBLE_CLASS, 0);
	public static final BaseType FLOAT_TYPE = new BaseType(FLOAT_CLASS, 0);
	public static final BaseType INT_TYPE = new BaseType(INT_CLASS, 0);
	public static final BaseType LONG_TYPE = new BaseType(LONG_CLASS, 0);
	public static final BaseType SHORT_TYPE = new BaseType(SHORT_CLASS, 0);
	public static final BaseType VOID_TYPE = new BaseType(VOID_CLASS, 0);

	public static final SimpleClass INVALID_CLASS = new SimpleClass("", ""); // NOI18N
	public static final BaseType INVALID_TYPE = new BaseType(INVALID_CLASS, 0);

	public static final SimpleClass NULL_CLASS = new SimpleClass("null", ""); // NOI18N
	public static final BaseType NULL_TYPE = new BaseType(NULL_CLASS, 0);

	public static final SimpleClass OBJECT_CLASS_ARRAY = new SimpleClass("java.lang.Object[]", "java.lang".length(),
			true); // NOI18N
	public static final BaseType OBJECT_TYPE_ARRAY = new BaseType(OBJECT_CLASS_ARRAY, 0);

	public static final SimpleClass OBJECT_CLASS = new SimpleClass("java.lang.Object", "java.lang".length(), true); // NOI18N
	public static final BaseType OBJECT_TYPE = new BaseType(OBJECT_CLASS, 0);

	public static final SimpleClass CLASS_CLASS = new SimpleClass("java.lang.Class", "java.lang".length(), true); // NOI18N
	public static final BaseType CLASS_TYPE = new BaseType(CLASS_CLASS, 0);

	public static final SimpleClass STRING_CLASS = new SimpleClass("java.lang.String", "java.lang".length(), true); // NOI18N
	public static final BaseType STRING_TYPE = new BaseType(STRING_CLASS, 0);

	static final int INTERFACE_BIT = (1 << 30); // no neg nums in modifiers
	static final int INTERFACE_BIT_FILTER = (~INTERFACE_BIT);

	private static final HashMap str2PrimitiveClass = new HashMap();
	private static final HashMap str2PrimitiveType = new HashMap();

	static {
		BaseType[] types = new BaseType[] { BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, DOUBLE_TYPE, FLOAT_TYPE, INT_TYPE,
				LONG_TYPE, SHORT_TYPE, VOID_TYPE };

		for (int i = types.length - 1; i >= 0; i--) {
			String typeName = types[i].getClazz().getName();
			str2PrimitiveClass.put(typeName, types[i].getClazz());
			str2PrimitiveType.put(typeName, types[i]);
		}
	}

	public static final JCParameter[] EMPTY_PARAMETERS = new JCParameter[0];
	public static final JCClass[] EMPTY_CLASSES = new JCClass[0];
	public static final JCPackage[] EMPTY_PACKAGES = new JCPackage[0];
	public static final JCField[] EMPTY_FIELDS = new JCField[0];
	public static final JCConstructor[] EMPTY_CONSTRUCTORS = new JCConstructor[0];
	public static final JCMethod[] EMPTY_METHODS = new JCMethod[0];

	private static JCFinder finder;

	private static int debugMode;

	/** Map holding the simple class instances */
	private static HashMap classCache = new HashMap(5003);

	/** Map holding the cached types */
	private static HashMap typeCache = new HashMap(5003);

	/** Debug expression creation */
	public static final int DEBUG_EXP = 1;
	/** Debug finding packages/classes/fields/methods */
	public static final int DEBUG_FIND = 2;

	/** Callback for initing completion. See EditorModule.restored(). */
	private static JCFinderInitializer initializer;

	/** Gets the current default finder. */
	public static synchronized JCFinder getFinder() {
		if (finder == null) {
			if (initializer == null) {
				throw new IllegalStateException("Editor: Java completion can't be initialized.");
			}

			initializer.initJCFinder();
		}

		return finder;
	}

	/** Set the current default finder */
	public static synchronized void setFinder(JCFinder f) {
		finder = f;
	}

	/**
	 * Sets initializer to init finder for case it's needed and was not done
	 * yet.
	 */
	public static void setFinderInitializer(JCFinderInitializer initializer) {
		JavaCompletion.initializer = initializer;
	}

	public JavaCompletion(ExtEditorUI extEditorUI) {
		super(extEditorUI);
	}

	protected CompletionView createView() {
		return new ListCompletionView(new JCCellRenderer());
	}

	protected CompletionQuery createQuery() {
		return new JavaCompletionQuery();
	}

	/** Get level from modifiers. */
	public static int getLevel(int modifiers) {
		if ((modifiers & Modifier.PUBLIC) != 0) {
			return PUBLIC_LEVEL;
		} else if ((modifiers & Modifier.PROTECTED) != 0) {
			return PROTECTED_LEVEL;
		} else if ((modifiers & Modifier.PRIVATE) == 0) {
			return PACKAGE_LEVEL;
		} else {
			return PRIVATE_LEVEL;
		}
	}

	public static boolean isPrimitiveClassName(String s) {
		return JavaTokenContext.isTypeOrVoid(s);
	}

	public static boolean isPrimitiveClass(JCClass c) {
		return (c.getPackageName().length() == 0) && isPrimitiveClassName(c.getName());
	}

	public static JCClass getPrimitiveClass(String s) {
		return (JCClass) str2PrimitiveClass.get(s);
	}

	public static JCType getPrimitiveType(String s) {
		return (JCType) str2PrimitiveType.get(s);
	}

	public static Iterator getPrimitiveClassIterator() {
		return str2PrimitiveClass.values().iterator();
	}

	public static JCClass getSimpleClass(String fullClassName, int packageNameLen) {
		JCClass cls = (JCClass) classCache.get(fullClassName);
		if (cls == null // not in cache yet
				|| packageNameLen != cls.getPackageName().length() // different
																	// class
		) {
			cls = new SimpleClass(fullClassName, packageNameLen, true);
			classCache.put(fullClassName, cls);
		}
		return cls;
	}

	public static JCClass getSimpleClass(JCClass cls) {
		return getSimpleClass(cls.getFullName(), cls.getPackageName().length());
	}

	public static JCClass createSimpleClass(String fullClassName) {
		int nameInd = fullClassName.lastIndexOf('.') + 1;
		return createSimpleClass(fullClassName.substring(nameInd),
				(nameInd > 0) ? fullClassName.substring(0, nameInd - 1) : ""); // NOI18N
	}

	public static JCClass createSimpleClass(String name, String packageName) {
		return new SimpleClass(name, packageName);
	}

	public static JCType createType(JCClass cls, int arrayDepth) {
		return new BaseType(cls, arrayDepth);
	}

	/**
	 * Create new type or get the existing one from the cache. The cache holds
	 * the arrays with the increasing array depth for the particular class as
	 * the members. Simple class is used for the caching to make it independent
	 * on the real completion classes that can become obsolete and thus should
	 * be garbage collected.
	 */
	public static JCType getType(JCClass cls, int arrayDepth) {
		if (cls == null) {
			return null;
		}

		JCType[] types = (JCType[]) typeCache.get(cls);
		if (types != null) {
			if (arrayDepth < types.length) {
				if (types[arrayDepth] == null) {
					types[arrayDepth] = new BaseType(types[0].getClazz(), arrayDepth);
				}
			} else { // array length depth too small for given array depth
				cls = types[0].getClazz();
				JCType[] tmp = new JCType[arrayDepth + 1];
				System.arraycopy(types, 0, tmp, 0, types.length);
				types = tmp;
				types[arrayDepth] = new BaseType(cls, arrayDepth);
				typeCache.put(cls, types);
			}
		} else { // types array not yet created
			cls = getSimpleClass(cls.getFullName(), cls.getPackageName().length());
			if (arrayDepth > 0) {
				types = new JCType[arrayDepth + 1];
				types[arrayDepth] = new BaseType(cls, arrayDepth);
			} else {
				types = new JCType[2];
			}
			types[0] = new BaseType(cls, 0);
			typeCache.put(cls, types);
		}

		return types[arrayDepth];
	}

	public static class BasePackage implements JCPackage {

		private String name;

		private JCClass[] classes;

		private int dotCnt = -1;

		private String lastName;

		public BasePackage(String name) {
			this(name, EMPTY_CLASSES);
		}

		public BasePackage(String name, JCClass[] classes) {
			this.name = name;
			this.classes = classes;
		}

		/** Get full name of this package */
		public final String getName() {
			return name;
		}

		public String getLastName() {
			if (lastName == null) {
				lastName = name.substring(name.lastIndexOf('.') + 1);
			}
			return lastName;
		}

		/** Get classes contained in this package */
		public JCClass[] getClasses() {
			return classes;
		}

		public void setClasses(JCClass[] classes) {
			this.classes = classes;
		}

		public int getDotCount() {
			if (dotCnt < 0) {
				int i = 0;
				do {
					dotCnt++;
					i = name.indexOf('.', i) + 1;
				} while (i > 0);
			}
			return dotCnt;
		}

		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCPackage p = (JCPackage) o;
			return name.compareTo(p.getName());
		}

		public int hashCode() {
			return name.hashCode();
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCPackage) {
				return name.equals(((JCPackage) o).getName());
			}
			if (o instanceof String) {
				return name.equals((String) o);
			}
			return false;
		}

		public String toString() {
			return name;
		}

	}

	public static class SimpleClass implements JCClass {

		protected String name;

		protected String packageName;

		protected String fullName;

		public SimpleClass(String name, String packageName) {
			this.name = name;
			this.packageName = packageName;
			if (name == null || packageName == null) {
				throw new NullPointerException("className=" + name + ", packageName=" + packageName);
			}
		}

		public SimpleClass(String fullName, int packageNameLen, boolean intern) {
			this.fullName = fullName;
			if (packageNameLen <= 0) {
				name = fullName;
				packageName = ""; // NOI18N
			} else {
				// use interned strings here
				name = fullName.substring(packageNameLen + 1);
				packageName = fullName.substring(0, packageNameLen);
				if (intern) {
					name = name.intern();
					packageName = packageName.intern();
				}
			}
		}

		SimpleClass() {
		}

		public final String getName() {
			return name;
		}

		public final String getPackageName() {
			return packageName;
		}

		public String getFullName() {
			if (fullName == null) {
				fullName = (packageName.length() > 0) ? (packageName + "." + name) : name; // NOI18N
			}
			return fullName;
		}

		public int getTagOffset() {
			return -1;
		}

		public boolean isInterface() {
			return false;
		}

		public int getModifiers() {
			return 0;
		}

		public JCClass getSuperclass() {
			return null;
		}

		public JCClass[] getInterfaces() {
			return EMPTY_CLASSES;
		}

		public JCField[] getFields() {
			return EMPTY_FIELDS;
		}

		public JCConstructor[] getConstructors() {
			return EMPTY_CONSTRUCTORS;
		}

		public JCMethod[] getMethods() {
			return EMPTY_METHODS;
		}

		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCClass c = (JCClass) o;

			int order = packageName.compareTo(c.getPackageName());
			if (order == 0) {
				order = name.compareTo(c.getName());
			}
			return order;
		}

		public int hashCode() {
			return name.hashCode() ^ packageName.hashCode();
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCClass) {
				JCClass c = (JCClass) o;
				return name.equals(c.getName()) && packageName.equals(c.getPackageName());
			}
			return false;
		}

		public String toString() {
			return (getPackageName().length() > 0) ? getPackageName() + '.' + getName().replace('.', '$') : getName()
					.replace('.', '$');
		}

	}

	/** Abstract class that assumes lazy initialization */
	public static abstract class AbstractClass extends SimpleClass {

		protected int modifiers;

		protected Body body;

		public AbstractClass(String name, String packageName, boolean iface, int modifiers) {
			super(name, packageName);
			this.modifiers = modifiers;
			if (iface) {
				this.modifiers |= INTERFACE_BIT;
			}
		}

		AbstractClass() {
			super();
		}

		/** Init internal representation */
		protected abstract void init();

		/** Is this class an interface? */
		public boolean isInterface() {
			return ((modifiers & INTERFACE_BIT) != 0);
		}

		/** Get modifiers for this class */
		public int getModifiers() {
			return modifiers & INTERFACE_BIT_FILTER;
		}

		public int getTagOffset() {
			if (body == null) {
				init();
			}
			return body.tagOffset;
		}

		/** Get superclass of this class */
		public JCClass getSuperclass() {
			if (body == null) {
				init();
			}
			return body.superClass;
		}

		/** Get interfaces this class implements */
		public JCClass[] getInterfaces() {
			if (body == null) {
				init();
			}
			return body.interfaces;
		}

		/** Get fields that this class contains */
		public JCField[] getFields() {
			if (body == null) {
				init();
			}
			return body.fields;
		}

		/** Get constructors that this class contains */
		public JCConstructor[] getConstructors() {
			if (body == null) {
				init();
			}
			return body.constructors;
		}

		/** Get methods that this class contains */
		public JCMethod[] getMethods() {
			if (body == null) {
				init();
			}
			return body.methods;
		}

		public static class Body {

			public int tagOffset;

			public JCClass superClass;

			public JCClass[] interfaces;

			public JCField[] fields;

			public JCConstructor[] constructors;

			public JCMethod[] methods;

		}

	}

	/** Description of the type */
	public static class BaseType implements JCType {

		protected JCClass clazz;

		protected int arrayDepth;

		public BaseType(JCClass clazz, int arrayDepth) {
			this.clazz = clazz;
			this.arrayDepth = arrayDepth;
			if (arrayDepth < 0) {
				throw new IllegalArgumentException("Array depth " + arrayDepth + " < 0."); // NOI18N
			}
		}

		BaseType() {
		}

		public JCClass getClazz() {
			return clazz;
		}

		public int getArrayDepth() {
			return arrayDepth;
		}

		public String format(boolean useFullName) {
			StringBuffer sb = new StringBuffer(useFullName ? getClazz().getFullName() : getClazz().getName());
			int ad = arrayDepth;
			while (ad > 0) {
				sb.append("[]"); // NOI18N
				ad--;
			}
			return sb.toString();
		}

		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCType t = (JCType) o;
			int order = clazz.compareTo(t.getClazz());
			if (order == 0) {
				order = arrayDepth - t.getArrayDepth();
			}
			return order;
		}

		public int hashCode() {
			return clazz.hashCode() + arrayDepth;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCType) {
				JCType t = (JCType) o;
				return clazz.equals(t.getClazz()) && arrayDepth == t.getArrayDepth();
			}
			return false;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(clazz.toString());
			int ad = arrayDepth;
			while (ad > 0) {
				sb.append("[]"); // NOI18N
				ad--;
			}
			return sb.toString();
		}

	}

	/** Description of the method parameter */
	public static class BaseParameter implements JCParameter {

		protected String name;

		protected JCType type;

		public BaseParameter(String name, JCType type) {
			this.name = name;
			this.type = type;
		}

		BaseParameter() {
		}

		/** Name of the parameter */
		public String getName() {
			return name;
		}

		/** Type of the parameter */
		public JCType getType() {
			return type;
		}

		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCParameter p = (JCParameter) o;
			return type.compareTo(p.getType()); // only by type
		}

		public int hashCode() {
			return type.hashCode() ^ name.hashCode();
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCParameter) {
				JCParameter p = (JCParameter) o;
				return type.equals(p.getType()); // only by type
			}
			return false;
		}

		public String toString() {
			return type.toString() + ' ' + name;
		}

	}

	public static class BaseField extends BaseParameter implements JCField {

		protected JCClass clazz;

		protected int modifiers;

		protected int tagOffset;

		public BaseField(JCClass clazz, String name, JCType type, int modifiers) {
			super(name, type);
			this.clazz = clazz;
			this.modifiers = modifiers;
		}

		BaseField() {
		}

		public int getModifiers() {
			return modifiers;
		}

		public JCClass getClazz() {
			return clazz;
		}

		public int getTagOffset() {
			return tagOffset;
		}

		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCField f = (JCField) o;
			int order = super.compareTo(o);
			if (order == 0) {
				order = name.compareTo(f.getName());
			}
			return order;
		}

		public int hashCode() {
			return type.hashCode() ^ name.hashCode() ^ modifiers;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCField) {
				JCField p = (JCField) o;
				return name.equals(p.getName()) && type.equals(p.getType());
			}
			return false;
		}

		public String toString() {
			return Modifier.toString(modifiers) + ' ' + super.toString();
		}

	}

	public static class BaseConstructor implements JCConstructor {

		protected JCClass clazz;

		protected int tagOffset;

		protected int modifiers;

		protected JCParameter[] parameters;

		protected JCClass[] exceptions;

		public BaseConstructor(JCClass clazz, int modifiers, JCParameter[] parameters, JCClass[] exceptions) {
			this.clazz = clazz;
			this.modifiers = modifiers;
			this.parameters = parameters;
			this.exceptions = exceptions;
		}

		BaseConstructor() {
		}

		public JCClass getClazz() {
			return clazz;
		}

		public int getTagOffset() {
			return tagOffset;
		}

		public int getModifiers() {
			return modifiers;
		}

		public JCParameter[] getParameters() {
			return parameters;
		}

		public JCClass[] getExceptions() {
			return exceptions;
		}

		/**
		 * This implementation expects that only the constructors inside one
		 * class will be compared.
		 */
		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCConstructor c = (JCConstructor) o;
			int order = 0;
			JCParameter[] mp = c.getParameters();
			int commonCnt = Math.min(parameters.length, mp.length);
			for (int i = 0; i < commonCnt; i++) {
				order = parameters[i].compareTo(mp[i]);
				if (order != 0) {
					return order;
				}
			}
			order = parameters.length - mp.length;
			return order;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCConstructor) {
				return (compareTo(o) == 0);
			}
			return false;
		}

		public int hashCode() {
			int h = 0;
			for (int i = 0; i < parameters.length; i++) {
				h ^= parameters[i].hashCode();
			}
			return h;
		}

		String toString(String returnTypeName, String methodName) {
			StringBuffer sb = new StringBuffer(Modifier.toString(modifiers));
			sb.append(' ');
			sb.append(returnTypeName);
			sb.append(methodName);
			// Add parameters
			sb.append('(');
			int cntM1 = parameters.length - 1;
			for (int i = 0; i <= cntM1; i++) {
				sb.append(parameters[i].toString());
				if (i < cntM1) {
					sb.append(", "); // NOI18N
				}
			}
			sb.append(')');
			// Add exceptions
			cntM1 = exceptions.length - 1;
			if (cntM1 >= 0) {
				sb.append(" throws "); // NOI18N
				for (int i = 0; i <= cntM1; i++) {
					sb.append(exceptions[i].toString());
					if (i < cntM1) {
						sb.append(", "); // NOI18N
					}
				}
			}
			return sb.toString();
		}

		public String toString() {
			return toString("", getClazz().getName()); // NOI18N
		}

	}

	public static class BaseMethod extends BaseConstructor implements JCMethod {

		protected String name;

		protected JCType returnType;

		public BaseMethod(JCClass clazz, String name, int modifiers, JCType returnType, JCParameter[] parameters,
				JCClass[] exceptions) {
			super(clazz, modifiers, parameters, exceptions);
			this.name = name;
			this.returnType = returnType;
		}

		BaseMethod() {
		}

		public String getName() {
			return name;
		}

		public JCType getReturnType() {
			return returnType;
		}

		public int compareTo(Object o) {
			if (this == o) {
				return 0;
			}
			JCMethod m = (JCMethod) o;
			int order = name.compareTo(m.getName());
			if (order == 0) {
				order = super.compareTo(o);
			}
			return order;
		}

		public int hashCode() {
			return name.hashCode() ^ super.hashCode();
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof JCMethod) {
				return (compareTo(o) == 0);
			}
			return false;
		}

		public String toString() {
			String rtn = getReturnType().toString();
			return toString((rtn.length() > 0) ? rtn + ' ' : "", name); // NOI18N
		}

	}

	public abstract static class AbstractProvider implements JCClassProvider {

		public abstract Iterator getClasses();

		public boolean append(JCClassProvider cp) {
			Iterator i = cp.getClasses();
			while (i.hasNext()) {
				JCClass c = (JCClass) i.next();
				if (!cp.notifyAppend(c, false)) {
					return false;
				}
				if (!appendClass(c)) {
					return false;
				}
				if (!cp.notifyAppend(c, true)) {
					return false;
				}
			}
			return true;
		}

		protected boolean appendClass(JCClass c) {
			return true;
		}

		public void reset() {
		}

		/**
		 * This method is executed by the target Class Provider to notify this
		 * provider about the class appending.
		 * 
		 * @param c
		 *            JC class that was appended
		 * @return true to continue building, false to stop build
		 */
		public boolean notifyAppend(JCClass c, boolean appendFinished) {
			return true;
		}

	}

	public static class ListProvider extends AbstractProvider {

		private List classList;

		public ListProvider() {
			classList = new ArrayList();
		}

		public ListProvider(List classList) {
			this.classList = classList;
		}

		protected boolean appendClass(JCClass c) {
			classList.add(c);
			return true;
		}

		public Iterator getClasses() {
			return classList.iterator();
		}

		public int getClassCount() {
			return classList.size();
		}

	}

	
	public static int getDebugMode() {
		return debugMode;
	}

	public static void setDebugMode(int newDebugMode) {
		debugMode = newDebugMode;
	}

	/** Interface for providing callback initialization of JavaCompletion. */
	public static interface JCFinderInitializer {
		public void initJCFinder();
	}
}
