package tethys.niluswraps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import nilus.ResponsibleParty;

/**
 * A few static checks of some nilus classes to see if it's 
 * worth writing them or not. 
 * @author dg50
 *
 */
public class NilusChecker {

	public static boolean isEmpty(Object nilusObject) {
		boolean empty = true;
		if (nilusObject == null) {
			return true;
		}
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
	
	private static boolean isEmptyList(List got) {
		if (got == null) {
			return true;
		}
		return got.size() == 0;
	}

	public static boolean isEmptyString(String string) {
		if (string == null || string.length() == 0) {
			return true;
		}
		return false;
	}
}
