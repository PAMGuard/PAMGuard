package performanceTests;

import java.util.Properties;

/**
 * Doesn't actually do anything, but does collate some system information
 * @author Doug Gillespie
 *
 */
public class SystemInfo implements PerformanceTest {

	private String resultString;

	@Override
	public String getName() {
		return "System Information";
	}

	@Override
	public String getResultString() {
		return resultString;
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public boolean runTest() {
		/*
		 * java.version Java Runtime Environment version 
java.vendor Java Runtime Environment vendor 
java.vendor.url Java vendor URL 
java.home Java installation directory 
java.vm.specification.version Java Virtual Machine specification version 
java.vm.specification.vendor Java Virtual Machine specification vendor 
java.vm.specification.name Java Virtual Machine specification name 
java.vm.version Java Virtual Machine implementation version 
java.vm.vendor Java Virtual Machine implementation vendor 
java.vm.name Java Virtual Machine implementation name 
java.specification.version Java Runtime Environment specification version 
java.specification.vendor Java Runtime Environment specification vendor 
java.specification.name Java Runtime Environment specification name 
java.class.version Java class format version number 
java.class.path Java class path 
java.library.path List of paths to search when loading libraries 
java.io.tmpdir Default temp file path 
java.compiler Name of JIT compiler to use 
java.ext.dirs Path of extension directory or directories 
os.name Operating system name 
os.arch Operating system architecture 
os.version Operating system version 
file.separator File separator ("/" on UNIX) 
path.separator Path separator (":" on UNIX) 
line.separator Line separator ("\n" on UNIX) 
user.name User's account name 
user.home User's home directory 
user.dir User's current working directory 

		 */

		Properties p = System.getProperties();
		resultString = "";
		appendProperty("user.name");
		appendProperty("java.version");
		appendProperty("java.vendor");
		appendProperty("java.vm.version");
		appendProperty("java.vm.name");
		appendProperty("os.name");
		appendProperty("os.arch");
		appendProperty("os.version");
		

		return true;
	}

	private void appendProperty(String key) {
		String property = System.getProperty(key);
		if (property == null) {
			appendString(String.format("%s: No such property", key));
		}
		else {
			appendString(String.format("%s: %s", key, property));
		}
	}
	
	private void appendString(String str) {
		if (resultString.length() > 0) {
			resultString += "\n";
		}
		resultString += str;
	}

}
