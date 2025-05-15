package tethys.niluswraps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import tethys.Collection;
import tethys.dbxml.DBXMLConnect;

public class NilusDocumentWrapper<T> {

	public T nilusObject;
	
	public String documentName;
	
	public NilusDocumentWrapper(T nilusDocument) {
		super();
		this.nilusObject = nilusDocument;
	}
	
	public String getDocumentId() {
		if (nilusObject == null) {
			return null;
		}
		return DBXMLConnect.getDocumentId(nilusObject);
	}
	
	/**
	 * Get an object out of the nilus object using a series of sequential getter functions 
	 * @param getterName function names (any number to work through class list) . 
	 * @return
	 */
	public Object getGotObjects(String... getterNames) {
		/**
		 * Be aware that this will probably get called in preference to 
		 * the function below, so need to check to see if the 
		 */
		return getGotObjects(nilusObject, getterNames);
	}

	/**
	 * Get an object out of the given object using a series of sequential getter functions 
	 * @param source source object
	 * @param getterName function names (any number to work through class list) . 
	 * @return
	 */
	public Object getGotObjects(Object source, String... getterNames) {
		Object obj = source;
		for (int i = 0; i < getterNames.length; i++) {
			obj = getGotObject(obj, getterNames[i]);
			if (obj == null) {
				break;
			}
		}
		return obj;
	}
	
	/**
	 * Get an object out of the main nilus object using a getter function (no function parameters). 
	 * @param source source object
	 * @param getterName function name. 
	 * @return
	 */
	public Object getGotObject(String getterName) {
		return getGotObject(nilusObject, getterName);
	}
	
	/**
	 * Get an object out of the given object using a getter function (no function parameters). 
	 * @param source source object
	 * @param getterName function name. 
	 * @return
	 */
	public Object getGotObject(Object source, String getterName) {
		if (source == null) {
			return null;
		}
		Class sourceClass = source.getClass();
		Method getId;
		try {
			getId = sourceClass.getDeclaredMethod(getterName, null);
			Object[] inputs = new Object[0];
			Object res = getId.invoke(source, inputs);
			return res;
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.printf("Unable to find method %s in object %s\n", getterName, source.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the class of the nilus object. 
	 * @return Java class of nilus object. 
	 */
	public Class getNilusClass() {
		return nilusObject.getClass();
	}
	
	/**
	 * Collection for the nilus object. 
	 * @return
	 */
	public Collection getCollection() {
		return Collection.fromClass(getNilusClass());
	}

	/**
	 * @return the nilusObject
	 */
	public T getNilusObject() {
		return nilusObject;
	}
	
}
