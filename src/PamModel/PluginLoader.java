package PamModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import PamController.PamController;
import PamUtils.FileFinder;
import PamView.help.PamHelp;

/**
 * Plugin finder / loader based on the Code Mike O wrote within PamModel. 
 * This can be incorporated into any module that supports plugins, by making a 
 * new Plugin type that's an extension of CommonPluginInterface and adding 
 * an appropriate function into the new interface to create the class needed for the 
 * plugin. <br>
 * For a usage example see analoginput.AnalogDevicesManager
 * @author dg50
 *
 * @param <T>
 */
public class PluginLoader<T extends CommonPluginInterface> {

	private Class templateClass;
	private PluginClassloader classLoader;

	private String pluginFolder;

	public PluginLoader(Class interfaceClass) {
		templateClass = interfaceClass;
		// seems to have to use the same class loader as generated in PamModel or settings won't load.
//		classLoader = new PluginClassloader(new URL[0], this.getClass().getClassLoader());
		classLoader = PamModel.getPamModel().getClassLoader();
	}

//	/**
//	 * Work out the class of the template type. 
//	 * @return class used for the template. 
//	 */
//	private Class workoutclass() {
//		Field[] fields = this.getClass().getDeclaredFields();
//		if (fields == null) {
//			return null;
//		}
//		for (int i = 0; i < fields.length; i++) {
//			Field f = fields[i];
//			if (f.getName().equals("classInstance")) {
//				return f.getClass();
//			}
//		}
//		return null;
//	}


	/**
	 * Get a list of plugin interfaces that match the template class. 
	 * Always returns an arraylist, though it may be empty. 
	 * @return List of plugins. Never null, but maybe empty. 
	 */
	public ArrayList<T> findPlugins() {
		if (templateClass == null) {
			templateClass = PamPluginInterface.class;
		}
		ArrayList<T> pluginList = new ArrayList();

		File dir = new File(getPluginFolder());
		if (dir.exists() == false || dir.isDirectory() == false) {
			System.out.printf("PluginLoader: No plugin directory available at %s to load %s plugins\n", dir, templateClass);
			return pluginList;
		}
		System.out.printf("PluginLoader: Searching %s to load %s plugins\n", dir, templateClass);			
		List<File> jarList;
		try {
			jarList = (List<File>) FileFinder.findFileExt(dir, "jar");
		} catch (FileNotFoundException e) {
			return pluginList;
		}
		if (jarList.size()==0) {
			System.out.printf("PluginLoader: Folder %s does not contain any jar files.\n", dir);
			return pluginList;
		}
		for (File aJar : jarList) {
			findPlugins(aJar, pluginList);
		}

		return pluginList;
	}

	/**
	 * Search jar file for plugins of templateClass
	 * @param aJar
	 * @param pluginList
	 */
	private void findPlugins(File aJar, ArrayList<T> pluginList) {
		JarFile jarFile;
		try {
			jarFile = new JarFile(aJar.getAbsolutePath());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return;
		}
		Enumeration<JarEntry> e = jarFile.entries();

		// cycle through the jar file.  Load the classes found and test all interfaces
		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();
			if(je.isDirectory() || !je.getName().endsWith(".class")){
				continue;
			}

			// convert the controller class name to binary format
			String className = je.getName().substring(0,je.getName().length()-6);	// get rid of the .class at the end
			className = className.replace('/', '.');	// convert to binary file name, as required by loadClass method					

			// get the URL to look at the plugin class inside the jar file, and use the Java Reflection API
			// to add that URL to the default classloader path.
			URL newURL = null;
			try {
				newURL = aJar.toURI().toURL();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

			classLoader.addURL(newURL);

			Class c;
			try {
				c = Class.forName(className, false, classLoader);
				//				System.out.println("Class is " + c.getName());
				if (templateClass.isAssignableFrom(c)) {
					Constructor constructor = c.getDeclaredConstructor(null);
					if (c != null) {
						T pf = (T) constructor.newInstance(null);
						if (pf != null) {
							pf.setJarFile(aJar.getAbsolutePath());
							System.out.printf("PluginLoader %s adding plugin class %s\n", 
									templateClass.getName(), pf.getClass().getName());
							pluginList.add(pf);
							if (pf.getHelpSetName() != null) {
								addHelpSet(pf);
							}
						}
						else {
							System.out.printf("PluginLoader %s unable to find constructor(null) in class %s\n", 
									templateClass.getName(), c.getName());
						}
					}
				}
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}

		}


	}

	/**
	 * If the plugin has a helpset, add that to the main PAMGuard help at
	 * this point. 
	 * @param plugin
	 */
	private void addHelpSet(T plugin) {
		String hs = plugin.getHelpSetName();
		/**
		 * I don't think this is going to work since it has to get into the helpset before the
		 * masterhelpset is built !
		 * Will be OK if this is added to the help BEFORE the help is conststructed so just put
		 * into a static list of plugins for the help
		 */
		if (hs != null) {
			PamHelp.addOtherPluginHelp(plugin);
		}
	}

	/**
	 * Get the folder for plugins. By default, it will search the plugins subfolder 
	 * of the installation, but there might be times you want to set this to 
	 * something else. 
	 * @return the pluginFolder
	 */
	public String getPluginFolder() {		
		if (pluginFolder == null) {
			pluginFolder = PamController.getInstance().getInstallFolder() + PamModel.pluginsFolder;
		}
		return pluginFolder;
	}

	/**
	 * Set a folder for plugins. By default, it will search the plugins subfolder 
	 * of the installation, but there might be times you want to set this to 
	 * something else. 
	 * @param pluginFolder the pluginFolder to set
	 */
	public void setPluginFolder(String pluginFolder) {
		this.pluginFolder = pluginFolder;
	}

}
