package PamController.pamWizard.configurations;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Rewrites the data source names held inside a module's settings, so that a
 * decimator can be inserted between sound acquisition and the modules which
 * follow it.
 * <p>
 * PAMGuard modules record their input as the name of a data block, held as a
 * string somewhere in their own settings class ({@code DecimatorParams.rawDataSource},
 * {@code FilterParams.rawDataSource}, and so on). There is no central registry of
 * these, so the only general way to repoint a module is to walk its settings
 * object and replace any string which exactly matches the old data block name.
 * <p>
 * Matching is deliberately exact. Both the short data name and the long name
 * ({@code "unit name, data name"}) are matched, since different modules store
 * different forms. Anything else is left alone.
 * <p>
 * <b>This must not be applied to every module.</b> A configuration can legitimately
 * mix sources: the SoundTrap click detector reads full rate data from sound
 * acquisition to extract stored clicks, while the FFT engine and LTSA read the
 * decimated data. Decimating the click detector's input would destroy the clicks
 * it exists to find. The caller decides which modules to rewrite - see
 * {@link PamConfigDescription#getKeepRawSourceModules()}.
 * <p>
 * Modules which record their source as an index rather than a name cannot be
 * rewritten this way. They are left untouched and picked up by the check that
 * runs after the configuration is loaded.
 *
 * @author Jamie Macaulay
 */
public class SourceNameRewriter {

	/**
	 * How deep to walk into a settings object. Settings are shallow structures; this
	 * is only here to stop a pathological object graph running away.
	 */
	private static final int MAX_DEPTH = 12;

	private final String[] oldNames;

	private final String[] newNames;

	private int replacements = 0;

	/**
	 * @param oldNames the data block names to replace, e.g. the short and long names
	 *                 of the acquisition raw data block.
	 * @param newNames the names to replace them with, in the same order.
	 */
	public SourceNameRewriter(String[] oldNames, String[] newNames) {
		if (oldNames == null || newNames == null || oldNames.length != newNames.length) {
			throw new IllegalArgumentException("SourceNameRewriter needs matching old and new name lists");
		}
		this.oldNames = oldNames;
		this.newNames = newNames;
	}

	/**
	 * Walk a settings object and replace any matching source names within it.
	 *
	 * @param settings the settings object, may be null.
	 * @return the number of strings replaced.
	 */
	public int rewrite(Object settings) {
		replacements = 0;
		if (settings != null) {
			walk(settings, 0, new IdentityHashMap<>());
		}
		return replacements;
	}

	/**
	 * The number of strings replaced by the last call to {@link #rewrite}.
	 * @return the replacement count.
	 */
	public int getReplacements() {
		return replacements;
	}

	/**
	 * Recursively walk an object, replacing matching strings in its fields.
	 *
	 * @param object the object to walk.
	 * @param depth  the current depth.
	 * @param seen   objects already visited, so that a cycle cannot loop forever.
	 */
	private void walk(Object object, int depth, IdentityHashMap<Object, Object> seen) {
		if (object == null || depth > MAX_DEPTH) {
			return;
		}
		if (seen.put(object, object) != null) {
			return; // already visited
		}

		Class<?> objectClass = object.getClass();

		if (objectClass.isArray()) {
			walkArray(object, depth, seen);
			return;
		}
		if (object instanceof Collection) {
			for (Object element : (Collection<?>) object) {
				walk(element, depth + 1, seen);
			}
			return;
		}
		if (object instanceof Map) {
			for (Object value : ((Map<?, ?>) object).values()) {
				walk(value, depth + 1, seen);
			}
			return;
		}
		if (isOpaque(objectClass)) {
			return;
		}

		// walk the class and its superclasses, since settings classes are often extended.
		for (Class<?> c = objectClass; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : declaredFields(c)) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Object value = read(field, object);
				if (value == null) {
					continue;
				}
				if (value instanceof String) {
					String replacement = replacementFor((String) value);
					if (replacement != null && !Modifier.isFinal(field.getModifiers())) {
						if (write(field, object, replacement)) {
							replacements++;
						}
					}
				}
				else {
					walk(value, depth + 1, seen);
				}
			}
		}
	}

	/**
	 * Walk an array, replacing matching strings in a string array in place and
	 * recursing into anything else.
	 */
	private void walkArray(Object array, int depth, IdentityHashMap<Object, Object> seen) {
		int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			if (element == null) {
				continue;
			}
			if (element instanceof String) {
				String replacement = replacementFor((String) element);
				if (replacement != null) {
					Array.set(array, i, replacement);
					replacements++;
				}
			}
			else {
				walk(element, depth + 1, seen);
			}
		}
	}

	/**
	 * The replacement for a string, or null if it is not one of the names being
	 * replaced.
	 */
	private String replacementFor(String value) {
		for (int i = 0; i < oldNames.length; i++) {
			if (oldNames[i] != null && oldNames[i].equals(value)) {
				return newNames[i];
			}
		}
		return null;
	}

	/**
	 * Whether a class should be treated as a leaf - primitives, boxed types and
	 * anything from the JDK. Walking into JDK internals would achieve nothing and
	 * trips over module access restrictions.
	 */
	private boolean isOpaque(Class<?> c) {
		if (c.isPrimitive() || c.isEnum()) {
			return true;
		}
		String name = c.getName();
		return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")
				|| name.startsWith("jdk.") || name.startsWith("com.sun.");
	}

	private Field[] declaredFields(Class<?> c) {
		try {
			return c.getDeclaredFields();
		}
		catch (Throwable e) {
			return new Field[0];
		}
	}

	private Object read(Field field, Object object) {
		try {
			field.setAccessible(true);
			return field.get(object);
		}
		catch (Throwable e) {
			// inaccessible field - nothing to do but leave it alone.
			return null;
		}
	}

	private boolean write(Field field, Object object, String value) {
		try {
			field.setAccessible(true);
			field.set(object, value);
			return true;
		}
		catch (Throwable e) {
			System.out.println(String.format("Unable to repoint %s.%s: %s",
					object.getClass().getSimpleName(), field.getName(), e.getMessage()));
			return false;
		}
	}
}
