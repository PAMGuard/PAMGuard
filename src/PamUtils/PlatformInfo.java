package PamUtils;
/**
 * @author Clint Blight
 * 
 */

public class PlatformInfo {
// Inspired by the article
// http://javablog.co.uk/2007/05/19/making-jni-cross-platform/
// CJB 2009-06-10

	public enum OSType {WINDOWS, MACOSX, LINUX, UNSUPPORTED}
	
	public static OSType calculateOS() {
	    String osName = System.getProperty("os.name").toLowerCase();
	    assert osName != null;
	    if (osName.startsWith("windows")) {
	        return OSType.WINDOWS;
	    }
	    if (osName.startsWith("mac os x")) {
	        return OSType.MACOSX;
	    }
	    if (osName.startsWith("linux")) {
	        return OSType.LINUX;
	    }
		System.out.println("Sorry, PAMGUARD doesn't know about your operating system");			
	    return OSType.UNSUPPORTED;
	}
	    
	public enum ARCHType {X86, X86_64,UNSUPPORTED}
	     
    public static ARCHType calculateArch() {
    	//For now only going to worry about shared libraries for 
    	//32bit Intel "X86" chips and 64bit AMD/Intel "X86_64" chips
    	//(so not Itanium IA64 style chips) CJB 2009-06-10
        String osArch = System.getProperty("os.arch").toLowerCase();
        assert osArch != null;
        if (osArch.equals("i386")) {
            return ARCHType.X86;
        }
        if (osArch.startsWith("amd64") || osArch.startsWith("x86_64")) {
            return ARCHType.X86_64;
        }
		System.out.println("Sorry, PAMGUARD doesn't know about your architecture system");			
        return ARCHType.UNSUPPORTED;
    }
    
}