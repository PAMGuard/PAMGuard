package tethys.niluswraps;

import java.awt.Window;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.renjin.methods.Methods;

import PamView.dialog.warn.WarnOnce;
import nilus.Calibration;
import nilus.Calibration.QualityAssurance;
import nilus.Helper;
import nilus.ResponsibleParty;

/**
 * A few static checks of some nilus classes to see if it's 
 * worth writing them or not. 
 * @author dg50
 *
 */
public class NilusChecker {
	
	public static void main(String args[]) {
		Calibration cal = new Calibration();
		try {
			Helper.createRequiredElements(cal);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		QualityAssurance qa;
		cal.setQualityAssurance(qa = new QualityAssurance());
		qa.setComment("Nothing to comment on ");
		
		int removed = removeEmptyFields(cal);
		System.out.printf("%d fields removed from object %s\n", removed, cal);

		
		ArrayList<Field> missing = checkEmptyFields(cal);
		for (Field field : missing) {
			System.out.printf("Field %s is required but empty in %s\n", field.getName(), field.getDeclaringClass().toString());
		}
	}
	
	public static boolean warnEmptyFields(Window owner, Object nilusObject) {
		ArrayList<Field> emptyFields = findEmptyFields(nilusObject, true);
		if (emptyFields == null || emptyFields.size() == 0) {
			return true;
		}
		String msg = String.format("<html>One or more fields in the nilus object %s are required but empty:<br>", nilusObject.getClass().getName());
		for (Field f : emptyFields) {
			msg += String.format("<br>Field %s in object %s", f.getName(), f.getDeclaringClass().getName());
		}
		msg += "<br><br>It is likely that this document will fail to write to the Tethys database.</html>";
		String tit = "Incomplete Tethys data";
		WarnOnce.showWarning(owner, tit, msg, WarnOnce.WARNING_MESSAGE);
		return false;
	}
	
	/**
	 * Find empty fields
	 * @param nilusObject object to search
	 * @param onlyRequired only list required fields. 
	 * @return list of empty, and optionally also required, fields. 
	 */
	public static ArrayList<Field> findEmptyFields(Object nilusObject, boolean onlyRequired) {
		return findEmptyFields(nilusObject, new ArrayList<Field>(), onlyRequired);
	}
	
	private static ArrayList<Field> findEmptyFields(Object nilusObject, ArrayList<Field> found, boolean onlyRequired) {
		if (nilusObject == null) {
			return found;
		}
		int removed = 0;

		Class<? extends Object> nilusClass = nilusObject.getClass();
		if (nilusClass.getCanonicalName().contains("java.lang")) {
			return found;
		}
		Method[] methods = nilusClass.getDeclaredMethods();
		Field[] fields = nilusClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Method getter = findGetter(fields[i], methods);
			if (getter == null) {
//				System.out.printf("Unable to find getter for field %s in %s\n", fields[i].getName(), nilusClass.getName());
				continue;
			}
			boolean required = isRequired(fields[i]);
//			System.out.printf("Field %30s is %s required\n", fields[i].getName(), required ? "   " : "NOT");
			Object gotObj = null;
			try {
				gotObj = getter.invoke(nilusObject, new Object[0]);
			} catch (IllegalAccessException | InvocationTargetException e) {
//				System.out.printf("Unable to invoce getter %s on %s\n", getter.getName(), nilusObject);
				continue;
			}
			boolean empty = isEmpty(gotObj);
			if (empty) {
				if (required || !onlyRequired) {
					found.add(fields[i]);
				}
			}
			else {
				found = findEmptyFields(gotObj, found, onlyRequired);
			}
		}
		return found;		
	}
	
	/**
	 * Remove empty fields from a nilus object. <br>
	 * An empty field is a field that is null, or has a String that is empty, or 
	 * only contains elements which are all themselves empty. i.e. an object that references
	 * empty objects will be considered empty.
	 * @param nilusObject
	 * @return number of empty fields removed. 
	 */
	public static int removeEmptyFields(Object nilusObject) {
		if (nilusObject == null) {
			return 0;
		}
		int removed = 0;

		Class<? extends Object> nilusClass = nilusObject.getClass();
		if (nilusClass.getCanonicalName().contains("java.lang")) {
			return 0;
		}
		Method[] methods = nilusClass.getDeclaredMethods();
		Field[] fields = nilusClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Method getter = findGetter(fields[i], methods);
			Method setter = findSetter(fields[i], methods);
			if (getter == null) {
//				System.out.printf("Unable to find getter for field %s in %s\n", fields[i].getName(), nilusClass.getName());
				continue;
			}
			if (setter == null) {
//				System.out.printf("Unable to find setter for field %s in %s\n", fields[i].getName(), nilusClass.getName());
				continue;
			}
			boolean required = isRequired(fields[i]);
//			System.out.printf("Field %30s is %s required\n", fields[i].getName(), required ? "   " : "NOT");
			Object gotObj = null;
			try {
				gotObj = getter.invoke(nilusObject, null);
			} catch (IllegalAccessException | InvocationTargetException e) {
//				System.out.printf("Unable to invoce getter %s on %s\n", getter.getName(), nilusObject);
				continue;
			}
			boolean empty = isEmpty(gotObj);
			if (empty && gotObj != null && canRemove(fields[i])) {
				try {
//					System.out.printf("Removing empty field %s in object %s\n", fields[i].getName(), nilusObject);
//					Object args = new Object[1];
					setter.invoke(nilusObject, new Object[1]);
					removed++;
				} catch (IllegalAccessException | InvocationTargetException e) {
					System.out.printf("Unable to invoce setter %s on %s\n", getter.getName(), nilusObject);
					continue;
				}
			}
			else {
				removed += removeEmptyFields(gotObj);
			}
		}
		return removed;		
	}
	
	/**
	 * Fields that can be removed. 
	 * @param field
	 * @return
	 */
	private static boolean canRemove(Field field) {
		if (field == null) {
			return true;
		}
		Class fClass = field.getType();
		if (fClass == String.class) {
			return true;
		}
		if (List.class.isAssignableFrom(fClass)) {
			return false;
		}
		if (fClass.isPrimitive()) {
			return false;
		}
		String className = fClass.getCanonicalName();
		if (className.contains("nilus.")) {
			return true;
		}
		return false;
	}

	/**
	 * Check an object for empty and required fields. 
	 * @param nilusObject
	 * @return a list of required empty fields in the nilusObjec and any objects references by that object. 
	 */
	public static ArrayList<Field> checkEmptyFields(Object nilusObject) {
		return checkEmptyFields(nilusObject, new ArrayList<Field>());
	}
	
	/**
	 * Check an object for empty and required fields. 
	 * @param nilusObject
	 * @param emptyFields
	 * @return
	 */
	private static ArrayList<Field> checkEmptyFields(Object nilusObject, ArrayList<Field> emptyFields) {
		if (nilusObject == null) {
			return emptyFields;
		}

		Class<? extends Object> nilusClass = nilusObject.getClass();
		if (nilusClass.isPrimitive()) {
			return emptyFields;
		}
		if (nilusClass.getCanonicalName().contains("java.lang")) {
			return emptyFields;
		}
		Method[] methods = nilusClass.getDeclaredMethods();
		Field[] fields = nilusClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Method getter = findGetter(fields[i], methods);
			Method setter = findSetter(fields[i], methods);
			if (getter == null) {
//				System.out.printf("Unable to find getter for field %s in %s\n", fields[i].getName(), nilusClass.getName());
				continue;
			}
			if (setter == null) {
//				System.out.printf("Unable to find setter for field %s in %s\n", fields[i].getName(), nilusClass.getName());
				continue;
			}
			boolean required = isRequired(fields[i]);
//			System.out.printf("Field %30s is %s required\n", fields[i].getName(), required ? "   " : "NOT");
			Object gotObj = null;
			try {
				gotObj = getter.invoke(nilusObject, null);
			} catch (IllegalAccessException | InvocationTargetException e) {
//				System.out.printf("Unable to invoce getter %s on %s\n", getter.getName(), nilusObject);
				continue;
			}
			boolean empty = isEmpty(gotObj);
			if (empty) {
				if (required) {
					emptyFields.add(fields[i]);
				}
			}
			else {
				checkEmptyFields(gotObj, emptyFields);
			}
//			if (required == true && empty == true) {
//				System.out.printf("Field %s is required but empty in %s\n", fields[i].getName(), nilusObject.toString());
//			}
		}
		return emptyFields;
	}
	
	/**
	 * See if a field has an annotation that indicates it's required. 
	 * @param field field
	 * @return required
	 */
	private static boolean isRequired(Field field) {
		Annotation[] annots = field.getAnnotations();
		for (int a = 0; a < annots.length; a++) {
//			System.out.printf("Field %s has annotation %d %s\n", fields[i].getName(), a, annots[a].toString());
			String str = annots[a].toString();
			if (str.contains("required=true")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Find a getter for a field. This will either be get... or is...
	 * @param field
	 * @param methods list of methods to search
	 * @return found method or null
	 */
	private static Method findGetter(Field field, Method[] methods) {
		String name = field.getName();
		String poss = "get"+name;
		Method found = findMethod(poss, methods);
		if (found != null) {
			return found;
		}
		poss = "is" + name;
		return findMethod(poss, methods);
	}
	
	/**
	 * Fine a setter for a field. This will always be set...
	 * @param field field
	 * @param methods list of methods to search
	 * @return found method or null
	 */
	private static Method findSetter(Field field, Method[] methods) {
		String name = field.getName();
		String poss = "set" + name;
		return findMethod(poss, methods);
	}
	
	/**
	 * Find a method based on it's name (case insensitive). 
	 * @param name name of method
	 * @param methods list of methods to search
	 * @return found method or null
	 */
	private static Method findMethod(String name, Method[] methods) {
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(name)) {
				return methods[i];
			}
		}
		return null;
	}

	/**
	 * Test if an object is empty. <br>
	 * An object is considered empty if any of the following criteria are met:
	 * <ul>
	 * <li>The object is null</li>
	 * <li>The object is a zero length string</li>
	 * <li>The object is not null, but all of it's fields satisfy this same criteria of being empty</li>
	 * <li>The object is a list which has no elements</li>
	 * </ul>
	 * Primitive types are never empty. 
	 * @param nilusObject
	 * @return true if it's empty
	 */
	public static boolean isEmpty(Object nilusObject) {
		if (nilusObject == null) {
			return true;
		}
		if (nilusObject instanceof String) {
			String str = (String) nilusObject;
			return (str.length() == 0);
		}
		if (nilusObject instanceof List) {
			return isEmptyList((List) nilusObject);
		}
		if (nilusObject.getClass().isPrimitive()) {
			return false;
		}
		boolean empty = true;
		// and check all getters 
		Class<? extends Object> nilusClass = nilusObject.getClass();
		Method[] methods = nilusClass.getDeclaredMethods();
		// searching for getters. 
		int nGet = 0;
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
				nGet ++;
				try {
					Object got = method.invoke(nilusObject, null);
					if (got != null) {
						if (got instanceof String) {
							if (isEmptyString((String) got) == false) {
								empty = false;
							}
						}
						else if (got instanceof List<?>) {
							if (isEmptyList((List) got) == false) {
								empty = false;
							}
						}
						else if (isEmpty(got) == false) {// it's some other class type, so recurecively ask back here.
							empty = false;
						}
						
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					return false; // better save than sorry if we don't understand. 
				}
			}
		}
		if (nGet == 0) {
			// there weren't any understandable getters, so assume not empty. May be some other primitive type. 
			empty = false;
		}
			
		return empty;
	}
	
	/**
	 * A list is considered empty if it has no elements
	 * @param list
	 * @return true if empty
	 */
	private static boolean isEmptyList(List list) {
		if (list == null) {
			return true;
		}
		return list.size() == 0;
	}

	/**
	 * A String is empty if it is null or of zero length
	 * @param string string
	 * @return true if empty
	 */
	public static boolean isEmptyString(String string) {
		if (string == null || string.length() == 0) {
			return true;
		}
		return false;
	}
}
