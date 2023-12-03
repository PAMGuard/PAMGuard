package tethys.niluswraps;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nilus.ChannelInfo.Sampling.Regimen;
import nilus.Deployment;
import nilus.Helper;
import nilus.QualityValueBasic;
import tethys.TethysTimeFuncs;

/**
 * Functions to automatically unpack a document from Tethys into a nilus object. 
 * @author dg50
 *
 */
public class NilusUnpacker {

	public Object unpackDocument(Document doc, Class nilusClass) throws SecurityException {
		
		Object nilusObject = null;
		nilusObject = unpackNilusClass(nilusClass, doc.getDocumentElement(), true);		
		return nilusObject;
		
	}
	
	/**
	 * Unpack an xml element into a nilus class. Should recursively work through
	 * all sub elements and lists, etc. 
	 * @param nilusClass class to unpack to
	 * @param nilusElement xml element
	 * @return nilus object. 
	 */
	private Object unpackNilusClass(Class<?> nilusClass, Node nilusElement, boolean useHelper) {

		Object nilusObject = null;
		/*
		 * First, find the constructor. Every class should have a zero argument
		 * constructor. 
		 */
		QualityValueBasic qb = null;
				
		Constructor<?> nilusConstructor = null;;
		try {
			nilusConstructor = nilusClass.getConstructor(null);
			nilusObject = nilusConstructor.newInstance(null);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//			e.printStackTrace();
//			return null;
		}
		
		if (useHelper) {
			try {
				Helper.createRequiredElements(nilusObject);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return unpackNilusObject(nilusObject, nilusElement);
	}
	private Object unpackNilusObject(Object nilusObject, Node nilusElement) {
		
//		if (nilusConstructor == null) {
//			// try to find any constructor and see what it takes. 
//			Constructor<?>[] allConstructors = nilusClass.getConstructors(); 
//			if (allConstructors.length == 0) {
//				System.out.println("Nilus unpacker: Unable to find constructor for class " + nilusClass.toString());
//				return null;
//			}
//			nilusConstructor = allConstructors[0]; // only try the first one for now. 
//			Parameter[] params = nilusConstructor.getParameters();
//			if (params.length == 1 && params[0].getType() == String.class) {
//				try {
//					nilusObject = nilusConstructor.newInstance(nilusElement.getTextContent());
//				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
//						| InvocationTargetException | DOMException e) {
//					e.printStackTrace();
////					return null;
//				}
//			}
//		}
//		if (nilusObject == null) {
//			System.out.println("Nilus unpacker: Unable to construct nilus object " + nilusClass.toString());
//			return null;
//		}
		if (nilusObject == null) {
			return null;
		}
		
		Class nilusClass = nilusObject.getClass();
		
		/**
		 * Get the declared fields for this class. May need to worry about 
		 * inherited fields ? . 
		 */
		Field[] fields = nilusClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (isFieldFinal(fields[i])) {
				continue;
			}
			String fieldName = fields[i].getName();
			String elementName = fieldName;
			// now try to find an annotation for the name, which might be
			// different. 
			XmlElement an = fields[i].getAnnotation(XmlElement.class);
			if (an == null) {
//				System.out.printf("No XmlElement annotation found for field %s in class %s\n",
//						nilusClass.getName(), fieldName);
			}
			boolean required = false;
			if (an != null) {
				required = an.required();
				elementName = an.name();
			}
			// find the xml element for it in parent
			Element child = findChild(nilusElement, elementName);
			/**
			 * Here we can put in some bespoke 'fixes', e.g. if the name in the xml has 
			 * changed for some reason. 
			 */
			if (child == null) {
				String altName = alternateElName(fieldName);
				if (altName != null) {
					child = findChild(nilusElement, altName);
				}
			}
			
			/**
			 * It is OK for a child not to exist, since not all elements are required, so 
			 * it's possible that they are simply not there. 
			 */
			if (child == null) {
				if (required) {
					System.out.printf("Field %s in class %s is required but cannot be found\n", fieldName, nilusClass.getName());
				}
				continue;
			}
			String childName = child.getNodeName();
						
			Object exObject = null; // this is the object (which may be a primitive) we're going to give to the setter. 

			if (List.class.isAssignableFrom(fields[i].getType())) {
				exObject = getNilusList(nilusObject, fields[i], (Element) nilusElement);
			}
			else {
				// find a setter for it. 
				Method setter = findSetter(nilusClass, fieldName);
				//			System.out.printf("Field %s with element %s and setter %s\n", fieldName, childName, setter);
				if (setter == null) {
					System.out.printf("No setter available for field %s and element %s\n", fieldName, elementName);
					continue;  // eventually do something more intelligent here. 
				} 
				Parameter[] params = setter.getParameters();
				Parameter setterParam = params[0];
				Class<?> paramClass = setterParam.getType();

				exObject = getElementObject(nilusObject, fieldName, paramClass, child);
				if (exObject != null) {
					try {
						// every nilus setter should have a single argument.
						setter.invoke(nilusObject, exObject);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return nilusObject;
	}

	private boolean isFieldFinal(Field field) {
		int mods = field.getModifiers();
		return Modifier.isFinal(mods);
	}

	/**
	 * Unpack the child element into the given parameter class. The element will 
	 * either be a primitive type, or a class, which has to be one of the nulus classes
	 * so should follow nilus rules of constructors, setters, etc. 
	 * @param fieldName 
	 * @param nilusObject 
	 * @param paramClass
	 * @param child
	 * @return
	 */
	private Object getElementObject(Object nilusObject, String fieldName, Class<?> paramClass, Node child) {
		String className = paramClass.getName();
		
		switch(className) {
		case "int":
		case "java.lang.Integer":
			return unpackInteger(child);
		case "java.math.BigInteger":
			return unpackBigInteger(child);
		case "java.lang.String":
			return unpackString(child);
		case "double":
		case "java.lang.Double":
			return unpackDouble(child);
		case "javax.xml.datatype.XMLGregorianCalendar":
			return unpackGregorianCalendar(child);
		}
		if (className.startsWith("nilus.")) {
			Object gotObject = null;
			gotObject = handleFunnys(nilusObject, fieldName, paramClass, child);
			if (gotObject == null) {
				// the helper should have made most required objects. so try to find a getter. 
				// and get a pre created version of the object. 
				Method getter = findGetter(nilusObject.getClass(), fieldName);
				if (getter != null) {
					try {
						gotObject = getter.invoke(nilusObject, null);
						if (gotObject == null) {
							Helper.createElement(nilusObject, fieldName);
							gotObject = getter.invoke(nilusObject, null);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
						//					e.printStackTrace();
					}
				}
			}
			if (gotObject != null) {
				return unpackNilusObject(gotObject, child);
			}
			else {
				return unpackNilusClass(paramClass, child, false);
			}
		}
		
		System.out.println("Unnown or unhandled data type: " + className);
		return null;
	}

	private Object handleFunnys(Object nilusObject, String fieldName, Class<?> paramClass, Node child) {
		Method setter = findSetter(nilusObject.getClass(), fieldName);
		if (setter == null) {
			return null;
		}
		if (paramClass == QualityValueBasic.class) {
			String val = child.getTextContent();
			QualityValueBasic qb = QualityValueBasic.fromValue(val);
			try {
				setter.invoke(nilusObject, qb);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return qb;
		}
		return null;
	}

	/**
	 * Unpack a list of nilus objects (or primatives?)
	 * @param parentObject parent object that will contain the list
	 * @param field field name
	 * @param parentEl parent element that contains the listed items. 
	 * @return
	 */
	private Object getNilusList(Object parentObject, Field field, Element parentEl) {
		// 
		String fieldName = field.getName();
		Method setter = findSetter(parentObject.getClass(), fieldName);
		Method getter = findGetter(parentObject.getClass(), fieldName);
		List nilusList = null;
		try {
			nilusList = (List) getter.invoke(parentObject, null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(nilusList);
		// need to work out what type of object is in the list from the getter signaturel 
		Class<?> retType = getter.getReturnType();
		AnnotatedType aRet = getter.getAnnotatedReturnType();
		String nm = aRet.getType().getTypeName();
		int n1 = nm.indexOf("<");
		int n2 = nm.indexOf(">");
		if (n1 < 1) {
			System.out.println("Invalid class");
		}
		String clsName = nm.substring(n1+1, n2);
		Class listCls = null;
		try {
			listCls = Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (listCls == null) {
			System.out.printf("Unable to find list class %s for nilus element %s\n", clsName, fieldName);
			return null;
		}
		
		Element el = (Element) parentEl;
//		el.get
//		System.out.println("Unpack children of " + parentEl.getNodeName());
		
		NodeList nodeList = parentEl.getChildNodes();
		int n = nodeList.getLength();
		int m = 0;
		for (int i = 0; i < n; i++) {
			 Node aNode = nodeList.item(i);
		if (aNode.getNodeType() != Node.ELEMENT_NODE) {
			continue;
		}
//			 System.out.println("Unpack node: " + aNode.getNodeName());
			Object listObject = getElementObject(parentObject, field.getName(), listCls, aNode);
			if (listObject != null) {
				nilusList.add(listObject);
				m++;
			}
		}
//		System.out.printf("Added %d children to list\n", m);
//		Node aChild = parentEl.getFirstChild();
//		while (aChild != null) {
//			 System.out.println("Unpack node: " + aChild.getNodeName());
//			Object listObject = getElementObject(listCls, aChild);
//			if (listObject != null) {
//				nilusList.add(listObject);
//			}
//			aChild.getNextSibling();
//		}
//		
		
		return nilusList;
	}

	/**
	 * Unpack an element as a String
	 * @param child
	 * @return
	 */
	private Object unpackString(Node child) {
		if (child == null) {
			return null;
		}
		return child.getTextContent();
	}

	/**
	 * Unpack an element as a BigInteger
	 * @param child
	 * @return long integer converted to BigInteger
	 */
	private BigInteger unpackBigInteger(Node child) {
		if (child == null) {
			return null;
		}
		Long value = null;
		value = Long.valueOf(child.getTextContent());
		return BigInteger.valueOf(value);
	}

	/**
	 * Unpack an element as am int32 (Integer)
	 * @param child
	 * @return int32 value 
	 */
	private Integer unpackInteger(Node child) {
		if (child == null) {
			return null;
		}
		Integer value = null;
		value = Integer.valueOf(child.getTextContent());
		return value;
	}


	/**
	 * Unpack an element as a Double
	 * @param child
	 * @return double precision value or null
	 */
	private Double unpackDouble(Node child) {
		if (child == null) {
			return null;
		}
		Double value = null;
		value = Double.valueOf(child.getTextContent());
		return value;
	}

	/**
	 * Unpack an element as a GregorianCalendar
	 * @param child
	 * @return GregorianCalendar value
	 */
	private XMLGregorianCalendar unpackGregorianCalendar(Node child) {
		if (child == null) {
			return null;
		}
		String calString = child.getTextContent();
		XMLGregorianCalendar xCal = TethysTimeFuncs.fromGregorianXML(calString);
		return xCal;
	}

	/**
	 * Find a child element with a given name. Assumes there is only one
	 * so not used with lists. 
	 * @param parentNode parent XML node
	 * @param childName name of child node. 
	 * @return
	 */
	Element findChild(Node parentNode, String childName) {
		if (parentNode instanceof Element == false) {
			return null;
		}
		Element parent = (Element) parentNode;
		NodeList children = parent.getElementsByTagName(childName);
		if (children == null || children.getLength() == 0) {
			String ch1 = childName.substring(0,1).toUpperCase();
			childName = ch1+childName.substring(1);
			children = parent.getElementsByTagName(childName);
			if (children == null) {
				return null;
			}
		}
		int n = children.getLength();
		for (int i = 0; i < n; i++) {
			Node child = children.item(i);
			String childNodeName = child.getNodeName();
			if (child.getNodeName().equals(childName)) {
				return (Element) child;
			}
		}
		return null;
	}
	
	/**
	 * Get an alternative element name (for old databases ?)
	 * @param fieldName
	 * @return
	 */
	private String alternateElName(String fieldName) {
		switch(fieldName) {
		case "sampleRateKHz":
			return "sampleRate_kHz";
		}
		return null;
	}
	
	/**
	 * Return all the setters in a class;
	 * @param nilusClass
	 * @return
	 */
	private ArrayList<Method> findSetters(Class<?> nilusClass) {
		Method[] methods = nilusClass.getMethods();
		ArrayList methodList = new ArrayList<>();
		for (int i = 0; i < methods.length; i++) {
			String name = methods[i].getName();
			if (name.startsWith("set")) {
				methodList.add(methods[i]);
			}
		}
		return methodList;
	}
	/**
	 * Find setter functions for a given field name. Generally
	 * this is a capitalization of the first character and 'set' 
	 * in front of it. 
	 * @param nilusClass class containing the method
	 * @param fieldName field name
	 * @return Method or null
	 */
	private Method findSetter(Class nilusClass, String fieldName) {
		String setterName = fieldName;
		if (setterName.startsWith("set") == false) {
			setterName = "set" + setterName;
		}
		Method[] methods = nilusClass.getMethods();
		if (methods == null) {
			return null;
		}
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(setterName)) {
				return methods[i];
			}
		}
				
		return null;
	}

	/**
	 * Find getter functions for a given field name. Generally
	 * this is a capitalization of the first character and 'get' 
	 * in front of it. 
	 * @param nilusClass class containing the method
	 * @param fieldName field name
	 * @return Method or null
	 */
	private Method findGetter(Class nilusClass, String fieldName) {
		String setterName = fieldName;
		if (setterName.startsWith("get") == false) {
			setterName = "get" + setterName;
		}
		Method[] methods = nilusClass.getMethods();
		if (methods == null) {
			return null;
		}
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(setterName)) {
				return methods[i];
			}
		}
		return null;
	}
	

	public static void main(String[] args) {
		String demoFile = "C:\\PAMGuardTest\\Tethys\\Meygen20223.xml";
		File file = new File(demoFile);
		System.out.printf("Unpacking file %s exists %s\n" , demoFile, file.exists());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NilusUnpacker unpacker = new NilusUnpacker();
		Class<Regimen> nilusClass = Regimen.class;

		ArrayList<Method> setters = null;
		try {
			setters = unpacker.findSetters(nilusClass);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		for (Method aSetter : setters) {
//			XmlElement an = aSetter.getAnnotation(XmlElement.class);
//			System.out.printf("Class %s Setter %s has xmlElement %s\n", nilusClass.getName(), aSetter.getName(), an);
//			Annotation[] anns = aSetter.getAnnotations();
//			for (int i = 0; i < anns.length; i++) {
//
//				System.out.printf("Class %s Setter %s has xmlElement %s\n", nilusClass.getName(), aSetter.getName(), anns[i]);
//			}
//		}
//		

//		Field[] fields = nilusClass.getDeclaredFields();
//		for (int i = 0; i < fields.length; i++) {
//			XmlElement an = fields[i].getAnnotation(XmlElement.class);
//			String fieldName = "unk";
//			if (an != null) {
//				fieldName = an.name();
//			}
//			System.out.printf("Class %s Field %s has xmlElement %s\n", nilusClass.getName(), fields[i].getName(), fieldName);
//		}
//		BeanInfo beanInfo = null;
//		try {
//			beanInfo = Introspector.getBeanInfo(aClass);
//		} catch (IntrospectionException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		
		
		
		Object obj = null;
		try {
			obj = unpacker.unpackDocument(doc, Deployment.class);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		System.out.println(obj);
	}
	
}
