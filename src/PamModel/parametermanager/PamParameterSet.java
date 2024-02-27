package PamModel.parametermanager;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;


/**
 * Description of the parameters within a class. Primarily holds a list
 * of PamParameterDataInterface objects each describing one field in the 
 * object. 
 * @author Doug Gillespie
 *
 */
public class PamParameterSet {

	private Hashtable<String, PamParameterData> parameterDatas;
	
	private List<Field> hiddenFields;
	
	private Object parentObject;
	
	private static boolean printDebug = false;
	
	public enum ParameterSetType {DETECTOR, DISPLAY};
	
	private ParameterSetType parameterSetType;
	/**
	 * Standard modifiers to exclude. This is important for many classes which will tend to 
	 * do crazy things such as incorporate ALL of their final fields, e.g. when a Color 
	 * object is written, you'll get a million static colours and still not get the RGB you want. 
	 * (Color actually got a special - may need to do this for some other types too !)
	 */
	public static final int STANDARD_MODIFIER_EXCLUSIONS = Modifier.FINAL | Modifier.STATIC;
	
	/**
	 * Create and empty set. Generally it's better to 
	 * use one of the autoGenerate functions. 
	 * @param parentObject
	 */
	public PamParameterSet(Object parentObject) {
		this.parentObject = parentObject;
		parameterDatas = new Hashtable<>();
		hiddenFields = new ArrayList<>();
	}

	/**
	 * Automatically generate a parameter set for a class. Will include all public fields and 
	 * any private or protected fields for which a getter can be found that has a similar enough name
	 * @param parentObject class to generate description for. Exception is anything that's listed 
	 * in the STANDARD_MODIFIER_EXCLUSIONS list (FINAL or STATIC).
	 * @return Created parameter set. 
	 */
	@Deprecated 
	public static PamParameterSet autoGenerate(Object parentObject) {
		return autoGenerate(parentObject, ParameterSetType.DETECTOR);
	}
	/**
	 * Automatically generate a parameter set for a class. Will include all public fields and 
	 * any private or protected fields for which a getter can be found that has a similar enough name
	 * @param parentObject class to generate description for. Exception is anything that's listed 
	 * in the STANDARD_MODIFIER_EXCLUSIONS list (FINAL or STATIC).
	 * @return Created parameter set. 
	 */
	public static PamParameterSet autoGenerate(Object parentObject, ParameterSetType parameterSetType) {
		PamParameterSet paramSet = autoGenerate(parentObject, STANDARD_MODIFIER_EXCLUSIONS);
		paramSet.setParameterSetType(parameterSetType);
		return paramSet;
	}
	
	/**
	 * 
	 * Automatically generate a parameter set for a class. Will include all public fields and 
	 * any private or protected fields for which a getter can be found that has a similar enough name
	 * so long as the fields modifiers are not included within the excludedModifiers parameter
	 * @param parentObject class to generate description for
	 * @param excludedModifiers bitmap of modifiers for fields you want to exclude, e.g. Modifier.FINAL | Modifier.STATIC
	 * @return Created parameter set. 
	 * @see Modifier
	 */
	public static PamParameterSet autoGenerate(Object parentObject, int excludedModifiers) {
		if (parentObject == null) {
			return null;
		}
		PamParameterSet special = checkSpecials(parentObject, excludedModifiers);
		if (special != null) {
			return special;
		}
//		if (parentObject.getClass() == BinaryStoreSettings.class) {
//			System.out.println("binary store");
//		}
//		System.out.println("Auto generate param set for " + parentObject.toString());
		ArrayList<Field> allFields = new ArrayList<>();
		PamParameterSet pps = new PamParameterSet(parentObject);
		Class<?> objClass = parentObject.getClass();
		Field[] fields = objClass.getDeclaredFields();
		allFields.addAll(Arrays.asList(fields));
		while (true) {
			objClass = objClass.getSuperclass();
			if (objClass == null) {
				break;
			}
			Field[] moreFields = objClass.getDeclaredFields();
			if (moreFields.length > 0) {
				allFields.addAll(Arrays.asList(moreFields));
			}
		}
		 Class<?>[] moreClasses = parentObject.getClass().getClasses();
		 for (int i = 0; i < moreClasses.length; i++) {
			 
		 }
		for (Field field:allFields) {
//			Field field = fields[i];
			if ((field.getModifiers()&Modifier.TRANSIENT) != 0) {
				continue;
			}
			if ((field.getModifiers()&excludedModifiers) != 0) {
				continue;
			}

			boolean isPublic = ((field.getModifiers()&Modifier.PUBLIC) != 0);
			if (isPublic) {
				pps.put(new SimplePamParameterData(parentObject, field));
			}
			else {
				Method getter = findPublicGetter(parentObject, field);
				Method setter = findPublicSetter(parentObject, field);
				if (getter != null) {
					pps.put(new PamParameterDataGetter(parentObject, field, getter, setter));
				}
				else {
					pps.hiddenFields.add(field);
				}
			}
		}
		if (pps.hiddenFields.size() > 0 && printDebug) {
			System.out.printf("Object %s-%s had %d hidden fields for which a getter could not be found\n", 
					parentObject.getClass().getName(), parentObject.toString(), pps.hiddenFields.size());
			for (Field f:pps.hiddenFields) {
				System.out.printf("\t%s - %s\n", f.getName(), f.getType().getName());
			}
		}
		
		return pps;
	}

	/**
	 * Find a getter who's got no input parameters and who's name starts with 
	 * get or is and contains the name if the field. 
	 * @param parentObject2
	 * @param field
	 */
	private static Method findPublicGetter(Object parentObject, Field field) {
		Method[] methods = parentObject.getClass().getMethods();
		if (methods == null) {
			return null;
		}
		String fieldNameLower = field.getName().toLowerCase();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];			/*
			 * Check that there are no input parameters. 
			 */
			Class<?>[] params = method.getParameterTypes();
			if (params != null && params.length > 0) {
				continue;
			}
			if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
				continue;
			}
			
			String name = method.getName();
			name = name.toLowerCase();
			int fieldInd = name.indexOf(fieldNameLower);
			int methodLen = name.length();
			int fieldLen = fieldNameLower.length();
			if (name.startsWith("is") && fieldInd == 2 && methodLen == fieldLen+2) {
				return method;
			}
			if (name.startsWith("get") && fieldInd == 3 && methodLen == fieldLen+3) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * Find a getter who's got one input parameter and void return and who's name starts with 
	 * set and contains the name if the field. 
	 * @param parentObject2
	 * @param field
	 */
	private static Method findPublicSetter(Object parentObject, Field field) {
		Method[] methods = parentObject.getClass().getMethods();
		if (methods == null) {
			return null;
		}
		String fieldNameLower = field.getName().toLowerCase();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];			/*
			 * Check that there are no input parameters. 
			 */
			Class<?>[] params = method.getParameterTypes();
			if (params != null && params.length != 1) {
				continue;
			}
			if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
				continue;
			}
			// check for void return type. 
//			Class<?> returnType = method.getReturnType();
//			if (returnType != Void.class) {
//				continue;
//			}
			
			String name = method.getName();
			name = name.toLowerCase();
			int fieldInd = name.indexOf(fieldNameLower);
			int methodLen = name.length();
			int fieldLen = fieldNameLower.length();
			
			if (name.startsWith("set") && fieldInd == 3 && methodLen == fieldLen+3) {
				return method;
			}
		}
		return null;
	}

	public static PamParameterSet checkSpecials(Object parentObject, int excludedModifiers) {
		if (parentObject.getClass() == Color.class) {
			PamParameterSet pps = new PamParameterSet(parentObject);
			pps.put(new FieldlessPamParameterData(parentObject, "Color") {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					Color col = (Color) getParentObject();
					String str = String.format("r=%d,g=%d,b=%d,a=%d", 
							col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());
					return str;
				}

				@Override
				public boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException {
					// TODO Auto-generated method stub
					return false;
				}
			});
			return pps;
		}
		return null;
	}

	public void put(PamParameterData pamParam) {
		parameterDatas.put(pamParam.getFieldName(), pamParam);
	}

	/**
	 * @return the parentObject
	 */
	public Object getParentObject() {
		return parentObject;
	}

	/**
	 * @return the parameterData list
	 * @see PamParameterData
	 */
	public Hashtable<String, PamParameterData> getParameterDatas() {
		return parameterDatas;
	}
	
	public Collection<PamParameterData> getParameterCollection() {
		return parameterDatas.values();
	}
	
	/**
	 * Find parameter data with the given name. 
	 * @param paramName
	 * @return
	 * @throws FieldNotFoundException
	 */
	public PamParameterData findParameterData(String paramName) throws FieldNotFoundException {
		PamParameterData data = parameterDatas.get(paramName);
		if (data != null) {
			return data;
		}
		else {
			throw new FieldNotFoundException(paramName);
		}
	}
	
	/**
	 * Remove a parameter data from the set
	 * @param paramName Parameter name (name of field in the owning class)
	 * @return the PamParameterData associated with that parameter name from the internal hashtable, or null if the parameter was not found
	 */
	public PamParameterData removeParameterData(String paramName) {
		return parameterDatas.remove(paramName);
	}

	/**
	 * @return the parameterSetType
	 */
	public ParameterSetType getParameterSetType() {
		return parameterSetType;
	}

	/**
	 * @param parameterSetType the parameterSetType to set
	 */
	public void setParameterSetType(ParameterSetType parameterSetType) {
		this.parameterSetType = parameterSetType;
	}

}
